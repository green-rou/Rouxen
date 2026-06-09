package com.greenrou.rouxen.di

import com.greenrou.rouxen.feature.dns.DnsViewModel
import com.greenrou.rouxen.feature.history.HistoryViewModel
import com.greenrou.rouxen.feature.home.HomeViewModel
import com.greenrou.rouxen.feature.ping.PingViewModel
import com.greenrou.rouxen.feature.splash.SplashViewModel
import com.greenrou.rouxen.feature.ssl.HeadersViewModel
import com.greenrou.rouxen.feature.ssl.SslViewModel
import com.greenrou.rouxen.feature.traceroute.TracerouteViewModel
import com.greenrou.rouxen.feature.whois.WhoisViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SplashViewModel() }
    viewModel { HomeViewModel() }
    viewModel { DnsViewModel(get()) }
    viewModel { SslViewModel(get()) }
    viewModel { HeadersViewModel(get()) }
    viewModel { PingViewModel(get()) }
    viewModel { WhoisViewModel(get(), get()) }
    viewModel { TracerouteViewModel(get()) }
    viewModel { HistoryViewModel(get(), get()) }
}
