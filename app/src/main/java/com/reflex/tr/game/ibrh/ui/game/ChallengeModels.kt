package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

enum class DailyChallenge(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val targetValue: Int,
    val rewardCoins: Int
) {
    ClassicScore20(
        titleRes = R.string.daily_challenge_classic_20_title,
        descriptionRes = R.string.daily_challenge_classic_20_description,
        targetValue = 20,
        rewardCoins = 100
    ),
    MovingTargetHits10(
        titleRes = R.string.daily_challenge_moving_10_title,
        descriptionRes = R.string.daily_challenge_moving_10_description,
        targetValue = 10,
        rewardCoins = 75
    ),
    FakeTargetScore5(
        titleRes = R.string.daily_challenge_fake_5_title,
        descriptionRes = R.string.daily_challenge_fake_5_description,
        targetValue = 5,
        rewardCoins = 50
    ),
    ColorReflexHits10(
        titleRes = R.string.daily_challenge_color_10_title,
        descriptionRes = R.string.daily_challenge_color_10_description,
        targetValue = 10,
        rewardCoins = 100
    ),
    Play3Games(
        titleRes = R.string.daily_challenge_play_3_title,
        descriptionRes = R.string.daily_challenge_play_3_description,
        targetValue = 3,
        rewardCoins = 50
    ),
    Combo5(
        titleRes = R.string.daily_challenge_combo_5_title,
        descriptionRes = R.string.daily_challenge_combo_5_description,
        targetValue = 5,
        rewardCoins = 75
    ),
    OpenLeaderboard(
        titleRes = R.string.daily_challenge_leaderboard_title,
        descriptionRes = R.string.daily_challenge_leaderboard_description,
        targetValue = 1,
        rewardCoins = 25
    ),
    VisitShop(
        titleRes = R.string.daily_challenge_shop_title,
        descriptionRes = R.string.daily_challenge_shop_description,
        targetValue = 1,
        rewardCoins = 25
    )
}

@Immutable
data class ChallengeState(
    val id: String,
    val type: WeeklyChallengeType = WeeklyChallengeType.ClassicScore50,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val target: Int,
    val progress: Int,
    val completed: Boolean,
    val claimed: Boolean = false,
    val rewardCoins: Int,
    val createdDate: String,
    val remainingDays: Int = 0
) {
    companion object {
        fun defaultWeekly(): ChallengeState {
            return ChallengeState(
                id = "weekly_classic_50",
                type = WeeklyChallengeType.ClassicScore50,
                titleRes = R.string.weekly_challenge_classic_50_title,
                descriptionRes = R.string.weekly_challenge_classic_50_description,
                target = 50,
                progress = 0,
                completed = false,
                claimed = false,
                rewardCoins = 500,
                createdDate = ""
            )
        }
    }
}

@Immutable
data class WeeklyGoalBoardState(
    val weekKey: String = "",
    val goals: List<WeeklyGoalState> = WeeklyGoalType.entries.map { WeeklyGoalState(type = it) },
    val bonusClaimed: Boolean = false,
    val bonusUnlockedThisGame: Boolean = false,
    val bonusRewardCoins: Int = 500
) {
    val allCompleted: Boolean
        get() = goals.isNotEmpty() && goals.all { it.completed }

    val totalRewardCoins: Int
        get() = goals.sumOf { if (it.rewardClaimedThisGame) it.rewardCoins else 0 } +
            if (bonusUnlockedThisGame) bonusRewardCoins else 0
}

@Immutable
data class WeeklyGoalState(
    val type: WeeklyGoalType,
    val progress: Int = 0,
    val claimed: Boolean = false,
    val rewardClaimedThisGame: Boolean = false
) {
    val target: Int
        get() = type.target

    val rewardCoins: Int
        get() = type.rewardCoins

    val completed: Boolean
        get() = progress >= target
}

enum class WeeklyGoalType(
    @StringRes val titleRes: Int,
    val target: Int,
    val rewardCoins: Int
) {
    Play20Games(R.string.weekly_goal_play_20_title, 20, 250),
    Score500(R.string.weekly_goal_score_500_title, 500, 500),
    Combo10FiveTimes(R.string.weekly_goal_combo_10_title, 5, 500)
}

enum class WeeklyChallengeType(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val target: Int,
    val rewardCoins: Int
) {
    ClassicScore50(
        titleRes = R.string.weekly_challenge_classic_50_title,
        descriptionRes = R.string.weekly_challenge_classic_50_description,
        target = 50,
        rewardCoins = 500
    ),
    ColorReflexScore30(
        titleRes = R.string.weekly_challenge_color_30_title,
        descriptionRes = R.string.weekly_challenge_color_30_description,
        target = 30,
        rewardCoins = 500
    ),
    FakeTargetScore20(
        titleRes = R.string.weekly_challenge_fake_20_title,
        descriptionRes = R.string.weekly_challenge_fake_20_description,
        target = 20,
        rewardCoins = 1000
    ),
    Play20Games(
        titleRes = R.string.weekly_challenge_play_20_title,
        descriptionRes = R.string.weekly_challenge_play_20_description,
        target = 20,
        rewardCoins = 250
    ),
    Combo10(
        titleRes = R.string.weekly_challenge_combo_10_title,
        descriptionRes = R.string.weekly_challenge_combo_10_description,
        target = 10,
        rewardCoins = 500
    )
}

