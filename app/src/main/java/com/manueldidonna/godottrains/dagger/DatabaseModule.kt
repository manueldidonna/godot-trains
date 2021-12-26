package com.manueldidonna.godottrains.dagger

import android.content.Context
import com.manueldidonna.godottrains.AppDatabase
import com.manueldidonna.godottrains.RecentTrainStationSearchQueries
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase(
            driver = AndroidSqliteDriver(
                schema = AppDatabase.Schema,
                context = context,
                name = "godot_trains.db"
            )
        )
    }

    @Provides
    fun provideRecentTrainStationSearchQueries(database: AppDatabase): RecentTrainStationSearchQueries {
        return database.recentTrainStationSearchQueries
    }
}
