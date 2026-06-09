package com.greenrou.rouxen.core.network.dns

import org.junit.Assert.assertEquals
import org.junit.Test
import org.xbill.DNS.ARecord
import org.xbill.DNS.AAAARecord
import org.xbill.DNS.CNAMERecord
import org.xbill.DNS.DClass
import org.xbill.DNS.MXRecord
import org.xbill.DNS.NSRecord
import org.xbill.DNS.Name
import org.xbill.DNS.TXTRecord
import java.net.Inet4Address
import java.net.Inet6Address

class DnsRecordMapperTest {

    @Test
    fun `ARecord maps to correct DnsRecord`() {
        val record = ARecord(
            Name.fromString("github.com."),
            DClass.IN,
            300,
            Inet4Address.getByName("140.82.112.4"),
        )
        val result = record.toDnsRecord()
        assertEquals("A", result.type)
        assertEquals("140.82.112.4", result.value)
        assertEquals(300L, result.ttl)
    }

    @Test
    fun `AAAARecord maps to IPv6 string`() {
        val record = AAAARecord(
            Name.fromString("github.com."),
            DClass.IN,
            60,
            Inet6Address.getByName("2606:50c0:8000::153"),
        )
        val result = record.toDnsRecord()
        assertEquals("AAAA", result.type)
        assertEquals("2606:50c0:8000:0:0:0:0:153", result.value)
    }

    @Test
    fun `MXRecord maps with priority prefix`() {
        val record = MXRecord(
            Name.fromString("example.com."),
            DClass.IN,
            3600,
            10,
            Name.fromString("mail.example.com."),
        )
        val result = record.toDnsRecord()
        assertEquals("MX", result.type)
        assertEquals("10 mail.example.com.", result.value)
    }

    @Test
    fun `NSRecord maps to target name`() {
        val record = NSRecord(
            Name.fromString("example.com."),
            DClass.IN,
            3600,
            Name.fromString("ns1.example.com."),
        )
        val result = record.toDnsRecord()
        assertEquals("NS", result.type)
        assertEquals("ns1.example.com.", result.value)
    }

    @Test
    fun `TXTRecord joins multiple strings`() {
        val record = TXTRecord(
            Name.fromString("example.com."),
            DClass.IN,
            3600,
            listOf("v=spf1", "include:_spf.google.com", "~all"),
        )
        val result = record.toDnsRecord()
        assertEquals("TXT", result.type)
        assertEquals("v=spf1 include:_spf.google.com ~all", result.value)
    }

    @Test
    fun `CNAMERecord maps to target`() {
        val record = CNAMERecord(
            Name.fromString("www.example.com."),
            DClass.IN,
            3600,
            Name.fromString("example.com."),
        )
        val result = record.toDnsRecord()
        assertEquals("CNAME", result.type)
        assertEquals("example.com.", result.value)
    }
}
