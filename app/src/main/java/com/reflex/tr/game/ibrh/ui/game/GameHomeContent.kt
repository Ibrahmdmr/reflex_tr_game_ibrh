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

private const val THEME_UNLOCK_CELEBRATION_DURATION_MS = 2_000L
private const val PLAYER_NAME_MAX_LENGTH = 12
private const val XP_PER_LEVEL = 250
private const val LEVEL_UP_POPUP_DURATION_MS = 1_800L
private const val LEVEL_UP_BONUS_COINS = 50

@Composable
fun HomeContent(
    bestScore: Int,
    bestScoresByMode: Map<GameMode, Int>,
    selectedMode: GameMode,
    dailyFeaturedMode: DailyFeaturedModeState,
    dailyChallengeState: DailyChallengeState,
    progressionState: ProgressionState,
    shouldAutoShowDailyRewardDialog: Boolean,
    playerProfile: PlayerProfile,
    leaderboardSnapshot: LeaderboardSnapshot,
    rewardedAdUiState: RewardedAdUiState,
    isSoundEnabled: Boolean,
    isEffectSoundEnabled: Boolean,
    isVibrationEnabled: Boolean,
    isDailyRewardNotificationEnabled: Boolean,
    isStreakNotificationEnabled: Boolean,
    isNewMissionNotificationEnabled: Boolean,
    isNotificationPermissionGranted: Boolean,
    isOnboardingCompleted: Boolean,
    isStorePreviewMode: Boolean,
    selectedLanguage: AppLanguage,
    onStartClick: () -> Unit,
    onModeStartClick: (GameMode) -> Unit,
    onHowToPlayClick: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSoundToggleClick: () -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onEffectSoundEnabledChange: (Boolean) -> Unit,
    onVibrationEnabledChange: (Boolean) -> Unit,
    onDailyRewardNotificationChange: (Boolean) -> Unit,
    onStreakNotificationChange: (Boolean) -> Unit,
    onNewMissionNotificationChange: (Boolean) -> Unit,
    onOpenOnboarding: () -> Unit,
    onDailyRewardClaim: () -> Unit,
    onDailyRewardDialogShown: () -> Unit,
    onDailyChallengeClaim: () -> Unit,
    onSeasonRewardClaim: (Int) -> Unit,
    onSeasonXpBoostClick: () -> Unit,
    onSeasonMissionClaim: (String) -> Unit,
    onDailyStreakProtect: () -> Unit,
    onCoinChestClick: () -> Unit,
    onShopCoinRewardClick: () -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit,
    onAchievementClaim: (String) -> Unit,
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onThemeTrial: (PlayerTheme) -> Unit,
    onPlayerNameChange: (String) -> Boolean,
    onPlayerTitleSelect: (PlayerTitle) -> Unit,
    onLeaderboardModeSelected: (GameMode) -> Unit,
    onLeaderboardPeriodSelected: (LeaderboardPeriod) -> Unit,
    onLeaderboardRefresh: () -> Unit,
    onLeaderboardOpenedForMission: () -> Unit,
    onShopOpenedForMission: () -> Unit,
    onStorePreviewModeChange: (Boolean) -> Unit,
    onRateAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompactHeight = maxHeight <= 780.dp
        val contentScrollState = rememberScrollState()
        val panelPadding = if (isCompactHeight) 10.dp else 14.dp
        val contentSpacing = if (isCompactHeight) 8.dp else 10.dp
        var selectedHomeTab by remember { mutableStateOf(HomeTab.Play) }
        var showDailyRewardPopup by remember(progressionState.dailyReward.canClaim) {
            mutableStateOf(shouldAutoShowDailyRewardDialog)
        }
        LaunchedEffect(shouldAutoShowDailyRewardDialog) {
            if (shouldAutoShowDailyRewardDialog && progressionState.dailyReward.canClaim) {
                showDailyRewardPopup = true
                onDailyRewardDialogShown()
            }
        }
        var showPlayerNameDialog by remember(
            playerProfile.name,
            playerProfile.hasCompletedNamePrompt,
            showDailyRewardPopup
        ) {
            mutableStateOf(!showDailyRewardPopup && !playerProfile.hasName && !playerProfile.hasCompletedNamePrompt)
        }
        var dismissedLevelUp by remember { mutableStateOf<Int?>(null) }
        val levelUp = progressionState.lastLevelUp

        BackHandler(enabled = selectedHomeTab != HomeTab.Play) {
            selectedHomeTab = if (selectedHomeTab.showInBottomNav) {
                HomeTab.Play
            } else {
                HomeTab.Profile
            }
        }

        LaunchedEffect(levelUp) {
            if (levelUp != null && dismissedLevelUp != levelUp) {
                delay(LEVEL_UP_POPUP_DURATION_MS)
                dismissedLevelUp = levelUp
            }
        }

        if (showDailyRewardPopup) {
            DailyRewardPopup(
                state = progressionState.dailyReward,
                selectedLanguage = selectedLanguage,
                onClaimClick = {
                    onDailyRewardClaim()
                    showDailyRewardPopup = false
                },
                onProtectClick = {
                    onDailyStreakProtect()
                    showDailyRewardPopup = false
                },
                onDismiss = { showDailyRewardPopup = false }
            )
        }

        if (!showDailyRewardPopup && showPlayerNameDialog) {
            PlayerNameDialog(
                currentName = playerProfile.name,
                hasCurrentName = playerProfile.hasName,
                onSave = { name ->
                    val saved = onPlayerNameChange(name)
                    if (saved) showPlayerNameDialog = false
                    saved
                },
                onDismiss = {
                    if (playerProfile.hasName) showPlayerNameDialog = false
                }
            )
        }

        if (!showDailyRewardPopup && levelUp != null && dismissedLevelUp != levelUp) {
            LevelUpPopup(
                level = levelUp,
                coinBonus = LEVEL_UP_BONUS_COINS,
                onDismiss = { dismissedLevelUp = levelUp }
            )
        }

        GamePanelCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight),
            contentPadding = panelPadding,
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(contentSpacing)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(contentScrollState)
                            .padding(bottom = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(contentSpacing)
                    ) {
                        HomeHeader(
                            isCompactHeight = isCompactHeight,
                            isSoundEnabled = isSoundEnabled,
                            onSoundToggleClick = onSoundToggleClick
                        )

                        if (!selectedHomeTab.showInBottomNav) {
                            ProfileSubPageBackButton(
                                onClick = { selectedHomeTab = HomeTab.Profile }
                            )
                        }

                        when (selectedHomeTab) {
                            HomeTab.Play -> PlayTabContent(
                                bestScore = bestScore,
                                bestScoresByMode = bestScoresByMode,
                                selectedMode = selectedMode,
                                dailyFeaturedMode = dailyFeaturedMode,
                                dailyChallengeState = dailyChallengeState,
                                rewardedAdUiState = rewardedAdUiState,
                                progressionState = progressionState,
                                isOnboardingCompleted = isOnboardingCompleted,
                                onModeStartClick = onModeStartClick,
                                onHowToPlayClick = onHowToPlayClick,
                                onDailyStreakProtect = onDailyStreakProtect,
                                onDailyRewardCardClick = { showDailyRewardPopup = true },
                                onDailyChallengeClaim = onDailyChallengeClaim,
                                onDailyChallengeDoubleRewardClick = onDailyChallengeDoubleRewardClick
                            )

                            HomeTab.Rewards -> RewardsTabContent(
                                dailyChallengeState = dailyChallengeState,
                                progressionState = progressionState,
                                rewardedAdUiState = rewardedAdUiState,
                                onDailyRewardClaim = onDailyRewardClaim,
                                onDailyStreakProtect = onDailyStreakProtect,
                                onDailyRewardCardClick = { showDailyRewardPopup = true },
                                onCoinChestClick = onCoinChestClick,
                                onDailyChallengeClaim = onDailyChallengeClaim,
                                onDailyChallengeDoubleRewardClick = onDailyChallengeDoubleRewardClick,
                                onAchievementClaim = onAchievementClaim
                            )

                            HomeTab.Achievements -> AchievementsTabContent(
                                progressionState = progressionState,
                                onAchievementClaim = onAchievementClaim
                            )

                            HomeTab.Season -> SeasonTabContent(
                                season = progressionState.season,
                                rewardedAdUiState = rewardedAdUiState,
                                onClaimClick = onSeasonRewardClaim,
                                onBoostClick = onSeasonXpBoostClick,
                                onMissionClaim = onSeasonMissionClaim
                            )

                            HomeTab.Profile -> ProfileTabContent(
                                bestScore = bestScore,
                                playerProfile = playerProfile,
                                progressionState = progressionState,
                                onEditNameClick = { showPlayerNameDialog = true },
                                onTitleSelect = onPlayerTitleSelect,
                                onQuickMenuSelected = { tab ->
                                    logHomeTabOpened(
                                        tab = tab,
                                        leaderboardSnapshot = leaderboardSnapshot
                                    )
                                    if (tab == HomeTab.Leaderboard) onLeaderboardOpenedForMission()
                                    if (tab == HomeTab.Shop) onShopOpenedForMission()
                                    selectedHomeTab = tab
                                }
                            )

                            HomeTab.Missions -> MissionsTabContent(
                                dailyChallengeState = dailyChallengeState,
                                progressionState = progressionState,
                                rewardedAdUiState = rewardedAdUiState,
                                onDailyRewardClaim = onDailyRewardClaim,
                                onDailyStreakProtect = onDailyStreakProtect,
                                onDailyRewardCardClick = { showDailyRewardPopup = true },
                                onDailyChallengeClaim = onDailyChallengeClaim,
                                onDailyChallengeDoubleRewardClick = onDailyChallengeDoubleRewardClick,
                                onAchievementClaim = onAchievementClaim
                            )

                            HomeTab.Shop -> ShopTabContent(
                        progressionState = progressionState,
                        selectedLanguage = selectedLanguage,
                        rewardedAdUiState = rewardedAdUiState,
                        onThemeSelect = onThemeSelect,
                        onThemeBuy = onThemeBuy,
                        onThemeTrial = onThemeTrial,
                        onCoinChestClick = onCoinChestClick,
                        onShopCoinRewardClick = onShopCoinRewardClick
                            )

                            HomeTab.Leaderboard -> LeaderboardTabContent(
                                leaderboardSnapshot = leaderboardSnapshot,
                                onModeSelected = onLeaderboardModeSelected,
                                onPeriodSelected = onLeaderboardPeriodSelected,
                                onRefreshClick = onLeaderboardRefresh
                            )

                            HomeTab.Settings -> SettingsTabContent(
                                playerProfile = playerProfile,
                                progressionState = progressionState,
                                bestScoresByMode = bestScoresByMode,
                                selectedLanguage = selectedLanguage,
                                isSoundEnabled = isSoundEnabled,
                                isEffectSoundEnabled = isEffectSoundEnabled,
                                isVibrationEnabled = isVibrationEnabled,
                                isDailyRewardNotificationEnabled = isDailyRewardNotificationEnabled,
                                isStreakNotificationEnabled = isStreakNotificationEnabled,
                                isNewMissionNotificationEnabled = isNewMissionNotificationEnabled,
                                isNotificationPermissionGranted = isNotificationPermissionGranted,
                                isStorePreviewMode = isStorePreviewMode,
                                onLanguageSelected = onLanguageSelected,
                                onSoundEnabledChange = onSoundEnabledChange,
                                onEffectSoundEnabledChange = onEffectSoundEnabledChange,
                                onVibrationEnabledChange = onVibrationEnabledChange,
                                onDailyRewardNotificationChange = onDailyRewardNotificationChange,
                                onStreakNotificationChange = onStreakNotificationChange,
                                onNewMissionNotificationChange = onNewMissionNotificationChange,
                                onOpenOnboarding = onOpenOnboarding,
                                onStorePreviewModeChange = onStorePreviewModeChange,
                                onRateAppClick = onRateAppClick,
                                onEditNameClick = { showPlayerNameDialog = true }
                            )
                        }
                    }

                    HomeBottomNavigation(
                        selectedTab = selectedHomeTab,
                        onTabSelected = { tab ->
                            if (tab != selectedHomeTab) {
                                logHomeTabOpened(
                                    tab = tab,
                                    leaderboardSnapshot = leaderboardSnapshot
                                )
                                if (tab == HomeTab.Leaderboard) onLeaderboardOpenedForMission()
                                if (tab == HomeTab.Shop) onShopOpenedForMission()
                                selectedHomeTab = tab
                            }
                        }
                    )

                    if (selectedHomeTab == HomeTab.Play) {
                        PrimaryGameButton(
                            text = stringResource(selectedMode.startButtonRes),
                            onClick = onStartClick,
                            height = if (isCompactHeight) 50.dp else 54.dp
                        )
                    }
                }
            }
        )
    }
}

