package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import com.reflex.tr.game.ibrh.R

/**
 * Rarity band of a title. Deliberately separate from [ThemeRarity]: retuning shop rarities must
 * not silently reclassify what a player earned.
 */
enum class PlayerTitleRarity(@StringRes val titleRes: Int) {
    Common(R.string.title_rarity_common),
    Rare(R.string.title_rarity_rare),
    Epic(R.string.title_rarity_epic),
    Legendary(R.string.title_rarity_legendary)
}

/** Grouping used only to label a title in the list. No rule reads it. */
enum class PlayerTitleCategory(@StringRes val titleRes: Int) {
    Starter(R.string.title_category_starter),
    Score(R.string.title_category_score),
    Combo(R.string.title_category_combo),
    Accuracy(R.string.title_category_accuracy),
    Boss(R.string.title_category_boss),
    Ultra(R.string.title_category_ultra),
    League(R.string.title_category_league),
    Collection(R.string.title_category_collection),
    Streak(R.string.title_category_streak),
    Mastery(R.string.title_category_mastery)
}

/**
 * Every title in the game, easiest first — the list is shown in this order, so it reads as a
 * ladder.
 *
 * [requirementValue] is the threshold the rule checks *and* the number the requirement line
 * prints, so the two can never disagree. Requirement strings without a placeholder simply ignore
 * it.
 *
 * @see meetsPlayerTitleRequirement for the conditions themselves.
 */
enum class PlayerTitle(
    val storageKey: String,
    @StringRes val titleRes: Int,
    @StringRes val requirementRes: Int,
    val requirementValue: Int,
    val category: PlayerTitleCategory,
    val rarity: PlayerTitleRarity
) {
    NewReflex(
        storageKey = "new_reflex",
        titleRes = R.string.player_title_new_reflex,
        requirementRes = R.string.player_title_new_reflex_requirement,
        requirementValue = 1,
        category = PlayerTitleCategory.Starter,
        rarity = PlayerTitleRarity.Common
    ),
    ComboHunter(
        storageKey = "combo_hunter",
        titleRes = R.string.player_title_combo_hunter,
        requirementRes = R.string.player_title_combo_hunter_requirement,
        requirementValue = 10,
        category = PlayerTitleCategory.Combo,
        rarity = PlayerTitleRarity.Rare
    ),
    SpeedMaster(
        storageKey = "speed_master",
        titleRes = R.string.player_title_speed_master,
        requirementRes = R.string.player_title_speed_master_requirement,
        requirementValue = 50,
        category = PlayerTitleCategory.Score,
        rarity = PlayerTitleRarity.Rare
    ),
    SharpTapper(
        storageKey = "sharp_tapper",
        titleRes = R.string.player_title_sharp_tapper,
        requirementRes = R.string.player_title_sharp_tapper_requirement,
        requirementValue = 85,
        category = PlayerTitleCategory.Accuracy,
        rarity = PlayerTitleRarity.Rare
    ),
    Collector(
        storageKey = "collector",
        titleRes = R.string.player_title_collector,
        requirementRes = R.string.player_title_collector_requirement,
        requirementValue = 5,
        category = PlayerTitleCategory.Collection,
        rarity = PlayerTitleRarity.Rare
    ),
    BossHunter(
        storageKey = "boss_hunter",
        titleRes = R.string.player_title_boss_hunter,
        requirementRes = R.string.player_title_boss_hunter_requirement,
        requirementValue = 5,
        category = PlayerTitleCategory.Boss,
        rarity = PlayerTitleRarity.Epic
    ),
    UltraPlayer(
        storageKey = "ultra_player",
        titleRes = R.string.player_title_ultra_player,
        requirementRes = R.string.player_title_ultra_player_requirement,
        requirementValue = 5,
        category = PlayerTitleCategory.Ultra,
        rarity = PlayerTitleRarity.Epic
    ),
    LoyalPlayer(
        storageKey = "loyal_player",
        titleRes = R.string.player_title_loyal_player,
        requirementRes = R.string.player_title_loyal_player_requirement,
        requirementValue = 7,
        category = PlayerTitleCategory.Streak,
        rarity = PlayerTitleRarity.Epic
    ),
    NeonWarrior(
        storageKey = "neon_warrior",
        titleRes = R.string.player_title_neon_warrior,
        requirementRes = R.string.player_title_neon_warrior_requirement,
        requirementValue = 1,
        category = PlayerTitleCategory.League,
        rarity = PlayerTitleRarity.Epic
    ),
    ReflexLegend(
        storageKey = "reflex_legend",
        titleRes = R.string.player_title_reflex_legend,
        requirementRes = R.string.player_title_reflex_legend_requirement,
        requirementValue = 100,
        category = PlayerTitleCategory.Mastery,
        rarity = PlayerTitleRarity.Legendary
    );

    companion object {
        fun fromStorageKey(key: String): PlayerTitle? =
            entries.firstOrNull { it.storageKey == key }
    }
}
