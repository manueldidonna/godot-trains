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
package com.manueldidonna.godottrains.searchstations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.manueldidonna.godottrains.EdgeToEdgeContent
import com.manueldidonna.godottrains.GodotTrainsTheme
import kotlinx.coroutines.flow.Flow

interface SearchStationsCallback {
    val recentSearchResults: Flow<List<String>>
    suspend fun getStationNamesByQuery(query: String): List<String>
    fun selectStationByName(stationName: String)
    fun cancelSearchAndGoBack()
}

@Composable
fun SearchStationsScreen(callback: SearchStationsCallback, searchDepartureStation: Boolean) {
    val updatedCallback by rememberUpdatedState(callback)

    Column(modifier = Modifier.fillMaxSize()) {
        val recentSearchResults by updatedCallback
            .recentSearchResults
            .collectAsState(emptyList())

        val (query, setQuery) = remember { mutableStateOf("") }

        var isLoading by remember { mutableStateOf(false) }

        val stationNames by produceState(emptyList<String>(), key1 = query) {
            isLoading = query.isNotBlank()
            value = updatedCallback.getStationNamesByQuery(query)
            isLoading = false
        }

        SearchToolbar(
            searchDepartureStation = searchDepartureStation,
            isLoading = isLoading,
            query = query,
            onQueryChange = setQuery,
            onBackArrowClick = updatedCallback::cancelSearchAndGoBack
        )

        if (recentSearchResults.isNotEmpty()) {
            RecentSearchResults(recentResults = recentSearchResults, onClick = setQuery)
            Divider()
        }

        if (stationNames.isNotEmpty())
            LazyColumn(
                contentPadding = remember { PaddingValues(top = 8.dp) },
                modifier = Modifier.navigationBarsPadding(bottom = false, start = true, end = true)
            ) {
                items(stationNames, key = { it }) { stationName ->
                    SearchResultEntity(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        stationName = stationName,
                        onClick = { updatedCallback.selectStationByName(stationName) }
                    )
                }
                item { Spacer(modifier = Modifier.navigationBarsHeight()) }
            }
    }
}

@Composable
private fun SearchToolbar(
    searchDepartureStation: Boolean,
    isLoading: Boolean,
    query: String,
    onQueryChange: (String) -> Unit = {},
    onBackArrowClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colors.surface,
        elevation = AppBarDefaults.TopAppBarElevation,
        contentColor = MaterialTheme.colors.onSurface
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding(bottom = false, start = true, end = true)
                .height(56.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackArrowClick,
                modifier = Modifier.padding(start = 4.dp, end = 12.dp)
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "go back")
            }

            val colors = MaterialTheme.colors
            val typography = MaterialTheme.typography
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text =
                        if (searchDepartureStation) "Search departure station..."
                        else "Search arrival station...",
                        style = typography.body1,
                        color = colors.onSurface.copy(alpha = ContentAlpha.medium)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = remember(typography) {
                        typography.body1.copy(color = colors.onSurface)
                    },
                    cursorBrush = remember(colors.primary) {
                        SolidColor(colors.primary)
                    },
                    singleLine = true,
                    keyboardOptions = remember {
                        KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Search)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isLoading)
                CircularProgressIndicator(
                    strokeWidth = 4.dp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .size(24.dp),
                )
        }
    }
}

@Composable
private fun RecentSearchResults(recentResults: List<String>, onClick: (String) -> Unit) {
    Column(modifier = Modifier.navigationBarsPadding(bottom = false, start = true, end = true)) {
        Text(
            text = "Recent search results",
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = remember { PaddingValues(start = 16.dp, end = 16.dp) },
        ) {
            items(recentResults) { result ->
                RecentSearchCarouselEntity(
                    query = result,
                    onClick = { onClick(result) }
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RecentSearchCarouselEntity(query: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colors
    val shape = MaterialTheme.shapes.small
    Text(
        text = query,
        style = MaterialTheme.typography.subtitle2,
        fontWeight = FontWeight.Medium,
        color = contentColorFor(colors.primary),
        modifier = Modifier
            .clip(shape)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = colors.onPrimary)
            )
            .background(color = colors.primary, shape = shape)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun SearchResultEntity(modifier: Modifier, stationName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Place,
            contentDescription = null,
            tint = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
        )
        Spacer(Modifier.width(24.dp))
        Text(text = stationName, style = MaterialTheme.typography.body1)
    }
}

@Preview
@Composable
private fun PreviewSearchToolbar() {
    GodotTrainsTheme {
        EdgeToEdgeContent {
            Surface {
                SearchToolbar(isLoading = true, query = "", searchDepartureStation = false)
            }
        }
    }
}

@Preview
@Composable
private fun PreviewRecentSearchResults() {
    GodotTrainsTheme {
        Surface {
            RecentSearchResults(
                recentResults = listOf("Torre del Greco", "Napoli P. Garibaldi"),
                onClick = {}
            )
        }
    }
}
