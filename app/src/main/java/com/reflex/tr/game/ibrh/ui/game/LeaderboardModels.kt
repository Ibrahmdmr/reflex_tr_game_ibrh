package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

@Immutable
data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val score: Int,
    val theme: PlayerTheme = PlayerTheme.NeonRed,
    val rankTier: RankTier = RankTier.Bronze,
    val isPlayer: Boolean = false
)

enum class PlayerTitle(@StringRes val titleRes: Int) {
    ReflexHunter(R.string.player_title_reflex_hunter),
    ComboMaster(R.string.player_title_combo_master),
    NeonLegend(R.string.player_title_neon_legend),
    TargetKing(R.string.player_title_target_king)
}

enum class RankTier(@StringRes val titleRes: Int) {
    Bronze(R.string.rank_bronze),
    Silver(R.string.rank_silver),
    Gold(R.string.rank_gold),
    Platinum(R.string.rank_platinum),
    NeonMaster(R.string.rank_neon_master),
    ReflexGod(R.string.rank_reflex_god)
}

@Immutable
data class PlayerProfile(
    val name: String = "",
    val title: PlayerTitle = PlayerTitle.ReflexHunter,
    val weeklyBestScore: Int = 0,
    val weeklyBestScoresByMode: Map<GameMode, Int> = GameMode.entries.associateWith { 0 },
    val hasCompletedNamePrompt: Boolean = false
) {
    val hasName: Boolean
        get() = name.isNotBlank()
}

enum class LeaderboardPeriod {
    Weekly,
    AllTime
}

@Immutable
data class LeaderboardSnapshot(
    val weekKey: String = "",
    val selectedMode: GameMode = GameMode.Classic,
    val selectedPeriod: LeaderboardPeriod = LeaderboardPeriod.AllTime,
    val entries: List<LeaderboardEntry> = emptyList(),
    val playerRank: Int = 0,
    @StringRes val motivationRes: Int = R.string.leaderboard_motivation_default,
    val motivationPlayerName: String = "",
    val motivationScoreGap: Int = 0,
    val refreshedTick: Int = 0,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    @StringRes val statusMessageRes: Int? = null
)

@Immutable
data class LeaderboardThemeState(
    val leaderboardSnapshot: LeaderboardSnapshot = LeaderboardSnapshot()
)
