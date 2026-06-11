package com.greenrou.rouxen.feature.traffic.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.greenrou.rouxen.feature.traffic.R
import com.greenrou.rouxen.feature.traffic.data.ConnectionRegistry
import com.greenrou.rouxen.feature.traffic.data.RateAggregator
import com.greenrou.rouxen.feature.traffic.engine.Ipv4Packet
import com.greenrou.rouxen.feature.traffic.engine.TcpHeader
import com.greenrou.rouxen.feature.traffic.engine.TcpRelay
import com.greenrou.rouxen.feature.traffic.engine.TunAddress
import com.greenrou.rouxen.feature.traffic.engine.UdpHeader
import com.greenrou.rouxen.feature.traffic.engine.UdpRelay
import org.koin.android.ext.android.inject
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

class TrafficVpnService : VpnService() {

    private val registry: ConnectionRegistry by inject()
    private val rateAggregator: RateAggregator by inject()

    private var tunFd: ParcelFileDescriptor? = null
    private var udpRelay: UdpRelay? = null
    private var tcpRelay: TcpRelay? = null

    @Volatile private var running = false
    private var pumpThread: Thread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startVpn()
            ACTION_STOP -> stopVpn()
        }
        return START_STICKY
    }

    override fun onRevoke() {
        stopVpn()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    private fun startVpn() {
        if (running) return
        TrafficVpnState.setStarting()
        startForeground(NOTIFICATION_ID, buildNotification())

        val builder = Builder()
            .setSession(SESSION_NAME)
            .addAddress(TunAddress.ADDRESS_STRING, 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("8.8.8.8")
            .setMtu(MTU)
            .setBlocking(false)

        val fd = try {
            builder.establish()
        } catch (e: Exception) {
            failStart(e.message ?: "Failed to establish VPN")
            return
        }

        if (fd == null) {
            failStart("Another VPN is active or permission was revoked")
            return
        }

        tunFd = fd
        udpRelay = UdpRelay(this, registry)
        tcpRelay = TcpRelay(this, registry)
        running = true
        TrafficVpnState.setRunning()
        pumpThread = thread(name = "TrafficPump") { pumpLoop() }
    }

    private fun failStart(reason: String) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        TrafficVpnState.setError(reason)
        stopSelf()
    }

    private fun stopVpn() {
        running = false
        pumpThread?.join(JOIN_TIMEOUT_MS)
        pumpThread = null

        runCatching { tunFd?.close() }
        tunFd = null
        udpRelay = null
        tcpRelay = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        if (TrafficVpnState.status.value !is VpnStatus.Error) {
            TrafficVpnState.setStopped()
        }
        stopSelf()
    }

    private fun pumpLoop() {
        val fd = tunFd ?: return
        val input = FileInputStream(fd.fileDescriptor)
        val output = FileOutputStream(fd.fileDescriptor)
        val readBuffer = ByteArray(MTU)
        val tunWriter: (ByteArray) -> Unit = { packet ->
            try {
                output.write(packet)
            } catch (_: IOException) {
            }
        }

        var lastSampleMs = System.currentTimeMillis()
        var lastPruneMs = lastSampleMs

        while (running) {
            val length = try {
                input.read(readBuffer)
            } catch (_: IOException) {
                -1
            }

            if (length > 0) {
                handlePacket(readBuffer, length, tunWriter)
            }

            udpRelay?.pollIncoming(tunWriter)
            tcpRelay?.pollSockets(tunWriter)

            val now = System.currentTimeMillis()
            if (now - lastSampleMs >= SAMPLE_INTERVAL_MS) {
                sampleRates(now)
                lastSampleMs = now
            }
            if (now - lastPruneMs >= PRUNE_INTERVAL_MS) {
                udpRelay?.pruneIdle(now)
                tcpRelay?.pruneInactive(now)
                lastPruneMs = now
            }

            if (length <= 0) {
                Thread.sleep(POLL_INTERVAL_MS)
            }
        }
    }

    private fun handlePacket(raw: ByteArray, length: Int, tunWriter: (ByteArray) -> Unit) {
        if (length < Ipv4Packet.HEADER_LENGTH || !Ipv4Packet.isIpv4(raw)) return
        val packet = Ipv4Packet(raw)
        if (packet.totalLength > length || packet.totalLength < Ipv4Packet.HEADER_LENGTH) return
        when (packet.protocol) {
            Ipv4Packet.PROTOCOL_UDP -> udpRelay?.onTunPacket(packet, UdpHeader(packet.payload()))
            Ipv4Packet.PROTOCOL_TCP -> tcpRelay?.onTunPacket(packet, TcpHeader(packet.payload()), tunWriter)
        }
    }

    private fun sampleRates(now: Long) {
        val byUid = registry.connectionsFlow.value.groupBy { it.uid }
        for ((uid, connections) in byUid) {
            rateAggregator.record(uid, connections.sumOf { it.rxBytes }, connections.sumOf { it.txBytes }, now)
        }
        rateAggregator.prune(byUid.keys)
    }

    private fun buildNotification(): Notification {
        val notificationManager = getSystemService(NotificationManager::class.java)!!
        notificationManager.createNotificationChannel(
            NotificationChannel(NOTIFICATION_CHANNEL_ID, "Traffic Monitor", NotificationManager.IMPORTANCE_LOW)
        )

        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, TrafficVpnService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Rouxen — Traffic Monitor")
            .setContentText("Capturing device traffic locally. No data leaves this device.")
            .setSmallIcon(R.drawable.ic_notification_traffic)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_notification_traffic, "Stop", stopIntent)
            .build()
    }

    companion object {
        const val ACTION_START = "com.greenrou.rouxen.feature.traffic.action.START"
        const val ACTION_STOP = "com.greenrou.rouxen.feature.traffic.action.STOP"

        private const val SESSION_NAME = "Rouxen Traffic Monitor"
        private const val MTU = 1500
        private const val NOTIFICATION_CHANNEL_ID = "traffic_monitor"
        private const val NOTIFICATION_ID = 1001

        private const val SAMPLE_INTERVAL_MS = 1_000L
        private const val PRUNE_INTERVAL_MS = 10_000L

        private const val POLL_INTERVAL_MS = 10L

        private const val JOIN_TIMEOUT_MS = 1_000L
    }
}
