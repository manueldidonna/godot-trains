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
package com.manueldidonna.godottrains.ui.searchstation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.insets.*
import com.manueldidonna.godottrains.GodotTrainsTheme
import com.manueldidonna.godottrains.R
import com.manueldidonna.godottrains.ThemeShapes
import com.manueldidonna.godottrains.data.models.Station

@Composable
fun SearchStationScreen(
    state: SearchStationUiState,
    onNavigationUp: () -> Unit,
    searchStations: (String) -> Unit,
    onStationSelection: (Station) -> Unit
) {
    val updatedSearchStations by rememberUpdatedState(searchStations)

    val (query, setQuery) = rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    LaunchedEffect(query.text) {
        updatedSearchStations(query.text)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(state.searchResults.isEmpty()) {
                if (state.searchResults.isEmpty()) focusRequester.requestFocus()
            }

            val hintStringResource =
                if (state.searchForDepartureStation) stringResource(id = R.string.search_departure_station_hint)
                else stringResource(id = R.string.search_arrival_station_hint)

            SearchStationAppBar(
                hint = hintStringResource,
                query = query,
                onQueryChange = setQuery,
                onBackArrowClick = onNavigationUp,
                isLoading = state.isSearchingStations,
                focusRequester = focusRequester
            )

            LazyColumn(
                modifier = Modifier
                    .navigationBarsPadding(bottom = false, start = true, end = true)
                    .imePadding(),
            ) {
                item {
                    Spacer(modifier = Modifier.statusBarsHeight(additional = 64.dp))
                }

                if (state.recentSearches.isNotEmpty()) {
                    item {
                        RecentSearches(
                            recentResults = state.recentSearches,
                            onClick = onStationSelection
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(state.searchResults, key = { it.id }) { station ->
                    StationResult(
                        stationName = station.name,
                        onClick = { onStationSelection(station) }
                    )
                }
                item { Spacer(modifier = Modifier.navigationBarsHeight()) }
            }
        }
    }
}

@Composable
private fun SearchStationAppBar(
    hint: String,
    query: TextFieldValue,
    isLoading: Boolean,
    focusRequester: FocusRequester,
    onQueryChange: (TextFieldValue) -> Unit,
    onBackArrowClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shape = CircleShape,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .zIndex(8f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val contentColor = LocalContentColor.current
            IconButton(
                onClick = onBackArrowClick,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.navigate_up_action)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                if (query.text.isEmpty()) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor.copy(alpha = ContentAlpha.medium)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    keyboardOptions = remember {
                        KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Search)
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    cursorBrush = remember(contentColor) { SolidColor(contentColor) }
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = 4.dp,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentSearches(recentResults: List<Station>, onClick: (Station) -> Unit) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.recent_searches_label),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = remember { PaddingValues(horizontal = 16.dp) },
        ) {
            items(recentResults) { result ->
                RecentSearchChip(
                    text = result.name,
                    onClick = { onClick(result) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun RecentSearchChip(text: String, onClick: () -> Unit) {
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

@Composable
private fun StationResult(stationName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            tint = LocalContentColor.current
        )
        Spacer(Modifier.width(24.dp))
        Text(
            text = stationName,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview
@Composable
private fun PreviewSearchAppBar() {
    GodotTrainsTheme {
        Surface {
            SearchStationAppBar(
                hint = "Search departure station",
                query = TextFieldValue(""),
                isLoading = true,
                onQueryChange = {},
                onBackArrowClick = {},
                focusRequester = remember { FocusRequester() }
            )
        }
    }
}
