package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.CompactStatCard
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun HomeQuickStats(
    bestScore: Int,
    progressionState: ProgressionState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickStatCard(
            title = stringResource(R.string.coin_wallet_title),
            value = stringResource(R.string.coin_wallet_value, progressionState.coins),
            accent = ArcadeGold,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = stringResource(R.string.profile_title),
            value = stringResource(R.string.level_value, progressionState.level),
            accent = ArcadeTeal,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = stringResource(R.string.best_score_label),
            value = bestScore.toString(),
            accent = ArcadeBlue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun AchievementCounterCard(
    achievements: List<AchievementState>,
    modifier: Modifier = Modifier
) {
    val unlockedCount = achievements.count { it.unlocked }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ArcadeGold.copy(alpha = 0.10f),
        shape = RoundedCornerShape(PremiumChipRadius),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.28f))
    ) {
        Text(
            text = stringResource(R.string.home_achievement_counter, unlockedCount, achievements.size),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = ReflexGamePalette.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun QuickStatCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    CompactStatCard(
        title = title,
        value = value,
        accentColor = accent,
        modifier = modifier
    )
}

@Composable
internal fun StatisticsTabContent(
    bestScoresByMode: Map<GameMode, Int>,
    progressionState: ProgressionState
) {
    val totalGames = progressionState.totalGames.coerceAtLeast(0)
    val totalScore = progressionState.totalScore.coerceAtLeast(0)
    val bestScore = bestScoresByMode.values.maxOrNull()?.coerceAtLeast(0) ?: 0
    val averageScore = if (totalGames > 0) totalScore / totalGames else 0
    val totalHits = progressionState.totalHits.coerceAtLeast(0)
    val totalMisses = progressionState.totalMisses.coerceAtLeast(0)
    val totalAttempts = totalHits.toLong() + totalMisses.toLong()
    val accuracyPercent = if (totalAttempts > 0) {
        ((totalHits * 100f) / totalAttempts.toFloat()).toInt().coerceIn(0, 100)
    } else {
        0
    }
    val mostPlayedCandidate = GameMode.entries.maxByOrNull { progressionState.gamesPlayedByMode[it] ?: 0 }
    val mostPlayedMode = mostPlayedCandidate?.takeIf { mode ->
        (progressionState.gamesPlayedByMode[mode] ?: 0) > 0
    }
    val completedMissionCount = completedMissionCount(progressionState)

    Text(
        text = stringResource(R.string.statistics_title),
        style = MaterialTheme.typography.titleLarge,
        color = ReflexGamePalette.textPrimary,
        textAlign = TextAlign.Center
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatisticsRow(
            items = listOf(
                StatisticItem(R.string.statistics_total_games, totalGames.toString(), ArcadeBlue),
                StatisticItem(R.string.statistics_total_score, totalScore.toString(), ArcadeTeal)
            )
        )
        StatisticsRow(
            items = listOf(
                StatisticItem(R.string.statistics_best_score, bestScore.toString(), ArcadeGold),
                StatisticItem(R.string.statistics_average_score, averageScore.toString(), ArcadeBlue)
            )
        )
        StatisticsRow(
            items = listOf(
                StatisticItem(R.string.statistics_total_hits, totalHits.toString(), ArcadeTeal),
                StatisticItem(R.string.statistics_total_misses, totalMisses.toString(), ArcadeBlue)
            )
        )
        StatisticsRow(
            items = listOf(
                StatisticItem(R.string.statistics_accuracy, stringResource(R.string.percent_value, accuracyPercent), ArcadeGold),
                StatisticItem(R.string.statistics_max_combo, progressionState.lifetimeMaxCombo.coerceAtLeast(0).toString(), ArcadeGold)
            )
        )
        StatisticsRow(
            items = listOf(
                StatisticItem(R.string.statistics_total_coins_earned, progressionState.totalCoinsEarned.coerceAtLeast(0).toString(), ArcadeTeal),
                StatisticItem(R.string.statistics_total_coins_spent, progressionState.totalCoinsSpent.coerceAtLeast(0).toString(), ArcadeBlue)
            )
        )
        StatisticsRow(
            items = listOf(
                StatisticItem(
                    R.string.statistics_most_played_mode,
                    mostPlayedMode?.let { stringResource(it.titleRes) } ?: stringResource(R.string.statistics_default_empty),
                    ArcadeBlue
                ),
                StatisticItem(R.string.statistics_rewarded_ads, progressionState.rewardedAdWatchCount.coerceAtLeast(0).toString(), ArcadeGold)
            )
        )
        StatisticsRow(
            items = listOf(
                StatisticItem(R.string.statistics_unlocked_themes, progressionState.unlockedThemes.size.coerceAtLeast(0).toString(), ArcadeTeal),
                StatisticItem(R.string.statistics_completed_missions, completedMissionCount.toString(), ArcadeBlue)
            )
        )
    }
    PersonalRecordsSection(
        records = progressionState.personalRecords,
        bestScoresByMode = bestScoresByMode
    )
    ModeStatisticsSection(
        bestScoresByMode = bestScoresByMode,
        gamesPlayedByMode = progressionState.gamesPlayedByMode
    )
}

private data class StatisticItem(
    val titleRes: Int,
    val value: String,
    val accent: Color
)

@Composable
private fun StatisticsRow(items: List<StatisticItem>) {
    // IntrinsicSize.Min + fillMaxHeight keeps both cards the height of the taller one.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            QuickStatCard(
                title = stringResource(item.titleRes),
                value = item.value,
                accent = item.accent,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun ModeStatisticsSection(
    bestScoresByMode: Map<GameMode, Int>,
    gamesPlayedByMode: Map<GameMode, Int>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.26f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.statistics_mode_summary_title),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary
            )
            GameMode.entries.forEach { mode ->
                ModeStatisticRow(
                    mode = mode,
                    bestScore = bestScoresByMode[mode]?.coerceAtLeast(0) ?: 0,
                    gamesPlayed = gamesPlayedByMode[mode]?.coerceAtLeast(0) ?: 0
                )
            }
        }
    }
}

