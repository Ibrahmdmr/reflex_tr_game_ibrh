package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
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
    onComboChallengeClaim: () -> Unit,
    onWeeklyChallengeClaim: () -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit,
    onAchievementClaim: (String) -> Unit
) {
    DailyChallengeCard(
        state = dailyChallengeState,
        rewardedAdUiState = rewardedAdUiState,
        onClaimClick = onDailyChallengeClaim,
        onDoubleRewardClick = onDailyChallengeDoubleRewardClick
    )
    WeeklyChallengeCard(
        state = progressionState.weeklyChallenge,
        onClaimClick = onWeeklyChallengeClaim
    )
    WeeklyGoalBoardCard(state = progressionState.weeklyGoalBoard)
    ComboChallengeCard(
        state = progressionState.comboChallenge,
        onClaimClick = onComboChallengeClaim
    )
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
    onInviteShareClick: () -> Unit,
    onDailyChallengeClaim: () -> Unit,
    onComboChallengeClaim: () -> Unit,
    onWeeklyChallengeClaim: () -> Unit,
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
    BonusHourCard(state = progressionState.bonusHour)
    WeeklyGoalBoardCard(state = progressionState.weeklyGoalBoard)
    MissionRewardsCard(
        dailyChallengeState = dailyChallengeState,
        weeklyChallengeState = progressionState.weeklyChallenge,
        comboChallengeState = progressionState.comboChallenge,
        achievements = progressionState.achievements,
        rewardedAdUiState = rewardedAdUiState,
        onDailyChallengeClaim = onDailyChallengeClaim,
        onComboChallengeClaim = onComboChallengeClaim,
        onWeeklyChallengeClaim = onWeeklyChallengeClaim,
        onDailyChallengeDoubleRewardClick = onDailyChallengeDoubleRewardClick,
        onAchievementClaim = onAchievementClaim
    )
    CoinChestCard(
        state = progressionState.coinChest,
        rewardedAdUiState = rewardedAdUiState,
        onOpenClick = onCoinChestClick
    )
    InviteFriendCard(
        rewardClaimed = progressionState.inviteRewardClaimed,
        onShareClick = onInviteShareClick
    )
}

@Composable
private fun InviteFriendCard(
    rewardClaimed: Boolean,
    onShareClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.invite_friend_title),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.invite_friend_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    if (rewardClaimed) {
                        R.string.invite_reward_claimed
                    } else {
                        R.string.invite_reward_available
                    }
                ),
                style = MaterialTheme.typography.labelLarge,
                color = ArcadeGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            OutlinedButton(
                onClick = onShareClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.invite_share_button),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BonusHourCard(state: BonusHourState) {
    val startLabel = formatBonusHour(state.startHour)
    val endLabel = formatBonusHour(state.endHour)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.bonus_hour_title),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.bonus_hour_window, startLabel, endLabel),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (state.isActive) {
                    stringResource(R.string.bonus_hour_active)
                } else {
                    stringResource(R.string.bonus_hour_next, startLabel)
                },
                style = MaterialTheme.typography.labelLarge,
                color = ArcadeGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (state.isActive) {
                    stringResource(R.string.bonus_hour_reward, state.coinBonusPercent)
                } else {
                    stringResource(
                        R.string.bonus_hour_remaining,
                        bonusHourRemainingText(state.minutesUntilStart)
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatBonusHour(hour: Int): String {
    return "${hour.coerceIn(0, 23).toString().padStart(2, '0')}:00"
}

@Composable
private fun bonusHourRemainingText(minutes: Int): String {
    val safeMinutes = minutes.coerceAtLeast(0)
    val hours = safeMinutes / 60
    val remainingMinutes = safeMinutes % 60
    return if (hours > 0) {
        stringResource(R.string.bonus_hour_remaining_hours, hours, remainingMinutes)
    } else {
        stringResource(R.string.bonus_hour_remaining_minutes, remainingMinutes)
    }
}
