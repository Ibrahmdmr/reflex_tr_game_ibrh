package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.ScoreHighlightCard
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoralSoft
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
fun GameOverOverlay(
    score: Int,
    bestScore: Int,
    isNewBestScore: Boolean,
    mode: GameMode,
    maxCombo: Int,
    accuracyPercent: Int,
    reason: String?,
    showContinueButton: Boolean,
    continueButtonText: String,
    continueHelperText: String?,
    isContinueEnabled: Boolean,
    isContinueLoading: Boolean,
    onHomeClick: () -> Unit,
    onChangeModeClick: () -> Unit,
    onContinueClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recordScale by animateFloatAsState(
        targetValue = if (isNewBestScore) 1.06f else 1f,
        animationSpec = tween(durationMillis = 260),
        label = "new_record_scale"
    )
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompactHeight = maxHeight <= 720.dp
        val iconSize = if (isCompactHeight) 58.dp else 82.dp
        val spacing = if (isCompactHeight) 12.dp else 16.dp
        val panelPadding = if (isCompactHeight) 16.dp else 22.dp

        GamePanelCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight - 24.dp),
            containerColor = ReflexGamePalette.cardGlassStrong,
            contentPadding = panelPadding
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .background(ArcadeCoralSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = ArcadeCoral,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    text = stringResource(R.string.game_over_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = ReflexGamePalette.textPrimary
                )

                Text(
                    text = stringResource(mode.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = ArcadeGold,
                    textAlign = TextAlign.Center
                )

                if (isNewBestScore) {
                    Surface(
                        modifier = Modifier.graphicsLayer {
                            scaleX = recordScale
                            scaleY = recordScale
                        },
                        shape = RoundedCornerShape(999.dp),
                        color = ArcadeGold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = stringResource(R.string.new_record),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = ReflexGamePalette.textPrimary
                        )
                    }
                }

                if (!reason.isNullOrBlank()) {
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ReflexGamePalette.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScoreHighlightCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.score),
                        value = score.toString(),
                        accentColor = ArcadeCoral
                    )
                    ScoreHighlightCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.best_score),
                        value = bestScore.toString(),
                        accentColor = ArcadeBlue
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScoreHighlightCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.max_combo),
                        value = stringResource(R.string.combo_short_value, maxCombo),
                        accentColor = ArcadeGold
                    )
                    ScoreHighlightCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.accuracy),
                        value = stringResource(R.string.percent_value, accuracyPercent),
                        accentColor = ArcadeCoral
                    )
                }

                if (showContinueButton) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SecondaryGameButton(
                            text = continueButtonText,
                            enabled = isContinueEnabled,
                            isLoading = isContinueLoading,
                            onClick = onContinueClick
                        )

                        if (!continueHelperText.isNullOrBlank()) {
                            Text(
                                text = continueHelperText,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                color = ReflexGamePalette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                PrimaryGameButton(
                    text = stringResource(R.string.retry_game),
                    onClick = onRetryClick
                )

                OutlinedButton(
                    onClick = onHomeClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(text = stringResource(R.string.back_to_home))
                }

                OutlinedButton(
                    onClick = onChangeModeClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(text = stringResource(R.string.change_mode))
                }
            }
        }
    }
}
