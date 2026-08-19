package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

/**
 * The three opening days, each a theme rather than a difficulty step. A new player sees exactly
 * one of these at a time, which is the whole point: the rest of the app can wait.
 *
 * [rewardChest] and the day-3 badge are optional extras — a build without those systems simply
 * leaves them null and pays the coins.
 */
enum class StarterJourneyDay(
    val dayNumber: Int,
    @StringRes val titleRes: Int,
    val rewardCoins: Int,
    val rewardChest: RewardChestType? = null
) {
    LearnTheGame(
        dayNumber = 1,
        titleRes = R.string.starter_day_1_title,
        rewardCoins = 100
    ),
    MakeACombo(
        dayNumber = 2,
        titleRes = R.string.starter_day_2_title,
        rewardCoins = 150,
        rewardChest = RewardChestType.Small
    ),
    PrepareForLeaderboard(
        dayNumber = 3,
        titleRes = R.string.starter_day_3_title,
        rewardCoins = 200
    );

    /** Derived rather than stored, so a task can never claim a day the day does not list. */
    val tasks: List<StarterTask>
        get() = StarterTask.entries.filter { it.day == this }

    companion object {
        fun fromDayNumber(dayNumber: Int): StarterJourneyDay? =
            entries.firstOrNull { it.dayNumber == dayNumber }
    }
}

/**
 * Every starter task. [target] is the count that finishes it; one-shot tasks use 1.
 *
 * @see advanceStarterJourneyAfterGame and [advanceStarterJourneyForAction] for what moves them.
 */
enum class StarterTask(
    val storageKey: String,
    val day: StarterJourneyDay,
    @StringRes val titleRes: Int,
    val target: Int = 1
) {
    FinishFirstGame("finish_first_game", StarterJourneyDay.LearnTheGame, R.string.starter_task_first_game),
    ScoreAnyPoint("score_any_point", StarterJourneyDay.LearnTheGame, R.string.starter_task_any_score),
    SeeGameOver("see_game_over", StarterJourneyDay.LearnTheGame, R.string.starter_task_game_over),
    ReachCombo5("reach_combo_5", StarterJourneyDay.MakeACombo, R.string.starter_task_combo_5),
    PlayThreeGames("play_three_games", StarterJourneyDay.MakeACombo, R.string.starter_task_three_games, target = 3),
    SeeDailyEvent("see_daily_event", StarterJourneyDay.MakeACombo, R.string.starter_task_daily_event),
    Score50("score_50", StarterJourneyDay.PrepareForLeaderboard, R.string.starter_task_score_50),
    OpenLeaderboard("open_leaderboard", StarterJourneyDay.PrepareForLeaderboard, R.string.starter_task_leaderboard),
    ClaimAnyReward("claim_any_reward", StarterJourneyDay.PrepareForLeaderboard, R.string.starter_task_claim_reward);

    companion object {
        fun fromStorageKey(key: String): StarterTask? =
            entries.firstOrNull { it.storageKey == key }
    }
}

/**
 * Where the player is in the opening three days.
 *
 * [daysSinceStart] is derived from the stored first-open date at load time rather than persisted,
 * so a clock change cannot leave a stale day behind.
 */
@Immutable
data class StarterJourneyState(
    val daysSinceStart: Int = 1,
    val taskProgress: Map<StarterTask, Int> = emptyMap(),
    val claimedDays: Set<Int> = emptySet()
) {
    /** The highest day the player has reached; later days stay locked until their date. */
    val unlockedDayNumber: Int
        get() = daysSinceStart.coerceIn(1, STARTER_JOURNEY_DAYS)

    /**
     * The day the card is about: the earliest unclaimed day that has already unlocked. Missing a
     * day therefore parks the journey on it rather than skipping past it.
     */
    val activeDay: StarterJourneyDay?
        get() = StarterJourneyDay.entries.firstOrNull {
            it.dayNumber !in claimedDays && it.dayNumber <= unlockedDayNumber
        }

    val isCompleted: Boolean
        get() = StarterJourneyDay.entries.all { it.dayNumber in claimedDays }

    fun progressOf(task: StarterTask): Int =
        (taskProgress[task] ?: 0).coerceIn(0, task.target.coerceAtLeast(1))

    fun isTaskCompleted(task: StarterTask): Boolean = progressOf(task) >= task.target.coerceAtLeast(1)

    fun isDayCompleted(day: StarterJourneyDay): Boolean = day.tasks.all { isTaskCompleted(it) }

    fun completedTaskCount(day: StarterJourneyDay): Int = day.tasks.count { isTaskCompleted(it) }

    fun dayProgressPercent(day: StarterJourneyDay): Int {
        val total = day.tasks.size.coerceAtLeast(1)
        return ((completedTaskCount(day) * 100) / total).coerceIn(0, 100)
    }

    /** True once the active day's tasks are all done and its reward is still waiting. */
    val hasClaimableReward: Boolean
        get() = activeDay?.let { isDayCompleted(it) } == true

    /**
     * Shown while there is still something to do inside the window, or a finished day left to
     * collect — a reward earned on day 3 is not lost just because midnight passed.
     */
    val isActive: Boolean
        get() = !isCompleted &&
            activeDay != null &&
            (daysSinceStart <= STARTER_JOURNEY_DAYS || hasClaimableReward)

    /** The "completed" note fades out after a week; the badge is the permanent record. */
    val showsCompletedNote: Boolean
        get() = isCompleted && daysSinceStart <= STARTER_JOURNEY_NOTE_DAYS
}