private enum class HomeTab(
    val titleRes: Int,
    val icon: String,
    val showInBottomNav: Boolean = false
) {
    Play(R.string.nav_play, "▶", true),
    Rewards(R.string.nav_rewards, "★", true),
    Shop(R.string.nav_shop, "◉", true),
    Profile(R.string.nav_profile, "◆", true),
    Leaderboard(R.string.nav_leaderboard, "#"),
    Achievements(R.string.nav_achievements, "◇"),
    Missions(R.string.nav_missions, "✓"),
    Season(R.string.season_title, "S"),
    Settings(R.string.nav_settings, "⚙")
}

private fun logHomeTabOpened(
    tab: HomeTab,
    leaderboardSnapshot: LeaderboardSnapshot
) {
    when (tab) {
        HomeTab.Profile -> FirebaseGameServices.logEvent(FirebaseEvent.ProfileOpened)
        HomeTab.Shop -> FirebaseGameServices.logEvent(FirebaseEvent.ShopOpened)
        HomeTab.Leaderboard -> FirebaseGameServices.logEvent(
            event = FirebaseEvent.LeaderboardOpened,
            params = Bundle().apply {
                putString(FirebaseParam.ModeName.key, leaderboardSnapshot.selectedMode.storageKey)
            }
        )
        HomeTab.Play,
        HomeTab.Rewards,
        HomeTab.Achievements,
        HomeTab.Season,
        HomeTab.Missions,
        HomeTab.Settings -> Unit
    }
}

private fun appVersionLabel(): String {
    return if (BuildConfig.DEBUG) {
        BuildConfig.VERSION_NAME
    } else {
        BuildConfig.VERSION_NAME.substringBefore("-debug")
    }
}

@Composable
private fun HomeHeader(
    isCompactHeight: Boolean,
    isSoundEnabled: Boolean,
    onSoundToggleClick: () -> Unit
) {
    GameLogo(isCompactHeight = isCompactHeight)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(34.dp))
        Text(
            text = stringResource(R.string.game_title),
            modifier = Modifier.weight(1f),
            style = if (isCompactHeight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
            color = ReflexGamePalette.textPrimary,
            textAlign = TextAlign.Center
        )
        SoundToggleButton(
            isSoundEnabled = isSoundEnabled,
            onClick = onSoundToggleClick
        )
    }
}

@Composable
private fun ProfileSubPageBackButton(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = ArcadeBlue.copy(alpha = 0.10f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.26f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "‹",
                style = MaterialTheme.typography.titleMedium,
                color = ArcadeGold
            )
            Text(
                text = stringResource(R.string.back_to_profile),
                style = MaterialTheme.typography.labelLarge,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlayTabContent(
    bestScore: Int,
    bestScoresByMode: Map<GameMode, Int>,
    selectedMode: GameMode,
    dailyFeaturedMode: DailyFeaturedModeState,
    dailyChallengeState: DailyChallengeState,
    rewardedAdUiState: RewardedAdUiState,
    progressionState: ProgressionState,
    isOnboardingCompleted: Boolean,
    onModeStartClick: (GameMode) -> Unit,
    onHowToPlayClick: () -> Unit,
    onDailyStreakProtect: () -> Unit,
    onDailyRewardCardClick: () -> Unit,
    onDailyChallengeClaim: () -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit
) {
    Text(
        text = stringResource(R.string.game_tagline),
        style = MaterialTheme.typography.bodyMedium,
        color = ReflexGamePalette.textSecondary,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    HomeQuickStats(
        bestScore = bestScore,
        progressionState = progressionState
    )
    HomeLevelProgressCard(progressionState = progressionState)
    if (isOnboardingCompleted && !progressionState.firstTargetBonusClaimed) {
        FirstTargetCard()
    }
    SeasonMiniCard(season = progressionState.season)
    DailyModeCard(
        state = dailyFeaturedMode,
        onPlayClick = onModeStartClick
    )
    AchievementCounterCard(achievements = progressionState.achievements)
    DailyStreakMiniCard(
        state = progressionState.dailyReward,
        onClick = onDailyRewardCardClick,
        onProtectClick = onDailyStreakProtect
    )
    DailyChallengeCard(
        state = dailyChallengeState,
        rewardedAdUiState = rewardedAdUiState,
        onClaimClick = onDailyChallengeClaim,
        onDoubleRewardClick = onDailyChallengeDoubleRewardClick
    )
    ThemeTargetCard(progressionState = progressionState)
    GameModeSection(
        bestScoresByMode = bestScoresByMode,
        selectedMode = selectedMode,
        onModeStartClick = onModeStartClick
    )
    HowToPlayEntryCard(onClick = onHowToPlayClick)
}

@Composable
private fun FirstTargetCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(ArcadeGold.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    style = MaterialTheme.typography.titleSmall,
                    color = ArcadeGold,
                    maxLines = 1
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.first_target_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.first_target_description, FirstTargetBonusCoins),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DailyModeCard(
    state: DailyFeaturedModeState,
    onPlayClick: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = modeAccentColor(state.mode)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.16f),
                            ReflexGamePalette.neonPurple.copy(alpha = 0.10f)
                        )
                    )
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.22f))
                        .border(1.dp, accent.copy(alpha = 0.46f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = modeIcon(state.mode),
                        style = MaterialTheme.typography.titleMedium,
                        color = accent,
                        maxLines = 1
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.daily_mode_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(state.mode.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(state.mode.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    color = accent.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.42f))
                ) {
                    Text(
                        text = stringResource(R.string.daily_mode_bonus),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ReflexGamePalette.textPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayClick(state.mode) },
                color = accent.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
            ) {
                Text(
                    text = stringResource(R.string.daily_mode_play),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SeasonMiniCard(
    season: SeasonState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.season_card_title, season.seasonNumber, season.level),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.season_days_left, season.remainingDays),
                    style = MaterialTheme.typography.labelMedium,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (season.isXpBoostActive) {
                Text(
                    text = stringResource(R.string.season_xp_boost_active_short),
                    style = MaterialTheme.typography.labelMedium,
                    color = ArcadeTeal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LinearProgressIndicator(
                progress = { season.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = ArcadeGold,
                trackColor = Color.White.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
private fun SeasonTabContent(
    season: SeasonState,
    rewardedAdUiState: RewardedAdUiState,
    onClaimClick: (Int) -> Unit,
    onBoostClick: () -> Unit,
    onMissionClaim: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.season_title),
        style = MaterialTheme.typography.titleLarge,
        color = ReflexGamePalette.textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.season_card_title, season.seasonNumber, season.level),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.season_days_left, season.remainingDays),
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcadeGold
                )
            }
            LinearProgressIndicator(
                progress = { season.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = ArcadeGold,
                trackColor = Color.White.copy(alpha = 0.12f)
            )
            Text(
                text = stringResource(R.string.season_next_reward, stringResource(season.nextReward.kind.titleRes)),
                style = MaterialTheme.typography.bodyMedium,
                color = ReflexGamePalette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    SeasonXpBoostCard(
        season = season,
        rewardedAdUiState = rewardedAdUiState,
        onBoostClick = onBoostClick
    )
    SeasonMissionSection(
        missions = season.missions,
        onMissionClaim = onMissionClaim
    )
    season.rewards.forEach { reward ->
        SeasonRewardCard(
            reward = reward,
            unlocked = reward.level <= season.level,
            onClaimClick = onClaimClick
        )
    }
}

@Composable
private fun SeasonXpBoostCard(
    season: SeasonState,
    rewardedAdUiState: RewardedAdUiState,
    onBoostClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.38f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.season_xp_boost_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (season.isXpBoostActive) {
                            stringResource(R.string.season_xp_boost_active, season.xpBoostRemainingMinutes)
                        } else {
                            stringResource(R.string.season_xp_boost_description, SeasonXpBoostBonusPercent)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = stringResource(R.string.season_xp_boost_bonus, SeasonXpBoostBonusPercent),
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcadeTeal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SecondaryGameButton(
                text = when {
                    rewardedAdUiState.isShowing || rewardedAdUiState.isLoading -> stringResource(R.string.rewarded_loading)
                    season.isXpBoostActive -> stringResource(R.string.season_xp_boost_refresh)
                    else -> stringResource(R.string.season_xp_boost_watch_ad)
                },
                enabled = rewardedAdUiState.isReady && !rewardedAdUiState.isShowing && !rewardedAdUiState.isLoading,
                isLoading = rewardedAdUiState.isShowing || rewardedAdUiState.isLoading,
                onClick = onBoostClick
            )
        }
    }
}

@Composable
private fun SeasonMissionSection(
    missions: List<SeasonMissionState>,
    onMissionClaim: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.season_missions_title),
        style = MaterialTheme.typography.titleMedium,
        color = ReflexGamePalette.textPrimary,
        modifier = Modifier.fillMaxWidth()
    )
    missions.forEach { mission ->
        SeasonMissionCard(
            mission = mission,
            onClaimClick = { onMissionClaim(mission.id) }
        )
    }
}

