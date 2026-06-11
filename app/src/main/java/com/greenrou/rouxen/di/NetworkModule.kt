package com.greenrou.rouxen.di

import com.greenrou.rouxen.core.network.dns.DnsClient
import com.greenrou.rouxen.core.network.dns.DnsJavaClient
import com.greenrou.rouxen.core.network.http.HttpClient
import com.greenrou.rouxen.core.network.http.SslInspector
import com.greenrou.rouxen.core.network.ipinfo.IpInfoApiService
import com.greenrou.rouxen.core.network.ipinfo.IpInfoClient
import com.greenrou.rouxen.core.network.system.SystemNetworkClient
import com.greenrou.rouxen.core.network.traceroute.TracerouteClient
import com.greenrou.rouxen.core.network.whois.WhoisClient
import com.greenrou.rouxen.feature.wifi.scanner.BleScanner
import com.greenrou.rouxen.feature.wifi.scanner.WifiScanner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("http://ip-api.com/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<DnsClient> { DnsJavaClient() }
    single { HttpClient(get()) }
    single { SslInspector(get()) }
    single { SystemNetworkClient() }
    single { get<Retrofit>().create(IpInfoApiService::class.java) }
    single { IpInfoClient(get()) }
    single { WhoisClient() }
    single { TracerouteClient() }
    single { WifiScanner(androidContext()) }
    single { BleScanner(androidContext()) }
}
