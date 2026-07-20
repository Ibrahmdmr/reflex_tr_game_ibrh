package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val GAME_PREFERENCES_NAME = "game_preferences"
private const val BEST_SCORE_KEY = "best_score"
private const val LANGUAGE_KEY = "language"
private const val SOUND_ENABLED_KEY = "sound_enabled"
private const val EFFECT_SOUND_ENABLED_KEY = "effect_sound_enabled"
private const val VIBRATION_ENABLED_KEY = "vibration_enabled"
private const val NOTIFICATION_DAILY_REWARD_KEY = "notification_daily_reward"
private const val NOTIFICATION_STREAK_KEY = "notification_streak"
private const val NOTIFICATION_NEW_MISSION_KEY = "notification_new_mission"
private const val ONBOARDING_COMPLETED_KEY = "onboarding_completed"
private const val FIRST_TARGET_BONUS_CLAIMED_KEY = "first_target_bonus_claimed"
private const val STORE_PREVIEW_MODE_KEY = "store_preview_mode"
private const val FIRST_OPEN_DATE_KEY = "first_open_date"
private const val REVIEW_LAST_PROMPT_TIME_KEY = "review_last_prompt_time"
private const val REVIEW_AUTO_COMPLETED_KEY = "review_auto_completed"
private const val LANGUAGE_TURKISH = "tr"
private const val LANGUAGE_ENGLISH = "en"
private const val DAILY_CHALLENGE_ID_KEY = "daily_challenge_id"
private const val DAILY_CHALLENGE_TYPE_KEY = "daily_challenge_type"
private const val DAILY_CHALLENGE_TARGET_KEY = "daily_challenge_target"
private const val DAILY_CHALLENGE_PROGRESS_KEY = "daily_challenge_progress"
private const val DAILY_CHALLENGE_COMPLETED_KEY = "daily_challenge_completed"
private const val DAILY_CHALLENGE_CREATED_DATE_KEY = "daily_challenge_created_date"
private const val DAILY_CHALLENGE_REWARD_CLAIMED_KEY = "daily_challenge_reward_claimed"
private const val DAILY_CHALLENGE_DOUBLE_REWARD_CLAIMED_KEY = "daily_challenge_double_reward_claimed"
private const val DAILY_CHALLENGE_REWARD_COINS_KEY = "daily_challenge_reward_coins"
private const val COINS_KEY = "coins"
private const val XP_KEY = "xp"
private const val TOTAL_GAMES_KEY = "total_games"
private const val TOTAL_HITS_KEY = "total_hits"
private const val LIFETIME_MAX_COMBO_KEY = "lifetime_max_combo"
private const val REWARDED_AD_WATCH_COUNT_KEY = "rewarded_ad_watch_count"
private const val SELECTED_THEME_KEY = "selected_theme"
private const val UNLOCKED_THEMES_KEY = "unlocked_themes"
private const val DAILY_REWARD_LAST_CLAIM_DATE_KEY = "daily_reward_last_claim_date"
private const val DAILY_DIALOG_LAST_SHOWN_DATE_KEY = "daily_dialog_last_shown_date"
private const val DAILY_REWARD_STREAK_KEY = "daily_reward_streak"
private const val COIN_CHEST_OPEN_DATE_KEY = "coin_chest_open_date"
private const val COIN_CHEST_OPEN_COUNT_KEY = "coin_chest_open_count"
private const val COIN_CHEST_LAST_REWARD_KEY = "coin_chest_last_reward"
private const val SHOP_COIN_REWARD_DATE_KEY = "shop_coin_reward_date"
private const val SHOP_COIN_REWARD_COUNT_KEY = "shop_coin_reward_count"
private const val ONE_MORE_GAME_BONUS_DATE_KEY = "one_more_game_bonus_date"
private const val ONE_MORE_GAME_BONUS_PLAYED_COUNT_KEY = "one_more_game_bonus_played_count"
private const val ONE_MORE_GAME_BONUS_CLAIMED_KEY = "one_more_game_bonus_claimed"
private const val ACHIEVEMENT_CLAIMED_IDS_KEY = "achievement_claimed_ids"
private const val WEEKLY_CHALLENGE_PROGRESS_KEY = "weekly_challenge_progress"
private const val WEEKLY_CHALLENGE_CREATED_DATE_KEY = "weekly_challenge_created_date"
private const val SEASON_NUMBER_KEY = "season_number"
private const val SEASON_START_DATE_KEY = "season_start_date"
private const val SEASON_XP_KEY = "season_xp"
private const val SEASON_CLAIMED_LEVELS_KEY = "season_claimed_levels"
private const val SEASON_BADGE_LEVELS_KEY = "season_badge_levels"
private const val SEASON_XP_BOOST_END_TIME_KEY = "season_xp_boost_end_time"
private const val SEASON_MISSION_DATE_KEY = "season_mission_date"
private const val SEASON_MISSION_PLAY_COUNT_KEY = "season_mission_play_count"
private const val SEASON_MISSION_REWARDED_COUNT_KEY = "season_mission_rewarded_count"
private const val SEASON_MISSION_XP_EARNED_KEY = "season_mission_xp_earned"
private const val SEASON_MISSION_CLAIMED_IDS_KEY = "season_mission_claimed_ids"
private const val PLAYER_NAME_KEY = "player_name"
private const val PLAYER_NAME_PROMPT_COMPLETED_KEY = "player_name_prompt_completed"
private const val PLAYER_TITLE_KEY = "player_title"
private const val PLAYER_WEEKLY_SCORE_KEY = "player_weekly_score"
private const val PLAYER_WEEKLY_SCORE_DATE_KEY = "player_weekly_score_date"
private const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
private const val REVIEW_COOLDOWN_MILLIS = 14L * DAY_IN_MILLIS
private const val DEFAULT_PLAYER_NAME = "Oyuncu"
private const val DATE_PATTERN = "yyyy-MM-dd"
private const val PROGRESSION_XP_PER_LEVEL = 250

enum class AppLanguage(val code: String) {
    Turkish(LANGUAGE_TURKISH),
    English(LANGUAGE_ENGLISH)
}

