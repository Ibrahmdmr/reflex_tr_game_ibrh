package com.reflex.tr.game.ibrh.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun BoostSelectionBottomSheet(
    coins: Int,
    isRewardedAdReady: Boolean,
    onStartWithoutBoost: () -> Unit,
    onBoostCoinClick: (GameBoost) -> Unit,
    onBoostAdClick: (GameBoost) -> Unit,
    onPowerUpClick: (GamePowerUp) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(GameDialogScrimColor)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        val sheetMaxHeight = maxHeight * 0.92f
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = sheetMaxHeight)
                .navigationBarsPadding()
                .clickable(onClick = {}),
            color = ReflexGamePalette.cardGlassStrong,
            shape = RoundedCornerShape(topStart = PremiumPanelRadius, topEnd = PremiumPanelRadius),
            border = BorderStroke(1.dp, ReflexGamePalette.neonPurple.copy(alpha = 0.42f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.power_up_sheet_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.power_up_sheet_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SheetSectionHeader(
                        title = stringResource(R.string.boost_section_title),
                        description = stringResource(R.string.boost_section_description)
                    )
                    GameBoost.entries.forEach { boost ->
                        BoostOptionRow(
                            boost = boost,
                            coins = coins,
                            isRewardedAdReady = isRewardedAdReady,
                            onCoinClick = onBoostCoinClick,
                            onAdClick = onBoostAdClick
                        )
                    }
                    SheetSectionHeader(title = stringResource(R.string.power_up_section_title))
                    GamePowerUp.entries.forEach { powerUp ->
                        PowerUpOptionRow(
                            powerUp = powerUp,
                            coins = coins,
                            onPowerUpClick = onPowerUpClick
                        )
                    }
                }
                SecondaryGameButton(
                    text = stringResource(R.string.power_up_start_without),
                    onClick = onStartWithoutBoost,
                    modifier = Modifier.height(48.dp)
                )
            }
        }
    }
}

@Composable
private fun SheetSectionHeader(
    title: String,
    description: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = ArcadeGold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Two actions, unlike [PowerUpOptionRow]: a boost can be bought or earned with an ad. */
@Composable
private fun BoostOptionRow(
    boost: GameBoost,
    coins: Int,
    isRewardedAdReady: Boolean,
    onCoinClick: (GameBoost) -> Unit,
    onAdClick: (GameBoost) -> Unit
) {
    val canBuyWithCoins = coins >= boost.coinPrice
    val missingCoins = (boost.coinPrice - coins.coerceAtLeast(0)).coerceAtLeast(0)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.07f),
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = boostIcon(boost),
                    style = MaterialTheme.typography.titleMedium,
                    color = ArcadeGold,
                    maxLines = 1
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(boost.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(boost.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecondaryGameButton(
                    text = if (canBuyWithCoins) {
                        stringResource(R.string.boost_buy_with_coins, boost.coinPrice)
                    } else {
                        stringResource(R.string.boost_missing_coins, missingCoins)
                    },
                    onClick = { onCoinClick(boost) },
                    enabled = canBuyWithCoins,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
                SecondaryGameButton(
                    text = stringResource(R.string.boost_watch_ad),
                    onClick = { onAdClick(boost) },
                    enabled = isRewardedAdReady,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
            }
        }
    }
}

/** U+FE0E forces text presentation, matching [ProfileBadge], so the glyphs take the row's tint. */
private fun boostIcon(boost: GameBoost): String = when (boost) {
    GameBoost.ExtraTime -> "⏱\uFE0E"
    GameBoost.ExtraLife -> "♥\uFE0E"
    GameBoost.ComboStart -> "⚡\uFE0E"
}

@Composable
private fun PowerUpOptionRow(
    powerUp: GamePowerUp,
    coins: Int,
    onPowerUpClick: (GamePowerUp) -> Unit
) {
    val canBuyWithCoins = coins >= powerUp.coinPrice
    val missingCoins = (powerUp.coinPrice - coins.coerceAtLeast(0)).coerceAtLeast(0)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.07f),
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = powerUpIcon(powerUp),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(powerUp.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(powerUp.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecondaryGameButton(
                    text = if (canBuyWithCoins) {
                        stringResource(R.string.power_up_buy_with_coins, powerUp.coinPrice)
                    } else {
                        stringResource(R.string.power_up_missing_coins, missingCoins)
                    },
                    onClick = { onPowerUpClick(powerUp) },
                    enabled = canBuyWithCoins,
                    modifier = Modifier
                        .height(48.dp)
                )
            }
        }
    }
}

private fun powerUpIcon(powerUp: GamePowerUp): String {
    return when (powerUp) {
        GamePowerUp.ExtraTime -> "+5"
        GamePowerUp.ExtraLife -> "+1"
        GamePowerUp.ComboProtection -> "C"
        GamePowerUp.FirstMistakeForgiveness -> "!"
    }
}
