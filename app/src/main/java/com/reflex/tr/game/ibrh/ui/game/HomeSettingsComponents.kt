package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.BuildConfig
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

/** The version without its build-type suffix, so Settings reads "1.2.2" in every variant. */
internal fun getDisplayVersionName(): String = BuildConfig.VERSION_NAME.substringBefore('-')

internal fun shouldShowDeveloperTools(): Boolean = BuildConfig.DEBUG

@Composable
internal fun SettingsSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(PremiumCardPadding),
            verticalArrangement = Arrangement.spacedBy(PremiumSectionSpacing)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = ArcadeGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            content()
        }
    }
}

@Composable
internal fun NotificationStatusMessage(
    isPermissionGranted: Boolean,
    hasEnabledToggle: Boolean
) {
    val text = when {
        !isPermissionGranted -> stringResource(R.string.settings_notifications_permission_closed_short)
        !hasEnabledToggle -> stringResource(R.string.settings_notifications_all_disabled)
        else -> stringResource(R.string.settings_notifications_description)
    }
    val accent = if (isPermissionGranted) ArcadeTeal else ArcadeGold
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(PremiumCompactRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = ReflexGamePalette.textPrimary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun NotificationToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.07f),
        shape = RoundedCornerShape(PremiumCompactRadius),
        border = BorderStroke(
            1.dp,
            if (checked) ArcadeTeal.copy(alpha = 0.36f) else Color.White.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    NotificationStateChip(checked = checked)
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
internal fun NotificationStateChip(
    checked: Boolean
) {
    val accent = if (checked) ArcadeTeal else Color.White.copy(alpha = 0.42f)
    Surface(
        color = accent.copy(alpha = if (checked) 0.18f else 0.08f),
        shape = RoundedCornerShape(PremiumPillRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Text(
            text = stringResource(if (checked) R.string.settings_toggle_on else R.string.settings_toggle_off),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (checked) ArcadeTeal else ReflexGamePalette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
internal fun SettingsInfoRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = ReflexGamePalette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            modifier = Modifier.weight(1.1f),
            style = MaterialTheme.typography.labelMedium,
            color = ReflexGamePalette.textPrimary,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun SettingsActionButton(
    text: String,
    onClick: () -> Unit
) {
    SecondaryGameButton(
        text = text,
        onClick = onClick
    )
}


@Composable
internal fun LanguageSelectionSection(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.language_selection_title),
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LanguageChip(
                text = stringResource(R.string.language_turkish),
                selected = selectedLanguage == AppLanguage.Turkish,
                onClick = { onLanguageSelected(AppLanguage.Turkish) },
                modifier = Modifier.weight(1f)
            )
            LanguageChip(
                text = stringResource(R.string.language_english),
                selected = selectedLanguage == AppLanguage.English,
                onClick = { onLanguageSelected(AppLanguage.English) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun LanguageChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) ArcadeGold else ReflexGamePalette.neonBlue
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = color.copy(alpha = if (selected) 0.22f else 0.1f),
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, color.copy(alpha = if (selected) 0.56f else 0.24f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleSmall,
            color = ReflexGamePalette.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}
