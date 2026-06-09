package com.greenrou.rouxen.core.network.ipinfo

import retrofit2.http.GET
import retrofit2.http.Path

interface IpInfoApiService {

    @GET("json/{ip}")
    suspend fun getIpInfo(@Path("ip") ip: String): IpInfoResponse
}
