package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.BuildConfig
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import java.util.Locale
import kotlinx.coroutines.delay
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
import com.reflex.tr.game.ibrh.firebase.FirebaseParam

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
