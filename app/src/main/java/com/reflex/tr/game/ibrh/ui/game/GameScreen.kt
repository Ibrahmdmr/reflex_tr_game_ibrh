package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.game.feedback.rememberGameSoundHooks
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import com.reflex.tr.game.ibrh.ui.theme.Reflex_tr_game_ibrhTheme
import java.util.Locale

private val ScreenHorizontalPadding = 20.dp
private val ScreenVerticalPadding = 18.dp

private enum class GamePopup {
    Boost,
    PauseExit,
    GameOver
}

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    rewardedAdUiState: RewardedAdUiState = RewardedAdUiState(),
    selectedLanguage: AppLanguage = AppLanguage.Turkish,
    isSoundEnabled: Boolean = true,
    isEffectSoundEnabled: Boolean = true,
    isVibrationEnabled: Boolean = true,
    isDailyRewardNotificationEnabled: Boolean = false,
    isStreakNotificationEnabled: Boolean = false,
    isNewMissionNotificationEnabled: Boolean = false,
    isNotificationPermissionGranted: Boolean = true,
    isOnboardingCompleted: Boolean = true,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    onSoundEnabledChange: (Boolean) -> Unit = {},
    onEffectSoundEnabledChange: (Boolean) -> Unit = {},
    onVibrationEnabledChange: (Boolean) -> Unit = {},
    onDailyRewardNotificationChange: (Boolean) -> Unit = {},
    onStreakNotificationChange: (Boolean) -> Unit = {},
    onNewMissionNotificationChange: (Boolean) -> Unit = {},
    onOpenOnboarding: () -> Unit = {},
    onRewardedAdRequested: (RewardedAction, onRewardEarned: () -> Unit) -> Unit = { _, onRewardEarned ->
        onRewardEarned()
    },
    onInterstitialAdRequested: () -> Boolean = { false },
    onInAppReviewRequested: (
        totalGames: Int,
        isNewBestScore: Boolean,
        score: Int,
        maxCombo: Int
    ) -> Unit = { _, _, _, _ -> },
    onRateAppClick: () -> Unit = {},
    onExitAppRequested: () -> Unit = {},
    onHowToPlayClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.shouldRequestInterstitialAd) {
        if (uiState.shouldRequestInterstitialAd) {
            val wasShown = onInterstitialAdRequested()
            viewModel.onInterstitialAdRequestHandled(wasShown)
        }
    }

    LaunchedEffect(uiState.isGameOver, uiState.progressionState.totalGames) {
        if (uiState.isGameOver) {
            onInAppReviewRequested(
                uiState.progressionState.totalGames,
                uiState.isNewBestScore,
                uiState.score,
                uiState.maxCombo
            )
        }
    }

    GameScreen(
        uiState = uiState,
        rewardedAdUiState = rewardedAdUiState,
        selectedLanguage = selectedLanguage,
        isSoundEnabled = isSoundEnabled,
        isEffectSoundEnabled = isEffectSoundEnabled,
        isVibrationEnabled = isVibrationEnabled,
        isDailyRewardNotificationEnabled = isDailyRewardNotificationEnabled,
        isStreakNotificationEnabled = isStreakNotificationEnabled,
        isNewMissionNotificationEnabled = isNewMissionNotificationEnabled,
        isNotificationPermissionGranted = isNotificationPermissionGranted,
        isOnboardingCompleted = isOnboardingCompleted,
        onStartClick = viewModel::startGame,
        onBoostCoinClick = viewModel::startGameWithCoinBoost,
        onBoostAdClick = { boost ->
            onRewardedAdRequested(RewardedAction.Boost) {
                viewModel.startGameWithRewardedBoost(boost)
            }
        },
        onModeStartClick = viewModel::selectMode,
        onHowToPlayClick = onHowToPlayClick,
        onLanguageSelected = onLanguageSelected,
        onSoundEnabledChange = onSoundEnabledChange,
        onEffectSoundEnabledChange = onEffectSoundEnabledChange,
        onVibrationEnabledChange = onVibrationEnabledChange,
        onDailyRewardNotificationChange = onDailyRewardNotificationChange,
        onStreakNotificationChange = onStreakNotificationChange,
        onNewMissionNotificationChange = onNewMissionNotificationChange,
        onOpenOnboarding = onOpenOnboarding,
        onDailyRewardClaim = viewModel::claimDailyReward,
        onDailyRewardDialogShown = viewModel::markDailyRewardDialogShown,
        onDailyChallengeClaim = viewModel::claimDailyChallengeReward,
        onSeasonRewardClaim = viewModel::claimSeasonReward,
        onSeasonXpBoostClick = {
            onRewardedAdRequested(RewardedAction.SeasonXpBoost, viewModel::activateSeasonXpBoost)
        },
        onSeasonMissionClaim = viewModel::claimSeasonMission,
        onDailyStreakProtect = {
            onRewardedAdRequested(RewardedAction.ProtectStreak, viewModel::protectDailyRewardStreak)
        },
        onCoinChestClick = {
            onRewardedAdRequested(RewardedAction.CoinChest, viewModel::onCoinChestRewardEarned)
        },
        onShopCoinRewardClick = {
            onRewardedAdRequested(RewardedAction.ShopCoinReward, viewModel::onShopCoinRewardEarned)
        },
        onDailyChallengeDoubleRewardClick = {
            onRewardedAdRequested(
                RewardedAction.DailyChallengeDoubleReward,
                viewModel::onDailyChallengeDoubleRewardEarned
            )
        },
        onAchievementClaim = viewModel::claimAchievementReward,
        onThemeSelect = viewModel::selectTheme,
        onThemeBuy = viewModel::buyTheme,
        onThemeTrial = { theme ->
            onRewardedAdRequested(RewardedAction.UnlockTheme) {
                viewModel.tryThemeForOneGame(theme)
            }
        },
        onPlayerNameChange = viewModel::updatePlayerName,
        onPlayerTitleSelect = viewModel::selectPlayerTitle,
        onLeaderboardModeSelected = viewModel::selectLeaderboardMode,
        onLeaderboardPeriodSelected = viewModel::selectLeaderboardPeriod,
        onLeaderboardRefresh = viewModel::refreshLeaderboard,
        onLeaderboardOpenedForMission = viewModel::onLeaderboardOpenedForMission,
        onShopOpenedForMission = viewModel::onShopOpenedForMission,
        onStorePreviewModeChange = viewModel::setStorePreviewMode,
        onRateAppClick = onRateAppClick,
        onExitAppRequested = onExitAppRequested,
        onHomeClick = viewModel::goToHome,
        onPauseGame = viewModel::pauseGame,
        onResumeGame = viewModel::resumeGame,
        onTargetTap = viewModel::onTargetTapped,
        onMissTap = viewModel::onMissTapped,
        onContinueClick = {
            if (uiState.isRewardContinueReady) {
                viewModel.continueGameAfterReward()
            } else {
                onRewardedAdRequested(RewardedAction.Continue, viewModel::onRewardContinueEarned)
            }
        },
        onDoubleCoinsClick = {
            onRewardedAdRequested(RewardedAction.DoubleCoins, viewModel::onDoubleCoinsRewardEarned)
        },
        onRetryClick = viewModel::retryGame
    )
}

