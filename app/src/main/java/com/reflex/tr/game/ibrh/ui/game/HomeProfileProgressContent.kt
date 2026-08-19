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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun ProfileProgressCard(
    playerProfile: PlayerProfile,
    progressionState: ProgressionState,
    bestScore: Int,
    onEditNameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val levelProgress = levelProgressFor(
        level = progressionState.level,
        xp = progressionState.xp
    )
    val rank = rankFor(level = progressionState.level)
    val achievementCount = progressionState.achievements.count { it.unlocked }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.neonBlue.copy(alpha = 0.14f),
        shape = RoundedCornerShape(PremiumSurfaceRadius),
        border = BorderStroke(1.dp, ReflexGamePalette.neonBlue.copy(alpha = 0.32f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = ArcadeTeal
                    )
                    Text(
                        text = playerProfile.name.ifBlank { stringResource(R.string.leaderboard_you) },
                        style = MaterialTheme.typography.titleMedium,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val activeTitle = playerProfile.activeTitle
                    if (activeTitle != null) {
                        PlayerTitleInlineLabel(title = activeTitle)
                    } else {
                        Text(
                            text = stringResource(R.string.player_title_none_selected),
                            style = MaterialTheme.typography.bodySmall,
                            color = ReflexGamePalette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isCollectionComplete(progressionState)) {
                        CollectionMasterBadge(
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = onEditNameClick) {
                        Text(
                            text = stringResource(R.string.profile_change_name),
                            color = ArcadeGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = stringResource(R.string.level_value, progressionState.level),
                        style = MaterialTheme.typography.labelMedium,
                        color = ArcadeTeal
                    )
                }
            }
            LinearProgressIndicator(
                progress = { levelProgress.fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(PremiumPillRadius)),
                color = ArcadeTeal,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = stringResource(R.string.xp_value, progressionState.xp, levelProgress.nextLevelXp),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textSecondary
            )
            Text(
                text = stringResource(R.string.xp_to_next_level_value, levelProgress.remainingXp),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_games_value, progressionState.totalGames),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.profile_best_value, bestScore),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.profile_combo_value, progressionState.lifetimeMaxCombo),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weekly_league_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                LeagueTierChip(tier = progressionState.weeklyLeague.tier)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_rank_value, stringResource(rank.titleRes)),
                    modifier = Modifier.weight(1.2f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.profile_achievements_value, achievementCount),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.coin_wallet_value, progressionState.coins),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(
                    R.string.selected_theme_value,
                    stringResource(progressionState.selectedTheme.titleRes)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (progressionState.dailyReward.loyalBadgeUnlocked) {
                Surface(
                    color = ArcadeGold.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(PremiumChipRadius),
                    border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.38f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.daily_reward_loyal_badge),
                            style = MaterialTheme.typography.labelLarge,
                            color = ArcadeGold
                        )
                        Text(
                            text = stringResource(R.string.daily_reward_loyal_badge_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = ReflexGamePalette.textSecondary
                        )
                    }
                }
            }
            if (progressionState.lastLevelUp != null) {
                Text(
                    text = stringResource(R.string.level_up_value, progressionState.lastLevelUp),
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcadeGold
                )
            }
        }
    }
}
