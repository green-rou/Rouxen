package com.greenrou.rouxen.core.network.ipinfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

class IpInfoClient(private val service: IpInfoApiService) {

    suspend fun getInfo(host: String): Result<IpInfoResponse> = withContext(Dispatchers.IO) {
        try {
            val ip = InetAddress.getByName(host).hostAddress ?: host
            val response = service.getIpInfo(ip)
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception("ip-api returned status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
