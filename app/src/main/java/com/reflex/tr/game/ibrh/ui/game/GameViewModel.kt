package com.reflex.tr.game.ibrh.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val INITIAL_LIVES = 3
        private const val INITIAL_TIME_SECONDS = 30
        private const val REWARD_CONTINUE_TIME_SECONDS = 10
        private const val REWARD_CONTINUE_GRACE_MILLIS = 2_000L
        private const val INITIAL_TARGET_SIZE_DP = 82
        private const val MIN_TARGET_SIZE_DP = 48
        private const val INITIAL_TARGET_VISIBLE_DURATION_MS = 1_800L
        private const val MIN_TARGET_VISIBLE_DURATION_MS = 850L
        private const val INTERSTITIAL_AD_GAME_OVER_INTERVAL = 2
        private const val REASON_TIME_UP = "Süre doldu."
        private const val REASON_NO_LIVES = "Canların tükendi."
    }

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var targetTimeoutJob: Job? = null
    private var rewardContinueGraceJob: Job? = null
    private var completedGameCount = 0
    private val gamePreferences = GamePreferences(application)

    init {
        observeBestScore()
    }

    fun startGame() {
        launchNewGame()
    }

    fun onTargetTapped() {
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        _uiState.update {
            val newScore = it.score + 1
            val newDifficulty = calculateDifficultyLevel(newScore)
            it.copy(
                score = newScore,
                bestScore = maxOf(it.bestScore, newScore),
                isNewBestScore = it.isNewBestScore || newScore > it.bestScore,
                difficultyLevel = newDifficulty,
                targetSizeDp = calculateTargetSizeDp(newScore),
                targetVisibleDurationMillis = calculateTargetVisibleDurationMillis(newScore),
                targetPosition = generateRandomTargetPosition(
                    currentX = it.targetPosition.xFraction,
                    currentY = it.targetPosition.yFraction
                ),
                targetLifetimeKey = it.targetLifetimeKey + 1
            )
        }
        startTargetTimeout()
    }

    fun onMissTapped() {
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        val remainingLives = currentState.lives - 1
        if (remainingLives <= 0) {
            endGame()
            return
        }

        _uiState.update {
            it.copy(
                lives = remainingLives,
                targetPosition = generateRandomTargetPosition(
                    currentX = it.targetPosition.xFraction,
                    currentY = it.targetPosition.yFraction
                ),
                targetLifetimeKey = it.targetLifetimeKey + 1
            )
        }
        startTargetTimeout()
    }

    fun retryGame() {
        launchNewGame()
    }

    fun pauseGame() {
        val currentState = _uiState.value
        if (!currentState.hasGameStarted || currentState.isPaused || currentState.isGameOver) return

        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        val currentState = _uiState.value
        if (!currentState.hasGameStarted || !currentState.isPaused || currentState.isGameOver) return

        _uiState.update { it.copy(isPaused = false) }
        if (currentState.isResumeGracePeriod) {
            startRewardContinueGracePeriod()
        } else {
            startTimer()
            startTargetTimeout()
        }
    }

    fun goToHome() {
        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
        _uiState.value = createInitialState().copy(
            bestScore = _uiState.value.bestScore,
            isNewBestScore = false
        )
    }

    fun onRewardContinueEarned() {
        val currentState = _uiState.value
        if (!currentState.isGameOver || currentState.hasUsedRewardContinue) return

        _uiState.update {
            it.copy(
                isRewardContinueReady = true,
                canContinueWithReward = true
            )
        }
    }

    fun continueGameAfterReward() {
        val currentState = _uiState.value
        if (
            !currentState.isGameOver ||
            currentState.hasUsedRewardContinue ||
            !currentState.isRewardContinueReady
        ) return

        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
        _uiState.update {
            it.copy(
                hasGameStarted = true,
                lives = 1,
                timeLeftSeconds = REWARD_CONTINUE_TIME_SECONDS,
                targetPosition = generateRandomTargetPosition(),
                isPaused = false,
                isResumeGracePeriod = true,
                isGameOver = false,
                gameOverReason = null,
                hasUsedRewardContinue = true,
                isRewardContinueReady = false,
                canContinueWithReward = false,
                shouldRequestInterstitialAd = false,
                targetLifetimeKey = it.targetLifetimeKey + 1
            )
        }
        startRewardContinueGracePeriod()
    }

    fun onInterstitialAdRequestHandled() {
        _uiState.update { it.copy(shouldRequestInterstitialAd = false) }
    }

    private fun launchNewGame() {
        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
        _uiState.value = createInitialState().copy(
            bestScore = _uiState.value.bestScore,
            hasGameStarted = true,
            isNewBestScore = false
        )
        startTimer()
        startTargetTimeout()
    }

    private fun observeBestScore() {
        viewModelScope.launch {
            gamePreferences.bestScoreFlow.collect { bestScore ->
                _uiState.update { it.copy(bestScore = maxOf(it.bestScore, bestScore)) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (
                _uiState.value.timeLeftSeconds > 0 &&
                !_uiState.value.isPaused &&
                !_uiState.value.isResumeGracePeriod &&
                !_uiState.value.isGameOver
            ) {
                delay(1_000L)

                val currentState = _uiState.value
                if (
                    currentState.isPaused ||
                    currentState.isResumeGracePeriod ||
                    currentState.isGameOver
                ) break

                val newTime = currentState.timeLeftSeconds - 1
                if (newTime <= 0) {
                    finishGame(
                        timeLeftSeconds = 0,
                        reason = REASON_TIME_UP
                    )
                    break
                } else {
                    _uiState.update { it.copy(timeLeftSeconds = newTime) }
                }
            }
        }
    }

    private fun endGame() {
        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
        finishGame(
            lives = 0,
            reason = REASON_NO_LIVES
        )
    }

    private fun startTargetTimeout() {
        targetTimeoutJob?.cancel()
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        val targetLifetimeKey = currentState.targetLifetimeKey
        val visibleDurationMillis = currentState.targetVisibleDurationMillis
        targetTimeoutJob = viewModelScope.launch {
            delay(visibleDurationMillis)
            val latestState = _uiState.value
            if (
                latestState.hasGameStarted &&
                    !latestState.isPaused &&
                    !latestState.isResumeGracePeriod &&
                    !latestState.isGameOver &&
                    latestState.targetLifetimeKey == targetLifetimeKey
            ) {
                onTargetTimedOut()
            }
        }
    }

    private fun onTargetTimedOut() {
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        val remainingLives = currentState.lives - 1
        if (remainingLives <= 0) {
            endGame()
            return
        }

        _uiState.update {
            it.copy(
                lives = remainingLives,
                targetPosition = generateRandomTargetPosition(
                    currentX = it.targetPosition.xFraction,
                    currentY = it.targetPosition.yFraction
                ),
                targetLifetimeKey = it.targetLifetimeKey + 1
            )
        }
        startTargetTimeout()
    }

    private fun finishGame(
        lives: Int = _uiState.value.lives,
        timeLeftSeconds: Int = _uiState.value.timeLeftSeconds,
        reason: String? = _uiState.value.gameOverReason
    ) {
        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
        completedGameCount += 1
        val shouldRequestInterstitialAd =
            completedGameCount % INTERSTITIAL_AD_GAME_OVER_INTERVAL == 0

        val finalState = _uiState.updateAndGet {
            it.copy(
                lives = lives,
                timeLeftSeconds = timeLeftSeconds,
                hasGameStarted = true,
                isPaused = false,
                isResumeGracePeriod = false,
                isGameOver = true,
                gameOverReason = reason,
                canContinueWithReward = !it.hasUsedRewardContinue,
                isRewardContinueReady = false,
                shouldRequestInterstitialAd = shouldRequestInterstitialAd
            )
        }

        viewModelScope.launch {
            gamePreferences.saveBestScore(finalState.score)
        }
    }

    private fun createInitialState(): GameUiState {
        return GameUiState(
            lives = INITIAL_LIVES,
            timeLeftSeconds = INITIAL_TIME_SECONDS,
            difficultyLevel = calculateDifficultyLevel(score = 0),
            targetSizeDp = calculateTargetSizeDp(score = 0),
            targetVisibleDurationMillis = calculateTargetVisibleDurationMillis(score = 0),
            targetPosition = generateRandomTargetPosition()
        )
    }

    private fun startRewardContinueGracePeriod() {
        rewardContinueGraceJob?.cancel()
        rewardContinueGraceJob = viewModelScope.launch {
            delay(REWARD_CONTINUE_GRACE_MILLIS)
            val currentState = _uiState.value
            if (
                currentState.hasGameStarted &&
                currentState.isResumeGracePeriod &&
                !currentState.isPaused &&
                !currentState.isGameOver
            ) {
                _uiState.update { it.copy(isResumeGracePeriod = false) }
                startTimer()
                startTargetTimeout()
            }
        }
    }

    private fun calculateDifficultyLevel(score: Int): Int {
        return (score / 5 + 1).coerceIn(1, 8)
    }

    private fun calculateTargetSizeDp(score: Int): Int {
        val sizeReduction = (score / 3) * 4
        return (INITIAL_TARGET_SIZE_DP - sizeReduction).coerceAtLeast(MIN_TARGET_SIZE_DP)
    }

    private fun calculateTargetVisibleDurationMillis(score: Int): Long {
        val durationReduction = (score / 2) * 80L
        return (INITIAL_TARGET_VISIBLE_DURATION_MS - durationReduction)
            .coerceAtLeast(MIN_TARGET_VISIBLE_DURATION_MS)
    }

    private fun GameUiState.canAcceptGameplayInput(): Boolean {
        return hasGameStarted &&
            !isPaused &&
            !isResumeGracePeriod &&
            !isGameOver
    }

    private fun generateRandomTargetPosition(
        currentX: Float? = null,
        currentY: Float? = null
    ): TargetPosition {
        repeat(20) {
            val newPosition = TargetPosition(
                xFraction = Random.nextFloat().coerceIn(0.15f, 0.85f),
                yFraction = Random.nextFloat().coerceIn(0.2f, 0.8f)
            )

            val isFarEnough =
                currentX == null || currentY == null ||
                    (kotlin.math.abs(newPosition.xFraction - currentX) > 0.12f) ||
                    (kotlin.math.abs(newPosition.yFraction - currentY) > 0.12f)

            if (isFarEnough) {
                return newPosition
            }
        }

        return TargetPosition(
            xFraction = 0.5f,
            yFraction = 0.5f
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
    }
}