@Composable
private fun SeasonMissionCard(
    mission: SeasonMissionState,
    onClaimClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.07f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (mission.completed && !mission.claimed) ArcadeGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f)
        )
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(mission.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(mission.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = stringResource(R.string.season_mission_reward_value, mission.rewardSeasonXp),
                    style = MaterialTheme.typography.labelMedium,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LinearProgressIndicator(
                progress = { mission.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = if (mission.completed) ArcadeGold else ArcadeBlue,
                trackColor = Color.White.copy(alpha = 0.12f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.season_mission_progress,
                        mission.progress.coerceAtMost(mission.target),
                        mission.target
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SecondaryGameButton(
                    text = when {
                        mission.claimed -> stringResource(R.string.claimed)
                        mission.completed -> stringResource(R.string.claim_reward)
                        else -> stringResource(R.string.season_mission_in_progress)
                    },
                    enabled = mission.completed && !mission.claimed,
                    onClick = onClaimClick,
                    modifier = Modifier.width(132.dp)
                )
            }
        }
    }
}

@Composable
private fun SeasonRewardCard(
    reward: SeasonRewardState,
    unlocked: Boolean,
    onClaimClick: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (reward.premium) ArcadeGold.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.07f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (reward.premium) ArcadeGold.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reward.level.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (reward.premium) ArcadeGold else ReflexGamePalette.textPrimary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(reward.kind.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.season_reward_coin_value, reward.coinReward),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SecondaryGameButton(
                text = when {
                    reward.claimed -> stringResource(R.string.claimed)
                    unlocked -> stringResource(R.string.claim_reward)
                    else -> stringResource(R.string.season_locked)
                },
                enabled = unlocked && !reward.claimed,
                onClick = { onClaimClick(reward.level) },
                modifier = Modifier.width(120.dp)
            )
        }
    }
}

@Composable
private fun ProfileTabContent(
    bestScore: Int,
    playerProfile: PlayerProfile,
    progressionState: ProgressionState,
    onEditNameClick: () -> Unit,
    onTitleSelect: (PlayerTitle) -> Unit,
    onQuickMenuSelected: (HomeTab) -> Unit
) {
    ProfileProgressCard(
        playerProfile = playerProfile,
        progressionState = progressionState,
        bestScore = bestScore,
        onEditNameClick = onEditNameClick,
        onTitleSelect = onTitleSelect
    )
    ProfileQuickMenu(onTabSelected = onQuickMenuSelected)
}

@Composable
private fun MissionsTabContent(
    dailyChallengeState: DailyChallengeState,
    progressionState: ProgressionState,
    rewardedAdUiState: RewardedAdUiState,
    onDailyRewardClaim: () -> Unit,
    onDailyStreakProtect: () -> Unit,
    onDailyRewardCardClick: () -> Unit,
    onDailyChallengeClaim: () -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit,
    onAchievementClaim: (String) -> Unit
) {
    DailyChallengeCard(
        state = dailyChallengeState,
        rewardedAdUiState = rewardedAdUiState,
        onClaimClick = onDailyChallengeClaim,
        onDoubleRewardClick = onDailyChallengeDoubleRewardClick
    )
    WeeklyChallengeCard(state = progressionState.weeklyChallenge)
    DailyRewardCard(
        state = progressionState.dailyReward,
        onClaimClick = onDailyRewardClaim,
        onProtectClick = onDailyStreakProtect,
        onCardClick = onDailyRewardCardClick
    )
    AchievementSection(
        achievements = progressionState.achievements,
        unlockedIds = progressionState.latestUnlockedAchievementIds,
        onClaimClick = onAchievementClaim
    )
}

@Composable
private fun RewardsTabContent(
    dailyChallengeState: DailyChallengeState,
    progressionState: ProgressionState,
    rewardedAdUiState: RewardedAdUiState,
    onDailyRewardClaim: () -> Unit,
    onDailyStreakProtect: () -> Unit,
    onDailyRewardCardClick: () -> Unit,
    onCoinChestClick: () -> Unit,
    onDailyChallengeClaim: () -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit,
    onAchievementClaim: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.reward_center_title),
        style = MaterialTheme.typography.titleLarge,
        color = ReflexGamePalette.textPrimary,
        textAlign = TextAlign.Center
    )
    DailyRewardCard(
        state = progressionState.dailyReward,
        onClaimClick = onDailyRewardClaim,
        onProtectClick = onDailyStreakProtect,
        onCardClick = onDailyRewardCardClick
    )
    CoinChestCard(
        state = progressionState.coinChest,
        rewardedAdUiState = rewardedAdUiState,
        onOpenClick = onCoinChestClick
    )
    MissionRewardsCard(
        dailyChallengeState = dailyChallengeState,
        weeklyChallengeState = progressionState.weeklyChallenge,
        achievements = progressionState.achievements,
        rewardedAdUiState = rewardedAdUiState,
        onDailyChallengeClaim = onDailyChallengeClaim,
        onDailyChallengeDoubleRewardClick = onDailyChallengeDoubleRewardClick,
        onAchievementClaim = onAchievementClaim
    )
}

