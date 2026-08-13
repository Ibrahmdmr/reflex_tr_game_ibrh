package com.reflex.tr.game.ibrh.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.feedback.rememberGameSoundHooks
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import kotlinx.coroutines.delay

private val ScreenHorizontalPadding = 20.dp

private val ScreenVerticalPadding = 18.dp

private enum class GameHapticType {
    Light,
    Miss,
    Combo,
    Success,
    Record
}

private enum class GamePopup {
    Boost,
    QuickGame,
    ModeTip,
    PauseExit,
    GameOver
}

private fun HapticFeedback.performSafeGameHaptic(
    enabled: Boolean,
    type: GameHapticType
) {
    if (!enabled) return
    runCatching {
        when (type) {
            GameHapticType.Light,
            GameHapticType.Combo,
            GameHapticType.Success -> performHapticFeedback(HapticFeedbackType.TextHandleMove)
            GameHapticType.Miss,
            GameHapticType.Record -> performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory),
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
        onPowerUpClick = viewModel::startGameWithPowerUp,
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
        onComboChallengeClaim = viewModel::claimComboChallengeReward,
        onWeeklyChallengeClaim = viewModel::claimWeeklyChallengeReward,
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
        onInviteShareClick = viewModel::onInviteShareCompleted,
        onDailyChallengeDoubleRewardClick = {
            onRewardVaultOpened ->
            onRewardedAdRequested(
                RewardedAction.DailyChallengeDoubleReward,
            ) {
                viewModel.onDailyChallengeDoubleRewardEarned()
                onRewardVaultOpened()
            }
        },
        onAchievementClaim = viewModel::claimAchievementReward,
        onThemeSelect = viewModel::selectTheme,
        onThemeBuy = viewModel::buyTheme,
        onThemeTrial = { theme ->
            onRewardedAdRequested(RewardedAction.UnlockTheme) {
                viewModel.tryThemeForOneGame(theme)
            }
        },
        onTargetSkinSelect = viewModel::selectTargetSkin,
        onTargetSkinBuy = viewModel::buyTargetSkin,
        onPlayerNameChange = viewModel::updatePlayerName,
        onPlayerTitleSelect = viewModel::selectPlayerTitle,
        onProfileBadgeSelect = viewModel::selectProfileBadge,
        onLeaderboardModeSelected = viewModel::selectLeaderboardMode,
        onLeaderboardPeriodSelected = viewModel::selectLeaderboardPeriod,
        onLeaderboardRefresh = viewModel::refreshLeaderboard,
        onDailyLeaderboardGoalClaim = viewModel::claimDailyLeaderboardGoalReward,
        onPersonalGoalClaim = viewModel::claimPersonalGoalReward,
        onModeTipShown = viewModel::markModeTipShown,
        onResetModeTips = viewModel::resetModeTips,
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
    onComboChallengeClaim: () -> Unit = {},
    onWeeklyChallengeClaim: () -> Unit = {},
    onSeasonRewardClaim: (Int) -> Unit,
    onSeasonXpBoostClick: () -> Unit = {},
    onSeasonMissionClaim: (String) -> Unit = {},
    onDailyStreakProtect: () -> Unit,
    onCoinChestClick: () -> Unit = {},
    onShopCoinRewardClick: () -> Unit = {},
    onInviteShareClick: () -> Unit = {},
    onPowerUpClick: (GamePowerUp) -> Boolean,
    onBoostCoinClick: (GameBoost) -> Boolean = { false },
    onBoostAdClick: (GameBoost) -> Unit = {},
    onDailyChallengeDoubleRewardClick: (() -> Unit) -> Unit = { onRewardVaultOpened -> onRewardVaultOpened() },
    onAchievementClaim: (String) -> Unit,
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onThemeTrial: (PlayerTheme) -> Unit,
    onTargetSkinSelect: (TargetSkin) -> Unit = {},
    onTargetSkinBuy: (TargetSkin) -> Unit = {},
    onPlayerNameChange: (String) -> Boolean,
    onPlayerTitleSelect: (PlayerTitle) -> Unit,
    onProfileBadgeSelect: (ProfileBadge) -> Unit,
    onLeaderboardModeSelected: (GameMode) -> Unit,
    onLeaderboardPeriodSelected: (LeaderboardPeriod) -> Unit,
    onLeaderboardRefresh: () -> Unit,
    onDailyLeaderboardGoalClaim: () -> Unit = {},
    onPersonalGoalClaim: () -> Unit = {},
    onModeTipShown: (GameMode) -> Unit = {},
    onResetModeTips: () -> Unit = {},
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
    val context = LocalContext.current
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
    var previousComboForHaptic by remember { mutableIntStateOf(uiState.combo) }
    var previousComboForSound by remember { mutableIntStateOf(uiState.combo) }
    var previousSuccessfulHitsForSound by remember { mutableIntStateOf(uiState.successfulHits) }
    var showExitGameDialog by rememberSaveable { mutableStateOf(false) }
    var showBoostSheet by rememberSaveable { mutableStateOf(false) }
    var quickGameModeToStart by rememberSaveable { mutableStateOf<GameMode?>(null) }
    var rewardVaultTrigger by remember { mutableIntStateOf(0) }
    var rewardVaultFeedback by remember { mutableStateOf<RewardVaultFeedback?>(null) }
    val modeTipVisible = uiState.hasGameStarted &&
        !uiState.isGameOver &&
        uiState.selectedMode !in uiState.shownModeTips
    val activeGamePopup = when {
        uiState.isGameOver -> GamePopup.GameOver
        quickGameModeToStart != null -> GamePopup.QuickGame
        showExitGameDialog -> GamePopup.PauseExit
        showBoostSheet && !uiState.hasGameStarted -> GamePopup.Boost
        modeTipVisible -> GamePopup.ModeTip
        else -> null
    }
    val isGameplayInputEnabled =
        !uiState.isPaused &&
            !uiState.isResumeGracePeriod &&
            !uiState.isGameOver &&
            !modeTipVisible

    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) {
            showBoostSheet = false
            quickGameModeToStart = null
            showExitGameDialog = false
            val hasNewRecord = uiState.isNewBestScore ||
                uiState.newPersonalRecords.isNotEmpty() ||
                uiState.progressionState.latestUnlockedProfileBadges.isNotEmpty()
            if (hasNewRecord) {
                soundHooks.onNewRecord()
            } else {
                soundHooks.onGameOver()
            }
            if (hasNewRecord) {
                haptic.performSafeGameHaptic(
                    enabled = isVibrationEnabled,
                    type = GameHapticType.Record
                )
            }
        }
    }

    LaunchedEffect(uiState.combo, uiState.hasGameStarted, uiState.isGameOver) {
        if (
            uiState.hasGameStarted &&
            !uiState.isGameOver &&
            ((previousComboForHaptic < 5 && uiState.combo >= 5) ||
                (previousComboForHaptic < 10 && uiState.combo >= 10) ||
                (previousComboForHaptic < 20 && uiState.combo >= 20))
        ) {
            haptic.performSafeGameHaptic(
                enabled = isVibrationEnabled,
                type = GameHapticType.Combo
            )
        }
        previousComboForHaptic = uiState.combo
    }

    LaunchedEffect(uiState.successfulHits, uiState.combo, uiState.lastTimingGrade) {
        if (uiState.successfulHits > previousSuccessfulHitsForSound) {
            when {
                previousComboForSound < 20 && uiState.combo >= 20 -> soundHooks.onComboBig()
                (previousComboForSound < 5 && uiState.combo >= 5) ||
                    (previousComboForSound < 10 && uiState.combo >= 10) -> soundHooks.onCombo()
                uiState.lastTimingGrade == TimingGrade.Perfect -> soundHooks.onPerfect()
                uiState.lastTimingGrade == TimingGrade.Great -> soundHooks.onGreat()
                else -> soundHooks.onHit()
            }
        }
        previousSuccessfulHitsForSound = uiState.successfulHits
        previousComboForSound = uiState.combo
    }

    LaunchedEffect(quickGameModeToStart) {
        val mode = quickGameModeToStart ?: return@LaunchedEffect
        delay(720L)
        showBoostSheet = false
        showExitGameDialog = false
        onModeStartClick(mode)
        onStartClick()
        quickGameModeToStart = null
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
            haptic.performSafeGameHaptic(
                enabled = isVibrationEnabled,
                type = GameHapticType.Miss
            )
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
                haptic.performSafeGameHaptic(
                    enabled = isVibrationEnabled,
                    type = GameHapticType.Light
                )
            } else if (uiState.isBossRoundActive) {
                haptic.performSafeGameHaptic(
                    enabled = isVibrationEnabled,
                    type = GameHapticType.Miss
                )
                soundHooks.onMiss()
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
            GamePopup.QuickGame -> quickGameModeToStart = null
            GamePopup.ModeTip -> onModeTipShown(uiState.selectedMode)
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

    val quickGameSafely = {
        if (activeGamePopup == null && !uiState.hasGameStarted && quickGameModeToStart == null) {
            quickGameModeToStart = chooseQuickGameMode(uiState.dailyFeaturedMode.mode)
        }
    }

    val startPowerUpSafely: (GamePowerUp) -> Unit = { powerUp ->
        if (onPowerUpClick(powerUp)) {
            showBoostSheet = false
        }
    }

    val startBoostWithCoinsSafely: (GameBoost) -> Unit = { boost ->
        if (onBoostCoinClick(boost)) {
            showBoostSheet = false
        }
    }

    // The ad path starts the run from its reward callback, so the sheet closes right away.
    val startBoostWithAdSafely: (GameBoost) -> Unit = { boost ->
        showBoostSheet = false
        onBoostAdClick(boost)
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
            uiState.isRewardContinueReady
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
    val safeShareScore = uiState.score.coerceAtLeast(0)
    val shareText = stringResource(
        if (uiState.isNewBestScore) {
            R.string.share_score_new_record_text
        } else {
            R.string.share_score_text
        },
        safeShareScore,
        stringResource(R.string.play_store_link)
    )
    val shareChooserTitle = stringResource(R.string.share_score_chooser_title)
    val inviteShareText = stringResource(
        R.string.invite_share_text,
        stringResource(R.string.play_store_link)
    )
    val inviteShareChooserTitle = stringResource(R.string.invite_share_chooser_title)
    val shareScoreSafely = {
        shareScore(
            context = context,
            text = shareText,
            chooserTitle = shareChooserTitle,
            score = safeShareScore,
            mode = uiState.selectedMode,
            isNewRecord = uiState.isNewBestScore
        )
    }
    val showRewardVault: (RewardVaultType, Int, Boolean) -> Unit = { type, amount, strongGlow ->
        soundHooks.onReward()
        haptic.performSafeGameHaptic(
            enabled = isVibrationEnabled,
            type = if (strongGlow) GameHapticType.Record else GameHapticType.Success
        )
        rewardVaultTrigger += 1
        rewardVaultFeedback = RewardVaultFeedback(
            type = type,
            amount = amount.coerceAtLeast(0),
            strongGlow = strongGlow,
            triggerKey = rewardVaultTrigger
        )
    }
    val claimDailyRewardWithVault = {
        val reward = uiState.progressionState.dailyReward
        val canShowVault = reward.canClaim && !reward.claimedToday
        onDailyRewardClaim()
        if (canShowVault) {
            showRewardVault(RewardVaultType.Coin, reward.rewardCoins, reward.isSuperReward)
        }
    }
    val claimDailyChallengeWithVault = {
        val challenge = uiState.dailyChallengeState
        val canShowVault = challenge.completed && !challenge.rewardClaimed
        onDailyChallengeClaim()
        if (canShowVault) {
            showRewardVault(RewardVaultType.BonusCoin, challenge.rewardCoins, false)
        }
    }
    val claimWeeklyChallengeWithVault = {
        val challenge = uiState.progressionState.weeklyChallenge
        val canShowVault = challenge.completed && !challenge.claimed
        onWeeklyChallengeClaim()
        if (canShowVault) {
            showRewardVault(RewardVaultType.Coin, challenge.rewardCoins, false)
        }
    }
    val claimDailyChallengeDoubleWithVault = {
        val challenge = uiState.dailyChallengeState
        val canShowVault = challenge.completed &&
            challenge.rewardClaimed &&
            !challenge.doubleRewardClaimed &&
            rewardedAdUiState.isReady &&
            !rewardedAdUiState.isShowing
        if (canShowVault) {
            onDailyChallengeDoubleRewardClick {
                showRewardVault(RewardVaultType.BonusCoin, challenge.rewardCoins, true)
            }
        } else {
            onDailyChallengeDoubleRewardClick {}
        }
    }
    val claimComboChallengeWithHaptic = {
        val challenge = uiState.progressionState.comboChallenge
        if (challenge.completed && !challenge.claimed) {
            soundHooks.onReward()
            haptic.performSafeGameHaptic(
                enabled = isVibrationEnabled,
                type = GameHapticType.Success
            )
        }
        onComboChallengeClaim()
    }
    val claimSeasonRewardWithHaptic: (Int) -> Unit = { level ->
        soundHooks.onReward()
        haptic.performSafeGameHaptic(
            enabled = isVibrationEnabled,
            type = GameHapticType.Success
        )
        onSeasonRewardClaim(level)
    }
    val claimSeasonMissionWithVault: (String) -> Unit = { missionId ->
        val mission = uiState.progressionState.season.missions.firstOrNull { it.id == missionId }
        val canShowVault = mission?.completed == true && !mission.claimed
        onSeasonMissionClaim(missionId)
        if (canShowVault) {
            showRewardVault(RewardVaultType.SeasonXp, mission?.rewardSeasonXp ?: 0, false)
        }
    }
    val claimAchievementWithHaptic: (String) -> Unit = { achievementId ->
        soundHooks.onReward()
        haptic.performSafeGameHaptic(
            enabled = isVibrationEnabled,
            type = GameHapticType.Success
        )
        onAchievementClaim(achievementId)
    }
    val buyThemeWithHaptic: (PlayerTheme) -> Unit = { theme ->
        val canBuy = theme !in uiState.progressionState.unlockedThemes &&
            uiState.progressionState.coins >= theme.coinPrice
        onThemeBuy(theme)
        if (canBuy) {
            soundHooks.onUnlock()
            haptic.performSafeGameHaptic(
                enabled = isVibrationEnabled,
                type = GameHapticType.Success
            )
        }
    }
    val buyTargetSkinWithHaptic: (TargetSkin) -> Unit = { skin ->
        val canBuy = skin !in uiState.progressionState.unlockedTargetSkins &&
            uiState.progressionState.coins >= skin.coinPrice
        onTargetSkinBuy(skin)
        if (canBuy) {
            soundHooks.onUnlock()
            haptic.performSafeGameHaptic(
                enabled = isVibrationEnabled,
                type = GameHapticType.Success
            )
        }
    }
    val claimDailyLeaderboardGoalWithHaptic = {
        val goal = uiState.progressionState.dailyLeaderboardGoal
        if (goal.completed && !goal.claimed) {
            soundHooks.onReward()
            haptic.performSafeGameHaptic(
                enabled = isVibrationEnabled,
                type = GameHapticType.Success
            )
        }
        onDailyLeaderboardGoalClaim()
    }
    val claimPersonalGoalWithHaptic = {
        val goal = uiState.progressionState.personalGoal
        if (goal.completed && !goal.claimed) {
            soundHooks.onReward()
            haptic.performSafeGameHaptic(
                enabled = isVibrationEnabled,
                type = GameHapticType.Success
            )
        }
        onPersonalGoalClaim()
    }
    val openCoinChestWithSound = {
        if (uiState.progressionState.coinChest.canOpen) {
            soundHooks.onReward()
        }
        onCoinChestClick()
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
                onQuickGameClick = quickGameSafely,
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
                onDailyRewardClaim = claimDailyRewardWithVault,
                onDailyRewardDialogShown = onDailyRewardDialogShown,
                onDailyChallengeClaim = claimDailyChallengeWithVault,
                onComboChallengeClaim = claimComboChallengeWithHaptic,
                onWeeklyChallengeClaim = claimWeeklyChallengeWithVault,
                onSeasonRewardClaim = claimSeasonRewardWithHaptic,
                onSeasonXpBoostClick = onSeasonXpBoostClick,
                onSeasonMissionClaim = claimSeasonMissionWithVault,
                onDailyStreakProtect = onDailyStreakProtect,
                onCoinChestClick = openCoinChestWithSound,
                onShopCoinRewardClick = onShopCoinRewardClick,
                onInviteShareClick = {
                    shareInvite(
                        context = context,
                        text = inviteShareText,
                        chooserTitle = inviteShareChooserTitle,
                        onShareLaunched = onInviteShareClick
                    )
                },
                onDailyChallengeDoubleRewardClick = claimDailyChallengeDoubleWithVault,
                onAchievementClaim = claimAchievementWithHaptic,
                onThemeSelect = onThemeSelect,
                onThemeBuy = buyThemeWithHaptic,
                onThemeTrial = onThemeTrial,
                onTargetSkinSelect = onTargetSkinSelect,
                onTargetSkinBuy = buyTargetSkinWithHaptic,
                onPlayerNameChange = onPlayerNameChange,
                onPlayerTitleSelect = onPlayerTitleSelect,
                onProfileBadgeSelect = onProfileBadgeSelect,
                onLeaderboardModeSelected = onLeaderboardModeSelected,
                onLeaderboardPeriodSelected = onLeaderboardPeriodSelected,
                onLeaderboardRefresh = onLeaderboardRefresh,
                onDailyLeaderboardGoalClaim = claimDailyLeaderboardGoalWithHaptic,
                onPersonalGoalClaim = claimPersonalGoalWithHaptic,
                onLeaderboardOpenedForMission = onLeaderboardOpenedForMission,
                onShopOpenedForMission = onShopOpenedForMission,
                onStorePreviewModeChange = onStorePreviewModeChange,
                onResetModeTips = onResetModeTips,
                onRateAppClick = onRateAppClick,
                onExitAppRequested = onExitAppRequested,
                modifier = Modifier.align(Alignment.Center)
            )
            if (boostVisible) {
                BoostSelectionBottomSheet(
                    coins = uiState.progressionState.coins,
                    isRewardedAdReady = rewardedAdUiState.isReady && !rewardedAdUiState.isShowing,
                    onStartWithoutBoost = startGameSafely,
                    onBoostCoinClick = startBoostWithCoinsSafely,
                    onBoostAdClick = startBoostWithAdSafely,
                    onPowerUpClick = startPowerUpSafely,
                    onDismiss = { showBoostSheet = false }
                )
            }
            quickGameModeToStart?.let { mode ->
                QuickGameSelectedOverlay(
                    mode = mode,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            rewardVaultFeedback?.let { feedback ->
                RewardVaultOverlay(
                    feedback = feedback,
                    onFinished = { rewardVaultFeedback = null },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            return@Box
        }

        GamePlayContent(
            uiState = uiState,
            missFeedbackTrigger = missFeedbackTrigger,
            hitFeedbackTrigger = hitFeedbackTrigger,
            hitFeedbackPosition = hitFeedbackPosition,
            timingGrade = uiState.lastTimingGrade,
            onTargetTap = shouldHandleTargetTap,
            onMissTap = shouldHandleMissTap
        )

        if (uiState.isResumeGracePeriod) {
            RewardContinueGraceOverlay(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (modeTipVisible) {
            ModeTipOverlay(
                mode = uiState.selectedMode,
                onDismiss = { onModeTipShown(uiState.selectedMode) },
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
                    maxFlawlessStreak = uiState.maxFlawlessStreak,
                    bossRoundBonusCoins = uiState.bossRoundTotalBonusCoins,
                    ultraMomentBonusCoins = uiState.ultraMomentTotalBonusCoins,
                    ultraMomentHits = uiState.ultraMomentTotalHits,
                    perfectHits = uiState.perfectHits,
                    greatHits = uiState.greatHits,
                    accuracyPercent = calculateAccuracyPercent(uiState),
                    newPersonalRecords = uiState.newPersonalRecords,
                    unlockedProfileBadges = uiState.progressionState.latestUnlockedProfileBadges,
                    reason = uiState.gameOverReasonRes?.let { stringResource(it) } ?: uiState.gameOverReason,
                    earnedCoins = uiState.earnedCoinsThisGame,
                    baseCoins = uiState.baseCoinsThisGame,
                    totalCoins = uiState.progressionState.coins,
                    seasonXp = uiState.progressionState.season.xp,
                    comboChallenge = uiState.progressionState.comboChallenge,
                    dailyMiniTournament = uiState.progressionState.dailyMiniTournament,
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
                    onShareScoreClick = shareScoreSafely,
                    onRetryClick = retrySafely
                )
            }
        }

        if (pauseExitVisible) {
            ExitGameDialog(
                selectedLanguage = selectedLanguage,
                mode = uiState.selectedMode,
                score = uiState.score,
                timeLeftSeconds = uiState.timeLeftSeconds,
                combo = uiState.combo,
                theme = uiState.progressionState.activeTheme,
                onContinueClick = resumeFromPause,
                onRetryClick = retrySafely,
                onHomeClick = returnHomeFromPause
            )
        }
        rewardVaultFeedback?.let { feedback ->
            RewardVaultOverlay(
                feedback = feedback,
                onFinished = { rewardVaultFeedback = null },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun calculateAccuracyPercent(uiState: GameUiState): Int {
    if (uiState.totalAttempts <= 0) return 0
    return ((uiState.successfulHits * 100f) / uiState.totalAttempts).toInt().coerceIn(0, 100)
}
