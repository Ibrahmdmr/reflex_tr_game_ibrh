package com.reflex.tr.game.ibrh.ui.game

import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.AdAnalyticsTracker
import com.reflex.tr.game.ibrh.ads.AdConfig
import com.reflex.tr.game.ibrh.ads.adParams
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
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
        private const val REWARD_CONTINUE_GRACE_MILLIS = 2_000L
        private const val INITIAL_TARGET_SIZE_DP = 82
        private const val MIN_TARGET_SIZE_DP = 48
        private const val INITIAL_TARGET_VISIBLE_DURATION_MS = 1_800L
        private const val MIN_TARGET_VISIBLE_DURATION_MS = 850L
        private const val COLOR_RULE_CHANGE_INTERVAL_MS = 5_000L
        private const val COMBO_WINDOW_MILLIS = 1_250L
        private val REASON_TIME_UP = R.string.game_over_reason_time_up
        private val REASON_NO_LIVES = R.string.game_over_reason_no_lives
    }

    private val adConfig = AdConfig.Default
    private val gamePreferences = GamePreferences(application)
    private val leaderboardRepository: LeaderboardRepository = FirestoreLeaderboardRepository()
    private var selectedLeaderboardMode = GameMode.Classic
    private var selectedLeaderboardPeriod = LeaderboardPeriod.AllTime
    private var leaderboardRefreshTick = 0
    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var targetTimeoutJob: Job? = null
    private var rewardContinueGraceJob: Job? = null
    private var movingTargetJob: Job? = null
    private var colorRuleJob: Job? = null
    private var comboResetJob: Job? = null
    private var leaderboardRefreshJob: Job? = null
    private var completedGameCount = 0
    private var nextInterstitialGameCount = randomInterstitialInterval()
    private var nextTargetId = 0L
    private var lastHitElapsedMillis = 0L
    private var gameStartedElapsedMillis = 0L
    private var lastInterstitialShownElapsedMillis = 0L

    init {
        observeBestScore()
        refreshProfileAndLeaderboard()
    }

    fun startGame() {
        trackGameStart(_uiState.value.selectedMode)
        launchNewGame(mode = _uiState.value.selectedMode)
    }

    fun startGame(mode: GameMode) {
        trackGameStart(mode)
        launchNewGame(mode = mode)
    }

    fun selectMode(mode: GameMode) {
        val bestScores = _uiState.value.bestScoresByMode
        _uiState.update {
            it.copy(
                selectedMode = mode,
                bestScore = bestScores[mode] ?: 0,
                isNewBestScore = false
            )
        }
    }

    fun onTargetTapped(targetId: Long) {
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        val tappedTarget = currentState.targets.firstOrNull { it.id == targetId } ?: return
        if (tappedTarget.role != GameTargetRole.Correct) {
            loseLife(countAttempt = true)
            return
        }

        val nextCombo = calculateNextCombo(currentState.combo)
        val updatedState = _uiState.updateAndGet {
            val newScore = it.score + 1
            val newDifficulty = calculateDifficultyLevel(newScore)
            val updatedDailyChallenge = advanceDailyChallengeForHit(
                state = it.dailyChallengeState,
                mode = it.selectedMode,
                score = newScore
            )
            val nextTargets = generateTargets(
                mode = it.selectedMode,
                score = newScore,
                currentTargets = it.targets,
                activeColor = it.activeColor
            )
            it.copy(
                score = newScore,
                bestScore = maxOf(it.bestScore, newScore),
                isNewBestScore = it.isNewBestScore || newScore > it.bestScore,
                difficultyLevel = newDifficulty,
                targetSizeDp = calculateTargetSizeDp(newScore, it.selectedMode),
                targetVisibleDurationMillis = calculateTargetVisibleDurationMillis(newScore, it.selectedMode),
                targetPosition = nextTargets.firstCorrectPosition(),
                targets = nextTargets,
                combo = nextCombo,
                maxCombo = maxOf(it.maxCombo, nextCombo),
                successfulHits = it.successfulHits + 1,
                totalAttempts = it.totalAttempts + 1,
                dailyChallengeState = updatedDailyChallenge,
                targetLifetimeKey = it.targetLifetimeKey + 1
            )
        }
        gamePreferences.saveDailyChallengeState(updatedState.dailyChallengeState)
        startComboResetTimer(expectedCombo = nextCombo)
        startTargetTimeout()
    }

    fun onMissTapped() {
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        loseLife(countAttempt = true)
    }

    fun retryGame() {
        launchNewGame(mode = _uiState.value.selectedMode)
    }

    fun pauseGame() {
        val currentState = _uiState.value
        if (!currentState.hasGameStarted || currentState.isPaused || currentState.isGameOver) return

        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
        movingTargetJob?.cancel()
        colorRuleJob?.cancel()
        comboResetJob?.cancel()
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
            startModeJobs()
        }
    }

    fun goToHome() {
        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
        movingTargetJob?.cancel()
        colorRuleJob?.cancel()
        comboResetJob?.cancel()
        _uiState.value = createInitialState(
            mode = _uiState.value.selectedMode,
            bestScores = _uiState.value.bestScoresByMode
        ).copy(
            bestScore = _uiState.value.bestScore,
            selectedMode = _uiState.value.selectedMode,
            dailyChallengeState = gamePreferences.getDailyChallengeState(),
            progressionState = gamePreferences.getProgressionState(),
            playerProfile = gamePreferences.getPlayerProfile(),
            leaderboardSnapshot = createLeaderboardSnapshot(
                profile = gamePreferences.getPlayerProfile(),
                progression = gamePreferences.getProgressionState(),
                bestScoresByMode = _uiState.value.bestScoresByMode
            ),
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
        movingTargetJob?.cancel()
        colorRuleJob?.cancel()
        comboResetJob?.cancel()
        val nextTargets = generateTargets(
            mode = currentState.selectedMode,
            score = currentState.score,
            currentTargets = currentState.targets,
            activeColor = currentState.activeColor
        )
        _uiState.update {
            it.copy(
                hasGameStarted = true,
                lives = 1,
                timeLeftSeconds = adConfig.rewardedContinueSeconds,
                targetPosition = nextTargets.firstCorrectPosition(),
                targets = nextTargets,
                isPaused = false,
                isResumeGracePeriod = true,
                isGameOver = false,
                gameOverReason = null,
                gameOverReasonRes = null,
                hasUsedRewardContinue = true,
                isRewardContinueReady = false,
                canContinueWithReward = false,
                shouldRequestInterstitialAd = false,
                targetLifetimeKey = it.targetLifetimeKey + 1
            )
        }
        startRewardContinueGracePeriod()
        AdAnalyticsTracker.track("continue_used", adParams("score" to currentState.score))
    }

    fun onDoubleCoinsRewardEarned() {
        val state = _uiState.value
        if (!state.isGameOver || state.isCoinDoubleClaimed || state.baseCoinsThisGame <= 0) return

        val bonusCoins = state.baseCoinsThisGame * (adConfig.doubleCoinMultiplier - 1).coerceAtLeast(1)
        val updatedProgression = state.progressionState.copy(
            coins = state.progressionState.coins + bonusCoins
        )
        _uiState.update {
            it.copy(
                progressionState = updatedProgression,
                earnedCoinsThisGame = it.earnedCoinsThisGame + bonusCoins,
                isCoinDoubleClaimed = true,
                pendingRewardedAction = null
            )
        }
        gamePreferences.saveProgressionState(updatedProgression)
        AdAnalyticsTracker.track(
            eventName = "double_coin_used",
            params = adParams(
                "base_coins" to state.baseCoinsThisGame,
                "multiplier" to adConfig.doubleCoinMultiplier,
                "total_coins" to updatedProgression.coins
            )
        )
    }

    fun claimDailyReward() {
        val state = _uiState.value
        val reward = state.progressionState.dailyReward
        if (!reward.canClaim) return

        val rewardedProgression = applyDailyReward(
            progression = state.progressionState
        )
        _uiState.update { it.copy(progressionState = rewardedProgression) }
        gamePreferences.saveProgressionState(rewardedProgression)
        gamePreferences.saveDailyRewardClaim(streakDay = rewardedProgression.dailyReward.streakDay)
    }

    fun protectDailyRewardStreak() {
        val state = _uiState.value
        val reward = state.progressionState.dailyReward
        if (!reward.canProtectStreak) return

        gamePreferences.protectDailyRewardStreak()
        val refreshedProgression = gamePreferences.getProgressionState()
        _uiState.update {
            it.copy(
                progressionState = it.progressionState.copy(
                    dailyReward = refreshedProgression.dailyReward
                )
            )
        }
    }

    fun updatePlayerName(name: String): Boolean {
        val sanitizedName = sanitizePlayerName(name) ?: return false
        gamePreferences.savePlayerName(sanitizedName)
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.PlayerNameChanged,
            params = Bundle().apply {
                putInt("name_length", sanitizedName.length)
            }
        )
        refreshProfileAndLeaderboard()
        return true
    }

    fun selectPlayerTitle(title: PlayerTitle) {
        gamePreferences.savePlayerTitle(title)
        refreshProfileAndLeaderboard()
    }

    fun selectLeaderboardMode(mode: GameMode) {
        selectedLeaderboardMode = mode
        refreshProfileAndLeaderboard()
    }

    fun selectLeaderboardPeriod(period: LeaderboardPeriod) {
        selectedLeaderboardPeriod = period
        refreshProfileAndLeaderboard()
    }

    fun refreshLeaderboard() {
        leaderboardRefreshTick += 1
        refreshProfileAndLeaderboard(showLoading = true)
    }

    fun claimAchievementReward(achievementId: String) {
        val state = _uiState.value
        val achievement = state.progressionState.achievements.firstOrNull { it.id == achievementId }
        if (achievement == null || !achievement.unlocked || achievement.claimed) return

        val updatedAchievements = state.progressionState.achievements.map {
            if (it.id == achievementId) it.copy(claimed = true) else it
        }
        val updatedXp = state.progressionState.xp + achievement.rewardXp
        val updatedProgression = state.progressionState.copy(
            coins = state.progressionState.coins + achievement.rewardCoins,
            xp = updatedXp,
            level = calculateLevel(updatedXp),
            achievements = updatedAchievements,
            lastLevelUp = calculateLevel(updatedXp).takeIf { it > state.progressionState.level },
            latestUnlockedAchievementIds = emptyList()
        )
        _uiState.update { it.copy(progressionState = updatedProgression) }
        gamePreferences.saveProgressionState(updatedProgression)
    }

    fun buyTheme(theme: PlayerTheme) {
        val state = _uiState.value
        val progression = state.progressionState
        if (theme in progression.unlockedThemes || progression.coins < theme.coinPrice) return

        val updatedProgression = progression.copy(
            coins = progression.coins - theme.coinPrice,
            selectedTheme = theme,
            unlockedThemes = progression.unlockedThemes + theme
        )
        _uiState.update {
            it.copy(
                progressionState = updatedProgression,
                leaderboardSnapshot = createLeaderboardSnapshot(
                    profile = it.playerProfile,
                    progression = updatedProgression,
                    bestScoresByMode = it.bestScoresByMode
                )
            )
        }
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.ThemePurchased,
            params = Bundle().apply {
                putString("theme", theme.storageKey)
                putInt("price", theme.coinPrice)
            }
        )
        gamePreferences.saveProgressionState(updatedProgression)
    }

    fun tryThemeForOneGame(theme: PlayerTheme) {
        val state = _uiState.value
        if (theme in state.progressionState.unlockedThemes) return

        val updatedProgression = state.progressionState.copy(
            trialTheme = theme,
            trialGamesRemaining = 1
        )
        _uiState.update { it.copy(progressionState = updatedProgression) }
    }

    fun selectTheme(theme: PlayerTheme) {
        val state = _uiState.value
        if (theme !in state.progressionState.unlockedThemes) return

        val updatedProgression = state.progressionState.copy(selectedTheme = theme)
        _uiState.update {
            it.copy(
                progressionState = updatedProgression,
                leaderboardSnapshot = createLeaderboardSnapshot(
                    profile = it.playerProfile,
                    progression = updatedProgression,
                    bestScoresByMode = it.bestScoresByMode
                )
            )
        }
        gamePreferences.saveProgressionState(updatedProgression)
    }

    fun onInterstitialAdRequestHandled(wasShown: Boolean) {
        if (wasShown) {
            lastInterstitialShownElapsedMillis = SystemClock.elapsedRealtime()
        }
        _uiState.update { it.copy(shouldRequestInterstitialAd = false) }
    }

    private fun launchNewGame(mode: GameMode) {
        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
        movingTargetJob?.cancel()
        colorRuleJob?.cancel()
        comboResetJob?.cancel()
        val initialState = createInitialState(
            mode = mode,
            bestScores = _uiState.value.bestScoresByMode
        )
        val currentProgression = gamePreferences.getProgressionState().copy(
            trialTheme = _uiState.value.progressionState.trialTheme,
            trialGamesRemaining = _uiState.value.progressionState.trialGamesRemaining
        )
        lastHitElapsedMillis = 0L
        gameStartedElapsedMillis = SystemClock.elapsedRealtime()
        _uiState.value = initialState.copy(
            bestScore = _uiState.value.bestScore,
            dailyChallengeState = gamePreferences.getDailyChallengeState(),
            progressionState = currentProgression,
            earnedCoinsThisGame = 0,
            baseCoinsThisGame = 0,
            isCoinDoubleClaimed = false,
            hasGameStarted = true,
            isNewBestScore = false
        )
        startTimer()
        startTargetTimeout()
        startModeJobs()
    }

    private fun observeBestScore() {
        viewModelScope.launch {
            gamePreferences.bestScoresFlow.collect { bestScores ->
                _uiState.update {
                    val profile = gamePreferences.getPlayerProfile()
                    it.copy(
                        bestScoresByMode = bestScores,
                        bestScore = bestScores[it.selectedMode] ?: 0,
                        playerProfile = profile,
                        leaderboardSnapshot = createLeaderboardSnapshot(
                            profile = profile,
                            progression = it.progressionState,
                            bestScoresByMode = bestScores
                        )
                    )
                }
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
                        reasonRes = REASON_TIME_UP
                    )
                    break
                } else {
                    _uiState.update { it.copy(timeLeftSeconds = newTime) }
                }
            }
        }
    }

    private fun endGame(countAttempt: Boolean = false) {
        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
        movingTargetJob?.cancel()
        colorRuleJob?.cancel()
        comboResetJob?.cancel()
        if (countAttempt) {
            lastHitElapsedMillis = 0L
            _uiState.update {
                it.copy(
                    totalAttempts = it.totalAttempts + 1,
                    combo = 0
                )
            }
        }
        finishGame(
            lives = 0,
            reasonRes = REASON_NO_LIVES
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
            lastHitElapsedMillis = 0L
            val nextTargets = generateTargets(
                mode = it.selectedMode,
                score = it.score,
                currentTargets = it.targets,
                activeColor = it.activeColor
            )
            it.copy(
                lives = remainingLives,
                combo = 0,
                targetPosition = nextTargets.firstCorrectPosition(),
                targets = nextTargets,
                targetLifetimeKey = it.targetLifetimeKey + 1
            )
        }
        startTargetTimeout()
        startModeJobs()
    }

    private fun finishGame(
        lives: Int = _uiState.value.lives,
        timeLeftSeconds: Int = _uiState.value.timeLeftSeconds,
        reason: String? = _uiState.value.gameOverReason,
        reasonRes: Int? = _uiState.value.gameOverReasonRes
    ) {
        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
        movingTargetJob?.cancel()
        colorRuleJob?.cancel()
        comboResetJob?.cancel()
        completedGameCount += 1
        val gameDurationMillis = (SystemClock.elapsedRealtime() - gameStartedElapsedMillis).coerceAtLeast(0L)
        val shouldRequestInterstitialAd = shouldRequestInterstitialAfterGame(
            score = _uiState.value.score,
            bestScore = _uiState.value.bestScore,
            isNewBestScore = _uiState.value.isNewBestScore,
            gameDurationMillis = gameDurationMillis
        )
        if (shouldRequestInterstitialAd) {
            nextInterstitialGameCount = completedGameCount + randomInterstitialInterval()
        }
        val scoreMode = _uiState.value.selectedMode
        val previousModeBestScore = _uiState.value.bestScoresByMode[scoreMode] ?: 0

        val finalState = _uiState.updateAndGet {
            val currentModeBest = it.bestScoresByMode[it.selectedMode] ?: 0
            val isNewModeBest = it.score > currentModeBest
            val earnedCoins = calculateEarnedCoins(
                score = it.score,
                maxCombo = it.maxCombo,
                isNewBestScore = isNewModeBest
            )
            val progressionFromStorage = gamePreferences.getProgressionState()
            val updatedProgression = updateProgressionAfterGame(
                progression = it.progressionState.copy(
                    dailyReward = progressionFromStorage.dailyReward,
                    achievements = progressionFromStorage.achievements,
                    weeklyChallenge = progressionFromStorage.weeklyChallenge
                ),
                mode = it.selectedMode,
                score = it.score,
                hits = it.successfulHits,
                maxCombo = it.maxCombo,
                earnedCoins = earnedCoins,
                isNewBestScore = isNewModeBest
            )
            val trialAwareProgression = if (updatedProgression.trialGamesRemaining > 0) {
                val remaining = updatedProgression.trialGamesRemaining - 1
                updatedProgression.copy(
                    trialGamesRemaining = remaining,
                    trialTheme = updatedProgression.trialTheme.takeIf { remaining > 0 }
                )
            } else {
                updatedProgression
            }
            val updatedBestScores = if (isNewModeBest) {
                it.bestScoresByMode + (it.selectedMode to it.score)
            } else {
                it.bestScoresByMode
            }
            val updatedDailyChallenge = advanceDailyChallengeForGameCompleted(it.dailyChallengeState)
            val updatedProfile = it.playerProfile.copy(
                weeklyBestScore = maxOf(it.playerProfile.weeklyBestScore, it.score),
                weeklyBestScoresByMode = it.playerProfile.weeklyBestScoresByMode +
                    (it.selectedMode to maxOf(it.playerProfile.weeklyBestScoresByMode[it.selectedMode] ?: 0, it.score))
            )
            val updatedLeaderboard = createLeaderboardSnapshot(
                profile = updatedProfile,
                progression = trialAwareProgression,
                bestScoresByMode = updatedBestScores
            )
            it.copy(
                lives = lives,
                timeLeftSeconds = timeLeftSeconds,
                dailyChallengeState = updatedDailyChallenge,
                hasGameStarted = true,
                isPaused = false,
                isResumeGracePeriod = false,
                isGameOver = true,
                gameOverReason = reason,
                gameOverReasonRes = reasonRes,
                bestScore = maxOf(currentModeBest, it.score),
                bestScoresByMode = updatedBestScores,
                isNewBestScore = it.isNewBestScore || isNewModeBest,
                progressionState = trialAwareProgression,
                playerProfile = updatedProfile,
                leaderboardSnapshot = updatedLeaderboard,
                baseCoinsThisGame = earnedCoins,
                earnedCoinsThisGame = earnedCoins,
                isCoinDoubleClaimed = false,
                canContinueWithReward = !it.hasUsedRewardContinue,
                isRewardContinueReady = false,
                shouldRequestInterstitialAd = shouldRequestInterstitialAd
            )
        }

        viewModelScope.launch {
            gamePreferences.saveBestScore(finalState.selectedMode, finalState.score)
        }
        gamePreferences.saveWeeklyBestScore(finalState.selectedMode, finalState.score)
        gamePreferences.saveDailyChallengeState(finalState.dailyChallengeState)
        gamePreferences.saveProgressionState(finalState.progressionState)
        if (finalState.score > previousModeBestScore) {
            viewModelScope.launch {
                val uploaded = leaderboardRepository.uploadScore(
                    playerName = finalState.playerProfile.name,
                    score = finalState.score,
                    level = finalState.progressionState.level,
                    selectedTheme = finalState.progressionState.activeTheme,
                    mode = finalState.selectedMode
                )
                if (uploaded) {
                    loadRemoteLeaderboard(showLoading = false)
                }
            }
        }
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.GameOver,
            params = Bundle().apply {
                putString("mode", finalState.selectedMode.storageKey)
                putInt("score", finalState.score)
                putInt("max_combo", finalState.maxCombo)
                putBoolean("new_best", finalState.isNewBestScore)
            }
        )
    }

    private fun trackGameStart(mode: GameMode) {
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.GameStart,
            params = Bundle().apply {
                putString("mode", mode.storageKey)
            }
        )
    }

    private fun createInitialState(
        mode: GameMode = GameMode.Classic,
        bestScores: Map<GameMode, Int> = GameMode.entries.associateWith { 0 }
    ): GameUiState {
        val activeColor = if (mode == GameMode.ColorReflex) {
            randomTargetColor()
        } else {
            ReflexTargetColor.Red
        }
        val targets = generateTargets(
            mode = mode,
            score = 0,
            activeColor = activeColor
        )
        val progression = gamePreferences.getProgressionState()
        val playerProfile = gamePreferences.getPlayerProfile()
        return GameUiState(
            lives = INITIAL_LIVES,
            timeLeftSeconds = INITIAL_TIME_SECONDS,
            difficultyLevel = calculateDifficultyLevel(score = 0),
            targetSizeDp = calculateTargetSizeDp(score = 0, mode = mode),
            targetVisibleDurationMillis = calculateTargetVisibleDurationMillis(score = 0, mode = mode),
            targetPosition = targets.firstCorrectPosition(),
            selectedMode = mode,
            bestScore = bestScores[mode] ?: 0,
            bestScoresByMode = bestScores,
            targets = targets,
            activeColor = activeColor,
            dailyChallengeState = gamePreferences.getDailyChallengeState(),
            progressionState = progression,
            playerProfile = playerProfile,
            leaderboardSnapshot = createLeaderboardSnapshot(
                profile = playerProfile,
                progression = progression,
                bestScoresByMode = bestScores
            )
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
                startModeJobs()
            }
        }
    }

    private fun calculateDifficultyLevel(score: Int): Int {
        return (score / 5 + 1).coerceIn(1, 8)
    }

    private fun calculateTargetSizeDp(score: Int, mode: GameMode): Int {
        val modeExtraReduction = when (mode) {
            GameMode.Classic -> 0
            GameMode.MovingTarget -> 2
            GameMode.FakeTarget -> 4
            GameMode.ColorReflex -> 2
        }
        val sizeReduction = (score / 3) * 4 + modeExtraReduction
        return (INITIAL_TARGET_SIZE_DP - sizeReduction).coerceAtLeast(MIN_TARGET_SIZE_DP)
    }

    private fun calculateTargetVisibleDurationMillis(score: Int, mode: GameMode): Long {
        val modeExtraReduction = when (mode) {
            GameMode.Classic -> 0L
            GameMode.MovingTarget -> 80L
            GameMode.FakeTarget -> 40L
            GameMode.ColorReflex -> 60L
        }
        val durationReduction = (score / 2) * 80L + modeExtraReduction
        return (INITIAL_TARGET_VISIBLE_DURATION_MS - durationReduction)
            .coerceAtLeast(MIN_TARGET_VISIBLE_DURATION_MS)
    }

    private fun loseLife(countAttempt: Boolean = false) {
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        lastHitElapsedMillis = 0L
        val remainingLives = currentState.lives - 1
        if (remainingLives <= 0) {
            endGame(countAttempt = countAttempt)
            return
        }

        _uiState.update {
            val nextTargets = generateTargets(
                mode = it.selectedMode,
                score = it.score,
                currentTargets = it.targets,
                activeColor = it.activeColor
            )
            it.copy(
                lives = remainingLives,
                combo = 0,
                totalAttempts = it.totalAttempts + if (countAttempt) 1 else 0,
                targetPosition = nextTargets.firstCorrectPosition(),
                targets = nextTargets,
                targetLifetimeKey = it.targetLifetimeKey + 1
            )
        }
        startTargetTimeout()
        startModeJobs()
    }

    private fun GameUiState.canAcceptGameplayInput(): Boolean {
        return hasGameStarted &&
            !isPaused &&
            !isResumeGracePeriod &&
            !isGameOver
    }

    private fun startModeJobs() {
        movingTargetJob?.cancel()
        colorRuleJob?.cancel()
        val state = _uiState.value
        if (!state.canAcceptGameplayInput()) return

        if (state.selectedMode == GameMode.MovingTarget) {
            movingTargetJob = viewModelScope.launch {
                while (_uiState.value.canAcceptGameplayInput() && _uiState.value.selectedMode == GameMode.MovingTarget) {
                    delay(calculateMovementIntervalMillis(_uiState.value.score))
                    moveTargets()
                }
            }
        }

        if (state.selectedMode == GameMode.ColorReflex) {
            colorRuleJob = viewModelScope.launch {
                while (_uiState.value.canAcceptGameplayInput() && _uiState.value.selectedMode == GameMode.ColorReflex) {
                    delay(COLOR_RULE_CHANGE_INTERVAL_MS)
                    rotateColorRule()
                }
            }
        }
    }

    private fun calculateNextCombo(currentCombo: Int): Int {
        val now = SystemClock.elapsedRealtime()
        val isComboContinuing = lastHitElapsedMillis > 0L &&
            now - lastHitElapsedMillis <= COMBO_WINDOW_MILLIS
        lastHitElapsedMillis = now
        return if (isComboContinuing) currentCombo + 1 else 1
    }

    private fun startComboResetTimer(expectedCombo: Int) {
        comboResetJob?.cancel()
        comboResetJob = viewModelScope.launch {
            delay(COMBO_WINDOW_MILLIS)
            val latestState = _uiState.value
            if (latestState.canAcceptGameplayInput() && latestState.combo == expectedCombo) {
                lastHitElapsedMillis = 0L
                _uiState.update { it.copy(combo = 0) }
            }
        }
    }

    private fun advanceDailyChallengeForHit(
        state: DailyChallengeState,
        mode: GameMode,
        score: Int
    ): DailyChallengeState {
        if (state.completed) return state

        val nextProgress = when (state.type) {
            DailyChallenge.Score20 -> score.coerceAtMost(state.target)
            DailyChallenge.Play3Games -> state.progress
            DailyChallenge.FakeTarget10 -> if (mode == GameMode.FakeTarget) {
                score.coerceAtMost(state.target)
            } else {
                state.progress
            }
        }
        return state.copy(
            progress = nextProgress,
            completed = nextProgress >= state.target
        )
    }

    private fun advanceDailyChallengeForGameCompleted(
        state: DailyChallengeState
    ): DailyChallengeState {
        if (state.completed || state.type != DailyChallenge.Play3Games) return state

        val nextProgress = (state.progress + 1).coerceAtMost(state.target)
        return state.copy(
            progress = nextProgress,
            completed = nextProgress >= state.target
        )
    }

    private fun calculateEarnedCoins(
        score: Int,
        maxCombo: Int,
        isNewBestScore: Boolean
    ): Int {
        val scoreCoins = score * 4
        val comboBonus = when {
            maxCombo >= 20 -> 80
            maxCombo >= 10 -> 45
            maxCombo >= 5 -> 25
            maxCombo >= 2 -> 10
            else -> 0
        }
        val recordBonus = if (isNewBestScore) 60 else 0
        return (scoreCoins + comboBonus + recordBonus).coerceAtLeast(if (score > 0) 8 else 0)
    }

    private fun calculateEarnedXp(
        score: Int,
        hits: Int,
        maxCombo: Int,
        isNewBestScore: Boolean
    ): Int {
        val comboXp = when {
            maxCombo >= 10 -> 45
            maxCombo >= 5 -> 25
            else -> 0
        }
        return 20 + (score * 3) + hits + comboXp + if (isNewBestScore) 60 else 0
    }

    private fun updateProgressionAfterGame(
        progression: ProgressionState,
        mode: GameMode,
        score: Int,
        hits: Int,
        maxCombo: Int,
        earnedCoins: Int,
        isNewBestScore: Boolean
    ): ProgressionState {
        val previousAchievements = progression.achievements
        val nextXp = progression.xp + calculateEarnedXp(score, hits, maxCombo, isNewBestScore)
        val nextTotalGames = progression.totalGames + 1
        val nextTotalHits = progression.totalHits + hits
        val nextMaxCombo = maxOf(progression.lifetimeMaxCombo, maxCombo)
        val updatedAchievements = previousAchievements.map { achievement ->
            val nextProgress = when (achievement.type) {
                AchievementType.PlayGames -> nextTotalGames
                AchievementType.ScoreInSingleGame -> maxOf(achievement.progress, score)
                AchievementType.HitTargets -> nextTotalHits
                AchievementType.ReachCombo -> nextMaxCombo
                AchievementType.FakeTargetScore -> if (mode == GameMode.FakeTarget) {
                    maxOf(achievement.progress, score)
                } else {
                    achievement.progress
                }
                AchievementType.ColorReflexScore -> if (mode == GameMode.ColorReflex) {
                    maxOf(achievement.progress, score)
                } else {
                    achievement.progress
                }
                AchievementType.BreakRecord -> if (isNewBestScore) 1 else achievement.progress
            }.coerceAtMost(achievement.target)
            achievement.copy(
                progress = nextProgress,
                unlocked = nextProgress >= achievement.target
            )
        }
        val newlyUnlocked = updatedAchievements.filter { updated ->
            updated.unlocked && previousAchievements.none { it.id == updated.id && it.unlocked }
        }.map { it.id }
        val nextLevel = calculateLevel(nextXp)
        val weeklyProgress = (progression.weeklyChallenge.progress + score)
            .coerceAtMost(progression.weeklyChallenge.target)
        val weeklyChallenge = progression.weeklyChallenge.copy(
            progress = weeklyProgress,
            completed = weeklyProgress >= progression.weeklyChallenge.target
        )

        return progression.copy(
            coins = progression.coins + earnedCoins,
            xp = nextXp,
            level = nextLevel,
            totalGames = nextTotalGames,
            totalHits = nextTotalHits,
            lifetimeMaxCombo = nextMaxCombo,
            achievements = updatedAchievements,
            weeklyChallenge = weeklyChallenge,
            latestUnlockedAchievementIds = newlyUnlocked,
            lastLevelUp = nextLevel.takeIf { it > progression.level }
        )
    }

    private fun calculateLevel(xp: Int): Int {
        return (xp / 250 + 1).coerceAtLeast(1)
    }

    private fun refreshProfileAndLeaderboard(showLoading: Boolean = false) {
        val profile = gamePreferences.getPlayerProfile()
        _uiState.update {
            it.copy(
                playerProfile = profile,
                leaderboardSnapshot = createLeaderboardSnapshot(
                    profile = profile,
                    progression = it.progressionState,
                    bestScoresByMode = it.bestScoresByMode
                ).copy(
                    isLoading = showLoading,
                    statusMessageRes = if (showLoading) R.string.leaderboard_loading else it.leaderboardSnapshot.statusMessageRes
                )
            )
        }
        loadRemoteLeaderboard(showLoading = showLoading)
    }

    private fun createLeaderboardSnapshot(
        profile: PlayerProfile,
        progression: ProgressionState,
        bestScoresByMode: Map<GameMode, Int>
    ): LeaderboardSnapshot {
        val mode = selectedLeaderboardMode
        val leaderboardScore = when (selectedLeaderboardPeriod) {
            LeaderboardPeriod.Weekly -> profile.weeklyBestScoresByMode[mode] ?: 0
            LeaderboardPeriod.AllTime -> bestScoresByMode[mode] ?: 0
        }
        return leaderboardRepository.getLocalLeaderboard(
            playerName = profile.name,
            playerScore = leaderboardScore,
            playerTheme = progression.activeTheme,
            playerRankTier = rankFor(score = leaderboardScore, level = progression.level),
            selectedMode = mode,
            selectedPeriod = selectedLeaderboardPeriod,
            refreshTick = leaderboardRefreshTick
        )
    }

    private fun loadRemoteLeaderboard(showLoading: Boolean) {
        leaderboardRefreshJob?.cancel()
        leaderboardRefreshJob = viewModelScope.launch {
            val state = _uiState.value
            val profile = gamePreferences.getPlayerProfile()
            val mode = selectedLeaderboardMode
            val leaderboardScore = when (selectedLeaderboardPeriod) {
                LeaderboardPeriod.Weekly -> profile.weeklyBestScoresByMode[mode] ?: 0
                LeaderboardPeriod.AllTime -> state.bestScoresByMode[mode] ?: 0
            }
            val snapshot = leaderboardRepository.refreshLeaderboard(
                playerName = profile.name,
                playerScore = leaderboardScore,
                playerTheme = state.progressionState.activeTheme,
                playerRankTier = rankFor(score = leaderboardScore, level = state.progressionState.level),
                playerLevel = state.progressionState.level,
                selectedMode = mode,
                selectedPeriod = selectedLeaderboardPeriod,
                refreshTick = leaderboardRefreshTick
            ).copy(isLoading = false)

            _uiState.update {
                it.copy(
                    playerProfile = profile,
                    leaderboardSnapshot = snapshot
                )
            }
        }
    }

    private fun sanitizePlayerName(name: String): String? {
        val cleanedName = name.trim().take(12)
        if (cleanedName.isBlank()) return null

        val loweredName = cleanedName.lowercase()
        val blockedTerms = listOf("amk", "aq", "oros", "sik", "fuck", "shit")
        if (blockedTerms.any { loweredName.contains(it) }) return null

        return cleanedName
    }

    private fun shouldRequestInterstitialAfterGame(
        score: Int,
        bestScore: Int,
        isNewBestScore: Boolean,
        gameDurationMillis: Long
    ): Boolean {
        val now = SystemClock.elapsedRealtime()
        val cooldownPassed = lastInterstitialShownElapsedMillis == 0L ||
            now - lastInterstitialShownElapsedMillis >= adConfig.interstitialCooldownMillis
        val isShortGame = gameDurationMillis < adConfig.shortGameThresholdMillis ||
            score <= adConfig.shortGameScoreThreshold
        val isHighValueRun = isNewBestScore ||
            (bestScore > 0 && score >= (bestScore * adConfig.highScoreDelayRatio).toInt())

        return completedGameCount > adConfig.firstInterstitialFreeGames &&
            completedGameCount >= nextInterstitialGameCount &&
            cooldownPassed &&
            !isShortGame &&
            !isHighValueRun
    }

    private fun applyDailyReward(
        progression: ProgressionState
    ): ProgressionState {
        val reward = progression.dailyReward
        if (reward.claimedToday) return progression
        val nextStreakDay = reward.streakDay + 1
        val nextDayInCycle = ((nextStreakDay - 1) % DailyRewardCoinPlan.size) + 1

        return progression.copy(
            coins = progression.coins + reward.rewardCoins,
            xp = progression.xp + if (reward.isSuperReward) 75 else 25,
            level = calculateLevel(progression.xp + if (reward.isSuperReward) 75 else 25),
            dailyReward = reward.copy(
                canClaim = false,
                claimedToday = true,
                canProtectStreak = false,
                isStreakAtRisk = false,
                nextRewardCoins = DailyRewardCoinPlan[nextDayInCycle - 1],
                loyalBadgeUnlocked = reward.loyalBadgeUnlocked || reward.streakDay >= 30
            )
        )
    }

    private fun moveTargets() {
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        _uiState.update {
            val movedTargets = it.targets.map { target ->
                target.copy(
                    position = generateRandomTargetPosition(
                        currentX = target.position.xFraction,
                        currentY = target.position.yFraction
                    )
                )
            }
            it.copy(
                targets = movedTargets,
                targetPosition = movedTargets.firstCorrectPosition()
            )
        }
    }

    private fun rotateColorRule() {
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        val newColor = nextColorRule(currentState.activeColor)
        _uiState.update {
            val nextTargets = generateTargets(
                mode = GameMode.ColorReflex,
                score = it.score,
                currentTargets = it.targets,
                activeColor = newColor
            )
            it.copy(
                activeColor = newColor,
                targets = nextTargets,
                targetPosition = nextTargets.firstCorrectPosition(),
                targetLifetimeKey = it.targetLifetimeKey + 1
            )
        }
        startTargetTimeout()
        startModeJobs()
    }

    private fun generateTargets(
        mode: GameMode,
        score: Int,
        currentTargets: List<GameTarget> = emptyList(),
        activeColor: ReflexTargetColor = ReflexTargetColor.Red
    ): List<GameTarget> {
        val currentCorrect = currentTargets.firstOrNull { it.role == GameTargetRole.Correct }?.position
        val usedPositions = mutableListOf<TargetPosition>()
        val correctPosition = generateRandomTargetPosition(
            currentX = currentCorrect?.xFraction,
            currentY = currentCorrect?.yFraction
        )
        usedPositions += correctPosition
        return when (mode) {
            GameMode.Classic,
            GameMode.MovingTarget -> listOf(
                GameTarget(
                    id = nextTargetId++,
                    position = correctPosition,
                    role = GameTargetRole.Correct,
                    color = ReflexTargetColor.Red
                )
            )
            GameMode.FakeTarget -> {
                val fakeCount = if (score >= 8) 2 else 1
                buildList {
                    add(
                        GameTarget(
                            id = nextTargetId++,
                            position = correctPosition,
                            role = GameTargetRole.Correct,
                            color = ReflexTargetColor.Red
                        )
                    )
                    repeat(fakeCount) {
                        add(
                            GameTarget(
                                id = nextTargetId++,
                                position = generateRandomTargetPositionAwayFrom(usedPositions),
                                role = GameTargetRole.Fake,
                                color = ReflexTargetColor.Red
                            ).also { usedPositions += it.position }
                        )
                    }
                }
            }
            GameMode.ColorReflex -> {
                val wrongColors = ReflexTargetColor.entries
                    .filterNot { it == activeColor }
                    .sortedBy { it.ordinal }
                    .take(if (score >= 12) 3 else 2)
                buildList {
                    wrongColors.forEach { wrongColor ->
                        add(
                            GameTarget(
                                id = nextTargetId++,
                                position = generateRandomTargetPositionAwayFrom(usedPositions),
                                role = GameTargetRole.WrongColor,
                                color = wrongColor
                            ).also { usedPositions += it.position }
                        )
                    }
                    add(
                        GameTarget(
                            id = nextTargetId++,
                            position = correctPosition,
                            role = GameTargetRole.Correct,
                            color = activeColor
                        )
                    )
                }
            }
        }
    }

    private fun nextColorRule(currentColor: ReflexTargetColor): ReflexTargetColor {
        val colorOrder = listOf(
            ReflexTargetColor.Red,
            ReflexTargetColor.Blue,
            ReflexTargetColor.Teal,
            ReflexTargetColor.Gold
        )
        val currentIndex = colorOrder.indexOf(currentColor).coerceAtLeast(0)
        return colorOrder[(currentIndex + 1) % colorOrder.size]
    }

    private fun List<GameTarget>.firstCorrectPosition(): TargetPosition {
        return firstOrNull { it.role == GameTargetRole.Correct }?.position ?: TargetPosition()
    }

    private fun randomTargetColor(except: ReflexTargetColor? = null): ReflexTargetColor {
        val colors = ReflexTargetColor.entries.filterNot { it == except }
        return colors.random()
    }

    private fun calculateMovementIntervalMillis(score: Int): Long {
        return (900L - (score / 2) * 70L).coerceAtLeast(320L)
    }

    private fun randomInterstitialInterval(): Int {
        return Random.nextInt(
            from = adConfig.interstitialMinGameInterval,
            until = adConfig.interstitialMaxGameInterval + 1
        )
    }

    private fun generateRandomTargetPositionAwayFrom(
        existingPositions: List<TargetPosition>
    ): TargetPosition {
        repeat(20) {
            val candidate = generateRandomTargetPosition()
            val isFarEnough = existingPositions.all { existing ->
                kotlin.math.abs(candidate.xFraction - existing.xFraction) > 0.16f ||
                    kotlin.math.abs(candidate.yFraction - existing.yFraction) > 0.16f
            }
            if (isFarEnough) return candidate
        }
        return generateRandomTargetPosition()
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
        movingTargetJob?.cancel()
        colorRuleJob?.cancel()
        comboResetJob?.cancel()
    }
}
