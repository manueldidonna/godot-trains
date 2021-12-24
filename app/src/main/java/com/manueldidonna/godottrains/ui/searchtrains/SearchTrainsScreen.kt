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
package com.manueldidonna.godottrains.ui.searchtrains

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.manueldidonna.godottrains.ThemeShapes
import com.manueldidonna.godottrains.data.models.Station
import com.manueldidonna.godottrains.ui.searchtrains.components.DateTimeInlinePicker
import kotlinx.datetime.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTrainsScreen(
    state: SearchTrainsUiState,
    searchDepartureStation: () -> Unit,
    searchArrivalStation: () -> Unit,
    setDepartureStation: (Station?) -> Unit,
    setArrivalStation: (Station?) -> Unit,
    searchOneWaySolutions: () -> Unit,
    setDepartureDateTime: (LocalDateTime) -> Unit
) {
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.isSearchAllowed,
            modifier = Modifier
                .zIndex(8f)
                .align(Alignment.BottomEnd),
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(180))
        ) {
            LargeFloatingActionButton(
                onClick = searchOneWaySolutions, modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Column {
            SearchAppBar(scrollBehavior = scrollBehavior)
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
                    .run { if (state.isSearchAllowed) padding(bottom = 96.dp) else this }
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                StationField(
                    label = "Departure station",
                    stationName = state.departureStation?.name,
                    onClick = searchDepartureStation,
                    recentStationSearches = state.recentStationSearchesWithoutSelectedStations,
                    onStationSelection = setDepartureStation
                )
                StationField(
                    label = "Arrival station",
                    stationName = state.arrivalStation?.name,
                    onClick = searchArrivalStation,
                    recentStationSearches = state.recentStationSearchesWithoutSelectedStations,
                    onStationSelection = setArrivalStation
                )
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Departure date", style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DateTimeInlinePicker(
                        selection = state.departureDateTime,
                        onSelectionChange = setDepartureDateTime
                    )
                }

            }
        }
    }

}

@Composable
private fun StationField(
    recentStationSearches: List<Station>,
    label: String,
    stationName: String?,
    onClick: () -> Unit,
    onStationSelection: (Station?) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.Place, contentDescription = null)
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
            if (stationName.isNullOrEmpty()) {
                Text(
                    text = "Choose station",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium),
                    style = MaterialTheme.typography.bodyLarge
                )
                if (recentStationSearches.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                        modifier = Modifier.weight(1f),
                        contentPadding = remember { PaddingValues(start = 32.dp, end = 16.dp) }
                    ) {
                        items(recentStationSearches) { station ->
                            RecentStationSearchChip(
                                text = station.name,
                                onClick = { onStationSelection(station) }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = stationName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onStationSelection(null) },
                    modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel station selection",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentStationSearchChip(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .clip(ThemeShapes.Chip)
            .border(1.dp, color = MaterialTheme.colorScheme.outline, shape = ThemeShapes.Chip)
            .background(color = MaterialTheme.colorScheme.surface, shape = ThemeShapes.Chip)
            .clickable(onClick = onClick)
            .height(32.dp)
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
    )
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