class GamePreferences(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences(
        GAME_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    init {
        migrateGlobalBestScoreToClassic()
        markFirstOpenDateIfNeeded()
    }

    private val bestScoresState = MutableStateFlow(loadBestScores())
    private val languageState = MutableStateFlow(loadLanguage())
    private val soundEnabledState = MutableStateFlow(loadSoundEnabled())
    private val effectSoundEnabledState = MutableStateFlow(loadEffectSoundEnabled())
    private val vibrationEnabledState = MutableStateFlow(loadVibrationEnabled())
    private val dailyRewardNotificationState = MutableStateFlow(loadDailyRewardNotificationEnabled())
    private val streakNotificationState = MutableStateFlow(loadStreakNotificationEnabled())
    private val newMissionNotificationState = MutableStateFlow(loadNewMissionNotificationEnabled())
    private val onboardingCompletedState = MutableStateFlow(loadOnboardingCompleted())
    private val storePreviewModeState = MutableStateFlow(loadStorePreviewModeEnabled())

    val bestScoresFlow: Flow<Map<GameMode, Int>> = bestScoresState.asStateFlow()
    val languageFlow: Flow<AppLanguage> = languageState.asStateFlow()
    val soundEnabledFlow: Flow<Boolean> = soundEnabledState.asStateFlow()
    val effectSoundEnabledFlow: Flow<Boolean> = effectSoundEnabledState.asStateFlow()
    val vibrationEnabledFlow: Flow<Boolean> = vibrationEnabledState.asStateFlow()
    val dailyRewardNotificationFlow: Flow<Boolean> = dailyRewardNotificationState.asStateFlow()
    val streakNotificationFlow: Flow<Boolean> = streakNotificationState.asStateFlow()
    val newMissionNotificationFlow: Flow<Boolean> = newMissionNotificationState.asStateFlow()
    val onboardingCompletedFlow: Flow<Boolean> = onboardingCompletedState.asStateFlow()
    val storePreviewModeFlow: Flow<Boolean> = storePreviewModeState.asStateFlow()

    suspend fun saveBestScore(mode: GameMode, score: Int) {
        val key = mode.bestScorePreferenceKey()
        val currentBestScore = sharedPreferences.getInt(key, 0)
        val safeScore = score.coerceAtLeast(0)
        if (safeScore <= currentBestScore) return

        sharedPreferences.edit()
            .putInt(key, safeScore)
            .commitSafely()

        bestScoresState.value = loadBestScores()
    }

    suspend fun saveLanguage(language: AppLanguage) {
        sharedPreferences.edit()
            .putString(LANGUAGE_KEY, language.code)
            .commitSafely()

        languageState.value = language
    }

    suspend fun saveSoundEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(SOUND_ENABLED_KEY, enabled)
            .commitSafely()

        soundEnabledState.value = enabled
    }

    suspend fun saveEffectSoundEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(EFFECT_SOUND_ENABLED_KEY, enabled)
            .commitSafely()

        effectSoundEnabledState.value = enabled
    }

    suspend fun saveVibrationEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(VIBRATION_ENABLED_KEY, enabled)
            .commitSafely()

        vibrationEnabledState.value = enabled
    }

    suspend fun saveDailyRewardNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(NOTIFICATION_DAILY_REWARD_KEY, enabled)
            .commitSafely()

        dailyRewardNotificationState.value = enabled
    }

    suspend fun saveStreakNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(NOTIFICATION_STREAK_KEY, enabled)
            .commitSafely()

        streakNotificationState.value = enabled
    }

    suspend fun saveNewMissionNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(NOTIFICATION_NEW_MISSION_KEY, enabled)
            .commitSafely()

        newMissionNotificationState.value = enabled
    }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit()
            .putBoolean(ONBOARDING_COMPLETED_KEY, completed)
            .commitSafely()

        onboardingCompletedState.value = completed
    }

    suspend fun saveStorePreviewModeEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(STORE_PREVIEW_MODE_KEY, enabled)
            .commitSafely()

        storePreviewModeState.value = enabled
    }

    fun isStorePreviewModeEnabled(): Boolean {
        return storePreviewModeState.value
    }

    fun getDailyChallengeState(): DailyChallengeState {
        val today = todayDateKey()
        val savedDate = sharedPreferences.getString(DAILY_CHALLENGE_CREATED_DATE_KEY, null)
        val savedType = sharedPreferences.getString(DAILY_CHALLENGE_TYPE_KEY, null)
        val type = dailyChallengeTypeFromName(savedType)

        if (savedDate == today && type != null) {
            return DailyChallengeState(
                id = sharedPreferences.getString(DAILY_CHALLENGE_ID_KEY, null)
                    ?: createDailyChallengeId(today, type),
                type = type,
                target = sharedPreferences.getInt(DAILY_CHALLENGE_TARGET_KEY, type.targetValue),
                progress = sharedPreferences.getInt(DAILY_CHALLENGE_PROGRESS_KEY, 0),
                completed = sharedPreferences.getBoolean(DAILY_CHALLENGE_COMPLETED_KEY, false),
                createdDate = today,
                rewardCoins = sharedPreferences.getInt(DAILY_CHALLENGE_REWARD_COINS_KEY, type.rewardCoins),
                rewardClaimed = sharedPreferences.getBoolean(DAILY_CHALLENGE_REWARD_CLAIMED_KEY, false),
                doubleRewardClaimed = sharedPreferences.getBoolean(DAILY_CHALLENGE_DOUBLE_REWARD_CLAIMED_KEY, false)
            )
        }

        val nextType = chooseDailyChallengeType(previousType = type)
        val newState = DailyChallengeState(
            id = createDailyChallengeId(today, nextType),
            type = nextType,
            target = nextType.targetValue,
            progress = 0,
            completed = false,
            createdDate = today,
            rewardCoins = nextType.rewardCoins
        )
        saveDailyChallengeState(newState)
        return newState
    }

    fun saveDailyChallengeState(state: DailyChallengeState) {
        val safeTarget = state.target.coerceAtLeast(1)
        sharedPreferences.edit()
            .putString(DAILY_CHALLENGE_ID_KEY, state.id)
            .putString(DAILY_CHALLENGE_TYPE_KEY, state.type.name)
            .putInt(DAILY_CHALLENGE_TARGET_KEY, safeTarget)
            .putInt(DAILY_CHALLENGE_PROGRESS_KEY, state.progress.coerceIn(0, safeTarget))
            .putBoolean(DAILY_CHALLENGE_COMPLETED_KEY, state.completed)
            .putString(DAILY_CHALLENGE_CREATED_DATE_KEY, safeDateKeyOrToday(state.createdDate))
            .putInt(DAILY_CHALLENGE_REWARD_COINS_KEY, state.rewardCoins.coerceAtLeast(0))
            .putBoolean(DAILY_CHALLENGE_REWARD_CLAIMED_KEY, state.rewardClaimed)
            .putBoolean(DAILY_CHALLENGE_DOUBLE_REWARD_CLAIMED_KEY, state.doubleRewardClaimed)
            .commitSafely()
    }

    fun getProgressionState(): ProgressionState {
        val safeXp = safeProgressionXp(sharedPreferences.getInt(XP_KEY, 0))
        return ProgressionState(
            coins = sharedPreferences.getInt(COINS_KEY, 0).coerceAtLeast(0),
            xp = safeXp,
            level = calculateLevel(safeXp),
            totalGames = sharedPreferences.getInt(TOTAL_GAMES_KEY, 0).coerceAtLeast(0),
            totalHits = sharedPreferences.getInt(TOTAL_HITS_KEY, 0).coerceAtLeast(0),
            lifetimeMaxCombo = sharedPreferences.getInt(LIFETIME_MAX_COMBO_KEY, 0).coerceAtLeast(0),
            rewardedAdWatchCount = sharedPreferences.getInt(REWARDED_AD_WATCH_COUNT_KEY, 0).coerceAtLeast(0),
            selectedTheme = loadSelectedTheme(),
            unlockedThemes = loadUnlockedThemes(),
            coinChest = loadCoinChestState(),
            shopCoinReward = loadShopCoinRewardState(),
            oneMoreGameBonus = loadOneMoreGameBonusState(),
            dailyReward = loadDailyRewardState(),
            season = loadSeasonState(),
            achievements = loadAchievements(),
            weeklyChallenge = loadWeeklyChallenge(),
            firstTargetBonusClaimed = sharedPreferences.getBoolean(FIRST_TARGET_BONUS_CLAIMED_KEY, false)
        )
    }

    fun getPlayerProfile(): PlayerProfile {
        val weekKey = weekDateKey()
        val savedWeek = sharedPreferences.getString(PLAYER_WEEKLY_SCORE_DATE_KEY, null)
            ?.takeIf { isValidWeekKey(it) }
        val savedName = safePlayerName(sharedPreferences.getString(PLAYER_NAME_KEY, "").orEmpty())
        val weeklyScoresByMode = GameMode.entries.associateWith { mode ->
            if (savedWeek == weekKey) {
                sharedPreferences.getInt(mode.weeklyScorePreferenceKey(), 0)
            } else {
                0
            }
        }
        return PlayerProfile(
            name = savedName,
            title = PlayerTitle.entries.firstOrNull {
                it.name == sharedPreferences.getString(PLAYER_TITLE_KEY, PlayerTitle.ReflexHunter.name)
            } ?: PlayerTitle.ReflexHunter,
            weeklyBestScore = if (savedWeek == weekKey) {
                maxOf(sharedPreferences.getInt(PLAYER_WEEKLY_SCORE_KEY, 0), weeklyScoresByMode.values.maxOrNull() ?: 0)
            } else {
                0
            },
            weeklyBestScoresByMode = weeklyScoresByMode,
            hasCompletedNamePrompt = savedName != DEFAULT_PLAYER_NAME ||
                sharedPreferences.getBoolean(PLAYER_NAME_PROMPT_COMPLETED_KEY, false)
        )
    }

    fun savePlayerName(name: String) {
        sharedPreferences.edit()
            .putString(PLAYER_NAME_KEY, safePlayerName(name))
            .putBoolean(PLAYER_NAME_PROMPT_COMPLETED_KEY, true)
            .commitSafely()
    }

    fun savePlayerTitle(title: PlayerTitle) {
        sharedPreferences.edit()
            .putString(PLAYER_TITLE_KEY, title.name)
            .commitSafely()
    }

    fun saveWeeklyBestScore(mode: GameMode, score: Int) {
        val safeScore = score.coerceAtLeast(0)
        val weekKey = weekDateKey()
        val savedWeek = sharedPreferences.getString(PLAYER_WEEKLY_SCORE_DATE_KEY, null)
        val currentScore = if (savedWeek == weekKey) sharedPreferences.getInt(PLAYER_WEEKLY_SCORE_KEY, 0) else 0
        val currentModeScore = if (savedWeek == weekKey) {
            sharedPreferences.getInt(mode.weeklyScorePreferenceKey(), 0)
        } else {
            0
        }
        if (safeScore <= currentScore && safeScore <= currentModeScore) return

        sharedPreferences.edit()
            .putString(PLAYER_WEEKLY_SCORE_DATE_KEY, weekKey)
            .putInt(PLAYER_WEEKLY_SCORE_KEY, maxOf(safeScore, currentScore))
            .putInt(mode.weeklyScorePreferenceKey(), maxOf(safeScore, currentModeScore))
            .commitSafely()
    }

    fun saveProgressionState(state: ProgressionState) {
        if (loadStorePreviewModeEnabled()) return

        val oneMoreGameBonus = oneMoreGameBonusForToday(state.oneMoreGameBonus)
        val safeUnlockedThemes = safeUnlockedThemes(state.unlockedThemes, state.selectedTheme)
        val safeSelectedTheme = safeSelectedTheme(state.selectedTheme, safeUnlockedThemes)
        val safeWeeklyChallengeDate = state.weeklyChallenge.createdDate.takeIf { isValidWeekKey(it) } ?: weekDateKey()
        sharedPreferences.edit()
            .putInt(COINS_KEY, state.coins.coerceAtLeast(0))
            .putInt(XP_KEY, safeProgressionXp(state.xp))
            .putInt(TOTAL_GAMES_KEY, state.totalGames.coerceAtLeast(0))
            .putInt(TOTAL_HITS_KEY, state.totalHits.coerceAtLeast(0))
            .putInt(LIFETIME_MAX_COMBO_KEY, state.lifetimeMaxCombo.coerceAtLeast(0))
            .putInt(REWARDED_AD_WATCH_COUNT_KEY, state.rewardedAdWatchCount.coerceAtLeast(0))
            .putBoolean(FIRST_TARGET_BONUS_CLAIMED_KEY, state.firstTargetBonusClaimed)
            .putString(SELECTED_THEME_KEY, safeSelectedTheme.storageKey)
            .putString(
                UNLOCKED_THEMES_KEY,
                safeUnlockedThemes.joinToString(separator = ",") { it.storageKey }
            )
            .putString(
                ACHIEVEMENT_CLAIMED_IDS_KEY,
                state.achievements.filter { it.claimed }.joinToString(separator = ",") { it.id }
            )
            .putString(COIN_CHEST_OPEN_DATE_KEY, safeDateKeyOrBlank(state.coinChest.lastOpenedDate))
            .putInt(COIN_CHEST_OPEN_COUNT_KEY, safeCount(state.coinChest.openedToday, state.coinChest.maxOpensPerDay))
            .putInt(COIN_CHEST_LAST_REWARD_KEY, state.coinChest.lastRewardCoins.coerceAtLeast(0))
            .putString(SHOP_COIN_REWARD_DATE_KEY, safeDateKeyOrBlank(state.shopCoinReward.lastClaimDate))
            .putInt(SHOP_COIN_REWARD_COUNT_KEY, safeCount(state.shopCoinReward.claimedToday, state.shopCoinReward.maxClaimsPerDay))
            .putString(ONE_MORE_GAME_BONUS_DATE_KEY, oneMoreGameBonus.dateKey)
            .putInt(ONE_MORE_GAME_BONUS_PLAYED_COUNT_KEY, oneMoreGameBonus.gamesPlayedToday.coerceAtLeast(0))
            .putBoolean(ONE_MORE_GAME_BONUS_CLAIMED_KEY, oneMoreGameBonus.bonusClaimedToday)
            .putInt(WEEKLY_CHALLENGE_PROGRESS_KEY, safeCount(state.weeklyChallenge.progress, state.weeklyChallenge.target))
            .putString(WEEKLY_CHALLENGE_CREATED_DATE_KEY, safeWeeklyChallengeDate)
            .putInt(SEASON_NUMBER_KEY, state.season.seasonNumber.coerceAtLeast(1))
            .putString(SEASON_START_DATE_KEY, safeDateKeyOrToday(state.season.startDateKey))
            .putInt(SEASON_XP_KEY, safeSeasonXp(state.season.xp))
            .putString(SEASON_CLAIMED_LEVELS_KEY, safeSeasonLevels(state.season.claimedRewardLevels).joinToString(","))
            .putString(SEASON_BADGE_LEVELS_KEY, safeSeasonLevels(state.season.preservedBadgeLevels).joinToString(","))
            .putLong(SEASON_XP_BOOST_END_TIME_KEY, state.season.xpBoostEndTimeMillis.coerceAtLeast(0L))
            .putString(SEASON_MISSION_DATE_KEY, safeDateKeyOrToday(state.season.missionDateKey))
            .putInt(SEASON_MISSION_PLAY_COUNT_KEY, state.season.gamesPlayedToday.coerceAtLeast(0))
            .putInt(SEASON_MISSION_REWARDED_COUNT_KEY, state.season.rewardedAdsWatchedToday.coerceAtLeast(0))
            .putInt(SEASON_MISSION_XP_EARNED_KEY, state.season.seasonXpEarnedToday.coerceIn(0, maxSeasonXp()))
            .putString(SEASON_MISSION_CLAIMED_IDS_KEY, state.season.claimedMissionIds.joinToString(","))
            .commitSafely()
    }

    fun saveDailyRewardClaim(streakDay: Int) {
        val today = todayDateKey()
        sharedPreferences.edit()
            .putString(DAILY_REWARD_LAST_CLAIM_DATE_KEY, today)
            .putInt(DAILY_REWARD_STREAK_KEY, streakDay.coerceAtLeast(1))
            .commitSafely()
    }

    fun shouldShowDailyRewardDialog(state: DailyRewardState): Boolean {
        val today = todayDateKey()
        val lastShownDate = sharedPreferences.getString(DAILY_DIALOG_LAST_SHOWN_DATE_KEY, "").orEmpty()
        val lastClaimDate = sharedPreferences.getString(DAILY_REWARD_LAST_CLAIM_DATE_KEY, "").orEmpty()
        return state.canClaim && lastShownDate != today && lastClaimDate != today
    }

    fun markDailyRewardDialogShown() {
        sharedPreferences.edit()
            .putString(DAILY_DIALOG_LAST_SHOWN_DATE_KEY, todayDateKey())
            .commitSafely()
    }

    fun protectDailyRewardStreak() {
        sharedPreferences.edit()
            .putString(DAILY_REWARD_LAST_CLAIM_DATE_KEY, yesterdayDateKey())
            .commitSafely()
    }

    private fun migrateGlobalBestScoreToClassic() {
        val legacyBestScore = sharedPreferences.getInt(BEST_SCORE_KEY, 0)
        if (legacyBestScore <= 0) return

        val classicKey = GameMode.Classic.bestScorePreferenceKey()
        val classicBestScore = sharedPreferences.getInt(classicKey, 0)
        if (classicBestScore >= legacyBestScore) return

        sharedPreferences.edit()
            .putInt(classicKey, legacyBestScore)
            .commitSafely()
    }

    private fun loadBestScores(): Map<GameMode, Int> {
        return GameMode.entries.associateWith { mode ->
            sharedPreferences.getInt(mode.bestScorePreferenceKey(), 0)
        }
    }

    private fun loadLanguage(): AppLanguage {
        val savedCode = sharedPreferences.getString(LANGUAGE_KEY, LANGUAGE_TURKISH)
        return AppLanguage.entries.firstOrNull { it.code == savedCode } ?: AppLanguage.Turkish
    }

    private fun loadSoundEnabled(): Boolean {
        return sharedPreferences.getBoolean(SOUND_ENABLED_KEY, true)
    }

    private fun loadEffectSoundEnabled(): Boolean {
        return sharedPreferences.getBoolean(EFFECT_SOUND_ENABLED_KEY, true)
    }

    private fun loadVibrationEnabled(): Boolean {
        return sharedPreferences.getBoolean(VIBRATION_ENABLED_KEY, true)
    }

    private fun loadDailyRewardNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(NOTIFICATION_DAILY_REWARD_KEY, false)
    }

    private fun loadStreakNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(NOTIFICATION_STREAK_KEY, false)
    }

    private fun loadNewMissionNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(NOTIFICATION_NEW_MISSION_KEY, false)
    }

    private fun loadOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(ONBOARDING_COMPLETED_KEY, false)
    }

    private fun loadStorePreviewModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(STORE_PREVIEW_MODE_KEY, false)
    }

    fun isOnboardingCompleted(): Boolean {
        return loadOnboardingCompleted()
    }

    fun shouldRequestInAppReviewAfterGame(
        totalGames: Int,
        isNewBestScore: Boolean,
        score: Int,
        maxCombo: Int
    ): Boolean {
        if (sharedPreferences.getBoolean(REVIEW_AUTO_COMPLETED_KEY, false)) return false

        val lastPromptTime = sharedPreferences.getLong(REVIEW_LAST_PROMPT_TIME_KEY, 0L)
        val cooldownPassed = lastPromptTime == 0L ||
            System.currentTimeMillis() - lastPromptTime >= REVIEW_COOLDOWN_MILLIS
        if (!cooldownPassed) return false

        val hasEnoughGames = totalGames >= 5
        val hasReturnedAnotherDay = activeAppDays() >= 2
        val positiveGameOverMoment = score >= 10 || maxCombo >= 5 || isNewBestScore

        return hasEnoughGames || isNewBestScore || hasReturnedAnotherDay || positiveGameOverMoment
    }

    fun markInAppReviewRequested(autoCompleted: Boolean) {
        sharedPreferences.edit()
            .putLong(REVIEW_LAST_PROMPT_TIME_KEY, System.currentTimeMillis())
            .putBoolean(REVIEW_AUTO_COMPLETED_KEY, autoCompleted || sharedPreferences.getBoolean(REVIEW_AUTO_COMPLETED_KEY, false))
            .commitSafely()
    }

    private fun markFirstOpenDateIfNeeded() {
        if (sharedPreferences.contains(FIRST_OPEN_DATE_KEY)) return

        sharedPreferences.edit()
            .putString(FIRST_OPEN_DATE_KEY, todayDateKey())
            .commitSafely()
    }

    private fun activeAppDays(): Int {
        val firstOpenDate = sharedPreferences.getString(FIRST_OPEN_DATE_KEY, null) ?: return 1
        val formatter = dateFormatter()
        return runCatching {
            val first = formatter.parse(firstOpenDate) ?: Date()
            val today = formatter.parse(todayDateKey()) ?: Date()
            (((today.time - first.time) / DAY_IN_MILLIS).toInt() + 1).coerceAtLeast(1)
        }.getOrDefault(1)
    }

    private fun chooseDailyChallengeType(previousType: DailyChallenge?): DailyChallenge {
        val availableTypes = DailyChallenge.entries.filterNot { it == previousType }
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return availableTypes.getOrNull(dayOfYear % availableTypes.size.coerceAtLeast(1))
            ?: DailyChallenge.ClassicScore20
    }

    private fun dailyChallengeTypeFromName(name: String?): DailyChallenge? {
        return when (name) {
            "Score20" -> DailyChallenge.ClassicScore20
            "FakeTarget10" -> DailyChallenge.FakeTargetScore5
            else -> DailyChallenge.entries.firstOrNull { it.name == name }
        }
    }

    private fun todayDateKey(): String {
        return dateFormatter().format(Calendar.getInstance().time)
    }

    private fun loadSelectedTheme(): PlayerTheme {
        val savedKey = sharedPreferences.getString(SELECTED_THEME_KEY, PlayerTheme.NeonRed.storageKey)
        return PlayerTheme.entries.firstOrNull { it.storageKey == savedKey } ?: PlayerTheme.NeonRed
    }

    private fun loadUnlockedThemes(): Set<PlayerTheme> {
        val saved = sharedPreferences.getString(UNLOCKED_THEMES_KEY, null)
        val unlocked = saved
            ?.split(",")
            ?.mapNotNull { key -> PlayerTheme.entries.firstOrNull { it.storageKey == key } }
            ?.toSet()
            .orEmpty()
        return unlocked + PlayerTheme.NeonRed
    }

    private fun loadCoinChestState(): CoinChestState {
        val today = todayDateKey()
        val savedDate = sharedPreferences.getString(COIN_CHEST_OPEN_DATE_KEY, "").orEmpty()
        val openedToday = if (savedDate == today) {
            sharedPreferences.getInt(COIN_CHEST_OPEN_COUNT_KEY, 0)
        } else {
            0
        }
        val lastReward = if (savedDate == today) {
            sharedPreferences.getInt(COIN_CHEST_LAST_REWARD_KEY, 0)
        } else {
            0
        }
        return CoinChestState(
            openedToday = openedToday.coerceIn(0, 3),
            lastOpenedDate = savedDate.takeIf { it == today }.orEmpty(),
            lastRewardCoins = lastReward.coerceAtLeast(0)
        )
    }

    private fun loadShopCoinRewardState(): ShopCoinRewardState {
        val today = todayDateKey()
        val savedDate = sharedPreferences.getString(SHOP_COIN_REWARD_DATE_KEY, "").orEmpty()
        val claimedToday = if (savedDate == today) {
            sharedPreferences.getInt(SHOP_COIN_REWARD_COUNT_KEY, 0)
        } else {
            0
        }
        return ShopCoinRewardState(
            claimedToday = claimedToday.coerceIn(0, 5),
            lastClaimDate = savedDate.takeIf { it == today }.orEmpty()
        )
    }

    private fun loadOneMoreGameBonusState(): OneMoreGameBonusState {
        val today = todayDateKey()
        val savedDate = sharedPreferences.getString(ONE_MORE_GAME_BONUS_DATE_KEY, "").orEmpty()
        if (savedDate != today) {
            return OneMoreGameBonusState(dateKey = today)
        }
        return OneMoreGameBonusState(
            dateKey = today,
            gamesPlayedToday = sharedPreferences.getInt(ONE_MORE_GAME_BONUS_PLAYED_COUNT_KEY, 0)
                .coerceAtLeast(0),
            bonusClaimedToday = sharedPreferences.getBoolean(ONE_MORE_GAME_BONUS_CLAIMED_KEY, false)
        )
    }

    private fun oneMoreGameBonusForToday(state: OneMoreGameBonusState): OneMoreGameBonusState {
        val today = todayDateKey()
        return if (state.dateKey == today) {
            state
        } else {
            OneMoreGameBonusState(dateKey = today)
        }
    }

    private fun loadSeasonState(): SeasonState {
        val today = todayDateKey()
        val savedStart = safeDateKeyOrBlank(sharedPreferences.getString(SEASON_START_DATE_KEY, "").orEmpty())
        if (savedStart.isBlank()) {
            return SeasonState(startDateKey = today, remainingDays = SeasonDurationDays)
        }
        val elapsedDays = daysBetween(savedStart, today).coerceAtLeast(0)
        val preservedBadges = safeSeasonLevels(
            sharedPreferences.getString(SEASON_BADGE_LEVELS_KEY, "").orEmpty().toIntSet()
        )
        val savedMissionDate = safeDateKeyOrBlank(sharedPreferences.getString(SEASON_MISSION_DATE_KEY, "").orEmpty())
        if (elapsedDays >= SeasonDurationDays) {
            return SeasonState(
                seasonNumber = sharedPreferences.getInt(SEASON_NUMBER_KEY, 1).coerceAtLeast(1) + 1,
                startDateKey = today,
                xp = 0,
                remainingDays = SeasonDurationDays,
                preservedBadgeLevels = preservedBadges,
                missionDateKey = today
            )
        }
        val isMissionToday = savedMissionDate == today
        return SeasonState(
            seasonNumber = sharedPreferences.getInt(SEASON_NUMBER_KEY, 1).coerceAtLeast(1),
            startDateKey = savedStart,
            xp = safeSeasonXp(sharedPreferences.getInt(SEASON_XP_KEY, 0)),
            remainingDays = (SeasonDurationDays - elapsedDays).coerceIn(0, SeasonDurationDays),
            claimedRewardLevels = safeSeasonLevels(
                sharedPreferences.getString(SEASON_CLAIMED_LEVELS_KEY, "").orEmpty().toIntSet()
            ),
            preservedBadgeLevels = preservedBadges,
            xpBoostEndTimeMillis = sharedPreferences.getLong(SEASON_XP_BOOST_END_TIME_KEY, 0L).coerceAtLeast(0L),
            missionDateKey = today,
            gamesPlayedToday = if (isMissionToday) sharedPreferences.getInt(SEASON_MISSION_PLAY_COUNT_KEY, 0)
                .coerceAtLeast(0) else 0,
            rewardedAdsWatchedToday = if (isMissionToday) sharedPreferences.getInt(SEASON_MISSION_REWARDED_COUNT_KEY, 0)
                .coerceAtLeast(0) else 0,
            seasonXpEarnedToday = if (isMissionToday) sharedPreferences.getInt(SEASON_MISSION_XP_EARNED_KEY, 0)
                .coerceIn(0, maxSeasonXp()) else 0,
            claimedMissionIds = if (isMissionToday) {
                sharedPreferences.getString(SEASON_MISSION_CLAIMED_IDS_KEY, "").orEmpty().toStringSet()
            } else {
                emptySet()
            }
        )
    }

    private fun loadDailyRewardState(): DailyRewardState {
        val today = todayDateKey()
        val lastClaimDate = safeDateKeyOrBlank(
            sharedPreferences.getString(DAILY_REWARD_LAST_CLAIM_DATE_KEY, "").orEmpty()
        )
        val savedStreak = sharedPreferences.getInt(DAILY_REWARD_STREAK_KEY, 0).coerceAtLeast(0)
        val daysSinceLastClaim = daysBetween(lastClaimDate, today)
        val claimedToday = daysSinceLastClaim == 0
        val dateMovedBackwards = daysSinceLastClaim < 0
        val canContinueStreak = daysSinceLastClaim == 0 || daysSinceLastClaim == 1
        val isStreakAtRisk = savedStreak > 0 && daysSinceLastClaim == 2
        val streakDay = when {
            dateMovedBackwards -> savedStreak.coerceAtLeast(1)
            claimedToday -> savedStreak.coerceAtLeast(1)
            canContinueStreak -> (savedStreak + 1).coerceAtLeast(1)
            else -> 1
        }
        val rewardCycleSize = DailyRewardCoinPlan.size.coerceAtLeast(1)
        val dayInCycle = ((streakDay - 1) % rewardCycleSize) + 1
        val nextStreakDay = if (claimedToday) streakDay + 1 else streakDay
        val nextDayInCycle = ((nextStreakDay - 1) % rewardCycleSize) + 1
        val rewardCoins = DailyRewardCoinPlan.getOrElse(dayInCycle - 1) { 0 }.coerceAtLeast(0)
        val nextRewardCoins = DailyRewardCoinPlan.getOrElse(nextDayInCycle - 1) { rewardCoins }.coerceAtLeast(0)
        return DailyRewardState(
            streakDay = streakDay,
            dayInCycle = dayInCycle,
            rewardCoins = rewardCoins,
            nextRewardCoins = nextRewardCoins,
            rewardType = if (dayInCycle == DailyRewardCoinPlan.size) DailyRewardType.SuperBox else DailyRewardType.Coins,
            rewardTheme = null,
            canClaim = !claimedToday && !dateMovedBackwards && !isStreakAtRisk,
            canProtectStreak = isStreakAtRisk,
            isStreakAtRisk = isStreakAtRisk,
            isSuperReward = dayInCycle == DailyRewardCoinPlan.size,
            loyalBadgeUnlocked = savedStreak >= 30 || streakDay >= 30,
            claimedToday = claimedToday,
            lastClaimDate = lastClaimDate
        )
    }

    private fun loadAchievements(): List<AchievementState> {
        val claimedIds = sharedPreferences.getString(ACHIEVEMENT_CLAIMED_IDS_KEY, "")
            .orEmpty()
            .split(",")
            .filter { it.isNotBlank() }
            .toSet()
        val totalGames = sharedPreferences.getInt(TOTAL_GAMES_KEY, 0).coerceAtLeast(0)
        val maxCombo = sharedPreferences.getInt(LIFETIME_MAX_COMBO_KEY, 0).coerceAtLeast(0)
        val rewardedAds = sharedPreferences.getInt(REWARDED_AD_WATCH_COUNT_KEY, 0).coerceAtLeast(0)
        val unlockedPaidThemes = loadUnlockedThemes().count { it.coinPrice > 0 }
        val bestScores = loadBestScores()
        val globalBest = bestScores.values.maxOrNull() ?: 0

        return defaultAchievements().map { achievement ->
            val progress = when (achievement.type) {
                AchievementType.BreakRecord -> if (globalBest > 0) 1 else 0
                AchievementType.ScoreInSingleGame -> globalBest
                AchievementType.PlayGames -> totalGames
                AchievementType.ReachCombo -> maxCombo
                AchievementType.RewardedAds -> rewardedAds
                AchievementType.ThemesUnlocked -> unlockedPaidThemes
            }.coerceAtMost(achievement.target)
            achievement.copy(
                progress = progress,
                unlocked = progress >= achievement.target,
                claimed = achievement.id in claimedIds && progress >= achievement.target
            )
        }
    }

    private fun defaultAchievements(): List<AchievementState> {
        val allPaidThemesTarget = PlayerTheme.entries.count { it.coinPrice > 0 }
        return listOf(
            AchievementState("record_breaker", AchievementType.BreakRecord, AchievementCategory.Score, com.reflex.tr.game.ibrh.R.string.achievement_first_record_title, com.reflex.tr.game.ibrh.R.string.achievement_first_record_description, 1, 0, 75, 50, false, false),
            AchievementState("score_25", AchievementType.ScoreInSingleGame, AchievementCategory.Score, com.reflex.tr.game.ibrh.R.string.achievement_score_25_title, com.reflex.tr.game.ibrh.R.string.achievement_score_25_description, 25, 0, 100, 70, false, false),
            AchievementState("score_50", AchievementType.ScoreInSingleGame, AchievementCategory.Score, com.reflex.tr.game.ibrh.R.string.achievement_score_50_title, com.reflex.tr.game.ibrh.R.string.achievement_score_50_description, 50, 0, 180, 120, false, false),
            AchievementState("score_100", AchievementType.ScoreInSingleGame, AchievementCategory.Score, com.reflex.tr.game.ibrh.R.string.achievement_score_100_title, com.reflex.tr.game.ibrh.R.string.achievement_score_100_description, 100, 0, 350, 220, false, false),
            AchievementState("play_10", AchievementType.PlayGames, AchievementCategory.Game, com.reflex.tr.game.ibrh.R.string.achievement_play_10_title, com.reflex.tr.game.ibrh.R.string.achievement_play_10_description, 10, 0, 160, 100, false, false),
            AchievementState("play_50", AchievementType.PlayGames, AchievementCategory.Game, com.reflex.tr.game.ibrh.R.string.achievement_play_50_title, com.reflex.tr.game.ibrh.R.string.achievement_play_50_description, 50, 0, 350, 220, false, false),
            AchievementState("play_100", AchievementType.PlayGames, AchievementCategory.Game, com.reflex.tr.game.ibrh.R.string.achievement_play_100_title, com.reflex.tr.game.ibrh.R.string.achievement_play_100_description, 100, 0, 700, 420, false, false),
            AchievementState("combo_5", AchievementType.ReachCombo, AchievementCategory.Combo, com.reflex.tr.game.ibrh.R.string.achievement_combo_5_title, com.reflex.tr.game.ibrh.R.string.achievement_combo_5_description, 5, 0, 120, 80, false, false),
            AchievementState("combo_master", AchievementType.ReachCombo, AchievementCategory.Combo, com.reflex.tr.game.ibrh.R.string.achievement_combo_master_title, com.reflex.tr.game.ibrh.R.string.achievement_combo_master_description, 10, 0, 240, 150, false, false),
            AchievementState("combo_20", AchievementType.ReachCombo, AchievementCategory.Combo, com.reflex.tr.game.ibrh.R.string.achievement_combo_20_title, com.reflex.tr.game.ibrh.R.string.achievement_combo_20_description, 20, 0, 500, 300, false, false),
            AchievementState("rewarded_ad_1", AchievementType.RewardedAds, AchievementCategory.Ads, com.reflex.tr.game.ibrh.R.string.achievement_rewarded_ad_1_title, com.reflex.tr.game.ibrh.R.string.achievement_rewarded_ad_1_description, 1, 0, 75, 50, false, false),
            AchievementState("rewarded_ad_10", AchievementType.RewardedAds, AchievementCategory.Ads, com.reflex.tr.game.ibrh.R.string.achievement_rewarded_ad_10_title, com.reflex.tr.game.ibrh.R.string.achievement_rewarded_ad_10_description, 10, 0, 250, 160, false, false),
            AchievementState("rewarded_ad_50", AchievementType.RewardedAds, AchievementCategory.Ads, com.reflex.tr.game.ibrh.R.string.achievement_rewarded_ad_50_title, com.reflex.tr.game.ibrh.R.string.achievement_rewarded_ad_50_description, 50, 0, 1000, 600, false, false),
            AchievementState("theme_unlock_1", AchievementType.ThemesUnlocked, AchievementCategory.Theme, com.reflex.tr.game.ibrh.R.string.achievement_theme_unlock_1_title, com.reflex.tr.game.ibrh.R.string.achievement_theme_unlock_1_description, 1, 0, 150, 100, false, false),
            AchievementState("theme_unlock_5", AchievementType.ThemesUnlocked, AchievementCategory.Theme, com.reflex.tr.game.ibrh.R.string.achievement_theme_unlock_5_title, com.reflex.tr.game.ibrh.R.string.achievement_theme_unlock_5_description, 5, 0, 600, 360, false, false),
            AchievementState("theme_unlock_all", AchievementType.ThemesUnlocked, AchievementCategory.Theme, com.reflex.tr.game.ibrh.R.string.achievement_theme_unlock_all_title, com.reflex.tr.game.ibrh.R.string.achievement_theme_unlock_all_description, allPaidThemesTarget, 0, 1500, 900, false, false)
        )
    }

    private fun loadWeeklyChallenge(): ChallengeState {
        val weekKey = weekDateKey()
        val savedWeek = sharedPreferences.getString(WEEKLY_CHALLENGE_CREATED_DATE_KEY, null)
            ?.takeIf { isValidWeekKey(it) }
        val progress = if (savedWeek == weekKey) {
            sharedPreferences.getInt(WEEKLY_CHALLENGE_PROGRESS_KEY, 0)
        } else {
            0
        }
        return ChallengeState.defaultWeekly().copy(
            id = "weekly_score_$weekKey",
            progress = progress.coerceIn(0, ChallengeState.defaultWeekly().target),
            completed = progress >= ChallengeState.defaultWeekly().target,
            createdDate = weekKey
        )
    }

    private fun calculateLevel(xp: Int): Int {
        return (safeProgressionXp(xp) / PROGRESSION_XP_PER_LEVEL + 1).coerceAtLeast(1)
    }

    private fun yesterdayDateKey(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormatter().format(calendar.time)
    }

    private fun daysBetween(startDateKey: String, endDateKey: String): Int {
        if (startDateKey.isBlank()) return Int.MAX_VALUE
        val formatter = dateFormatter()
        return runCatching {
            val start = formatter.parse(startDateKey) ?: Date(0L)
            val end = formatter.parse(endDateKey) ?: Date(0L)
            ((end.time - start.time) / DAY_IN_MILLIS).toInt()
        }.getOrDefault(Int.MAX_VALUE)
    }

    private fun weekDateKey(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-W${calendar.get(Calendar.WEEK_OF_YEAR)}"
    }

    private fun createDailyChallengeId(
        date: String,
        type: DailyChallenge
    ): String {
        return "${date}_${type.name.lowercase(Locale.US)}"
    }
}

