package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

private const val THEME_UNLOCK_CELEBRATION_DURATION_MS = 2_000L

@Composable
internal fun ThemeCard(
    theme: PlayerTheme,
    selected: Boolean,
    trialActive: Boolean,
    unlocked: Boolean,
    canBuy: Boolean,
    currentCoins: Int,
    onSelect: () -> Unit,
    onBuy: () -> Unit,
    onTrial: () -> Unit
) {
    val spec = themeVisualSpec(theme)
    val accent = spec.primary
    val isPrestigeTheme = theme == PlayerTheme.MatrixGreen
    val safeCoinCount = currentCoins.coerceAtLeast(0)
    val remainingCoins = (theme.coinPrice - safeCoinCount).coerceAtLeast(0)
    val unlockProgress = if (theme.coinPrice > 0) {
        (safeCoinCount.toFloat() / theme.coinPrice.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }
    val rarityGlow = when (theme.rarity) {
        ThemeRarity.Common -> 0.18f
        ThemeRarity.Rare -> 0.28f
        ThemeRarity.Epic -> 0.42f
        ThemeRarity.Legendary -> 0.58f
        ThemeRarity.Mythic -> 0.82f
    }
    val shouldAnimatePulse = selected || trialActive || (canBuy && !unlocked) || theme.rarity == ThemeRarity.Mythic
    val pulse = if (shouldAnimatePulse) {
        val animatedPulse by rememberInfiniteTransition(label = "theme_card_pulse").animateFloat(
            initialValue = 0.75f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "theme_card_pulse_value"
        )
        animatedPulse
    } else {
        1f
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shadowElevation = when {
                    selected || trialActive -> 26f * pulse
                    canBuy && !unlocked -> 18f * pulse
                    theme.rarity == ThemeRarity.Mythic -> 24f * pulse
                    else -> 8f
                }
            },
        color = when {
            selected || trialActive -> accent.copy(alpha = 0.18f + rarityGlow * 0.08f)
            canBuy && !unlocked -> ArcadeGold.copy(alpha = 0.08f)
            else -> ReflexGamePalette.cardGlassStrong
        },
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            width = when {
                selected || trialActive -> 2.dp
                theme.rarity == ThemeRarity.Mythic -> 1.8.dp
                else -> 1.dp
            },
            color = when {
                selected || trialActive -> accent.copy(alpha = 0.86f)
                canBuy && !unlocked -> ArcadeGold.copy(alpha = 0.62f)
                else -> accent.copy(alpha = 0.26f + rarityGlow * 0.38f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemePreview(theme = theme, pulse = pulse)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(theme.titleRes),
                            style = MaterialTheme.typography.titleSmall,
                            color = ReflexGamePalette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            color = accent.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
                        ) {
                            Text(
                                text = stringResource(theme.rarity.titleRes),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = ReflexGamePalette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        if (selected) {
                            ThemeStatusBadge(
                                text = stringResource(R.string.theme_selected),
                                color = ArcadeTeal
                            )
                        }
                        if (isPrestigeTheme) {
                            ThemeStatusBadge(
                                text = stringResource(R.string.theme_legendary_label),
                                color = ArcadeGold
                            )
                        }
                        if (isPrestigeTheme) {
                            ThemeStatusBadge(
                                text = stringResource(R.string.theme_prestige_label),
                                color = ArcadeGold
                            )
                        }
                    }
                    Text(
                        text = stringResource(theme.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            trialActive -> stringResource(R.string.theme_trial_active)
                            selected -> stringResource(R.string.theme_selected)
                            unlocked -> stringResource(R.string.theme_unlocked)
                            canBuy -> stringResource(R.string.theme_price_affordable, theme.coinPrice)
                            else -> stringResource(R.string.theme_price_locked, theme.coinPrice)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (canBuy || unlocked || selected || trialActive) accent else ReflexGamePalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!unlocked && theme.coinPrice > 0) {
                        Text(
                            text = stringResource(R.string.theme_target_remaining, remainingCoins),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (canBuy) ArcadeGold else ReflexGamePalette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        LinearProgressIndicator(
                            progress = { unlockProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = accent,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                    }
                }
                Column(
                    modifier = Modifier.width(132.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!unlocked && canBuy) {
                        PrimaryGameButton(
                            text = stringResource(R.string.buy_theme),
                            onClick = onBuy,
                            height = 48.dp
                        )
                    } else {
                        SecondaryGameButton(
                            text = when {
                                selected -> stringResource(R.string.theme_selected)
                                unlocked -> stringResource(R.string.select_theme)
                                else -> stringResource(R.string.theme_insufficient_coins)
                            },
                            enabled = when {
                                selected -> false
                                unlocked -> true
                                else -> false
                            },
                            onClick = when {
                                unlocked -> onSelect
                                else -> onBuy
                            }
                        )
                    }
                    if (!unlocked && !trialActive) {
                        SecondaryGameButton(
                            text = stringResource(R.string.theme_try_ad),
                            onClick = onTrial
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ThemeStatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.34f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun ThemePreview(
    theme: PlayerTheme,
    pulse: Float
) {
    val spec = themeVisualSpec(theme)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        spec.backgroundTop,
                        spec.primary.copy(alpha = 0.78f),
                        spec.backgroundBottom
                    )
                )
            )
    ) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .offset(x = (24 + index * 52).dp, y = (12 + (index % 3) * 13).dp)
                    .size((10 + index * 2).dp)
                    .clip(CircleShape)
                    .background(spec.secondary.copy(alpha = 0.16f + pulse * 0.18f))
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 26.dp)
                .size(46.dp)
                .graphicsLayer {
                    scaleX = 0.92f + pulse * 0.08f
                    scaleY = 0.92f + pulse * 0.08f
                    shadowElevation = 28f
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.88f),
                            spec.primary.copy(alpha = 0.72f),
                            spec.secondary
                        )
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.62f), CircleShape)
        )
        Text(
            text = stringResource(spec.previewLabelRes),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            style = MaterialTheme.typography.labelLarge,
            color = ReflexGamePalette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
