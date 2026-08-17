package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun HomeHeader(
    isCompactHeight: Boolean,
    isSoundEnabled: Boolean,
    onSoundToggleClick: () -> Unit
) {
    GameLogo(isCompactHeight = isCompactHeight)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(34.dp))
        Text(
            text = stringResource(R.string.game_title),
            modifier = Modifier.weight(1f),
            style = if (isCompactHeight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
            color = ReflexGamePalette.textPrimary,
            textAlign = TextAlign.Center
        )
        SoundToggleButton(
            isSoundEnabled = isSoundEnabled,
            onClick = onSoundToggleClick
        )
    }
}

@Composable
internal fun ProfileSubPageBackButton(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = ArcadeBlue.copy(alpha = 0.10f),
        shape = RoundedCornerShape(PremiumCompactRadius),
        border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.26f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "‹",
                style = MaterialTheme.typography.titleMedium,
                color = ArcadeGold
            )
            Text(
                text = stringResource(R.string.back_to_profile),
                style = MaterialTheme.typography.labelLarge,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun PlayTabContent(
    bestScore: Int,
    bestScoresByMode: Map<GameMode, Int>,
    selectedMode: GameMode,
    dailyFeaturedMode: DailyFeaturedModeState,
    dailyChallengeState: DailyChallengeState,
    rewardedAdUiState: RewardedAdUiState,
    progressionState: ProgressionState,
    isOnboardingCompleted: Boolean,
    onModeStartClick: (GameMode) -> Unit,
    onHowToPlayClick: () -> Unit,
    onDailyStreakProtect: () -> Unit,
    onDailyRewardCardClick: () -> Unit,
    onSuggestionTabClick: (HomeTab) -> Unit,
    onDailyChallengeClaim: () -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.game_tagline),
        style = MaterialTheme.typography.bodyMedium,
        color = ReflexGamePalette.textSecondary,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    HomeQuickStats(
        bestScore = bestScore,
        progressionState = progressionState
    )
    GameModeSection(
        bestScoresByMode = bestScoresByMode,
        modeMasteryXpByMode = progressionState.modeMasteryXpByMode,
        selectedMode = selectedMode,
        onModeStartClick = onModeStartClick
    )
    NextGoalSuggestionCard(
        bestScore = bestScore,
        progressionState = progressionState,
        dailyChallengeState = dailyChallengeState,
        onDailyRewardClick = onDailyRewardCardClick,
        onTabClick = onSuggestionTabClick
    )
    if (isOnboardingCompleted && !progressionState.firstTargetBonusClaimed) {
        FirstTargetCard()
    }
    DailyModeCard(
        state = dailyFeaturedMode,
        masteryProgress = modeMasteryProgressFor(progressionState.modeMasteryXpByMode, dailyFeaturedMode.mode),
        onPlayClick = onModeStartClick
    )
    DailyMiniTournamentCard(
        state = progressionState.dailyMiniTournament,
        onPlayClick = onModeStartClick
    )
    HowToPlayEntryCard(onClick = onHowToPlayClick)
}

