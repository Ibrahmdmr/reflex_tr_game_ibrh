package com.reflex.tr.game.ibrh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ArcadeBlue,
    onPrimary = ArcadeCard,
    secondary = ArcadeTeal,
    onSecondary = ArcadeDarkSurface,
    tertiary = ArcadeGold,
    background = ArcadeDarkSurface,
    onBackground = ArcadeCard,
    surface = ArcadeDarkCard,
    onSurface = ArcadeCard,
    surfaceVariant = ArcadeDarkBorder,
    onSurfaceVariant = ArcadeBlueLight,
    outline = ArcadeDarkBorder,
    error = ArcadeCoral,
    onError = ArcadeCard
)

@Composable
fun Reflex_tr_game_ibrhTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
