package com.greenrou.rouxen.feature.dns

import com.greenrou.rouxen.core.network.dns.DnsRecord

sealed class DnsState {
    object Idle : DnsState()
    object Loading : DnsState()
    data class Success(val records: List<DnsRecord>) : DnsState()
    data class Error(val message: String) : DnsState()
}
