package com.greenrou.rouxen.di

import androidx.room.Room
import com.greenrou.rouxen.feature.device.DeviceStatsRepository
import com.greenrou.rouxen.feature.dns.data.DnsRepositoryImpl
import com.greenrou.rouxen.feature.dns.domain.DnsRepository
import com.greenrou.rouxen.feature.history.db.AppDatabase
import com.greenrou.rouxen.feature.ssl.data.SslRepositoryImpl
import com.greenrou.rouxen.feature.ssl.domain.SslRepository
import com.greenrou.rouxen.feature.traffic.data.ArpTableReader
import com.greenrou.rouxen.feature.traffic.data.ConnectionRegistry
import com.greenrou.rouxen.feature.traffic.data.RateAggregator
import com.greenrou.rouxen.feature.traffic.data.ReverseDnsResolver
import com.greenrou.rouxen.feature.traffic.data.TrafficRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single<DnsRepository> { DnsRepositoryImpl(get()) }
    single<SslRepository> { SslRepositoryImpl(get(), get()) }
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "rouxen.db")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().scanDao() }
    single { DeviceStatsRepository(androidContext()) }
    single { ConnectionRegistry() }
    single { RateAggregator() }
    single { ReverseDnsResolver() }
    single { ArpTableReader() }
    single { TrafficRepository(get(), get(), androidContext()) }
}