@Composable
fun GameScreen(
    uiState: GameUiState,
    rewardedAdUiState: RewardedAdUiState,
    selectedLanguage: AppLanguage,
    isSoundEnabled: Boolean,
    isEffectSoundEnabled: Boolean = true,
    isVibrationEnabled: Boolean = true,
    isDailyRewardNotificationEnabled: Boolean = false,
    isStreakNotificationEnabled: Boolean = false,
    isNewMissionNotificationEnabled: Boolean = false,
    isNotificationPermissionGranted: Boolean = true,
    isOnboardingCompleted: Boolean = true,
    onStartClick: () -> Unit,
    onModeStartClick: (GameMode) -> Unit,
    onHowToPlayClick: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onEffectSoundEnabledChange: (Boolean) -> Unit = {},
    onVibrationEnabledChange: (Boolean) -> Unit = {},
    onDailyRewardNotificationChange: (Boolean) -> Unit = {},
    onStreakNotificationChange: (Boolean) -> Unit = {},
    onNewMissionNotificationChange: (Boolean) -> Unit = {},
    onOpenOnboarding: () -> Unit = {},
    onDailyRewardClaim: () -> Unit,
    onDailyRewardDialogShown: () -> Unit = {},
    onDailyChallengeClaim: () -> Unit = {},
    onSeasonRewardClaim: (Int) -> Unit,
    onSeasonXpBoostClick: () -> Unit = {},
    onSeasonMissionClaim: (String) -> Unit = {},
    onDailyStreakProtect: () -> Unit,
    onCoinChestClick: () -> Unit = {},
    onShopCoinRewardClick: () -> Unit = {},
    onBoostCoinClick: (GameBoost) -> Boolean,
    onBoostAdClick: (GameBoost) -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit = {},
    onAchievementClaim: (String) -> Unit,
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onThemeTrial: (PlayerTheme) -> Unit,
    onPlayerNameChange: (String) -> Boolean,
    onPlayerTitleSelect: (PlayerTitle) -> Unit,
    onLeaderboardModeSelected: (GameMode) -> Unit,
    onLeaderboardPeriodSelected: (LeaderboardPeriod) -> Unit,
    onLeaderboardRefresh: () -> Unit,
    onLeaderboardOpenedForMission: () -> Unit = {},
    onShopOpenedForMission: () -> Unit = {},
    onStorePreviewModeChange: (Boolean) -> Unit = {},
    onRateAppClick: () -> Unit = {},
    onExitAppRequested: () -> Unit = {},
    onHomeClick: () -> Unit,
    onPauseGame: () -> Unit,
    onResumeGame: () -> Unit,
    onTargetTap: (Long) -> Unit,
    onMissTap: () -> Unit,
    onContinueClick: () -> Unit,
    onDoubleCoinsClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val soundHooks = rememberGameSoundHooks(isSoundEnabled = isSoundEnabled && isEffectSoundEnabled)
    val selectedThemeSpec = themeVisualSpec(uiState.progressionState.activeTheme)
    val backgroundPulse by rememberInfiniteTransition(label = "screen_background_pulse").animateFloat(
        initialValue = 0.18f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "screen_background_pulse_value"
    )
    var missFeedbackTrigger by remember { mutableIntStateOf(0) }
    var hitFeedbackTrigger by remember { mutableIntStateOf(0) }
    var hitFeedbackPosition by remember { mutableStateOf(uiState.targetPosition) }
    var previousLives by remember { mutableIntStateOf(uiState.lives) }
    var showExitGameDialog by rememberSaveable { mutableStateOf(false) }
    var showBoostSheet by rememberSaveable { mutableStateOf(false) }
    val activeGamePopup = when {
        uiState.isGameOver -> GamePopup.GameOver
        showExitGameDialog -> GamePopup.PauseExit
        showBoostSheet && !uiState.hasGameStarted -> GamePopup.Boost
        else -> null
    }
    val isGameplayInputEnabled =
        !uiState.isPaused &&
            !uiState.isResumeGracePeriod &&
            !uiState.isGameOver

    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) {
            showBoostSheet = false
            showExitGameDialog = false
            soundHooks.onGameOver()
        }
    }

    LaunchedEffect(uiState.hasGameStarted, uiState.isResumeGracePeriod) {
        if (uiState.hasGameStarted) {
            showBoostSheet = false
        }
        if (uiState.isResumeGracePeriod) {
            showExitGameDialog = false
        }
    }

    LaunchedEffect(uiState.timeLeftSeconds, uiState.hasGameStarted, uiState.isGameOver) {
        if (
            uiState.hasGameStarted &&
            !uiState.isGameOver &&
            uiState.timeLeftSeconds in 1..5
        ) {
            soundHooks.onCountdown()
        }
    }

    LaunchedEffect(uiState.lives, uiState.hasGameStarted) {
        if (uiState.hasGameStarted && uiState.lives < previousLives) {
            missFeedbackTrigger += 1
            if (isVibrationEnabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            soundHooks.onMiss()
        }
        previousLives = uiState.lives
    }

    val handleMissTap: () -> Unit = {
        if (isGameplayInputEnabled) {
            onMissTap()
        }
    }

    val handleTargetTap: (Long) -> Unit = { targetId ->
        if (isGameplayInputEnabled) {
            val tappedTarget = uiState.targets.firstOrNull { it.id == targetId }
            if (tappedTarget?.role == GameTargetRole.Correct) {
                hitFeedbackPosition = tappedTarget.position
                hitFeedbackTrigger += 1
                if (isVibrationEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                soundHooks.onHit()
                val nextCombo = uiState.combo + 1
                if (nextCombo == 2 || nextCombo == 5 || nextCombo == 10) {
                    soundHooks.onCombo()
                }
            }
            onTargetTap(targetId)
        }
    }

    BackHandler(enabled = uiState.hasGameStarted || activeGamePopup != null) {
        when (activeGamePopup) {
            GamePopup.GameOver -> {
                showExitGameDialog = false
                showBoostSheet = false
                onHomeClick()
            }
            GamePopup.PauseExit -> {
                showExitGameDialog = false
                onResumeGame()
            }
            GamePopup.Boost -> showBoostSheet = false
            null -> {
                if (uiState.hasGameStarted && !showExitGameDialog) {
                    onPauseGame()
                    showExitGameDialog = true
                }
            }
        }
    }

    val requestBoostSheet = {
        if (activeGamePopup == null && !uiState.hasGameStarted) {
            showBoostSheet = true
        }
    }

    val goHomeSafely = {
        showBoostSheet = false
        showExitGameDialog = false
        onHomeClick()
    }

    val retrySafely = {
        showBoostSheet = false
        showExitGameDialog = false
        onRetryClick()
    }

    val continueSafely = {
        showExitGameDialog = false
        onContinueClick()
    }

    val doubleCoinsSafely = {
        showExitGameDialog = false
        onDoubleCoinsClick()
    }

    val startGameSafely = {
        showBoostSheet = false
        onStartClick()
    }

    val startBoostAdSafely: (GameBoost) -> Unit = { boost ->
        showBoostSheet = false
        onBoostAdClick(boost)
    }

    val startBoostCoinSafely: (GameBoost) -> Unit = { boost ->
        if (onBoostCoinClick(boost)) {
            showBoostSheet = false
        }
    }

    val returnHomeFromGameOver = {
        goHomeSafely()
    }

    val returnHomeFromPause = {
        showExitGameDialog = false
        onHomeClick()
    }

    val resumeFromPause = {
        showExitGameDialog = false
        onResumeGame()
    }

    val playButtonClick = {
        requestBoostSheet()
    }

    val gameOverVisible = activeGamePopup == GamePopup.GameOver

    val pauseExitVisible = activeGamePopup == GamePopup.PauseExit

    val boostVisible = activeGamePopup == GamePopup.Boost

    val isPopupBlockingInput = activeGamePopup != null

    val shouldHandleMissTap: () -> Unit = {
        if (!isPopupBlockingInput) {
            handleMissTap()
        }
    }

    val shouldHandleTargetTap: (Long) -> Unit = { targetId ->
        if (!isPopupBlockingInput) {
            handleTargetTap(targetId)
        }
    }

    val shouldShowContinueSlot =
        uiState.canContinueWithReward ||
            uiState.isRewardContinueReady ||
            uiState.hasUsedRewardContinue
    val continueButtonText = when {
        uiState.isRewardContinueReady ->
            stringResource(R.string.continue_game)
        uiState.hasUsedRewardContinue ->
            stringResource(R.string.continue_used)
        rewardedAdUiState.isShowing ->
            stringResource(R.string.rewarded_opening)
        rewardedAdUiState.isReady ->
            stringResource(R.string.watch_ad_and_continue)
        rewardedAdUiState.hasLoadFailed ->
            stringResource(R.string.rewarded_not_ready)
        else ->
            stringResource(R.string.rewarded_loading)
    }
    val continueButtonEnabled =
        uiState.isRewardContinueReady ||
            (
                uiState.canContinueWithReward &&
                    rewardedAdUiState.isReady &&
                    !rewardedAdUiState.isShowing
            )
    val continueButtonLoading =
        !uiState.isRewardContinueReady &&
        uiState.canContinueWithReward &&
            (rewardedAdUiState.isShowing || rewardedAdUiState.isLoading)
    val continueHelperText = when {
        uiState.isRewardContinueReady ->
            stringResource(R.string.rewarded_continue_ready_helper)
        uiState.hasUsedRewardContinue ->
            stringResource(R.string.continue_used_helper)
        rewardedAdUiState.isShowing ->
            stringResource(R.string.rewarded_opening_helper)
        rewardedAdUiState.hasLoadFailed ->
            stringResource(R.string.rewarded_not_ready_helper)
        !rewardedAdUiState.isReady ->
            stringResource(R.string.rewarded_loading_helper)
        else -> null
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ReflexGamePalette.homeGradientTop,
                        selectedThemeSpec.primary.copy(alpha = 0.38f + backgroundPulse * 0.22f),
                        selectedThemeSpec.backgroundBottom
                    )
                )
            )
            .padding(
                horizontal = ScreenHorizontalPadding,
                vertical = ScreenVerticalPadding
            )
    ) {
        if (!uiState.hasGameStarted) {
            HomeContent(
                bestScore = uiState.bestScore,
                bestScoresByMode = uiState.bestScoresByMode,
                selectedMode = uiState.selectedMode,
                dailyFeaturedMode = uiState.dailyFeaturedMode,
                dailyChallengeState = uiState.dailyChallengeState,
                progressionState = uiState.progressionState,
                shouldAutoShowDailyRewardDialog = uiState.shouldAutoShowDailyRewardDialog,
                playerProfile = uiState.playerProfile,
                leaderboardSnapshot = uiState.leaderboardSnapshot,
                rewardedAdUiState = rewardedAdUiState,
                isSoundEnabled = isSoundEnabled,
                onStartClick = playButtonClick,
                onModeStartClick = onModeStartClick,
                onHowToPlayClick = onHowToPlayClick,
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
                onSoundToggleClick = { onSoundEnabledChange(!isSoundEnabled) },
                isEffectSoundEnabled = isEffectSoundEnabled,
                isVibrationEnabled = isVibrationEnabled,
                isDailyRewardNotificationEnabled = isDailyRewardNotificationEnabled,
                isStreakNotificationEnabled = isStreakNotificationEnabled,
                isNewMissionNotificationEnabled = isNewMissionNotificationEnabled,
                isNotificationPermissionGranted = isNotificationPermissionGranted,
                isOnboardingCompleted = isOnboardingCompleted,
                isStorePreviewMode = uiState.isStorePreviewMode,
                onSoundEnabledChange = onSoundEnabledChange,
                onEffectSoundEnabledChange = onEffectSoundEnabledChange,
                onVibrationEnabledChange = onVibrationEnabledChange,
                onDailyRewardNotificationChange = onDailyRewardNotificationChange,
                onStreakNotificationChange = onStreakNotificationChange,
                onNewMissionNotificationChange = onNewMissionNotificationChange,
                onOpenOnboarding = onOpenOnboarding,
                onDailyRewardClaim = onDailyRewardClaim,
                onDailyRewardDialogShown = onDailyRewardDialogShown,
                onDailyChallengeClaim = onDailyChallengeClaim,
                onSeasonRewardClaim = onSeasonRewardClaim,
                onSeasonXpBoostClick = onSeasonXpBoostClick,
                onSeasonMissionClaim = onSeasonMissionClaim,
                onDailyStreakProtect = onDailyStreakProtect,
                onCoinChestClick = onCoinChestClick,
                onShopCoinRewardClick = onShopCoinRewardClick,
                onDailyChallengeDoubleRewardClick = onDailyChallengeDoubleRewardClick,
                onAchievementClaim = onAchievementClaim,
                onThemeSelect = onThemeSelect,
                onThemeBuy = onThemeBuy,
                onThemeTrial = onThemeTrial,
                onPlayerNameChange = onPlayerNameChange,
                onPlayerTitleSelect = onPlayerTitleSelect,
                onLeaderboardModeSelected = onLeaderboardModeSelected,
                onLeaderboardPeriodSelected = onLeaderboardPeriodSelected,
                onLeaderboardRefresh = onLeaderboardRefresh,
                onLeaderboardOpenedForMission = onLeaderboardOpenedForMission,
                onShopOpenedForMission = onShopOpenedForMission,
                onStorePreviewModeChange = onStorePreviewModeChange,
                onRateAppClick = onRateAppClick,
                onExitAppRequested = onExitAppRequested,
                modifier = Modifier.align(Alignment.Center)
            )
            if (boostVisible) {
                BoostSelectionBottomSheet(
                    coins = uiState.progressionState.coins,
                    rewardedAdUiState = rewardedAdUiState,
                    onStartWithoutBoost = startGameSafely,
                    onCoinBoostClick = startBoostCoinSafely,
                    onAdBoostClick = startBoostAdSafely,
                    onDismiss = { showBoostSheet = false }
                )
            }
            return@Box
        }

        GamePlayContent(
            uiState = uiState,
            missFeedbackTrigger = missFeedbackTrigger,
            hitFeedbackTrigger = hitFeedbackTrigger,
            hitFeedbackPosition = hitFeedbackPosition,
            onTargetTap = shouldHandleTargetTap,
            onMissTap = shouldHandleMissTap
        )

        if (uiState.isResumeGracePeriod) {
            RewardContinueGraceOverlay(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = gameOverVisible,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.96f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GameDialogScrimColor),
                contentAlignment = Alignment.Center
            ) {
                GameOverOverlay(
                    score = uiState.score,
                    bestScore = uiState.bestScore,
                    isNewBestScore = uiState.isNewBestScore,
                    mode = uiState.selectedMode,
                    maxCombo = uiState.maxCombo,
                    accuracyPercent = calculateAccuracyPercent(uiState),
                    reason = uiState.gameOverReasonRes?.let { stringResource(it) } ?: uiState.gameOverReason,
                    earnedCoins = uiState.earnedCoinsThisGame,
                    baseCoins = uiState.baseCoinsThisGame,
                    totalCoins = uiState.progressionState.coins,
                    seasonXp = uiState.progressionState.season.xp,
                    isCoinDoubleClaimed = uiState.isCoinDoubleClaimed,
                    showContinueButton = shouldShowContinueSlot,
                    continueButtonText = continueButtonText,
                    continueHelperText = continueHelperText,
                    isContinueEnabled = continueButtonEnabled,
                    isContinueLoading = continueButtonLoading,
                    onHomeClick = returnHomeFromGameOver,
                    onChangeModeClick = returnHomeFromGameOver,
                    onOpenThemeStoreClick = returnHomeFromGameOver,
                    isDoubleCoinsEnabled = !uiState.isCoinDoubleClaimed &&
                        uiState.baseCoinsThisGame > 0 &&
                        rewardedAdUiState.isReady &&
                        !rewardedAdUiState.isShowing,
                    isDoubleCoinsLoading = rewardedAdUiState.isShowing || rewardedAdUiState.isLoading,
                    doubleCoinsText = when {
                        uiState.isCoinDoubleClaimed -> stringResource(R.string.coin_bonus_claimed)
                        rewardedAdUiState.isReady -> stringResource(R.string.double_coins)
                        rewardedAdUiState.isLoading -> stringResource(R.string.rewarded_loading)
                        else -> stringResource(R.string.rewarded_not_ready)
                    },
                    onContinueClick = continueSafely,
                    onDoubleCoinsClick = doubleCoinsSafely,
                    onRetryClick = retrySafely
                )
            }
        }

        if (pauseExitVisible) {
            ExitGameDialog(
                selectedLanguage = selectedLanguage,
                tomorrowRewardCoins = uiState.progressionState.dailyReward.nextRewardCoins,
                onContinueClick = resumeFromPause,
                onHomeClick = returnHomeFromPause
            )
        }
    }
}

