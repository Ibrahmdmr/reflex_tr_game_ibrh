package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun RewardChestCard(
    state: RewardChestState,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bestChest = state.bestPendingChest ?: return
    val accent = rewardChestAccent(bestChest)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.40f))
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
                    text = stringResource(R.string.reward_chest_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                RewardChestBadge(type = bestChest)
            }
            Text(
                text = pluralStringResource(
                    R.plurals.reward_chest_pending_plural,
                    state.pendingCount,
                    state.pendingCount
                ),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (state.lastRewardCoins > 0) {
                    stringResource(R.string.reward_chest_last_reward, state.lastRewardCoins)
                } else {
                    stringResource(R.string.reward_chest_next_hint, state.gamesUntilNextChest)
                },
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            SecondaryGameButton(
                text = stringResource(R.string.reward_chest_open),
                onClick = onOpenClick,
                modifier = Modifier.height(46.dp)
            )
        }
    }
}

@Composable
internal fun RewardChestGameOverCard(
    type: RewardChestType,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = rewardChestAccent(type)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.48f))
    ) {
        Column(
            modifier = Modifier.padding(PremiumCardPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u25C6",
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        maxLines = 1
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.reward_chest_earned_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(type.titleRes),
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            SecondaryGameButton(
                text = stringResource(R.string.reward_chest_open),
                onClick = onOpenClick,
                modifier = Modifier.height(46.dp)
            )
        }
    }
}

@Composable
private fun RewardChestBadge(type: RewardChestType) {
    val accent = rewardChestAccent(type)
    Surface(
        color = accent.copy(alpha = 0.18f),
        shape = RoundedCornerShape(PremiumPillRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.42f))
    ) {
        Text(
            text = stringResource(type.titleRes),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = ReflexGamePalette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