@Composable
private fun AchievementsTabContent(
    progressionState: ProgressionState,
    onAchievementClaim: (String) -> Unit
) {
    val unlockedCount = progressionState.achievements.count { it.unlocked }
    Text(
        text = stringResource(R.string.achievements_title),
        style = MaterialTheme.typography.titleLarge,
        color = ReflexGamePalette.textPrimary,
        textAlign = TextAlign.Center
    )
    AchievementCounterCard(achievements = progressionState.achievements)
    AchievementCategory.entries.forEach { category ->
        val categoryAchievements = progressionState.achievements
            .filter { it.category == category }
            .let(::sortedAchievementsForDisplay)
        if (categoryAchievements.isNotEmpty()) {
            AchievementCategorySection(
                category = category,
                achievements = categoryAchievements,
                unlockedIds = progressionState.latestUnlockedAchievementIds,
                onClaimClick = onAchievementClaim
            )
        }
    }
    if (progressionState.achievements.isEmpty()) {
        Text(
            text = stringResource(R.string.achievements_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = ReflexGamePalette.textSecondary,
            textAlign = TextAlign.Center
        )
    } else {
        Text(
            text = stringResource(R.string.achievement_summary_value, unlockedCount, progressionState.achievements.size),
            style = MaterialTheme.typography.labelMedium,
            color = ReflexGamePalette.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SettingsTabContent(
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

@Composable
private fun SettingsSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
private fun NotificationStatusMessage(
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
        shape = RoundedCornerShape(14.dp),
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
private fun NotificationToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.055f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (checked) ArcadeTeal.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.1f)
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
                        maxLines = 1,
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
private fun NotificationStateChip(
    checked: Boolean
) {
    val accent = if (checked) ArcadeTeal else Color.White.copy(alpha = 0.42f)
    Surface(
        color = accent.copy(alpha = if (checked) 0.18f else 0.08f),
        shape = RoundedCornerShape(999.dp),
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
private fun SettingsToggleRow(
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
private fun SettingsInfoRow(
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
private fun SettingsActionButton(
    text: String,
    onClick: () -> Unit
) {
    SecondaryGameButton(
        text = text,
        onClick = onClick
    )
}

@Composable
private fun ShopTabContent(
    progressionState: ProgressionState,
    selectedLanguage: AppLanguage,
    rewardedAdUiState: RewardedAdUiState,
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onThemeTrial: (PlayerTheme) -> Unit,
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
    ThemeShopSection(
        progressionState = progressionState,
        selectedLanguage = selectedLanguage,
        onThemeSelect = onThemeSelect,
        onThemeBuy = onThemeBuy,
        onThemeTrial = onThemeTrial
    )
}

@Composable
private fun ShopCoinEarnCard(
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

@Composable
private fun LeaderboardTabContent(
    leaderboardSnapshot: LeaderboardSnapshot,
    onModeSelected: (GameMode) -> Unit,
    onPeriodSelected: (LeaderboardPeriod) -> Unit,
    onRefreshClick: () -> Unit
) {
    LeaderboardSection(
        snapshot = leaderboardSnapshot,
        onModeSelected = onModeSelected,
        onPeriodSelected = onPeriodSelected,
        onRefreshClick = onRefreshClick
    )
}

@Composable
private fun HomeQuickStats(
    bestScore: Int,
    progressionState: ProgressionState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickStatCard(
            title = stringResource(R.string.coin_wallet_title),
            value = stringResource(R.string.coin_wallet_value, progressionState.coins),
            accent = ArcadeGold,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = stringResource(R.string.profile_title),
            value = stringResource(R.string.level_value, progressionState.level),
            accent = ArcadeTeal,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = stringResource(R.string.best_score_label),
            value = bestScore.toString(),
            accent = ArcadeBlue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HomeLevelProgressCard(
    progressionState: ProgressionState,
    modifier: Modifier = Modifier
) {
    val currentLevelXp = (progressionState.level - 1) * XP_PER_LEVEL
    val nextLevelXp = progressionState.level * XP_PER_LEVEL
    val levelProgress = ((progressionState.xp - currentLevelXp).toFloat() / XP_PER_LEVEL)
        .coerceIn(0f, 1f)
    val remainingXp = (nextLevelXp - progressionState.xp).coerceAtLeast(0)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ArcadeTeal.copy(alpha = 0.11f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_level_progress_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.level_value, progressionState.level),
                    style = MaterialTheme.typography.labelMedium,
                    color = ArcadeTeal
                )
            }
            LinearProgressIndicator(
                progress = { levelProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = ArcadeTeal,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = stringResource(R.string.xp_to_next_level_value, remainingXp),
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary
            )
        }
    }
}

@Composable
private fun AchievementCounterCard(
    achievements: List<AchievementState>,
    modifier: Modifier = Modifier
) {
    val unlockedCount = achievements.count { it.unlocked }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ArcadeGold.copy(alpha = 0.10f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.28f))
    ) {
        Text(
            text = stringResource(R.string.home_achievement_counter, unlockedCount, achievements.size),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = ReflexGamePalette.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = ReflexGamePalette.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AchievementSummaryCard(
    achievements: List<AchievementState>
) {
    val unlockedCount = achievements.count { it.unlocked }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ArcadeTeal.copy(alpha = 0.12f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "★",
                style = MaterialTheme.typography.titleMedium,
                color = ArcadeGold
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.achievements_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.achievement_summary_value, unlockedCount, achievements.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ProfileQuickMenu(
    onTabSelected: (HomeTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileQuickMenuCard(
                title = stringResource(R.string.season_title),
                description = stringResource(R.string.profile_quick_season_description),
                icon = "S",
                accent = ArcadeGold,
                onClick = { onTabSelected(HomeTab.Season) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileQuickMenuCard(
                    title = stringResource(R.string.leaderboard_title),
                    description = stringResource(R.string.profile_quick_leaderboard_description),
                    icon = "#",
                    accent = ArcadeBlue,
                    onClick = { onTabSelected(HomeTab.Leaderboard) },
                    modifier = Modifier.weight(1f)
                )
                ProfileQuickMenuCard(
                    title = stringResource(R.string.achievements_title),
                    description = stringResource(R.string.profile_quick_achievements_description),
                    icon = "◇",
                    accent = ArcadeGold,
                    onClick = { onTabSelected(HomeTab.Achievements) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileQuickMenuCard(
                    title = stringResource(R.string.nav_missions),
                    description = stringResource(R.string.profile_quick_missions_description),
                    icon = "✓",
                    accent = ArcadeTeal,
                    onClick = { onTabSelected(HomeTab.Missions) },
                    modifier = Modifier.weight(1f)
                )
                ProfileQuickMenuCard(
                    title = stringResource(R.string.nav_settings),
                    description = stringResource(R.string.profile_quick_settings_description),
                    icon = "⚙",
                    accent = ArcadeCoral,
                    onClick = { onTabSelected(HomeTab.Settings) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileQuickMenuCard(
    title: String,
    description: String,
    icon: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = accent.copy(alpha = 0.11f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleSmall,
                color = accent
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.titleSmall,
                color = accent
            )
        }
    }
}

@Composable
private fun HomeBottomNavigation(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    val bottomTabs = HomeTab.entries.filter { it.showInBottomNav }
    val selectedBottomTab = if (selectedTab.showInBottomNav) selectedTab else HomeTab.Profile
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.06f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            bottomTabs.forEach { tab ->
                val selected = tab == selectedBottomTab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) },
                    color = if (selected) ArcadeBlue.copy(alpha = 0.20f) else Color.Transparent,
                    shape = RoundedCornerShape(13.dp),
                    border = if (selected) {
                        BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.52f))
                    } else {
                        null
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = tab.icon,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary
                        )
                        Text(
                            text = stringResource(tab.titleRes),
                            modifier = Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SoundToggleButton(
    isSoundEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isSoundEnabled) ArcadeTeal else ReflexGamePalette.textSecondary
    val scale by animateFloatAsState(
        targetValue = if (isSoundEnabled) 1f else 0.94f,
        animationSpec = tween(durationMillis = 160),
        label = "sound_toggle_scale"
    )
    val contentDescription = if (isSoundEnabled) {
        stringResource(R.string.sound_on)
    } else {
        stringResource(R.string.sound_off)
    }

    Surface(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (isSoundEnabled) 10f else 2f
            }
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        color = accentColor.copy(alpha = if (isSoundEnabled) 0.18f else 0.1f),
        shape = CircleShape,
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (isSoundEnabled) 0.52f else 0.26f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(
                    if (isSoundEnabled) {
                        R.drawable.ic_volume_up_24
                    } else {
                        R.drawable.ic_volume_off_24
                    }
                ),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HowToPlayEntryCard(
    onClick: () -> Unit
) {
    val interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(ArcadeGold)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.how_to_play_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.how_to_play_home_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textSecondary
                )
            }
            Text(
                text = stringResource(R.string.open_details),
                style = MaterialTheme.typography.labelMedium,
                color = ArcadeGold
            )
        }
    }
}

@Composable
private fun GameModeSection(
    bestScoresByMode: Map<GameMode, Int>,
    selectedMode: GameMode,
    onModeStartClick: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.game_modes_title),
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
        GameMode.entries.forEach { mode ->
            GameModeCard(
                mode = mode,
                bestScore = bestScoresByMode[mode] ?: 0,
                selected = mode == selectedMode,
                onClick = { onModeStartClick(mode) }
            )
        }
    }
}

@Composable
internal fun GameModeCard(
    mode: GameMode,
    onClick: () -> Unit,
    bestScore: Int? = null,
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val accentColor = modeAccentColor(mode)
    val interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.01f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "mode_card_selected_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (selected) 14f else 4f
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = if (selected) accentColor.copy(alpha = 0.18f) else ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (selected) 0.62f else 0.28f))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = modeIcon(mode),
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(mode.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(mode.descriptionRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    color = accentColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = stringResource(mode.difficultyRes),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (bestScore != null) {
                Text(
                    text = stringResource(R.string.mode_best_score_value, bestScore),
                    modifier = Modifier.padding(start = 36.dp, end = 12.dp, bottom = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun DailyChallengeCard(
    state: DailyChallengeState,
    rewardedAdUiState: RewardedAdUiState = RewardedAdUiState(),
    onClaimClick: () -> Unit = {},
    onDoubleRewardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accent = if (state.completed) ArcadeTeal else ArcadeGold
    val scale by animateFloatAsState(
        targetValue = if (state.completed) 1.01f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "daily_challenge_complete_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (state.completed) 16f else 6f
            },
        color = if (state.completed) {
            ArcadeTeal.copy(alpha = 0.12f)
        } else {
            ReflexGamePalette.cardGlassStrong
        },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = if (state.completed) 0.42f else 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.completed) "✓" else "!",
                        style = MaterialTheme.typography.titleMedium,
                        color = accent
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.daily_challenge_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (state.completed) {
                            stringResource(R.string.daily_challenge_completed_title)
                        } else {
                            stringResource(state.type.titleRes)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (state.completed) {
                            if (state.rewardClaimed) {
                                stringResource(R.string.mission_reward_daily_claimed, state.rewardCoins)
                            } else {
                                stringResource(R.string.daily_challenge_completed_description)
                            }
                        } else {
                            stringResource(state.type.descriptionRes)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    color = accent.copy(alpha = if (state.completed) 0.2f else 0.14f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = if (state.completed) 0.42f else 0.24f))
                ) {
                    Text(
                        text = if (state.completed) {
                            stringResource(R.string.daily_challenge_completed_badge)
                        } else {
                            stringResource(R.string.daily_challenge_progress, state.progress, state.target)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = stringResource(R.string.mission_reward_ready_value, state.rewardCoins),
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (state.completed && !state.rewardClaimed) {
                SecondaryGameButton(
                    text = stringResource(R.string.claim_reward),
                    onClick = onClaimClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (state.completed && state.rewardClaimed && !state.doubleRewardClaimed) {
                SecondaryGameButton(
                    text = when {
                        rewardedAdUiState.isReady -> stringResource(R.string.daily_challenge_double_reward)
                        rewardedAdUiState.isLoading || rewardedAdUiState.isShowing -> stringResource(R.string.rewarded_loading)
                        else -> stringResource(R.string.rewarded_not_ready)
                    },
                    enabled = rewardedAdUiState.isReady && !rewardedAdUiState.isShowing,
                    onClick = onDoubleRewardClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MissionRewardsCard(
    dailyChallengeState: DailyChallengeState,
    weeklyChallengeState: ChallengeState,
    achievements: List<AchievementState>,
    rewardedAdUiState: RewardedAdUiState,
    onDailyChallengeClaim: () -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit,
    onAchievementClaim: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedAchievements = achievements
        .filter { it.unlocked }
        .sortedWith(compareBy<AchievementState> { it.claimed }.thenBy { it.id })
    val hasCompletedReward = dailyChallengeState.completed ||
        weeklyChallengeState.completed ||
        completedAchievements.isNotEmpty()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        .background(ArcadeTeal.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArcadeTeal
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.mission_rewards_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.mission_rewards_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            MissionRewardRow(
                icon = if (dailyChallengeState.completed) "✓" else "!",
                title = stringResource(R.string.daily_challenge_title),
                detail = when {
                    dailyChallengeState.doubleRewardClaimed -> stringResource(R.string.mission_reward_daily_doubled)
                    dailyChallengeState.rewardClaimed -> stringResource(
                        R.string.mission_reward_daily_claimed,
                        dailyChallengeState.rewardCoins
                    )
                    else -> stringResource(
                        R.string.daily_challenge_progress,
                        dailyChallengeState.progress,
                        dailyChallengeState.target
                    )
                },
                accent = if (dailyChallengeState.completed) ArcadeTeal else ArcadeGold
            ) {
                if (dailyChallengeState.completed && !dailyChallengeState.rewardClaimed) {
                    SecondaryGameButton(
                        text = stringResource(R.string.claim_reward),
                        onClick = onDailyChallengeClaim,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (dailyChallengeState.completed && dailyChallengeState.rewardClaimed && !dailyChallengeState.doubleRewardClaimed) {
                    SecondaryGameButton(
                        text = when {
                            rewardedAdUiState.isReady -> stringResource(R.string.daily_challenge_double_reward)
                            rewardedAdUiState.isLoading || rewardedAdUiState.isShowing -> stringResource(R.string.rewarded_loading)
                            else -> stringResource(R.string.rewarded_not_ready)
                        },
                        enabled = rewardedAdUiState.isReady && !rewardedAdUiState.isShowing,
                        onClick = onDailyChallengeDoubleRewardClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            MissionRewardRow(
                icon = if (weeklyChallengeState.completed) "✓" else "#",
                title = stringResource(R.string.weekly_challenge_title),
                detail = if (weeklyChallengeState.completed) {
                    stringResource(R.string.mission_reward_weekly_completed, weeklyChallengeState.rewardCoins)
                } else {
                    stringResource(
                        R.string.weekly_challenge_progress,
                        weeklyChallengeState.progress,
                        weeklyChallengeState.target,
                        weeklyChallengeState.rewardCoins
                    )
                },
                accent = if (weeklyChallengeState.completed) ArcadeTeal else ArcadeBlue
            )

            completedAchievements.forEach { achievement ->
                MissionRewardRow(
                    icon = if (achievement.claimed) "✓" else "★",
                    title = stringResource(achievement.titleRes),
                    detail = if (achievement.claimed) {
                        stringResource(R.string.mission_reward_claimed_value, achievement.rewardCoins)
                    } else {
                        stringResource(R.string.mission_reward_ready_value, achievement.rewardCoins)
                    },
                    accent = if (achievement.claimed) ArcadeTeal else ArcadeGold
                ) {
                    if (!achievement.claimed) {
                        SecondaryGameButton(
                            text = stringResource(R.string.claim_reward),
                            onClick = { onAchievementClaim(achievement.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (!hasCompletedReward) {
                Text(
                    text = stringResource(R.string.mission_rewards_empty),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MissionRewardRow(
    icon: String,
    title: String,
    detail: String,
    accent: Color,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent
                )
            }
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
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        action?.invoke()
    }
}

@Composable
private fun CoinWalletCard(
    coins: Int,
    selectedTheme: PlayerTheme,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ArcadeGold.copy(alpha = 0.13f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.34f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ArcadeGold.copy(alpha = 0.24f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "◉", color = ArcadeGold, style = MaterialTheme.typography.titleMedium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.coin_wallet_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcadeGold
                )
                Text(
                    text = stringResource(R.string.coin_wallet_value, coins),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.selected_theme_value, stringResource(selectedTheme.titleRes)),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun DailyStreakMiniCard(
    state: DailyRewardState,
    onClick: () -> Unit,
    onProtectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = when {
        state.isStreakAtRisk -> ArcadeCoral
        state.claimedToday -> ArcadeTeal
        else -> ArcadeGold
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "★", color = accent)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.isStreakAtRisk) {
                            stringResource(R.string.daily_reward_streak_at_risk_title)
                        } else {
                            stringResource(R.string.daily_reward_streak_value, state.streakDay)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.daily_reward_next_value, state.nextRewardCoins),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            DailyRewardProgressLine(state = state)
            if (state.isStreakAtRisk) {
                SecondaryGameButton(
                    text = stringResource(R.string.daily_reward_protect_button),
                    onClick = onProtectClick
                )
            }
        }
    }
}

@Composable
private fun DailyRewardCard(
    state: DailyRewardState,
    onClaimClick: () -> Unit,
    onProtectClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = when {
        state.isStreakAtRisk -> ArcadeCoral
        state.canClaim -> ArcadeGold
        else -> ArcadeTeal
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        color = if (state.canClaim || state.isStreakAtRisk) ReflexGamePalette.cardGlassStrong else ArcadeTeal.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (state.isStreakAtRisk) "!" else if (state.canClaim) "★" else "✓", color = accent)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.daily_reward_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = accent
                    )
                    Text(
                        text = stringResource(R.string.daily_reward_streak, state.streakDay),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary
                    )
                    Text(
                        text = dailyRewardText(state),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary
                    )
                    Text(
                        text = stringResource(R.string.daily_reward_next_value, state.nextRewardCoins),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            DailyRewardProgressLine(state = state)
            if (state.canClaim) {
                PrimaryGameButton(
                    text = if (state.isSuperReward) {
                        stringResource(R.string.daily_reward_super_claim)
                    } else {
                        stringResource(R.string.claim_reward)
                    },
                    onClick = onClaimClick
                )
            } else if (state.isStreakAtRisk) {
                Text(
                    text = stringResource(R.string.daily_reward_protect_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
                SecondaryGameButton(
                    text = stringResource(R.string.daily_reward_protect_button),
                    onClick = onProtectClick
                )
            } else {
                Text(
                    text = stringResource(R.string.daily_reward_claimed_today),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun DailyRewardProgressLine(
    state: DailyRewardState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        DailyRewardCoinPlan.forEachIndexed { index, coins ->
            val dayNumber = index + 1
            val active = dayNumber <= state.dayInCycle && !state.isStreakAtRisk
            val isToday = dayNumber == state.dayInCycle
            val color = when {
                isToday && state.isSuperReward -> ArcadeGold
                active -> ArcadeTeal
                else -> Color.White.copy(alpha = 0.18f)
            }
            Surface(
                modifier = Modifier.weight(1f),
                color = color.copy(alpha = if (active) 0.22f else 0.08f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, color.copy(alpha = 0.42f))
            ) {
                Text(
                    text = if (dayNumber == DailyRewardCoinPlan.size) {
                        stringResource(R.string.daily_reward_day_super)
                    } else {
                        stringResource(R.string.daily_reward_day_short, dayNumber)
                    },
                    modifier = Modifier.padding(horizontal = 1.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProfileProgressCard(
    playerProfile: PlayerProfile,
    progressionState: ProgressionState,
    bestScore: Int,
    onEditNameClick: () -> Unit,
    onTitleSelect: (PlayerTitle) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLevelXp = ((progressionState.level - 1) * 250)
    val nextLevelXp = progressionState.level * 250
    val levelProgress = ((progressionState.xp - currentLevelXp).toFloat() / 250f).coerceIn(0f, 1f)
    val remainingXp = (nextLevelXp - progressionState.xp).coerceAtLeast(0)
    val rank = rankFor(score = bestScore, level = progressionState.level)
    val achievementCount = progressionState.achievements.count { it.unlocked }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.neonBlue.copy(alpha = 0.14f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ReflexGamePalette.neonBlue.copy(alpha = 0.32f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = ArcadeTeal
                    )
                    Text(
                        text = playerProfile.name.ifBlank { stringResource(R.string.leaderboard_you) },
                        style = MaterialTheme.typography.titleMedium,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(playerProfile.title.titleRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = onEditNameClick) {
                        Text(
                            text = stringResource(R.string.profile_change_name),
                            color = ArcadeGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = stringResource(R.string.level_value, progressionState.level),
                        style = MaterialTheme.typography.labelMedium,
                        color = ArcadeTeal
                    )
                }
            }
            LinearProgressIndicator(
                progress = { levelProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = ArcadeTeal,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = stringResource(R.string.xp_value, progressionState.xp, nextLevelXp),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textSecondary
            )
            Text(
                text = stringResource(R.string.xp_to_next_level_value, remainingXp),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_games_value, progressionState.totalGames),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.profile_best_value, bestScore),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.profile_combo_value, progressionState.lifetimeMaxCombo),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_rank_value, stringResource(rank.titleRes)),
                    modifier = Modifier.weight(1.2f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.profile_achievements_value, achievementCount),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.coin_wallet_value, progressionState.coins),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(
                    R.string.selected_theme_value,
                    stringResource(progressionState.selectedTheme.titleRes)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (progressionState.dailyReward.loyalBadgeUnlocked) {
                Surface(
                    color = ArcadeGold.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.38f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.daily_reward_loyal_badge),
                            style = MaterialTheme.typography.labelLarge,
                            color = ArcadeGold
                        )
                        Text(
                            text = stringResource(R.string.daily_reward_loyal_badge_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = ReflexGamePalette.textSecondary
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.profile_title_select),
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary
                )
                PlayerTitle.entries.chunked(2).forEach { rowTitles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTitles.forEach { title ->
                            val selected = title == playerProfile.title
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onTitleSelect(title) },
                                color = if (selected) ArcadeTeal.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(
                                    1.dp,
                                    (if (selected) ArcadeTeal else ArcadeBlue).copy(alpha = 0.34f)
                                )
                            ) {
                                Text(
                                    text = stringResource(title.titleRes),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ReflexGamePalette.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            if (progressionState.lastLevelUp != null) {
                Text(
                    text = stringResource(R.string.level_up_value, progressionState.lastLevelUp),
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcadeGold
                )
            }
        }
    }
}

@Composable
private fun AchievementSection(
    achievements: List<AchievementState>,
    unlockedIds: List<String>,
    onClaimClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.achievements_title),
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
        if (achievements.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ReflexGamePalette.cardGlassStrong,
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.28f))
            ) {
                Text(
                    text = stringResource(R.string.achievements_empty),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            sortedAchievementsForDisplay(achievements).forEach { achievement ->
                AchievementCard(
                    achievement = achievement,
                    highlighted = achievement.id in unlockedIds,
                    onClaimClick = { onClaimClick(achievement.id) }
                )
            }
        }
    }
}

@Composable
private fun AchievementCategorySection(
    category: AchievementCategory,
    achievements: List<AchievementState>,
    unlockedIds: List<String>,
    onClaimClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(category.titleRes),
            style = MaterialTheme.typography.titleSmall,
            color = ArcadeGold
        )
        achievements.forEach { achievement ->
            AchievementCard(
                achievement = achievement,
                highlighted = achievement.id in unlockedIds,
                onClaimClick = { onClaimClick(achievement.id) }
            )
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: AchievementState,
    highlighted: Boolean,
    onClaimClick: () -> Unit
) {
    val accent = when {
        achievement.claimed -> ArcadeTeal
        achievement.unlocked -> ArcadeGold
        else -> ArcadeBlue
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (highlighted || achievement.unlocked) accent.copy(alpha = 0.13f) else ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = if (highlighted) 0.62f else 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (achievement.unlocked) "★" else "◇",
                    style = MaterialTheme.typography.titleMedium,
                    color = accent
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(achievement.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary
                    )
                    Text(
                        text = stringResource(achievement.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary
                    )
                }
                if (achievement.unlocked && !achievement.claimed) {
                    SecondaryGameButton(
                        text = stringResource(R.string.claim_reward),
                        onClick = onClaimClick,
                        modifier = Modifier.width(132.dp)
                    )
                }
            }
            LinearProgressIndicator(
                progress = { achievement.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = accent,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = stringResource(
                    R.string.achievement_progress_percent_value,
                    achievement.progressPercent,
                    achievement.progress.coerceAtMost(achievement.target),
                    achievement.target,
                    achievement.rewardCoins
                ),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textSecondary
            )
        }
    }
}

private fun sortedAchievementsForDisplay(
    achievements: List<AchievementState>
): List<AchievementState> {
    return achievements.sortedWith(
        compareBy<AchievementState> {
            when {
                it.unlocked && !it.claimed -> 0
                !it.claimed -> 1
                else -> 2
            }
        }.thenBy { it.category.ordinal }
            .thenBy { it.target }
            .thenBy { it.id }
    )
}

@Composable
private fun LevelUpPopup(
    level: Int,
    coinBonus: Int,
    onDismiss: () -> Unit
) {
    PolishedGameDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PrimaryGameButton(
                text = stringResource(R.string.ok),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        },
        title = stringResource(R.string.level_up_value, level)
    ) {
        Text(
            text = stringResource(R.string.level_up_bonus_message, coinBonus),
            modifier = Modifier.fillMaxWidth(),
            color = ReflexGamePalette.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LeaderboardSection(
    snapshot: LeaderboardSnapshot,
    onModeSelected: (GameMode) -> Unit,
    onPeriodSelected: (LeaderboardPeriod) -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var showRefreshMessage by remember(snapshot.refreshedTick, snapshot.statusMessageRes) {
        mutableStateOf(snapshot.statusMessageRes != null)
    }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(450)
            isRefreshing = false
        }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.leaderboard_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.leaderboard_local_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
            Surface(
                modifier = Modifier.clickable {
                    isRefreshing = true
                    onRefreshClick()
                },
                color = ArcadeBlue.copy(alpha = 0.18f),
                shape = CircleShape,
                border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.4f))
            ) {
                Text(
                    text = if (isRefreshing || snapshot.isLoading) "…" else "↻",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
        LeaderboardPeriodSelector(
            selectedPeriod = snapshot.selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )
        LeaderboardModeSelector(
            selectedMode = snapshot.selectedMode,
            onModeSelected = onModeSelected
        )
        val statusMessageRes = snapshot.statusMessageRes
        if (showRefreshMessage && statusMessageRes != null) {
            Text(
                text = stringResource(statusMessageRes),
                style = MaterialTheme.typography.bodySmall,
                color = if (snapshot.isOffline) ArcadeGold else ArcadeTeal
            )
        }
        if (snapshot.entries.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ReflexGamePalette.cardGlassStrong,
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.32f))
            ) {
                Text(
                    text = stringResource(R.string.leaderboard_empty),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Text(
                text = if (snapshot.motivationRes == R.string.leaderboard_motivation_pass_player) {
                    stringResource(
                        snapshot.motivationRes,
                        snapshot.motivationPlayerName,
                        snapshot.motivationScoreGap
                    )
                } else {
                    stringResource(snapshot.motivationRes)
                },
                style = MaterialTheme.typography.bodySmall,
                color = ArcadeGold
            )
        }
        snapshot.entries.forEach { entry ->
            val accent = when {
                entry.isPlayer -> ArcadeGold
                entry.rank <= 3 -> ArcadeTeal
                else -> themeAccentColor(entry.theme)
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (entry.isPlayer) ArcadeGold.copy(alpha = 0.18f) else ReflexGamePalette.cardGlassStrong,
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = if (entry.rank <= 3 || entry.isPlayer) 0.58f else 0.26f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text(
                        text = stringResource(R.string.leaderboard_rank_value, entry.rank),
                        modifier = Modifier.width(34.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (entry.rank <= 3 || entry.isPlayer) ArcadeGold else ReflexGamePalette.textSecondary
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(themeAccentColor(entry.theme))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (entry.isPlayer) {
                                if (entry.name.isBlank()) {
                                    stringResource(R.string.leaderboard_you)
                                } else {
                                    stringResource(R.string.leaderboard_you_named, entry.name)
                                }
                            } else {
                                entry.name
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = ReflexGamePalette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(entry.rankTier.titleRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = ReflexGamePalette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = entry.score.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (entry.isPlayer) ArcadeGold else ReflexGamePalette.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardPeriodSelector(
    selectedPeriod: LeaderboardPeriod,
    onPeriodSelected: (LeaderboardPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(LeaderboardPeriod.AllTime).forEach { period ->
            val selected = period == selectedPeriod
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPeriodSelected(period) },
                color = if (selected) ArcadeGold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, (if (selected) ArcadeGold else ArcadeBlue).copy(alpha = 0.36f))
            ) {
                Text(
                    text = stringResource(
                        when (period) {
                            LeaderboardPeriod.Weekly -> R.string.leaderboard_period_weekly
                            LeaderboardPeriod.AllTime -> R.string.leaderboard_period_all_time
                        }
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) ArcadeGold else ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LeaderboardModeSelector(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit
) {
    GameMode.entries.chunked(2).forEach { rowModes ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rowModes.forEach { mode ->
                val selected = mode == selectedMode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onModeSelected(mode) },
                    color = if (selected) themeAccentColor(PlayerTheme.CyberBlue).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, (if (selected) ArcadeTeal else ArcadeBlue).copy(alpha = 0.34f))
                ) {
                    Text(
                        text = stringResource(mode.titleRes),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyChallengeCard(
    state: ChallengeState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (state.completed) ArcadeTeal.copy(alpha = 0.12f) else ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.weekly_challenge_title),
                style = MaterialTheme.typography.labelLarge,
                color = ArcadeTeal
            )
            Text(
                text = stringResource(state.titleRes),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary
            )
            Text(
                text = stringResource(state.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary
            )
            LinearProgressIndicator(
                progress = { state.progress.toFloat() / state.target.toFloat().coerceAtLeast(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = ArcadeTeal,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = stringResource(R.string.weekly_challenge_progress, state.progress, state.target, state.rewardCoins),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textSecondary
            )
        }
    }
}

@Composable
private fun PlayerNameDialog(
    currentName: String,
    hasCurrentName: Boolean,
    onSave: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    val playerNameSuggestions = stringArrayResource(R.array.player_name_suggestions).toList()
    var name by remember(currentName, hasCurrentName) {
        mutableStateOf(if (hasCurrentName) currentName else playerNameSuggestions.random())
    }
    var hasError by remember { mutableStateOf(false) }
    val titleText = stringResource(R.string.player_name_dialog_title)
    val descriptionText = stringResource(R.string.player_name_dialog_description)
    val hintText = stringResource(R.string.player_name_dialog_hint)
    val saveText = stringResource(R.string.player_name_save)
    val errorText = stringResource(R.string.player_name_error)
    val randomNameText = stringResource(R.string.player_name_random)
    val suggestionsText = stringResource(R.string.player_name_suggestions)

    PolishedGameDialog(
        onDismissRequest = onDismiss,
        title = titleText,
        confirmButton = {
            PrimaryGameButton(
                text = saveText,
                onClick = {
                    val candidate = name.trim().take(PLAYER_NAME_MAX_LENGTH)
                    hasError = candidate.isBlank() || !onSave(candidate)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textSecondary
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(PLAYER_NAME_MAX_LENGTH)
                        hasError = false
                    },
                    singleLine = true,
                    isError = hasError,
                    label = {
                        Text(text = hintText)
                    },
                    supportingText = {
                        Text(
                            text = stringResource(R.string.player_name_character_count, name.length, PLAYER_NAME_MAX_LENGTH),
                            color = ReflexGamePalette.textSecondary
                        )
                    }
                )
                if (hasError) {
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArcadeCoral
                    )
                }
                Text(
                    text = suggestionsText,
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary
                )
                playerNameSuggestions.chunked(3).forEach { rowSuggestions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSuggestions.forEach { suggestion ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        name = suggestion
                                        hasError = false
                                    },
                                color = if (name == suggestion) ArcadeTeal.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = if (name == suggestion) 0.52f else 0.22f))
                            ) {
                                Text(
                                    text = suggestion,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ReflexGamePalette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                SecondaryGameButton(
                    text = randomNameText,
                    onClick = {
                        name = playerNameSuggestions.random()
                        hasError = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
    }
}

@Composable
private fun DailyRewardPopup(
    state: DailyRewardState,
    selectedLanguage: AppLanguage,
    onClaimClick: () -> Unit,
    onProtectClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = when {
        state.isStreakAtRisk -> localizedHomeStringResource(R.string.daily_reward_streak_at_risk_title, selectedLanguage)
        state.isSuperReward -> localizedHomeStringResource(R.string.daily_reward_super_title, selectedLanguage)
        else -> localizedHomeStringResource(R.string.daily_reward_title, selectedLanguage)
    }
    val message = when {
        state.isStreakAtRisk -> localizedHomeStringResource(R.string.daily_reward_protect_message, selectedLanguage)
        state.isSuperReward -> localizedHomeStringResource(
            id = R.string.daily_reward_super_message,
            selectedLanguage = selectedLanguage,
            state.rewardCoins
        )
        state.claimedToday -> localizedHomeStringResource(R.string.daily_reward_claimed_today, selectedLanguage)
        else -> localizedHomeStringResource(
            id = R.string.daily_reward_popup_message,
            selectedLanguage = selectedLanguage,
            state.rewardCoins
        )
    }
    val claimText = when {
        state.isStreakAtRisk -> localizedHomeStringResource(R.string.daily_reward_protect_button, selectedLanguage)
        state.isSuperReward -> localizedHomeStringResource(R.string.daily_reward_super_claim, selectedLanguage)
        state.claimedToday -> localizedHomeStringResource(R.string.daily_reward_continue, selectedLanguage)
        else -> localizedHomeStringResource(R.string.daily_reward_continue, selectedLanguage)
    }

    PolishedGameDialog(
        onDismissRequest = onDismiss,
        title = title,
        confirmButton = {
            PrimaryGameButton(
                text = claimText,
                onClick = when {
                    state.isStreakAtRisk -> onProtectClick
                    state.canClaim -> onClaimClick
                    else -> onDismiss
                }
            )
        }
    ) {
        Text(
            text = message,
            modifier = Modifier.fillMaxWidth(),
            color = ReflexGamePalette.textSecondary,
            textAlign = TextAlign.Center
        )
        DailyRewardProgressLine(state = state)
    }
}

@Composable
private fun dailyRewardText(state: DailyRewardState): String {
    return when (state.rewardType) {
        DailyRewardType.Coins -> stringResource(R.string.daily_reward_coin_value, state.rewardCoins)
        DailyRewardType.SuperBox -> stringResource(R.string.daily_reward_super_value, state.rewardCoins)
    }
}

@Composable
private fun ThemeTargetCard(
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
private fun CoinChestCard(
    state: CoinChestState,
    rewardedAdUiState: RewardedAdUiState,
    onOpenClick: () -> Unit
) {
    val canOpen = state.canOpen && rewardedAdUiState.isReady && !rewardedAdUiState.isShowing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.38f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(R.string.coin_chest_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.coin_chest_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val lastRewardText = if (state.lastRewardCoins > 0) {
                    " • ${stringResource(R.string.coin_chest_last_reward, state.lastRewardCoins)}"
                } else {
                    ""
                }
                Text(
                    text = stringResource(R.string.coin_chest_remaining, state.remainingOpens, state.maxOpensPerDay) + lastRewardText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (state.canOpen) ArcadeGold else ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SecondaryGameButton(
                text = when {
                    !state.canOpen -> stringResource(R.string.coin_chest_limit_reached)
                    rewardedAdUiState.isLoading || rewardedAdUiState.isShowing -> stringResource(R.string.rewarded_loading)
                    !rewardedAdUiState.isReady -> stringResource(R.string.rewarded_not_ready)
                    else -> stringResource(R.string.coin_chest_open)
                },
                enabled = canOpen,
                onClick = onOpenClick
            )
        }
    }
}

@Composable
private fun ThemeShopSection(
    progressionState: ProgressionState,
    selectedLanguage: AppLanguage,
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onThemeTrial: (PlayerTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    var unlockedThemePopup by remember { mutableStateOf<PlayerTheme?>(null) }

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
                            unlockedThemePopup = theme
                        }
                    },
                    onTrial = { onThemeTrial(theme) }
                )
            }
        }

        unlockedThemePopup?.let { theme ->
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(GameDialogScrimColor.copy(alpha = 0.72f))
                    .clickable { unlockedThemePopup = null }
            )
            ThemeUnlockCelebration(
                theme = theme,
                selectedLanguage = selectedLanguage,
                onDismiss = { unlockedThemePopup = null },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 38.dp)
            )
        }
    }
}

@Composable
private fun ThemeUnlockCelebration(
    theme: PlayerTheme,
    selectedLanguage: AppLanguage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = themeVisualSpec(theme)
    val title = localizedHomeStringResource(R.string.theme_unlocked_popup_title, selectedLanguage)
    val pulse by rememberInfiniteTransition(label = "theme_unlock_glow").animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420),
            repeatMode = RepeatMode.Reverse
        ),
        label = "theme_unlock_glow_value"
    )
    val confettiProgress by rememberInfiniteTransition(label = "theme_unlock_confetti").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 820),
            repeatMode = RepeatMode.Restart
        ),
        label = "theme_unlock_confetti_value"
    )

    LaunchedEffect(theme) {
        delay(THEME_UNLOCK_CELEBRATION_DURATION_MS)
        onDismiss()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, spec.primary.copy(alpha = 0.72f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp)
                .graphicsLayer {
                    shadowElevation = 18f + pulse * 10f
                    scaleX = 0.98f + pulse * 0.02f
                    scaleY = 0.98f + pulse * 0.02f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            spec.primary.copy(alpha = 0.28f),
                            spec.secondary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            ThemeUnlockConfetti(
                primaryColor = spec.primary,
                secondaryColor = spec.secondary,
                progress = confettiProgress,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(spec.primary.copy(alpha = 0.22f))
                        .border(1.dp, spec.primary.copy(alpha = 0.62f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.titleLarge,
                        color = spec.primary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ThemeUnlockConfetti(
    primaryColor: Color,
    secondaryColor: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val colors = listOf(primaryColor, secondaryColor, ArcadeGold, ArcadeTeal)
        repeat(18) { index ->
            val xSeed = ((index * 37) % 100) / 100f
            val ySeed = ((index * 19) % 70) / 100f
            val x = size.width * xSeed
            val y = (size.height * (ySeed + progress * 0.82f)) % size.height
            drawCircle(
                color = colors[index % colors.size].copy(alpha = 0.78f),
                radius = (2 + index % 3).dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun ThemeCard(
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
    val pulse by rememberInfiniteTransition(label = "theme_card_pulse").animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "theme_card_pulse_value"
    )
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
private fun ThemeStatusBadge(
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
private fun ThemePreview(
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

@Composable
private fun localizedHomeStringResource(
    @StringRes id: Int,
    selectedLanguage: AppLanguage,
    vararg args: Any
): String {
    val context = LocalContext.current
    val localizedContext = remember(context, selectedLanguage) {
        context.createHomeLanguageContext(selectedLanguage)
    }
    return if (args.isEmpty()) {
        localizedContext.getString(id)
    } else {
        localizedContext.getString(id, *args)
    }
}

private fun Context.createHomeLanguageContext(language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.code)
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}

internal fun themeAccentColor(theme: PlayerTheme): Color {
    return themeVisualSpec(theme).primary
}

internal data class ThemeVisualSpec(
    val primary: Color,
    val secondary: Color,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    @StringRes val previewLabelRes: Int
)

internal fun themeVisualSpec(theme: PlayerTheme): ThemeVisualSpec {
    return when (theme) {
        PlayerTheme.NeonRed -> ThemeVisualSpec(
            primary = ArcadeCoral,
            secondary = Color(0xFFFF7A8A),
            backgroundTop = Color(0xFF170816),
            backgroundBottom = Color(0xFF3A1022),
            previewLabelRes = R.string.theme_preview_neon
        )
        PlayerTheme.CyberBlue -> ThemeVisualSpec(
            primary = ArcadeBlue,
            secondary = Color(0xFF49F3FF),
            backgroundTop = Color(0xFF06142E),
            backgroundBottom = Color(0xFF0D3B7A),
            previewLabelRes = R.string.theme_preview_cyber
        )
        PlayerTheme.PurpleStorm -> ThemeVisualSpec(
            primary = ReflexGamePalette.neonPurple,
            secondary = Color(0xFFFF4FD8),
            backgroundTop = Color(0xFF160826),
            backgroundBottom = Color(0xFF45209B),
            previewLabelRes = R.string.theme_preview_storm
        )
        PlayerTheme.IceNeon -> ThemeVisualSpec(
            primary = Color(0xFF8DEBFF),
            secondary = Color(0xFFB9F8FF),
            backgroundTop = Color(0xFF061927),
            backgroundBottom = Color(0xFF1E6B88),
            previewLabelRes = R.string.theme_preview_ice
        )
        PlayerTheme.LavaCore -> ThemeVisualSpec(
            primary = Color(0xFFFF5A1F),
            secondary = Color(0xFFFFC857),
            backgroundTop = Color(0xFF230606),
            backgroundBottom = Color(0xFF7A210B),
            previewLabelRes = R.string.theme_preview_lava
        )
        PlayerTheme.ToxicPulse -> ThemeVisualSpec(
            primary = Color(0xFFB9FF2F),
            secondary = Color(0xFF2CFFB7),
            backgroundTop = Color(0xFF071907),
            backgroundBottom = Color(0xFF245B18),
            previewLabelRes = R.string.theme_preview_toxic
        )
        PlayerTheme.MatrixGreen -> ThemeVisualSpec(
            primary = Color(0xFF21FF72),
            secondary = Color(0xFF00D46A),
            backgroundTop = Color(0xFF020D08),
            backgroundBottom = Color(0xFF06351C),
            previewLabelRes = R.string.theme_preview_matrix
        )
        PlayerTheme.GoldFire -> ThemeVisualSpec(
            primary = ArcadeGold,
            secondary = Color(0xFFFF8A2A),
            backgroundTop = Color(0xFF1C1202),
            backgroundBottom = Color(0xFF6C3C05),
            previewLabelRes = R.string.theme_preview_gold
        )
        PlayerTheme.ShadowBlack -> ThemeVisualSpec(
            primary = Color(0xFF8A94A6),
            secondary = Color(0xFF30384A),
            backgroundTop = Color(0xFF02030A),
            backgroundBottom = Color(0xFF151827),
            previewLabelRes = R.string.theme_preview_shadow
        )
        PlayerTheme.GalaxyWave -> ThemeVisualSpec(
            primary = Color(0xFF6F8CFF),
            secondary = Color(0xFFFF5BEF),
            backgroundTop = Color(0xFF050421),
            backgroundBottom = Color(0xFF23116D),
            previewLabelRes = R.string.theme_preview_galaxy
        )
        PlayerTheme.RainbowFlux -> ThemeVisualSpec(
            primary = Color(0xFFFF4FD8),
            secondary = Color(0xFF49F3FF),
            backgroundTop = Color(0xFF15051F),
            backgroundBottom = Color(0xFF123A62),
            previewLabelRes = R.string.theme_preview_flux
        )
    }
}

@Composable
private fun LanguageSelectionSection(
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
private fun LanguageChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) ArcadeGold else ReflexGamePalette.neonBlue
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = color.copy(alpha = if (selected) 0.22f else 0.1f),
        shape = RoundedCornerShape(18.dp),
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

internal fun modeAccentColor(mode: GameMode): Color {
    return when (mode) {
        GameMode.Classic -> ArcadeGold
        GameMode.MovingTarget -> ArcadeBlue
        GameMode.FakeTarget -> ReflexGamePalette.targetRing
        GameMode.ColorReflex -> ArcadeTeal
    }
}

internal fun modeIcon(mode: GameMode): String {
    return when (mode) {
        GameMode.Classic -> "◎"
        GameMode.MovingTarget -> "↗"
        GameMode.FakeTarget -> "◇"
        GameMode.ColorReflex -> "◆"
    }
}

@Composable
private fun GameLogo(isCompactHeight: Boolean) {
    val containerSize = if (isCompactHeight) 50.dp else 82.dp
    val iconSize = if (isCompactHeight) 42.dp else 66.dp
    val badgeOffsetX = if (isCompactHeight) 17.dp else 28.dp
    val badgeOffsetY = if (isCompactHeight) (-15).dp else (-24).dp
    val badgeSize = if (isCompactHeight) 12.dp else 14.dp

    Box(
        modifier = Modifier.size(containerSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ReflexGamePalette.targetRing.copy(alpha = 0.34f),
                            ReflexGamePalette.neonPurple.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
        Image(
            painter = painterResource(R.drawable.refleks_avi_icon_full),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .shadow(22.dp, CircleShape, clip = false)
                .clip(CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = badgeOffsetX, y = badgeOffsetY)
                .size(badgeSize)
                .clip(CircleShape)
                .background(ArcadeGold)
        )
    }
}

@Composable
private fun BestScoreHero(
    bestScore: Int,
    isCompactHeight: Boolean
) {
    val rowHorizontalPadding = if (isCompactHeight) 14.dp else 16.dp
    val rowVerticalPadding = if (isCompactHeight) 12.dp else 14.dp
    val rowSpacing = if (isCompactHeight) 10.dp else 12.dp
    val iconSize = if (isCompactHeight) 38.dp else 42.dp

    Surface(
        color = ReflexGamePalette.neonBlue.copy(alpha = 0.18f),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = ReflexGamePalette.neonBlue.copy(alpha = 0.26f)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = rowHorizontalPadding,
                vertical = rowVerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(ArcadeGold.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    color = ArcadeGold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.best_score),
                    style = MaterialTheme.typography.labelLarge,
                    color = ReflexGamePalette.textSecondary
                )
                Text(
                    text = bestScore.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = ReflexGamePalette.textPrimary
                )
            }
        }
    }
}
