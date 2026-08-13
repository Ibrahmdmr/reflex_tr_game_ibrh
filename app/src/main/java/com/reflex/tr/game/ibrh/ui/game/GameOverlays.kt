package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import kotlinx.coroutines.delay

internal enum class RewardVaultType {
    Coin,
    SeasonXp,
    BonusCoin,
    ThemeDiscount
}

internal data class RewardVaultFeedback(
    val type: RewardVaultType,
    val amount: Int,
    val strongGlow: Boolean,
    val triggerKey: Int
)

@Composable
internal fun RewardVaultOverlay(
    feedback: RewardVaultFeedback,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var opened by remember(feedback.triggerKey) { mutableStateOf(false) }
    LaunchedEffect(feedback.triggerKey) {
        opened = true
        delay(1_850L)
        onFinished()
    }
    val scale by animateFloatAsState(
        targetValue = if (opened) 1f else 0.72f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "reward_vault_scale"
    )
    val lidRotation by animateFloatAsState(
        targetValue = if (opened) -18f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "reward_vault_lid"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (opened) {
            if (feedback.strongGlow) 0.58f else 0.36f
        } else {
            0.08f
        },
        animationSpec = tween(durationMillis = 300),
        label = "reward_vault_glow"
    )
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(120)) + scaleIn(initialScale = 0.84f),
        exit = fadeOut(animationSpec = tween(160)) + scaleOut(targetScale = 0.92f),
        modifier = modifier
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.18f),
            shape = RoundedCornerShape(PremiumOverlayRadius),
            border = BorderStroke(
                1.dp,
                if (feedback.strongGlow) {
                    Color.White.copy(alpha = 0.5f)
                } else {
                    ReflexGamePalette.textPrimary.copy(alpha = 0.24f)
                }
            ),
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        ReflexGamePalette.textPrimary.copy(alpha = glowAlpha),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(46.dp)
                            )
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Surface(
                            color = ReflexGamePalette.textPrimary.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp),
                            modifier = Modifier
                                .size(width = 58.dp, height = 18.dp)
                                .graphicsLayer {
                                    rotationX = lidRotation
                                    translationY = if (opened) -8f else 0f
                                }
                        ) {}
                        Surface(
                            color = ReflexGamePalette.cardGlassStrong,
                            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                            border = BorderStroke(1.dp, ReflexGamePalette.textPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier.size(width = 68.dp, height = 46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.reward_vault_chest_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ReflexGamePalette.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.reward_vault_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = rewardVaultText(feedback),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun rewardVaultText(feedback: RewardVaultFeedback): String {
    return when (feedback.type) {
        RewardVaultType.Coin -> stringResource(R.string.reward_vault_coin, feedback.amount)
        RewardVaultType.SeasonXp -> stringResource(R.string.reward_vault_season_xp, feedback.amount)
        RewardVaultType.BonusCoin -> stringResource(R.string.reward_vault_bonus_coin, feedback.amount)
        RewardVaultType.ThemeDiscount -> stringResource(R.string.reward_vault_theme_discount)
    }
}

@Composable
internal fun ModeTipOverlay(
    mode: GameMode,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameDialogScrimColor.copy(alpha = 0.46f))
            .clickable(onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = ReflexGamePalette.cardGlassStrong,
            shape = RoundedCornerShape(PremiumOverlayRadius),
            border = BorderStroke(1.dp, modeTipAccent(mode).copy(alpha = 0.38f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.mode_tip_title, stringResource(mode.titleRes)),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(modeTipDescriptionRes(mode)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center
                )
                SecondaryGameButton(
                    text = stringResource(R.string.mode_tip_dont_show_again),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@StringRes
private fun modeTipDescriptionRes(mode: GameMode): Int {
    return when (mode) {
        GameMode.Classic -> R.string.mode_tip_classic
        GameMode.MovingTarget -> R.string.mode_tip_moving_target
        GameMode.FakeTarget -> R.string.mode_tip_fake_target
        GameMode.ColorReflex -> R.string.mode_tip_color_reflex
    }
}

private fun modeTipAccent(mode: GameMode): Color {
    return when (mode) {
        GameMode.Classic -> Color(0xFFFFD166)
        GameMode.MovingTarget -> Color(0xFF4D9FFF)
        GameMode.FakeTarget -> Color(0xFFFF6B8A)
        GameMode.ColorReflex -> Color(0xFF46F0C2)
    }
}

@Composable
internal fun QuickGameSelectedOverlay(
    mode: GameMode,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumSurfaceRadius),
        border = BorderStroke(1.dp, modeTipAccent(mode).copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.quick_game_title),
                style = MaterialTheme.typography.labelLarge,
                color = modeTipAccent(mode),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.quick_game_selected_mode, stringResource(mode.titleRes)),
                style = MaterialTheme.typography.titleMedium,
                color = ReflexGamePalette.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun RewardContinueGraceOverlay(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumOverlayRadius),
        tonalElevation = 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.14f)
        )
    ) {
        Text(
            text = stringResource(R.string.rewarded_continue_grace),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
    }
}
