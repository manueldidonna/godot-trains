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

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.manueldidonna.godottrains.R
import com.manueldidonna.godottrains.ThemeShapes
import com.manueldidonna.godottrains.data.models.Station
import com.manueldidonna.godottrains.ui.searchtrains.components.DateTimeInlinePicker
import kotlinx.datetime.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
            enter = fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.6f),
            exit = fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.6f)
        ) {
            LargeFloatingActionButton(
                onClick = searchOneWaySolutions,
                modifier = Modifier
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
            SearchAppBar(
                scrollBehavior = scrollBehavior,
                isConnectedToNetwork = state.isConnectedToNetwork
            )
            Column(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
                    .run { if (state.isSearchAllowed) padding(bottom = 96.dp) else this }
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                StationField(
                    label = stringResource(id = R.string.departure_station_label),
                    stationName = state.departureStation?.name,
                    onClick = searchDepartureStation,
                    recentStationSearches = state.orderedRecentStationSearches,
                    onStationSelection = setDepartureStation
                )
                StationField(
                    label = stringResource(id = R.string.arrival_station_label),
                    stationName = state.arrivalStation?.name,
                    onClick = searchArrivalStation,
                    recentStationSearches = state.orderedRecentStationSearches,
                    onStationSelection = setArrivalStation
                )
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.departure_date_label),
                            style = MaterialTheme.typography.titleSmall
                        )
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
    val scrollThreshold = with(LocalDensity.current) { 104.dp.toPx() }
    val showRecentSearches = stationName.isNullOrEmpty() && recentStationSearches.isNotEmpty()

    // The Recent searches carousel is removed from the composition when showRecentSearches = false
    // so recreate the tracker when the composable state resets
    val scrollTracker = remember(showRecentSearches) { ScrollTrackerNestedScrollConnection() }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.Place, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.titleSmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(24.dp))
                if (stationName.isNullOrEmpty()) {
                    Text(
                        text = stringResource(id = R.string.choose_station_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.graphicsLayer {
                            clip = true
                            alpha = 1f - (scrollTracker.scrolledWidth / scrollThreshold).coerceIn(
                                0f,
                                1f
                            )
                        }
                    )
                } else {
                    Text(
                        text = stationName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { onStationSelection(null) },
                        modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(id = R.string.cancel_station_selection_action),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val recentStationsAnimationSpec = remember {
                spring<IntOffset>(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showRecentSearches,
                modifier = Modifier.fillMaxSize(),
                enter = slideInHorizontally(
                    animationSpec = recentStationsAnimationSpec,
                    initialOffsetX = { it }
                ),
                exit = slideOutHorizontally(
                    animationSpec = recentStationsAnimationSpec,
                    targetOffsetX = { it }
                )
            ) {
                RecentStationsRow(
                    stations = recentStationSearches,
                    onSelection = onStationSelection,
                    nestedScrollConnection = scrollTracker
                )
            }
        }
    }
}

private class ScrollTrackerNestedScrollConnection(
    initialScrolledWidthValue: Float = 0f
) : NestedScrollConnection {
    var scrolledWidth: Float by mutableStateOf(initialScrolledWidthValue)
        private set

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        scrolledWidth -= consumed.x
        return Offset.Zero
    }
}

@Composable
private fun RecentStationsRow(
    nestedScrollConnection: NestedScrollConnection,
    stations: List<Station>,
    onSelection: (Station) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
        contentPadding = remember { PaddingValues(start = 200.dp, end = 24.dp) },
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.nestedScroll(nestedScrollConnection)
    ) {
        items(stations) { station ->
            RecentStationSearchChip(
                text = station.name,
                onClick = { onSelection(station) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.RecentStationSearchChip(text: String, onClick: () -> Unit) {
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
            .animateItemPlacement()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    isConnectedToNetwork: Boolean
) {
    Column {
        val windowInsets = LocalWindowInsets.current
        val density = LocalDensity.current
        val statusBarHeight = remember(windowInsets) {
            with(density) { windowInsets.statusBars.top.toDp() }
        }

        AnimatedVisibility(visible = !isConnectedToNetwork) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .zIndex(8f)
            ) {
                Text(
                    text = stringResource(id = R.string.no_internet_connection),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = statusBarHeight + 12.dp, bottom = 12.dp)
                )
            }
        }

        AnimatedVisibility(visible = isConnectedToNetwork) {
            Spacer(modifier = Modifier.height(statusBarHeight))
        }

        SmallTopAppBar(
            modifier = Modifier.navigationBarsPadding(bottom = false, start = true, end = true),
            title = { Text(text = stringResource(id = R.string.search_trains_app_bar_title)) },
            scrollBehavior = scrollBehavior
        )
    }
}
