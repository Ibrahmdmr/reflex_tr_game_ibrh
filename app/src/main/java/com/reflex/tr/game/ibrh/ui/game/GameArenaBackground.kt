package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold

internal data class ComboTier(
    @StringRes val labelRes: Int?,
    val glowBoost: Float,
    val scoreScale: Float
)

private data class ArenaParticle(
    val xFraction: Float,
    val yFraction: Float,
    val sizeDp: Int
)

private val ArenaParticles = List(10) { index ->
    ArenaParticle(
        xFraction = ((index * 37) % 100) / 100f,
        yFraction = ((index * 61) % 100) / 100f,
        sizeDp = 5 + index % 5 * 2
    )
}

internal fun comboTierFor(combo: Int): ComboTier {
    return when {
        combo >= 20 -> ComboTier(R.string.combo_tier_ultra, 1f, 1.42f)
        combo >= 15 -> ComboTier(R.string.combo_tier_energy, 0.82f, 1.32f)
        combo >= 10 -> ComboTier(R.string.combo_tier_explosion, 0.68f, 1.24f)
        combo >= 5 -> ComboTier(R.string.combo_tier_pulse, 0.42f, 1.14f)
        combo >= 3 -> ComboTier(R.string.combo_tier_glow, 0.24f, 1.06f)
        else -> ComboTier(null, 0f, 1f)
    }
}

@Composable
internal fun AnimatedArenaBackground(
    theme: PlayerTheme,
    combo: Int,
    isUltraMomentActive: Boolean
) {
    val spec = remember(theme) { themeVisualSpec(theme) }
    val transition = rememberInfiniteTransition(label = "arena_background")
    val drift by transition.animateFloat(
        initialValue = -0.12f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arena_drift"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arena_pulse"
    )
    val comboBoost = ((combo / 20f) + if (isUltraMomentActive) 0.16f else 0f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        spec.backgroundTop,
                        spec.primary.copy(alpha = 0.2f + comboBoost * 0.18f + pulse * if (isUltraMomentActive) 0.045f else 0f),
                        spec.backgroundBottom
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 800f * drift),
                    end = androidx.compose.ui.geometry.Offset(900f, 900f * (1f - drift))
                ),
                shape = RoundedCornerShape(PremiumPanelRadius)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val driftPx = drift * size.minDimension
            repeat(4) { index ->
                val y = size.height * (0.18f + index * 0.22f) + driftPx * (0.34f + index * 0.08f)
                drawLine(
                    color = spec.primary.copy(alpha = 0.045f + comboBoost * 0.035f),
                    start = androidx.compose.ui.geometry.Offset(-size.width * 0.18f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width * 1.18f, y - size.height * 0.2f),
                    strokeWidth = 2.5f + comboBoost * 2.5f,
                    cap = StrokeCap.Round
                )
            }
            repeat(6) { index ->
                val x = size.width * (((index * 29) % 100) / 100f) + driftPx * 0.18f
                val y = size.height * (((index * 47) % 100) / 100f) - driftPx * 0.12f
                drawCircle(
                    color = spec.secondary.copy(alpha = 0.08f + pulse * 0.1f + comboBoost * 0.08f),
                    radius = size.minDimension * (0.012f + (index % 3) * 0.005f),
                    center = Offset(x, y)
                )
            }
            ArenaParticles.forEach { particle ->
                val radius = particle.sizeDp.dp.toPx() / 2f
                val x = 24.dp.toPx() + particle.xFraction * 260.dp.toPx() + drift * 22.dp.toPx()
                val y = 18.dp.toPx() + particle.yFraction * 420.dp.toPx() - drift * 18.dp.toPx()
                val alpha = 0.14f + pulse * 0.24f + comboBoost * 0.16f
                drawCircle(
                    color = spec.secondary.copy(alpha = alpha * 0.34f),
                    radius = radius * (2.4f + comboBoost),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = spec.secondary.copy(alpha = 0.42f),
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
internal fun ComboEnergyOverlay(combo: Int) {
    if (combo < 5) return

    val tier = comboTierFor(combo)
    val alpha = (0.08f + tier.glowBoost * 0.16f).coerceAtMost(0.28f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        ArcadeGold.copy(alpha = alpha),
                        ArcadeBlue.copy(alpha = alpha * 0.42f),
                        Color.Transparent
                    )
                )
            )
    )
}
