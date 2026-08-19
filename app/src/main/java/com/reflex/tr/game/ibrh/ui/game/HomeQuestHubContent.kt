package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

/**
 * Compact home-screen entry. Replaces the separate daily-event and weekly-league cards there so
 * the same figures are not repeated three times down the page.
 */
@Composable
internal fun QuestHubCard(
    summary: QuestHubRewardSummary,
    recommendation: QuestHubRecommendationType,
    dailyEvent: DailyEventState,
    weeklyLeague: WeeklyLeagueState,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (summary.hasRewards) ArcadeGold else ArcadeTeal
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(PremiumCardPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.next_goal_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                LeagueTierChip(tier = weeklyLeague.tier)
            }
            Text(
                text = stringResource(recommendation.titleRes),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (summary.hasRewards) {
                    pluralStringResource(
                        R.plurals.quest_hub_summary_line_plural,
                        summary.count,
                        summary.count,
                        summary.totalCoins
                    )
                } else {
                    stringResource(
                        R.string.daily_event_progress_value,
                        dailyEvent.progress.coerceIn(0, dailyEvent.target),
                        dailyEvent.target
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            SecondaryGameButton(
                text = stringResource(recommendation.buttonRes),
                onClick = onOpenClick,
                modifier = Modifier.height(46.dp)
            )
        }
    }
}

/**
 * Header of the rewards tab: what is waiting, and the single next step. The detail sections below
 * it keep their own claim buttons, so nothing here can pay a reward.
 */
@Composable
internal fun QuestHubSummarySection(
    summary: QuestHubRewardSummary,
    recommendation: QuestHubRecommendationType,
    onRecommendationClick: (HomeTab) -> Unit
) {
    LaunchedEffect(summary.count, recommendation) {
        logQuestHubEvent(
            event = FirebaseEvent.ClaimableRewardsViewed,
            rewardCount = summary.count,
            totalRewardCoins = summary.totalCoins,
            recommendation = recommendation
        )
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(
            1.dp,
            (if (summary.hasRewards) ArcadeGold else ArcadeTeal).copy(alpha = 0.34f)
        )
    ) {
        Column(
            modifier = Modifier.padding(PremiumCardPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.quest_hub_claimable_title),
                style = MaterialTheme.typography.labelMedium,
                color = ArcadeGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (summary.hasRewards) {
                Text(
                    text = pluralStringResource(
                        R.plurals.quest_hub_reward_ready_plural,
                        summary.count,
                        summary.count
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.quest_hub_total_coins, summary.totalCoins),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Only the headline reward is named; the sections below already list every one.
                summary.topReward?.let { top ->
                    QuestHubRewardRow(reward = top)
                }
            } else {
                Text(
                    text = stringResource(R.string.quest_hub_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SecondaryGameButton(
                text = stringResource(recommendation.buttonRes),
                onClick = {
                    logQuestHubEvent(
                        event = FirebaseEvent.QuestRecommendationClicked,
                        recommendation = recommendation
                    )
                    onRecommendationClick(recommendation.targetTab)
                },
                modifier = Modifier.height(46.dp)
            )
        }
    }
}

@Composable
private fun QuestHubRewardRow(reward: QuestHubReward) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(reward.kind.titleRes),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = ReflexGamePalette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(R.string.coins_earned_value, reward.coins),
            style = MaterialTheme.typography.labelSmall,
            color = ArcadeGold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** The one progress bar style shared by every quest, league and shop card. */
@Composable
internal fun QuestProgressBar(
    percent: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { percent.coerceIn(0, 100) / 100f },
        modifier = modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(PremiumPillRadius)),
        color = accent,
        trackColor = Color.White.copy(alpha = 0.1f)
    )
}
