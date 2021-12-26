package com.manueldidonna.godottrains.dagger

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ServicesModule {
    @Provides
    fun providesConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService()!!
    }
}
