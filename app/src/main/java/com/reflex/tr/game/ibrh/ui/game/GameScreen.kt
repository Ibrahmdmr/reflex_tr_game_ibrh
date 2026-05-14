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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    rewardedAdUiState: RewardedAdUiState = RewardedAdUiState(),
    selectedLanguage: AppLanguage = AppLanguage.Turkish,
    isSoundEnabled: Boolean = true,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    onSoundEnabledChange: (Boolean) -> Unit = {},
    onRewardedContinueRequested: (onRewardEarned: () -> Unit) -> Unit = { onRewardEarned ->
        onRewardEarned()
    },
    onInterstitialAdRequested: () -> Unit = {},
    onHowToPlayClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.shouldRequestInterstitialAd) {
        if (uiState.shouldRequestInterstitialAd) {
            onInterstitialAdRequested()
            viewModel.onInterstitialAdRequestHandled()
        }
    }

    GameScreen(
        uiState = uiState,
        rewardedAdUiState = rewardedAdUiState,
        selectedLanguage = selectedLanguage,
        isSoundEnabled = isSoundEnabled,
        onStartClick = viewModel::startGame,
        onModeStartClick = viewModel::selectMode,
        onHowToPlayClick = onHowToPlayClick,
        onLanguageSelected = onLanguageSelected,
        onSoundEnabledChange = onSoundEnabledChange,
        onHomeClick = viewModel::goToHome,
        onPauseGame = viewModel::pauseGame,
        onResumeGame = viewModel::resumeGame,
        onTargetTap = viewModel::onTargetTapped,
        onMissTap = viewModel::onMissTapped,
        onContinueClick = {
            if (uiState.isRewardContinueReady) {
                viewModel.continueGameAfterReward()
            } else {
                onRewardedContinueRequested(viewModel::onRewardContinueEarned)
            }
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
    onStartClick: () -> Unit,
    onModeStartClick: (GameMode) -> Unit,
    onHowToPlayClick: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onHomeClick: () -> Unit,
    onPauseGame: () -> Unit,
    onResumeGame: () -> Unit,
    onTargetTap: (Long) -> Unit,
    onMissTap: () -> Unit,
    onContinueClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val soundHooks = rememberGameSoundHooks(isSoundEnabled = isSoundEnabled)
    var missFeedbackTrigger by remember { mutableIntStateOf(0) }
    var hitFeedbackTrigger by remember { mutableIntStateOf(0) }
    var hitFeedbackPosition by remember { mutableStateOf(uiState.targetPosition) }
    var previousLives by remember { mutableIntStateOf(uiState.lives) }
    var showExitGameDialog by remember { mutableStateOf(false) }
    val isGameplayInputEnabled =
        !uiState.isPaused &&
            !uiState.isResumeGracePeriod &&
            !uiState.isGameOver

    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) {
            soundHooks.onGameOver()
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
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                soundHooks.onHit()
                val nextCombo = uiState.combo + 1
                if (nextCombo == 2 || nextCombo == 5 || nextCombo == 10) {
                    soundHooks.onCombo()
                }
            }
            onTargetTap(targetId)
        }
    }

    BackHandler(enabled = uiState.hasGameStarted) {
        if (uiState.isGameOver) {
            onHomeClick()
        } else if (!showExitGameDialog) {
            onPauseGame()
            showExitGameDialog = true
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
                        ReflexGamePalette.homeGradientBottom
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
                dailyChallengeState = uiState.dailyChallengeState,
                isSoundEnabled = isSoundEnabled,
                onStartClick = onStartClick,
                onModeStartClick = onModeStartClick,
                onHowToPlayClick = onHowToPlayClick,
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
                onSoundToggleClick = { onSoundEnabledChange(!isSoundEnabled) },
                modifier = Modifier.align(Alignment.Center)
            )
            return@Box
        }

        GamePlayContent(
            uiState = uiState,
            missFeedbackTrigger = missFeedbackTrigger,
            hitFeedbackTrigger = hitFeedbackTrigger,
            hitFeedbackPosition = hitFeedbackPosition,
            onTargetTap = handleTargetTap,
            onMissTap = handleMissTap
        )

        if (uiState.isResumeGracePeriod) {
            RewardContinueGraceOverlay(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = uiState.isGameOver,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.96f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA050A1A)),
                contentAlignment = Alignment.Center
            ) {
                GameOverOverlay(
                    score = uiState.score,
                    bestScore = uiState.bestScore,
                    isNewBestScore = uiState.isNewBestScore,
                    mode = uiState.selectedMode,
                    maxCombo = uiState.maxCombo,
                    accuracyPercent = calculateAccuracyPercent(uiState),
                    reason = uiState.gameOverReason,
                    showContinueButton = shouldShowContinueSlot,
                    continueButtonText = continueButtonText,
                    continueHelperText = continueHelperText,
                    isContinueEnabled = continueButtonEnabled,
                    isContinueLoading = continueButtonLoading,
                    onHomeClick = onHomeClick,
                    onChangeModeClick = onHomeClick,
                    onContinueClick = onContinueClick,
                    onRetryClick = onRetryClick
                )
            }
        }

        if (showExitGameDialog) {
            ExitGameDialog(
                selectedLanguage = selectedLanguage,
                onContinueClick = {
                    showExitGameDialog = false
                    onResumeGame()
                },
                onHomeClick = {
                    showExitGameDialog = false
                    onHomeClick()
                }
            )
        }
    }
}

