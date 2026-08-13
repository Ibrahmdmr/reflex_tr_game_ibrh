package com.reflex.tr.game.ibrh.ui.game

import android.os.Bundle
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.reflex.tr.game.ibrh.BuildConfig
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.AdAnalyticsTracker
import com.reflex.tr.game.ibrh.ads.AdConfig
import com.reflex.tr.game.ibrh.ads.adParams
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

/**
 * Collaborators are injected rather than constructed inline so the game logic can be unit tested
 * against fakes. Production wiring lives in [GameViewModel.Factory]; obtain the view model with
 * `viewModel(factory = GameViewModel.Factory)`.
 */
class GameViewModel internal constructor(
    private val gamePreferences: GamePreferences,
    private val leaderboardRepository: LeaderboardRepository,
    private val targetEngine: GameTargetEngine,
    private val adConfig: AdConfig,
    private val defaultPlayerName: () -> String
) : ViewModel() {

    companion object {
        /** Production wiring for [GameViewModel]. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    ?: error("APPLICATION_KEY is missing; GameViewModel needs an Application.")
                val preferences = GamePreferences(application)
                GameViewModel(
                    gamePreferences = preferences,
                    leaderboardRepository = FirestoreLeaderboardRepository(),
                    targetEngine = GameTargetEngine(),
                    adConfig = AdConfig.Default,
                    // The app overrides the system locale, so the fallback name must be resolved
                    // against the in-app language rather than through a plain getString().
                    defaultPlayerName = {
                        application.createHomeLanguageContext(preferences.currentLanguage)
                            .getString(R.string.profile_default_player_name)
                    }
                )
            }
        }

        private const val INITIAL_LIVES = 3
        private const val REWARD_CONTINUE_GRACE_MILLIS = 2_000L
        private const val REWARDED_CALLBACK_DEDUPE_MILLIS = 2_000L
        private const val COMBO_WINDOW_MILLIS = 1_250L
        private const val PERFECT_TIMING_THRESHOLD_MS = 300L
        private const val GREAT_TIMING_THRESHOLD_MS = 700L
        private const val PERFECT_TIMING_BONUS_COINS = 1
        private const val FLAWLESS_STREAK_5_BONUS_COINS = 10
        private const val FLAWLESS_STREAK_10_BONUS_COINS = 25
        private const val FLAWLESS_STREAK_20_BONUS_COINS = 75
        private const val BOSS_ROUND_DURATION_SECONDS = 5
        private const val BOSS_ROUND_HIT_SCORE_BONUS = 1
        private const val BOSS_ROUND_HIT_COIN_BONUS = 5
        private const val BOSS_ROUND_SPEED_PERCENT = 65
        private const val BOSS_ROUND_MIN_VISIBLE_DURATION_MILLIS = 850L
        private val BOSS_ROUND_SCORE_THRESHOLDS = setOf(15, 30, 50)
        private const val ULTRA_MOMENT_DURATION_SECONDS = 7
        private const val ULTRA_MOMENT_HIT_COIN_BONUS = 1
        private val ULTRA_MOMENT_COMBO_THRESHOLDS = setOf(10, 20)
        private const val LEVEL_UP_COIN_BONUS = 50
        private const val REWARDED_AD_XP_REWARD = 20
        private const val DAILY_CHALLENGE_XP_REWARD = 60
        private const val INVITE_SHARE_REWARD_COINS = 100
        private const val SEASON_XP_GAME_PLAYED = 35
        private const val SEASON_XP_CHALLENGE_COMPLETED = 90
        private const val SEASON_XP_DAILY_STREAK = 70
        private const val SEASON_XP_ACHIEVEMENT_CLAIM = 120
        private const val SEASON_XP_REWARDED_AD = 30
        private val REASON_TIME_UP = R.string.game_over_reason_time_up
        private val REASON_NO_LIVES = R.string.game_over_reason_no_lives
    }

    private var selectedLeaderboardMode = GameMode.Classic
    private var selectedLeaderboardPeriod = LeaderboardPeriod.AllTime
    private var leaderboardRefreshTick = 0
    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<GameUiState> = combine(
        _uiState,
        gamePreferences.storePreviewModeFlow
    ) { state, previewEnabled ->
        if (BuildConfig.DEBUG && previewEnabled) {
            storePreviewUiState(state)
        } else {
            state.copy(isStorePreviewMode = false)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = if (BuildConfig.DEBUG && gamePreferences.isStorePreviewModeEnabled()) {
            storePreviewUiState(_uiState.value)
        } else {
            _uiState.value.copy(isStorePreviewMode = false)
        }
    )

    private var timerJob: Job? = null
    private var targetTimeoutJob: Job? = null
    private var rewardContinueGraceJob: Job? = null
    private var movingTargetJob: Job? = null
    private var colorRuleJob: Job? = null
    private var comboResetJob: Job? = null
    private var bossRoundJob: Job? = null
    private var bossRoundResultClearJob: Job? = null
    private var ultraMomentJob: Job? = null
    private var ultraMomentResultClearJob: Job? = null
    private var leaderboardRefreshJob: Job? = null
    private var bonusHourJob: Job? = null
    private var completedGameCount = 0
    private var nextInterstitialGameCount = randomInterstitialInterval()
    private var lastHitElapsedMillis = 0L
    private var gameStartedElapsedMillis = 0L
    private var lastInterstitialShownElapsedMillis = 0L
    private var lastRewardedAdElapsedMillis = 0L
    private var lastRewardedGrantAction: RewardedAction? = null
    private var lastRewardedGrantElapsedMillis = 0L
    private var targetSpawnElapsedMillis = 0L
    private var targetSpawnLifetimeKey = -1

    init {
        observeBestScore()
        startBonusHourTicker()
        refreshProfileAndLeaderboard()
    }

    fun setStorePreviewMode(enabled: Boolean) {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch {
            gamePreferences.saveStorePreviewModeEnabled(enabled)
        }
    }

    fun markModeTipShown(mode: GameMode) {
        gamePreferences.markModeTipShown(mode)
        _uiState.update { state ->
            state.copy(
                shownModeTips = state.shownModeTips + mode,
                isPaused = if (
                    state.hasGameStarted &&
                    !state.isGameOver &&
                    state.selectedMode == mode &&
                    mode !in state.shownModeTips
                ) {
                    false
                } else {
                    state.isPaused
                }
            )
        }
        val currentState = _uiState.value
        if (currentState.hasGameStarted && !currentState.isPaused && !currentState.isGameOver) {
            startTimer()
            startTargetTimeout()
            startModeJobs()
        }
    }

    fun resetModeTips() {
        gamePreferences.resetModeTips()
        _uiState.update { it.copy(shownModeTips = emptySet()) }
    }

    fun startGame() {
        if (isStorePreviewModeActive()) return
        val refreshedState = refreshDailyFeaturedMode()
        trackGameStart(refreshedState.selectedMode)
        launchNewGame(mode = refreshedState.selectedMode)
    }

    fun startGame(mode: GameMode) {
        if (isStorePreviewModeActive()) return
        refreshDailyFeaturedMode()
        trackGameStart(mode)
        launchNewGame(mode = mode)
    }

    fun startGameWithCoinBoost(boost: GameBoost): Boolean {
        if (isStorePreviewModeActive()) return false
        val state = _uiState.value
        if (state.progressionState.coins < boost.coinPrice) return false

        val updatedProgression = state.progressionState.copy(
            coins = state.progressionState.coins - boost.coinPrice,
            totalCoinsSpent = addSpentCoins(state.progressionState.totalCoinsSpent, boost.coinPrice)
        )
        saveProgressionAndUpdateState(updatedProgression)
        val refreshedState = refreshDailyFeaturedMode()
        trackGameStart(refreshedState.selectedMode)
        launchNewGame(mode = refreshedState.selectedMode, boost = boost)
        return true
    }

    fun startGameWithRewardedBoost(boost: GameBoost) {
        if (isStorePreviewModeActive()) return
        if (!tryConsumeRewardedCallback(RewardedAction.Boost)) return
        val updatedProgression = recordRewardedAdWatched(_uiState.value.progressionState)
        saveRewardedProgressionAndLog(updatedProgression)
        val refreshedState = refreshDailyFeaturedMode()
        trackGameStart(refreshedState.selectedMode)
        launchNewGame(mode = refreshedState.selectedMode, boost = boost)
    }

    fun startGameWithPowerUp(powerUp: GamePowerUp): Boolean {
        if (isStorePreviewModeActive()) return false
        val state = _uiState.value
        if (state.progressionState.coins < powerUp.coinPrice) return false

        val updatedProgression = state.progressionState.copy(
            coins = (state.progressionState.coins - powerUp.coinPrice).coerceAtLeast(0),
            totalCoinsSpent = addSpentCoins(state.progressionState.totalCoinsSpent, powerUp.coinPrice)
        )
        saveProgressionAndUpdateState(updatedProgression)
        val refreshedState = refreshDailyFeaturedMode()
        trackGameStart(refreshedState.selectedMode)
        launchNewGame(mode = refreshedState.selectedMode, powerUp = powerUp)
        return true
    }

    fun selectMode(mode: GameMode) {
        if (isStorePreviewModeActive()) return
        val bestScores = _uiState.value.bestScoresByMode
        _uiState.update {
            it.copy(
                selectedMode = mode,
                bestScore = bestScores[mode] ?: 0,
                isNewBestScore = false
            )
        }
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.ModeSelected,
            params = Bundle().apply {
                putString(FirebaseParam.ModeName.key, mode.storageKey)
            }
        )
    }

    fun onTargetTapped(targetId: Long) {
        if (isStorePreviewModeActive()) return
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        val tappedTarget = currentState.targets.firstOrNull { it.id == targetId } ?: return
        if (tappedTarget.role != GameTargetRole.Correct) {
            if (currentState.isBossRoundActive) {
                endBossRound(refreshTarget = true)
                return
            }
            loseLife(countAttempt = true)
            return
        }

        val timingGrade = calculateTimingGrade(currentState.targetLifetimeKey)
        val nextCombo = calculateNextCombo(
            currentCombo = currentState.combo,
            timingGrade = timingGrade
        )
        // Derive the next state from the state we already read instead of inside an update{}
        // lambda: MutableStateFlow.update/updateAndGet may re-run the lambda on CAS contention,
        // and targetEngine.generateTargets() is randomised, so a re-run would spawn
        // different targets than the ones we act on below.
        val updatedState = currentState.let {
            val wasBossRoundActive = it.isBossRoundActive
            val scoreGain = 1 + if (wasBossRoundActive) BOSS_ROUND_HIT_SCORE_BONUS else 0
            val newScore = it.score + scoreGain
            val newDifficulty = calculateDifficultyLevel(newScore)
            val nextFlawlessStreak = incrementStat(it.flawlessStreak)
            val flawlessBonusCoins = flawlessStreakBonusFor(nextFlawlessStreak)
            val bossHitCoinBonus = if (wasBossRoundActive) BOSS_ROUND_HIT_COIN_BONUS else 0
            val bossThreshold = bossRoundThresholdForScore(
                newScore = newScore,
                triggeredThresholds = it.triggeredBossRoundScores
            )
            val shouldStartBossRound = bossThreshold != null && !wasBossRoundActive
            val wasUltraMomentActive = it.isUltraMomentActive
            val ultraHitCoinBonus = if (wasUltraMomentActive) ULTRA_MOMENT_HIT_COIN_BONUS else 0
            val ultraThreshold = ultraMomentThresholdForCombo(
                combo = nextCombo,
                triggeredThresholds = it.triggeredUltraMomentCombos
            )
            val shouldStartUltraMoment = ultraThreshold != null && !wasUltraMomentActive
            val updatedDailyChallenge = advanceDailyChallengeForHit(
                state = it.dailyChallengeState,
                mode = it.selectedMode,
                score = newScore,
                combo = nextCombo
            )
            val updatedComboChallenge = advanceComboChallengeDuringGame(
                progression = it.progressionState,
                combo = nextCombo
            )
            val nextTargets = targetEngine.generateTargets(
                mode = it.selectedMode,
                score = newScore,
                currentTargets = it.targets,
                activeColor = it.activeColor,
                progression = it.progressionState
            )
            it.copy(
                score = newScore,
                bestScore = maxOf(it.bestScore, newScore),
                isNewBestScore = it.isNewBestScore || newScore > it.bestScore,
                difficultyLevel = newDifficulty,
                targetSizeDp = calculateTargetSizeDp(
                    score = newScore,
                    mode = it.selectedMode,
                    progression = it.progressionState
                ),
                targetVisibleDurationMillis = calculateTargetVisibleDurationMillis(
                    score = newScore,
                    mode = it.selectedMode,
                    progression = it.progressionState
                ).withBossRoundSpeed(shouldStartBossRound || wasBossRoundActive),
                targetPosition = nextTargets.firstCorrectPosition(),
                targets = nextTargets,
                combo = nextCombo,
                maxCombo = maxOf(it.maxCombo, nextCombo),
                perfectHits = it.perfectHits + if (timingGrade == TimingGrade.Perfect) 1 else 0,
                greatHits = it.greatHits + if (timingGrade == TimingGrade.Great) 1 else 0,
                lastTimingGrade = timingGrade.takeIf { grade -> grade != TimingGrade.Normal },
                flawlessStreak = nextFlawlessStreak,
                maxFlawlessStreak = maxOf(it.maxFlawlessStreak, nextFlawlessStreak),
                flawlessStreakBonusCoins = it.flawlessStreakBonusCoins + flawlessBonusCoins,
                lastFlawlessStreakMilestone = nextFlawlessStreak.takeIf { flawlessBonusCoins > 0 },
                isBossRoundActive = shouldStartBossRound || wasBossRoundActive,
                bossRoundTimeLeftSeconds = when {
                    shouldStartBossRound -> BOSS_ROUND_DURATION_SECONDS
                    wasBossRoundActive -> it.bossRoundTimeLeftSeconds
                    else -> 0
                },
                bossRoundHits = if (shouldStartBossRound) 0 else it.bossRoundHits + if (wasBossRoundActive) 1 else 0,
                bossRoundBonusCoins = if (shouldStartBossRound) 0 else it.bossRoundBonusCoins + bossHitCoinBonus,
                bossRoundTotalBonusCoins = it.bossRoundTotalBonusCoins + bossHitCoinBonus,
                bossRoundResultHits = if (shouldStartBossRound) 0 else it.bossRoundResultHits,
                bossRoundResultBonusCoins = if (shouldStartBossRound) 0 else it.bossRoundResultBonusCoins,
                isBossRoundResultVisible = if (shouldStartBossRound) false else it.isBossRoundResultVisible,
                bossRoundFeedbackKey = it.bossRoundFeedbackKey + if (shouldStartBossRound) 1 else 0,
                triggeredBossRoundScores = bossThreshold?.takeIf { shouldStartBossRound }?.let { threshold ->
                    it.triggeredBossRoundScores + threshold
                } ?: it.triggeredBossRoundScores,
                isUltraMomentActive = shouldStartUltraMoment || wasUltraMomentActive,
                ultraMomentTimeLeftSeconds = when {
                    shouldStartUltraMoment -> ULTRA_MOMENT_DURATION_SECONDS
                    wasUltraMomentActive -> it.ultraMomentTimeLeftSeconds
                    else -> 0
                },
                ultraMomentHits = if (shouldStartUltraMoment) {
                    0
                } else {
                    it.ultraMomentHits + if (wasUltraMomentActive) 1 else 0
                },
                ultraMomentTotalHits = it.ultraMomentTotalHits + if (wasUltraMomentActive) 1 else 0,
                ultraMomentBonusCoins = if (shouldStartUltraMoment) {
                    0
                } else {
                    it.ultraMomentBonusCoins + ultraHitCoinBonus
                },
                ultraMomentTotalBonusCoins = it.ultraMomentTotalBonusCoins + ultraHitCoinBonus,
                ultraMomentResultHits = if (shouldStartUltraMoment) 0 else it.ultraMomentResultHits,
                ultraMomentResultBonusCoins = if (shouldStartUltraMoment) 0 else it.ultraMomentResultBonusCoins,
                isUltraMomentResultVisible = if (shouldStartUltraMoment) false else it.isUltraMomentResultVisible,
                ultraMomentFeedbackKey = it.ultraMomentFeedbackKey + if (shouldStartUltraMoment) 1 else 0,
                triggeredUltraMomentCombos = ultraThreshold?.takeIf { shouldStartUltraMoment }?.let { threshold ->
                    it.triggeredUltraMomentCombos + threshold
                } ?: it.triggeredUltraMomentCombos,
                successfulHits = it.successfulHits + 1,
                totalAttempts = it.totalAttempts + 1,
                dailyChallengeState = updatedDailyChallenge,
                progressionState = updatedComboChallenge,
                targetLifetimeKey = it.targetLifetimeKey + 1
            )
        }
        _uiState.value = updatedState
        gamePreferences.saveProgressionState(updatedState.progressionState)
        gamePreferences.saveDailyChallengeState(updatedState.dailyChallengeState)
        logChallengeCompletedIfNeeded(
            previous = currentState.dailyChallengeState,
            updated = updatedState.dailyChallengeState
        )
        if (!currentState.isBossRoundActive && updatedState.isBossRoundActive) {
            startBossRoundTimer()
        }
        if (!currentState.isUltraMomentActive && updatedState.isUltraMomentActive) {
            startUltraMomentTimer()
        }
        startComboResetTimer(expectedCombo = nextCombo)
        startTargetTimeout()
    }

    fun onMissTapped() {
        if (isStorePreviewModeActive()) return
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        if (currentState.isBossRoundActive) {
            endBossRound(refreshTarget = true)
            return
        }
        loseLife(countAttempt = true)
    }

    fun retryGame() {
        if (isStorePreviewModeActive()) return
        launchNewGame(mode = _uiState.value.selectedMode)
    }

    fun pauseGame() {
        if (isStorePreviewModeActive()) return
        val currentState = _uiState.value
        if (!currentState.hasGameStarted || currentState.isPaused || currentState.isGameOver) return

        cancelGameplayJobs()
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        if (isStorePreviewModeActive()) return
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
        if (isStorePreviewModeActive()) return
        cancelGameplayJobs()
        val pendingModeMasteryLevelUp = _uiState.value.progressionState.lastModeMasteryLevelUp
        val storedProgression = gamePreferences.getProgressionState().copy(
            lastModeMasteryLevelUp = pendingModeMasteryLevelUp
        )
        _uiState.value = createInitialState(
            mode = _uiState.value.selectedMode,
            bestScores = _uiState.value.bestScoresByMode
        ).copy(
            bestScore = _uiState.value.bestScore,
            selectedMode = _uiState.value.selectedMode,
            dailyChallengeState = gamePreferences.getDailyChallengeState(),
            progressionState = storedProgression,
            playerProfile = gamePreferences.getPlayerProfile(),
            leaderboardSnapshot = createLeaderboardSnapshot(
                profile = gamePreferences.getPlayerProfile(),
                progression = storedProgression,
                bestScoresByMode = _uiState.value.bestScoresByMode
            ),
            isNewBestScore = false
        )
    }

    fun onRewardContinueEarned() {
        if (isStorePreviewModeActive()) return
        val currentState = _uiState.value
        if (
            !currentState.isGameOver ||
            currentState.hasUsedRewardContinue ||
            currentState.isRewardContinueReady
        ) return
        if (!tryConsumeRewardedCallback(RewardedAction.Continue)) return

        val updatedProgression = recordRewardedAdWatched(currentState.progressionState)
        _uiState.update {
            it.copy(
                progressionState = updatedProgression,
                isRewardContinueReady = true,
                canContinueWithReward = true
            )
        }
        gamePreferences.saveProgressionState(updatedProgression)
        logNewAchievementUnlocks(updatedProgression)
    }

    fun continueGameAfterReward() {
        if (isStorePreviewModeActive()) return
        val currentState = _uiState.value
        if (
            !currentState.isGameOver ||
            currentState.hasUsedRewardContinue ||
            !currentState.isRewardContinueReady
        ) return

        cancelGameplayJobs()
        val nextTargets = targetEngine.generateTargets(
            mode = currentState.selectedMode,
            score = currentState.score,
            currentTargets = currentState.targets,
            activeColor = currentState.activeColor,
            progression = currentState.progressionState
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
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        if (!state.isGameOver || state.isCoinDoubleClaimed || state.baseCoinsThisGame <= 0) return
        if (!tryConsumeRewardedCallback(RewardedAction.DoubleCoins)) return

        val bonusCoins = state.baseCoinsThisGame * (adConfig.doubleCoinMultiplier - 1).coerceAtLeast(1)
        val updatedProgression = recordRewardedAdWatched(
            addCoins(state.progressionState, bonusCoins)
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
        logNewAchievementUnlocks(updatedProgression)
        AdAnalyticsTracker.track(
            eventName = "double_coin_used",
            params = adParams(
                "base_coins" to state.baseCoinsThisGame,
                "multiplier" to adConfig.doubleCoinMultiplier,
                "total_coins" to updatedProgression.coins
            )
        )
    }

    fun onDailyChallengeDoubleRewardEarned() {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        val challenge = state.dailyChallengeState
        if (!challenge.completed || !challenge.rewardClaimed || challenge.doubleRewardClaimed) return
        if (!tryConsumeRewardedCallback(RewardedAction.DailyChallengeDoubleReward)) return

        val updatedChallenge = challenge.copy(doubleRewardClaimed = true)
        val updatedProgression = recordRewardedAdWatched(
            addCoins(state.progressionState, challenge.rewardCoins)
        )
        _uiState.update {
            it.copy(
                dailyChallengeState = updatedChallenge,
                progressionState = updatedProgression,
                pendingRewardedAction = null
            )
        }
        gamePreferences.saveDailyChallengeState(updatedChallenge)
        gamePreferences.saveProgressionState(updatedProgression)
        logNewAchievementUnlocks(updatedProgression)
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.ChallengeRewardDoubled,
            params = Bundle().apply {
                putString(FirebaseParam.ChallengeName.key, challenge.type.name)
                putInt(FirebaseParam.CoinAmount.key, challenge.rewardCoins)
            }
        )
        AdAnalyticsTracker.track(
            eventName = "daily_challenge_double_reward",
            params = adParams("reward_coins" to challenge.rewardCoins)
        )
    }

    fun claimDailyChallengeReward() {
        if (isStorePreviewModeActive()) return
        claimDailyChallengeBaseRewardIfReady()
    }

    fun claimWeeklyChallengeReward() {
        if (isStorePreviewModeActive()) return
        val freshProgression = gamePreferences.getProgressionState()
        val challenge = freshProgression.weeklyChallenge
        if (!challenge.completed || challenge.claimed) return

        val updatedChallenge = challenge.copy(claimed = true)
        val updatedProgression = addCoins(
            progression = freshProgression.copy(weeklyChallenge = updatedChallenge),
            coins = challenge.rewardCoins
        )
        _uiState.update { it.copy(progressionState = updatedProgression) }
        gamePreferences.saveProgressionState(updatedProgression)
    }

    fun claimDailyLeaderboardGoalReward() {
        if (isStorePreviewModeActive()) return
        val freshProgression = gamePreferences.getProgressionState()
        val goal = freshProgression.dailyLeaderboardGoal
        if (!goal.completed || goal.claimed) return

        val updatedGoal = goal.copy(claimed = true)
        val updatedProgression = addCoins(
            progression = freshProgression.copy(dailyLeaderboardGoal = updatedGoal),
            coins = goal.rewardCoins
        )
        saveProgressionAndUpdateState(updatedProgression)
    }

    fun claimPersonalGoalReward() {
        if (isStorePreviewModeActive()) return
        val freshProgression = gamePreferences.getProgressionState()
        val goal = freshProgression.personalGoal
        if (!goal.completed || goal.claimed) return

        val updatedGoal = goal.copy(claimed = true)
        val updatedProgression = addCoins(
            progression = freshProgression.copy(personalGoal = updatedGoal),
            coins = goal.rewardCoins
        )
        saveProgressionAndUpdateState(updatedProgression)
    }

    fun claimComboChallengeReward() {
        if (isStorePreviewModeActive()) return
        val freshProgression = gamePreferences.getProgressionState()
        val challenge = freshProgression.comboChallenge
        if (!challenge.completed || challenge.claimed) return

        val updatedChallenge = challenge.copy(claimed = true)
        val updatedProgression = addCoins(
            progression = freshProgression.copy(comboChallenge = updatedChallenge),
            coins = challenge.rewardCoins
        )
        saveProgressionAndUpdateState(updatedProgression)
    }

    fun onLeaderboardOpenedForMission() {
        if (isStorePreviewModeActive()) return
        advanceDailyChallengeForVisit(DailyChallenge.OpenLeaderboard)
        updateDailyLeaderboardGoalFromSnapshot(_uiState.value.leaderboardSnapshot)
    }

    fun onShopOpenedForMission() {
        if (isStorePreviewModeActive()) return
        advanceDailyChallengeForVisit(DailyChallenge.VisitShop)
    }

    fun onCoinChestRewardEarned() {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        val chest = state.progressionState.coinChest
        if (!chest.canOpen) return
        if (!tryConsumeRewardedCallback(RewardedAction.CoinChest)) return

        val rewardCoins = randomCoinChestReward()
        val updatedChest = chest.copy(
            openedToday = (chest.openedToday + 1).coerceAtMost(chest.maxOpensPerDay),
            lastOpenedDate = todayDateKey(),
            lastRewardCoins = rewardCoins
        )
        val updatedProgression = recordRewardedAdWatched(
            addCoins(state.progressionState, rewardCoins).copy(
                coinChest = updatedChest
            )
        )
        _uiState.update {
            it.copy(
                progressionState = updatedProgression,
                pendingRewardedAction = null
            )
        }
        gamePreferences.saveProgressionState(updatedProgression)
        logNewAchievementUnlocks(updatedProgression)
        AdAnalyticsTracker.track(
            eventName = "coin_chest_reward",
            params = adParams(
                "reward_coins" to rewardCoins,
                "opened_today" to updatedChest.openedToday,
                "remaining" to updatedChest.remainingOpens
            )
        )
    }

    fun onShopCoinRewardEarned() {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        val rewardState = state.progressionState.shopCoinReward
        if (!rewardState.canClaim) return
        if (!tryConsumeRewardedCallback(RewardedAction.ShopCoinReward)) return

        val updatedRewardState = rewardState.copy(
            claimedToday = (rewardState.claimedToday + 1).coerceAtMost(rewardState.maxClaimsPerDay),
            lastClaimDate = todayDateKey()
        )
        val updatedProgression = recordRewardedAdWatched(
            addCoins(state.progressionState, rewardState.rewardCoins).copy(
                shopCoinReward = updatedRewardState
            )
        )
        _uiState.update {
            it.copy(
                progressionState = updatedProgression,
                pendingRewardedAction = null
            )
        }
        gamePreferences.saveProgressionState(updatedProgression)
        logNewAchievementUnlocks(updatedProgression)
    }

    fun onInviteShareCompleted() {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        FirebaseGameServices.logEvent(event = FirebaseEvent.InviteShareClicked)
        if (state.progressionState.inviteRewardClaimed) return

        val updatedProgression = addCoins(
            progression = state.progressionState,
            coins = INVITE_SHARE_REWARD_COINS
        ).copy(inviteRewardClaimed = true)
        _uiState.update { it.copy(progressionState = updatedProgression) }
        gamePreferences.saveProgressionState(updatedProgression)
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.InviteRewardClaimed,
            params = Bundle().apply {
                putInt(FirebaseParam.CoinAmount.key, INVITE_SHARE_REWARD_COINS)
            }
        )
    }

    fun claimDailyReward() {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        val reward = state.progressionState.dailyReward
        if (!reward.canClaim) return

        val rewardedProgression = applyDailyReward(
            progression = state.progressionState
        )
        _uiState.update {
            it.copy(
                progressionState = rewardedProgression,
                shouldAutoShowDailyRewardDialog = false
            )
        }
        gamePreferences.saveProgressionState(rewardedProgression)
        gamePreferences.saveDailyRewardClaim(streakDay = rewardedProgression.dailyReward.streakDay)
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.DailyRewardClaimed,
            params = Bundle().apply {
                putInt(FirebaseParam.CoinAmount.key, reward.rewardCoins)
                putInt(FirebaseParam.StreakDay.key, reward.streakDay)
            }
        )
    }

    fun markDailyRewardDialogShown() {
        if (isStorePreviewModeActive()) return
        gamePreferences.markDailyRewardDialogShown()
        _uiState.update { it.copy(shouldAutoShowDailyRewardDialog = false) }
    }

    fun protectDailyRewardStreak() {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        val reward = state.progressionState.dailyReward
        if (!reward.canProtectStreak) return
        if (!tryConsumeRewardedCallback(RewardedAction.ProtectStreak)) return

        gamePreferences.protectDailyRewardStreak()
        val refreshedProgression = gamePreferences.getProgressionState()
        val updatedProgression = recordRewardedAdWatched(
            state.progressionState.copy(dailyReward = refreshedProgression.dailyReward)
        )
        _uiState.update {
            it.copy(
                progressionState = updatedProgression
            )
        }
        gamePreferences.saveProgressionState(updatedProgression)
        logNewAchievementUnlocks(updatedProgression)
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.StreakProtected,
            params = Bundle().apply {
                putInt(FirebaseParam.StreakDay.key, reward.streakDay)
            }
        )
    }

    fun updatePlayerName(name: String): Boolean {
        if (isStorePreviewModeActive()) return true
        val sanitizedName = sanitizePlayerName(name) ?: return false
        gamePreferences.savePlayerName(sanitizedName)
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.PlayerNameChanged,
            params = Bundle().apply {
                putInt(FirebaseParam.NameLength.key, sanitizedName.length)
            }
        )
        refreshProfileAndLeaderboard()
        return true
    }

    fun selectPlayerTitle(title: PlayerTitle) {
        if (isStorePreviewModeActive()) return
        gamePreferences.savePlayerTitle(title)
        refreshProfileAndLeaderboard()
    }

    fun selectProfileBadge(badge: ProfileBadge) {
        if (isStorePreviewModeActive()) return
        val progression = _uiState.value.progressionState
        if (badge !in unlockedProfileBadges(progression)) return
        val current = progression.selectedProfileBadgeIds
        val updatedIds = if (badge.storageKey in current) {
            current - badge.storageKey
        } else {
            (current + badge.storageKey).distinct().takeLast(3)
        }
        saveProgressionAndUpdateState(progression.copy(selectedProfileBadgeIds = updatedIds))
    }

    fun selectLeaderboardMode(mode: GameMode) {
        if (isStorePreviewModeActive()) return
        selectedLeaderboardMode = mode
        refreshProfileAndLeaderboard()
    }

    fun selectLeaderboardPeriod(period: LeaderboardPeriod) {
        if (isStorePreviewModeActive()) return
        selectedLeaderboardPeriod = period
        refreshProfileAndLeaderboard()
    }

    fun refreshLeaderboard() {
        if (isStorePreviewModeActive()) return
        leaderboardRefreshTick += 1
        refreshProfileAndLeaderboard(showLoading = true)
    }

    fun claimAchievementReward(achievementId: String) {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        val achievement = state.progressionState.achievements.firstOrNull { it.id == achievementId }
        if (achievement == null || !achievement.unlocked || achievement.claimed) return

        val updatedAchievements = state.progressionState.achievements.map {
            if (it.id == achievementId) it.copy(claimed = true) else it
        }
        val rewardProgression = addCoins(state.progressionState, achievement.rewardCoins).copy(
            achievements = updatedAchievements,
            latestUnlockedAchievementIds = emptyList()
        )
        val updatedProgression = addSeasonXp(
            addXpWithLevelRewards(rewardProgression, achievement.rewardXp),
            SEASON_XP_ACHIEVEMENT_CLAIM
        )
        _uiState.update { it.copy(progressionState = updatedProgression) }
        gamePreferences.saveProgressionState(updatedProgression)
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.AchievementClaimed,
            params = Bundle().apply {
                putString(FirebaseParam.AchievementId.key, achievement.id)
                putInt(FirebaseParam.CoinAmount.key, achievement.rewardCoins)
            }
        )
    }

    fun claimSeasonReward(level: Int) {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        val season = state.progressionState.season
        if (level !in 1..SeasonMaxLevel || level > season.level || level in season.claimedRewardLevels) return

        val reward = seasonRewardForLevel(level, season.claimedRewardLevels)
        val badgeReward = reward.kind == SeasonRewardKind.ProfileBadge ||
            reward.kind == SeasonRewardKind.NeonAvatar ||
            reward.kind == SeasonRewardKind.GoldFrame ||
            reward.kind == SeasonRewardKind.LeaderboardBadge
        val updatedProgression = addCoins(state.progressionState, reward.coinReward).copy(
            season = season.copy(
                claimedRewardLevels = season.claimedRewardLevels + level,
                preservedBadgeLevels = if (badgeReward) season.preservedBadgeLevels + level else season.preservedBadgeLevels
            )
        )
        _uiState.update { it.copy(progressionState = updatedProgression) }
        gamePreferences.saveProgressionState(updatedProgression)
    }

    fun activateSeasonXpBoost() {
        if (isStorePreviewModeActive()) return
        if (!tryConsumeRewardedCallback(RewardedAction.SeasonXpBoost)) return
        val watchedProgression = recordRewardedAdWatched(_uiState.value.progressionState)
        val updatedSeason = seasonForToday(watchedProgression.season).copy(
            xpBoostEndTimeMillis = System.currentTimeMillis() + SeasonXpBoostDurationMillis
        )
        val updatedProgression = watchedProgression.copy(season = updatedSeason)
        saveRewardedProgressionAndLog(updatedProgression)
    }

    fun claimSeasonMission(missionId: String) {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        val season = seasonForToday(state.progressionState.season)
        val mission = season.missions.firstOrNull { it.id == missionId } ?: return
        if (!mission.completed || mission.claimed) return

        val claimedProgression = state.progressionState.copy(
            season = season.copy(claimedMissionIds = season.claimedMissionIds + mission.id)
        )
        val updatedProgression = addSeasonXp(claimedProgression, mission.rewardSeasonXp)
        _uiState.update { it.copy(progressionState = updatedProgression) }
        gamePreferences.saveProgressionState(updatedProgression)
    }

    fun buyTheme(theme: PlayerTheme) {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        val progression = state.progressionState
        if (theme in progression.unlockedThemes || progression.coins < theme.coinPrice) return

        val updatedProgression = updateAchievementProgress(
            progression.copy(
                coins = progression.coins - theme.coinPrice,
                totalCoinsSpent = addSpentCoins(progression.totalCoinsSpent, theme.coinPrice),
                selectedTheme = theme,
                unlockedThemes = progression.unlockedThemes + theme
            )
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
                putString(FirebaseParam.ThemeName.key, theme.storageKey)
                putInt(FirebaseParam.CoinAmount.key, theme.coinPrice)
            }
        )
        gamePreferences.saveProgressionState(updatedProgression)
        logNewAchievementUnlocks(updatedProgression)
    }

    fun tryThemeForOneGame(theme: PlayerTheme) {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        if (theme in state.progressionState.unlockedThemes) return
        if (!tryConsumeRewardedCallback(RewardedAction.UnlockTheme)) return

        val updatedProgression = recordRewardedAdWatched(
            state.progressionState.copy(
                trialTheme = theme,
                trialGamesRemaining = 1
            )
        )
        saveRewardedProgressionAndLog(updatedProgression)
    }

    fun selectTheme(theme: PlayerTheme) {
        if (isStorePreviewModeActive()) return
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
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.ThemeSelected,
            params = Bundle().apply {
                putString(FirebaseParam.ThemeName.key, theme.storageKey)
            }
        )
    }

    fun buyTargetSkin(skin: TargetSkin) {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        val progression = state.progressionState
        if (skin in progression.unlockedTargetSkins || progression.coins < skin.coinPrice) return

        val updatedProgression = progression.copy(
            coins = (progression.coins - skin.coinPrice).coerceAtLeast(0),
            totalCoinsSpent = addSpentCoins(progression.totalCoinsSpent, skin.coinPrice),
            selectedTargetSkin = skin,
            unlockedTargetSkins = progression.unlockedTargetSkins + skin
        )
        saveProgressionAndUpdateState(updatedProgression)
    }

    fun selectTargetSkin(skin: TargetSkin) {
        if (isStorePreviewModeActive()) return
        val state = _uiState.value
        if (skin !in state.progressionState.unlockedTargetSkins) return

        val updatedProgression = state.progressionState.copy(selectedTargetSkin = skin)
        saveProgressionAndUpdateState(updatedProgression)
    }

    fun onInterstitialAdRequestHandled(wasShown: Boolean) {
        if (isStorePreviewModeActive()) return
        if (wasShown) {
            lastInterstitialShownElapsedMillis = SystemClock.elapsedRealtime()
        }
        _uiState.update { it.copy(shouldRequestInterstitialAd = false) }
    }

    private fun isStorePreviewModeActive(): Boolean {
        return BuildConfig.DEBUG && gamePreferences.isStorePreviewModeEnabled()
    }

    private fun cancelGameplayJobs() {
        timerJob?.cancel()
        targetTimeoutJob?.cancel()
        rewardContinueGraceJob?.cancel()
        movingTargetJob?.cancel()
        colorRuleJob?.cancel()
        comboResetJob?.cancel()
        bossRoundJob?.cancel()
        bossRoundResultClearJob?.cancel()
        ultraMomentJob?.cancel()
        ultraMomentResultClearJob?.cancel()
    }

    private fun saveProgressionAndUpdateState(progression: ProgressionState) {
        gamePreferences.saveProgressionState(progression)
        _uiState.update { it.copy(progressionState = progression) }
    }

    private fun saveRewardedProgressionAndLog(progression: ProgressionState) {
        saveProgressionAndUpdateState(progression)
        logNewAchievementUnlocks(progression)
    }

    private fun addCoins(progression: ProgressionState, coins: Int): ProgressionState {
        val safeCoins = coins.coerceAtLeast(0)
        val totalCoins = progression.coins.toLong() + safeCoins.toLong()
        val totalEarned = progression.totalCoinsEarned.toLong() + safeCoins.toLong()
        return progression.copy(
            coins = totalCoins.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(),
            totalCoinsEarned = totalEarned.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        )
    }

    private fun addSpentCoins(currentSpent: Int, spentCoins: Int): Int {
        return (currentSpent.coerceAtLeast(0).toLong() + spentCoins.coerceAtLeast(0).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
    }

    private fun updateDailyLeaderboardGoalAfterScore(
        progression: ProgressionState,
        score: Int,
        leaderboardScoreSubmitted: Boolean
    ): ProgressionState {
        val goal = dailyLeaderboardGoalForToday(progression.dailyLeaderboardGoal)
        if (goal.claimed) return progression.copy(dailyLeaderboardGoal = goal)

        val nextProgress = when (goal.type) {
            DailyLeaderboardGoalType.SubmitScore ->
                if (leaderboardScoreSubmitted) goal.target else goal.progress
            DailyLeaderboardGoalType.ImproveScore10 ->
                maxOf(goal.progress, safeScore(score) - goal.initialScore)
            DailyLeaderboardGoalType.Climb3Ranks,
            DailyLeaderboardGoalType.ReachTop50 -> goal.progress
        }.coerceIn(0, goal.target)
        val nextGoal = goal.copy(
            progress = nextProgress,
            completed = nextProgress >= goal.target
        )
        return progression.copy(dailyLeaderboardGoal = nextGoal)
    }

    private fun updateDailyLeaderboardGoalFromSnapshot(snapshot: LeaderboardSnapshot) {
        val updatedProgression = updateDailyLeaderboardGoalFromSnapshot(
            progression = _uiState.value.progressionState,
            snapshot = snapshot
        )
        if (updatedProgression == _uiState.value.progressionState) return

        saveProgressionAndUpdateState(updatedProgression)
    }

    private fun updateDailyLeaderboardGoalFromSnapshot(
        progression: ProgressionState,
        snapshot: LeaderboardSnapshot
    ): ProgressionState {
        val goal = dailyLeaderboardGoalForToday(progression.dailyLeaderboardGoal)
        if (goal.claimed || snapshot.playerRank <= 0) {
            return progression.copy(dailyLeaderboardGoal = goal)
        }

        val currentRank = snapshot.playerRank.coerceAtLeast(0)
        val seededGoal = if (goal.initialRank <= 0 && currentRank > 0) {
            goal.copy(initialRank = currentRank)
        } else {
            goal
        }
        val nextProgress = when (seededGoal.type) {
            DailyLeaderboardGoalType.ReachTop50 ->
                if (currentRank in 1..50) seededGoal.target else seededGoal.progress
            DailyLeaderboardGoalType.Climb3Ranks ->
                if (seededGoal.initialRank > 0) seededGoal.initialRank - currentRank else seededGoal.progress
            DailyLeaderboardGoalType.SubmitScore,
            DailyLeaderboardGoalType.ImproveScore10 -> seededGoal.progress
        }.coerceIn(0, seededGoal.target)
        val nextGoal = seededGoal.copy(
            progress = nextProgress,
            completed = nextProgress >= seededGoal.target
        )
        return progression.copy(dailyLeaderboardGoal = nextGoal)
    }

    private fun dailyLeaderboardGoalForToday(goal: DailyLeaderboardGoalState): DailyLeaderboardGoalState {
        return if (goal.createdDate == todayDateKey()) {
            goal
        } else {
            gamePreferences.getProgressionState().dailyLeaderboardGoal
        }
    }

    private fun personalGoalForToday(goal: PersonalGoalState): PersonalGoalState {
        return if (goal.createdDate == todayDateKey()) {
            goal
        } else {
            gamePreferences.getProgressionState().personalGoal
        }
    }

    private fun updatePersonalGoalAfterScore(
        progression: ProgressionState,
        score: Int
    ): ProgressionState {
        val goal = personalGoalForToday(progression.personalGoal)
        if (goal.claimed) return progression.copy(personalGoal = goal)

        val nextProgressScore = maxOf(goal.progressScore, safeScore(score))
        val nextGoal = goal.copy(
            progressScore = nextProgressScore,
            completed = nextProgressScore >= goal.targetScore
        )
        return progression.copy(personalGoal = nextGoal)
    }

    private fun comboChallengeForToday(challenge: ComboChallengeState): ComboChallengeState {
        return if (challenge.createdDate == todayDateKey()) {
            challenge
        } else {
            gamePreferences.getProgressionState().comboChallenge
        }
    }

    private fun advanceComboChallengeDuringGame(
        progression: ProgressionState,
        combo: Int
    ): ProgressionState {
        val challenge = comboChallengeForToday(progression.comboChallenge)
        if (challenge.claimed || challenge.completed) return progression.copy(comboChallenge = challenge)

        val nextProgress = when (challenge.type) {
            ComboChallengeType.Combo5,
            ComboChallengeType.Combo10,
            ComboChallengeType.NoMistake10Hits -> maxOf(challenge.progress, combo)
            ComboChallengeType.TotalCombo20In3Games -> challenge.progress
        }.coerceIn(0, challenge.target)
        val nextChallenge = challenge.copy(
            progress = nextProgress,
            completed = nextProgress >= challenge.target
        )
        return progression.copy(comboChallenge = nextChallenge)
    }

    private fun updateComboChallengeAfterGame(
        progression: ProgressionState,
        maxCombo: Int
    ): ProgressionState {
        val challenge = comboChallengeForToday(progression.comboChallenge)
        if (
            challenge.claimed ||
            challenge.completed ||
            challenge.type != ComboChallengeType.TotalCombo20In3Games
        ) {
            return progression.copy(comboChallenge = challenge)
        }

        val nextGamesUsed = (challenge.gamesUsed + 1).coerceIn(0, 3)
        val nextProgress = (challenge.progress + maxCombo.coerceAtLeast(0)).coerceIn(0, challenge.target)
        val nextChallenge = challenge.copy(
            progress = nextProgress,
            gamesUsed = nextGamesUsed,
            completed = nextProgress >= challenge.target
        )
        return progression.copy(comboChallenge = nextChallenge)
    }

    private fun safePlayerName(profile: PlayerProfile): String {
        return profile.name.trim().takeIf { it.isNotBlank() } ?: fallbackPlayerName()
    }

    private fun fallbackPlayerName(): String = defaultPlayerName()

    private fun safeTheme(progression: ProgressionState): PlayerTheme {
        return progression.activeTheme.takeIf {
            it == PlayerTheme.NeonRed || it in progression.unlockedThemes || it == progression.trialTheme
        } ?: PlayerTheme.NeonRed
    }

    private fun safeScore(score: Int): Int {
        return score.coerceAtLeast(0)
    }

    private fun incrementStat(value: Int): Int {
        return (value.coerceAtLeast(0).toLong() + 1L)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
    }

    private fun flawlessStreakBonusFor(streak: Int): Int {
        return when (streak) {
            5 -> FLAWLESS_STREAK_5_BONUS_COINS
            10 -> FLAWLESS_STREAK_10_BONUS_COINS
            20 -> FLAWLESS_STREAK_20_BONUS_COINS
            else -> 0
        }
    }

    private fun bossRoundThresholdForScore(
        newScore: Int,
        triggeredThresholds: Set<Int>
    ): Int? {
        return BOSS_ROUND_SCORE_THRESHOLDS
            .filter { it !in triggeredThresholds && newScore >= it }
            .minOrNull()
    }

    private fun ultraMomentThresholdForCombo(
        combo: Int,
        triggeredThresholds: Set<Int>
    ): Int? {
        return ULTRA_MOMENT_COMBO_THRESHOLDS
            .filter { it !in triggeredThresholds && combo >= it }
            .minOrNull()
    }

    private fun Long.withBossRoundSpeed(isBossRoundActive: Boolean): Long {
        if (!isBossRoundActive) return this
        return (this * BOSS_ROUND_SPEED_PERCENT / 100L)
            .coerceAtLeast(BOSS_ROUND_MIN_VISIBLE_DURATION_MILLIS)
    }

    private fun startBossRoundTimer() {
        bossRoundJob?.cancel()
        bossRoundJob = viewModelScope.launch {
            repeat(BOSS_ROUND_DURATION_SECONDS) {
                delay(1_000L)
                val shouldContinue = _uiState.updateAndGet { state ->
                    if (!state.canAcceptGameplayInput() || !state.isBossRoundActive) {
                        state
                    } else {
                        state.copy(
                            bossRoundTimeLeftSeconds = (state.bossRoundTimeLeftSeconds - 1).coerceAtLeast(0)
                        )
                    }
                }.isBossRoundActive
                if (!shouldContinue) return@launch
            }
            endBossRound(cancelTimer = false)
        }
    }

    private fun endBossRound(
        refreshTarget: Boolean = false,
        cancelTimer: Boolean = true
    ) {
        if (cancelTimer) {
            bossRoundJob?.cancel()
        }
        // generateTargets() is randomised, so it must not run inside an update{} lambda.
        val endedState = _uiState.value.let { state ->
            if (!state.isBossRoundActive) return@let state

            val nextTargets = if (refreshTarget) {
                targetEngine.generateTargets(
                    mode = state.selectedMode,
                    score = state.score,
                    currentTargets = state.targets,
                    activeColor = state.activeColor,
                    progression = state.progressionState
                )
            } else {
                state.targets
            }
            state.copy(
                isBossRoundActive = false,
                bossRoundTimeLeftSeconds = 0,
                bossRoundResultHits = state.bossRoundHits.coerceAtLeast(0),
                bossRoundResultBonusCoins = state.bossRoundBonusCoins.coerceAtLeast(0),
                isBossRoundResultVisible = true,
                bossRoundHits = 0,
                bossRoundBonusCoins = 0,
                bossRoundFeedbackKey = state.bossRoundFeedbackKey + 1,
                targetVisibleDurationMillis = calculateTargetVisibleDurationMillis(
                    score = state.score,
                    mode = state.selectedMode,
                    progression = state.progressionState
                ),
                targetPosition = nextTargets.firstCorrectPosition(),
                targets = nextTargets,
                targetLifetimeKey = state.targetLifetimeKey + if (refreshTarget) 1 else 0
            )
        }
        _uiState.value = endedState
        if (refreshTarget && endedState.canAcceptGameplayInput()) {
            startTargetTimeout()
            startModeJobs()
        }
        scheduleBossRoundResultClear(endedState.bossRoundFeedbackKey)
    }

    private fun scheduleBossRoundResultClear(feedbackKey: Int) {
        bossRoundResultClearJob?.cancel()
        bossRoundResultClearJob = viewModelScope.launch {
            delay(2_000L)
            _uiState.update { state ->
                if (state.bossRoundFeedbackKey != feedbackKey || state.isBossRoundActive) {
                    state
                } else {
                    state.copy(
                        bossRoundResultHits = 0,
                        bossRoundResultBonusCoins = 0,
                        isBossRoundResultVisible = false
                    )
                }
            }
        }
    }

    private fun startUltraMomentTimer() {
        ultraMomentJob?.cancel()
        ultraMomentJob = viewModelScope.launch {
            repeat(ULTRA_MOMENT_DURATION_SECONDS) {
                delay(1_000L)
                val shouldContinue = _uiState.updateAndGet { state ->
                    if (!state.canAcceptGameplayInput() || !state.isUltraMomentActive) {
                        state
                    } else {
                        state.copy(
                            ultraMomentTimeLeftSeconds = (state.ultraMomentTimeLeftSeconds - 1).coerceAtLeast(0)
                        )
                    }
                }.isUltraMomentActive
                if (!shouldContinue) return@launch
            }
            endUltraMoment(cancelTimer = false)
        }
    }

    private fun endUltraMoment(cancelTimer: Boolean = true) {
        if (cancelTimer) {
            ultraMomentJob?.cancel()
        }
        val endedState = _uiState.updateAndGet { state ->
            if (!state.isUltraMomentActive) return@updateAndGet state

            state.copy(
                isUltraMomentActive = false,
                ultraMomentTimeLeftSeconds = 0,
                ultraMomentResultHits = state.ultraMomentHits.coerceAtLeast(0),
                ultraMomentResultBonusCoins = state.ultraMomentBonusCoins.coerceAtLeast(0),
                isUltraMomentResultVisible = true,
                ultraMomentHits = 0,
                ultraMomentBonusCoins = 0,
                ultraMomentFeedbackKey = state.ultraMomentFeedbackKey + 1
            )
        }
        scheduleUltraMomentResultClear(endedState.ultraMomentFeedbackKey)
    }

    private fun scheduleUltraMomentResultClear(feedbackKey: Int) {
        ultraMomentResultClearJob?.cancel()
        ultraMomentResultClearJob = viewModelScope.launch {
            delay(2_000L)
            _uiState.update { state ->
                if (state.ultraMomentFeedbackKey != feedbackKey || state.isUltraMomentActive) {
                    state
                } else {
                    state.copy(
                        ultraMomentResultHits = 0,
                        ultraMomentResultBonusCoins = 0,
                        isUltraMomentResultVisible = false
                    )
                }
            }
        }
    }

    private fun tryConsumeRewardedCallback(action: RewardedAction): Boolean {
        val now = SystemClock.elapsedRealtime()
        val isDuplicate = lastRewardedGrantAction == action &&
            now - lastRewardedGrantElapsedMillis < REWARDED_CALLBACK_DEDUPE_MILLIS
        if (isDuplicate) return false

        lastRewardedGrantAction = action
        lastRewardedGrantElapsedMillis = now
        return true
    }

    private fun launchNewGame(
        mode: GameMode,
        boost: GameBoost? = null,
        powerUp: GamePowerUp? = null
    ) {
        cancelGameplayJobs()
        val initialState = createInitialState(
            mode = mode,
            bestScores = _uiState.value.bestScoresByMode
        )
        val shouldPauseForModeTip = mode !in initialState.shownModeTips
        val currentProgression = gamePreferences.getProgressionState().copy(
            trialTheme = _uiState.value.progressionState.trialTheme,
            trialGamesRemaining = _uiState.value.progressionState.trialGamesRemaining
        )
        lastHitElapsedMillis = 0L
        targetSpawnElapsedMillis = 0L
        targetSpawnLifetimeKey = -1
        gameStartedElapsedMillis = SystemClock.elapsedRealtime()
        _uiState.value = initialState.copy(
            bestScore = _uiState.value.bestScore,
            dailyChallengeState = gamePreferences.getDailyChallengeState(),
            progressionState = currentProgression,
            activeBoost = boost,
            activePowerUp = powerUp,
            isPowerUpConsumed = false,
            lives = initialState.lives +
                if (boost == GameBoost.ExtraLife || powerUp == GamePowerUp.ExtraLife) 1 else 0,
            timeLeftSeconds = initialState.timeLeftSeconds +
                if (boost == GameBoost.ExtraTime || powerUp == GamePowerUp.ExtraTime) 5 else 0,
            combo = if (boost == GameBoost.ComboStart) 5 else initialState.combo,
            maxCombo = if (boost == GameBoost.ComboStart) 5 else initialState.maxCombo,
            earnedCoinsThisGame = 0,
            baseCoinsThisGame = 0,
            isCoinDoubleClaimed = false,
            oneMoreGameBonusEarnedThisGame = 0,
            hasGameStarted = true,
            isPaused = shouldPauseForModeTip,
            isNewBestScore = false
        )
        if (!shouldPauseForModeTip) {
            startTimer()
            startTargetTimeout()
            startModeJobs()
        }
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
        cancelGameplayJobs()
        if (countAttempt) {
            lastHitElapsedMillis = 0L
            _uiState.update {
                it.copy(
                    totalAttempts = it.totalAttempts + 1,
                    combo = 0,
                    flawlessStreak = 0,
                    lastFlawlessStreakMilestone = null
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
        if (targetSpawnLifetimeKey != targetLifetimeKey) {
            targetSpawnElapsedMillis = SystemClock.elapsedRealtime()
            targetSpawnLifetimeKey = targetLifetimeKey
        }
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

        if (currentState.isBossRoundActive) {
            endBossRound(refreshTarget = true)
            return
        }

        if (consumeFirstMistakeForgiveness(countAttempt = false)) return

        val remainingLives = currentState.lives - 1
        if (remainingLives <= 0) {
            endGame()
            return
        }

        lastHitElapsedMillis = 0L
        // generateTargets() is randomised, so it must not run inside an update{} lambda.
        _uiState.value = currentState.let {
            val nextTargets = targetEngine.generateTargets(
                mode = it.selectedMode,
                score = it.score,
                currentTargets = it.targets,
                activeColor = it.activeColor,
                progression = it.progressionState
            )
            it.copy(
                lives = remainingLives,
                combo = if (shouldProtectCombo(it)) it.combo else 0,
                flawlessStreak = 0,
                lastFlawlessStreakMilestone = null,
                isPowerUpConsumed = it.isPowerUpConsumed || it.activePowerUp == GamePowerUp.ComboProtection,
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
        cancelGameplayJobs()
        completedGameCount += 1
        val stateBeforeFinish = _uiState.value
        val gameDurationMillis = (SystemClock.elapsedRealtime() - gameStartedElapsedMillis).coerceAtLeast(0L)
        val shouldRequestInterstitialAd = shouldRequestInterstitialAfterGame(
            score = stateBeforeFinish.score,
            bestScore = stateBeforeFinish.bestScore,
            isNewBestScore = stateBeforeFinish.isNewBestScore,
            gameDurationMillis = gameDurationMillis
        )
        if (shouldRequestInterstitialAd) {
            nextInterstitialGameCount = completedGameCount + randomInterstitialInterval()
        }

        val previousModeBestScore = stateBeforeFinish.bestScoresByMode[stateBeforeFinish.selectedMode] ?: 0
        val previousDailyChallenge = stateBeforeFinish.dailyChallengeState
        val completedState = completeGameState(
            lives = lives,
            timeLeftSeconds = timeLeftSeconds,
            reason = reason,
            reasonRes = reasonRes,
            gameDurationMillis = gameDurationMillis,
            shouldRequestInterstitialAd = shouldRequestInterstitialAd
        )
        val finalProgression = updateDailyLeaderboardGoalAfterScore(
            progression = completedState.progressionState,
            score = completedState.score,
            leaderboardScoreSubmitted = completedState.score > safeScore(previousModeBestScore)
        ).let { progression ->
            updatePersonalGoalAfterScore(
                progression = progression,
                score = completedState.score
            )
        }.let { progression ->
            updateComboChallengeAfterGame(
                progression = progression,
                maxCombo = completedState.maxCombo
            )
        }
        val finalState = if (finalProgression == completedState.progressionState) {
            completedState
        } else {
            completedState.copy(progressionState = finalProgression).also { updatedState ->
                _uiState.update { it.copy(progressionState = updatedState.progressionState) }
            }
        }

        persistCompletedGame(
            finalState = finalState,
            previousDailyChallenge = previousDailyChallenge
        )
        uploadLeaderboardScoreIfNeeded(
            finalState = finalState,
            previousModeBestScore = previousModeBestScore
        )
        logGameOver(finalState)
    }

    private fun completeGameState(
        lives: Int,
        timeLeftSeconds: Int,
        reason: String?,
        reasonRes: Int?,
        gameDurationMillis: Long,
        shouldRequestInterstitialAd: Boolean
    ): GameUiState {
        return _uiState.updateAndGet { state ->
            val finalScore = safeScore(state.score)
            val currentModeBest = safeScore(state.bestScoresByMode[state.selectedMode] ?: 0)
            val isNewModeBest = finalScore > currentModeBest
            val progressionBeforeGame = progressionForGameCompletion(state.progressionState)
            val rewards = calculateCompletedGameRewards(
                state = state,
                score = finalScore,
                isNewModeBest = isNewModeBest,
                progressionBeforeGame = progressionBeforeGame
            )
            val accuracyPercent = accuracyPercent(
                hits = state.successfulHits,
                attempts = state.totalAttempts
            )
            val survivalSeconds = (gameDurationMillis / 1_000L)
                .coerceIn(0L, Int.MAX_VALUE.toLong())
                .toInt()
            val newPersonalRecords = personalRecordsBrokenByGame(
                progression = progressionBeforeGame,
                bestScoresByMode = state.bestScoresByMode,
                mode = state.selectedMode,
                score = finalScore,
                maxCombo = state.maxCombo,
                accuracyPercent = accuracyPercent,
                survivalSeconds = survivalSeconds,
                earnedCoins = rewards.totalCoins
            )
            val unlockedBadgesBeforeGame = unlockedProfileBadges(progressionBeforeGame)
            val updatedProgression = updateProgressionAfterGame(
                progression = progressionBeforeGame,
                score = finalScore,
                mode = state.selectedMode,
                hits = state.successfulHits,
                misses = (state.totalAttempts - state.successfulHits).coerceAtLeast(0),
                maxCombo = state.maxCombo,
                maxFlawlessStreak = state.maxFlawlessStreak,
                earnedCoins = rewards.totalCoins,
                accuracyPercent = accuracyPercent,
                survivalSeconds = survivalSeconds,
                bossRoundHits = state.bossRoundResultHits + state.bossRoundHits,
                ultraMomentHits = state.ultraMomentTotalHits,
                dailyMiniTournamentRewardCoins = rewards.dailyMiniTournamentCoins,
                isNewBestScore = isNewModeBest
            ).copy(
                oneMoreGameBonus = advanceOneMoreGameBonusAfterCompletedGame(
                    state = progressionBeforeGame.oneMoreGameBonus,
                    bonusAwarded = rewards.oneMoreBonusCoins > 0
                ),
                firstTargetBonusClaimed = progressionBeforeGame.firstTargetBonusClaimed ||
                    rewards.firstTargetBonusCoins > 0
            )
            val trialAwareProgression = consumeTrialThemeGame(updatedProgression)
            val newlyUnlockedBadges = unlockedProfileBadges(trialAwareProgression) - unlockedBadgesBeforeGame
            val updatedBestScores = if (isNewModeBest) {
                state.bestScoresByMode + (state.selectedMode to finalScore)
            } else {
                state.bestScoresByMode
            }
            val updatedDailyChallenge = advanceDailyChallengeForGameCompleted(state.dailyChallengeState)
            val updatedProfile = updateProfileAfterGame(
                profile = state.playerProfile,
                mode = state.selectedMode,
                score = finalScore
            )
            val updatedLeaderboard = createLeaderboardSnapshot(
                profile = updatedProfile,
                progression = trialAwareProgression,
                bestScoresByMode = updatedBestScores
            )

            state.copy(
                score = finalScore,
                lives = lives,
                timeLeftSeconds = timeLeftSeconds,
                dailyChallengeState = updatedDailyChallenge,
                hasGameStarted = true,
                isPaused = false,
                isResumeGracePeriod = false,
                isGameOver = true,
                gameOverReason = reason,
                gameOverReasonRes = reasonRes,
                bestScore = maxOf(currentModeBest, finalScore),
                bestScoresByMode = updatedBestScores,
                isNewBestScore = state.isNewBestScore || isNewModeBest,
                progressionState = trialAwareProgression.copy(latestUnlockedProfileBadges = newlyUnlockedBadges),
                newPersonalRecords = newPersonalRecords,
                playerProfile = updatedProfile,
                leaderboardSnapshot = updatedLeaderboard,
                activeBoost = null,
                activePowerUp = null,
                isPowerUpConsumed = false,
                baseCoinsThisGame = rewards.baseCoins,
                earnedCoinsThisGame = rewards.totalCoins,
                isCoinDoubleClaimed = false,
                oneMoreGameBonusEarnedThisGame = rewards.oneMoreBonusCoins,
                canContinueWithReward = !state.hasUsedRewardContinue,
                isRewardContinueReady = false,
                shouldRequestInterstitialAd = shouldRequestInterstitialAd
            )
        }
    }

    private data class CompletedGameRewards(
        val baseCoins: Int,
        val firstTargetBonusCoins: Int,
        val oneMoreBonusCoins: Int,
        val dailyMiniTournamentCoins: Int,
        val totalCoins: Int
    )

    private fun progressionForGameCompletion(currentProgression: ProgressionState): ProgressionState {
        val progressionFromStorage = gamePreferences.getProgressionState()
        return currentProgression.copy(
            dailyReward = progressionFromStorage.dailyReward,
            achievements = progressionFromStorage.achievements,
            weeklyChallenge = progressionFromStorage.weeklyChallenge,
            dailyLeaderboardGoal = progressionFromStorage.dailyLeaderboardGoal,
            bonusHour = progressionFromStorage.bonusHour,
            dailyMiniTournament = progressionFromStorage.dailyMiniTournament,
            season = progressionFromStorage.season,
            oneMoreGameBonus = progressionFromStorage.oneMoreGameBonus,
            firstTargetBonusClaimed = progressionFromStorage.firstTargetBonusClaimed,
            inviteRewardClaimed = progressionFromStorage.inviteRewardClaimed
        )
    }

    private fun calculateCompletedGameRewards(
        state: GameUiState,
        score: Int,
        isNewModeBest: Boolean,
        progressionBeforeGame: ProgressionState
    ): CompletedGameRewards {
        val baseCoins = calculateEarnedCoins(
            score = score,
            maxCombo = state.maxCombo,
            isNewBestScore = isNewModeBest
        )
        val firstTargetBonusCoins = if (
            gamePreferences.isOnboardingCompleted() &&
            !progressionBeforeGame.firstTargetBonusClaimed
        ) {
            FirstTargetBonusCoins
        } else {
            0
        }
        val oneMoreBonusCoins = if (progressionBeforeGame.oneMoreGameBonus.shouldRewardNextCompletedGame) {
            progressionBeforeGame.oneMoreGameBonus.rewardCoins
        } else {
            0
        }
        val dailyModeBonusCoins = calculateDailyModeBonusCoins(
            baseCoins = baseCoins,
            playedMode = state.selectedMode,
            dailyFeaturedMode = state.dailyFeaturedMode
        )
        val bonusHourCoins = calculateBonusHourCoins(
            baseCoins = baseCoins,
            bonusHour = progressionBeforeGame.bonusHour
        )
        val dailyMiniTournamentCoins = dailyMiniTournamentRewardForGame(
            tournament = progressionBeforeGame.dailyMiniTournament,
            playedMode = state.selectedMode,
            score = score
        )
        val perfectTimingCoins = state.perfectHits.coerceAtLeast(0) * PERFECT_TIMING_BONUS_COINS
        val flawlessStreakCoins = state.flawlessStreakBonusCoins.coerceAtLeast(0)
        val bossRoundCoins = state.bossRoundTotalBonusCoins.coerceAtLeast(0)
        val ultraMomentCoins = state.ultraMomentTotalBonusCoins.coerceAtLeast(0)

        return CompletedGameRewards(
            baseCoins = baseCoins,
            firstTargetBonusCoins = firstTargetBonusCoins,
            oneMoreBonusCoins = oneMoreBonusCoins,
            dailyMiniTournamentCoins = dailyMiniTournamentCoins,
            totalCoins = baseCoins + firstTargetBonusCoins + oneMoreBonusCoins +
                dailyModeBonusCoins + bonusHourCoins + dailyMiniTournamentCoins +
                perfectTimingCoins + flawlessStreakCoins + bossRoundCoins + ultraMomentCoins
        )
    }

    private fun calculateBonusHourCoins(
        baseCoins: Int,
        bonusHour: BonusHourState
    ): Int {
        if (!bonusHour.isActive || baseCoins <= 0) return 0

        return (baseCoins * bonusHour.coinBonusPercent / 100).coerceAtLeast(1)
    }

    private fun consumeTrialThemeGame(progression: ProgressionState): ProgressionState {
        if (progression.trialGamesRemaining <= 0) return progression

        val remaining = progression.trialGamesRemaining - 1
        return progression.copy(
            trialGamesRemaining = remaining,
            trialTheme = progression.trialTheme.takeIf { remaining > 0 }
        )
    }

    private fun updateProfileAfterGame(
        profile: PlayerProfile,
        mode: GameMode,
        score: Int
    ): PlayerProfile {
        return profile.copy(
            weeklyBestScore = maxOf(profile.weeklyBestScore, score),
            weeklyBestScoresByMode = profile.weeklyBestScoresByMode +
                (mode to maxOf(profile.weeklyBestScoresByMode[mode] ?: 0, score))
        )
    }

    private fun persistCompletedGame(
        finalState: GameUiState,
        previousDailyChallenge: DailyChallengeState
    ) {
        viewModelScope.launch {
            gamePreferences.saveBestScore(finalState.selectedMode, finalState.score)
        }
        gamePreferences.saveWeeklyBestScore(finalState.selectedMode, finalState.score)
        gamePreferences.saveDailyChallengeState(finalState.dailyChallengeState)
        gamePreferences.saveProgressionState(finalState.progressionState)
        logNewAchievementUnlocks(finalState.progressionState)
        logChallengeCompletedIfNeeded(
            previous = previousDailyChallenge,
            updated = finalState.dailyChallengeState
        )
    }

    private fun uploadLeaderboardScoreIfNeeded(
        finalState: GameUiState,
        previousModeBestScore: Int
    ) {
        if (finalState.score <= safeScore(previousModeBestScore)) return

        viewModelScope.launch {
            val uploaded = leaderboardRepository.uploadScore(
                playerName = safePlayerName(finalState.playerProfile),
                score = safeScore(finalState.score),
                level = finalState.progressionState.level,
                selectedTheme = safeTheme(finalState.progressionState),
                mode = finalState.selectedMode
            )
            if (uploaded) {
                loadRemoteLeaderboard(showLoading = false)
            }
        }
    }

    private fun logGameOver(finalState: GameUiState) {
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.GameOver,
            params = Bundle().apply {
                putString(FirebaseParam.ModeName.key, finalState.selectedMode.storageKey)
                putInt(FirebaseParam.Score.key, safeScore(finalState.score))
                putInt(FirebaseParam.MaxCombo.key, finalState.maxCombo)
                putBoolean(FirebaseParam.NewBest.key, finalState.isNewBestScore)
            }
        )
    }

    private fun trackGameStart(mode: GameMode) {
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.GameStart,
            params = Bundle().apply {
                putString(FirebaseParam.ModeName.key, mode.storageKey)
            }
        )
    }

    private fun createInitialState(
        mode: GameMode = GameMode.Classic,
        bestScores: Map<GameMode, Int> = GameMode.entries.associateWith { 0 }
    ): GameUiState {
        val progression = gamePreferences.getProgressionState()
        val playerProfile = gamePreferences.getPlayerProfile()
        val activeColor = if (mode == GameMode.ColorReflex) {
            targetEngine.randomTargetColor()
        } else {
            ReflexTargetColor.Red
        }
        val targets = targetEngine.generateTargets(
            mode = mode,
            score = 0,
            activeColor = activeColor,
            progression = progression
        )
        return GameUiState(
            lives = INITIAL_LIVES,
            timeLeftSeconds = GameDifficultyConfig.initialTimeSeconds(mode),
            difficultyLevel = calculateDifficultyLevel(score = 0),
            targetSizeDp = calculateTargetSizeDp(score = 0, mode = mode, progression = progression),
            targetVisibleDurationMillis = calculateTargetVisibleDurationMillis(
                score = 0,
                mode = mode,
                progression = progression
            ),
            targetPosition = targets.firstCorrectPosition(),
            selectedMode = mode,
            dailyFeaturedMode = createDailyFeaturedMode(),
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
            ),
            shownModeTips = gamePreferences.getShownModeTips(),
            shouldAutoShowDailyRewardDialog = gamePreferences.shouldShowDailyRewardDialog(progression.dailyReward)
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

    private fun loseLife(countAttempt: Boolean = false) {
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        if (consumeFirstMistakeForgiveness(countAttempt = countAttempt)) return

        lastHitElapsedMillis = 0L
        val remainingLives = currentState.lives - 1
        if (remainingLives <= 0) {
            endGame(countAttempt = countAttempt)
            return
        }

        // generateTargets() is randomised, so it must not run inside an update{} lambda.
        _uiState.value = currentState.let {
            val nextTargets = targetEngine.generateTargets(
                mode = it.selectedMode,
                score = it.score,
                currentTargets = it.targets,
                activeColor = it.activeColor,
                progression = it.progressionState
            )
            it.copy(
                lives = remainingLives,
                combo = if (shouldProtectCombo(it)) it.combo else 0,
                flawlessStreak = 0,
                lastFlawlessStreakMilestone = null,
                isPowerUpConsumed = it.isPowerUpConsumed || it.activePowerUp == GamePowerUp.ComboProtection,
                totalAttempts = it.totalAttempts + if (countAttempt) 1 else 0,
                targetPosition = nextTargets.firstCorrectPosition(),
                targets = nextTargets,
                targetLifetimeKey = it.targetLifetimeKey + 1
            )
        }
        startTargetTimeout()
        startModeJobs()
    }

    private fun consumeFirstMistakeForgiveness(countAttempt: Boolean): Boolean {
        val currentState = _uiState.value
        if (
            currentState.activePowerUp != GamePowerUp.FirstMistakeForgiveness ||
            currentState.isPowerUpConsumed
        ) {
            return false
        }

        lastHitElapsedMillis = 0L
        // generateTargets() is randomised, so it must not run inside an update{} lambda.
        _uiState.value = currentState.let {
            val nextTargets = targetEngine.generateTargets(
                mode = it.selectedMode,
                score = it.score,
                currentTargets = it.targets,
                activeColor = it.activeColor,
                progression = it.progressionState
            )
            it.copy(
                combo = 0,
                flawlessStreak = 0,
                lastFlawlessStreakMilestone = null,
                isPowerUpConsumed = true,
                totalAttempts = it.totalAttempts + if (countAttempt) 1 else 0,
                targetPosition = nextTargets.firstCorrectPosition(),
                targets = nextTargets,
                targetLifetimeKey = it.targetLifetimeKey + 1
            )
        }
        startTargetTimeout()
        startModeJobs()
        return true
    }

    private fun shouldProtectCombo(state: GameUiState): Boolean {
        return state.activePowerUp == GamePowerUp.ComboProtection && !state.isPowerUpConsumed
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
                    val latestState = _uiState.value
                    delay(
                        targetEngine.calculateMovementIntervalMillis(
                            score = latestState.score,
                            mode = latestState.selectedMode,
                            progression = latestState.progressionState
                        )
                    )
                    moveTargets()
                }
            }
        }

        if (state.selectedMode == GameMode.ColorReflex) {
            colorRuleJob = viewModelScope.launch {
                while (_uiState.value.canAcceptGameplayInput() && _uiState.value.selectedMode == GameMode.ColorReflex) {
                    val latestState = _uiState.value
                    delay(
                        GameDifficultyConfig.colorRuleIntervalMillis(
                            score = latestState.score,
                            mode = latestState.selectedMode,
                            progression = latestState.progressionState
                        )
                    )
                    rotateColorRule()
                }
            }
        }
    }

    private fun calculateTimingGrade(targetLifetimeKey: Int): TimingGrade {
        val elapsedMillis = if (targetSpawnLifetimeKey == targetLifetimeKey && targetSpawnElapsedMillis > 0L) {
            SystemClock.elapsedRealtime() - targetSpawnElapsedMillis
        } else {
            Long.MAX_VALUE
        }
        return when {
            elapsedMillis <= PERFECT_TIMING_THRESHOLD_MS -> TimingGrade.Perfect
            elapsedMillis <= GREAT_TIMING_THRESHOLD_MS -> TimingGrade.Great
            else -> TimingGrade.Normal
        }
    }

    private fun calculateNextCombo(
        currentCombo: Int,
        timingGrade: TimingGrade
    ): Int {
        val now = SystemClock.elapsedRealtime()
        val isComboContinuing = lastHitElapsedMillis > 0L &&
            now - lastHitElapsedMillis <= COMBO_WINDOW_MILLIS
        lastHitElapsedMillis = now
        val baseCombo = if (isComboContinuing) currentCombo + 1 else 1
        return baseCombo + if (timingGrade == TimingGrade.Perfect) 1 else 0
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

    private fun advanceDailyChallengeForVisit(type: DailyChallenge) {
        val previous = _uiState.value.dailyChallengeState
        if (previous.completed || previous.type != type) return

        val updated = previous.copy(
            progress = 1,
            completed = true
        )
        _uiState.update { it.copy(dailyChallengeState = updated) }
        gamePreferences.saveDailyChallengeState(updated)
        logChallengeCompletedIfNeeded(previous = previous, updated = updated)
    }

    private fun claimDailyChallengeBaseRewardIfReady() {
        val state = _uiState.value
        val challenge = state.dailyChallengeState
        if (!challenge.completed || challenge.rewardClaimed) return

        val updatedChallenge = challenge.copy(rewardClaimed = true)
        val updatedProgression = addXpWithLevelRewards(
            addCoins(state.progressionState, challenge.rewardCoins),
            DAILY_CHALLENGE_XP_REWARD
        ).let { addSeasonXp(it, SEASON_XP_CHALLENGE_COMPLETED) }
            .let { advanceSeasonQuestForDailyMissionClaim(it) }
        _uiState.update {
            it.copy(
                dailyChallengeState = updatedChallenge,
                progressionState = updatedProgression
            )
        }
        gamePreferences.saveDailyChallengeState(updatedChallenge)
        gamePreferences.saveProgressionState(updatedProgression)
    }

    private fun logChallengeCompletedIfNeeded(
        previous: DailyChallengeState,
        updated: DailyChallengeState
    ) {
        if (previous.completed || !updated.completed) return

        FirebaseGameServices.logEvent(
            event = FirebaseEvent.ChallengeCompleted,
            params = Bundle().apply {
                putString(FirebaseParam.ChallengeName.key, updated.type.name)
                putInt(FirebaseParam.CoinAmount.key, updated.rewardCoins)
            }
        )
    }

    private fun updateProgressionAfterGame(
        progression: ProgressionState,
        score: Int,
        mode: GameMode,
        hits: Int,
        misses: Int,
        maxCombo: Int,
        maxFlawlessStreak: Int,
        earnedCoins: Int,
        accuracyPercent: Int,
        survivalSeconds: Int,
        bossRoundHits: Int,
        ultraMomentHits: Int,
        dailyMiniTournamentRewardCoins: Int,
        isNewBestScore: Boolean
    ): ProgressionState {
        val earnedXp = calculateEarnedXp(score, hits, maxCombo, isNewBestScore)
        val nextTotalGames = incrementStat(progression.totalGames)
        val nextTotalScore = (progression.totalScore.coerceAtLeast(0).toLong() + score.coerceAtLeast(0).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        val nextGamesPlayedByMode = progression.gamesPlayedByMode +
            (mode to incrementStat(progression.gamesPlayedByMode[mode] ?: 0))
        val nextTotalHits = (progression.totalHits.coerceAtLeast(0).toLong() + hits.coerceAtLeast(0).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        val nextTotalMisses = (progression.totalMisses.coerceAtLeast(0).toLong() + misses.coerceAtLeast(0).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        val nextMaxCombo = maxOf(progression.lifetimeMaxCombo, maxCombo)
        val nextMaxFlawlessStreak = maxOf(progression.lifetimeMaxFlawlessStreak, maxFlawlessStreak)
        val nextPersonalRecords = progression.personalRecords.copy(
            bestScore = maxOf(progression.personalRecords.bestScore, score.coerceAtLeast(0)),
            bestCombo = maxOf(progression.personalRecords.bestCombo, maxCombo.coerceAtLeast(0)),
            bestAccuracyPercent = maxOf(
                progression.personalRecords.bestAccuracyPercent,
                accuracyPercent.coerceIn(0, 100)
            ),
            longestSurvivalSeconds = maxOf(
                progression.personalRecords.longestSurvivalSeconds,
                survivalSeconds.coerceAtLeast(0)
            ),
            mostCoinsInGame = maxOf(progression.personalRecords.mostCoinsInGame, earnedCoins.coerceAtLeast(0))
        )
        val nextTotalBossRoundHits = (progression.totalBossRoundHits.coerceAtLeast(0).toLong() +
            bossRoundHits.coerceAtLeast(0).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        val nextTotalUltraMomentHits = (progression.totalUltraMomentHits.coerceAtLeast(0).toLong() +
            ultraMomentHits.coerceAtLeast(0).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        val masteryResult = advanceModeMastery(
            progression = progression,
            mode = mode,
            score = score,
            maxCombo = maxCombo
        )
        val weeklyChallenge = advanceWeeklyChallenge(
            challenge = progression.weeklyChallenge,
            mode = mode,
            score = score,
            maxCombo = maxCombo
        )
        val weeklyGoalBoard = advanceWeeklyGoalBoardAfterGame(
            board = progression.weeklyGoalBoard,
            score = score,
            maxCombo = maxCombo
        )
        val dailyMiniTournament = advanceDailyMiniTournamentAfterGame(
            tournament = progression.dailyMiniTournament,
            playedMode = mode,
            score = score
        ).let { tournament ->
            if (dailyMiniTournamentRewardCoins > 0) {
                tournament.copy(claimed = true, rewardClaimedThisGame = true)
            } else {
                tournament
            }
        }
        val seasonWithQuestProgress = advanceSeasonQuestsAfterGame(
            season = progression.season,
            score = score,
            maxCombo = maxCombo,
            theme = progression.activeTheme,
            targetSkin = progression.selectedTargetSkin
        )
        val seasonQuestRewardCoins = seasonWithQuestProgress.seasonQuestRewardCoinsThisGame
        val seasonHunterUnlocked = progression.seasonHunterBadgeUnlocked ||
            seasonWithQuestProgress.seasonQuestsCompleted

        val gameProgression = addCoins(
            progression,
            earnedCoins + masteryResult.coinBonus + weeklyGoalBoard.totalRewardCoins + seasonQuestRewardCoins
        ).copy(
            totalGames = nextTotalGames,
            totalScore = nextTotalScore,
            gamesPlayedByMode = nextGamesPlayedByMode,
            modeMasteryXpByMode = masteryResult.xpByMode,
            totalHits = nextTotalHits,
            totalMisses = nextTotalMisses,
            lifetimeMaxCombo = nextMaxCombo,
            lifetimeMaxFlawlessStreak = nextMaxFlawlessStreak,
            personalRecords = nextPersonalRecords,
            totalBossRoundHits = nextTotalBossRoundHits,
            totalUltraMomentHits = nextTotalUltraMomentHits,
            weeklyChallenge = weeklyChallenge,
            weeklyGoalBoard = weeklyGoalBoard,
            dailyMiniTournament = dailyMiniTournament,
            season = seasonWithQuestProgress,
            seasonHunterBadgeUnlocked = seasonHunterUnlocked,
            lastModeMasteryLevelUp = masteryResult.levelUp
        )

        return addSeasonXp(
            addXpWithLevelRewards(gameProgression, earnedXp),
            SEASON_XP_GAME_PLAYED,
            countGamePlayed = true
        ).let {
            updateAchievementProgress(
                progression = it,
                score = score,
                isNewBestScore = isNewBestScore
            )
        }
    }

    private fun personalRecordsBrokenByGame(
        progression: ProgressionState,
        bestScoresByMode: Map<GameMode, Int>,
        mode: GameMode,
        score: Int,
        maxCombo: Int,
        accuracyPercent: Int,
        survivalSeconds: Int,
        earnedCoins: Int
    ): Set<PersonalRecordType> {
        val records = mutableSetOf<PersonalRecordType>()
        if (score > progression.personalRecords.bestScore.coerceAtLeast(0)) {
            records += PersonalRecordType.HighestScore
        }
        if (maxCombo > progression.personalRecords.bestCombo.coerceAtLeast(0)) {
            records += PersonalRecordType.HighestCombo
        }
        if (accuracyPercent.coerceIn(0, 100) > progression.personalRecords.bestAccuracyPercent.coerceIn(0, 100)) {
            records += PersonalRecordType.BestAccuracy
        }
        if (survivalSeconds > progression.personalRecords.longestSurvivalSeconds.coerceAtLeast(0)) {
            records += PersonalRecordType.LongestSurvival
        }
        if (earnedCoins > progression.personalRecords.mostCoinsInGame.coerceAtLeast(0)) {
            records += PersonalRecordType.MostCoinsInGame
        }
        if (score > (bestScoresByMode[mode] ?: 0).coerceAtLeast(0)) {
            records += when (mode) {
                GameMode.Classic -> PersonalRecordType.ClassicBest
                GameMode.MovingTarget -> PersonalRecordType.MovingTargetBest
                GameMode.FakeTarget -> PersonalRecordType.FakeTargetBest
                GameMode.ColorReflex -> PersonalRecordType.ColorReflexBest
            }
        }
        return records
    }

    private fun accuracyPercent(hits: Int, attempts: Int): Int {
        val safeAttempts = attempts.coerceAtLeast(0)
        if (safeAttempts == 0) return 0
        return ((hits.coerceAtLeast(0) * 100f) / safeAttempts).toInt().coerceIn(0, 100)
    }

    private fun advanceModeMastery(
        progression: ProgressionState,
        mode: GameMode,
        score: Int,
        maxCombo: Int
    ): ModeMasteryAdvanceResult {
        val currentXp = progression.modeMasteryXpByMode[mode]?.coerceAtLeast(0) ?: 0
        val currentProgress = ModeMasteryProgress(currentXp)
        val earnedXp = calculateModeMasteryXp(score = score, maxCombo = maxCombo)
        val maxXp = (MODE_MASTERY_MAX_LEVEL - 1) * MODE_MASTERY_XP_PER_LEVEL
        val nextXp = (currentXp.toLong() + earnedXp.toLong())
            .coerceIn(0L, maxXp.toLong())
            .toInt()
        val nextProgress = ModeMasteryProgress(nextXp)
        val crossedLevels = ((currentProgress.level + 1)..nextProgress.level).toList()
        val coinBonus = crossedLevels.sumOf { level -> modeMasteryLevelReward(level) }
        return ModeMasteryAdvanceResult(
            xpByMode = progression.modeMasteryXpByMode + (mode to nextXp),
            coinBonus = coinBonus,
            levelUp = crossedLevels.lastOrNull()?.let { level ->
                ModeMasteryLevelUp(
                    mode = mode,
                    level = level,
                    coinBonus = coinBonus
                )
            }
        )
    }

    private fun calculateModeMasteryXp(score: Int, maxCombo: Int): Int {
        return 20 + score.coerceIn(0, 30) + maxCombo.coerceIn(0, 10)
    }

    private fun modeMasteryLevelReward(level: Int): Int {
        return when (level) {
            10 -> 750
            5 -> 300
            else -> 100
        }
    }

    private data class ModeMasteryAdvanceResult(
        val xpByMode: Map<GameMode, Int>,
        val coinBonus: Int,
        val levelUp: ModeMasteryLevelUp?
    )

    private fun advanceWeeklyChallenge(
        challenge: ChallengeState,
        mode: GameMode,
        score: Int,
        maxCombo: Int
    ): ChallengeState {
        if (challenge.claimed) return challenge
        val nextProgress = when (challenge.type) {
            WeeklyChallengeType.ClassicScore50 ->
                if (mode == GameMode.Classic) maxOf(challenge.progress, score) else challenge.progress
            WeeklyChallengeType.ColorReflexScore30 ->
                if (mode == GameMode.ColorReflex) maxOf(challenge.progress, score) else challenge.progress
            WeeklyChallengeType.FakeTargetScore20 ->
                if (mode == GameMode.FakeTarget) maxOf(challenge.progress, score) else challenge.progress
            WeeklyChallengeType.Play20Games ->
                challenge.progress + 1
            WeeklyChallengeType.Combo10 ->
                maxOf(challenge.progress, maxCombo)
        }.coerceIn(0, challenge.target)
        return challenge.copy(
            progress = nextProgress,
            completed = nextProgress >= challenge.target
        )
    }

    private fun advanceWeeklyGoalBoardAfterGame(
        board: WeeklyGoalBoardState,
        score: Int,
        maxCombo: Int
    ): WeeklyGoalBoardState {
        val freshBoard = gamePreferences.getProgressionState().weeklyGoalBoard
        val currentBoard = if (board.weekKey == freshBoard.weekKey) board else freshBoard
        val updatedGoals = currentBoard.goals.map { goal ->
            val nextProgress = when (goal.type) {
                WeeklyGoalType.Play20Games -> goal.progress + 1
                WeeklyGoalType.Score500 -> goal.progress + score.coerceAtLeast(0)
                WeeklyGoalType.Combo10FiveTimes -> goal.progress + if (maxCombo >= 10) 1 else 0
            }.coerceIn(0, goal.target)
            val shouldClaimReward = nextProgress >= goal.target && !goal.claimed
            goal.copy(
                progress = nextProgress,
                claimed = goal.claimed || shouldClaimReward,
                rewardClaimedThisGame = shouldClaimReward
            )
        }
        val shouldClaimBonus = updatedGoals.all { it.completed } && !currentBoard.bonusClaimed
        return currentBoard.copy(
            goals = updatedGoals,
            bonusClaimed = currentBoard.bonusClaimed || shouldClaimBonus,
            bonusUnlockedThisGame = shouldClaimBonus
        )
    }

    private fun advanceSeasonQuestsAfterGame(
        season: SeasonState,
        score: Int,
        maxCombo: Int,
        theme: PlayerTheme,
        targetSkin: TargetSkin
    ): SeasonState {
        val currentSeason = seasonForToday(season)
        val usedCosmetics = currentSeason.usedCosmeticKeys +
            "theme:${theme.storageKey}" +
            "skin:${targetSkin.storageKey}"
        val updatedQuests = currentSeason.quests.map { quest ->
            val nextProgress = when (quest.type) {
                SeasonQuestType.Play100Games -> quest.progress + 1
                SeasonQuestType.Score3000 -> quest.progress + score.coerceAtLeast(0)
                SeasonQuestType.Combo10TwentyFiveTimes -> quest.progress + if (maxCombo >= 10) 1 else 0
                SeasonQuestType.Complete10DailyMissions -> quest.progress
                SeasonQuestType.Use5Cosmetics -> usedCosmetics.size
            }.coerceIn(0, quest.target)
            val shouldClaimReward = nextProgress >= quest.target && !quest.claimed
            quest.copy(
                progress = nextProgress,
                claimed = quest.claimed || shouldClaimReward,
                rewardClaimedThisGame = shouldClaimReward
            )
        }
        return currentSeason.copy(
            quests = updatedQuests,
            usedCosmeticKeys = usedCosmetics
        )
    }

    private fun advanceSeasonQuestForDailyMissionClaim(progression: ProgressionState): ProgressionState {
        val season = seasonForToday(progression.season)
        val updatedQuests = season.quests.map { quest ->
            if (quest.type != SeasonQuestType.Complete10DailyMissions) {
                return@map quest.copy(rewardClaimedThisGame = false)
            }
            val nextProgress = (quest.progress + 1).coerceIn(0, quest.target)
            val shouldClaimReward = nextProgress >= quest.target && !quest.claimed
            quest.copy(
                progress = nextProgress,
                claimed = quest.claimed || shouldClaimReward,
                rewardClaimedThisGame = shouldClaimReward
            )
        }
        val updatedSeason = season.copy(quests = updatedQuests)
        val seasonHunterUnlocked = progression.seasonHunterBadgeUnlocked ||
            updatedSeason.seasonQuestsCompleted
        return addCoins(
            progression.copy(
                season = updatedSeason,
                seasonHunterBadgeUnlocked = seasonHunterUnlocked
            ),
            updatedSeason.seasonQuestRewardCoinsThisGame
        )
    }

    private fun recordRewardedAdWatched(progression: ProgressionState): ProgressionState {
        lastRewardedAdElapsedMillis = SystemClock.elapsedRealtime()
        return addSeasonXp(updateAchievementProgress(
            addXpWithLevelRewards(
                progression.copy(
                    rewardedAdWatchCount = progression.rewardedAdWatchCount + 1
                ),
                REWARDED_AD_XP_REWARD
            )
        ), SEASON_XP_REWARDED_AD, countRewardedAd = true)
    }

    private fun logNewAchievementUnlocks(progression: ProgressionState) {
        if (progression.latestUnlockedAchievementIds.isEmpty()) return
        progression.achievements
            .filter { it.id in progression.latestUnlockedAchievementIds }
            .forEach { achievement ->
                FirebaseGameServices.logEvent(
                    event = FirebaseEvent.AchievementUnlocked,
                    params = Bundle().apply {
                        putString(FirebaseParam.AchievementId.key, achievement.id)
                        putInt(FirebaseParam.CoinAmount.key, achievement.rewardCoins)
                    }
                )
            }
    }

    private fun addXpWithLevelRewards(
        progression: ProgressionState,
        xpAmount: Int
    ): ProgressionState {
        val previousLevel = progression.level
        val previousRank = rankFor(score = 0, level = previousLevel)
        val nextXp = (progression.xp + xpAmount.coerceAtLeast(0)).coerceAtLeast(0)
        val nextLevel = calculateProgressionLevel(nextXp)
        val gainedLevels = (nextLevel - previousLevel).coerceAtLeast(0)
        val levelBonusCoins = gainedLevels * LEVEL_UP_COIN_BONUS
        val nextRank = rankFor(score = 0, level = nextLevel)

        if (gainedLevels > 0) {
            FirebaseGameServices.logEvent(
                event = FirebaseEvent.LevelUp,
                params = Bundle().apply {
                    putInt(FirebaseParam.Level.key, nextLevel)
                    putInt(FirebaseParam.CoinAmount.key, levelBonusCoins)
                }
            )
        }
        if (nextRank != previousRank) {
            FirebaseGameServices.logEvent(
                event = FirebaseEvent.RankChanged,
                params = Bundle().apply {
                    putString(FirebaseParam.RankName.key, nextRank.name)
                    putInt(FirebaseParam.Level.key, nextLevel)
                }
            )
        }

        return progression.copy(
            coins = progression.coins + levelBonusCoins,
            xp = nextXp,
            level = nextLevel,
            lastLevelUp = nextLevel.takeIf { gainedLevels > 0 }
        )
    }

    private fun randomCoinChestReward(): Int {
        val roll = Random.nextInt(100)
        return when {
            roll < 40 -> 50
            roll < 68 -> 75
            roll < 86 -> 100
            roll < 96 -> 150
            else -> 250
        }
    }

    private fun refreshDailyFeaturedMode(): GameUiState {
        return _uiState.updateAndGet { state ->
            val today = todayDateKey()
            if (state.dailyFeaturedMode.dateKey == today) {
                state
            } else {
                state.copy(dailyFeaturedMode = createDailyFeaturedMode(today))
            }
        }
    }

    private fun startBonusHourTicker() {
        bonusHourJob?.cancel()
        bonusHourJob = viewModelScope.launch {
            while (true) {
                refreshBonusHourState()
                delay(60_000L)
            }
        }
    }

    private fun refreshBonusHourState() {
        val bonusHour = gamePreferences.getBonusHourState()
        val freshDailyMiniTournament = gamePreferences.getDailyMiniTournamentState()
        _uiState.update { state ->
            val dailyMiniTournament = if (state.isGameOver) {
                state.progressionState.dailyMiniTournament
            } else {
                freshDailyMiniTournament
            }
            if (
                state.progressionState.bonusHour == bonusHour &&
                state.progressionState.dailyMiniTournament == dailyMiniTournament
            ) {
                state
            } else {
                state.copy(
                    progressionState = state.progressionState.copy(
                        bonusHour = bonusHour,
                        dailyMiniTournament = dailyMiniTournament
                    )
                )
            }
        }
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
        }.let(::safeScore)
        return leaderboardRepository.getLocalLeaderboard(
            playerName = safePlayerName(profile),
            playerScore = leaderboardScore,
            playerTheme = safeTheme(progression),
            playerRankTier = rankFor(score = leaderboardScore, level = progression.level),
            selectedMode = mode,
            selectedPeriod = selectedLeaderboardPeriod,
            refreshTick = leaderboardRefreshTick
        )
    }

    private fun loadRemoteLeaderboard(showLoading: Boolean) {
        if (leaderboardRefreshJob?.isActive == true) return
        leaderboardRefreshJob = viewModelScope.launch {
            val state = _uiState.value
            val profile = gamePreferences.getPlayerProfile()
            val mode = selectedLeaderboardMode
            val leaderboardScore = when (selectedLeaderboardPeriod) {
                LeaderboardPeriod.Weekly -> profile.weeklyBestScoresByMode[mode] ?: 0
                LeaderboardPeriod.AllTime -> state.bestScoresByMode[mode] ?: 0
            }.let(::safeScore)
            val snapshot = leaderboardRepository.refreshLeaderboard(
                playerName = safePlayerName(profile),
                playerScore = leaderboardScore,
                playerTheme = safeTheme(state.progressionState),
                playerRankTier = rankFor(score = leaderboardScore, level = state.progressionState.level),
                playerLevel = state.progressionState.level,
                selectedMode = mode,
                selectedPeriod = selectedLeaderboardPeriod,
                refreshTick = leaderboardRefreshTick
            ).copy(isLoading = false)

            val currentState = _uiState.value
            val updatedProgression = updateDailyLeaderboardGoalFromSnapshot(
                progression = currentState.progressionState,
                snapshot = snapshot
            )
            if (updatedProgression != currentState.progressionState) {
                gamePreferences.saveProgressionState(updatedProgression)
            }
            _uiState.update {
                it.copy(
                    playerProfile = profile,
                    progressionState = updatedProgression,
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
        val rewardedCooldownPassed = lastRewardedAdElapsedMillis == 0L ||
            now - lastRewardedAdElapsedMillis >= adConfig.interstitialCooldownMillis
        val isShortGame = gameDurationMillis < adConfig.shortGameThresholdMillis ||
            score <= adConfig.shortGameScoreThreshold
        val isHighValueRun = isNewBestScore ||
            (bestScore > 0 && score >= (bestScore * adConfig.highScoreDelayRatio).toInt())

        return completedGameCount > adConfig.firstInterstitialFreeGames &&
            completedGameCount >= nextInterstitialGameCount &&
            cooldownPassed &&
            rewardedCooldownPassed &&
            !isShortGame &&
            !isHighValueRun
    }

    private fun applyDailyReward(
        progression: ProgressionState
    ): ProgressionState {
        val reward = progression.dailyReward
        if (reward.claimedToday) return progression
        val nextStreakDay = reward.streakDay + 1
        val rewardCycleSize = DailyRewardCoinPlan.size.coerceAtLeast(1)
        val nextDayInCycle = ((nextStreakDay - 1) % rewardCycleSize) + 1
        val nextRewardCoins = DailyRewardCoinPlan
            .getOrElse(nextDayInCycle - 1) { reward.nextRewardCoins }
            .coerceAtLeast(0)

        val rewardedProgression = addCoins(progression, reward.rewardCoins).copy(
            dailyReward = reward.copy(
                canClaim = false,
                claimedToday = true,
                canProtectStreak = false,
                isStreakAtRisk = false,
                nextRewardCoins = nextRewardCoins,
                loyalBadgeUnlocked = reward.loyalBadgeUnlocked || reward.streakDay >= 30
            )
        )

        return addSeasonXp(
            addXpWithLevelRewards(rewardedProgression, if (reward.isSuperReward) 75 else 25),
            SEASON_XP_DAILY_STREAK
        )
    }

    private fun moveTargets() {
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        // Randomised positions are generated once, outside the update{} lambda.
        val movedTargets = currentState.targets.map { target ->
            target.copy(
                position = targetEngine.generateRandomTargetPosition(
                    currentX = target.position.xFraction,
                    currentY = target.position.yFraction
                )
            )
        }
        _uiState.value = currentState.copy(
            targets = movedTargets,
            targetPosition = movedTargets.firstCorrectPosition()
        )
    }

    private fun rotateColorRule() {
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

        val newColor = targetEngine.nextColorRule(currentState.activeColor)
        // generateTargets() is randomised, so it must not run inside an update{} lambda.
        _uiState.value = currentState.let {
            val nextTargets = targetEngine.generateTargets(
                mode = GameMode.ColorReflex,
                score = it.score,
                currentTargets = it.targets,
                activeColor = newColor,
                progression = it.progressionState
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

    private fun randomInterstitialInterval(): Int {
        return Random.nextInt(
            from = adConfig.interstitialMinGameInterval,
            until = adConfig.interstitialMaxGameInterval + 1
        )
    }

    override fun onCleared() {
        super.onCleared()
        cancelGameplayJobs()
    }
}
