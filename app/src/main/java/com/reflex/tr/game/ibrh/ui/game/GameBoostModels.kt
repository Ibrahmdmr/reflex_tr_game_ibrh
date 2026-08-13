package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import com.reflex.tr.game.ibrh.R

enum class RewardedAction {
    Continue,
    DoubleCoins,
    UnlockTheme,
    ProtectStreak,
    CoinChest,
    ShopCoinReward,
    DailyChallengeDoubleReward,
    Boost,
    SeasonXpBoost
}

enum class GameBoost(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val coinPrice: Int
) {
    ExtraTime(
        titleRes = R.string.boost_extra_time_title,
        descriptionRes = R.string.boost_extra_time_description,
        coinPrice = 120
    ),
    ExtraLife(
        titleRes = R.string.boost_extra_life_title,
        descriptionRes = R.string.boost_extra_life_description,
        coinPrice = 150
    ),
    ComboStart(
        titleRes = R.string.boost_combo_start_title,
        descriptionRes = R.string.boost_combo_start_description,
        coinPrice = 180
    )
}

enum class GamePowerUp(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val coinPrice: Int
) {
    ExtraTime(
        titleRes = R.string.power_up_extra_time_title,
        descriptionRes = R.string.power_up_extra_time_description,
        coinPrice = 250
    ),
    ExtraLife(
        titleRes = R.string.power_up_extra_life_title,
        descriptionRes = R.string.power_up_extra_life_description,
        coinPrice = 300
    ),
    ComboProtection(
        titleRes = R.string.power_up_combo_protection_title,
        descriptionRes = R.string.power_up_combo_protection_description,
        coinPrice = 500
    ),
    FirstMistakeForgiveness(
        titleRes = R.string.power_up_first_mistake_title,
        descriptionRes = R.string.power_up_first_mistake_description,
        coinPrice = 750
    )
}

const val FirstFiveExperienceGameLimit = 5