@Composable
private fun NextGoalSuggestionCard(
    bestScore: Int,
    progressionState: ProgressionState,
    dailyChallengeState: DailyChallengeState,
    onDailyRewardClick: () -> Unit,
    onTabClick: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestion = nextGoalSuggestion(
        bestScore = bestScore,
        progressionState = progressionState,
        dailyChallengeState = dailyChallengeState,
        onDailyRewardClick = onDailyRewardClick,
        onTabClick = onTabClick
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(ArcadeBlue.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArcadeGold
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(suggestion.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(suggestion.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Bounded width so the suggestion title and description keep their column.
            SecondaryGameButton(
                modifier = Modifier.widthIn(min = 96.dp, max = 124.dp),
                text = stringResource(suggestion.buttonRes),
                onClick = suggestion.onClick
            )
        }
    }
}

private data class NextGoalSuggestion(
    val titleRes: Int,
    val descriptionRes: Int,
    val buttonRes: Int,
    val onClick: () -> Unit
)

private fun nextGoalSuggestion(
    bestScore: Int,
    progressionState: ProgressionState,
    dailyChallengeState: DailyChallengeState,
    onDailyRewardClick: () -> Unit,
    onTabClick: (HomeTab) -> Unit
): NextGoalSuggestion {
    val currentCoins = progressionState.coins.coerceAtLeast(0)
    val nearestLockedTheme = PlayerTheme.entries
        .filter { it.coinPrice > 0 && it !in progressionState.unlockedThemes }
        .minByOrNull { it.coinPrice }
    val canBuyTheme = nearestLockedTheme?.let { currentCoins >= it.coinPrice } == true
    val canClaimSeasonReward = progressionState.season.hasClaimableReward

    return when {
        progressionState.dailyReward.canClaim -> NextGoalSuggestion(
            titleRes = R.string.next_goal_daily_reward_title,
            descriptionRes = R.string.next_goal_daily_reward_description,
            buttonRes = R.string.next_goal_daily_reward_button,
            onClick = onDailyRewardClick
        )
        canClaimSeasonReward -> NextGoalSuggestion(
            titleRes = R.string.next_goal_season_reward_title,
            descriptionRes = R.string.next_goal_season_reward_description,
            buttonRes = R.string.next_goal_season_reward_button,
            onClick = { onTabClick(HomeTab.Season) }
        )
        !dailyChallengeState.completed -> NextGoalSuggestion(
            titleRes = R.string.next_goal_daily_mission_title,
            descriptionRes = R.string.next_goal_daily_mission_description,
            buttonRes = R.string.next_goal_daily_mission_button,
            onClick = { onTabClick(HomeTab.Missions) }
        )
        canBuyTheme -> NextGoalSuggestion(
            titleRes = R.string.next_goal_unlock_theme_title,
            descriptionRes = R.string.next_goal_unlock_theme_description,
            buttonRes = R.string.next_goal_unlock_theme_button,
            onClick = { onTabClick(HomeTab.Shop) }
        )
        bestScore < 20 || !progressionState.dailyLeaderboardGoal.claimed -> NextGoalSuggestion(
            titleRes = R.string.next_goal_leaderboard_title,
            descriptionRes = R.string.next_goal_leaderboard_description,
            buttonRes = R.string.next_goal_leaderboard_button,
            onClick = { onTabClick(HomeTab.Leaderboard) }
        )
        nearestLockedTheme != null && currentCoins < nearestLockedTheme.coinPrice -> NextGoalSuggestion(
            titleRes = R.string.next_goal_coin_title,
            descriptionRes = R.string.next_goal_coin_description,
            buttonRes = R.string.next_goal_coin_button,
            onClick = { onTabClick(HomeTab.Shop) }
        )
        else -> NextGoalSuggestion(
            titleRes = R.string.next_goal_daily_mission_title,
            descriptionRes = R.string.next_goal_daily_mission_description,
            buttonRes = R.string.next_goal_daily_mission_button,
            onClick = { onTabClick(HomeTab.Missions) }
        )
    }
}

@Composable
internal fun FirstTargetCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(ArcadeGold.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    style = MaterialTheme.typography.titleSmall,
                    color = ArcadeGold,
                    maxLines = 1
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.first_target_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.first_target_description, FirstTargetBonusCoins),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun DailyModeCard(
    state: DailyFeaturedModeState,
    masteryProgress: ModeMasteryProgress,
    onPlayClick: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = modeAccentColor(state.mode)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumSurfaceRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.16f),
                            ReflexGamePalette.neonPurple.copy(alpha = 0.10f)
                        )
                    )
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.22f))
                        .border(1.dp, accent.copy(alpha = 0.46f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = modeIcon(state.mode),
                        style = MaterialTheme.typography.titleMedium,
                        color = accent,
                        maxLines = 1
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.daily_mode_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(state.mode.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(state.mode.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.mode_mastery_level_value, masteryProgress.level),
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    color = accent.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(PremiumPillRadius),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.42f))
                ) {
                    Text(
                        text = stringResource(R.string.daily_mode_bonus, state.coinBonusPercent),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ReflexGamePalette.textPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            LinearProgressIndicator(
                progress = { masteryProgress.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                color = accent,
                trackColor = ReflexGamePalette.textPrimary.copy(alpha = 0.08f)
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayClick(state.mode) },
                color = accent.copy(alpha = 0.18f),
                shape = RoundedCornerShape(PremiumChipRadius),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
            ) {
                Text(
                    text = stringResource(R.string.daily_mode_play),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
