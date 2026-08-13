package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import kotlin.math.roundToInt

@Composable
internal fun ScoreFeedbackEffect(
    trigger: Int,
    combo: Int,
    timingGrade: TimingGrade?,
    flawlessStreakMilestone: Int?,
    isUltraMomentActive: Boolean,
    position: TargetPosition,
    targetSize: androidx.compose.ui.unit.Dp
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.82f) }
    val rise = remember { Animatable(0f) }
    val tier = comboTierFor(combo)
    val feedbackText = when {
        flawlessStreakMilestone != null -> stringResource(flawlessStreakMilestoneTitleRes(flawlessStreakMilestone))
        timingGrade == TimingGrade.Perfect -> stringResource(R.string.timing_perfect)
        timingGrade == TimingGrade.Great -> stringResource(R.string.timing_great)
        else -> when {
            combo >= 20 -> stringResource(R.string.combo_ultra)
            combo >= 10 -> stringResource(R.string.combo_perfect)
            combo >= 5 -> stringResource(R.string.combo_value, combo)
            combo >= 3 -> stringResource(R.string.combo_great)
            combo >= 2 -> stringResource(R.string.combo_good)
            else -> stringResource(R.string.score_feedback_plus_one)
        }
    }

    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        alpha.snapTo(0.94f)
        scale.snapTo(0.78f)
        rise.snapTo(0f)
        val timingScale = if (timingGrade == TimingGrade.Perfect || flawlessStreakMilestone != null) 1.12f else 1.06f
        val ultraScale = if (isUltraMomentActive) 1.08f else 1f
        scale.animateTo(timingScale * tier.scoreScale * ultraScale, tween(90, easing = FastOutSlowInEasing))
        rise.animateTo(-24f, tween(240, easing = FastOutSlowInEasing))
        alpha.animateTo(0f, tween(130, easing = FastOutLinearInEasing))
    }

    if (alpha.value <= 0f) return

    val density = LocalDensity.current
    val targetSizePx = with(density) { targetSize.roundToPx() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxX = (constraints.maxWidth - targetSizePx).coerceAtLeast(0)
        val maxY = (constraints.maxHeight - targetSizePx).coerceAtLeast(0)
        val feedbackX = (maxX * position.xFraction + targetSizePx * 0.32f)
            .roundToInt()
            .coerceIn(0, maxX)
        val feedbackY = (maxY * position.yFraction - targetSizePx * 0.22f + rise.value)
            .roundToInt()
            .coerceIn(0, maxY)

        Text(
            text = feedbackText,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = feedbackX,
                        y = feedbackY
                    )
                }
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                },
            style = MaterialTheme.typography.titleMedium,
            color = when {
                flawlessStreakMilestone != null -> ArcadeGold
                timingGrade == TimingGrade.Perfect -> ArcadeGold
                timingGrade == TimingGrade.Great -> ArcadeBlue
                isUltraMomentActive -> ArcadeTeal
                else -> if (combo >= 10) ArcadeGold else ReflexGamePalette.textPrimary
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@StringRes
private fun flawlessStreakMilestoneTitleRes(streak: Int): Int {
    return when (streak) {
        5 -> R.string.flawless_streak_5
        10 -> R.string.flawless_streak_10
        20 -> R.string.flawless_streak_20
        else -> R.string.flawless_streak_title
    }
}
