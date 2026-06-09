package com.greenrou.rouxen.core.network.dns

enum class DnsRecordType(val code: Int) {
    A(1),
    AAAA(28),
    MX(15),
    NS(2),
    TXT(16),
    CNAME(5),
}
