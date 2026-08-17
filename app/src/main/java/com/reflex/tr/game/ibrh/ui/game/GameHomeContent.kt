package com.reflex.tr.game.ibrh.ui.game

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import kotlinx.coroutines.delay

private enum class HomePopup {
    DailyReward,
    PlayerName,
    LevelUp,
    ModeMasteryLevelUp,
    ThemeUnlock,
    HomeExit
}

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
    onQuickGameClick: () -> Unit,
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
    onComboChallengeClaim: () -> Unit,
    onWeeklyChallengeClaim: () -> Unit,
    onSeasonRewardClaim: (Int) -> Unit,
    onSeasonXpBoostClick: () -> Unit,
    onSeasonMissionClaim: (String) -> Unit,
    onDailyStreakProtect: () -> Unit,
    onCoinChestClick: () -> Unit,
    onShopCoinRewardClick: () -> Unit,
    onInviteShareClick: () -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit,
    onAchievementClaim: (String) -> Unit,
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onThemeTrial: (PlayerTheme) -> Unit,
    onTargetSkinSelect: (TargetSkin) -> Unit,
    onTargetSkinBuy: (TargetSkin) -> Unit,
    onPlayerNameChange: (String) -> Boolean,
    onPlayerTitleSelect: (PlayerTitle) -> Unit,
    onProfileBadgeSelect: (ProfileBadge) -> Unit,
    onLeaderboardModeSelected: (GameMode) -> Unit,
    onLeaderboardPeriodSelected: (LeaderboardPeriod) -> Unit,
    onLeaderboardRefresh: () -> Unit,
    onDailyLeaderboardGoalClaim: () -> Unit,
    onPersonalGoalClaim: () -> Unit,
    onLeaderboardOpenedForMission: () -> Unit,
    onShopOpenedForMission: () -> Unit,
    onStorePreviewModeChange: (Boolean) -> Unit,
    onResetModeTips: () -> Unit,
    onRateAppClick: () -> Unit,
    onExitAppRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompactHeight = maxHeight <= 780.dp
        val contentScrollState = rememberScrollState()
        val panelPadding = if (isCompactHeight) 10.dp else 14.dp
        val contentSpacing = if (isCompactHeight) 8.dp else 10.dp
        var selectedHomeTab by rememberSaveable { mutableStateOf(HomeTab.Play) }
        var showDailyRewardPopup by rememberSaveable(progressionState.dailyReward.canClaim) {
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
            playerProfile.hasCompletedNamePrompt
        ) {
            mutableStateOf(!playerProfile.hasName && !playerProfile.hasCompletedNamePrompt)
        }
        var unlockedThemePopup by rememberSaveable { mutableStateOf<PlayerTheme?>(null) }
        var showHomeExitDialog by rememberSaveable { mutableStateOf(false) }
        var dismissedLevelUp by rememberSaveable { mutableStateOf<Int?>(null) }
        var dismissedModeMasteryLevelUp by rememberSaveable { mutableStateOf<String?>(null) }
        val levelUp = progressionState.lastLevelUp
        val modeMasteryLevelUp = progressionState.lastModeMasteryLevelUp
        val modeMasteryLevelUpKey = modeMasteryLevelUp?.let {
            "${it.mode.storageKey}_${it.level}_${it.coinBonus}"
        }
        val activeHomePopup = when {
            showDailyRewardPopup -> HomePopup.DailyReward
            showPlayerNameDialog -> HomePopup.PlayerName
            levelUp != null && dismissedLevelUp != levelUp -> HomePopup.LevelUp
            modeMasteryLevelUp != null && dismissedModeMasteryLevelUp != modeMasteryLevelUpKey ->
                HomePopup.ModeMasteryLevelUp
            unlockedThemePopup != null -> HomePopup.ThemeUnlock
            showHomeExitDialog -> HomePopup.HomeExit
            else -> null
        }

        BackHandler(enabled = true) {
            when (activeHomePopup) {
                HomePopup.DailyReward -> showDailyRewardPopup = false
                HomePopup.PlayerName -> {
                    if (playerProfile.hasName) {
                        showPlayerNameDialog = false
                    }
                }
                HomePopup.LevelUp -> dismissedLevelUp = levelUp
                HomePopup.ModeMasteryLevelUp -> dismissedModeMasteryLevelUp = modeMasteryLevelUpKey
                HomePopup.ThemeUnlock -> unlockedThemePopup = null
                HomePopup.HomeExit -> showHomeExitDialog = false
                null -> {
                    if (selectedHomeTab == HomeTab.Play) {
                        showHomeExitDialog = true
                    } else {
                        selectedHomeTab = if (selectedHomeTab.showInBottomNav) {
                            HomeTab.Play
                        } else {
                            HomeTab.Profile
                        }
                    }
                }
            }
        }

        LaunchedEffect(levelUp) {
            if (levelUp != null && dismissedLevelUp != levelUp) {
                delay(LEVEL_UP_POPUP_DURATION_MS)
                dismissedLevelUp = levelUp
            }
        }

        LaunchedEffect(modeMasteryLevelUpKey) {
            if (modeMasteryLevelUpKey != null && dismissedModeMasteryLevelUp != modeMasteryLevelUpKey) {
                delay(LEVEL_UP_POPUP_DURATION_MS)
                dismissedModeMasteryLevelUp = modeMasteryLevelUpKey
            }
        }

        if (activeHomePopup == HomePopup.DailyReward) {
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

        if (activeHomePopup == HomePopup.PlayerName) {
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

        if (activeHomePopup == HomePopup.LevelUp && levelUp != null) {
            LevelUpPopup(
                level = levelUp,
                coinBonus = LEVEL_UP_BONUS_COINS,
                onDismiss = { dismissedLevelUp = levelUp }
            )
        }

        if (activeHomePopup == HomePopup.ModeMasteryLevelUp && modeMasteryLevelUp != null) {
            ModeMasteryLevelUpPopup(
                levelUp = modeMasteryLevelUp,
                onDismiss = { dismissedModeMasteryLevelUp = modeMasteryLevelUpKey }
            )
        }

        if (activeHomePopup == HomePopup.HomeExit) {
            HomeExitDialog(
                onStayClick = { showHomeExitDialog = false },
                onExitClick = {
                    showHomeExitDialog = false
                    onExitAppRequested()
                }
            )
        }

        GamePanelCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .navigationBarsPadding(),
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
                            .padding(bottom = if (isCompactHeight) 20.dp else 16.dp),
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
                                onSuggestionTabClick = { tab ->
                                    logHomeTabOpened(
                                        tab = tab,
                                        leaderboardSnapshot = leaderboardSnapshot
                                    )
                                    if (tab == HomeTab.Leaderboard) onLeaderboardOpenedForMission()
                                    if (tab == HomeTab.Shop) onShopOpenedForMission()
                                    selectedHomeTab = tab
                                },
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
                                onInviteShareClick = onInviteShareClick,
                                onDailyChallengeClaim = onDailyChallengeClaim,
                                onComboChallengeClaim = onComboChallengeClaim,
                                onWeeklyChallengeClaim = onWeeklyChallengeClaim,
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
                                onPersonalGoalClaim = onPersonalGoalClaim,
                                onProfileBadgeSelect = onProfileBadgeSelect,
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
                                onComboChallengeClaim = onComboChallengeClaim,
                                onWeeklyChallengeClaim = onWeeklyChallengeClaim,
                                onDailyChallengeDoubleRewardClick = onDailyChallengeDoubleRewardClick,
                                onAchievementClaim = onAchievementClaim
                            )

                            HomeTab.Statistics -> StatisticsTabContent(
                                bestScoresByMode = bestScoresByMode,
                                progressionState = progressionState
                            )

                            HomeTab.Collection -> CollectionTabContent(
                                progressionState = progressionState
                            )

                            HomeTab.Shop -> ShopTabContent(
                                progressionState = progressionState,
                                selectedLanguage = selectedLanguage,
                                rewardedAdUiState = rewardedAdUiState,
                                unlockedThemePopup = unlockedThemePopup,
                                popupBlocked = activeHomePopup != null && activeHomePopup != HomePopup.ThemeUnlock,
                                onThemeUnlockPopupChange = { unlockedThemePopup = it },
                                onThemeSelect = onThemeSelect,
                                onThemeBuy = onThemeBuy,
                                onThemeTrial = onThemeTrial,
                                onTargetSkinSelect = onTargetSkinSelect,
                                onTargetSkinBuy = onTargetSkinBuy,
                                onCoinChestClick = onCoinChestClick,
                                onShopCoinRewardClick = onShopCoinRewardClick
                            )

                            HomeTab.Leaderboard -> LeaderboardTabContent(
                                leaderboardSnapshot = leaderboardSnapshot,
                                dailyLeaderboardGoal = progressionState.dailyLeaderboardGoal,
                                onDailyLeaderboardGoalClaim = onDailyLeaderboardGoalClaim,
                                onModeSelected = onLeaderboardModeSelected,
                                onPeriodSelected = onLeaderboardPeriodSelected,
                                onRefreshClick = onLeaderboardRefresh
                            )

                            HomeTab.Settings -> SettingsTabContent(
                                playerProfile = playerProfile,
                                progressionState = progressionState,
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
                                onResetModeTips = onResetModeTips,
                                onRateAppClick = onRateAppClick,
                                onEditNameClick = { showPlayerNameDialog = true }
                            )
                        }
                    }

                    HomeBottomNavigation(
                        selectedTab = selectedHomeTab,
                        onTabSelected = { tab ->
                            if (activeHomePopup == null && tab != selectedHomeTab) {
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
                            onClick = {
                                if (activeHomePopup == null) {
                                    onStartClick()
                                }
                            },
                            height = if (isCompactHeight) 50.dp else 54.dp
                        )
                        SecondaryGameButton(
                            text = stringResource(R.string.quick_game_button),
                            onClick = {
                                if (activeHomePopup == null) {
                                    onQuickGameClick()
                                }
                            }
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun HomeExitDialog(
    onStayClick: () -> Unit,
    onExitClick: () -> Unit
) {
    PolishedGameDialog(
        onDismissRequest = onStayClick,
        title = stringResource(R.string.exit_app_title),
        confirmButton = {
            PrimaryGameButton(
                text = stringResource(R.string.continue_game),
                onClick = onStayClick,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            SecondaryGameButton(
                text = stringResource(R.string.exit_app_confirm),
                onClick = onExitClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Text(
            text = stringResource(R.string.exit_app_message),
            modifier = Modifier.fillMaxWidth(),
            color = ReflexGamePalette.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

internal enum class HomeTab(
    val titleRes: Int,
    val icon: String,
    val showInBottomNav: Boolean = false
) {
    Play(R.string.nav_play, "▶", true),
    Rewards(R.string.nav_rewards, "★", true),
    Shop(R.string.nav_shop, "◉", true),
    Profile(R.string.nav_profile, "◆", true),
    Leaderboard(R.string.nav_leaderboard, "#"),
    Statistics(R.string.statistics_title, "%"),
    Collection(R.string.collection_title, "▣"),
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
        HomeTab.Collection,
        HomeTab.Statistics,
        HomeTab.Season,
        HomeTab.Missions,
        HomeTab.Settings -> Unit
    }
}
