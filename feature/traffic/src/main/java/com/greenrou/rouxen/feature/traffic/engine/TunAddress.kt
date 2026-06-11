package com.greenrou.rouxen.feature.traffic.engine

import java.net.InetAddress

object TunAddress {
    const val ADDRESS_STRING = "10.0.0.2"
    val ADDRESS: InetAddress = InetAddress.getByName(ADDRESS_STRING)
}
