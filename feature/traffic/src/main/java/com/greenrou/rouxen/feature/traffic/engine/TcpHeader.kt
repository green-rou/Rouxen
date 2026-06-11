package com.greenrou.rouxen.feature.traffic.engine

import java.net.InetAddress

class TcpHeader(private val raw: ByteArray) {

    val sourcePort: Int get() = readUInt16(0)
    val destinationPort: Int get() = readUInt16(2)
    val sequenceNumber: Long get() = readUInt32(4)
    val ackNumber: Long get() = readUInt32(8)
    val dataOffset: Int get() = (raw[12].toInt() shr 4) and 0x0F
    val headerLength: Int get() = dataOffset * 4
    val window: Int get() = readUInt16(14)

    private val flagsByte: Int get() = raw[13].toInt() and 0xFF
    val fin: Boolean get() = flagsByte and FLAG_FIN != 0
    val syn: Boolean get() = flagsByte and FLAG_SYN != 0
    val rst: Boolean get() = flagsByte and FLAG_RST != 0
    val psh: Boolean get() = flagsByte and FLAG_PSH != 0
    val ack: Boolean get() = flagsByte and FLAG_ACK != 0
    val urg: Boolean get() = flagsByte and FLAG_URG != 0

    fun data(): ByteArray = raw.copyOfRange(headerLength, raw.size)

    private fun readUInt16(offset: Int): Int =
        ((raw[offset].toInt() and 0xFF) shl 8) or (raw[offset + 1].toInt() and 0xFF)

    private fun readUInt32(offset: Int): Long =
        ((raw[offset].toLong() and 0xFF) shl 24) or
            ((raw[offset + 1].toLong() and 0xFF) shl 16) or
            ((raw[offset + 2].toLong() and 0xFF) shl 8) or
            (raw[offset + 3].toLong() and 0xFF)

    companion object {
        const val FLAG_FIN = 0x01
        const val FLAG_SYN = 0x02
        const val FLAG_RST = 0x04
        const val FLAG_PSH = 0x08
        const val FLAG_ACK = 0x10
        const val FLAG_URG = 0x20
        const val HEADER_LENGTH = 20

        fun build(
            sourcePort: Int,
            destinationPort: Int,
            sequenceNumber: Long,
            ackNumber: Long,
            flags: Int,
            window: Int,
            sourceAddress: InetAddress,
            destinationAddress: InetAddress,
            data: ByteArray = ByteArray(0),
        ): ByteArray {
            val segment = ByteArray(HEADER_LENGTH + data.size)
            Ipv4Packet.writeUInt16(segment, 0, sourcePort)
            Ipv4Packet.writeUInt16(segment, 2, destinationPort)
            writeUInt32(segment, 4, sequenceNumber)
            writeUInt32(segment, 8, ackNumber)
            segment[12] = (5 shl 4).toByte()
            segment[13] = flags.toByte()
            Ipv4Packet.writeUInt16(segment, 14, window)
            System.arraycopy(data, 0, segment, HEADER_LENGTH, data.size)

            val pseudo = pseudoHeader(sourceAddress, destinationAddress, segment.size)
            val checksum = computeChecksum(pseudo, segment)
            Ipv4Packet.writeUInt16(segment, 16, checksum)
            return segment
        }

        fun pseudoHeader(source: InetAddress, destination: InetAddress, tcpLength: Int): ByteArray {
            val header = ByteArray(12)
            System.arraycopy(source.address, 0, header, 0, 4)
            System.arraycopy(destination.address, 0, header, 4, 4)
            header[8] = 0
            header[9] = Ipv4Packet.PROTOCOL_TCP.toByte()
            Ipv4Packet.writeUInt16(header, 10, tcpLength)
            return header
        }

        fun computeChecksum(pseudoHeader: ByteArray, segment: ByteArray): Int =
            Ipv4Packet.foldChecksum(
                Ipv4Packet.sumWords(pseudoHeader, 0, pseudoHeader.size) +
                    Ipv4Packet.sumWords(segment, 0, segment.size)
            )

        private fun writeUInt32(buffer: ByteArray, offset: Int, value: Long) {
            buffer[offset] = (value shr 24).toByte()
            buffer[offset + 1] = (value shr 16).toByte()
            buffer[offset + 2] = (value shr 8).toByte()
            buffer[offset + 3] = value.toByte()
        }
    }
}
