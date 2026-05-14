package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.BestScoreBadge
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
import com.reflex.tr.game.ibrh.ui.game.components.GameStatCard
import com.reflex.tr.game.ibrh.ui.game.components.LivesStatCard
import com.reflex.tr.game.ibrh.ui.game.feedback.HitFeedbackEffect
import com.reflex.tr.game.ibrh.ui.game.feedback.MissFeedbackEffect
import com.reflex.tr.game.ibrh.ui.game.feedback.TargetMarker
import com.reflex.tr.game.ibrh.ui.game.feedback.rememberShakeTranslationX
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import kotlin.math.roundToInt

@Composable
fun GamePlayContent(
    uiState: GameUiState,
    missFeedbackTrigger: Int,
    hitFeedbackTrigger: Int,
    hitFeedbackPosition: TargetPosition,
    onTargetTap: (Long) -> Unit,
    onMissTap: () -> Unit
) {
    val missTapInteractionSource = remember { MutableInteractionSource() }
    val shakeTranslationX = rememberShakeTranslationX(trigger = missFeedbackTrigger)
    val targetSize = uiState.targetSizeDp.dp

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GameStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.score),
                value = uiState.score.toString(),
                accentColor = ArcadeBlue
            )
            LivesStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.lives),
                lives = uiState.lives,
                accentColor = ArcadeCoral,
                alertTrigger = missFeedbackTrigger
            )
            GameStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.time),
                value = stringResource(R.string.seconds_short, uiState.timeLeftSeconds),
                accentColor = ArcadeGold
            )
        }
        if (uiState.combo >= 2) {
            ComboStatusBar(combo = uiState.combo)
        }

        GamePanelCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            containerColor = ReflexGamePalette.cardGlass
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        ModeBadge(mode = uiState.selectedMode)
                        Text(
                            text = stringResource(uiState.selectedMode.arenaTitleRes),
                            style = MaterialTheme.typography.titleMedium,
                            color = ReflexGamePalette.textPrimary
                        )
                        Text(
                            text = playAreaSubtitle(uiState),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReflexGamePalette.textSecondary
                        )
                    }
                    BestScoreBadge(bestScore = uiState.bestScore)
                }

                if (uiState.selectedMode == GameMode.ColorReflex) {
                    ColorTaskBadge(activeColor = uiState.activeColor)
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    ReflexGamePalette.cardGlassStrong,
                                    Color(0xFF0B1432),
                                    Color(0xFF27155D)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = ReflexGamePalette.neonBlue.copy(
                                alpha = 0.2f + (uiState.combo / 10f).coerceIn(0f, 0.22f)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .clickable(
                            enabled = !uiState.isGameOver,
                            indication = null,
                            interactionSource = missTapInteractionSource,
                            onClick = onMissTap
                        )
                        .graphicsLayer {
                            translationX = shakeTranslationX
                        }
                ) {
                    MissFeedbackEffect(trigger = missFeedbackTrigger)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.play_area_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReflexGamePalette.textSecondary.copy(alpha = 0.78f),
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                    }

                    if (!uiState.isGameOver) {
                        val density = LocalDensity.current
                        val targetSizePx = with(density) { targetSize.roundToPx() }
                        val maxOffsetX = (constraints.maxWidth - targetSizePx).coerceAtLeast(0)
                        val maxOffsetY = (constraints.maxHeight - targetSizePx).coerceAtLeast(0)

                        uiState.targets.forEach { target ->
                            TargetMarker(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            x = (maxOffsetX * target.position.xFraction).roundToInt(),
                                            y = (maxOffsetY * target.position.yFraction).roundToInt()
                                        )
                                    }
                                    .size(targetSize),
                                targetColor = target.color,
                                role = target.role,
                                spawnKey = target.id,
                                comboLevel = uiState.combo,
                                onTap = { onTargetTap(target.id) }
                            )
                        }
                    }

                    HitFeedbackEffect(
                        trigger = hitFeedbackTrigger,
                        position = hitFeedbackPosition,
                        targetSize = targetSize
                    )
                    ScoreFeedbackEffect(
                        trigger = hitFeedbackTrigger,
                        combo = uiState.combo,
                        position = hitFeedbackPosition,
                        targetSize = targetSize
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorTaskBadge(activeColor: ReflexTargetColor) {
    val color = activeColor.toTaskColor()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .graphicsLayer {
                shadowElevation = 18f
            },
        color = color.copy(alpha = 0.26f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.82f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(color, RoundedCornerShape(999.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(999.dp))
            )
            Text(
                text = stringResource(
                    R.string.mode_color_task_badge,
                    stringResource(activeColor.labelRes).uppercase()
                ),
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.titleMedium,
                color = ReflexGamePalette.textPrimary
            )
        }
    }
}

private fun ReflexTargetColor.toTaskColor(): Color {
    return when (this) {
        ReflexTargetColor.Red -> ReflexGamePalette.targetCore
        ReflexTargetColor.Blue -> ArcadeBlue
        ReflexTargetColor.Gold -> ArcadeGold
        ReflexTargetColor.Teal -> ArcadeTeal
    }
}

@Composable
private fun ComboStatusBar(combo: Int) {
    val accent = when {
        combo >= 10 -> ArcadeGold
        combo >= 5 -> ReflexGamePalette.targetRing
        else -> ArcadeBlue
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = accent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.36f))
    ) {
        Text(
            text = stringResource(R.string.combo_value, combo),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            color = ReflexGamePalette.textPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ScoreFeedbackEffect(
    trigger: Int,
    combo: Int,
    position: TargetPosition,
    targetSize: androidx.compose.ui.unit.Dp
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.82f) }
    val rise = remember { Animatable(0f) }
    val feedbackText = when {
        combo >= 10 -> stringResource(R.string.combo_perfect)
        combo >= 5 -> stringResource(R.string.combo_value, combo)
        combo >= 2 -> stringResource(R.string.combo_great)
        else -> stringResource(R.string.score_feedback_plus_one)
    }

    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        alpha.snapTo(1f)
        scale.snapTo(0.82f)
        rise.snapTo(0f)
        scale.animateTo(1.12f, tween(110, easing = FastOutSlowInEasing))
        rise.animateTo(-34f, tween(320, easing = FastOutSlowInEasing))
        alpha.animateTo(0f, tween(170, easing = FastOutLinearInEasing))
    }

    if (alpha.value <= 0f) return

    val density = LocalDensity.current
    val targetSizePx = with(density) { targetSize.roundToPx() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxX = (constraints.maxWidth - targetSizePx).coerceAtLeast(0)
        val maxY = (constraints.maxHeight - targetSizePx).coerceAtLeast(0)

        Text(
            text = feedbackText,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (maxX * position.xFraction).roundToInt(),
                        y = (maxY * position.yFraction).roundToInt() + rise.value.roundToInt()
                    )
                }
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                },
            style = MaterialTheme.typography.titleLarge,
            color = if (combo >= 10) ArcadeGold else ReflexGamePalette.textPrimary
        )
    }
}

@Composable
private fun ModeBadge(mode: GameMode) {
    Surface(
        color = ReflexGamePalette.neonBlue.copy(alpha = 0.18f),
        shape = RoundedCornerShape(999.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = ReflexGamePalette.neonBlue.copy(alpha = 0.28f)
        )
    ) {
        Text(
            text = stringResource(mode.titleRes),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = ReflexGamePalette.textPrimary
        )
    }
}

@Composable
private fun playAreaSubtitle(uiState: GameUiState): String {
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
