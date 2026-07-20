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
internal fun ThemeTargetCard(
    progressionState: ProgressionState,
    modifier: Modifier = Modifier
) {
    val targetTheme = PlayerTheme.entries
        .filterNot { it in progressionState.unlockedThemes }
        .filter { it.coinPrice > 0 }
        .minByOrNull { it.coinPrice }

    if (targetTheme == null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = ReflexGamePalette.cardGlassStrong,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.32f))
        ) {
            Text(
                text = stringResource(R.string.theme_target_all_unlocked_empty),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = ReflexGamePalette.textPrimary,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val currentCoins = progressionState.coins.coerceAtLeast(0)
    val progress = (currentCoins.toFloat() / targetTheme.coinPrice.toFloat()).coerceIn(0f, 1f)
    val cappedCoins = currentCoins.coerceAtMost(targetTheme.coinPrice)
    val completionPercent = (progress * 100f).toInt().coerceIn(0, 100)
    val accent = themeAccentColor(targetTheme)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.theme_target_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(targetTheme.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = stringResource(R.string.theme_target_completion, completionPercent),
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                text = stringResource(R.string.theme_target_progress, cappedCoins, targetTheme.coinPrice),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
internal fun ThemeShopSection(
    progressionState: ProgressionState,
    selectedLanguage: AppLanguage,
    unlockedThemePopup: PlayerTheme? = null,
    popupBlocked: Boolean = false,
    onThemeUnlockPopupChange: (PlayerTheme?) -> Unit = {},
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onThemeTrial: (PlayerTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.theme_shop_title),
                style = MaterialTheme.typography.titleMedium,
                color = ReflexGamePalette.textPrimary
            )
            PlayerTheme.entries.forEach { theme ->
                ThemeCard(
                    theme = theme,
                    selected = progressionState.activeTheme == theme,
                    trialActive = progressionState.trialTheme == theme,
                    unlocked = theme in progressionState.unlockedThemes,
                    canBuy = progressionState.coins >= theme.coinPrice,
                    currentCoins = progressionState.coins,
                    onSelect = { onThemeSelect(theme) },
                    onBuy = {
                        onThemeBuy(theme)
                        if (progressionState.coins >= theme.coinPrice) {
                            onThemeUnlockPopupChange(theme)
                        }
                    },
                    onTrial = { onThemeTrial(theme) }
                )
            }
        }

        if (!popupBlocked) {
            unlockedThemePopup?.let { theme ->
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(GameDialogScrimColor.copy(alpha = 0.72f))
                        .clickable { onThemeUnlockPopupChange(null) }
                )
                ThemeUnlockCelebration(
                    theme = theme,
                    selectedLanguage = selectedLanguage,
                    onDismiss = { onThemeUnlockPopupChange(null) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 38.dp)
                )
            }
        }
    }
}
