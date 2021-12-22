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

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.material.MaterialTheme as MaterialTheme2

object ThemeShapes {
    val Card = RoundedCornerShape(28.dp)
    val Chip = RoundedCornerShape(8.dp)
}

@Composable
fun GodotTrainsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val useDynamicColors = Build.VERSION.SDK_INT >= 31
    val colorScheme = when {
        darkTheme && useDynamicColors -> dynamicDarkColorScheme(context)
        !darkTheme && useDynamicColors -> dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme) {
        MaterialTheme2(content = content)
    }
}

@Composable
fun EdgeToEdgeContent(
    useDarkIcons: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }
    ProvideWindowInsets(content = content)
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF216c21),
    onPrimary = Color(0xFFffffff),
    primaryContainer = Color(0xFFa7f69a),
    onPrimaryContainer = Color(0xFF002201),
    secondary = Color(0xFF53634e),
    onSecondary = Color(0xFFffffff),
    secondaryContainer = Color(0xFFd6e8cd),
    onSecondaryContainer = Color(0xFF111f0f),
    tertiary = Color(0xFF386569),
    onTertiary = Color(0xFFffffff),
    tertiaryContainer = Color(0xFFbcebef),
    onTertiaryContainer = Color(0xFF002022),
    error = Color(0xFFba1b1b),
    errorContainer = Color(0xFFffdad4),
    onError = Color(0xFFffffff),
    onErrorContainer = Color(0xFF410001),
    background = Color(0xFFfdfdf7),
    onBackground = Color(0xFF1a1c19),
    surface = Color(0xFFfdfdf7),
    onSurface = Color(0xFF1a1c19),
    surfaceVariant = Color(0xFFdfe5d8),
    onSurfaceVariant = Color(0xFF42493f),
    outline = Color(0xFF73796f),
    inverseOnSurface = Color(0xFFf1f1eb),
    inverseSurface = Color(0xFF2f312d),
    inversePrimary = Color(0xFF8bd980),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8bd980),
    onPrimary = Color(0xFF003a02),
    primaryContainer = Color(0xFF00530a),
    onPrimaryContainer = Color(0xFFa7f69a),
    secondary = Color(0xFFbaccb2),
    onSecondary = Color(0xFF263423),
    secondaryContainer = Color(0xFF3c4b38),
    onSecondaryContainer = Color(0xFFd6e8cd),
    tertiary = Color(0xFFa0cfd3),
    onTertiary = Color(0xFF00363a),
    tertiaryContainer = Color(0xFF1e4d51),
    onTertiaryContainer = Color(0xFFbcebef),
    error = Color(0xFFffb4a9),
    errorContainer = Color(0xFF930006),
    onError = Color(0xFF680003),
    onErrorContainer = Color(0xFFffdad4),
    background = Color(0xFF1a1c19),
    onBackground = Color(0xFFe2e3dc),
    surface = Color(0xFF1a1c19),
    onSurface = Color(0xFFe2e3dc),
    surfaceVariant = Color(0xFF42493f),
    onSurfaceVariant = Color(0xFFc2c9bc),
    outline = Color(0xFF8c9387),
    inverseOnSurface = Color(0xFF1a1c19),
    inverseSurface = Color(0xFFe2e3dc),
    inversePrimary = Color(0xFF216c21),
)
