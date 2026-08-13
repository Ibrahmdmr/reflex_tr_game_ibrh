package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.CompactStatCard
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun PerformanceSummaryGrid(
    score: Int,
    bestScore: Int,
    maxCombo: Int,
    maxFlawlessStreak: Int,
    bossRoundBonusCoins: Int,
    ultraMomentBonusCoins: Int,
    ultraMomentHits: Int,
    perfectHits: Int,
    greatHits: Int,
    accuracyPercent: Int,
    seasonXp: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // IntrinsicSize.Min + fillMaxHeight makes both cards in a row adopt the taller one's
        // height. The prominent score card carries extra padding and a larger value style, so
        // without this its neighbour would render visibly shorter.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryMetricCard(
                title = stringResource(R.string.score),
                value = score.toString(),
                accentColor = ArcadeCoral,
                // Equal weight across both rows keeps the 2x2 grid aligned; the score card's
                // emphasis comes from `prominent`, not from being wider than its neighbours.
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                prominent = true
            )
            SummaryMetricCard(
                title = stringResource(R.string.best_score),
                value = bestScore.toString(),
                accentColor = ArcadeBlue,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryMetricCard(
                title = stringResource(R.string.accuracy),
                value = stringResource(R.string.percent_value, accuracyPercent),
                accentColor = ArcadeCoral,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            SummaryMetricCard(
                title = stringResource(R.string.max_combo),
                value = stringResource(R.string.combo_short_value, maxCombo),
                accentColor = ArcadeGold,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        SecondarySummaryDetails(
            maxFlawlessStreak = maxFlawlessStreak,
            bossRoundBonusCoins = bossRoundBonusCoins,
            ultraMomentBonusCoins = ultraMomentBonusCoins,
            ultraMomentHits = ultraMomentHits,
            perfectHits = perfectHits,
            greatHits = greatHits,
            seasonXp = seasonXp
        )
    }
}

@Composable
private fun SecondarySummaryDetails(
    maxFlawlessStreak: Int,
    bossRoundBonusCoins: Int,
    ultraMomentBonusCoins: Int,
    ultraMomentHits: Int,
    perfectHits: Int,
    greatHits: Int,
    seasonXp: Int
) {
    val details = listOf(
        stringResource(R.string.flawless_streak_best) to maxFlawlessStreak.coerceAtLeast(0).toString(),
        stringResource(R.string.boss_round_bonus) to stringResource(
            R.string.coin_bonus_short_value,
            bossRoundBonusCoins.coerceAtLeast(0)
        ),
        stringResource(R.string.ultra_moment_bonus) to stringResource(
            R.string.coin_bonus_short_value,
            ultraMomentBonusCoins.coerceAtLeast(0)
        ),
        stringResource(R.string.ultra_moment_hits) to ultraMomentHits.coerceAtLeast(0).toString(),
        stringResource(R.string.timing_perfect) to perfectHits.coerceAtLeast(0).toString(),
        stringResource(R.string.timing_great) to greatHits.coerceAtLeast(0).toString(),
        stringResource(R.string.game_over_season_xp) to seasonXp.coerceAtLeast(0).toString()
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(PremiumChipRadius),
        color = ReflexGamePalette.cardGlassStrong,
        border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            details.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { (title, value) ->
                        SecondaryDetailText(
                            title = title,
                            value = value,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SecondaryDetailText(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = ReflexGamePalette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = ReflexGamePalette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SummaryMetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    prominent: Boolean = false
) {
    CompactStatCard(
        title = title,
        value = value,
        accentColor = accentColor,
        modifier = modifier,
        prominent = prominent
    )
}
