package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

@Immutable
data class PersonalRecordsState(
    val bestScore: Int = 0,
    val bestCombo: Int = 0,
    val bestAccuracyPercent: Int = 0,
    val longestSurvivalSeconds: Int = 0,
    val mostCoinsInGame: Int = 0
)

/**
 * @property icon a language-neutral glyph carrying U+FE0E, so it renders as monochrome text and
 * takes the badge tint. Title initials do not work here: the Turkish ones collide.
 */
enum class ProfileBadge(
    val storageKey: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val lockedHintRes: Int,
    val rarityRank: Int,
    val icon: String
) {
    FirstGame("first_game", R.string.badge_first_game, R.string.badge_first_game_description, R.string.badge_first_game_hint, 1, "▶\uFE0E"),
    StarterComplete("starter_complete", R.string.badge_starter_complete, R.string.badge_starter_complete_description, R.string.badge_starter_complete_hint, 2, "◎\uFE0E"),
    ComboHunter("combo_hunter", R.string.badge_combo_hunter, R.string.badge_combo_hunter_description, R.string.badge_combo_hunter_hint, 2, "⚡\uFE0E"),
    RecordBreaker("record_breaker", R.string.badge_record_breaker, R.string.badge_record_breaker_description, R.string.badge_record_breaker_hint, 3, "★\uFE0E"),
    DailyPlayer("daily_player", R.string.badge_daily_player, R.string.badge_daily_player_description, R.string.badge_daily_player_hint, 2, "☀\uFE0E"),
    LoyalPlayer("loyal_player", R.string.badge_loyal_player, R.string.badge_loyal_player_description, R.string.badge_loyal_player_hint, 4, "♥\uFE0E"),
    CollectionMaster("collection_master", R.string.badge_collection_master, R.string.badge_collection_master_description, R.string.badge_collection_master_hint, 5, "◆\uFE0E"),
    BossHunter("boss_hunter", R.string.badge_boss_hunter, R.string.badge_boss_hunter_description, R.string.badge_boss_hunter_hint, 4, "⚔\uFE0E"),
    UltraPlayer("ultra_player", R.string.badge_ultra_player, R.string.badge_ultra_player_description, R.string.badge_ultra_player_hint, 4, "✦\uFE0E"),
    SeasonHunter("season_hunter", R.string.badge_season_hunter, R.string.badge_season_hunter_description, R.string.badge_season_hunter_hint, 5, "❄\uFE0E"),
    NeonLeaguePlayer("neon_league_player", R.string.badge_neon_league, R.string.badge_neon_league_description, R.string.badge_neon_league_hint, 5, "◈\uFE0E")
}

enum class PersonalRecordType(@StringRes val titleRes: Int) {
    HighestScore(R.string.personal_record_highest_score),
    HighestCombo(R.string.personal_record_highest_combo),
    BestAccuracy(R.string.personal_record_best_accuracy),
    LongestSurvival(R.string.personal_record_longest_survival),
    MostCoinsInGame(R.string.personal_record_most_coins),
    ClassicBest(R.string.personal_record_classic_best),
    MovingTargetBest(R.string.personal_record_moving_best),
    FakeTargetBest(R.string.personal_record_fake_best),
    ColorReflexBest(R.string.personal_record_color_best)
}

enum class AchievementType {
    BreakRecord,
    ScoreInSingleGame,
    PlayGames,
    ReachCombo,
    RewardedAds,
    ThemesUnlocked
}

enum class AchievementCategory(@StringRes val titleRes: Int) {
    Score(R.string.achievement_category_score),
    Game(R.string.achievement_category_game),
    Combo(R.string.achievement_category_combo),
    Ads(R.string.achievement_category_ads),
    Theme(R.string.achievement_category_theme)
}

@Immutable
data class AchievementState(
    val id: String,
    val type: AchievementType,
    val category: AchievementCategory,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val target: Int,
    val progress: Int,
    val rewardCoins: Int,
    val rewardXp: Int,
    val unlocked: Boolean,
    val claimed: Boolean
) {
    val progressPercent: Int
        get() = ((progress.coerceAtMost(target) * 100f) / target.coerceAtLeast(1)).toInt()
}