private fun calculateAccuracyPercent(uiState: GameUiState): Int {
    if (uiState.totalAttempts <= 0) return 0
    return ((uiState.successfulHits * 100f) / uiState.totalAttempts).toInt().coerceIn(0, 100)
}

@Composable
private fun ExitGameDialog(
    selectedLanguage: AppLanguage,
    onContinueClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val title = localizedStringResource(R.string.exit_game_title, selectedLanguage)
    val message = localizedStringResource(R.string.exit_game_message, selectedLanguage)
    val continueText = localizedStringResource(R.string.continue_game, selectedLanguage)
    val homeText = localizedStringResource(R.string.back_to_home, selectedLanguage)

    AlertDialog(
        onDismissRequest = onContinueClick,
        containerColor = ReflexGamePalette.cardGlassStrong,
        title = {
            Text(
                text = title,
                color = ReflexGamePalette.textPrimary
            )
        },
        text = {
            Text(
                text = message,
                color = ReflexGamePalette.textSecondary
            )
        },
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
    )
}

@Composable
private fun localizedStringResource(
    @StringRes id: Int,
    selectedLanguage: AppLanguage
): String {
    val context = LocalContext.current
    val localizedContext = remember(context, selectedLanguage) {
        context.createLanguageContext(selectedLanguage)
    }
    return localizedContext.getString(id)
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
            onModeStartClick = {},
            onHowToPlayClick = {},
            onLanguageSelected = {},
            onSoundEnabledChange = {},
            onHomeClick = {},
            onPauseGame = {},
            onResumeGame = {},
            onTargetTap = {},
            onMissTap = {},
            onContinueClick = {},
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
            onModeStartClick = {},
            onHowToPlayClick = {},
            onLanguageSelected = {},
            onSoundEnabledChange = {},
            onHomeClick = {},
            onPauseGame = {},
            onResumeGame = {},
            onTargetTap = {},
            onMissTap = {},
            onContinueClick = {},
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
                gameOverReason = "Canların tükendi.",
                canContinueWithReward = true
            ),
            rewardedAdUiState = RewardedAdUiState(isReady = true),
            selectedLanguage = AppLanguage.Turkish,
            isSoundEnabled = true,
            onStartClick = {},
            onModeStartClick = {},
            onHowToPlayClick = {},
            onLanguageSelected = {},
            onSoundEnabledChange = {},
            onHomeClick = {},
            onPauseGame = {},
            onResumeGame = {},
            onTargetTap = {},
            onMissTap = {},
            onContinueClick = {},
            onRetryClick = {}
        )
    }
}
