package com.reflex.tr.game.ibrh.ui.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.game.feedback.rememberGameSoundHooks
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import com.reflex.tr.game.ibrh.ui.theme.Reflex_tr_game_ibrhTheme
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.random.Random

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

private enum class RewardVaultType {
    Coin,
    SeasonXp,
    BonusCoin,
    ThemeDiscount
}

private data class RewardVaultFeedback(
    val type: RewardVaultType,
    val amount: Int,
    val strongGlow: Boolean,
    val triggerKey: Int
)

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
                    onStartWithoutBoost = startGameSafely,
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

@Composable
private fun RewardVaultOverlay(
    feedback: RewardVaultFeedback,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var opened by remember(feedback.triggerKey) { mutableStateOf(false) }
    LaunchedEffect(feedback.triggerKey) {
        opened = true
        delay(1_850L)
        onFinished()
    }
    val scale by animateFloatAsState(
        targetValue = if (opened) 1f else 0.72f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "reward_vault_scale"
    )
    val lidRotation by animateFloatAsState(
        targetValue = if (opened) -18f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "reward_vault_lid"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (opened) {
            if (feedback.strongGlow) 0.58f else 0.36f
        } else {
            0.08f
        },
        animationSpec = tween(durationMillis = 300),
        label = "reward_vault_glow"
    )
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(120)) + scaleIn(initialScale = 0.84f),
        exit = fadeOut(animationSpec = tween(160)) + scaleOut(targetScale = 0.92f),
        modifier = modifier
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.18f),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(
                1.dp,
                if (feedback.strongGlow) {
                    Color.White.copy(alpha = 0.5f)
                } else {
                    ReflexGamePalette.textPrimary.copy(alpha = 0.24f)
                }
            ),
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        ReflexGamePalette.textPrimary.copy(alpha = glowAlpha),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(46.dp)
                            )
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Surface(
                            color = ReflexGamePalette.textPrimary.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp),
                            modifier = Modifier
                                .size(width = 58.dp, height = 18.dp)
                                .graphicsLayer {
                                    rotationX = lidRotation
                                    translationY = if (opened) -8f else 0f
                                }
                        ) {}
                        Surface(
                            color = ReflexGamePalette.cardGlassStrong,
                            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                            border = BorderStroke(1.dp, ReflexGamePalette.textPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier.size(width = 68.dp, height = 46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.reward_vault_chest_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ReflexGamePalette.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.reward_vault_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = rewardVaultText(feedback),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun rewardVaultText(feedback: RewardVaultFeedback): String {
    return when (feedback.type) {
        RewardVaultType.Coin -> stringResource(R.string.reward_vault_coin, feedback.amount)
        RewardVaultType.SeasonXp -> stringResource(R.string.reward_vault_season_xp, feedback.amount)
        RewardVaultType.BonusCoin -> stringResource(R.string.reward_vault_bonus_coin, feedback.amount)
        RewardVaultType.ThemeDiscount -> stringResource(R.string.reward_vault_theme_discount)
    }
}

@Composable
private fun ModeTipOverlay(
    mode: GameMode,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GameDialogScrimColor.copy(alpha = 0.46f))
            .clickable(onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = ReflexGamePalette.cardGlassStrong,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, modeTipAccent(mode).copy(alpha = 0.38f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.mode_tip_title, stringResource(mode.titleRes)),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(modeTipDescriptionRes(mode)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center
                )
                SecondaryGameButton(
                    text = stringResource(R.string.mode_tip_dont_show_again),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@StringRes
private fun modeTipDescriptionRes(mode: GameMode): Int {
    return when (mode) {
        GameMode.Classic -> R.string.mode_tip_classic
        GameMode.MovingTarget -> R.string.mode_tip_moving_target
        GameMode.FakeTarget -> R.string.mode_tip_fake_target
        GameMode.ColorReflex -> R.string.mode_tip_color_reflex
    }
}

private fun modeTipAccent(mode: GameMode): Color {
    return when (mode) {
        GameMode.Classic -> Color(0xFFFFD166)
        GameMode.MovingTarget -> Color(0xFF4D9FFF)
        GameMode.FakeTarget -> Color(0xFFFF6B8A)
        GameMode.ColorReflex -> Color(0xFF46F0C2)
    }
}

@Composable
private fun QuickGameSelectedOverlay(
    mode: GameMode,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, modeTipAccent(mode).copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.quick_game_title),
                style = MaterialTheme.typography.labelLarge,
                color = modeTipAccent(mode),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.quick_game_selected_mode, stringResource(mode.titleRes)),
                style = MaterialTheme.typography.titleMedium,
                color = ReflexGamePalette.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun chooseQuickGameMode(dailyFeaturedMode: GameMode): GameMode {
    val modes = GameMode.entries.toList()
    val weightedModes = modes + listOf(dailyFeaturedMode, dailyFeaturedMode)
    return weightedModes.getOrElse(Random.nextInt(weightedModes.size.coerceAtLeast(1))) {
        GameMode.Classic
    }
}

private fun shareInvite(
    context: Context,
    text: String,
    chooserTitle: String,
    onShareLaunched: () -> Unit
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooserIntent = Intent.createChooser(sendIntent, chooserTitle).apply {
        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    val launched = runCatching {
        context.startActivity(chooserIntent)
    }.isSuccess
    if (launched) {
        onShareLaunched()
    }
}

private fun shareScore(
    context: Context,
    text: String,
    chooserTitle: String,
    score: Int,
    mode: GameMode,
    isNewRecord: Boolean
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooserIntent = Intent.createChooser(sendIntent, chooserTitle).apply {
        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    val launched = runCatching {
        context.startActivity(chooserIntent)
    }.isSuccess
    if (launched) {
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.ScoreShared,
            params = Bundle().apply {
                putInt(FirebaseParam.Score.key, score.coerceAtLeast(0))
                putString(FirebaseParam.Mode.key, mode.storageKey)
                putBoolean(FirebaseParam.IsNewRecord.key, isNewRecord)
            }
        )
    }
}

@Composable
private fun BoostSelectionBottomSheet(
    coins: Int,
    onStartWithoutBoost: () -> Unit,
    onPowerUpClick: (GamePowerUp) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(GameDialogScrimColor)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        val sheetMaxHeight = maxHeight * 0.92f
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = sheetMaxHeight)
                .navigationBarsPadding()
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
                    text = stringResource(R.string.power_up_sheet_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.power_up_sheet_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GamePowerUp.entries.forEach { powerUp ->
                        PowerUpOptionRow(
                            powerUp = powerUp,
                            coins = coins,
                            onPowerUpClick = onPowerUpClick
                        )
                    }
                }
                SecondaryGameButton(
                    text = stringResource(R.string.power_up_start_without),
                    onClick = onStartWithoutBoost,
                    modifier = Modifier.height(48.dp)
                )
            }
        }
    }
}

@Composable
private fun PowerUpOptionRow(
    powerUp: GamePowerUp,
    coins: Int,
    onPowerUpClick: (GamePowerUp) -> Unit
) {
    val canBuyWithCoins = coins >= powerUp.coinPrice
    val missingCoins = (powerUp.coinPrice - coins.coerceAtLeast(0)).coerceAtLeast(0)
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
                    text = powerUpIcon(powerUp),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(powerUp.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(powerUp.descriptionRes),
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
                    text = if (canBuyWithCoins) {
                        stringResource(R.string.power_up_buy_with_coins, powerUp.coinPrice)
                    } else {
                        stringResource(R.string.power_up_missing_coins, missingCoins)
                    },
                    onClick = { onPowerUpClick(powerUp) },
                    enabled = canBuyWithCoins,
                    modifier = Modifier
                        .height(48.dp)
                )
            }
        }
    }
}

private fun powerUpIcon(powerUp: GamePowerUp): String {
    return when (powerUp) {
        GamePowerUp.ExtraTime -> "+5"
        GamePowerUp.ExtraLife -> "+1"
        GamePowerUp.ComboProtection -> "C"
        GamePowerUp.FirstMistakeForgiveness -> "!"
    }
}

private fun calculateAccuracyPercent(uiState: GameUiState): Int {
    if (uiState.totalAttempts <= 0) return 0
    return ((uiState.successfulHits * 100f) / uiState.totalAttempts).toInt().coerceIn(0, 100)
}

@Composable
private fun ExitGameDialog(
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
            shape = RoundedCornerShape(22.dp),
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
        shape = RoundedCornerShape(14.dp),
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
            onPowerUpClick = { true },
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
            onProfileBadgeSelect = {},
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
            onPowerUpClick = { true },
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
            onProfileBadgeSelect = {},
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
            onPowerUpClick = { true },
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
            onProfileBadgeSelect = {},
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
