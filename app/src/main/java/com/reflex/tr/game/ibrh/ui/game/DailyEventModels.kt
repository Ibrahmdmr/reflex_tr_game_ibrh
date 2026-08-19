package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

/**
 * The pool the daily event rotates through; [dailyEventTypeForDate] picks one per day.
 * [requiredMode] is set when the event only counts runs in that mode.
 */
enum class DailyEventType(
    val storageKey: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val target: Int,
    val rewardCoins: Int,
    val requiredMode: GameMode? = null
) {
    ComboDay(
        storageKey = "combo_day",
        titleRes = R.string.daily_event_combo_title,
        descriptionRes = R.string.daily_event_combo_description,
        target = 1,
        rewardCoins = 75
    ),
    ClassicDay(
        storageKey = "classic_day",
        titleRes = R.string.daily_event_classic_title,
        descriptionRes = R.string.daily_event_classic_description,
        target = 100,
        rewardCoins = 100,
        requiredMode = GameMode.Classic
    ),
    AccuracyDay(
        storageKey = "accuracy_day",
        titleRes = R.string.daily_event_accuracy_title,
        descriptionRes = R.string.daily_event_accuracy_description,
        target = 1,
        rewardCoins = 100
    ),
    ColorReflexDay(
        storageKey = "color_reflex_day",
        titleRes = R.string.daily_event_color_title,
        descriptionRes = R.string.daily_event_color_description,
        target = 3,
        rewardCoins = 75,
        requiredMode = GameMode.ColorReflex
    ),
    BossHunt(
        storageKey = "boss_hunt",
        titleRes = R.string.daily_event_boss_title,
        descriptionRes = R.string.daily_event_boss_description,
        target = 2,
        rewardCoins = 125
    ),
    UltraMomentDay(
        storageKey = "ultra_moment_day",
        titleRes = R.string.daily_event_ultra_title,
        descriptionRes = R.string.daily_event_ultra_description,
        target = 1,
        rewardCoins = 100
    ),
    StreakGuard(
        storageKey = "streak_guard",
        titleRes = R.string.daily_event_streak_title,
        descriptionRes = R.string.daily_event_streak_description,
        target = 15,
        rewardCoins = 125
    ),
    ShopDay(
        storageKey = "shop_day",
        titleRes = R.string.daily_event_shop_title,
        descriptionRes = R.string.daily_event_shop_description,
        target = 1,
        rewardCoins = 75
    );

    companion object {
        fun fromStorageKey(key: String): DailyEventType? =
            entries.firstOrNull { it.storageKey == key }
    }
}

/** Today's event and how far the player has got with it. */
@Immutable
data class DailyEventState(
    val createdDate: String = "",
    val type: DailyEventType = DailyEventType.ComboDay,
    val progress: Int = 0,
    val claimed: Boolean = false
) {
    val target: Int
        get() = type.target

    val rewardCoins: Int
        get() = type.rewardCoins

    val completed: Boolean
        get() = progress >= target

    val progressPercent: Int
        get() = ((progress.coerceIn(0, target) * 100f) / target.coerceAtLeast(1)).toInt().coerceIn(0, 100)

    val canClaim: Boolean
        get() = completed && !claimed
}
