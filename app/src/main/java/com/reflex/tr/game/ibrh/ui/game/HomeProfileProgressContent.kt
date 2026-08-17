package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun ProfileProgressCard(
    playerProfile: PlayerProfile,
    progressionState: ProgressionState,
    bestScore: Int,
    onEditNameClick: () -> Unit,
    onTitleSelect: (PlayerTitle) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLevelXp = ((progressionState.level - 1) * 250)
    val nextLevelXp = progressionState.level * 250
    val levelProgress = ((progressionState.xp - currentLevelXp).toFloat() / 250f).coerceIn(0f, 1f)
    val remainingXp = (nextLevelXp - progressionState.xp).coerceAtLeast(0)
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
                    Text(
                        text = stringResource(playerProfile.title.titleRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary
                    )
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
                progress = { levelProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(PremiumPillRadius)),
                color = ArcadeTeal,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = stringResource(R.string.xp_value, progressionState.xp, nextLevelXp),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textSecondary
            )
            Text(
                text = stringResource(R.string.xp_to_next_level_value, remainingXp),
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.profile_title_select),
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary
                )
                PlayerTitle.entries.chunked(2).forEach { rowTitles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTitles.forEach { title ->
                            val selected = title == playerProfile.title
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onTitleSelect(title) },
                                color = if (selected) ArcadeTeal.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(PremiumPillRadius),
                                border = BorderStroke(
                                    1.dp,
                                    (if (selected) ArcadeTeal else ArcadeBlue).copy(alpha = 0.34f)
                                )
                            ) {
                                Text(
                                    text = stringResource(title.titleRes),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ReflexGamePalette.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
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
