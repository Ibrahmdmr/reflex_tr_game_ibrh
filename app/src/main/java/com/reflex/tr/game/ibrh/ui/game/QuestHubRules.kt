package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseParam

internal enum class QuestHubRewardKind(
    @StringRes val titleRes: Int,
    val targetTab: HomeTab
) {
    StarterJourney(R.string.starter_journey_title, HomeTab.Rewards),
    DailyReward(R.string.daily_reward_title, HomeTab.Rewards),
    DailyEvent(R.string.daily_event_section_title, HomeTab.Rewards),
    DailyChallenge(R.string.daily_challenge_title, HomeTab.Rewards),
    WeeklyLeague(R.string.weekly_league_title, HomeTab.Rewards),
    WeeklyChallenge(R.string.weekly_challenge_title, HomeTab.Rewards),
    ComboChallenge(R.string.combo_challenge_title, HomeTab.Rewards),
    LeaderboardGoal(R.string.daily_leaderboard_goal_title, HomeTab.Leaderboard),
    PersonalGoal(R.string.personal_goal_title, HomeTab.Rewards),
    SeasonReward(R.string.season_title, HomeTab.Season),
    Achievement(R.string.nav_achievements, HomeTab.Achievements)
}

@Immutable
internal data class QuestHubReward(
    val kind: QuestHubRewardKind,
    val coins: Int
)

@Immutable
internal data class QuestHubRewardSummary(
    val rewards: List<QuestHubReward> = emptyList()
) {
    val count: Int get() = rewards.size
    val totalCoins: Int get() = rewards.sumOf { it.coins }
    val hasRewards: Boolean get() = rewards.isNotEmpty()

    val topReward: QuestHubReward? get() = rewards.maxByOrNull { it.coins }
}

internal enum class QuestHubRecommendationType(
    @StringRes val titleRes: Int,
    @StringRes val buttonRes: Int,
    val targetTab: HomeTab
) {
    ClaimReward(R.string.quest_hub_reco_claim, R.string.quest_hub_claim, HomeTab.Rewards),
    FinishStarterTask(R.string.quest_hub_reco_starter, R.string.quest_hub_view, HomeTab.Rewards),
    FinishDailyEvent(R.string.quest_hub_reco_daily_event, R.string.quest_hub_view, HomeTab.Rewards),
    ClimbLeague(R.string.quest_hub_reco_league, R.string.quest_hub_view, HomeTab.Rewards),
    CompleteSeasonMission(R.string.quest_hub_reco_season, R.string.quest_hub_view, HomeTab.Season),
    UnlockTheme(R.string.quest_hub_reco_theme, R.string.quest_hub_view, HomeTab.Shop),
    PlayAnotherGame(R.string.quest_hub_reco_play, R.string.quest_hub_play, HomeTab.Play)
}

private const val NEARLY_DONE_PERCENT = 50

