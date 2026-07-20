package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun MissionsTabContent(
    dailyChallengeState: DailyChallengeState,
    progressionState: ProgressionState,
    rewardedAdUiState: RewardedAdUiState,
    onDailyRewardClaim: () -> Unit,
    onDailyStreakProtect: () -> Unit,
    onDailyRewardCardClick: () -> Unit,
    onDailyChallengeClaim: () -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit,
    onAchievementClaim: (String) -> Unit
) {
    DailyChallengeCard(
        state = dailyChallengeState,
        rewardedAdUiState = rewardedAdUiState,
        onClaimClick = onDailyChallengeClaim,
        onDoubleRewardClick = onDailyChallengeDoubleRewardClick
    )
    WeeklyChallengeCard(state = progressionState.weeklyChallenge)
    DailyRewardCard(
        state = progressionState.dailyReward,
        onClaimClick = onDailyRewardClaim,
        onProtectClick = onDailyStreakProtect,
        onCardClick = onDailyRewardCardClick
    )
    AchievementSection(
        achievements = progressionState.achievements,
        unlockedIds = progressionState.latestUnlockedAchievementIds,
        onClaimClick = onAchievementClaim
    )
}

@Composable
internal fun RewardsTabContent(
    dailyChallengeState: DailyChallengeState,
    progressionState: ProgressionState,
    rewardedAdUiState: RewardedAdUiState,
    onDailyRewardClaim: () -> Unit,
    onDailyStreakProtect: () -> Unit,
    onDailyRewardCardClick: () -> Unit,
    onCoinChestClick: () -> Unit,
    onDailyChallengeClaim: () -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit,
    onAchievementClaim: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.reward_center_title),
        style = MaterialTheme.typography.titleLarge,
        color = ReflexGamePalette.textPrimary,
        textAlign = TextAlign.Center
    )
    DailyRewardCard(
        state = progressionState.dailyReward,
        onClaimClick = onDailyRewardClaim,
        onProtectClick = onDailyStreakProtect,
        onCardClick = onDailyRewardCardClick
    )
    CoinChestCard(
        state = progressionState.coinChest,
        rewardedAdUiState = rewardedAdUiState,
        onOpenClick = onCoinChestClick
    )
    MissionRewardsCard(
        dailyChallengeState = dailyChallengeState,
        weeklyChallengeState = progressionState.weeklyChallenge,
        achievements = progressionState.achievements,
        rewardedAdUiState = rewardedAdUiState,
        onDailyChallengeClaim = onDailyChallengeClaim,
        onDailyChallengeDoubleRewardClick = onDailyChallengeDoubleRewardClick,
        onAchievementClaim = onAchievementClaim
    )
}