@Immutable
data class DailyLeaderboardGoalState(
    val id: String = "",
    val type: DailyLeaderboardGoalType = DailyLeaderboardGoalType.SubmitScore,
    @StringRes val titleRes: Int = DailyLeaderboardGoalType.SubmitScore.titleRes,
    @StringRes val descriptionRes: Int = DailyLeaderboardGoalType.SubmitScore.descriptionRes,
    val target: Int = DailyLeaderboardGoalType.SubmitScore.target,
    val progress: Int = 0,
    val completed: Boolean = false,
    val claimed: Boolean = false,
    val rewardCoins: Int = DailyLeaderboardGoalType.SubmitScore.rewardCoins,
    val createdDate: String = "",
    val initialScore: Int = 0,
    val initialRank: Int = 0
)

@Immutable
data class PersonalGoalState(
    val createdDate: String = "",
    val targetScore: Int = 5,
    val initialBestScore: Int = 0,
    val progressScore: Int = 0,
    val completed: Boolean = false,
    val claimed: Boolean = false,
    val rewardCoins: Int = 100
) {
    val currentBestScore: Int
        get() = maxOf(initialBestScore, progressScore).coerceAtLeast(0)

    val remainingScore: Int
        get() = (targetScore - currentBestScore).coerceAtLeast(0)
}

@Immutable
data class ComboChallengeState(
    val createdDate: String = "",
    val type: ComboChallengeType = ComboChallengeType.Combo5,
    @StringRes val titleRes: Int = ComboChallengeType.Combo5.titleRes,
    @StringRes val descriptionRes: Int = ComboChallengeType.Combo5.descriptionRes,
    val target: Int = ComboChallengeType.Combo5.target,
    val progress: Int = 0,
    val gamesUsed: Int = 0,
    val completed: Boolean = false,
    val claimed: Boolean = false,
    val rewardCoins: Int = ComboChallengeType.Combo5.rewardCoins
)

enum class ComboChallengeType(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val target: Int,
    val rewardCoins: Int
) {
    Combo5(
        titleRes = R.string.combo_challenge_combo_5_title,
        descriptionRes = R.string.combo_challenge_combo_5_description,
        target = 5,
        rewardCoins = 100
    ),
    Combo10(
        titleRes = R.string.combo_challenge_combo_10_title,
        descriptionRes = R.string.combo_challenge_combo_10_description,
        target = 10,
        rewardCoins = 250
    ),
    TotalCombo20In3Games(
        titleRes = R.string.combo_challenge_total_20_title,
        descriptionRes = R.string.combo_challenge_total_20_description,
        target = 20,
        rewardCoins = 500
    ),
    NoMistake10Hits(
        titleRes = R.string.combo_challenge_no_mistake_10_title,
        descriptionRes = R.string.combo_challenge_no_mistake_10_description,
        target = 10,
        rewardCoins = 500
    )
}

enum class DailyLeaderboardGoalType(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val target: Int,
    val rewardCoins: Int
) {
    SubmitScore(
        titleRes = R.string.daily_leaderboard_goal_submit_title,
        descriptionRes = R.string.daily_leaderboard_goal_submit_description,
        target = 1,
        rewardCoins = 100
    ),
    ImproveScore10(
        titleRes = R.string.daily_leaderboard_goal_improve_score_title,
        descriptionRes = R.string.daily_leaderboard_goal_improve_score_description,
        target = 10,
        rewardCoins = 250
    ),
    Climb3Ranks(
        titleRes = R.string.daily_leaderboard_goal_climb_ranks_title,
        descriptionRes = R.string.daily_leaderboard_goal_climb_ranks_description,
        target = 3,
        rewardCoins = 500
    ),
    ReachTop50(
        titleRes = R.string.daily_leaderboard_goal_top_50_title,
        descriptionRes = R.string.daily_leaderboard_goal_top_50_description,
        target = 1,
        rewardCoins = 500
    )
}

@Immutable
data class DailyChallengeState(
    val id: String,
    val type: DailyChallenge,
    val target: Int,
    val progress: Int,
    val completed: Boolean,
    val createdDate: String,
    val rewardCoins: Int = 100,
    val rewardClaimed: Boolean = false,
    val doubleRewardClaimed: Boolean = false
) {
    companion object {
        fun default(): DailyChallengeState {
            return DailyChallengeState(
                id = "default_score20",
                type = DailyChallenge.ClassicScore20,
                target = DailyChallenge.ClassicScore20.targetValue,
                progress = 0,
                completed = false,
                createdDate = "",
                rewardCoins = DailyChallenge.ClassicScore20.rewardCoins
            )
        }
    }
}
