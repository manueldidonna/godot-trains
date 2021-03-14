package com.manueldidonna.godottrains.searchstations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun SearchStationsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        val (query, setQuery) = remember { mutableStateOf("") }
        SearchToolbar(query = query, onQueryChange = setQuery)
        RecentSearchResults()
        Divider()
        LazyColumn(contentPadding = remember { PaddingValues(top = 8.dp) }) {
            items(5) {
                SearchResultEntity(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    stationName = if(it % 2 == 0) "Torre del Greco" else "Napoli Piazza Garibaldi",
                    onClick = { /*TODO*/ }
                )
            }
        }
    }
}

@Composable
private fun SearchToolbar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colors.surface,
        elevation = AppBarDefaults.TopAppBarElevation,
        contentColor = MaterialTheme.colors.onSurface
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(56.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.padding(start = 4.dp, end = 12.dp)
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "go back")
            }

            val colors = MaterialTheme.colors
            val typography = MaterialTheme.typography
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search...",
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
                )
            }
        }
    }
}

@Composable
private fun RecentSearchResults() {
    Column {
        Text(
            text = "Recent search results",
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.padding(24.dp),
            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = remember { PaddingValues(start = 24.dp, end = 24.dp) }
        ) {
            items(4) {
                RecentSearchCarouselEntity("Torre del Greco", {})
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
