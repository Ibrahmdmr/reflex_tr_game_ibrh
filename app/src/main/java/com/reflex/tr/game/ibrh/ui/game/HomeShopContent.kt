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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun ShopTabContent(
    progressionState: ProgressionState,
    selectedLanguage: AppLanguage,
    rewardedAdUiState: RewardedAdUiState,
    unlockedThemePopup: PlayerTheme? = null,
    popupBlocked: Boolean = false,
    onThemeUnlockPopupChange: (PlayerTheme?) -> Unit = {},
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onThemeTrial: (PlayerTheme) -> Unit,
    onCoinChestClick: () -> Unit,
    onShopCoinRewardClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.theme_shop_title),
        style = MaterialTheme.typography.titleLarge,
        color = ReflexGamePalette.textPrimary
    )
    Text(
        text = stringResource(R.string.theme_shop_description),
        style = MaterialTheme.typography.bodyMedium,
        color = ReflexGamePalette.textSecondary,
        textAlign = TextAlign.Center
    )
    ShopCoinEarnCard(
        progressionState = progressionState,
        rewardedAdUiState = rewardedAdUiState,
        onEarnClick = onShopCoinRewardClick
    )
    CoinChestCard(
        state = progressionState.coinChest,
        rewardedAdUiState = rewardedAdUiState,
        onOpenClick = onCoinChestClick
    )
    ThemeShopSection(
        progressionState = progressionState,
        selectedLanguage = selectedLanguage,
        unlockedThemePopup = unlockedThemePopup,
        popupBlocked = popupBlocked,
        onThemeUnlockPopupChange = onThemeUnlockPopupChange,
        onThemeSelect = onThemeSelect,
        onThemeBuy = onThemeBuy,
        onThemeTrial = onThemeTrial
    )
}

@Composable
internal fun ShopCoinEarnCard(
    progressionState: ProgressionState,
    rewardedAdUiState: RewardedAdUiState,
    onEarnClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rewardState = progressionState.shopCoinReward
    val currentCoins = progressionState.coins.coerceAtLeast(0)
    val targetTheme = PlayerTheme.entries
        .filterNot { it in progressionState.unlockedThemes }
        .filter { it.coinPrice > 0 }
        .minByOrNull { it.coinPrice }
    val remainingCoins = targetTheme?.let { (it.coinPrice - currentCoins).coerceAtLeast(0) } ?: 0
    val canWatch = rewardState.canClaim && rewardedAdUiState.isReady && !rewardedAdUiState.isShowing && !rewardedAdUiState.isLoading

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.38f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.shop_coin_earn_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (targetTheme == null) {
                            stringResource(R.string.theme_target_all_unlocked_empty)
                        } else {
                            stringResource(
                                R.string.shop_coin_earn_target,
                                stringResource(targetTheme.titleRes),
                                remainingCoins
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = stringResource(R.string.coin_wallet_value, currentCoins),
                    style = MaterialTheme.typography.labelMedium,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.shop_coin_earn_remaining_rights,
                        rewardState.remainingClaims,
                        rewardState.maxClaimsPerDay
                    ),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (rewardState.canClaim) ReflexGamePalette.textSecondary else ArcadeCoral,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SecondaryGameButton(
                    text = when {
                        !rewardState.canClaim -> stringResource(R.string.shop_coin_earn_limit_reached)
                        rewardedAdUiState.isLoading || rewardedAdUiState.isShowing -> stringResource(R.string.rewarded_loading)
                        !rewardedAdUiState.isReady -> stringResource(R.string.shop_coin_earn_ad_not_ready)
                        else -> stringResource(R.string.shop_coin_earn_button, rewardState.rewardCoins)
                    },
                    enabled = canWatch,
                    onClick = onEarnClick,
                    modifier = Modifier.weight(1f)
                )
            }
            if (rewardState.canClaim && !rewardedAdUiState.isReady && !rewardedAdUiState.isLoading && !rewardedAdUiState.isShowing) {
                Text(
                    text = stringResource(R.string.shop_coin_earn_ad_not_ready),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
