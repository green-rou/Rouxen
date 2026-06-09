package com.greenrou.rouxen

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.greenrou.rouxen.di.networkModule
import com.greenrou.rouxen.di.repositoryModule
import com.greenrou.rouxen.di.useCaseModule
import com.greenrou.rouxen.di.viewModelModule

class RouxenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RouxenApplication)
            modules(
                networkModule,
                repositoryModule,
                useCaseModule,
                viewModelModule,
            )
        }
    }
}
