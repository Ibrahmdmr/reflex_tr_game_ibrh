package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun SettingsTabContent(
    playerProfile: PlayerProfile,
    progressionState: ProgressionState,
    selectedLanguage: AppLanguage,
    isSoundEnabled: Boolean,
    isEffectSoundEnabled: Boolean,
    isVibrationEnabled: Boolean,
    isDailyRewardNotificationEnabled: Boolean,
    isStreakNotificationEnabled: Boolean,
    isNewMissionNotificationEnabled: Boolean,
    isNotificationPermissionGranted: Boolean,
    isStorePreviewMode: Boolean,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onEffectSoundEnabledChange: (Boolean) -> Unit,
    onVibrationEnabledChange: (Boolean) -> Unit,
    onDailyRewardNotificationChange: (Boolean) -> Unit,
    onStreakNotificationChange: (Boolean) -> Unit,
    onNewMissionNotificationChange: (Boolean) -> Unit,
    onOpenOnboarding: () -> Unit,
    onStorePreviewModeChange: (Boolean) -> Unit,
    onResetModeTips: () -> Unit,
    onRateAppClick: () -> Unit,
    onEditNameClick: () -> Unit
) {
    val rank = rankFor(level = progressionState.level)
    val uriHandler = LocalUriHandler.current
    val unlockedAchievements = progressionState.achievements.count { it.unlocked }
    val totalScore = progressionState.totalHits

    Text(
        text = stringResource(R.string.settings_title),
        style = MaterialTheme.typography.titleLarge,
        color = ReflexGamePalette.textPrimary,
        textAlign = TextAlign.Center
    )

    SettingsSectionCard(title = stringResource(R.string.settings_sound_title)) {
        SettingsToggleRow(
            title = stringResource(R.string.settings_game_sounds),
            description = stringResource(R.string.settings_game_sounds_description),
            checked = isSoundEnabled,
            onCheckedChange = onSoundEnabledChange
        )
        SettingsToggleRow(
            title = stringResource(R.string.settings_effect_sounds),
            description = stringResource(R.string.settings_effect_sounds_description),
            checked = isEffectSoundEnabled,
            onCheckedChange = onEffectSoundEnabledChange
        )
        SettingsToggleRow(
            title = stringResource(R.string.settings_vibration),
            description = stringResource(R.string.settings_vibration_description),
            checked = isVibrationEnabled,
            onCheckedChange = onVibrationEnabledChange
        )
    }

    SettingsSectionCard(title = stringResource(R.string.settings_language_title)) {
        LanguageSelectionSection(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = onLanguageSelected
        )
    }

    SettingsSectionCard(title = stringResource(R.string.settings_account_title)) {
        SettingsInfoRow(
            title = stringResource(R.string.settings_player_name),
            value = playerProfile.name.ifBlank { stringResource(R.string.leaderboard_you) }
        )
        SettingsInfoRow(
            title = stringResource(R.string.settings_level),
            value = stringResource(R.string.level_value, progressionState.level)
        )
        SettingsInfoRow(
            title = stringResource(R.string.settings_rank),
            value = stringResource(rank.titleRes)
        )
        SecondaryGameButton(
            text = stringResource(R.string.profile_change_name),
            onClick = onEditNameClick
        )
    }

    SettingsSectionCard(title = stringResource(R.string.settings_notifications_title)) {
        NotificationStatusMessage(
            isPermissionGranted = isNotificationPermissionGranted,
            hasEnabledToggle = isDailyRewardNotificationEnabled || isStreakNotificationEnabled || isNewMissionNotificationEnabled
        )
        NotificationToggleRow(
            title = stringResource(R.string.settings_daily_reward_reminder),
            description = stringResource(R.string.settings_daily_reward_reminder_description),
            checked = isDailyRewardNotificationEnabled,
            onCheckedChange = onDailyRewardNotificationChange
        )
        NotificationToggleRow(
            title = stringResource(R.string.settings_streak_reminder),
            description = stringResource(R.string.settings_streak_reminder_description),
            checked = isStreakNotificationEnabled,
            onCheckedChange = onStreakNotificationChange
        )
        NotificationToggleRow(
            title = stringResource(R.string.settings_new_mission_notification),
            description = stringResource(R.string.settings_new_mission_notification_description),
            checked = isNewMissionNotificationEnabled,
            onCheckedChange = onNewMissionNotificationChange
        )
    }

    SettingsSectionCard(title = stringResource(R.string.settings_data_title)) {
        SettingsInfoRow(
            title = stringResource(R.string.settings_app_version),
            value = getDisplayVersionName()
        )
        SettingsInfoRow(
            title = stringResource(R.string.settings_total_games),
            value = progressionState.totalGames.toString()
        )
        SettingsInfoRow(
            title = stringResource(R.string.settings_total_score),
            value = totalScore.toString()
        )
        SettingsInfoRow(
            title = stringResource(R.string.settings_unlocked_achievements),
            value = stringResource(R.string.achievement_summary_value, unlockedAchievements, progressionState.achievements.size)
        )
    }

    if (shouldShowDeveloperTools()) {
        SettingsSectionCard(title = stringResource(R.string.store_preview_settings_title)) {
            SettingsActionButton(
                text = stringResource(
                    if (isStorePreviewMode) {
                        R.string.store_preview_disable
                    } else {
                        R.string.store_preview_enable
                    }
                ),
                onClick = { onStorePreviewModeChange(!isStorePreviewMode) }
            )
        }
    }

    SettingsSectionCard(title = stringResource(R.string.settings_support_title)) {
        SettingsActionButton(
            text = stringResource(R.string.settings_open_onboarding),
            onClick = onOpenOnboarding
        )
        SettingsActionButton(
            text = stringResource(R.string.settings_reset_mode_tips),
            onClick = onResetModeTips
        )
        SettingsActionButton(
            text = stringResource(R.string.settings_contact_us),
            onClick = { runCatching { uriHandler.openUri("mailto:support@reflexavi.app") } }
        )
        SettingsActionButton(
            text = stringResource(R.string.settings_privacy_policy),
            onClick = { runCatching { uriHandler.openUri("https://reflexavi.app/privacy") } }
        )
        Text(
            text = stringResource(R.string.settings_rate_app_prompt_title),
            style = MaterialTheme.typography.labelLarge,
            color = ReflexGamePalette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(R.string.settings_rate_app_prompt_message),
            style = MaterialTheme.typography.bodySmall,
            color = ReflexGamePalette.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        SettingsActionButton(
            text = stringResource(R.string.settings_rate_app),
            onClick = onRateAppClick
        )
    }
}
