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
package com.manueldidonna.godottrains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manueldidonna.godottrains.entities.OneWaySolution
import com.manueldidonna.godottrains.network.LeFrecceApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class TrainsViewModel : ViewModel() {

    // TODO: this should be a constructor dependency
    private val leFrecceApi = LeFrecceApi.Ktor

    data class State(
        val departureStationName: String? = null,
        val arrivalStationName: String? = null,
        val departureTimeInMinutes: Int = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .run { hour * 60 + minute },
        val departureDate: LocalDate = Clock.System.todayAt(TimeZone.currentSystemDefault())
    ) {
        val isSearchAllowed: Boolean
            get() = !departureStationName.isNullOrBlank()
                    && !arrivalStationName.isNullOrBlank()
                    && arrivalStationName != departureStationName
    }

    private val _stateFlow = MutableStateFlow(State())

    val stateFlow: StateFlow<State>
        get() = _stateFlow

    fun swapStationNames() {
        val state = _stateFlow.value
        _stateFlow.value = state.copy(
            departureStationName = state.arrivalStationName,
            arrivalStationName = state.departureStationName
        )
    }

    fun setArrivalStationName(stationName: String) {
        _stateFlow.value = _stateFlow.value.copy(arrivalStationName = stationName)
    }

    fun setDepartureStationName(stationName: String) {
        _stateFlow.value = _stateFlow.value.copy(departureStationName = stationName)
    }

    fun setDepartureTimeInMinutes(timeInMinutes: Int) {
        _stateFlow.value = _stateFlow.value.copy(departureTimeInMinutes = timeInMinutes)
    }

    fun setDepartureDate(localDate: LocalDate) {
        _stateFlow.value = _stateFlow.value.copy(departureDate = localDate)
    }

    suspend fun getStationNamesByQuery(query: String): List<String> {
        return leFrecceApi.getStationsByPartialName(query)
    }

    // TODO: save results to the disk with jetpack DataStore
    val recentSearchResults = flowOf(
        listOf(
            "Torre del Greco",
            "Napoli Piazza Garibaldi",
            "Napoli MonteSanto"
        )
    )

    private val oneWaySolutionsForState =
        mutableMapOf<State, MutableStateFlow<List<OneWaySolution>?>>()

    fun getOneWaySolutions(): StateFlow<List<OneWaySolution>?> {
        val currentState = _stateFlow.value
        var solutionsStateFlow = oneWaySolutionsForState[currentState]
        if (solutionsStateFlow == null) {
            solutionsStateFlow = MutableStateFlow(null)
            oneWaySolutionsForState[currentState] = solutionsStateFlow
        }
        if (solutionsStateFlow.value.isNullOrEmpty()) {
            viewModelScope.launch {
                solutionsStateFlow.value = getOneWaySolutions(
                    departureStationName = currentState.departureStationName,
                    arrivalStationName = currentState.arrivalStationName,
                    departureDateTime = getLocalDateTime(
                        localDate = currentState.departureDate,
                        timeInMinutes = currentState.departureTimeInMinutes
                    )
                )
            }
        }
        return solutionsStateFlow
    }

    suspend fun loadNextOneWaySolutions() {
        val currentState = _stateFlow.value
        val currentSolutionsFlow = oneWaySolutionsForState[currentState] ?: return
        val currentSolutions = currentSolutionsFlow.value
        if (currentSolutions.isNullOrEmpty()) return
        currentSolutionsFlow.value = currentSolutions + getOneWaySolutions(
            departureStationName = currentState.departureStationName,
            arrivalStationName = currentState.arrivalStationName,
            // we need to change the date to get the next solutions
            departureDateTime = currentSolutions.last().departureDateTime.getNextDepartureTime()
        )
    }

    private fun getLocalDateTime(localDate: LocalDate, timeInMinutes: Int): LocalDateTime {
        require(timeInMinutes >= 0)
        return LocalDateTime(
            year = localDate.year,
            month = localDate.month,
            dayOfMonth = localDate.dayOfMonth,
            hour = timeInMinutes / 60,
            minute = timeInMinutes % 60
        )
    }

    private fun LocalDateTime.getNextDepartureTime(): LocalDateTime {
        val timeZone = TimeZone.currentSystemDefault()
        return toInstant(timeZone)
            .plus(1, DateTimeUnit.MINUTE, timeZone)
            .toLocalDateTime(timeZone)
    }

    private suspend fun getOneWaySolutions(
        departureStationName: String?,
        arrivalStationName: String?,
        departureDateTime: LocalDateTime,
    ): List<OneWaySolution> {
        require(!departureStationName.isNullOrBlank())
        require(!arrivalStationName.isNullOrBlank())
        require(arrivalStationName != departureStationName)
        return leFrecceApi.getOneWaySolutions(
            departureStationName = departureStationName,
            arrivalStationName = arrivalStationName,
            firstDepartureDateTime = departureDateTime
        )
    }
}
