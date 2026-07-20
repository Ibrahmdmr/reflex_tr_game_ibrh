package com.reflex.tr.game.ibrh.ui.game

import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reflex.tr.game.ibrh.BuildConfig
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.AdAnalyticsTracker
import com.reflex.tr.game.ibrh.ads.AdConfig
import com.reflex.tr.game.ibrh.ads.adParams
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
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
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val INITIAL_LIVES = 3
        private const val INITIAL_TIME_SECONDS = 30
        private const val REWARD_CONTINUE_GRACE_MILLIS = 2_000L
        private const val REWARDED_CALLBACK_DEDUPE_MILLIS = 2_000L
        private const val COLOR_RULE_CHANGE_INTERVAL_MS = 5_000L
        private const val COMBO_WINDOW_MILLIS = 1_250L
        private const val LEVEL_UP_COIN_BONUS = 50
        private const val REWARDED_AD_XP_REWARD = 20
        private const val DAILY_CHALLENGE_XP_REWARD = 60
        private const val SEASON_XP_GAME_PLAYED = 35
        private const val SEASON_XP_CHALLENGE_COMPLETED = 90
        private const val SEASON_XP_DAILY_STREAK = 70
        private const val SEASON_XP_ACHIEVEMENT_CLAIM = 120
        private const val SEASON_XP_REWARDED_AD = 30
        private const val FALLBACK_PLAYER_NAME = "Oyuncu"
        private val REASON_TIME_UP = R.string.game_over_reason_time_up
        private val REASON_NO_LIVES = R.string.game_over_reason_no_lives
    }

    private val adConfig = AdConfig.Default
    private val gamePreferences = GamePreferences(application)
    private val leaderboardRepository: LeaderboardRepository = FirestoreLeaderboardRepository()
    private val targetEngine = GameTargetEngine()
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
    private var leaderboardRefreshJob: Job? = null
    private var completedGameCount = 0
    private var nextInterstitialGameCount = randomInterstitialInterval()
    private var lastHitElapsedMillis = 0L
    private var gameStartedElapsedMillis = 0L
    private var lastInterstitialShownElapsedMillis = 0L
    private var lastRewardedAdElapsedMillis = 0L
    private var lastRewardedGrantAction: RewardedAction? = null
    private var lastRewardedGrantElapsedMillis = 0L

    init {
        observeBestScore()
        refreshProfileAndLeaderboard()
    }

    fun setStorePreviewMode(enabled: Boolean) {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch {
            gamePreferences.saveStorePreviewModeEnabled(enabled)
        }
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
            coins = state.progressionState.coins - boost.coinPrice
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
                score = newScore,
                combo = nextCombo
            )
            val nextTargets = targetEngine.generateTargets(
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
                targetSizeDp = calculateTargetSizeDp(
                    score = newScore,
                    mode = it.selectedMode,
                    progression = it.progressionState
                ),
                targetVisibleDurationMillis = calculateTargetVisibleDurationMillis(
                    score = newScore,
                    mode = it.selectedMode,
                    progression = it.progressionState
                ),
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
        logChallengeCompletedIfNeeded(
            previous = currentState.dailyChallengeState,
            updated = updatedState.dailyChallengeState
        )
        startComboResetTimer(expectedCombo = nextCombo)
        startTargetTimeout()
    }

    fun onMissTapped() {
        if (isStorePreviewModeActive()) return
        val currentState = _uiState.value
        if (!currentState.canAcceptGameplayInput()) return

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

    fun onLeaderboardOpenedForMission() {
        if (isStorePreviewModeActive()) return
        advanceDailyChallengeForVisit(DailyChallenge.OpenLeaderboard)
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
        val totalCoins = progression.coins.toLong() + coins.coerceAtLeast(0).toLong()
        return progression.copy(coins = totalCoins.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt())
    }

    private fun safePlayerName(profile: PlayerProfile): String {
        return profile.name.trim().takeIf { it.isNotBlank() } ?: FALLBACK_PLAYER_NAME
    }

    private fun safeTheme(progression: ProgressionState): PlayerTheme {
        return progression.activeTheme.takeIf {
            it == PlayerTheme.NeonRed || it in progression.unlockedThemes || it == progression.trialTheme
        } ?: PlayerTheme.NeonRed
    }

    private fun safeScore(score: Int): Int {
        return score.coerceAtLeast(0)
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

    private fun launchNewGame(mode: GameMode, boost: GameBoost? = null) {
        cancelGameplayJobs()
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
            activeBoost = boost,
            lives = initialState.lives + if (boost == GameBoost.ExtraLife) 1 else 0,
            timeLeftSeconds = initialState.timeLeftSeconds + if (boost == GameBoost.ExtraTime) 5 else 0,
            combo = if (boost == GameBoost.ComboStart) 5 else initialState.combo,
            maxCombo = if (boost == GameBoost.ComboStart) 5 else initialState.maxCombo,
            earnedCoinsThisGame = 0,
            baseCoinsThisGame = 0,
            isCoinDoubleClaimed = false,
            oneMoreGameBonusEarnedThisGame = 0,
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
        cancelGameplayJobs()
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
            val nextTargets = targetEngine.generateTargets(
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
        val finalState = completeGameState(
            lives = lives,
            timeLeftSeconds = timeLeftSeconds,
            reason = reason,
            reasonRes = reasonRes,
            shouldRequestInterstitialAd = shouldRequestInterstitialAd
        )

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
            val updatedProgression = updateProgressionAfterGame(
                progression = progressionBeforeGame,
                score = finalScore,
                hits = state.successfulHits,
                maxCombo = state.maxCombo,
                earnedCoins = rewards.totalCoins,
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
                progressionState = trialAwareProgression,
                playerProfile = updatedProfile,
                leaderboardSnapshot = updatedLeaderboard,
                activeBoost = null,
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
        val totalCoins: Int
    )

    private fun progressionForGameCompletion(currentProgression: ProgressionState): ProgressionState {
        val progressionFromStorage = gamePreferences.getProgressionState()
        return currentProgression.copy(
            dailyReward = progressionFromStorage.dailyReward,
            achievements = progressionFromStorage.achievements,
            weeklyChallenge = progressionFromStorage.weeklyChallenge,
            oneMoreGameBonus = progressionFromStorage.oneMoreGameBonus,
            firstTargetBonusClaimed = progressionFromStorage.firstTargetBonusClaimed
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

        return CompletedGameRewards(
            baseCoins = baseCoins,
            firstTargetBonusCoins = firstTargetBonusCoins,
            oneMoreBonusCoins = oneMoreBonusCoins,
            totalCoins = baseCoins + firstTargetBonusCoins + oneMoreBonusCoins + dailyModeBonusCoins
        )
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
        val activeColor = if (mode == GameMode.ColorReflex) {
            targetEngine.randomTargetColor()
        } else {
            ReflexTargetColor.Red
        }
        val targets = targetEngine.generateTargets(
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

        lastHitElapsedMillis = 0L
        val remainingLives = currentState.lives - 1
        if (remainingLives <= 0) {
            endGame(countAttempt = countAttempt)
            return
        }

        _uiState.update {
            val nextTargets = targetEngine.generateTargets(
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
                    delay(targetEngine.calculateMovementIntervalMillis(_uiState.value.score))
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
        hits: Int,
        maxCombo: Int,
        earnedCoins: Int,
        isNewBestScore: Boolean
    ): ProgressionState {
        val earnedXp = calculateEarnedXp(score, hits, maxCombo, isNewBestScore)
        val nextTotalGames = progression.totalGames + 1
        val nextTotalHits = progression.totalHits + hits
        val nextMaxCombo = maxOf(progression.lifetimeMaxCombo, maxCombo)
        val weeklyProgress = (progression.weeklyChallenge.progress + score)
            .coerceAtMost(progression.weeklyChallenge.target)
        val weeklyChallenge = progression.weeklyChallenge.copy(
            progress = weeklyProgress,
            completed = weeklyProgress >= progression.weeklyChallenge.target
        )

        val gameProgression = addCoins(progression, earnedCoins).copy(
            totalGames = nextTotalGames,
            totalHits = nextTotalHits,
            lifetimeMaxCombo = nextMaxCombo,
            weeklyChallenge = weeklyChallenge
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
        leaderboardRefreshJob?.cancel()
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

        _uiState.update {
            val movedTargets = it.targets.map { target ->
                target.copy(
                    position = targetEngine.generateRandomTargetPosition(
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

        val newColor = targetEngine.nextColorRule(currentState.activeColor)
        _uiState.update {
            val nextTargets = targetEngine.generateTargets(
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
