package com.greenrou.rouxen.core.network.ipinfo

import com.google.gson.annotations.SerializedName

data class IpInfoResponse(
    @SerializedName("query") val ip: String,
    @SerializedName("country") val country: String,
    @SerializedName("regionName") val region: String,
    @SerializedName("city") val city: String,
    @SerializedName("isp") val isp: String,
    @SerializedName("org") val org: String,
    @SerializedName("as") val asNumber: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("status") val status: String,
)
