package com.greenrou.rouxen.di

import androidx.room.Room
import com.greenrou.rouxen.feature.dns.data.DnsRepositoryImpl
import com.greenrou.rouxen.feature.dns.domain.DnsRepository
import com.greenrou.rouxen.feature.history.db.AppDatabase
import com.greenrou.rouxen.feature.ssl.data.SslRepositoryImpl
import com.greenrou.rouxen.feature.ssl.domain.SslRepository
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
}
