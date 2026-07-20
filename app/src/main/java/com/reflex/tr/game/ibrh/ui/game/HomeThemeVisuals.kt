package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

private const val THEME_UNLOCK_CELEBRATION_DURATION_MS = 2_000L

internal fun themeAccentColor(theme: PlayerTheme): Color {
    return themeVisualSpec(theme).primary
}

internal data class ThemeVisualSpec(
    val primary: Color,
    val secondary: Color,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    @StringRes val previewLabelRes: Int
)

internal fun themeVisualSpec(theme: PlayerTheme): ThemeVisualSpec {
    return when (theme) {
        PlayerTheme.NeonRed -> ThemeVisualSpec(
            primary = ArcadeCoral,
            secondary = Color(0xFFFF7A8A),
            backgroundTop = Color(0xFF170816),
            backgroundBottom = Color(0xFF3A1022),
            previewLabelRes = R.string.theme_preview_neon
        )
        PlayerTheme.CyberBlue -> ThemeVisualSpec(
            primary = ArcadeBlue,
            secondary = Color(0xFF49F3FF),
            backgroundTop = Color(0xFF06142E),
            backgroundBottom = Color(0xFF0D3B7A),
            previewLabelRes = R.string.theme_preview_cyber
        )
        PlayerTheme.PurpleStorm -> ThemeVisualSpec(
            primary = ReflexGamePalette.neonPurple,
            secondary = Color(0xFFFF4FD8),
            backgroundTop = Color(0xFF160826),
            backgroundBottom = Color(0xFF45209B),
            previewLabelRes = R.string.theme_preview_storm
        )
        PlayerTheme.IceNeon -> ThemeVisualSpec(
            primary = Color(0xFF8DEBFF),
            secondary = Color(0xFFB9F8FF),
            backgroundTop = Color(0xFF061927),
            backgroundBottom = Color(0xFF1E6B88),
            previewLabelRes = R.string.theme_preview_ice
        )
        PlayerTheme.LavaCore -> ThemeVisualSpec(
            primary = Color(0xFFFF5A1F),
            secondary = Color(0xFFFFC857),
            backgroundTop = Color(0xFF230606),
            backgroundBottom = Color(0xFF7A210B),
            previewLabelRes = R.string.theme_preview_lava
        )
        PlayerTheme.ToxicPulse -> ThemeVisualSpec(
            primary = Color(0xFFB9FF2F),
            secondary = Color(0xFF2CFFB7),
            backgroundTop = Color(0xFF071907),
            backgroundBottom = Color(0xFF245B18),
            previewLabelRes = R.string.theme_preview_toxic
        )
        PlayerTheme.MatrixGreen -> ThemeVisualSpec(
            primary = Color(0xFF21FF72),
            secondary = Color(0xFF00D46A),
            backgroundTop = Color(0xFF020D08),
            backgroundBottom = Color(0xFF06351C),
            previewLabelRes = R.string.theme_preview_matrix
        )
        PlayerTheme.GoldFire -> ThemeVisualSpec(
            primary = ArcadeGold,
            secondary = Color(0xFFFF8A2A),
            backgroundTop = Color(0xFF1C1202),
            backgroundBottom = Color(0xFF6C3C05),
            previewLabelRes = R.string.theme_preview_gold
        )
        PlayerTheme.ShadowBlack -> ThemeVisualSpec(
            primary = Color(0xFF8A94A6),
            secondary = Color(0xFF30384A),
            backgroundTop = Color(0xFF02030A),
            backgroundBottom = Color(0xFF151827),
            previewLabelRes = R.string.theme_preview_shadow
        )
        PlayerTheme.GalaxyWave -> ThemeVisualSpec(
            primary = Color(0xFF6F8CFF),
            secondary = Color(0xFFFF5BEF),
            backgroundTop = Color(0xFF050421),
            backgroundBottom = Color(0xFF23116D),
            previewLabelRes = R.string.theme_preview_galaxy
        )
        PlayerTheme.RainbowFlux -> ThemeVisualSpec(
            primary = Color(0xFFFF4FD8),
            secondary = Color(0xFF49F3FF),
            backgroundTop = Color(0xFF15051F),
            backgroundBottom = Color(0xFF123A62),
            previewLabelRes = R.string.theme_preview_flux
        )
    }
}
