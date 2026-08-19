package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
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
    maxFlawlessStreak: Int,
    bossRoundBonusCoins: Int,
    ultraMomentBonusCoins: Int,
    ultraMomentHits: Int,
    perfectHits: Int,
    greatHits: Int,
    accuracyPercent: Int,
    newPersonalRecords: Set<PersonalRecordType>,
    unlockedProfileBadges: Set<ProfileBadge>,
    reason: String?,
    earnedCoins: Int,
    baseCoins: Int,
    totalCoins: Int,
    seasonXp: Int,
    comboChallenge: ComboChallengeState,
    dailyEvent: DailyEventState,
    leaguePointsEarned: Int,
    leagueUpgradedTo: LeagueTier?,
    dailyMiniTournament: DailyMiniTournamentState,
    rewardChestEarned: RewardChestType?,
    newPlayerTitles: List<PlayerTitle>,
    starterJourney: StarterJourneyState,
    starterTaskCompletedThisGame: Boolean,
    isCoinDoubleClaimed: Boolean,
    showContinueButton: Boolean,
    continueButtonText: String,
    continueHelperText: String?,
    isContinueEnabled: Boolean,
    isContinueLoading: Boolean,
    onHomeClick: () -> Unit,
    isDoubleCoinsEnabled: Boolean,
    isDoubleCoinsLoading: Boolean,
    doubleCoinsText: String,
    onContinueClick: () -> Unit,
    onDoubleCoinsClick: () -> Unit,
    onShareScoreClick: () -> Unit,
    onRewardChestOpenClick: () -> Unit,
    shareScoreButtonText: String,
    isSharingScore: Boolean,
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
                .heightIn(max = maxHeight - 12.dp)
                .navigationBarsPadding(),
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

                Text(
                    text = score.toString(),
                    style = if (isCompactHeight) {
                        MaterialTheme.typography.displaySmall
                    } else {
                        MaterialTheme.typography.displayMedium
                    },
                    color = if (isNewBestScore) ArcadeGold else ReflexGamePalette.textPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = stringResource(R.string.score),
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
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

                if (newPersonalRecords.isNotEmpty()) {
                    NewPersonalRecordsCard(
                        records = newPersonalRecords,
                        modifier = Modifier.graphicsLayer {
                            scaleX = recordScale
                            scaleY = recordScale
                        }
                    )
                }

                if (unlockedProfileBadges.isNotEmpty()) {
                    UnlockedProfileBadgesCard(badges = unlockedProfileBadges)
                }

                NewPlayerTitlesCard(titles = newPlayerTitles)

                StarterJourneyGameOverNote(
                    state = starterJourney,
                    taskCompletedThisGame = starterTaskCompletedThisGame
                )

                // Run-critical actions stay above the summary cards so they clear the fold.
                PrimaryGameButton(
                    text = stringResource(R.string.retry_game),
                    onClick = onRetryClick
                )

                SecondaryGameButton(
                    text = shareScoreButtonText,
                    onClick = onShareScoreClick,
                    enabled = !isSharingScore,
                    isLoading = isSharingScore
                )

                if (showContinueButton) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SecondaryGameButton(
                            text = continueButtonText,
                            onClick = onContinueClick,
                            enabled = isContinueEnabled,
                            isLoading = isContinueLoading
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
                    maxFlawlessStreak = maxFlawlessStreak,
                    bossRoundBonusCoins = bossRoundBonusCoins,
                    ultraMomentBonusCoins = ultraMomentBonusCoins,
                    ultraMomentHits = ultraMomentHits,
                    perfectHits = perfectHits,
                    greatHits = greatHits,
                    accuracyPercent = accuracyPercent,
                    seasonXp = seasonXp
                )

                SmartGameSuggestionCard(
                    suggestionRes = smartGameSuggestionRes(
                        score = score,
                        bestScore = bestScore,
                        isNewBestScore = isNewBestScore,
                        maxCombo = maxCombo,
                        accuracyPercent = accuracyPercent
                    )
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

                rewardChestEarned?.let { chest ->
                    RewardChestGameOverCard(
                        type = chest,
                        onOpenClick = onRewardChestOpenClick
                    )
                }

                ComboChallengeProgressCard(state = comboChallenge)

                // At most two progress lines; the rest is a link so the panel does not grow a
                // list of everything that moved this run.
                val progressLines = buildList {
                    leagueUpgradedTo?.let {
                        add(stringResource(R.string.weekly_league_upgraded_value, stringResource(it.titleRes)))
                    }
                    if (dailyEvent.completed && dailyEvent.progress > 0) {
                        add(stringResource(R.string.daily_event_game_over_completed))
                    }
                    if (leaguePointsEarned > 0) {
                        add(stringResource(R.string.weekly_league_game_over_points, leaguePointsEarned))
                    }
                    if (dailyEvent.progress > 0 && !dailyEvent.completed) {
                        add(
                            stringResource(
                                R.string.daily_event_game_over_progress,
                                dailyEvent.progress,
                                dailyEvent.target
                            )
                        )
                    }
                }
                progressLines.take(2).forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.labelMedium,
                        color = ReflexGamePalette.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (progressLines.size > 2) {
                    Text(
                        text = stringResource(R.string.quest_hub_more_progress),
                        style = MaterialTheme.typography.labelSmall,
                        color = ArcadeGold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (mode == dailyMiniTournament.mode) {
                    DailyMiniTournamentGameOverCard(state = dailyMiniTournament)
                }

                if (isDoubleCoinsEnabled || isDoubleCoinsLoading) {
                    SecondaryGameButton(
                        text = doubleCoinsText,
                        onClick = onDoubleCoinsClick,
                        enabled = isDoubleCoinsEnabled,
                        isLoading = isDoubleCoinsLoading
                    )
                }

                SecondaryGameButton(
                    text = stringResource(R.string.back_to_home),
                    onClick = onHomeClick
                )
            }
        }
    }
}

@Composable
private fun gameOverHeadline(
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

internal fun isNearRecordScore(
    score: Int,
    bestScore: Int,
    isNewBestScore: Boolean
): Boolean {
    if (isNewBestScore || bestScore <= 0 || score >= bestScore) return false
    val thresholdScore = (bestScore * NEAR_RECORD_THRESHOLD_PERCENT + 99) / 100
    return score >= thresholdScore
}

private const val NEAR_RECORD_THRESHOLD_PERCENT = 80
