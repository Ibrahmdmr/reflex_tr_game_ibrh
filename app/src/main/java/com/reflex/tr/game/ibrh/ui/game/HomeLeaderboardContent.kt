package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import kotlinx.coroutines.delay

@Composable
internal fun LeaderboardTabContent(
    leaderboardSnapshot: LeaderboardSnapshot,
    dailyLeaderboardGoal: DailyLeaderboardGoalState,
    onDailyLeaderboardGoalClaim: () -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onPeriodSelected: (LeaderboardPeriod) -> Unit,
    onRefreshClick: () -> Unit
) {
    LeaderboardSection(
        snapshot = leaderboardSnapshot,
        dailyLeaderboardGoal = dailyLeaderboardGoal,
        onDailyLeaderboardGoalClaim = onDailyLeaderboardGoalClaim,
        onModeSelected = onModeSelected,
        onPeriodSelected = onPeriodSelected,
        onRefreshClick = onRefreshClick
    )
}


@Composable
internal fun LeaderboardSection(
    snapshot: LeaderboardSnapshot,
    dailyLeaderboardGoal: DailyLeaderboardGoalState,
    onDailyLeaderboardGoalClaim: () -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onPeriodSelected: (LeaderboardPeriod) -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var showRefreshMessage by remember(snapshot.refreshedTick, snapshot.statusMessageRes) {
        mutableStateOf(snapshot.statusMessageRes != null)
    }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(450)
            isRefreshing = false
        }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.leaderboard_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.leaderboard_local_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
            Surface(
                modifier = Modifier.clickable {
                    if (!isRefreshing && !snapshot.isLoading) {
                        isRefreshing = true
                        onRefreshClick()
                    }
                },
                color = ArcadeBlue.copy(alpha = 0.18f),
                shape = CircleShape,
                border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.4f))
            ) {
                Text(
                    text = if (isRefreshing || snapshot.isLoading) "…" else "↻",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
        LeaderboardPeriodSelector(
            selectedPeriod = snapshot.selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )
        LeaderboardModeSelector(
            selectedMode = snapshot.selectedMode,
            onModeSelected = onModeSelected
        )
        DailyLeaderboardGoalCard(
            state = dailyLeaderboardGoal,
            onClaimClick = onDailyLeaderboardGoalClaim
        )
        val statusMessageRes = snapshot.statusMessageRes
        if (showRefreshMessage && statusMessageRes != null) {
            Text(
                text = stringResource(statusMessageRes),
                style = MaterialTheme.typography.bodySmall,
                color = if (snapshot.isOffline) ArcadeGold else ArcadeTeal
            )
        }
        if (snapshot.entries.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ReflexGamePalette.cardGlassStrong,
                shape = RoundedCornerShape(PremiumChipRadius),
                border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.32f))
            ) {
                Text(
                    text = stringResource(R.string.leaderboard_empty),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Text(
                text = if (snapshot.motivationRes == R.string.leaderboard_motivation_pass_player) {
                    stringResource(
                        snapshot.motivationRes,
                        snapshot.motivationPlayerName,
                        snapshot.motivationScoreGap
                    )
                } else {
                    stringResource(snapshot.motivationRes)
                },
                style = MaterialTheme.typography.bodySmall,
                color = ArcadeGold
            )
        }
        snapshot.entries.forEach { entry ->
            val accent = when {
                entry.isPlayer -> ArcadeGold
                entry.rank <= 3 -> ArcadeTeal
                else -> themeAccentColor(entry.theme)
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (entry.isPlayer) ArcadeGold.copy(alpha = 0.18f) else ReflexGamePalette.cardGlassStrong,
                shape = RoundedCornerShape(PremiumChipRadius),
                border = BorderStroke(1.dp, accent.copy(alpha = if (entry.rank <= 3 || entry.isPlayer) 0.58f else 0.26f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text(
                        text = stringResource(R.string.leaderboard_rank_value, entry.rank),
                        modifier = Modifier.width(34.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (entry.rank <= 3 || entry.isPlayer) ArcadeGold else ReflexGamePalette.textSecondary
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(themeAccentColor(entry.theme))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (entry.isPlayer) {
                                if (entry.name.isBlank()) {
                                    stringResource(R.string.leaderboard_you)
                                } else {
                                    stringResource(R.string.leaderboard_you_named, entry.name)
                                }
                            } else {
                                entry.name
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = ReflexGamePalette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(entry.rankTier.titleRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = ReflexGamePalette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = entry.score.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (entry.isPlayer) ArcadeGold else ReflexGamePalette.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyLeaderboardGoalCard(
    state: DailyLeaderboardGoalState,
    onClaimClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumChipRadius),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.daily_leaderboard_goal_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(state.titleRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ArcadeGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = stringResource(R.string.daily_leaderboard_goal_reward, state.rewardCoins),
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcadeTeal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(state.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (state.claimed) {
                    stringResource(R.string.daily_leaderboard_goal_claimed)
                } else {
                    stringResource(
                        R.string.daily_leaderboard_goal_progress,
                        state.progress.coerceIn(0, state.target),
                        state.target.coerceAtLeast(1)
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (state.completed) ArcadeTeal else ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (state.completed && !state.claimed) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClaimClick() },
                    color = ArcadeGold.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(PremiumPillRadius),
                    border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.42f))
                ) {
                    Text(
                        text = stringResource(R.string.daily_leaderboard_goal_claim),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = ReflexGamePalette.textPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
internal fun LeaderboardPeriodSelector(
    selectedPeriod: LeaderboardPeriod,
    onPeriodSelected: (LeaderboardPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(LeaderboardPeriod.AllTime).forEach { period ->
            val selected = period == selectedPeriod
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPeriodSelected(period) },
                color = if (selected) ArcadeGold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(PremiumPillRadius),
                border = BorderStroke(1.dp, (if (selected) ArcadeGold else ArcadeBlue).copy(alpha = 0.36f))
            ) {
                Text(
                    text = stringResource(
                        when (period) {
                            LeaderboardPeriod.Weekly -> R.string.leaderboard_period_weekly
                            LeaderboardPeriod.AllTime -> R.string.leaderboard_period_all_time
                        }
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) ArcadeGold else ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun LeaderboardModeSelector(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit
) {
    GameMode.entries.chunked(2).forEach { rowModes ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rowModes.forEach { mode ->
                val selected = mode == selectedMode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onModeSelected(mode) },
                    color = if (selected) themeAccentColor(PlayerTheme.CyberBlue).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(PremiumCompactRadius),
                    border = BorderStroke(1.dp, (if (selected) ArcadeTeal else ArcadeBlue).copy(alpha = 0.34f))
                ) {
                    Text(
                        text = stringResource(mode.titleRes),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