private fun GameMode.bestScorePreferenceKey(): String {
    return "best_score_${storageKey}"
}

private fun GameMode.weeklyScorePreferenceKey(): String {
    return "weekly_score_${storageKey}"
}

private fun String.toIntSet(): Set<Int> {
    return split(",")
        .mapNotNull { value -> value.trim().toIntOrNull() }
        .filter { it > 0 }
        .toSet()
}

private fun String.toStringSet(): Set<String> {
    return split(",")
        .map { value -> value.trim() }
        .filter { it.isNotBlank() }
        .toSet()
}

private fun SharedPreferences.Editor.commitSafely() {
    if (!commit()) {
        apply()
    }
}

private fun safePlayerName(name: String): String {
    return name.trim()
        .take(12)
        .ifBlank { DEFAULT_PLAYER_NAME }
}

private fun safeProgressionXp(xp: Int): Int {
    return xp.coerceAtLeast(0)
}

private fun safeCount(value: Int, max: Int): Int {
    return value.coerceIn(0, max.coerceAtLeast(0))
}

private fun safeSeasonXp(xp: Int): Int {
    return xp.coerceIn(0, maxSeasonXp())
}

private fun maxSeasonXp(): Int {
    return (SeasonMaxLevel - 1) * SeasonXpPerLevel
}

