package com.greenrou.rouxen.di

import com.greenrou.rouxen.feature.dns.domain.GetDnsRecordsUseCase
import com.greenrou.rouxen.feature.history.domain.DeleteScanUseCase
import com.greenrou.rouxen.feature.history.domain.ExportScanUseCase
import com.greenrou.rouxen.feature.history.domain.GetScanHistoryUseCase
import com.greenrou.rouxen.feature.history.domain.SaveScanUseCase
import com.greenrou.rouxen.feature.ssl.domain.GetHttpHeadersUseCase
import com.greenrou.rouxen.feature.ssl.domain.GetSslInfoUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { GetDnsRecordsUseCase(get()) }
    factory { GetSslInfoUseCase(get()) }
    factory { GetHttpHeadersUseCase(get()) }
    factory { SaveScanUseCase(get()) }
    factory { GetScanHistoryUseCase(get()) }
    factory { DeleteScanUseCase(get()) }
    factory { ExportScanUseCase() }
}
