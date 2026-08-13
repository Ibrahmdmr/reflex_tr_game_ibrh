package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    onTargetSkinSelect: (TargetSkin) -> Unit,
    onTargetSkinBuy: (TargetSkin) -> Unit,
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
    ShopFeaturedSection(
        progressionState = progressionState,
        onThemeSelect = onThemeSelect,
        onThemeBuy = onThemeBuy,
        onTargetSkinSelect = onTargetSkinSelect,
        onTargetSkinBuy = onTargetSkinBuy
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
    TargetSkinShopSection(
        progressionState = progressionState,
        onTargetSkinSelect = onTargetSkinSelect,
        onTargetSkinBuy = onTargetSkinBuy
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
private fun ShopFeaturedSection(
    progressionState: ProgressionState,
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onTargetSkinSelect: (TargetSkin) -> Unit,
    onTargetSkinBuy: (TargetSkin) -> Unit
) {
    val nearestTheme = PlayerTheme.entries
        .filter { it.coinPrice > 0 && it !in progressionState.unlockedThemes }
        .minByOrNull { it.coinPrice }
        ?: PlayerTheme.entries.firstOrNull { it.coinPrice > 0 }
        ?: PlayerTheme.NeonRed
    val prestigeTheme = PlayerTheme.MatrixGreen
    val popularSkin = TargetSkin.NeonRing

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.shop_featured_title),
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
        FeaturedThemeCard(
            label = stringResource(R.string.shop_featured_nearest_theme),
            theme = nearestTheme,
            progressionState = progressionState,
            onThemeSelect = onThemeSelect,
            onThemeBuy = onThemeBuy
        )
        FeaturedThemeCard(
            label = stringResource(R.string.shop_featured_prestige_theme),
            theme = prestigeTheme,
            progressionState = progressionState,
            onThemeSelect = onThemeSelect,
            onThemeBuy = onThemeBuy
        )
        FeaturedSkinCard(
            label = stringResource(R.string.shop_featured_popular_skin),
            skin = popularSkin,
            progressionState = progressionState,
            onTargetSkinSelect = onTargetSkinSelect,
            onTargetSkinBuy = onTargetSkinBuy
        )
    }
}

@Composable
private fun FeaturedThemeCard(
    label: String,
    theme: PlayerTheme,
    progressionState: ProgressionState,
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit
) {
    val currentCoins = progressionState.coins.coerceAtLeast(0)
    val unlocked = theme in progressionState.unlockedThemes
    val selected = progressionState.activeTheme == theme
    val canBuy = !unlocked && currentCoins >= theme.coinPrice
    val accent = themeAccentColor(theme)
    FeaturedShopCard(
        label = label,
        name = stringResource(theme.titleRes),
        price = theme.coinPrice,
        currentCoins = currentCoins,
        selected = selected,
        unlocked = unlocked,
        canBuy = canBuy,
        accent = accent,
        preview = { FeaturedThemePreview(theme = theme) },
        onClick = {
            when {
                unlocked -> onThemeSelect(theme)
                canBuy -> onThemeBuy(theme)
            }
        }
    )
}

@Composable
private fun FeaturedSkinCard(
    label: String,
    skin: TargetSkin,
    progressionState: ProgressionState,
    onTargetSkinSelect: (TargetSkin) -> Unit,
    onTargetSkinBuy: (TargetSkin) -> Unit
) {
    val currentCoins = progressionState.coins.coerceAtLeast(0)
    val unlocked = skin in progressionState.unlockedTargetSkins
    val selected = progressionState.selectedTargetSkin == skin
    val canBuy = !unlocked && currentCoins >= skin.coinPrice
    val accent = targetSkinAccent(skin)
    FeaturedShopCard(
        label = label,
        name = stringResource(skin.titleRes),
        price = skin.coinPrice,
        currentCoins = currentCoins,
        selected = selected,
        unlocked = unlocked,
        canBuy = canBuy,
        accent = accent,
        preview = { TargetSkinPreview(skin = skin, accent = accent) },
        onClick = {
            when {
                unlocked -> onTargetSkinSelect(skin)
                canBuy -> onTargetSkinBuy(skin)
            }
        }
    )
}