internal fun claimableRewards(
    progression: ProgressionState,
    dailyChallenge: DailyChallengeState
): List<QuestHubReward> = buildList {
    // First in priority: a new player should see their own track above everything else.
    progression.starterJourney.let {
        if (it.isActive && it.hasClaimableReward) {
            add(QuestHubReward(QuestHubRewardKind.StarterJourney, it.activeDay?.rewardCoins ?: 0))
        }
    }
    val dailyReward = progression.dailyReward
    if (dailyReward.canClaim && !dailyReward.claimedToday) {
        add(QuestHubReward(QuestHubRewardKind.DailyReward, dailyReward.rewardCoins))
    }
    progression.dailyEvent.let {
        if (it.canClaim) add(QuestHubReward(QuestHubRewardKind.DailyEvent, it.rewardCoins))
    }
    if (dailyChallenge.completed && !dailyChallenge.rewardClaimed) {
        add(QuestHubReward(QuestHubRewardKind.DailyChallenge, dailyChallenge.rewardCoins))
    }
    progression.weeklyLeague.let {
        if (it.canClaimReward) {
            add(QuestHubReward(QuestHubRewardKind.WeeklyLeague, it.pendingRewardTier.rewardCoins))
        }
    }
    progression.weeklyChallenge.let {
        if (it.completed && !it.claimed) {
            add(QuestHubReward(QuestHubRewardKind.WeeklyChallenge, it.rewardCoins))
        }
    }
    progression.comboChallenge.let {
        if (it.completed && !it.claimed) {
            add(QuestHubReward(QuestHubRewardKind.ComboChallenge, it.rewardCoins))
        }
    }
    progression.dailyLeaderboardGoal.let {
        if (it.completed && !it.claimed) {
            add(QuestHubReward(QuestHubRewardKind.LeaderboardGoal, it.rewardCoins))
        }
    }
    progression.personalGoal.let {
        if (it.completed && !it.claimed) {
            add(QuestHubReward(QuestHubRewardKind.PersonalGoal, it.rewardCoins))
        }
    }
    if (progression.season.hasClaimableReward) {
        add(QuestHubReward(QuestHubRewardKind.SeasonReward, progression.season.nextReward.coinReward))
    }
    progression.achievements.count { it.unlocked && !it.claimed }.let { pending ->
        if (pending > 0) {
            val coins = progression.achievements
                .filter { it.unlocked && !it.claimed }
                .sumOf { it.rewardCoins }
            add(QuestHubReward(QuestHubRewardKind.Achievement, coins))
        }
    }
}

internal fun questHubRewardSummary(
    progression: ProgressionState,
    dailyChallenge: DailyChallengeState
): QuestHubRewardSummary = QuestHubRewardSummary(claimableRewards(progression, dailyChallenge))

/** The one source for "next", so the hub card and the "next goal" card cannot disagree. */
internal fun questHubRecommendation(
    progression: ProgressionState,
    dailyChallenge: DailyChallengeState
): QuestHubRecommendationType {
    if (claimableRewards(progression, dailyChallenge).isNotEmpty()) {
        return QuestHubRecommendationType.ClaimReward
    }
    // Ahead of every ordinary suggestion; it disappears on its own after the opening days.
    if (progression.starterJourney.isActive) {
        return QuestHubRecommendationType.FinishStarterTask
    }
    val event = progression.dailyEvent
    if (!event.completed && event.progressPercent >= NEARLY_DONE_PERCENT) {
        return QuestHubRecommendationType.FinishDailyEvent
    }
    val league = progression.weeklyLeague
    if (league.tier.next != null && league.tierProgressPercent >= NEARLY_DONE_PERCENT) {
        return QuestHubRecommendationType.ClimbLeague
    }
    val nearlyDoneMission = progression.season.missions.any {
        !it.completed && it.progressPercent >= NEARLY_DONE_PERCENT
    }
    if (nearlyDoneMission) return QuestHubRecommendationType.CompleteSeasonMission

    val affordableTheme = PlayerTheme.entries.any {
        it.coinPrice > 0 && it !in progression.unlockedThemes && progression.coins >= it.coinPrice
    }
    if (affordableTheme) return QuestHubRecommendationType.UnlockTheme

    return QuestHubRecommendationType.PlayAnotherGame
}

/** Carries only counts and section names — never playerName or uid. */
internal fun logQuestHubEvent(
    event: FirebaseEvent,
    section: String? = null,
    rewardCount: Int = 0,
    totalRewardCoins: Int = 0,
    recommendation: QuestHubRecommendationType? = null
) {
    logGameEvent(event) {
        section?.let { putString(FirebaseParam.Section.key, it) }
        putInt(FirebaseParam.RewardCount.key, rewardCount.coerceAtLeast(0))
        putInt(FirebaseParam.TotalRewardCoin.key, totalRewardCoins.coerceAtLeast(0))
        recommendation?.let {
            putString(FirebaseParam.RecommendationType.key, it.name)
        }
    }
}
