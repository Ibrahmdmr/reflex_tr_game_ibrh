package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.R
import java.util.Calendar
import kotlin.math.absoluteValue

interface LeaderboardRepository {
    fun getWeeklyLeaderboard(
        playerName: String,
        playerScore: Int,
        playerTheme: PlayerTheme,
        playerRankTier: RankTier,
        selectedMode: GameMode,
        selectedPeriod: LeaderboardPeriod,
        refreshTick: Int
    ): LeaderboardSnapshot
}

class LocalLeaderboardRepository : LeaderboardRepository {
    override fun getWeeklyLeaderboard(
        playerName: String,
        playerScore: Int,
        playerTheme: PlayerTheme,
        playerRankTier: RankTier,
        selectedMode: GameMode,
        selectedPeriod: LeaderboardPeriod,
        refreshTick: Int
    ): LeaderboardSnapshot {
        val weekKey = currentWeekKey()
        val seed = (weekKey + playerName + selectedMode.storageKey + selectedPeriod.name + refreshTick).hashCode().absoluteValue
        val names = listOf("Nova", "Blitz", "Pulse", "Echo", "Shadow", "NeonX", "Cyber", "ReflexPro", "TargetKing", "Matrix")
            .shuffledSeeded(seed)
            .take(7)
        val fakeEntries = names.mapIndexed { index, name ->
            val scoreOffset = ((seed / (index + 3)) % 18) - 8
            val anchor = when (index) {
                0 -> playerScore + 12
                1 -> playerScore + 6
                2 -> playerScore + 2
                3 -> playerScore - 3
                else -> playerScore - 8 - index * 2
            }
            val minimumScore = if (selectedPeriod == LeaderboardPeriod.Weekly) 2 else 4
            val score = maxOf(minimumScore, anchor + scoreOffset)
            LeaderboardEntry(
                rank = 0,
                name = name,
                score = score,
                theme = PlayerTheme.entries[(seed + index) % PlayerTheme.entries.size],
                rankTier = rankFor(score = score, level = 1 + score / 20)
            )
        }
        val displayName = playerName.ifBlank { "Oyuncu" }
        val playerEntry = LeaderboardEntry(
            rank = 0,
            name = displayName,
            score = playerScore,
            theme = playerTheme,
            rankTier = playerRankTier,
            isPlayer = true
        )
        val rankedEntries = (fakeEntries + playerEntry)
            .sortedByDescending { it.score }
            .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
        val playerRank = rankedEntries.firstOrNull { it.isPlayer }?.rank ?: rankedEntries.size
        val nextOpponent = rankedEntries
            .filter { !it.isPlayer && it.rank < playerRank }
            .minByOrNull { it.rank }
        val motivationRes = when {
            playerRank <= 3 -> R.string.leaderboard_motivation_top3
            nextOpponent != null -> R.string.leaderboard_motivation_pass_player
            else -> R.string.leaderboard_motivation_default
        }
        return LeaderboardSnapshot(
            weekKey = weekKey,
            selectedMode = selectedMode,
            selectedPeriod = selectedPeriod,
            entries = rankedEntries.take(8),
            playerRank = playerRank,
            motivationRes = motivationRes,
            motivationPlayerName = nextOpponent?.name.orEmpty(),
            motivationScoreGap = nextOpponent?.let {
                (it.score - playerScore + 1).coerceAtLeast(1)
            } ?: 0,
            refreshedTick = refreshTick
        )
    }

    private fun currentWeekKey(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-W${calendar.get(Calendar.WEEK_OF_YEAR)}"
    }
}

fun rankFor(score: Int, level: Int): RankTier {
    val power = score + level * 8
    return when {
        power >= 420 -> RankTier.ReflexGod
        power >= 260 -> RankTier.NeonMaster
        power >= 170 -> RankTier.Platinum
        power >= 100 -> RankTier.Gold
        power >= 45 -> RankTier.Silver
        else -> RankTier.Bronze
    }
}

private fun <T> List<T>.shuffledSeeded(seed: Int): List<T> {
    return mapIndexed { index, item -> ((seed / (index + 1)) + index * 31) to item }
        .sortedBy { it.first }
        .map { it.second }
}
