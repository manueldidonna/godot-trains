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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface SearchTrainsCallback {
    val departureStationName: Flow<String>
    val arrivalStationName: Flow<String>
    val departureTimeInMinutes: Flow<Int>
    val departureDate: Flow<LocalDate>
    val isSearchAllowed: Flow<Boolean>
    fun searchDepartureStation()
    fun searchArrivalStation()
    fun swapStationNames()
    fun setDepartureTimeInMinutes(timeInMinutes: Int)
    fun setDepartureDate(localDate: LocalDate)
    fun searchOneWaySolutions()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTrainsScreen(callback: SearchTrainsCallback) {
    val updatedCallback by rememberUpdatedState(callback)
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    val isSearchAllowed by updatedCallback.isSearchAllowed.collectAsState(false)
    val departureStationName by updatedCallback.departureStationName.collectAsState("")
    val arrivalStationName by updatedCallback.arrivalStationName.collectAsState("")
    Scaffold(
        floatingActionButton = {
            if (isSearchAllowed) {
                LargeFloatingActionButton(
                    onClick = updatedCallback::searchOneWaySolutions,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        topBar = {
            SearchAppBar(scrollBehavior = scrollBehavior)
        }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            StationField(label = "Departure station", stationName = departureStationName) {
                updatedCallback.searchDepartureStation()
            }
            StationField(label = "Arrival station", stationName = arrivalStationName) {
                updatedCallback.searchArrivalStation()
            }
        }
    }

}

@Composable
private fun StationField(
    label: String,
    stationName: String,
    onClick: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.Place, contentDescription = "Location icon")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.titleSmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(24.dp))
            if (stationName.isEmpty()) {
                Text(
                    text = "Choose station",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(
                    text = stationName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAppBar(scrollBehavior: TopAppBarScrollBehavior) {
    SmallTopAppBar(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(bottom = false, start = true, end = true),
        title = { Text("Search") },
        scrollBehavior = scrollBehavior
    )
}
