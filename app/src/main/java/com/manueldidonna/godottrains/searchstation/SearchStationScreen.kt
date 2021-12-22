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
package com.manueldidonna.godottrains.searchstation

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
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.manueldidonna.godottrains.GodotTrainsTheme
import com.manueldidonna.godottrains.ThemeShapes
import kotlinx.coroutines.flow.Flow

interface SearchStationsCallback {
    val recentSearchResults: Flow<List<String>>
    suspend fun getStationNamesByQuery(query: String): List<String>
    fun selectStationByName(stationName: String)
    fun cancelSearchAndGoBack()
}

@Composable
fun SearchStationScreen(callback: SearchStationsCallback, searchDepartureStation: Boolean) {
    val updatedCallback by rememberUpdatedState(callback)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            val recentSearchResults by updatedCallback
                .recentSearchResults
                .collectAsState(emptyList())

            val (query, setQuery) = remember { mutableStateOf(TextFieldValue("")) }

            var isLoading by remember { mutableStateOf(false) }

            val stationNames by produceState(emptyList<String>(), key1 = query) {
                isLoading = query.text.isNotBlank()
                value = updatedCallback.getStationNamesByQuery(query.text)
                isLoading = false
            }

            val focusRequester = remember { FocusRequester() }
            
            LaunchedEffect(key1 = stationNames.isEmpty()) {
                if (stationNames.isEmpty()) focusRequester.requestFocus()
            }

            SearchStationAppBar(
                hint = if (searchDepartureStation) "Search departure station" else "Search arrival station",
                query = query,
                onQueryChange = setQuery,
                onBackArrowClick = updatedCallback::cancelSearchAndGoBack,
                isLoading = isLoading,
                focusRequester = focusRequester
            )

            LazyColumn(
                modifier = Modifier.navigationBarsPadding(bottom = false, start = true, end = true)
            ) {
                if (recentSearchResults.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        RecentSearches(recentResults = recentSearchResults, onClick = { text ->
                            setQuery(TextFieldValue(text, selection = TextRange(text.length)))
                        })
                    }
                }
                items(stationNames, key = { it }) { stationName ->
                    StationResult(
                        stationName = stationName,
                        onClick = { updatedCallback.selectStationByName(stationName) }
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
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .clip(CircleShape),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackArrowClick,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
            }
            Box(modifier = Modifier.weight(1f)) {
                if (query.text.isEmpty()) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium)
                    )
                }
                val cursorColor = MaterialTheme.colorScheme.onSurfaceVariant
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    keyboardOptions = remember {
                        KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Search)
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    cursorBrush = remember(cursorColor) { SolidColor(cursorColor) }
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun RecentSearches(recentResults: List<String>, onClick: (String) -> Unit) {
    Column {
        Text(
            text = "Recent searches",
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
                    text = result,
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
        fontWeight = FontWeight.Medium,
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
private fun PreviewRecentSearches() {
    GodotTrainsTheme {
        Surface {
            RecentSearches(
                recentResults = listOf("Torre del Greco", "Napoli P. Garibaldi"),
                onClick = {}
            )
        }
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
