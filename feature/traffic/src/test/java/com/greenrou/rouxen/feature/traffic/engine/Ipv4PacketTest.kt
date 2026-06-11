package com.greenrou.rouxen.feature.traffic.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress

class Ipv4PacketTest {

    @Test
    fun `header checksum is self-consistent`() {
        val header = ByteArray(20)
        header[0] = 0x45
        Ipv4Packet.writeUInt16(header, 2, 20)
        header[8] = 64
        header[9] = Ipv4Packet.PROTOCOL_UDP.toByte()
        System.arraycopy(byteArrayOf(10, 0, 0, 2), 0, header, 12, 4)
        System.arraycopy(byteArrayOf(8, 8, 8, 8), 0, header, 16, 4)

        val checksum = Ipv4Packet.checksum(header, 0, 20)
        Ipv4Packet.writeUInt16(header, 10, checksum)

        assertEquals(0, Ipv4Packet.checksum(header, 0, 20))
    }

    @Test
    fun `buildResponse swaps addresses and recomputes checksum`() {
        val original = ByteArray(24) // 20-byte header + 4-byte payload
        original[0] = 0x45
        Ipv4Packet.writeUInt16(original, 2, 24)
        original[9] = Ipv4Packet.PROTOCOL_UDP.toByte()
        System.arraycopy(byteArrayOf(10, 0, 0, 2), 0, original, 12, 4) // TUN address
        System.arraycopy(byteArrayOf(8, 8, 8, 8), 0, original, 16, 4) // remote address
        val packet = Ipv4Packet(original)

        val responsePayload = byteArrayOf(1, 2, 3, 4, 5, 6)
        val response = packet.buildResponse(responsePayload)
        val responsePacket = Ipv4Packet(response)

        assertEquals(InetAddress.getByAddress(byteArrayOf(8, 8, 8, 8)), responsePacket.sourceAddress)
        assertEquals(InetAddress.getByAddress(byteArrayOf(10, 0, 0, 2)), responsePacket.destinationAddress)
        assertEquals(Ipv4Packet.PROTOCOL_UDP, responsePacket.protocol)
        assertEquals(20 + responsePayload.size, responsePacket.totalLength)
        assertTrue(responsePayload.contentEquals(responsePacket.payload()))
        assertEquals(0, Ipv4Packet.checksum(response, 0, 20))
    }

    @Test
    fun `isIpv4 detects version nibble`() {
        assertTrue(Ipv4Packet.isIpv4(byteArrayOf(0x45)))
        assertTrue(!Ipv4Packet.isIpv4(byteArrayOf(0x60)))
        assertTrue(!Ipv4Packet.isIpv4(ByteArray(0)))
    }
}
