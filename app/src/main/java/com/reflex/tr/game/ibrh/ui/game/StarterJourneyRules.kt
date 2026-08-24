package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseParam

internal const val STARTER_JOURNEY_DAYS = 3

internal const val STARTER_JOURNEY_NOTE_DAYS = 7

private const val STARTER_COMBO_TARGET = 5
private const val STARTER_SCORE_TARGET = 50

data class StarterJourneyAdvance(
    val state: StarterJourneyState,
    val completedTasks: List<StarterTask>
) {
    val hasProgress: Boolean
        get() = completedTasks.isNotEmpty()
}

/** Unlocked and still unclaimed — stops a strong first run from clearing day 3 unseen. */
private fun StarterJourneyState.acceptsProgressFor(task: StarterTask): Boolean =
    task.day.dayNumber <= unlockedDayNumber &&
        task.day.dayNumber !in claimedDays &&
        !isTaskCompleted(task)

private fun StarterJourneyState.withProgress(
    task: StarterTask,
    amount: Int = 1
): StarterJourneyState {
    if (amount <= 0 || !acceptsProgressFor(task)) return this
    val next = (progressOf(task) + amount).coerceIn(0, task.target.coerceAtLeast(1))
    if (next == progressOf(task)) return this
    return copy(taskProgress = taskProgress + (task to next))
}

internal fun advanceStarterJourneyAfterGame(
    state: StarterJourneyState,
    score: Int,
    maxCombo: Int
): StarterJourneyAdvance {
    val safeScore = score.coerceAtLeast(0)
    val safeCombo = maxCombo.coerceAtLeast(0)
    var advanced = state
        .withProgress(StarterTask.FinishFirstGame)
        .withProgress(StarterTask.SeeGameOver)
        .withProgress(StarterTask.PlayThreeGames)
    if (safeScore > 0) advanced = advanced.withProgress(StarterTask.ScoreAnyPoint)
    if (safeCombo >= STARTER_COMBO_TARGET) advanced = advanced.withProgress(StarterTask.ReachCombo5)
    if (safeScore >= STARTER_SCORE_TARGET) advanced = advanced.withProgress(StarterTask.Score50)
    return StarterJourneyAdvance(advanced, newlyCompletedTasks(state, advanced))
}

internal fun advanceStarterJourneyForAction(
    state: StarterJourneyState,
    task: StarterTask
): StarterJourneyAdvance {
    val advanced = state.withProgress(task)
    return StarterJourneyAdvance(advanced, newlyCompletedTasks(state, advanced))
}

private fun newlyCompletedTasks(
    before: StarterJourneyState,
    after: StarterJourneyState
): List<StarterTask> = StarterTask.entries.filter {
    after.isTaskCompleted(it) && !before.isTaskCompleted(it)
}

/** A day lands in [StarterJourneyState.claimedDays] here and nowhere else, so it cannot pay twice. */
internal fun claimedStarterJourneyDay(
    state: StarterJourneyState
): Pair<StarterJourneyState, StarterJourneyDay>? {
    val day = state.activeDay ?: return null
    if (!state.isDayCompleted(day)) return null
    return state.copy(claimedDays = state.claimedDays + day.dayNumber) to day
}

/** Carries only the day and task identity — never playerName or uid. */
internal fun logStarterJourneyEvent(
    event: FirebaseEvent,
    day: StarterJourneyDay? = null,
    task: StarterTask? = null,
    rewardCoins: Int = 0
) {
    logGameEvent(event) {
        putInt(FirebaseParam.Day.key, (day ?: task?.day)?.dayNumber ?: 0)
        task?.let { putString(FirebaseParam.TaskId.key, it.storageKey) }
        putInt(FirebaseParam.RewardCoin.key, rewardCoins.coerceAtLeast(0))
    }
}
