package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/** Every rule the daily event needs. UI reads state; only these functions decide it. */

/** Result of feeding a finished run into today's event, so the caller can tell what moved. */
data class DailyEventAdvance(
    val state: DailyEventState,
    val gainedProgress: Int,
    val justCompleted: Boolean
)

/**
 * Picks the event for [dateKey]. Deriving it from the date keeps the choice stable for the whole
 * day and identical for everyone, with no backend involved.
 */
fun dailyEventTypeForDate(dateKey: String = todayDateKey()): DailyEventType {
    val types = DailyEventType.entries
    val index = Math.floorMod(epochDayOf(dateKey), types.size)
    return types[index]
}

/** Today's event, rolled over when [state] belongs to an earlier day. */
fun dailyEventForToday(
    state: DailyEventState,
    dateKey: String = todayDateKey()
): DailyEventState {
    val type = dailyEventTypeForDate(dateKey)
    return if (state.createdDate == dateKey && state.type == type) {
        state
    } else {
        DailyEventState(createdDate = dateKey, type = type)
    }
}

/**
 * Applies one finished run. Returns the same state when the run does not count, so callers can
 * skip persisting and skip the Game Over notice.
 */
fun advanceDailyEventAfterGame(
    state: DailyEventState,
    mode: GameMode,
    score: Int,
    maxCombo: Int,
    accuracyPercent: Int,
    bossRoundHits: Int,
    ultraMomentHits: Int,
    maxFlawlessStreak: Int,
    usedNonDefaultCosmetic: Boolean,
    dateKey: String = todayDateKey()
): DailyEventAdvance {
    val today = dailyEventForToday(state, dateKey)
    if (today.completed) return DailyEventAdvance(today, gainedProgress = 0, justCompleted = false)
    if (today.type.requiredMode != null && today.type.requiredMode != mode) {
        return DailyEventAdvance(today, gainedProgress = 0, justCompleted = false)
    }

    val gain = when (today.type) {
        DailyEventType.ComboDay -> if (maxCombo >= 10) 1 else 0
        DailyEventType.ClassicDay -> score.coerceAtLeast(0)
        DailyEventType.AccuracyDay -> if (accuracyPercent >= 80) 1 else 0
        DailyEventType.ColorReflexDay -> 1
        DailyEventType.BossHunt -> bossRoundHits.coerceAtLeast(0)
        DailyEventType.UltraMomentDay -> if (ultraMomentHits > 0) 1 else 0
        DailyEventType.StreakGuard -> maxFlawlessStreak.coerceAtLeast(0)
        DailyEventType.ShopDay -> if (usedNonDefaultCosmetic) 1 else 0
    }
    if (gain <= 0) return DailyEventAdvance(today, gainedProgress = 0, justCompleted = false)

    // StreakGuard reports the best streak of a single run rather than a running total, so it takes
    // the maximum instead of accumulating across runs.
    val nextProgress = when (today.type) {
        DailyEventType.StreakGuard -> maxOf(today.progress, gain)
        else -> today.progress + gain
    }.coerceIn(0, today.target)

    if (nextProgress == today.progress) {
        return DailyEventAdvance(today, gainedProgress = 0, justCompleted = false)
    }
    val advanced = today.copy(progress = nextProgress)
    return DailyEventAdvance(
        state = advanced,
        gainedProgress = nextProgress - today.progress,
        justCompleted = advanced.completed
    )
}

/** Hours and minutes until the event rotates, for the "time left" line. */
fun dailyEventRemainingMinutes(nowMillis: Long = System.currentTimeMillis()): Int {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return ((calendar.timeInMillis - nowMillis).coerceAtLeast(0L) / 60_000L)
        .coerceAtMost(24L * 60L)
        .toInt()
}

/** Days since the epoch for a `yyyy-MM-dd` key; 0 when the key cannot be read. */
private fun epochDayOf(dateKey: String): Int {
    val parts = dateKey.split("-")
    if (parts.size != 3) return 0
    val year = parts[0].toIntOrNull() ?: return 0
    val month = parts[1].toIntOrNull() ?: return 0
    val day = parts[2].toIntOrNull() ?: return 0
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US).apply {
        clear()
        set(year, month - 1, day)
    }
    return (calendar.timeInMillis / 86_400_000L).toInt()
}

/**
 * Daily-event analytics. Carries only the event figures — never the player name or uid.
 */
fun logDailyEventEvent(event: FirebaseEvent, state: DailyEventState) {
    logGameEvent(event) {
        putString(FirebaseParam.EventType.key, state.type.storageKey)
        putInt(FirebaseParam.Progress.key, state.progress.coerceAtLeast(0))
        putInt(FirebaseParam.Target.key, state.target.coerceAtLeast(0))
        putInt(FirebaseParam.RewardCoin.key, state.rewardCoins.coerceAtLeast(0))
    }
}
