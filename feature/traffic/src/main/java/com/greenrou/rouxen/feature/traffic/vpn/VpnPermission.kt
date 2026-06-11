package com.greenrou.rouxen.feature.traffic.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService

fun vpnPrepareIntent(context: Context): Intent? = VpnService.prepare(context)
