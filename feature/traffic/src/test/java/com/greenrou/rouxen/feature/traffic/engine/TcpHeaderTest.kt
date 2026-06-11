package com.greenrou.rouxen.feature.traffic.engine

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress

class TcpHeaderTest {

    private val source: InetAddress = InetAddress.getByAddress(byteArrayOf(10, 0, 0, 2))
    private val destination: InetAddress = InetAddress.getByAddress(byteArrayOf(8, 8, 8, 8))

    @Test
    fun `build produces a self-consistent checksum`() {
        val data = "hello".toByteArray()
        val segment = TcpHeader.build(
            sourcePort = 5000,
            destinationPort = 443,
            sequenceNumber = 0x12345678L,
            ackNumber = 0xFFFFFFFFL,
            flags = TcpHeader.FLAG_SYN or TcpHeader.FLAG_ACK,
            window = 65535,
            sourceAddress = source,
            destinationAddress = destination,
            data = data,
        )

        val pseudo = TcpHeader.pseudoHeader(source, destination, segment.size)
        assertEquals(0, TcpHeader.computeChecksum(pseudo, segment))
    }

    @Test
    fun `parses fields written by build`() {
        val data = "ping".toByteArray()
        val segment = TcpHeader.build(
            sourcePort = 12345,
            destinationPort = 80,
            sequenceNumber = 1000L,
            ackNumber = 2000L,
            flags = TcpHeader.FLAG_ACK or TcpHeader.FLAG_PSH,
            window = 4096,
            sourceAddress = source,
            destinationAddress = destination,
            data = data,
        )

        val header = TcpHeader(segment)
        assertEquals(12345, header.sourcePort)
        assertEquals(80, header.destinationPort)
        assertEquals(1000L, header.sequenceNumber)
        assertEquals(2000L, header.ackNumber)
        assertEquals(4096, header.window)
        assertEquals(20, header.headerLength)
        assertTrue(header.ack)
        assertTrue(header.psh)
        assertFalse(header.syn)
        assertFalse(header.fin)
        assertFalse(header.rst)
        assertArrayEquals(data, header.data())
    }

    @Test
    fun `synAck sets syn and ack flags`() {
        val segment = TcpHeader.build(
            sourcePort = 443,
            destinationPort = 50000,
            sequenceNumber = 0L,
            ackNumber = 1L,
            flags = TcpHeader.FLAG_SYN or TcpHeader.FLAG_ACK,
            window = 65535,
            sourceAddress = source,
            destinationAddress = destination,
        )

        val header = TcpHeader(segment)
        assertTrue(header.syn)
        assertTrue(header.ack)
    }
}