@Composable
private fun PersonalRecordsSection(
    records: PersonalRecordsState,
    bestScoresByMode: Map<GameMode, Int>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.personal_records_title),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary
            )
            StatisticsRow(
                items = listOf(
                    StatisticItem(R.string.personal_record_highest_score, records.bestScore.coerceAtLeast(0).toString(), ArcadeGold),
                    StatisticItem(R.string.personal_record_highest_combo, records.bestCombo.coerceAtLeast(0).toString(), ArcadeBlue)
                )
            )
            StatisticsRow(
                items = listOf(
                    StatisticItem(
                        R.string.personal_record_best_accuracy,
                        stringResource(R.string.percent_value, records.bestAccuracyPercent.coerceIn(0, 100)),
                        ArcadeTeal
                    ),
                    StatisticItem(
                        R.string.personal_record_longest_survival,
                        stringResource(R.string.seconds_short, records.longestSurvivalSeconds.coerceAtLeast(0)),
                        ArcadeGold
                    )
                )
            )
            StatisticsRow(
                items = listOf(
                    StatisticItem(R.string.personal_record_most_coins, records.mostCoinsInGame.coerceAtLeast(0).toString(), ArcadeTeal),
                    StatisticItem(
                        R.string.personal_record_classic_best,
                        (bestScoresByMode[GameMode.Classic] ?: 0).coerceAtLeast(0).toString(),
                        ArcadeGold
                    )
                )
            )
            StatisticsRow(
                items = listOf(
                    StatisticItem(
                        R.string.personal_record_moving_best,
                        (bestScoresByMode[GameMode.MovingTarget] ?: 0).coerceAtLeast(0).toString(),
                        ArcadeBlue
                    ),
                    StatisticItem(
                        R.string.personal_record_fake_best,
                        (bestScoresByMode[GameMode.FakeTarget] ?: 0).coerceAtLeast(0).toString(),
                        Color(0xFFFF6B8A)
                    )
                )
            )
            StatisticsRow(
                items = listOf(
                    StatisticItem(
                        R.string.personal_record_color_best,
                        (bestScoresByMode[GameMode.ColorReflex] ?: 0).coerceAtLeast(0).toString(),
                        ArcadeTeal
                    )
                )
            )
        }
    }
}

@Composable
private fun ModeStatisticRow(
    mode: GameMode,
    bestScore: Int,
    gamesPlayed: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = modeAccent(mode).copy(alpha = 0.10f),
        shape = RoundedCornerShape(PremiumCompactRadius),
        border = BorderStroke(1.dp, modeAccent(mode).copy(alpha = 0.26f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(mode.titleRes),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = ReflexGamePalette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.statistics_mode_best_value, bestScore),
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.statistics_mode_played_value, gamesPlayed),
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun completedMissionCount(progressionState: ProgressionState): Int {
    val dailyRewardCount = if (progressionState.dailyReward.claimedToday) 1 else 0
    val weeklyChallengeCount = if (progressionState.weeklyChallenge.completed) 1 else 0
    val leaderboardGoalCount = if (progressionState.dailyLeaderboardGoal.completed) 1 else 0
    val comboChallengeCount = if (progressionState.comboChallenge.completed) 1 else 0
    return progressionState.achievements.count { it.claimed } +
        progressionState.season.missions.count { it.completed } +
        dailyRewardCount +
        weeklyChallengeCount +
        leaderboardGoalCount +
        comboChallengeCount
}

private fun modeAccent(mode: GameMode): Color {
    return when (mode) {
        GameMode.Classic -> ArcadeGold
        GameMode.MovingTarget -> ArcadeBlue
        GameMode.FakeTarget -> Color(0xFFFF6B8A)
        GameMode.ColorReflex -> ArcadeTeal
    }
}
