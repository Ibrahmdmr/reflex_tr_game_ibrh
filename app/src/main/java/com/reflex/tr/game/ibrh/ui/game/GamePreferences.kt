package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
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
private const val DAILY_REWARD_STREAK_KEY = "daily_reward_streak"
private const val COIN_CHEST_OPEN_DATE_KEY = "coin_chest_open_date"
private const val COIN_CHEST_OPEN_COUNT_KEY = "coin_chest_open_count"
private const val COIN_CHEST_LAST_REWARD_KEY = "coin_chest_last_reward"
private const val ONE_MORE_GAME_BONUS_DATE_KEY = "one_more_game_bonus_date"
private const val ONE_MORE_GAME_BONUS_PLAYED_COUNT_KEY = "one_more_game_bonus_played_count"
private const val ONE_MORE_GAME_BONUS_CLAIMED_KEY = "one_more_game_bonus_claimed"
private const val ACHIEVEMENT_CLAIMED_IDS_KEY = "achievement_claimed_ids"
private const val WEEKLY_CHALLENGE_PROGRESS_KEY = "weekly_challenge_progress"
private const val WEEKLY_CHALLENGE_CREATED_DATE_KEY = "weekly_challenge_created_date"
private const val PLAYER_NAME_KEY = "player_name"
private const val PLAYER_NAME_PROMPT_COMPLETED_KEY = "player_name_prompt_completed"
private const val PLAYER_TITLE_KEY = "player_title"
private const val PLAYER_WEEKLY_SCORE_KEY = "player_weekly_score"
private const val PLAYER_WEEKLY_SCORE_DATE_KEY = "player_weekly_score_date"
private const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
private const val DEFAULT_PLAYER_NAME = ""

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
    }

    private val bestScoresState = MutableStateFlow(loadBestScores())
    private val languageState = MutableStateFlow(loadLanguage())
    private val soundEnabledState = MutableStateFlow(loadSoundEnabled())
    private val effectSoundEnabledState = MutableStateFlow(loadEffectSoundEnabled())
    private val vibrationEnabledState = MutableStateFlow(loadVibrationEnabled())
    private val dailyRewardNotificationState = MutableStateFlow(loadDailyRewardNotificationEnabled())
    private val streakNotificationState = MutableStateFlow(loadStreakNotificationEnabled())
    private val newMissionNotificationState = MutableStateFlow(loadNewMissionNotificationEnabled())

    val bestScoresFlow: Flow<Map<GameMode, Int>> = bestScoresState.asStateFlow()
    val languageFlow: Flow<AppLanguage> = languageState.asStateFlow()
    val soundEnabledFlow: Flow<Boolean> = soundEnabledState.asStateFlow()
    val effectSoundEnabledFlow: Flow<Boolean> = effectSoundEnabledState.asStateFlow()
    val vibrationEnabledFlow: Flow<Boolean> = vibrationEnabledState.asStateFlow()
    val dailyRewardNotificationFlow: Flow<Boolean> = dailyRewardNotificationState.asStateFlow()
    val streakNotificationFlow: Flow<Boolean> = streakNotificationState.asStateFlow()
    val newMissionNotificationFlow: Flow<Boolean> = newMissionNotificationState.asStateFlow()

    suspend fun saveBestScore(mode: GameMode, score: Int) {
        val key = mode.bestScorePreferenceKey()
        val currentBestScore = sharedPreferences.getInt(key, 0)
        if (score <= currentBestScore) return

        sharedPreferences.edit()
            .putInt(key, score)
            .apply()

        bestScoresState.value = loadBestScores()
    }

    suspend fun saveLanguage(language: AppLanguage) {
        sharedPreferences.edit()
            .putString(LANGUAGE_KEY, language.code)
            .apply()

        languageState.value = language
    }

    suspend fun saveSoundEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(SOUND_ENABLED_KEY, enabled)
            .apply()

        soundEnabledState.value = enabled
    }

    suspend fun saveEffectSoundEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(EFFECT_SOUND_ENABLED_KEY, enabled)
            .apply()

        effectSoundEnabledState.value = enabled
    }

    suspend fun saveVibrationEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(VIBRATION_ENABLED_KEY, enabled)
            .apply()

        vibrationEnabledState.value = enabled
    }

    suspend fun saveDailyRewardNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(NOTIFICATION_DAILY_REWARD_KEY, enabled)
            .apply()

        dailyRewardNotificationState.value = enabled
    }

    suspend fun saveStreakNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(NOTIFICATION_STREAK_KEY, enabled)
            .apply()

        streakNotificationState.value = enabled
    }

    suspend fun saveNewMissionNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(NOTIFICATION_NEW_MISSION_KEY, enabled)
            .apply()

        newMissionNotificationState.value = enabled
    }

    fun getDailyChallengeState(): DailyChallengeState {
        val today = todayDateKey()
        val savedDate = sharedPreferences.getString(DAILY_CHALLENGE_CREATED_DATE_KEY, null)
        val savedType = sharedPreferences.getString(DAILY_CHALLENGE_TYPE_KEY, null)
        val type = DailyChallenge.entries.firstOrNull { it.name == savedType }

        if (savedDate == today && type != null) {
            return DailyChallengeState(
                id = sharedPreferences.getString(DAILY_CHALLENGE_ID_KEY, null)
                    ?: createDailyChallengeId(today, type),
                type = type,
                target = sharedPreferences.getInt(DAILY_CHALLENGE_TARGET_KEY, type.targetValue),
                progress = sharedPreferences.getInt(DAILY_CHALLENGE_PROGRESS_KEY, 0),
                completed = sharedPreferences.getBoolean(DAILY_CHALLENGE_COMPLETED_KEY, false),
                createdDate = today,
                rewardCoins = sharedPreferences.getInt(DAILY_CHALLENGE_REWARD_COINS_KEY, 100),
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
            rewardCoins = 100
        )
        saveDailyChallengeState(newState)
        return newState
    }

    fun saveDailyChallengeState(state: DailyChallengeState) {
        sharedPreferences.edit()
            .putString(DAILY_CHALLENGE_ID_KEY, state.id)
            .putString(DAILY_CHALLENGE_TYPE_KEY, state.type.name)
            .putInt(DAILY_CHALLENGE_TARGET_KEY, state.target)
            .putInt(DAILY_CHALLENGE_PROGRESS_KEY, state.progress.coerceIn(0, state.target))
            .putBoolean(DAILY_CHALLENGE_COMPLETED_KEY, state.completed)
            .putString(DAILY_CHALLENGE_CREATED_DATE_KEY, state.createdDate)
            .putInt(DAILY_CHALLENGE_REWARD_COINS_KEY, state.rewardCoins.coerceAtLeast(0))
            .putBoolean(DAILY_CHALLENGE_REWARD_CLAIMED_KEY, state.rewardClaimed)
            .putBoolean(DAILY_CHALLENGE_DOUBLE_REWARD_CLAIMED_KEY, state.doubleRewardClaimed)
            .apply()
    }

    fun getProgressionState(): ProgressionState {
        return ProgressionState(
            coins = sharedPreferences.getInt(COINS_KEY, 0).coerceAtLeast(0),
            xp = sharedPreferences.getInt(XP_KEY, 0).coerceAtLeast(0),
            level = calculateLevel(sharedPreferences.getInt(XP_KEY, 0).coerceAtLeast(0)),
            totalGames = sharedPreferences.getInt(TOTAL_GAMES_KEY, 0).coerceAtLeast(0),
            totalHits = sharedPreferences.getInt(TOTAL_HITS_KEY, 0).coerceAtLeast(0),
            lifetimeMaxCombo = sharedPreferences.getInt(LIFETIME_MAX_COMBO_KEY, 0).coerceAtLeast(0),
            rewardedAdWatchCount = sharedPreferences.getInt(REWARDED_AD_WATCH_COUNT_KEY, 0).coerceAtLeast(0),
            selectedTheme = loadSelectedTheme(),
            unlockedThemes = loadUnlockedThemes(),
            coinChest = loadCoinChestState(),
            oneMoreGameBonus = loadOneMoreGameBonusState(),
            dailyReward = loadDailyRewardState(),
            achievements = loadAchievements(),
            weeklyChallenge = loadWeeklyChallenge()
        )
    }

    fun getPlayerProfile(): PlayerProfile {
        val weekKey = weekDateKey()
        val savedWeek = sharedPreferences.getString(PLAYER_WEEKLY_SCORE_DATE_KEY, null)
        val savedName = sharedPreferences.getString(PLAYER_NAME_KEY, "").orEmpty()
        val weeklyScoresByMode = GameMode.entries.associateWith { mode ->
            if (savedWeek == weekKey) {
                sharedPreferences.getInt(mode.weeklyScorePreferenceKey(), 0)
            } else {
                0
            }
        }
        return PlayerProfile(
            name = savedName.ifBlank { DEFAULT_PLAYER_NAME },
            title = PlayerTitle.entries.firstOrNull {
                it.name == sharedPreferences.getString(PLAYER_TITLE_KEY, PlayerTitle.ReflexHunter.name)
            } ?: PlayerTitle.ReflexHunter,
            weeklyBestScore = if (savedWeek == weekKey) {
                maxOf(sharedPreferences.getInt(PLAYER_WEEKLY_SCORE_KEY, 0), weeklyScoresByMode.values.maxOrNull() ?: 0)
            } else {
                0
            },
            weeklyBestScoresByMode = weeklyScoresByMode,
            hasCompletedNamePrompt = savedName.isNotBlank() ||
                sharedPreferences.getBoolean(PLAYER_NAME_PROMPT_COMPLETED_KEY, false)
        )
    }

    fun savePlayerName(name: String) {
        sharedPreferences.edit()
            .putString(PLAYER_NAME_KEY, name)
            .putBoolean(PLAYER_NAME_PROMPT_COMPLETED_KEY, true)
            .apply()
    }

    fun savePlayerTitle(title: PlayerTitle) {
        sharedPreferences.edit()
            .putString(PLAYER_TITLE_KEY, title.name)
            .apply()
    }

    fun saveWeeklyBestScore(mode: GameMode, score: Int) {
        val weekKey = weekDateKey()
        val savedWeek = sharedPreferences.getString(PLAYER_WEEKLY_SCORE_DATE_KEY, null)
        val currentScore = if (savedWeek == weekKey) sharedPreferences.getInt(PLAYER_WEEKLY_SCORE_KEY, 0) else 0
        val currentModeScore = if (savedWeek == weekKey) {
            sharedPreferences.getInt(mode.weeklyScorePreferenceKey(), 0)
        } else {
            0
        }
        if (score <= currentScore && score <= currentModeScore) return

        sharedPreferences.edit()
            .putString(PLAYER_WEEKLY_SCORE_DATE_KEY, weekKey)
            .putInt(PLAYER_WEEKLY_SCORE_KEY, maxOf(score, currentScore))
            .putInt(mode.weeklyScorePreferenceKey(), maxOf(score, currentModeScore))
            .apply()
    }

    fun saveProgressionState(state: ProgressionState) {
        val oneMoreGameBonus = oneMoreGameBonusForToday(state.oneMoreGameBonus)
        sharedPreferences.edit()
            .putInt(COINS_KEY, state.coins.coerceAtLeast(0))
            .putInt(XP_KEY, state.xp.coerceAtLeast(0))
            .putInt(TOTAL_GAMES_KEY, state.totalGames.coerceAtLeast(0))
            .putInt(TOTAL_HITS_KEY, state.totalHits.coerceAtLeast(0))
            .putInt(LIFETIME_MAX_COMBO_KEY, state.lifetimeMaxCombo.coerceAtLeast(0))
            .putInt(REWARDED_AD_WATCH_COUNT_KEY, state.rewardedAdWatchCount.coerceAtLeast(0))
            .putString(SELECTED_THEME_KEY, state.selectedTheme.storageKey)
            .putString(
                UNLOCKED_THEMES_KEY,
                state.unlockedThemes.joinToString(separator = ",") { it.storageKey }
            )
            .putString(
                ACHIEVEMENT_CLAIMED_IDS_KEY,
                state.achievements.filter { it.claimed }.joinToString(separator = ",") { it.id }
            )
            .putString(COIN_CHEST_OPEN_DATE_KEY, state.coinChest.lastOpenedDate)
            .putInt(COIN_CHEST_OPEN_COUNT_KEY, state.coinChest.openedToday.coerceIn(0, state.coinChest.maxOpensPerDay))
            .putInt(COIN_CHEST_LAST_REWARD_KEY, state.coinChest.lastRewardCoins.coerceAtLeast(0))
            .putString(ONE_MORE_GAME_BONUS_DATE_KEY, oneMoreGameBonus.dateKey)
            .putInt(ONE_MORE_GAME_BONUS_PLAYED_COUNT_KEY, oneMoreGameBonus.gamesPlayedToday.coerceAtLeast(0))
            .putBoolean(ONE_MORE_GAME_BONUS_CLAIMED_KEY, oneMoreGameBonus.bonusClaimedToday)
            .putInt(WEEKLY_CHALLENGE_PROGRESS_KEY, state.weeklyChallenge.progress.coerceIn(0, state.weeklyChallenge.target))
            .putString(WEEKLY_CHALLENGE_CREATED_DATE_KEY, state.weeklyChallenge.createdDate)
            .apply()
    }

    fun saveDailyRewardClaim(streakDay: Int) {
        val today = todayDateKey()
        sharedPreferences.edit()
            .putString(DAILY_REWARD_LAST_CLAIM_DATE_KEY, today)
            .putInt(DAILY_REWARD_STREAK_KEY, streakDay.coerceAtLeast(1))
            .apply()
    }

    fun protectDailyRewardStreak() {
        sharedPreferences.edit()
            .putString(DAILY_REWARD_LAST_CLAIM_DATE_KEY, yesterdayDateKey())
            .apply()
    }

    private fun migrateGlobalBestScoreToClassic() {
        val legacyBestScore = sharedPreferences.getInt(BEST_SCORE_KEY, 0)
        if (legacyBestScore <= 0) return

        val classicKey = GameMode.Classic.bestScorePreferenceKey()
        val classicBestScore = sharedPreferences.getInt(classicKey, 0)
        if (classicBestScore >= legacyBestScore) return

        sharedPreferences.edit()
            .putInt(classicKey, legacyBestScore)
            .apply()
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

    private fun chooseDailyChallengeType(previousType: DailyChallenge?): DailyChallenge {
        val availableTypes = DailyChallenge.entries.filterNot { it == previousType }
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return availableTypes[dayOfYear % availableTypes.size]
    }

    private fun todayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
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

    private fun loadDailyRewardState(): DailyRewardState {
        val today = todayDateKey()
        val lastClaimDate = sharedPreferences.getString(DAILY_REWARD_LAST_CLAIM_DATE_KEY, "").orEmpty()
        val savedStreak = sharedPreferences.getInt(DAILY_REWARD_STREAK_KEY, 0)
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
        val dayInCycle = ((streakDay - 1) % DailyRewardCoinPlan.size) + 1
        val nextStreakDay = if (claimedToday) streakDay + 1 else streakDay
        val nextDayInCycle = ((nextStreakDay - 1) % DailyRewardCoinPlan.size) + 1
        return DailyRewardState(
            streakDay = streakDay,
            dayInCycle = dayInCycle,
            rewardCoins = DailyRewardCoinPlan[dayInCycle - 1],
            nextRewardCoins = DailyRewardCoinPlan[nextDayInCycle - 1],
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
        val totalGames = sharedPreferences.getInt(TOTAL_GAMES_KEY, 0)
        val maxCombo = sharedPreferences.getInt(LIFETIME_MAX_COMBO_KEY, 0)
        val rewardedAds = sharedPreferences.getInt(REWARDED_AD_WATCH_COUNT_KEY, 0)
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
        return (xp / 250 + 1).coerceAtLeast(1)
    }

    private fun yesterdayDateKey(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }

    private fun daysBetween(startDateKey: String, endDateKey: String): Int {
        if (startDateKey.isBlank()) return Int.MAX_VALUE
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
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
