package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.BuildConfig
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import java.util.Locale
import kotlinx.coroutines.delay
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
import com.reflex.tr.game.ibrh.firebase.FirebaseParam

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
