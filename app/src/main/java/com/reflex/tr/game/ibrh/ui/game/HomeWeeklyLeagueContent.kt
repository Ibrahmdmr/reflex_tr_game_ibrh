package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

internal val LeagueTier.accentColor: Color
    get() = when (this) {
        LeagueTier.Bronze -> Color(0xFFCD7F32)
        LeagueTier.Silver -> Color(0xFFB9C4DE)
        LeagueTier.Gold -> ArcadeGold
        LeagueTier.Diamond -> ArcadeTeal
        LeagueTier.Neon -> ReflexGamePalette.neonPurple
    }

@Composable
internal fun WeeklyLeagueCard(
    state: WeeklyLeagueState,
    remainingMinutes: Int,
    onClaimClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Null on a screen that already *is* the league view, which then shows no "view" button. */
    onViewClick: (() -> Unit)? = null
) {
    val accent = state.tier.accentColor
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
                    text = stringResource(R.string.weekly_league_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = ArcadeBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                LeagueTierChip(tier = state.tier)
            }
            Text(
                text = stringResource(R.string.weekly_league_points_value, state.points),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LeagueProgressBar(state = state, accent = accent)
            Text(
                text = leagueRemainingTimeText(remainingMinutes),
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            when {
                state.canClaimReward -> SecondaryGameButton(
                    text = stringResource(R.string.weekly_league_claim),
                    onClick = onClaimClick,
                    modifier = Modifier.height(46.dp)
                )

                onViewClick != null -> SecondaryGameButton(
                    text = stringResource(R.string.weekly_league_view),
                    onClick = onViewClick,
                    modifier = Modifier.height(46.dp)
                )
            }
        }
    }
}

@Composable
internal fun WeeklyLeagueSection(
    state: WeeklyLeagueState,
    remainingMinutes: Int,
    onClaimClick: () -> Unit
) {
    LaunchedEffect(state.tier) {
        logWeeklyLeagueEvent(
            event = FirebaseEvent.WeeklyLeagueViewed,
            tier = state.tier,
            totalPoints = state.points
        )
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WeeklyLeagueCard(
            state = state,
            remainingMinutes = remainingMinutes,
            onClaimClick = onClaimClick
        )
        if (state.canClaimReward) {
            LastWeekRewardRow(tier = state.pendingRewardTier)
        }
        Text(
            text = stringResource(R.string.weekly_league_rewards_title),
            style = MaterialTheme.typography.labelMedium,
            color = ArcadeGold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        LeagueTier.entries.forEach { tier ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.weekly_league_reward_row,
                        stringResource(tier.titleRes),
                        tier.rewardCoins
                    ),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (tier == state.tier) tier.accentColor else ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.weekly_league_points_value, tier.minPoints),
                    style = MaterialTheme.typography.labelSmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun LeagueTierChip(tier: LeagueTier) {
    Surface(
        color = tier.accentColor.copy(alpha = 0.16f),
        shape = RoundedCornerShape(PremiumPillRadius),
        border = BorderStroke(1.dp, tier.accentColor.copy(alpha = 0.44f))
    ) {
        Text(
            text = stringResource(tier.titleRes),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = tier.accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LastWeekRewardRow(tier: LeagueTier) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ArcadeGold.copy(alpha = 0.12f),
        shape = RoundedCornerShape(PremiumChipRadius),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.weekly_league_last_week_reward),
                style = MaterialTheme.typography.labelMedium,
                color = ArcadeGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    R.string.weekly_league_last_week_summary,
                    stringResource(tier.titleRes),
                    tier.rewardCoins
                ),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LeagueProgressBar(state: WeeklyLeagueState, accent: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        QuestProgressBar(percent = state.tierProgressPercent, accent = accent)
        Text(
            text = if (state.tier.next == null) {
                stringResource(R.string.weekly_league_top_tier)
            } else {
                stringResource(R.string.weekly_league_next_tier, state.pointsToNextTier)
            },
            style = MaterialTheme.typography.labelSmall,
            color = ReflexGamePalette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun leagueRemainingTimeText(remainingMinutes: Int): String {
    val safeMinutes = remainingMinutes.coerceAtLeast(0)
    return stringResource(
        R.string.weekly_league_time_left,
        safeMinutes / (24 * 60),
        (safeMinutes / 60) % 24
    )
}
