package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TournamentDarkColorScheme = darkColorScheme(
    primary = EsportsOrange,
    onPrimary = Color.Black,
    primaryContainer = SurfaceElevatedDark,
    onPrimaryContainer = EsportsOrange,
    secondary = EsportsGold,
    onSecondary = Color.Black,
    tertiary = EsportsGreen,
    background = BgDark,
    onBackground = TextWhite,
    surface = CardDark,
    onSurface = TextWhite,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TournamentDarkColorScheme,
        typography = Typography,
        content = content
    )
}