@Composable
private fun BoostSelectionBottomSheet(
    coins: Int,
    rewardedAdUiState: RewardedAdUiState,
    onStartWithoutBoost: () -> Unit,
    onCoinBoostClick: (GameBoost) -> Unit,
    onAdBoostClick: (GameBoost) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameDialogScrimColor)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = {}),
            color = ReflexGamePalette.cardGlassStrong,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            border = BorderStroke(1.dp, ReflexGamePalette.neonPurple.copy(alpha = 0.42f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.boost_sheet_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.boost_sheet_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                GameBoost.entries.forEach { boost ->
                    BoostOptionRow(
                        boost = boost,
                        coins = coins,
                        rewardedAdUiState = rewardedAdUiState,
                        onCoinBoostClick = onCoinBoostClick,
                        onAdBoostClick = onAdBoostClick
                    )
                }
                SecondaryGameButton(
                    text = stringResource(R.string.boost_start_without),
                    onClick = onStartWithoutBoost,
                    modifier = Modifier.height(48.dp)
                )
            }
        }
    }
}

@Composable
private fun BoostOptionRow(
    boost: GameBoost,
    coins: Int,
    rewardedAdUiState: RewardedAdUiState,
    onCoinBoostClick: (GameBoost) -> Unit,
    onAdBoostClick: (GameBoost) -> Unit
) {
    val canBuyWithCoins = coins >= boost.coinPrice
    val canUseAd = rewardedAdUiState.isReady && !rewardedAdUiState.isShowing
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.07f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
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
                Text(
                    text = boostIcon(boost),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(boost.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(boost.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecondaryGameButton(
                    text = stringResource(R.string.boost_buy_with_coins, boost.coinPrice),
                    onClick = { onCoinBoostClick(boost) },
                    enabled = canBuyWithCoins,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
                SecondaryGameButton(
                    text = when {
                        rewardedAdUiState.isShowing || rewardedAdUiState.isLoading -> stringResource(R.string.rewarded_loading)
                        rewardedAdUiState.hasLoadFailed || !rewardedAdUiState.isReady -> stringResource(R.string.rewarded_not_ready)
                        else -> stringResource(R.string.boost_use_ad)
                    },
                    onClick = { onAdBoostClick(boost) },
                    enabled = canUseAd,
                    isLoading = rewardedAdUiState.isShowing || rewardedAdUiState.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
            }
        }
    }
}

private fun boostIcon(boost: GameBoost): String {
    return when (boost) {
        GameBoost.ExtraTime -> "+5"
        GameBoost.ExtraLife -> "+1"
        GameBoost.ComboStart -> "x5"
    }
}

private fun calculateAccuracyPercent(uiState: GameUiState): Int {
    if (uiState.totalAttempts <= 0) return 0
    return ((uiState.successfulHits * 100f) / uiState.totalAttempts).toInt().coerceIn(0, 100)
}

@Composable
private fun ExitGameDialog(
    selectedLanguage: AppLanguage,
    tomorrowRewardCoins: Int,
    onContinueClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val title = localizedStringResource(R.string.exit_game_title, selectedLanguage)
    val message = localizedStringResource(R.string.exit_game_message, selectedLanguage)
    val fomoMessage = localizedStringResource(
        R.string.daily_reward_exit_fomo,
        selectedLanguage,
        tomorrowRewardCoins
    )
    val continueText = localizedStringResource(R.string.continue_game, selectedLanguage)
    val homeText = localizedStringResource(R.string.back_to_home, selectedLanguage)

    PolishedGameDialog(
        onDismissRequest = onContinueClick,
        title = title,
        confirmButton = {
            PrimaryGameButton(
                text = continueText,
                onClick = onContinueClick,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            SecondaryGameButton(
                text = homeText,
                onClick = onHomeClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Text(
            text = message,
            modifier = Modifier.fillMaxWidth(),
            color = ReflexGamePalette.textSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            text = fomoMessage,
            modifier = Modifier.fillMaxWidth(),
            color = ReflexGamePalette.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun localizedStringResource(
    @StringRes id: Int,
    selectedLanguage: AppLanguage,
    vararg args: Any
): String {
    val context = LocalContext.current
    val localizedContext = remember(context, selectedLanguage) {
        context.createLanguageContext(selectedLanguage)
    }
    return if (args.isEmpty()) {
        localizedContext.getString(id)
    } else {
        localizedContext.getString(id, *args)
    }
}

private fun Context.createLanguageContext(language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.code)
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}

@Composable
private fun RewardContinueGraceOverlay(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(24.dp),
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

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    Reflex_tr_game_ibrhTheme {
        GameScreen(
            uiState = GameUiState(bestScore = 27),
            rewardedAdUiState = RewardedAdUiState(isReady = true),
            selectedLanguage = AppLanguage.Turkish,
            isSoundEnabled = true,
            onStartClick = {},
            onBoostCoinClick = { true },
            onBoostAdClick = {},
            onModeStartClick = {},
            onHowToPlayClick = {},
            onLanguageSelected = {},
            onSoundEnabledChange = {},
            onDailyRewardClaim = {},
            onSeasonRewardClaim = {},
            onDailyStreakProtect = {},
            onAchievementClaim = {},
            onThemeSelect = {},
            onThemeBuy = {},
            onThemeTrial = {},
            onPlayerNameChange = { true },
            onPlayerTitleSelect = {},
            onLeaderboardModeSelected = {},
            onLeaderboardPeriodSelected = {},
            onLeaderboardRefresh = {},
            onHomeClick = {},
            onPauseGame = {},
            onResumeGame = {},
            onTargetTap = {},
            onMissTap = {},
            onContinueClick = {},
            onDoubleCoinsClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayingPreview() {
    Reflex_tr_game_ibrhTheme {
        GameScreen(
            uiState = GameUiState(
                score = 12,
                bestScore = 27,
                lives = 2,
                timeLeftSeconds = 18,
                hasGameStarted = true
            ),
            rewardedAdUiState = RewardedAdUiState(isReady = true),
            selectedLanguage = AppLanguage.Turkish,
            isSoundEnabled = true,
            onStartClick = {},
            onBoostCoinClick = { true },
            onBoostAdClick = {},
            onModeStartClick = {},
            onHowToPlayClick = {},
            onLanguageSelected = {},
            onSoundEnabledChange = {},
            onDailyRewardClaim = {},
            onSeasonRewardClaim = {},
            onDailyStreakProtect = {},
            onAchievementClaim = {},
            onThemeSelect = {},
            onThemeBuy = {},
            onThemeTrial = {},
            onPlayerNameChange = { true },
            onPlayerTitleSelect = {},
            onLeaderboardModeSelected = {},
            onLeaderboardPeriodSelected = {},
            onLeaderboardRefresh = {},
            onHomeClick = {},
            onPauseGame = {},
            onResumeGame = {},
            onTargetTap = {},
            onMissTap = {},
            onContinueClick = {},
            onDoubleCoinsClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameOverPreview() {
    Reflex_tr_game_ibrhTheme {
        GameScreen(
            uiState = GameUiState(
                score = 16,
                bestScore = 27,
                isNewBestScore = true,
                lives = 0,
                timeLeftSeconds = 0,
                hasGameStarted = true,
                isGameOver = true,
                gameOverReasonRes = R.string.game_over_reason_no_lives,
                canContinueWithReward = true
            ),
            rewardedAdUiState = RewardedAdUiState(isReady = true),
            selectedLanguage = AppLanguage.Turkish,
            isSoundEnabled = true,
            onStartClick = {},
            onBoostCoinClick = { true },
            onBoostAdClick = {},
            onModeStartClick = {},
            onHowToPlayClick = {},
            onLanguageSelected = {},
            onSoundEnabledChange = {},
            onDailyRewardClaim = {},
            onSeasonRewardClaim = {},
            onDailyStreakProtect = {},
            onAchievementClaim = {},
            onThemeSelect = {},
            onThemeBuy = {},
            onThemeTrial = {},
            onPlayerNameChange = { true },
            onPlayerTitleSelect = {},
            onLeaderboardModeSelected = {},
            onLeaderboardPeriodSelected = {},
            onLeaderboardRefresh = {},
            onHomeClick = {},
            onPauseGame = {},
            onResumeGame = {},
            onTargetTap = {},
            onMissTap = {},
            onContinueClick = {},
            onDoubleCoinsClick = {},
            onRetryClick = {}
        )
    }
}
