/*
 * Copyright (C) 2021 Manuel Di Donna
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  he Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.manueldidonna.godottrains.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manueldidonna.godottrains.data.models.OneWaySolution
import com.manueldidonna.godottrains.data.models.Station
import com.manueldidonna.godottrains.data.repositories.SolutionsRepository
import com.manueldidonna.godottrains.data.repositories.StationsRepository
import com.manueldidonna.godottrains.data.sources.StationsLocalDataSource
import com.manueldidonna.godottrains.data.sources.TrenitaliaSolutionsRemoteDataSource
import com.manueldidonna.godottrains.data.sources.TrenitaliaStationsRemoteDataSource
import com.manueldidonna.godottrains.data.SqlDelightDatabase
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class TrainsViewModelState(
    val recentStationSearches: List<Station> = emptyList(),
    val departureStation: Station? = null,
    val arrivalStation: Station? = null,
    val departureDateTime: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()),
    val oneWaySolutions: List<OneWaySolution> = emptyList(),
    val isLoadingSolutions: Boolean = false,
    val isLoadingMoreSolutions: Boolean = false,
    val stationSearchResults: List<Station> = emptyList(),
    val isSearchingStations: Boolean = false,
)

val TrainsViewModelState.isSearchTrainsAllowed: Boolean
    get() = departureStation != null && arrivalStation != null && arrivalStation != departureStation

class TrainsViewModel : ViewModel() {

    // TODO: remove from viewmodel
    private val client = HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }

    // TODO: get repositories as dependencies
    private val stationsRepository = StationsRepository(
        trenitaliaStationsRemoteDataSource = TrenitaliaStationsRemoteDataSource(client),
        stationsLocalDataSource = StationsLocalDataSource(SqlDelightDatabase.recentTrainStationSearchQueries)
    )

    // TODO: get repositories as dependencies
    private val solutionsRepository = SolutionsRepository(
        trenitaliaSolutionsRemoteDataSource = TrenitaliaSolutionsRemoteDataSource(client)
    )

    init {
        viewModelScope.launch {
            stationsRepository.getRecentStationSearches().collect { stations ->
                _viewModelState.update { it.copy(recentStationSearches = stations) }
            }
        }
    }

    private val _viewModelState = MutableStateFlow(TrainsViewModelState())
    val viewModelState = _viewModelState.asStateFlow()

    fun setArrivalStation(station: Station?) {
        saveRecentStationSearch(station)
        _viewModelState.update {
            it.copy(arrivalStation = station)
        }
    }

    fun setDepartureStation(station: Station?) {
        saveRecentStationSearch(station)
        _viewModelState.update {
            it.copy(departureStation = station)
        }
    }

    private fun saveRecentStationSearch(station: Station?) {
        if (station == null) return
        viewModelScope.launch {
            stationsRepository.insertRecentStationSearchResult(station)
        }
    }

    fun setDepartureDateTime(dateTime: LocalDateTime) {
        _viewModelState.update {
            it.copy(departureDateTime = dateTime)
        }
    }

    private var searchStationsJob: Job? = null
    fun searchStations(partialStationName: String) {
        _viewModelState.update { it.copy(isSearchingStations = partialStationName.isNotBlank()) }
        searchStationsJob?.cancel()
        searchStationsJob = viewModelScope.launch {
            val stations = stationsRepository.searchStations(partialStationName)
            if (!this.isActive) return@launch
            _viewModelState.update {
                it.copy(isSearchingStations = false, stationSearchResults = stations)
            }
        }
    }

    fun searchOneWaySolutions() {
        val state = _viewModelState.value
        if (
            state.departureStation == null ||
            state.arrivalStation == null ||
            state.arrivalStation == state.departureStation
        ) {
            // TODO: propagate error to the UI
            return
        }
        _viewModelState.update {
            it.copy(isLoadingSolutions = true)
        }
        viewModelScope.launch {
            val solutions = solutionsRepository.getOneWaySolutions(
                departureStationId = state.departureStation.id,
                arrivalStationId = state.arrivalStation.id,
                departureDateTime = state.departureDateTime
            )
            _viewModelState.update {
                it.copy(oneWaySolutions = solutions, isLoadingSolutions = false)
            }
        }
    }

    fun loadMoreOneWaySolutions() {
        val state = _viewModelState.value
        if (
            state.departureStation == null ||
            state.arrivalStation == null ||
            state.arrivalStation == state.departureStation
        ) {
            // TODO: propagate error to the UI
            return
        }

        if (state.oneWaySolutions.isEmpty()) {
            return searchOneWaySolutions()
        }

        _viewModelState.update {
            it.copy(isLoadingMoreSolutions = true)
        }
        viewModelScope.launch {
            val solutions = solutionsRepository.getOneWaySolutions(
                departureStationId = state.departureStation.id,
                arrivalStationId = state.arrivalStation.id,
                departureDateTime = state.oneWaySolutions.last().getNextDepartureTime()
            )
            _viewModelState.update {
                it.copy(
                    oneWaySolutions = it.oneWaySolutions + solutions,
                    isLoadingMoreSolutions = false
                )
            }
        }
    }

    private fun OneWaySolution.getNextDepartureTime(): LocalDateTime {
        val timeZone = TimeZone.currentSystemDefault()
        return trains.first().departureDateTime
            .toInstant(timeZone)
            .plus(1, DateTimeUnit.MINUTE, timeZone)
            .toLocalDateTime(timeZone)
    }
}
