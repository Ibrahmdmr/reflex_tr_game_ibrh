package com.reflex.tr.game.ibrh.ui.game

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                            shape = RoundedCornerShape(PremiumPanelRadius)
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

internal fun ReflexTargetColor.toTaskColor(): Color {
    return when (this) {
        ReflexTargetColor.Red -> Color(0xFFFF335F)
        ReflexTargetColor.Blue -> Color(0xFF39A8FF)
        ReflexTargetColor.Gold -> Color(0xFFFFD84D)
        ReflexTargetColor.Teal -> Color(0xFF22F2A6)
    }
}
