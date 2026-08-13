package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.BestScoreBadge
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
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
    timingGrade: TimingGrade?,
    onTargetTap: (Long) -> Unit,
    onMissTap: () -> Unit
) {
    val missTapInteractionSource = remember { MutableInteractionSource() }
    val shakeTranslationX = rememberShakeTranslationX(trigger = missFeedbackTrigger)
    val activeTheme = uiState.progressionState.activeTheme
    val targetSize = remember(uiState.targetSizeDp) { uiState.targetSizeDp.dp }
    val themeSpec = remember(activeTheme) { themeVisualSpec(activeTheme) }
    val comboTier by remember(uiState.combo) {
        derivedStateOf { comboTierFor(uiState.combo) }
    }
    val firstFiveHintRes by remember(
        uiState.hasGameStarted,
        uiState.isGameOver,
        uiState.progressionState.totalGames,
        uiState.score
    ) {
        derivedStateOf { firstFiveExperienceHintRes(uiState) }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LivePerformanceBar(
            uiState = uiState,
            livesAlertTrigger = missFeedbackTrigger
        )
        uiState.activePowerUp?.let { powerUp ->
            ActivePowerUpBadge(
                powerUp = powerUp,
                consumed = uiState.isPowerUpConsumed
            )
        }
        if (uiState.combo >= 2) {
            ComboStatusBar(combo = uiState.combo)
        }
        if (uiState.isBossRoundActive || uiState.isBossRoundResultVisible) {
            BossRoundStatusBar(
                isActive = uiState.isBossRoundActive,
                timeLeftSeconds = uiState.bossRoundTimeLeftSeconds,
                hits = if (uiState.isBossRoundActive) uiState.bossRoundHits else uiState.bossRoundResultHits,
                bonusCoins = if (uiState.isBossRoundActive) {
                    uiState.bossRoundBonusCoins
                } else {
                    uiState.bossRoundResultBonusCoins
                }
            )
        }
        if (uiState.isUltraMomentActive || uiState.isUltraMomentResultVisible) {
            UltraMomentStatusBar(
                isActive = uiState.isUltraMomentActive,
                timeLeftSeconds = uiState.ultraMomentTimeLeftSeconds,
                hits = if (uiState.isUltraMomentActive) uiState.ultraMomentHits else uiState.ultraMomentResultHits,
                bonusCoins = if (uiState.isUltraMomentActive) {
                    uiState.ultraMomentBonusCoins
                } else {
                    uiState.ultraMomentResultBonusCoins
                }
            )
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
                            color = ReflexGamePalette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = playAreaSubtitle(uiState),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReflexGamePalette.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BestScoreBadge(bestScore = uiState.bestScore)
                }

                firstFiveHintRes?.let { hintRes ->
                    Spacer(modifier = Modifier.height(10.dp))
                    FirstFiveExperienceHint(text = stringResource(hintRes))
                }

                if (uiState.selectedMode == GameMode.ColorReflex) {
                    if (firstFiveHintRes == null) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    ColorTaskBadge(activeColor = uiState.activeColor)
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Spacer(modifier = Modifier.height(if (firstFiveHintRes == null) 16.dp else 10.dp))
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = themeSpec.primary.copy(
                                alpha = 0.24f + comboTier.glowBoost * 0.38f
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
                    AnimatedArenaBackground(
                        theme = activeTheme,
                        combo = uiState.combo,
                        isUltraMomentActive = uiState.isUltraMomentActive
                    )
                    MissFeedbackEffect(trigger = missFeedbackTrigger)
                    ComboEnergyOverlay(combo = uiState.combo)

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
                        val horizontalRoomPx = (constraints.maxWidth - targetSizePx).coerceAtLeast(0)
                        val verticalRoomPx = (constraints.maxHeight - targetSizePx).coerceAtLeast(0)
                        val safeHorizontalPx = with(density) { 14.dp.roundToPx() }
                            .coerceAtMost(horizontalRoomPx / 2)
                        val safeTopPx = with(density) { 34.dp.roundToPx() }
                            .coerceAtMost(verticalRoomPx)
                        val safeBottomPx = with(density) { 16.dp.roundToPx() }
                            .coerceAtMost((verticalRoomPx - safeTopPx).coerceAtLeast(0))
                        val maxOffsetX = (horizontalRoomPx - safeHorizontalPx * 2).coerceAtLeast(0)
                        val maxOffsetY = (verticalRoomPx - safeTopPx - safeBottomPx).coerceAtLeast(0)

                        uiState.targets.forEach { target ->
                            TargetMarker(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            x = safeHorizontalPx + (maxOffsetX * target.position.xFraction).roundToInt(),
                                            y = safeTopPx + (maxOffsetY * target.position.yFraction).roundToInt()
                                        )
                                    }
                                    .size(targetSize),
                                targetColor = target.color,
                                role = target.role,
                                spawnKey = target.id,
                                comboLevel = uiState.combo + if (uiState.isUltraMomentActive) 10 else 0,
                                theme = activeTheme,
                                skin = uiState.progressionState.selectedTargetSkin,
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
                        timingGrade = timingGrade,
                        flawlessStreakMilestone = uiState.lastFlawlessStreakMilestone,
                        isUltraMomentActive = uiState.isUltraMomentActive,
                        position = hitFeedbackPosition,
                        targetSize = targetSize
                    )
                }
            }
        }
    }
}

