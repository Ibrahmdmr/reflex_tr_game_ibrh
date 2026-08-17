package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun ThemeShopSection(
    progressionState: ProgressionState,
    selectedLanguage: AppLanguage,
    modifier: Modifier = Modifier,
    unlockedThemePopup: PlayerTheme? = null,
    popupBlocked: Boolean = false,
    onThemeUnlockPopupChange: (PlayerTheme?) -> Unit = {},
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onThemeTrial: (PlayerTheme) -> Unit
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