@Composable
private fun FeaturedShopCard(
    label: String,
    name: String,
    price: Int,
    currentCoins: Int,
    selected: Boolean,
    unlocked: Boolean,
    canBuy: Boolean,
    accent: Color,
    preview: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val safePrice = price.coerceAtLeast(0)
    val cappedCoins = currentCoins.coerceIn(0, safePrice.takeIf { it > 0 } ?: currentCoins.coerceAtLeast(0))
    val progress = if (safePrice > 0) {
        (currentCoins.toFloat() / safePrice.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }
    val remainingCoins = (safePrice - currentCoins).coerceAtLeast(0)
    val clickEnabled = unlocked || canBuy

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = clickEnabled, onClick = onClick),
        color = if (selected) accent.copy(alpha = 0.15f) else ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = if (selected) 0.52f else 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(PremiumCardPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    preview()
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            selected -> stringResource(R.string.theme_selected)
                            unlocked -> stringResource(R.string.theme_unlocked)
                            canBuy -> stringResource(R.string.theme_price_affordable, safePrice)
                            else -> stringResource(R.string.theme_target_remaining, remainingCoins)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected || unlocked || canBuy) ArcadeGold else ReflexGamePalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                SecondaryGameButton(
                    text = when {
                        selected -> stringResource(R.string.theme_selected)
                        unlocked -> stringResource(R.string.select_theme)
                        canBuy -> stringResource(R.string.buy_theme)
                        else -> stringResource(R.string.theme_insufficient_coins)
                    },
                    enabled = !selected && clickEnabled,
                    onClick = onClick,
                    modifier = Modifier.widthIn(min = 108.dp, max = 132.dp)
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = accent,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = stringResource(R.string.theme_target_progress, cappedCoins, safePrice),
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeaturedThemePreview(theme: PlayerTheme) {
    val spec = themeVisualSpec(theme)
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        spec.backgroundTop,
                        spec.primary.copy(alpha = 0.76f),
                        spec.backgroundBottom
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.86f),
                            spec.primary.copy(alpha = 0.76f),
                            spec.secondary
                        )
                    )
                )
        )
    }
}

@Composable
private fun TargetSkinShopSection(
    progressionState: ProgressionState,
    onTargetSkinSelect: (TargetSkin) -> Unit,
    onTargetSkinBuy: (TargetSkin) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.target_skin_shop_title),
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
        TargetSkin.entries.forEach { skin ->
            TargetSkinCard(
                skin = skin,
                selected = progressionState.selectedTargetSkin == skin,
                unlocked = skin in progressionState.unlockedTargetSkins,
                canBuy = progressionState.coins >= skin.coinPrice,
                onSelect = { onTargetSkinSelect(skin) },
                onBuy = { onTargetSkinBuy(skin) }
            )
        }
    }
}

@Composable
private fun TargetSkinCard(
    skin: TargetSkin,
    selected: Boolean,
    unlocked: Boolean,
    canBuy: Boolean,
    onSelect: () -> Unit,
    onBuy: () -> Unit
) {
    val accent = targetSkinAccent(skin)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (selected) accent.copy(alpha = 0.16f) else ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = if (selected) 0.56f else 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TargetSkinPreview(skin = skin, accent = accent)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(skin.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when {
                        selected -> stringResource(R.string.target_skin_selected)
                        unlocked -> stringResource(R.string.target_skin_unlocked)
                        else -> stringResource(R.string.target_skin_price, skin.coinPrice)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected || unlocked) ArcadeGold else ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SecondaryGameButton(
                text = when {
                    selected -> stringResource(R.string.target_skin_selected)
                    unlocked -> stringResource(R.string.target_skin_select)
                    canBuy -> stringResource(R.string.target_skin_buy)
                    else -> stringResource(R.string.target_skin_locked)
                },
                enabled = !selected && (unlocked || canBuy),
                onClick = {
                    if (unlocked) onSelect() else onBuy()
                }
            )
        }
    }
}

@Composable
private fun TargetSkinPreview(
    skin: TargetSkin,
    accent: Color
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (skin == TargetSkin.CyberDot) 18.dp else 30.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.92f),
                            accent.copy(alpha = 0.86f)
                        )
                    )
                )
        )
        if (skin == TargetSkin.NeonRing || skin == TargetSkin.MatrixOrb) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
            )
        }
        Text(
            text = when (skin) {
                TargetSkin.ClassicTarget -> "○"
                TargetSkin.NeonRing -> "◎"
                TargetSkin.CyberDot -> "•"
                TargetSkin.FireCore -> "◆"
                TargetSkin.MatrixOrb -> "◇"
            },
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
    }
}

private fun targetSkinAccent(skin: TargetSkin): Color {
    return when (skin) {
        TargetSkin.ClassicTarget -> ArcadeGold
        TargetSkin.NeonRing -> Color(0xFF41F2FF)
        TargetSkin.CyberDot -> Color(0xFF9F7BFF)
        TargetSkin.FireCore -> Color(0xFFFF6B3D)
        TargetSkin.MatrixOrb -> Color(0xFF49FF91)
    }
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
