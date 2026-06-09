package com.greenrou.rouxen.core.network.dns

import org.xbill.DNS.ARecord
import org.xbill.DNS.AAAARecord
import org.xbill.DNS.CNAMERecord
import org.xbill.DNS.MXRecord
import org.xbill.DNS.NSRecord
import org.xbill.DNS.Record
import org.xbill.DNS.TXTRecord

fun Record.toDnsRecord(): DnsRecord {
    val type = org.xbill.DNS.Type.string(this.type)
    val value = when (this) {
        is ARecord -> address.hostAddress
        is AAAARecord -> address.hostAddress
        is MXRecord -> "${priority} ${target}"
        is NSRecord -> target.toString()
        is TXTRecord -> strings.joinToString(" ")
        is CNAMERecord -> target.toString()
        else -> rdataToString()
    }
    return DnsRecord(type = type, value = value, ttl = ttl)
}
