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
import com.manueldidonna.godottrains.domain.CheckNetworkStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

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
    val isConnectedToNetwork: Boolean = true
)

val TrainsViewModelState.isSearchTrainsAllowed: Boolean
    get() = isConnectedToNetwork && departureStation != null && arrivalStation != null && arrivalStation != departureStation

@HiltViewModel
class TrainsViewModel @Inject constructor(
    private val stationsRepository: StationsRepository,
    private val solutionsRepository: SolutionsRepository,
    private val checkNetworkStatus: CheckNetworkStatusUseCase
) : ViewModel() {

    private val _viewModelState = MutableStateFlow(TrainsViewModelState())
    val viewModelState = _viewModelState.asStateFlow()

    init {
        viewModelScope.launch {
            stationsRepository.getRecentStationSearches().collect { stations ->
                _viewModelState.update { it.copy(recentStationSearches = stations) }
            }
        }
        viewModelScope.launch {
            checkNetworkStatus.isConnectedToNetwork().collect { connected ->
                _viewModelState.update { it.copy(isConnectedToNetwork = connected) }
            }
        }
    }

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
