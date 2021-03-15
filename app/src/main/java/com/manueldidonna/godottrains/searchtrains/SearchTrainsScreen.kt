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
package com.manueldidonna.godottrains.searchtrains

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.manueldidonna.godottrains.R
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayAt

interface SearchTrainsCallback {
    val departureStationName: Flow<String?>
    val arrivalStationName: Flow<String?>
    val departureTimeInMinutes: Flow<Int>
    val departureDate: Flow<LocalDate>
    fun searchDepartureStation()
    fun searchArrivalStation()
    fun setDepartureTimeInMinutes(timeInMinutes: Int)
    fun setDepartureDate(localDate: LocalDate)
}

@Composable
fun SearchTrainsScreen(callback: SearchTrainsCallback) {
    val updatedCallback by rememberUpdatedState(callback)
    Box(modifier = Modifier.fillMaxSize()) {
        GodotTrainsAppBar(
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(8f)
        )

        val departureStationName by updatedCallback.departureStationName.collectAsState(null)
        val arrivalStationName by updatedCallback.arrivalStationName.collectAsState(null)
        val departureTimeInMinutes by updatedCallback.departureTimeInMinutes.collectAsState(0)
        val departureDate by updatedCallback.departureDate.collectAsState(
            initial = Clock.System.todayAt(TimeZone.currentSystemDefault())
        )

        val isSearchEnabled by remember {
            derivedStateOf {
                !departureStationName.isNullOrEmpty() && !arrivalStationName.isNullOrEmpty()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 56.dp) // appbar padding
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(top = 24.dp, bottom = 96.dp)
        ) {
            StationsDisplayCard(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                cardElevation = 2.dp,
                cardShape = MaterialTheme.shapes.medium,
                arrivalStationName = arrivalStationName,
                departureStationName = departureStationName,
                onArrivalStationNameClick = callback::searchArrivalStation,
                onDepartureStationNameClick = callback::searchDepartureStation
            )

            if (isSearchEnabled) {
                DepartureDateCard(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    cardElevation = 2.dp,
                    cardShape = MaterialTheme.shapes.medium,
                    selectedLocalDate = departureDate,
                    onLocalDateChange = updatedCallback::setDepartureDate
                )

                DepartureTimeCard(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    cardElevation = 2.dp,
                    cardShape = MaterialTheme.shapes.medium,
                    selectedTimeInMinutes = departureTimeInMinutes,
                    onTimeChange = updatedCallback::setDepartureTimeInMinutes
                )
            } else {
                WelcomeVectorImage(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }

        SearchFloatingButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            onClick = { /*TODO*/ },
            enabled = isSearchEnabled
        )
    }
}

@Composable
private fun GodotTrainsAppBar(modifier: Modifier = Modifier) {
    Surface(
        elevation = AppBarDefaults.TopAppBarElevation,
        color = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface,
        modifier = modifier
    ) {
        TopAppBar(
            title = { Text("Godot Trains") },
            backgroundColor = Color.Transparent,
            contentColor = LocalContentColor.current,
            elevation = 0.dp,
            modifier = Modifier.statusBarsPadding(),
        )
    }
}

@Composable
private fun WelcomeVectorImage(modifier: Modifier) {
    CoilImage(
        data = R.drawable.travel_world,
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
private fun SearchFloatingButton(modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    if (!enabled) return
    ExtendedFloatingActionButton(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 32.dp),
        text = { Text(text = "SEARCH") },
        onClick = onClick,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    )
}
