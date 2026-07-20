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

private fun appVersionLabel(): String {
    return if (BuildConfig.DEBUG) {
        BuildConfig.VERSION_NAME
    } else {
        BuildConfig.VERSION_NAME.substringBefore("-debug")
    }
}

@Composable
internal fun SettingsTabContent(
    playerProfile: PlayerProfile,
    progressionState: ProgressionState,
    bestScoresByMode: Map<GameMode, Int>,
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
    onRateAppClick: () -> Unit,
    onEditNameClick: () -> Unit
) {
    val rank = rankFor(score = bestScoresByMode.values.maxOrNull() ?: 0, level = progressionState.level)
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
            value = appVersionLabel()
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

    if (BuildConfig.DEBUG) {
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
