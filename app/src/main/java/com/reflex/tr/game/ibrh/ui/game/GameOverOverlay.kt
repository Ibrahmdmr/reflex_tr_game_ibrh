package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
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
    earnedCoins: Int,
    baseCoins: Int,
    totalCoins: Int,
    seasonXp: Int,
    isCoinDoubleClaimed: Boolean,
    showContinueButton: Boolean,
    continueButtonText: String,
    continueHelperText: String?,
    isContinueEnabled: Boolean,
    isContinueLoading: Boolean,
    onHomeClick: () -> Unit,
    onChangeModeClick: () -> Unit,
    onOpenThemeStoreClick: () -> Unit,
    isDoubleCoinsEnabled: Boolean,
    isDoubleCoinsLoading: Boolean,
    doubleCoinsText: String,
    onContinueClick: () -> Unit,
    onDoubleCoinsClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recordScale by animateFloatAsState(
        targetValue = if (isNewBestScore) 1.06f else 1f,
        animationSpec = tween(durationMillis = 260),
        label = "new_record_scale"
    )
    val scrollState = rememberScrollState()
    val isNearRecord = isNearRecordScore(
        score = score,
        bestScore = bestScore,
        isNewBestScore = isNewBestScore
    )

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompactHeight = maxHeight <= 720.dp
        val iconSize = if (isCompactHeight) 48.dp else 62.dp
        val spacing = if (isCompactHeight) 8.dp else 10.dp
        val panelPadding = if (isCompactHeight) 12.dp else 16.dp

        GamePanelCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight - 12.dp),
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
                        .background(
                            if (isNewBestScore) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        ArcadeGold.copy(alpha = 0.72f),
                                        ArcadeCoralSoft
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    colors = listOf(
                                        ArcadeCoralSoft,
                                        ArcadeCoral.copy(alpha = 0.18f)
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isNewBestScore) {
                        NewRecordConfetti()
                    }
                    Text(
                        text = if (isNewBestScore) "★" else "!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (isNewBestScore) ArcadeGold else ArcadeCoral,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    text = gameOverHeadline(
                        score = score,
                        bestScore = bestScore,
                        isNewBestScore = isNewBestScore,
                        showContinueButton = showContinueButton,
                        isNearRecord = isNearRecord
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (isNearRecord) {
                    Text(
                        text = stringResource(R.string.game_over_near_record_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArcadeGold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = stringResource(mode.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = ArcadeGold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isNewBestScore) {
                    NewRecordBadge(
                        modifier = Modifier.graphicsLayer {
                            scaleX = recordScale
                            scaleY = recordScale
                        }
                    )
                }

                if (!reason.isNullOrBlank()) {
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ReflexGamePalette.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                PerformanceSummaryGrid(
                    score = score,
                    bestScore = bestScore,
                    maxCombo = maxCombo,
                    accuracyPercent = accuracyPercent,
                    seasonXp = seasonXp
                )

                CoinEarnedCard(
                    earnedCoins = earnedCoins,
                    baseCoins = baseCoins,
                    totalCoins = totalCoins,
                    isCoinDoubleClaimed = isCoinDoubleClaimed,
                    bonusText = bonusIncludedText(
                        earnedCoins = earnedCoins,
                        baseCoins = baseCoins
                    )
                )

                PrimaryGameButton(
                    text = stringResource(R.string.retry_game),
                    onClick = onRetryClick
                )

                if (showContinueButton) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CompactRewardButton(
                            text = continueButtonText,
                            enabled = isContinueEnabled,
                            onClick = onContinueClick,
                            modifier = Modifier.fillMaxWidth(0.82f)
                        )

                        if (!continueHelperText.isNullOrBlank()) {
                            Text(
                                text = continueHelperText,
                                modifier = Modifier.fillMaxWidth(0.86f),
                                style = MaterialTheme.typography.bodySmall,
                                color = ReflexGamePalette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                CompactRewardButton(
                    text = doubleCoinsText,
                    enabled = isDoubleCoinsEnabled,
                    onClick = onDoubleCoinsClick,
                    modifier = if (showContinueButton) {
                        Modifier
                            .fillMaxWidth(0.74f)
                            .align(Alignment.CenterHorizontally)
                    } else {
                        Modifier.fillMaxWidth(0.82f)
                    }
                )

                OutlinedButton(
                    onClick = onHomeClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = stringResource(R.string.back_to_home),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun NewRecordBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = ArcadeGold.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.54f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            ArcadeGold.copy(alpha = 0.18f),
                            ArcadeCoral.copy(alpha = 0.12f),
                            ArcadeBlue.copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            NewRecordConfetti()
            Text(
                text = stringResource(R.string.new_record),
                style = MaterialTheme.typography.titleMedium,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NewRecordConfetti() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val colors = listOf(ArcadeGold, ArcadeBlue, ArcadeCoral, Color.White)
        repeat(14) { index ->
            val x = size.width * (((index * 23) % 100) / 100f)
            val y = size.height * (((index * 41) % 100) / 100f)
            drawCircle(
                color = colors[index % colors.size].copy(alpha = 0.52f),
                radius = 2.2f + (index % 3),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

@Composable
private fun PerformanceSummaryGrid(
    score: Int,
    bestScore: Int,
    maxCombo: Int,
    accuracyPercent: Int,
    seasonXp: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryMetricCard(
                title = stringResource(R.string.score),
                value = score.toString(),
                accentColor = ArcadeCoral,
                modifier = Modifier.weight(1f)
            )
            SummaryMetricCard(
                title = stringResource(R.string.best_score),
                value = bestScore.toString(),
                accentColor = ArcadeBlue,
                modifier = Modifier.weight(1f)
            )
            SummaryMetricCard(
                title = stringResource(R.string.accuracy),
                value = stringResource(R.string.percent_value, accuracyPercent),
                accentColor = ArcadeCoral,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryMetricCard(
                title = stringResource(R.string.max_combo),
                value = stringResource(R.string.combo_short_value, maxCombo),
                accentColor = ArcadeGold,
                modifier = Modifier.weight(1f)
            )
            SummaryMetricCard(
                title = stringResource(R.string.game_over_season_xp),
                value = seasonXp.toString(),
                accentColor = ArcadeGold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryMetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.13f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CompactRewardButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun gameOverHeadline(
    score: Int,
    bestScore: Int,
    isNewBestScore: Boolean,
    showContinueButton: Boolean,
    isNearRecord: Boolean
): String {
    return when {
        isNewBestScore -> stringResource(R.string.game_over_title)
        isNearRecord -> stringResource(R.string.continue_almost_record_title)
        showContinueButton -> stringResource(R.string.continue_offer_title)
        else -> stringResource(R.string.game_over_title)
    }
}

private fun isNearRecordScore(
    score: Int,
    bestScore: Int,
    isNewBestScore: Boolean
): Boolean {
    if (isNewBestScore || bestScore <= 0 || score >= bestScore) return false
    val thresholdScore = (bestScore * NEAR_RECORD_THRESHOLD_PERCENT + 99) / 100
    return score >= thresholdScore
}

private const val NEAR_RECORD_THRESHOLD_PERCENT = 80

@Composable
private fun bonusIncludedText(
    earnedCoins: Int,
    baseCoins: Int
): String? {
    val bonusCoins = (earnedCoins - baseCoins).coerceAtLeast(0)
    return if (bonusCoins > 0) stringResource(R.string.coin_bonus_included, bonusCoins) else null
}

@Composable
private fun CoinEarnedCard(
    earnedCoins: Int,
    baseCoins: Int,
    totalCoins: Int,
    isCoinDoubleClaimed: Boolean,
    bonusText: String?
) {
    val coinScale by animateFloatAsState(
        targetValue = if (earnedCoins > baseCoins || isCoinDoubleClaimed) 1.05f else 1f,
        animationSpec = tween(durationMillis = 260),
        label = "coin_earned_scale"
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = coinScale
                scaleY = coinScale
            },
        shape = RoundedCornerShape(18.dp),
        color = ArcadeGold.copy(alpha = 0.14f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(ArcadeGold.copy(alpha = 0.24f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "◉", color = ArcadeGold, style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.coins_earned_value, earnedCoins),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary
                )
                val detailText = listOfNotNull(
                    bonusText,
                    stringResource(R.string.coin_wallet_value, totalCoins)
                ).joinToString(" • ")
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (bonusText == null) ReflexGamePalette.textSecondary else ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
