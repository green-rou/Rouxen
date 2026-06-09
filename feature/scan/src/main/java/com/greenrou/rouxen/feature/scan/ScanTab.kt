package com.greenrou.rouxen.feature.scan

enum class ScanTab(val route: String, val label: String) {
    DNS("dns", "DNS"),
    SSL("ssl", "SSL"),
    HEADERS("headers", "Headers"),
    PING("ping", "Ping"),
    WHOIS("whois", "Whois"),
    TRACEROUTE("traceroute", "Traceroute"),
}