private fun safeSeasonLevels(levels: Set<Int>): Set<Int> {
    return levels
        .filter { it in 1..SeasonMaxLevel }
        .toSet()
}

private fun safeUnlockedThemes(
    unlockedThemes: Set<PlayerTheme>,
    selectedTheme: PlayerTheme
): Set<PlayerTheme> {
    return unlockedThemes + selectedTheme + PlayerTheme.NeonRed
}

private fun safeSelectedTheme(
    selectedTheme: PlayerTheme,
    unlockedThemes: Set<PlayerTheme>
): PlayerTheme {
    return selectedTheme.takeIf { it in unlockedThemes } ?: PlayerTheme.NeonRed
}

private fun safeDateKeyOrBlank(dateKey: String): String {
    return dateKey.takeIf { isValidDateKey(it) }.orEmpty()
}

private fun safeDateKeyOrToday(dateKey: String): String {
    return safeDateKeyOrBlank(dateKey).ifBlank { dateFormatter().format(Calendar.getInstance().time) }
}

private fun isValidDateKey(dateKey: String): Boolean {
    if (dateKey.length != DATE_PATTERN.length) return false
    return runCatching {
        dateFormatter().parse(dateKey) != null
    }.getOrDefault(false)
}

private fun isValidWeekKey(weekKey: String): Boolean {
    val parts = weekKey.split("-W")
    if (parts.size != 2) return false
    val year = parts[0].toIntOrNull() ?: return false
    val week = parts[1].toIntOrNull() ?: return false
    return year in 2000..2100 && week in 1..53
}

private fun dateFormatter(): SimpleDateFormat {
    return SimpleDateFormat(DATE_PATTERN, Locale.US).apply {
        isLenient = false
    }
}
