package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.feedback.rememberGameSoundHooks
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import com.reflex.tr.game.ibrh.ui.theme.Reflex_tr_game_ibrhTheme

private val ScreenHorizontalPadding = 20.dp
private val ScreenVerticalPadding = 18.dp

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    rewardedAdUiState: RewardedAdUiState = RewardedAdUiState(),
    onRewardedContinueRequested: (onRewardEarned: () -> Unit) -> Unit = { onRewardEarned ->
        onRewardEarned()
    },
    onInterstitialAdRequested: () -> Unit = {}
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
        onStartClick = viewModel::startGame,
        onHomeClick = viewModel::goToHome,
        onTargetTap = viewModel::onTargetTapped,
        onMissTap = viewModel::onMissTapped,
        onContinueClick = {
            onRewardedContinueRequested(viewModel::continueGameAfterReward)
        },
        onRetryClick = viewModel::retryGame
    )
}

@Composable
fun GameScreen(
    uiState: GameUiState,
    rewardedAdUiState: RewardedAdUiState,
    onStartClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTargetTap: () -> Unit,
    onMissTap: () -> Unit,
    onContinueClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val soundHooks = rememberGameSoundHooks()
    var missFeedbackTrigger by remember { mutableIntStateOf(0) }
    var hitFeedbackTrigger by remember { mutableIntStateOf(0) }
    var hitFeedbackPosition by remember { mutableStateOf(uiState.targetPosition) }
    var previousLives by remember { mutableIntStateOf(uiState.lives) }

    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) {
            soundHooks.onGameOver()
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
        onMissTap()
    }

    val handleTargetTap: () -> Unit = {
        if (!uiState.isGameOver) {
            hitFeedbackPosition = uiState.targetPosition
            hitFeedbackTrigger += 1
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            soundHooks.onHit()
        }
        onTargetTap()
    }

    val shouldShowContinueSlot = uiState.canContinueWithReward || uiState.hasUsedRewardContinue
    val continueButtonText = when {
        uiState.hasUsedRewardContinue ->
            androidx.compose.ui.res.stringResource(R.string.continue_used)
        rewardedAdUiState.isShowing ->
            androidx.compose.ui.res.stringResource(R.string.rewarded_opening)
        rewardedAdUiState.isReady ->
            androidx.compose.ui.res.stringResource(R.string.watch_ad_and_continue)
        rewardedAdUiState.hasLoadFailed ->
            androidx.compose.ui.res.stringResource(R.string.rewarded_not_ready)
        else ->
            androidx.compose.ui.res.stringResource(R.string.rewarded_loading)
    }
    val continueButtonEnabled =
        uiState.canContinueWithReward &&
            rewardedAdUiState.isReady &&
            !rewardedAdUiState.isShowing
    val continueButtonLoading =
        uiState.canContinueWithReward &&
            (rewardedAdUiState.isShowing || rewardedAdUiState.isLoading)
    val continueHelperText = when {
        uiState.hasUsedRewardContinue ->
            androidx.compose.ui.res.stringResource(R.string.continue_used_helper)
        rewardedAdUiState.isShowing ->
            androidx.compose.ui.res.stringResource(R.string.rewarded_opening_helper)
        rewardedAdUiState.hasLoadFailed ->
            androidx.compose.ui.res.stringResource(R.string.rewarded_not_ready_helper)
        !rewardedAdUiState.isReady ->
            androidx.compose.ui.res.stringResource(R.string.rewarded_loading_helper)
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
                onStartClick = onStartClick,
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
                    reason = uiState.gameOverReason,
                    showContinueButton = shouldShowContinueSlot,
                    continueButtonText = continueButtonText,
                    continueHelperText = continueHelperText,
                    isContinueEnabled = continueButtonEnabled,
                    isContinueLoading = continueButtonLoading,
                    onHomeClick = onHomeClick,
                    onContinueClick = onContinueClick,
                    onRetryClick = onRetryClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    Reflex_tr_game_ibrhTheme {
        GameScreen(
            uiState = GameUiState(bestScore = 27),
            rewardedAdUiState = RewardedAdUiState(isReady = true),
            onStartClick = {},
            onHomeClick = {},
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
            onStartClick = {},
            onHomeClick = {},
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
            onStartClick = {},
            onHomeClick = {},
            onTargetTap = {},
            onMissTap = {},
            onContinueClick = {},
            onRetryClick = {}
        )
    }
}
