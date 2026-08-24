package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.PremiumState
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun RewardsTabContent(
    dailyChallengeState: DailyChallengeState,
    progressionState: ProgressionState,
    rewardedAdUiState: RewardedAdUiState,
    onDailyRewardClaim: () -> Unit,
    onDailyStreakProtect: () -> Unit,
    onDailyRewardCardClick: () -> Unit,
    onInviteShareClick: () -> Unit,
    onDailyChallengeClaim: () -> Unit,
    onComboChallengeClaim: () -> Unit,
    onWeeklyChallengeClaim: () -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit,
    onAchievementClaim: (String) -> Unit,
    onDailyEventClaim: () -> Unit,
    onDailyEventPlayClick: () -> Unit,
    onWeeklyLeagueClaim: () -> Unit,
    onRewardChestOpenClick: () -> Unit,
    onStarterRewardClaim: () -> Unit,
    onDailyEventViewed: () -> Unit,
    premiumState: PremiumState,
    onBonusOfferClick: (RewardedOfferType) -> Unit,
    onBonusLimitReached: (RewardedOfferType) -> Unit,
    onPremiumCardClick: () -> Unit,
    onBonusesOpened: (List<RewardedOfferState>) -> Unit,
    onSectionTabClick: (HomeTab) -> Unit
) {
    Text(
        text = stringResource(R.string.reward_center_title),
        style = MaterialTheme.typography.titleLarge,
        color = ReflexGamePalette.textPrimary,
        textAlign = TextAlign.Center
    )
    QuestHubSummarySection(
        summary = questHubRewardSummary(progressionState, dailyChallengeState),
        recommendation = questHubRecommendation(progressionState, dailyChallengeState),
        onRecommendationClick = onSectionTabClick
    )

    if (progressionState.starterJourney.isActive) {
        QuestSectionHeader(titleRes = R.string.starter_section_title)
        StarterJourneyCard(
            state = progressionState.starterJourney,
            onClaimClick = onStarterRewardClaim
        )
    } else if (progressionState.starterJourney.showsCompletedNote) {
        StarterJourneyCompletedNote()
    }

    QuestSectionHeader(titleRes = R.string.quest_hub_section_today)
    DailyRewardCard(
        state = progressionState.dailyReward,
        onClaimClick = onDailyRewardClaim,
        onProtectClick = onDailyStreakProtect,
        onCardClick = onDailyRewardCardClick
    )
    DailyEventDetailSection(
        state = progressionState.dailyEvent,
        remainingMinutes = dailyEventRemainingMinutes(),
        onPlayClick = onDailyEventPlayClick,
        onClaimClick = onDailyEventClaim,
        onViewed = onDailyEventViewed
    )
    BonusHourCard(state = progressionState.bonusHour)

    DailyChallengeCard(
        state = dailyChallengeState,
        rewardedAdUiState = rewardedAdUiState,
        onClaimClick = onDailyChallengeClaim,
        onDoubleRewardClick = onDailyChallengeDoubleRewardClick
    )
    ComboChallengeCard(
        state = progressionState.comboChallenge,
        onClaimClick = onComboChallengeClaim
    )

    QuestSectionHeader(titleRes = R.string.quest_hub_section_week)
    WeeklyLeagueSection(
        state = progressionState.weeklyLeague,
        remainingMinutes = weeklyLeagueRemainingMinutes(),
        onClaimClick = onWeeklyLeagueClaim
    )
    WeeklyChallengeCard(
        state = progressionState.weeklyChallenge,
        onClaimClick = onWeeklyChallengeClaim
    )
    WeeklyGoalBoardCard(state = progressionState.weeklyGoalBoard)
    // Ahead of the achievements: that list is sixteen cards long, and everything actionable was
    // stranded below it.
    QuestSectionHeader(titleRes = R.string.bonuses_title)
    // Only when one is waiting: an empty stack is not worth a card on an already long tab.
    if (progressionState.rewardChest.hasPendingChest) {
        RewardChestCard(
            state = progressionState.rewardChest,
            onOpenClick = onRewardChestOpenClick
        )
    }
    BonusesSection(
        // Recomputed only when something it reads actually changes, so the section does not
        // rebuild its offer list on every recomposition of the tab.
        offers = remember(progressionState, rewardedAdUiState) {
            bonusOffers(progressionState, rewardedAdUiState)
        },
        premiumState = premiumState,
        onOfferClick = onBonusOfferClick,
        onLimitReached = onBonusLimitReached,
        onPremiumClick = onPremiumCardClick,
        onOpened = onBonusesOpened
    )

    QuestSectionHeader(titleRes = R.string.quest_hub_section_achievements)
    AchievementSection(
        achievements = progressionState.achievements,
        unlockedIds = progressionState.latestUnlockedAchievementIds,
        onClaimClick = onAchievementClaim
    )

    InviteFriendCard(
        rewardClaimed = progressionState.inviteRewardClaimed,
        onShareClick = onInviteShareClick
    )
}

@Composable
private fun QuestSectionHeader(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleSmall,
        color = ArcadeGold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
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
        shape = RoundedCornerShape(PremiumCardRadius),
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
                shape = RoundedCornerShape(PremiumChipRadius)
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
        shape = RoundedCornerShape(PremiumCardRadius),
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
