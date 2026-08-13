package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

private const val THEME_UNLOCK_CELEBRATION_DURATION_MS = 2_000L

@Composable
internal fun CoinChestCard(
    state: CoinChestState,
    rewardedAdUiState: RewardedAdUiState,
    onOpenClick: () -> Unit
) {
    val canOpen = state.canOpen && rewardedAdUiState.isReady && !rewardedAdUiState.isShowing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.38f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ArcadeGold.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArcadeGold
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(R.string.coin_chest_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.coin_chest_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val lastRewardText = if (state.lastRewardCoins > 0) {
                    " • ${stringResource(R.string.coin_chest_last_reward, state.lastRewardCoins)}"
                } else {
                    ""
                }
                Text(
                    text = stringResource(R.string.coin_chest_remaining, state.remainingOpens, state.maxOpensPerDay) + lastRewardText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (state.canOpen) ArcadeGold else ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Bounded width: SecondaryGameButton fills its width, so an unconstrained one in a
            // Row collapses the sibling text column to zero and hides the whole card body.
            SecondaryGameButton(
                modifier = Modifier.widthIn(min = 96.dp, max = 112.dp),
                text = when {
                    !state.canOpen -> stringResource(R.string.coin_chest_limit_reached)
                    rewardedAdUiState.isLoading || rewardedAdUiState.isShowing -> stringResource(R.string.rewarded_loading)
                    !rewardedAdUiState.isReady -> stringResource(R.string.rewarded_not_ready)
                    else -> stringResource(R.string.coin_chest_open)
                },
                enabled = canOpen,
                onClick = onOpenClick
            )
        }
    }
}