@Composable
private fun LivePerformanceBar(
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
        shape = RoundedCornerShape(18.dp),
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
                label = stringResource(R.string.accuracy),
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
        shape = RoundedCornerShape(12.dp),
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
private fun ActivePowerUpBadge(
    powerUp: GamePowerUp,
    consumed: Boolean
) {
    Surface(
        color = ArcadeGold.copy(alpha = if (consumed) 0.10f else 0.18f),
        shape = RoundedCornerShape(999.dp),
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
private fun firstFiveExperienceHintRes(uiState: GameUiState): Int? {
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
private fun FirstFiveExperienceHint(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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

private data class ComboTier(
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

private fun comboTierFor(combo: Int): ComboTier {
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
private fun AnimatedArenaBackground(
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
                shape = RoundedCornerShape(28.dp)
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
private fun ComboEnergyOverlay(combo: Int) {
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

@Composable
private fun ColorTaskBadge(activeColor: ReflexTargetColor) {
    val color = activeColor.toTaskColor()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .graphicsLayer {
                shadowElevation = 12f
            },
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.68f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun ReflexTargetColor.toTaskColor(): Color {
    return when (this) {
        ReflexTargetColor.Red -> Color(0xFFFF335F)
        ReflexTargetColor.Blue -> Color(0xFF39A8FF)
        ReflexTargetColor.Gold -> Color(0xFFFFD84D)
        ReflexTargetColor.Teal -> Color(0xFF22F2A6)
    }
}

@Composable
private fun ComboStatusBar(combo: Int) {
    val tier = comboTierFor(combo)
    val accent = when {
        combo >= 20 -> Color(0xFFFF4FD8)
        combo >= 10 -> ArcadeGold
        combo >= 5 -> ReflexGamePalette.targetRing
        else -> ArcadeBlue
    }
    val transition = rememberInfiniteTransition(label = "combo_status_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "combo_status_scale"
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (combo >= 5) pulse else 1f
                scaleY = if (combo >= 5) pulse else 1f
                shadowElevation = 12f + tier.glowBoost * 22f
            },
        color = accent.copy(alpha = 0.14f + tier.glowBoost * 0.12f),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.36f + tier.glowBoost * 0.34f))
    ) {
        Text(
            text = if (tier.labelRes == null) {
                stringResource(R.string.combo_value, combo)
            } else {
                "${stringResource(R.string.combo_value, combo)}  •  ${stringResource(tier.labelRes)}"
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            color = ReflexGamePalette.textPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BossRoundStatusBar(
    isActive: Boolean,
    timeLeftSeconds: Int,
    hits: Int,
    bonusCoins: Int
) {
    val transition = rememberInfiniteTransition(label = "boss_round_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "boss_round_scale"
    )
    val title = if (isActive) {
        stringResource(R.string.boss_round_started)
    } else {
        stringResource(R.string.boss_round_result, hits.coerceAtLeast(0))
    }
    val subtitle = if (isActive) {
        stringResource(
            R.string.boss_round_active_summary,
            timeLeftSeconds.coerceAtLeast(0),
            hits.coerceAtLeast(0),
            bonusCoins.coerceAtLeast(0)
        )
    } else {
        stringResource(R.string.boss_round_bonus_value, bonusCoins.coerceAtLeast(0))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (isActive) pulse else 1f
                scaleY = if (isActive) pulse else 1f
                shadowElevation = if (isActive) 18f else 8f
            },
        color = ArcadeCoral.copy(alpha = if (isActive) 0.15f else 0.1f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = if (isActive) 0.46f else 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = ArcadeGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UltraMomentStatusBar(
    isActive: Boolean,
    timeLeftSeconds: Int,
    hits: Int,
    bonusCoins: Int
) {
    val transition = rememberInfiniteTransition(label = "ultra_moment_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 560, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ultra_moment_scale"
    )
    val title = if (isActive) {
        stringResource(R.string.ultra_moment_started)
    } else {
        stringResource(R.string.ultra_moment_result, hits.coerceAtLeast(0))
    }
    val subtitle = if (isActive) {
        stringResource(
            R.string.ultra_moment_active_summary,
            timeLeftSeconds.coerceAtLeast(0),
            hits.coerceAtLeast(0),
            bonusCoins.coerceAtLeast(0)
        )
    } else {
        stringResource(R.string.ultra_moment_bonus_value, bonusCoins.coerceAtLeast(0))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (isActive) pulse else 1f
                scaleY = if (isActive) pulse else 1f
                shadowElevation = if (isActive) 18f else 8f
            },
        color = ArcadeTeal.copy(alpha = if (isActive) 0.15f else 0.1f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = if (isActive) 0.46f else 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = ArcadeTeal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ScoreFeedbackEffect(
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
            color = ReflexGamePalette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
