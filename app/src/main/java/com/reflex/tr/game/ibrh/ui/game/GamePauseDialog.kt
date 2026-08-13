package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun ExitGameDialog(
    selectedLanguage: AppLanguage,
    mode: GameMode,
    score: Int,
    timeLeftSeconds: Int,
    combo: Int,
    theme: PlayerTheme,
    onContinueClick: () -> Unit,
    onRetryClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val title = localizedStringResource(R.string.exit_game_title, selectedLanguage)
    val message = localizedStringResource(R.string.exit_game_message, selectedLanguage)
    val continueText = localizedStringResource(R.string.continue_game, selectedLanguage)
    val retryText = localizedStringResource(R.string.pause_restart, selectedLanguage)
    val homeText = localizedStringResource(R.string.back_to_home, selectedLanguage)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(GameDialogScrimColor.copy(alpha = 0.9f))
            .navigationBarsPadding()
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        val scrollState = rememberScrollState()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .heightIn(max = maxHeight - 24.dp),
            color = ReflexGamePalette.cardGlassStrong,
            shape = RoundedCornerShape(PremiumOverlayRadius),
            border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.36f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                PauseStatsGrid(
                    selectedLanguage = selectedLanguage,
                    mode = mode,
                    score = score,
                    timeLeftSeconds = timeLeftSeconds,
                    combo = combo,
                    theme = theme
                )
                PrimaryGameButton(
                    text = continueText,
                    onClick = onContinueClick,
                    modifier = Modifier.fillMaxWidth()
                )
                SecondaryGameButton(
                    text = retryText,
                    onClick = onRetryClick,
                    modifier = Modifier.fillMaxWidth()
                )
                SecondaryGameButton(
                    text = homeText,
                    onClick = onHomeClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PauseStatsGrid(
    selectedLanguage: AppLanguage,
    mode: GameMode,
    score: Int,
    timeLeftSeconds: Int,
    combo: Int,
    theme: PlayerTheme
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PauseStatChip(
                label = localizedStringResource(R.string.pause_active_mode, selectedLanguage),
                value = localizedStringResource(mode.titleRes, selectedLanguage),
                accentColor = ArcadeBlue,
                modifier = Modifier.weight(1f)
            )
            PauseStatChip(
                label = localizedStringResource(R.string.pause_selected_theme, selectedLanguage),
                value = localizedStringResource(theme.titleRes, selectedLanguage),
                accentColor = ArcadeTeal,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PauseStatChip(
                label = localizedStringResource(R.string.score, selectedLanguage),
                value = score.coerceAtLeast(0).toString(),
                accentColor = ArcadeGold,
                modifier = Modifier.weight(1f)
            )
            PauseStatChip(
                label = localizedStringResource(R.string.time, selectedLanguage),
                value = localizedStringResource(R.string.seconds_short, selectedLanguage, timeLeftSeconds.coerceAtLeast(0)),
                accentColor = ArcadeBlue,
                modifier = Modifier.weight(1f)
            )
            PauseStatChip(
                label = localizedStringResource(R.string.combo, selectedLanguage),
                value = localizedStringResource(R.string.combo_short_value, selectedLanguage, combo.coerceAtLeast(0)),
                accentColor = ArcadeGold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PauseStatChip(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accentColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(PremiumCompactRadius),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.32f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
