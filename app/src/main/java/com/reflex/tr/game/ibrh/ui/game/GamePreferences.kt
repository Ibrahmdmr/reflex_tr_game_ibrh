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
private const val INVITE_REWARD_CLAIMED_KEY = "invite_reward_claimed"
private const val STORE_PREVIEW_MODE_KEY = "store_preview_mode"
private const val FIRST_OPEN_DATE_KEY = "first_open_date"
private const val REVIEW_LAST_PROMPT_TIME_KEY = "review_last_prompt_time"
private const val REVIEW_AUTO_COMPLETED_KEY = "review_auto_completed"
private const val SHOWN_MODE_TIPS_KEY = "shown_mode_tips"
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
private const val TOTAL_COINS_EARNED_KEY = "total_coins_earned"
private const val TOTAL_COINS_SPENT_KEY = "total_coins_spent"
private const val XP_KEY = "xp"
private const val TOTAL_GAMES_KEY = "total_games"
private const val TOTAL_SCORE_KEY = "total_score"
private const val TOTAL_HITS_KEY = "total_hits"
private const val TOTAL_MISSES_KEY = "total_misses"
private const val LIFETIME_MAX_COMBO_KEY = "lifetime_max_combo"
private const val LIFETIME_MAX_FLAWLESS_STREAK_KEY = "lifetime_max_flawless_streak"
private const val PERSONAL_RECORD_BEST_SCORE_KEY = "personal_record_best_score"
private const val PERSONAL_RECORD_BEST_COMBO_KEY = "personal_record_best_combo"
private const val PERSONAL_RECORD_BEST_ACCURACY_KEY = "personal_record_best_accuracy"
private const val PERSONAL_RECORD_LONGEST_SURVIVAL_KEY = "personal_record_longest_survival"
private const val PERSONAL_RECORD_MOST_COINS_KEY = "personal_record_most_coins"
private const val SELECTED_PROFILE_BADGES_KEY = "selected_profile_badges"
private const val TOTAL_BOSS_ROUND_HITS_KEY = "total_boss_round_hits"
private const val TOTAL_ULTRA_MOMENT_HITS_KEY = "total_ultra_moment_hits"
private const val SEASON_HUNTER_BADGE_UNLOCKED_KEY = "season_hunter_badge_unlocked"
private const val REWARDED_AD_WATCH_COUNT_KEY = "rewarded_ad_watch_count"
private const val SELECTED_THEME_KEY = "selected_theme"
private const val UNLOCKED_THEMES_KEY = "unlocked_themes"
private const val SELECTED_TARGET_SKIN_KEY = "selected_target_skin"
private const val UNLOCKED_TARGET_SKINS_KEY = "unlocked_target_skins"
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
private const val WEEKLY_CHALLENGE_TYPE_KEY = "weekly_challenge_type"
private const val WEEKLY_CHALLENGE_CLAIMED_KEY = "weekly_challenge_claimed"
private const val WEEKLY_GOAL_BOARD_WEEK_KEY = "weekly_goal_board_week"
private const val WEEKLY_GOAL_BONUS_CLAIMED_KEY = "weekly_goal_bonus_claimed"
private const val DAILY_LEADERBOARD_GOAL_DATE_KEY = "daily_leaderboard_goal_date"
private const val DAILY_LEADERBOARD_GOAL_TYPE_KEY = "daily_leaderboard_goal_type"
private const val DAILY_LEADERBOARD_GOAL_PROGRESS_KEY = "daily_leaderboard_goal_progress"
private const val DAILY_LEADERBOARD_GOAL_CLAIMED_KEY = "daily_leaderboard_goal_claimed"
private const val DAILY_LEADERBOARD_GOAL_INITIAL_SCORE_KEY = "daily_leaderboard_goal_initial_score"
private const val DAILY_LEADERBOARD_GOAL_INITIAL_RANK_KEY = "daily_leaderboard_goal_initial_rank"
private const val PERSONAL_GOAL_DATE_KEY = "personal_goal_date"
private const val PERSONAL_GOAL_TARGET_SCORE_KEY = "personal_goal_target_score"
private const val PERSONAL_GOAL_INITIAL_BEST_SCORE_KEY = "personal_goal_initial_best_score"
private const val PERSONAL_GOAL_PROGRESS_SCORE_KEY = "personal_goal_progress_score"
private const val PERSONAL_GOAL_CLAIMED_KEY = "personal_goal_claimed"
private const val PERSONAL_GOAL_REWARD_COINS_KEY = "personal_goal_reward_coins"
private const val COMBO_CHALLENGE_DATE_KEY = "combo_challenge_date"
private const val COMBO_CHALLENGE_TYPE_KEY = "combo_challenge_type"
private const val COMBO_CHALLENGE_PROGRESS_KEY = "combo_challenge_progress"
private const val COMBO_CHALLENGE_GAMES_USED_KEY = "combo_challenge_games_used"
private const val COMBO_CHALLENGE_CLAIMED_KEY = "combo_challenge_claimed"
private const val DAILY_MINI_TOURNAMENT_DATE_KEY = "daily_mini_tournament_date"
private const val DAILY_MINI_TOURNAMENT_MODE_KEY = "daily_mini_tournament_mode"
private const val DAILY_MINI_TOURNAMENT_BEST_SCORE_KEY = "daily_mini_tournament_best_score"
private const val DAILY_MINI_TOURNAMENT_CLAIMED_KEY = "daily_mini_tournament_claimed"
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
private const val SEASON_QUEST_CLAIMED_IDS_KEY = "season_quest_claimed_ids"
private const val SEASON_QUEST_USED_COSMETICS_KEY = "season_quest_used_cosmetics"
private const val PLAYER_NAME_KEY = "player_name"
private const val PLAYER_NAME_PROMPT_COMPLETED_KEY = "player_name_prompt_completed"
private const val PLAYER_TITLE_KEY = "player_title"
private const val PLAYER_WEEKLY_SCORE_KEY = "player_weekly_score"
private const val PLAYER_WEEKLY_SCORE_DATE_KEY = "player_weekly_score_date"
private const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
private const val REVIEW_COOLDOWN_MILLIS = 14L * DAY_IN_MILLIS
private const val DATE_PATTERN = "yyyy-MM-dd"
private const val PROGRESSION_XP_PER_LEVEL = 250
private val BONUS_HOUR_CANDIDATES = listOf(12, 18, 20, 21)

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

    /** Seçili dilin anlık değeri — Compose dışındaki katmanların string lokalize etmesi için. */
    val currentLanguage: AppLanguage get() = languageState.value
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

    fun getShownModeTips(): Set<GameMode> {
        return loadShownModeTips()
    }

    fun markModeTipShown(mode: GameMode) {
        val updatedModes = loadShownModeTips() + mode
        sharedPreferences.edit()
            .putString(SHOWN_MODE_TIPS_KEY, updatedModes.joinToString(separator = ",") { it.storageKey })
            .commitSafely()
    }

    fun resetModeTips() {
        sharedPreferences.edit()
            .remove(SHOWN_MODE_TIPS_KEY)
            .commitSafely()
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
        val unlockedTargetSkins = loadUnlockedTargetSkins()
        val selectedTargetSkin = safeSelectedTargetSkin(loadSelectedTargetSkin(), unlockedTargetSkins)
        return ProgressionState(
            coins = sharedPreferences.getInt(COINS_KEY, 0).coerceAtLeast(0),
            totalCoinsEarned = sharedPreferences.getInt(TOTAL_COINS_EARNED_KEY, 0).coerceAtLeast(0),
            totalCoinsSpent = sharedPreferences.getInt(TOTAL_COINS_SPENT_KEY, 0).coerceAtLeast(0),
            xp = safeXp,
            level = calculateLevel(safeXp),
            totalGames = sharedPreferences.getInt(TOTAL_GAMES_KEY, 0).coerceAtLeast(0),
            totalScore = sharedPreferences.getInt(TOTAL_SCORE_KEY, 0).coerceAtLeast(0),
            gamesPlayedByMode = loadGamesPlayedByMode(),
            modeMasteryXpByMode = loadModeMasteryXpByMode(),
            totalHits = sharedPreferences.getInt(TOTAL_HITS_KEY, 0).coerceAtLeast(0),
            totalMisses = sharedPreferences.getInt(TOTAL_MISSES_KEY, 0).coerceAtLeast(0),
            lifetimeMaxCombo = sharedPreferences.getInt(LIFETIME_MAX_COMBO_KEY, 0).coerceAtLeast(0),
            lifetimeMaxFlawlessStreak = sharedPreferences.getInt(LIFETIME_MAX_FLAWLESS_STREAK_KEY, 0).coerceAtLeast(0),
            rewardedAdWatchCount = sharedPreferences.getInt(REWARDED_AD_WATCH_COUNT_KEY, 0).coerceAtLeast(0),
            selectedTheme = loadSelectedTheme(),
            unlockedThemes = loadUnlockedThemes(),
            selectedTargetSkin = selectedTargetSkin,
            unlockedTargetSkins = unlockedTargetSkins,
            coinChest = loadCoinChestState(),
            shopCoinReward = loadShopCoinRewardState(),
            oneMoreGameBonus = loadOneMoreGameBonusState(),
            dailyReward = loadDailyRewardState(),
            bonusHour = loadBonusHourState(),
            dailyMiniTournament = loadDailyMiniTournament(),
            season = loadSeasonState(),
            achievements = loadAchievements(),
            weeklyChallenge = loadWeeklyChallenge(),
            weeklyGoalBoard = loadWeeklyGoalBoard(),
            dailyLeaderboardGoal = loadDailyLeaderboardGoal(),
            personalGoal = loadPersonalGoal(),
            comboChallenge = loadComboChallenge(),
            personalRecords = loadPersonalRecords(),
            selectedProfileBadgeIds = loadSelectedProfileBadgeIds(),
            totalBossRoundHits = sharedPreferences.getInt(TOTAL_BOSS_ROUND_HITS_KEY, 0).coerceAtLeast(0),
            totalUltraMomentHits = sharedPreferences.getInt(TOTAL_ULTRA_MOMENT_HITS_KEY, 0).coerceAtLeast(0),
            seasonHunterBadgeUnlocked = sharedPreferences.getBoolean(SEASON_HUNTER_BADGE_UNLOCKED_KEY, false),
            firstTargetBonusClaimed = sharedPreferences.getBoolean(FIRST_TARGET_BONUS_CLAIMED_KEY, false),
            inviteRewardClaimed = sharedPreferences.getBoolean(INVITE_REWARD_CLAIMED_KEY, false)
        )
    }

    fun getBonusHourState(): BonusHourState {
        return loadBonusHourState()
    }

    fun getDailyMiniTournamentState(): DailyMiniTournamentState {
        return loadDailyMiniTournament()
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
            hasCompletedNamePrompt = savedName.isNotBlank() ||
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
        val safeUnlockedTargetSkins = safeUnlockedTargetSkins(state.unlockedTargetSkins, state.selectedTargetSkin)
        val safeSelectedTargetSkin = safeSelectedTargetSkin(state.selectedTargetSkin, safeUnlockedTargetSkins)
        val safeWeeklyChallengeDate = state.weeklyChallenge.createdDate.takeIf { isValidWeekKey(it) } ?: weekDateKey()
        sharedPreferences.edit()
            .putInt(COINS_KEY, state.coins.coerceAtLeast(0))
            .putInt(TOTAL_COINS_EARNED_KEY, state.totalCoinsEarned.coerceAtLeast(0))
            .putInt(TOTAL_COINS_SPENT_KEY, state.totalCoinsSpent.coerceAtLeast(0))
            .putInt(XP_KEY, safeProgressionXp(state.xp))
            .putInt(TOTAL_GAMES_KEY, state.totalGames.coerceAtLeast(0))
            .putInt(TOTAL_SCORE_KEY, state.totalScore.coerceAtLeast(0))
            .apply {
                GameMode.entries.forEach { mode ->
                    putInt(mode.gamesPlayedPreferenceKey(), state.gamesPlayedByMode[mode]?.coerceAtLeast(0) ?: 0)
                    putInt(
                        mode.masteryXpPreferenceKey(),
                        state.modeMasteryXpByMode[mode]
                            ?.coerceIn(0, (MODE_MASTERY_MAX_LEVEL - 1) * MODE_MASTERY_XP_PER_LEVEL)
                            ?: 0
                    )
                }
            }
            .putInt(TOTAL_HITS_KEY, state.totalHits.coerceAtLeast(0))
            .putInt(TOTAL_MISSES_KEY, state.totalMisses.coerceAtLeast(0))
            .putInt(LIFETIME_MAX_COMBO_KEY, state.lifetimeMaxCombo.coerceAtLeast(0))
            .putInt(LIFETIME_MAX_FLAWLESS_STREAK_KEY, state.lifetimeMaxFlawlessStreak.coerceAtLeast(0))
            .putInt(PERSONAL_RECORD_BEST_SCORE_KEY, state.personalRecords.bestScore.coerceAtLeast(0))
            .putInt(PERSONAL_RECORD_BEST_COMBO_KEY, state.personalRecords.bestCombo.coerceAtLeast(0))
            .putInt(PERSONAL_RECORD_BEST_ACCURACY_KEY, state.personalRecords.bestAccuracyPercent.coerceIn(0, 100))
            .putInt(PERSONAL_RECORD_LONGEST_SURVIVAL_KEY, state.personalRecords.longestSurvivalSeconds.coerceAtLeast(0))
            .putInt(PERSONAL_RECORD_MOST_COINS_KEY, state.personalRecords.mostCoinsInGame.coerceAtLeast(0))
            .putString(SELECTED_PROFILE_BADGES_KEY, safeProfileBadgeIds(state.selectedProfileBadgeIds).joinToString(","))
            .putInt(TOTAL_BOSS_ROUND_HITS_KEY, state.totalBossRoundHits.coerceAtLeast(0))
            .putInt(TOTAL_ULTRA_MOMENT_HITS_KEY, state.totalUltraMomentHits.coerceAtLeast(0))
            .putBoolean(SEASON_HUNTER_BADGE_UNLOCKED_KEY, state.seasonHunterBadgeUnlocked)
            .putInt(REWARDED_AD_WATCH_COUNT_KEY, state.rewardedAdWatchCount.coerceAtLeast(0))
            .putBoolean(FIRST_TARGET_BONUS_CLAIMED_KEY, state.firstTargetBonusClaimed)
            .putBoolean(INVITE_REWARD_CLAIMED_KEY, state.inviteRewardClaimed)
            .putString(SELECTED_THEME_KEY, safeSelectedTheme.storageKey)
            .putString(
                UNLOCKED_THEMES_KEY,
                safeUnlockedThemes.joinToString(separator = ",") { it.storageKey }
            )
            .putString(SELECTED_TARGET_SKIN_KEY, safeSelectedTargetSkin.storageKey)
            .putString(
                UNLOCKED_TARGET_SKINS_KEY,
                safeUnlockedTargetSkins.joinToString(separator = ",") { it.storageKey }
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
            .putString(WEEKLY_CHALLENGE_TYPE_KEY, state.weeklyChallenge.type.name)
            .putBoolean(WEEKLY_CHALLENGE_CLAIMED_KEY, state.weeklyChallenge.claimed)
            .putString(
                WEEKLY_GOAL_BOARD_WEEK_KEY,
                state.weeklyGoalBoard.weekKey.takeIf { isValidWeekKey(it) } ?: weekDateKey()
            )
            .putBoolean(WEEKLY_GOAL_BONUS_CLAIMED_KEY, state.weeklyGoalBoard.bonusClaimed)
            .apply {
                state.weeklyGoalBoard.goals.forEach { goal ->
                    putInt(goal.type.weeklyGoalProgressKey(), safeCount(goal.progress, goal.target))
                    putBoolean(goal.type.weeklyGoalClaimedKey(), goal.claimed)
                }
            }
            .putString(DAILY_LEADERBOARD_GOAL_DATE_KEY, safeDateKeyOrToday(state.dailyLeaderboardGoal.createdDate))
            .putString(DAILY_LEADERBOARD_GOAL_TYPE_KEY, state.dailyLeaderboardGoal.type.name)
            .putInt(
                DAILY_LEADERBOARD_GOAL_PROGRESS_KEY,
                safeCount(state.dailyLeaderboardGoal.progress, state.dailyLeaderboardGoal.target)
            )
            .putBoolean(DAILY_LEADERBOARD_GOAL_CLAIMED_KEY, state.dailyLeaderboardGoal.claimed)
            .putInt(DAILY_LEADERBOARD_GOAL_INITIAL_SCORE_KEY, state.dailyLeaderboardGoal.initialScore.coerceAtLeast(0))
            .putInt(DAILY_LEADERBOARD_GOAL_INITIAL_RANK_KEY, state.dailyLeaderboardGoal.initialRank.coerceAtLeast(0))
            .putString(PERSONAL_GOAL_DATE_KEY, safeDateKeyOrToday(state.personalGoal.createdDate))
            .putInt(PERSONAL_GOAL_TARGET_SCORE_KEY, state.personalGoal.targetScore.coerceAtLeast(1))
            .putInt(PERSONAL_GOAL_INITIAL_BEST_SCORE_KEY, state.personalGoal.initialBestScore.coerceAtLeast(0))
            .putInt(PERSONAL_GOAL_PROGRESS_SCORE_KEY, state.personalGoal.progressScore.coerceAtLeast(0))
            .putBoolean(PERSONAL_GOAL_CLAIMED_KEY, state.personalGoal.claimed)
            .putInt(PERSONAL_GOAL_REWARD_COINS_KEY, state.personalGoal.rewardCoins.coerceAtLeast(0))
            .putString(COMBO_CHALLENGE_DATE_KEY, safeDateKeyOrToday(state.comboChallenge.createdDate))
            .putString(COMBO_CHALLENGE_TYPE_KEY, state.comboChallenge.type.name)
            .putInt(COMBO_CHALLENGE_PROGRESS_KEY, safeCount(state.comboChallenge.progress, state.comboChallenge.target))
            .putInt(COMBO_CHALLENGE_GAMES_USED_KEY, state.comboChallenge.gamesUsed.coerceIn(0, 3))
            .putBoolean(COMBO_CHALLENGE_CLAIMED_KEY, state.comboChallenge.claimed)
            .putString(DAILY_MINI_TOURNAMENT_DATE_KEY, safeDateKeyOrToday(state.dailyMiniTournament.dateKey))
            .putString(DAILY_MINI_TOURNAMENT_MODE_KEY, state.dailyMiniTournament.mode.storageKey)
            .putInt(
                DAILY_MINI_TOURNAMENT_BEST_SCORE_KEY,
                state.dailyMiniTournament.bestScore.coerceAtLeast(0)
            )
            .putBoolean(DAILY_MINI_TOURNAMENT_CLAIMED_KEY, state.dailyMiniTournament.claimed)
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
            .putString(SEASON_QUEST_CLAIMED_IDS_KEY, state.season.quests.filter { it.claimed }.joinToString(",") { it.type.name })
            .putString(SEASON_QUEST_USED_COSMETICS_KEY, state.season.usedCosmeticKeys.joinToString(","))
            .apply {
                state.season.quests.forEach { quest ->
                    putInt(quest.type.seasonQuestProgressKey(), safeCount(quest.progress, quest.target))
                }
            }
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

    private fun loadShownModeTips(): Set<GameMode> {
        return sharedPreferences.getString(SHOWN_MODE_TIPS_KEY, null)
            .orEmpty()
            .split(",")
            .mapNotNull { key -> GameMode.entries.firstOrNull { it.storageKey == key } }
            .toSet()
    }

    private fun loadGamesPlayedByMode(): Map<GameMode, Int> {
        return GameMode.entries.associateWith { mode ->
            sharedPreferences.getInt(mode.gamesPlayedPreferenceKey(), 0).coerceAtLeast(0)
        }
    }

    private fun loadModeMasteryXpByMode(): Map<GameMode, Int> {
        return GameMode.entries.associateWith { mode ->
            sharedPreferences.getInt(mode.masteryXpPreferenceKey(), 0)
                .coerceIn(0, (MODE_MASTERY_MAX_LEVEL - 1) * MODE_MASTERY_XP_PER_LEVEL)
        }
    }

    private fun loadSelectedTargetSkin(): TargetSkin {
        val savedKey = sharedPreferences.getString(SELECTED_TARGET_SKIN_KEY, TargetSkin.ClassicTarget.storageKey)
        return TargetSkin.entries.firstOrNull { it.storageKey == savedKey } ?: TargetSkin.ClassicTarget
    }

    private fun loadUnlockedTargetSkins(): Set<TargetSkin> {
        val saved = sharedPreferences.getString(UNLOCKED_TARGET_SKINS_KEY, null)
        val unlocked = saved
            ?.split(",")
            ?.mapNotNull { key -> TargetSkin.entries.firstOrNull { it.storageKey == key } }
            ?.toSet()
            .orEmpty()
        return unlocked + TargetSkin.ClassicTarget
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
        val claimedQuestIds = sharedPreferences.getString(SEASON_QUEST_CLAIMED_IDS_KEY, "")
            .orEmpty()
            .toStringSet()
        val usedCosmetics = sharedPreferences.getString(SEASON_QUEST_USED_COSMETICS_KEY, "")
            .orEmpty()
            .toStringSet()
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
            },
            quests = loadSeasonQuests(claimedQuestIds, usedCosmetics),
            usedCosmeticKeys = usedCosmetics
        )
    }

    private fun loadSeasonQuests(
        claimedQuestIds: Set<String>,
        usedCosmetics: Set<String>
    ): List<SeasonQuestState> {
        return SeasonQuestType.entries.map { type ->
            val storedProgress = sharedPreferences.getInt(type.seasonQuestProgressKey(), 0)
            val progress = when (type) {
                SeasonQuestType.Use5Cosmetics -> maxOf(storedProgress, usedCosmetics.size)
                else -> storedProgress
            }.coerceIn(0, type.target)
            SeasonQuestState(
                type = type,
                progress = progress,
                claimed = type.name in claimedQuestIds && progress >= type.target
            )
        }
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
        val savedType = sharedPreferences.getString(WEEKLY_CHALLENGE_TYPE_KEY, null)
            ?.let { value -> WeeklyChallengeType.entries.firstOrNull { it.name == value } }
        val type = if (savedWeek == weekKey && savedType != null) {
            savedType
        } else {
            chooseWeeklyChallengeType(weekKey)
        }
        val progress = if (savedWeek == weekKey) {
            sharedPreferences.getInt(WEEKLY_CHALLENGE_PROGRESS_KEY, 0)
        } else {
            0
        }
        val safeProgress = progress.coerceIn(0, type.target)
        return ChallengeState(
            id = "weekly_${type.name.lowercase(Locale.US)}_$weekKey",
            type = type,
            titleRes = type.titleRes,
            descriptionRes = type.descriptionRes,
            target = type.target,
            progress = safeProgress,
            completed = safeProgress >= type.target,
            claimed = savedWeek == weekKey && sharedPreferences.getBoolean(WEEKLY_CHALLENGE_CLAIMED_KEY, false),
            rewardCoins = type.rewardCoins,
            createdDate = weekKey,
            remainingDays = remainingDaysInWeek()
        )
    }

    private fun loadWeeklyGoalBoard(): WeeklyGoalBoardState {
        val weekKey = weekDateKey()
        val savedWeek = sharedPreferences.getString(WEEKLY_GOAL_BOARD_WEEK_KEY, null)
            ?.takeIf { isValidWeekKey(it) }
        val isCurrentWeek = savedWeek == weekKey
        val goals = WeeklyGoalType.entries.map { type ->
            val progress = if (isCurrentWeek) {
                sharedPreferences.getInt(type.weeklyGoalProgressKey(), 0)
            } else {
                0
            }.coerceIn(0, type.target)
            WeeklyGoalState(
                type = type,
                progress = progress,
                claimed = isCurrentWeek &&
                    progress >= type.target &&
                    sharedPreferences.getBoolean(type.weeklyGoalClaimedKey(), false)
            )
        }
        return WeeklyGoalBoardState(
            weekKey = weekKey,
            goals = goals,
            bonusClaimed = isCurrentWeek &&
                goals.all { it.completed } &&
                sharedPreferences.getBoolean(WEEKLY_GOAL_BONUS_CLAIMED_KEY, false),
            bonusRewardCoins = 500
        )
    }

    private fun loadDailyLeaderboardGoal(): DailyLeaderboardGoalState {
        val today = todayDateKey()
        val savedDate = sharedPreferences.getString(DAILY_LEADERBOARD_GOAL_DATE_KEY, null)
            ?.takeIf { isValidDateKey(it) }
        val savedType = sharedPreferences.getString(DAILY_LEADERBOARD_GOAL_TYPE_KEY, null)
            ?.let { value -> DailyLeaderboardGoalType.entries.firstOrNull { it.name == value } }
        val type = if (savedDate == today && savedType != null) {
            savedType
        } else {
            chooseDailyLeaderboardGoalType(today)
        }
        val initialScore = if (savedDate == today) {
            sharedPreferences.getInt(DAILY_LEADERBOARD_GOAL_INITIAL_SCORE_KEY, currentAllTimeBestScore())
        } else {
            currentAllTimeBestScore()
        }.coerceAtLeast(0)
        val initialRank = if (savedDate == today) {
            sharedPreferences.getInt(DAILY_LEADERBOARD_GOAL_INITIAL_RANK_KEY, 0)
        } else {
            0
        }.coerceAtLeast(0)
        val progress = if (savedDate == today) {
            sharedPreferences.getInt(DAILY_LEADERBOARD_GOAL_PROGRESS_KEY, 0)
        } else {
            0
        }.coerceIn(0, type.target)
        return DailyLeaderboardGoalState(
            id = "daily_leaderboard_${type.name.lowercase(Locale.US)}_$today",
            type = type,
            titleRes = type.titleRes,
            descriptionRes = type.descriptionRes,
            target = type.target,
            progress = progress,
            completed = progress >= type.target,
            claimed = savedDate == today && sharedPreferences.getBoolean(DAILY_LEADERBOARD_GOAL_CLAIMED_KEY, false),
            rewardCoins = type.rewardCoins,
            createdDate = today,
            initialScore = initialScore,
            initialRank = initialRank
        )
    }

    private fun chooseDailyLeaderboardGoalType(dateKey: String): DailyLeaderboardGoalType {
        val seed = dateKey.fold(0) { acc, char -> acc + char.code }
        return DailyLeaderboardGoalType.entries.getOrNull(
            seed % DailyLeaderboardGoalType.entries.size.coerceAtLeast(1)
        ) ?: DailyLeaderboardGoalType.SubmitScore
    }

    private fun loadPersonalGoal(): PersonalGoalState {
        val today = todayDateKey()
        val savedDate = sharedPreferences.getString(PERSONAL_GOAL_DATE_KEY, null)
            ?.takeIf { isValidDateKey(it) }
        val initialBestScore = if (savedDate == today) {
            sharedPreferences.getInt(PERSONAL_GOAL_INITIAL_BEST_SCORE_KEY, currentAllTimeBestScore())
        } else {
            currentAllTimeBestScore()
        }.coerceAtLeast(0)
        val targetScore = if (savedDate == today) {
            sharedPreferences.getInt(PERSONAL_GOAL_TARGET_SCORE_KEY, personalGoalTargetFor(initialBestScore))
        } else {
            personalGoalTargetFor(initialBestScore)
        }.coerceAtLeast(
            (initialBestScore.toLong() + 1L)
                .coerceAtMost(Int.MAX_VALUE.toLong())
                .toInt()
        )
        val progressScore = if (savedDate == today) {
            sharedPreferences.getInt(PERSONAL_GOAL_PROGRESS_SCORE_KEY, initialBestScore)
        } else {
            initialBestScore
        }.coerceAtLeast(0)
        return PersonalGoalState(
            createdDate = today,
            targetScore = targetScore,
            initialBestScore = initialBestScore,
            progressScore = progressScore,
            completed = progressScore >= targetScore,
            claimed = savedDate == today && sharedPreferences.getBoolean(PERSONAL_GOAL_CLAIMED_KEY, false),
            rewardCoins = if (savedDate == today) {
                sharedPreferences.getInt(PERSONAL_GOAL_REWARD_COINS_KEY, personalGoalRewardFor(initialBestScore))
            } else {
                personalGoalRewardFor(initialBestScore)
            }.coerceAtLeast(0)
        )
    }

    private fun personalGoalTargetFor(bestScore: Int): Int {
        val safeBestScore = bestScore.coerceAtLeast(0)
        val increment = when {
            safeBestScore < 50 -> 5
            safeBestScore < 100 -> 10
            else -> 15
        }
        return (safeBestScore.toLong() + increment.toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
    }

    private fun personalGoalRewardFor(bestScore: Int): Int {
        return when {
            bestScore < 50 -> 100
            bestScore < 100 -> 250
            else -> 500
        }
    }

    private fun loadComboChallenge(): ComboChallengeState {
        val today = todayDateKey()
        val savedDate = sharedPreferences.getString(COMBO_CHALLENGE_DATE_KEY, null)
            ?.takeIf { isValidDateKey(it) }
        val savedType = sharedPreferences.getString(COMBO_CHALLENGE_TYPE_KEY, null)
            ?.let { value -> ComboChallengeType.entries.firstOrNull { it.name == value } }
        val type = if (savedDate == today && savedType != null) {
            savedType
        } else {
            chooseComboChallengeType(today)
        }
        val progress = if (savedDate == today) {
            sharedPreferences.getInt(COMBO_CHALLENGE_PROGRESS_KEY, 0)
        } else {
            0
        }.coerceIn(0, type.target)
        return ComboChallengeState(
            createdDate = today,
            type = type,
            titleRes = type.titleRes,
            descriptionRes = type.descriptionRes,
            target = type.target,
            progress = progress,
            gamesUsed = if (savedDate == today) {
                sharedPreferences.getInt(COMBO_CHALLENGE_GAMES_USED_KEY, 0).coerceIn(0, 3)
            } else {
                0
            },
            completed = progress >= type.target,
            claimed = savedDate == today && sharedPreferences.getBoolean(COMBO_CHALLENGE_CLAIMED_KEY, false),
            rewardCoins = type.rewardCoins
        )
    }

    private fun loadDailyMiniTournament(): DailyMiniTournamentState {
        val today = todayDateKey()
        val savedDate = sharedPreferences.getString(DAILY_MINI_TOURNAMENT_DATE_KEY, null)
            ?.takeIf { isValidDateKey(it) }
        val baseState = createDailyMiniTournamentState(dateKey = today)
        val savedMode = sharedPreferences.getString(DAILY_MINI_TOURNAMENT_MODE_KEY, null)
            ?.let { key -> GameMode.entries.firstOrNull { it.storageKey == key } }
        val mode = if (savedDate == today && savedMode != null) savedMode else baseState.mode
        val bestScore = if (savedDate == today) {
            sharedPreferences.getInt(DAILY_MINI_TOURNAMENT_BEST_SCORE_KEY, 0)
        } else {
            0
        }.coerceAtLeast(0)
        val targetScore = dailyMiniTournamentTargetFor(mode)
        return DailyMiniTournamentState(
            dateKey = today,
            mode = mode,
            bestScore = bestScore,
            targetScore = targetScore,
            rewardCoins = baseState.rewardCoins,
            completed = bestScore >= targetScore,
            claimed = savedDate == today &&
                bestScore >= targetScore &&
                sharedPreferences.getBoolean(DAILY_MINI_TOURNAMENT_CLAIMED_KEY, false)
        )
    }

    private fun loadPersonalRecords(): PersonalRecordsState {
        val legacyBestScore = loadBestScores().values.maxOrNull()?.coerceAtLeast(0) ?: 0
        val legacyBestCombo = sharedPreferences.getInt(LIFETIME_MAX_COMBO_KEY, 0).coerceAtLeast(0)
        return PersonalRecordsState(
            bestScore = maxOf(
                sharedPreferences.getInt(PERSONAL_RECORD_BEST_SCORE_KEY, 0).coerceAtLeast(0),
                legacyBestScore
            ),
            bestCombo = maxOf(
                sharedPreferences.getInt(PERSONAL_RECORD_BEST_COMBO_KEY, 0).coerceAtLeast(0),
                legacyBestCombo
            ),
            bestAccuracyPercent = sharedPreferences.getInt(PERSONAL_RECORD_BEST_ACCURACY_KEY, 0).coerceIn(0, 100),
            longestSurvivalSeconds = sharedPreferences.getInt(PERSONAL_RECORD_LONGEST_SURVIVAL_KEY, 0).coerceAtLeast(0),
            mostCoinsInGame = sharedPreferences.getInt(PERSONAL_RECORD_MOST_COINS_KEY, 0).coerceAtLeast(0)
        )
    }

    private fun loadSelectedProfileBadgeIds(): List<String> {
        return safeProfileBadgeIds(
            sharedPreferences.getString(SELECTED_PROFILE_BADGES_KEY, "")
                .orEmpty()
                .split(",")
        )
    }

    private fun safeProfileBadgeIds(ids: List<String>): List<String> {
        val validKeys = ProfileBadge.entries.map { it.storageKey }.toSet()
        return ids
            .map { it.trim() }
            .filter { it in validKeys }
            .distinct()
            .take(3)
    }

    private fun chooseComboChallengeType(dateKey: String): ComboChallengeType {
        val seed = dateKey.fold(0) { acc, char -> acc + char.code }
        return ComboChallengeType.entries.getOrNull(
            seed % ComboChallengeType.entries.size.coerceAtLeast(1)
        ) ?: ComboChallengeType.Combo5
    }

    private fun currentAllTimeBestScore(): Int {
        return GameMode.entries.maxOfOrNull { mode ->
            sharedPreferences.getInt(mode.bestScorePreferenceKey(), 0)
        }?.coerceAtLeast(0) ?: 0
    }

    private fun loadBonusHourState(): BonusHourState {
        val calendar = Calendar.getInstance()
        val startHour = chooseBonusHourStart(todayDateKey())
        val endHour = (startHour + 1) % 24
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentMinuteOfDay = currentHour * 60 + currentMinute
        val startMinuteOfDay = startHour * 60
        val endMinuteOfDay = startMinuteOfDay + 60
        val isActive = currentMinuteOfDay in startMinuteOfDay until endMinuteOfDay
        val minutesUntilStart = when {
            isActive -> 0
            currentMinuteOfDay < startMinuteOfDay -> startMinuteOfDay - currentMinuteOfDay
            else -> (24 * 60 - currentMinuteOfDay) + startMinuteOfDay
        }
        return BonusHourState(
            startHour = startHour,
            endHour = endHour,
            isActive = isActive,
            minutesUntilStart = minutesUntilStart.coerceIn(0, 24 * 60),
            coinBonusPercent = 25
        )
    }

    private fun chooseBonusHourStart(dateKey: String): Int {
        val seed = dateKey.fold(0) { acc, char -> acc + char.code }
        return BONUS_HOUR_CANDIDATES.getOrNull(seed % BONUS_HOUR_CANDIDATES.size.coerceAtLeast(1)) ?: 20
    }

    private fun chooseWeeklyChallengeType(weekKey: String): WeeklyChallengeType {
        val seed = weekKey.fold(0) { acc, char -> acc + char.code }
        return WeeklyChallengeType.entries.getOrNull(seed % WeeklyChallengeType.entries.size.coerceAtLeast(1))
            ?: WeeklyChallengeType.ClassicScore50
    }

    private fun remainingDaysInWeek(): Int {
        val calendar = Calendar.getInstance()
        val lastDayOfWeek = calendar.firstDayOfWeek + 6
        val normalizedLastDay = if (lastDayOfWeek > Calendar.SATURDAY) {
            lastDayOfWeek - Calendar.SATURDAY
        } else {
            lastDayOfWeek
        }
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val remaining = (normalizedLastDay - today).let { if (it < 0) it + 7 else it }
        return remaining.coerceIn(0, 6)
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

private fun GameMode.gamesPlayedPreferenceKey(): String {
    return "games_played_${storageKey}"
}

private fun GameMode.masteryXpPreferenceKey(): String {
    return "mode_mastery_xp_${storageKey}"
}

private fun GameMode.weeklyScorePreferenceKey(): String {
    return "weekly_score_${storageKey}"
}

private fun WeeklyGoalType.weeklyGoalProgressKey(): String {
    return "weekly_goal_${name.lowercase(Locale.US)}_progress"
}

private fun WeeklyGoalType.weeklyGoalClaimedKey(): String {
    return "weekly_goal_${name.lowercase(Locale.US)}_claimed"
}

private fun SeasonQuestType.seasonQuestProgressKey(): String {
    return "season_quest_${name.lowercase(Locale.US)}_progress"
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

/**
 * Hands the write off to the background. [SharedPreferences.Editor.commit] writes to disk
 * synchronously and would block the main thread during the game loop, whereas
 * [SharedPreferences.Editor.apply] updates the in-memory value immediately and defers the
 * disk write. Android guarantees pending apply() writes are flushed before the process dies.
 */
private fun SharedPreferences.Editor.commitSafely() {
    apply()
}

/**
 * Normalises the name, returning an empty string when none was entered. The displayed
 * default is deliberately not produced here: persisting a fixed Turkish value would show
 * "Oyuncu" to English users too. The UI resolves the fallback per language via
 * `ifBlank { ... }`.
 */
private fun safePlayerName(name: String): String {
    return name.trim().take(12)
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

private fun safeUnlockedTargetSkins(
    unlockedTargetSkins: Set<TargetSkin>,
    selectedTargetSkin: TargetSkin
): Set<TargetSkin> {
    return unlockedTargetSkins + selectedTargetSkin + TargetSkin.ClassicTarget
}

private fun safeSelectedTargetSkin(
    selectedTargetSkin: TargetSkin,
    unlockedTargetSkins: Set<TargetSkin>
): TargetSkin {
    return selectedTargetSkin.takeIf { it in unlockedTargetSkins } ?: TargetSkin.ClassicTarget
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
