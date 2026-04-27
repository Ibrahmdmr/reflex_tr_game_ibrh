package com.reflex.tr.game.ibrh.ui.game

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import kotlin.math.roundToInt

@Composable
fun GamePlayContent(
    uiState: GameUiState,
    missFeedbackTrigger: Int,
    hitFeedbackTrigger: Int,
    hitFeedbackPosition: TargetPosition,
    onTargetTap: () -> Unit,
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
                        Text(
                            text = stringResource(R.string.play_area_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = ReflexGamePalette.textPrimary
                        )
                        Text(
                            text = stringResource(R.string.play_area_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReflexGamePalette.textSecondary
                        )
                    }
                    BestScoreBadge(bestScore = uiState.bestScore)
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            color = ReflexGamePalette.neonBlue.copy(alpha = 0.2f),
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

                        TargetMarker(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = (maxOffsetX * uiState.targetPosition.xFraction).roundToInt(),
                                        y = (maxOffsetY * uiState.targetPosition.yFraction).roundToInt()
                                    )
                                }
                                .size(targetSize),
                            onTap = onTargetTap
                        )
                    }

                    HitFeedbackEffect(
                        trigger = hitFeedbackTrigger,
                        position = hitFeedbackPosition,
                        targetSize = targetSize
                    )
                }
            }
        }
    }
}
