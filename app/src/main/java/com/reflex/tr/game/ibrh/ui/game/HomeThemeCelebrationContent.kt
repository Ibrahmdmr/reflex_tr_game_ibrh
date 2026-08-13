package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import kotlinx.coroutines.delay

private const val THEME_UNLOCK_CELEBRATION_DURATION_MS = 2_000L

@Composable
internal fun ThemeUnlockCelebration(
    theme: PlayerTheme,
    selectedLanguage: AppLanguage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = themeVisualSpec(theme)
    val title = localizedStringResource(R.string.theme_unlocked_popup_title, selectedLanguage)
    val pulse by rememberInfiniteTransition(label = "theme_unlock_glow").animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420),
            repeatMode = RepeatMode.Reverse
        ),
        label = "theme_unlock_glow_value"
    )
    val confettiProgress by rememberInfiniteTransition(label = "theme_unlock_confetti").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 820),
            repeatMode = RepeatMode.Restart
        ),
        label = "theme_unlock_confetti_value"
    )

    LaunchedEffect(theme) {
        delay(THEME_UNLOCK_CELEBRATION_DURATION_MS)
        onDismiss()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumSurfaceRadius),
        border = BorderStroke(1.dp, spec.primary.copy(alpha = 0.72f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp)
                .graphicsLayer {
                    shadowElevation = 18f + pulse * 10f
                    scaleX = 0.98f + pulse * 0.02f
                    scaleY = 0.98f + pulse * 0.02f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            spec.primary.copy(alpha = 0.28f),
                            spec.secondary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            ThemeUnlockConfetti(
                primaryColor = spec.primary,
                secondaryColor = spec.secondary,
                progress = confettiProgress,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(spec.primary.copy(alpha = 0.22f))
                        .border(1.dp, spec.primary.copy(alpha = 0.62f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.titleLarge,
                        color = spec.primary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun ThemeUnlockConfetti(
    primaryColor: Color,
    secondaryColor: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val colors = listOf(primaryColor, secondaryColor, ArcadeGold, ArcadeTeal)
        repeat(18) { index ->
            val xSeed = ((index * 37) % 100) / 100f
            val ySeed = ((index * 19) % 70) / 100f
            val x = size.width * xSeed
            val y = (size.height * (ySeed + progress * 0.82f)) % size.height
            drawCircle(
                color = colors[index % colors.size].copy(alpha = 0.78f),
                radius = (2 + index % 3).dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
