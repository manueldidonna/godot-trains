package com.manueldidonna.godottrains.data.sources

import com.manueldidonna.godottrains.RecentTrainStationSearchQueries
import com.manueldidonna.godottrains.data.models.Station
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StationsLocalDataSource @Inject constructor(
    private val recentTrainStationSearchQueries: RecentTrainStationSearchQueries
) {
    fun getRecentStationSearches(): Flow<List<Station>> {
        return recentTrainStationSearchQueries
            .getRecentStationSearches { identifier, name, shortName ->
                Station(id = identifier, name = name, shortName = shortName)
            }
            .asFlow()
            .mapToList()
    }

    suspend fun insertRecentStationSearch(station: Station) {
        withContext(Dispatchers.IO) {
            recentTrainStationSearchQueries.insertNewStationSearch(
                train_identifier = station.id,
                name = station.name,
                short_name = station.shortName
            )
        }
    }
}
