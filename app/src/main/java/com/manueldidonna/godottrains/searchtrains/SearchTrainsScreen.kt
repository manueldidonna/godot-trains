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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.manueldidonna.godottrains.R
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun SearchTrainsScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        GodotTrainsAppBar(
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(8f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 56.dp) // appbar
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(top = 24.dp, bottom = 96.dp)
        ) {
            ScreenContent()
        }

        SearchFloatingButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            onClick = { /*TODO*/ }
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
private fun ColumnScope.ScreenContent() {
    StationsDisplayCard(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
        cardElevation = 2.dp,
        cardShape = MaterialTheme.shapes.medium
    )

    Button(
        onClick = { /*TODO*/ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 48.dp)
    ) {
        Text(text = "BROWSE HISTORY")
    }

    CoilImage(
        data = R.drawable.travel_world,
        contentDescription = null,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    )

//    DepartureDateCard(
//        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
//        cardElevation = 2.dp,
//        cardShape = MaterialTheme.shapes.medium
//    )
//
//    DepartureTimeCard(
//        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
//        cardElevation = 2.dp,
//        cardShape = MaterialTheme.shapes.medium
//    )
}

@Composable
private fun SearchFloatingButton(modifier: Modifier, onClick: () -> Unit) {
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
