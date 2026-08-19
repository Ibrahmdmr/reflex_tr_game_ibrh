package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun LivePerformanceBar(
    uiState: GameUiState,
    livesAlertTrigger: Int
) {
    val accuracyPercent = remember(uiState.successfulHits, uiState.totalAttempts) {
        if (uiState.totalAttempts <= 0) {
            0
        } else {
            ((uiState.successfulHits * 100f) / uiState.totalAttempts)
                .toInt()
                .coerceIn(0, 100)
        }
    }
    val transition = rememberInfiniteTransition(label = "live_performance_bar")
    val comboPulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 620, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_combo_pulse"
    )
    val timePulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_time_pulse"
    )
    val livesScale = remember { Animatable(1f) }
    LaunchedEffect(livesAlertTrigger) {
        if (livesAlertTrigger <= 0) return@LaunchedEffect
        livesScale.snapTo(1.08f)
        livesScale.animateTo(1f, tween(durationMillis = 180, easing = FastOutSlowInEasing))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ReflexGamePalette.textPrimary.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LivePerformanceMetric(
                label = stringResource(R.string.score),
                value = uiState.score.coerceAtLeast(0).toString(),
                accentColor = ArcadeBlue,
                modifier = Modifier.weight(1f)
            )
            LivePerformanceMetric(
                label = stringResource(R.string.time),
                value = stringResource(R.string.seconds_short, uiState.timeLeftSeconds.coerceAtLeast(0)),
                accentColor = if (uiState.timeLeftSeconds <= 5) ArcadeCoral else ArcadeGold,
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        val scale = if (uiState.timeLeftSeconds in 1..5) timePulse else 1f
                        scaleX = scale
                        scaleY = scale
                    }
            )
            LivePerformanceMetric(
                label = stringResource(R.string.lives),
                value = uiState.lives.coerceAtLeast(0).toString(),
                accentColor = if (uiState.lives <= 1) ArcadeCoral else ArcadeTeal,
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        scaleX = livesScale.value
                        scaleY = livesScale.value
                    }
            )
            LivePerformanceMetric(
                label = stringResource(R.string.combo),
                value = stringResource(R.string.combo_short_value, uiState.combo.coerceAtLeast(0)),
                accentColor = if (uiState.combo >= 5) ArcadeGold else ArcadeBlue,
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        val scale = if (uiState.combo >= 5) comboPulse else 1f
                        scaleX = scale
                        scaleY = scale
                    }
            )
            LivePerformanceMetric(
                // Its own short label: the full word does not fit a fifth of a 360dp screen.
                label = stringResource(R.string.hud_accuracy),
                value = stringResource(R.string.percent_value, accuracyPercent),
                accentColor = ArcadeTeal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LivePerformanceMetric(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accentColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(PremiumCompactRadius),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.26f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun ActivePowerUpBadge(
    powerUp: GamePowerUp,
    consumed: Boolean
) {
    Surface(
        color = ArcadeGold.copy(alpha = if (consumed) 0.10f else 0.18f),
        shape = RoundedCornerShape(PremiumPillRadius),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = if (consumed) 0.22f else 0.42f))
    ) {
        Text(
            text = if (consumed) {
                stringResource(R.string.power_up_badge_used, stringResource(powerUp.titleRes))
            } else {
                stringResource(R.string.power_up_badge_active, stringResource(powerUp.titleRes))
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (consumed) ReflexGamePalette.textSecondary else ArcadeGold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@StringRes
internal fun firstFiveExperienceHintRes(uiState: GameUiState): Int? {
    if (
        !uiState.hasGameStarted ||
        uiState.isGameOver ||
        uiState.progressionState.totalGames >= FirstFiveExperienceGameLimit
    ) {
        return null
    }

    return when ((uiState.score / 2) % 3) {
        0 -> R.string.first_five_hint_tap_fast
        1 -> R.string.first_five_hint_keep_combo
        else -> R.string.first_five_hint_wrong_target
    }
}

@Composable
internal fun FirstFiveExperienceHint(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(PremiumChipRadius),
        color = ArcadeTeal.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.32f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = ReflexGamePalette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun ModeBadge(mode: GameMode) {
    Surface(
        color = ReflexGamePalette.neonBlue.copy(alpha = 0.18f),
        shape = RoundedCornerShape(PremiumPillRadius),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = ReflexGamePalette.neonBlue.copy(alpha = 0.28f)
        )
    ) {
        Text(
            text = stringResource(mode.titleRes),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = ReflexGamePalette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun playAreaSubtitle(uiState: GameUiState): String {
    return when (uiState.selectedMode) {
        GameMode.Classic -> stringResource(R.string.play_area_subtitle)
        GameMode.MovingTarget -> stringResource(R.string.mode_moving_play_hint)
        GameMode.FakeTarget -> stringResource(R.string.mode_fake_play_hint)
        GameMode.ColorReflex -> stringResource(
            R.string.mode_color_play_hint,
            stringResource(uiState.activeColor.labelRes)
        )
    }
}
