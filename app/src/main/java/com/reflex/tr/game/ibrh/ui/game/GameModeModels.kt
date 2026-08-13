package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

const val MODE_MASTERY_XP_PER_LEVEL = 100

const val MODE_MASTERY_MAX_LEVEL = 10

enum class GameMode(
    val storageKey: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val longDescriptionRes: Int,
    @StringRes val difficultyRes: Int,
    @StringRes val arenaTitleRes: Int,
    @StringRes val startButtonRes: Int
) {
    Classic(
        storageKey = "classic",
        titleRes = R.string.mode_classic_title,
        descriptionRes = R.string.mode_classic_description,
        longDescriptionRes = R.string.mode_classic_long_description,
        difficultyRes = R.string.mode_difficulty_easy,
        arenaTitleRes = R.string.arena_reflex,
        startButtonRes = R.string.start_classic_game
    ),
    MovingTarget(
        storageKey = "moving_target",
        titleRes = R.string.mode_moving_title,
        descriptionRes = R.string.mode_moving_description,
        longDescriptionRes = R.string.mode_moving_long_description,
        difficultyRes = R.string.mode_difficulty_medium,
        arenaTitleRes = R.string.arena_target,
        startButtonRes = R.string.start_moving_game
    ),
    FakeTarget(
        storageKey = "fake_target",
        titleRes = R.string.mode_fake_title,
        descriptionRes = R.string.mode_fake_description,
        longDescriptionRes = R.string.mode_fake_long_description,
        difficultyRes = R.string.mode_difficulty_hard,
        arenaTitleRes = R.string.arena_challenge,
        startButtonRes = R.string.start_fake_game
    ),
    ColorReflex(
        storageKey = "color_reflex",
        titleRes = R.string.mode_color_title,
        descriptionRes = R.string.mode_color_description,
        longDescriptionRes = R.string.mode_color_long_description,
        difficultyRes = R.string.mode_difficulty_medium,
        arenaTitleRes = R.string.arena_challenge,
        startButtonRes = R.string.start_color_game
    )
}

@Immutable
data class DailyFeaturedModeState(
    val dateKey: String = "",
    val mode: GameMode = GameMode.Classic,
    val coinBonusPercent: Int = 20
)

@Immutable
data class ModeMasteryProgress(
    val xp: Int = 0
) {
    val level: Int
        get() = ((xp.coerceAtLeast(0) / MODE_MASTERY_XP_PER_LEVEL) + 1)
            .coerceIn(1, MODE_MASTERY_MAX_LEVEL)

    val progressXp: Int
        get() = if (level >= MODE_MASTERY_MAX_LEVEL) {
            MODE_MASTERY_XP_PER_LEVEL
        } else {
            xp.coerceAtLeast(0) % MODE_MASTERY_XP_PER_LEVEL
        }

    val progressFraction: Float
        get() = (progressXp.toFloat() / MODE_MASTERY_XP_PER_LEVEL.toFloat()).coerceIn(0f, 1f)
}

@Immutable
data class ModeMasteryLevelUp(
    val mode: GameMode,
    val level: Int,
    val coinBonus: Int
)

fun modeMasteryProgressFor(
    modeMasteryXpByMode: Map<GameMode, Int>,
    mode: GameMode
): ModeMasteryProgress {
    return ModeMasteryProgress(modeMasteryXpByMode[mode]?.coerceAtLeast(0) ?: 0)
}
