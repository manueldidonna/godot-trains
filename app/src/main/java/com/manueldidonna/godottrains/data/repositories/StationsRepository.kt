package com.manueldidonna.godottrains.data.repositories

import com.manueldidonna.godottrains.data.models.Station
import com.manueldidonna.godottrains.data.sources.StationsLocalDataSource
import com.manueldidonna.godottrains.data.sources.TrenitaliaStationsRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StationsRepository @Inject constructor(
    private val trenitaliaStationsRemoteDataSource: TrenitaliaStationsRemoteDataSource,
    private val stationsLocalDataSource: StationsLocalDataSource
) {
    suspend fun searchStations(partialName: String): List<Station> {
        if (partialName.isBlank()) return emptyList()
        return trenitaliaStationsRemoteDataSource.searchStations(partialName)
    }

    fun getRecentStationSearches(): Flow<List<Station>> {
        return stationsLocalDataSource.getRecentStationSearches()
    }

    suspend fun insertRecentStationSearchResult(station: Station) {
        stationsLocalDataSource.insertRecentStationSearch(station)
    }
}
