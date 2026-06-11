package com.greenrou.rouxen.feature.traffic.engine

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class UdpHeaderTest {

    @Test
    fun `parses fields written by build`() {
        val data = "dns-query".toByteArray()
        val datagram = UdpHeader.build(sourcePort = 53000, destinationPort = 53, data = data)

        val header = UdpHeader(datagram)
        assertEquals(53000, header.sourcePort)
        assertEquals(53, header.destinationPort)
        assertEquals(UdpHeader.HEADER_LENGTH + data.size, header.length)
        assertEquals(0, header.checksum)
        assertArrayEquals(data, header.data())
    }
}
