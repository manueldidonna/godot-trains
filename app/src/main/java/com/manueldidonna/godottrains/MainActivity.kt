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

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import com.manueldidonna.godottrains.searchstations.SearchStationsCallback
import com.manueldidonna.godottrains.searchstations.SearchStationsScreen
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            GodotTrainsTheme {
                EdgeToEdgeContent {
                    GodotTrains()
                }
            }
        }
    }
}

@Composable
fun GodotTrains() {
    Surface(color = MaterialTheme.colors.background) {
        val fakeSearchStationsCallback = remember {
            object : SearchStationsCallback {
                override suspend fun getStationNamesByQuery(query: String): List<String> {
                    if (query.isEmpty()) return emptyList()
                    return coroutineScope {
                        delay(400L) // delay the request
                        Log.d("station names", "scope for $query is active: $isActive")
                        delay(500L) // simulate web request delay
                        Log.d("station names", "after delay for $query")
                        return@coroutineScope List(5) { "$query $it" }
                    }
                }

                override fun selectStationByName(stationName: String) {

                }

                override val recentSearchResults = flowOf(
                    listOf(
                        "Torre del Greco",
                        "Napoli Piazza Garibaldi",
                        "Napoli MonteSanto"
                    )
                )
            }
        }
        SearchStationsScreen(fakeSearchStationsCallback)
    }
}
