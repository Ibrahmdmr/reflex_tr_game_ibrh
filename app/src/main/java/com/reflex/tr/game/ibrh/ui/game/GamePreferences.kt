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
private const val LANGUAGE_TURKISH = "tr"
private const val LANGUAGE_ENGLISH = "en"
private const val DAILY_CHALLENGE_ID_KEY = "daily_challenge_id"
private const val DAILY_CHALLENGE_TYPE_KEY = "daily_challenge_type"
private const val DAILY_CHALLENGE_TARGET_KEY = "daily_challenge_target"
private const val DAILY_CHALLENGE_PROGRESS_KEY = "daily_challenge_progress"
private const val DAILY_CHALLENGE_COMPLETED_KEY = "daily_challenge_completed"
private const val DAILY_CHALLENGE_CREATED_DATE_KEY = "daily_challenge_created_date"
private const val COINS_KEY = "coins"
private const val XP_KEY = "xp"
private const val TOTAL_GAMES_KEY = "total_games"
private const val TOTAL_HITS_KEY = "total_hits"
private const val LIFETIME_MAX_COMBO_KEY = "lifetime_max_combo"
private const val SELECTED_THEME_KEY = "selected_theme"
private const val UNLOCKED_THEMES_KEY = "unlocked_themes"
private const val DAILY_REWARD_LAST_CLAIM_DATE_KEY = "daily_reward_last_claim_date"
private const val DAILY_REWARD_STREAK_KEY = "daily_reward_streak"
private const val ACHIEVEMENT_CLAIMED_IDS_KEY = "achievement_claimed_ids"
private const val WEEKLY_CHALLENGE_PROGRESS_KEY = "weekly_challenge_progress"
private const val WEEKLY_CHALLENGE_CREATED_DATE_KEY = "weekly_challenge_created_date"
private const val PLAYER_NAME_KEY = "player_name"
private const val PLAYER_NAME_PROMPT_COMPLETED_KEY = "player_name_prompt_completed"
private const val PLAYER_TITLE_KEY = "player_title"
private const val PLAYER_WEEKLY_SCORE_KEY = "player_weekly_score"
private const val PLAYER_WEEKLY_SCORE_DATE_KEY = "player_weekly_score_date"
private const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
private const val DEFAULT_PLAYER_NAME = "Oyuncu"

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

    val bestScoresFlow: Flow<Map<GameMode, Int>> = bestScoresState.asStateFlow()
    val languageFlow: Flow<AppLanguage> = languageState.asStateFlow()
    val soundEnabledFlow: Flow<Boolean> = soundEnabledState.asStateFlow()

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
                createdDate = today
            )
        }

        val nextType = chooseDailyChallengeType(previousType = type)
        val newState = DailyChallengeState(
            id = createDailyChallengeId(today, nextType),
            type = nextType,
            target = nextType.targetValue,
            progress = 0,
            completed = false,
            createdDate = today
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
            selectedTheme = loadSelectedTheme(),
            unlockedThemes = loadUnlockedThemes(),
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
        sharedPreferences.edit()
            .putInt(COINS_KEY, state.coins.coerceAtLeast(0))
            .putInt(XP_KEY, state.xp.coerceAtLeast(0))
            .putInt(TOTAL_GAMES_KEY, state.totalGames.coerceAtLeast(0))
            .putInt(TOTAL_HITS_KEY, state.totalHits.coerceAtLeast(0))
            .putInt(LIFETIME_MAX_COMBO_KEY, state.lifetimeMaxCombo.coerceAtLeast(0))
            .putString(SELECTED_THEME_KEY, state.selectedTheme.storageKey)
            .putString(
                UNLOCKED_THEMES_KEY,
                state.unlockedThemes.joinToString(separator = ",") { it.storageKey }
            )
            .putString(
                ACHIEVEMENT_CLAIMED_IDS_KEY,
                state.achievements.filter { it.claimed }.joinToString(separator = ",") { it.id }
            )
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
        val totalHits = sharedPreferences.getInt(TOTAL_HITS_KEY, 0)
        val maxCombo = sharedPreferences.getInt(LIFETIME_MAX_COMBO_KEY, 0)
        val bestScores = loadBestScores()
        val globalBest = bestScores.values.maxOrNull() ?: 0
        val fakeBest = bestScores[GameMode.FakeTarget] ?: 0
        val colorBest = bestScores[GameMode.ColorReflex] ?: 0

        return defaultAchievements().map { achievement ->
            val progress = when (achievement.type) {
                AchievementType.PlayGames -> totalGames
                AchievementType.ScoreInSingleGame -> globalBest
                AchievementType.HitTargets -> totalHits
                AchievementType.ReachCombo -> maxCombo
                AchievementType.FakeTargetScore -> fakeBest
                AchievementType.ColorReflexScore -> colorBest
                AchievementType.BreakRecord -> if (globalBest > 0) 1 else 0
            }.coerceAtMost(achievement.target)
            achievement.copy(
                progress = progress,
                unlocked = progress >= achievement.target,
                claimed = achievement.id in claimedIds
            )
        }
    }

    private fun defaultAchievements(): List<AchievementState> {
        return listOf(
            AchievementState("first_game", AchievementType.PlayGames, com.reflex.tr.game.ibrh.R.string.achievement_first_game_title, com.reflex.tr.game.ibrh.R.string.achievement_first_game_description, 1, 0, 50, 40, false, false),
            AchievementState("score_50", AchievementType.ScoreInSingleGame, com.reflex.tr.game.ibrh.R.string.achievement_score_50_title, com.reflex.tr.game.ibrh.R.string.achievement_score_50_description, 50, 0, 180, 120, false, false),
            AchievementState("hit_100", AchievementType.HitTargets, com.reflex.tr.game.ibrh.R.string.achievement_hit_100_title, com.reflex.tr.game.ibrh.R.string.achievement_hit_100_description, 100, 0, 220, 150, false, false),
            AchievementState("combo_master", AchievementType.ReachCombo, com.reflex.tr.game.ibrh.R.string.achievement_combo_master_title, com.reflex.tr.game.ibrh.R.string.achievement_combo_master_description, 10, 0, 160, 120, false, false),
            AchievementState("fake_master", AchievementType.FakeTargetScore, com.reflex.tr.game.ibrh.R.string.achievement_fake_master_title, com.reflex.tr.game.ibrh.R.string.achievement_fake_master_description, 20, 0, 200, 140, false, false),
            AchievementState("color_champion", AchievementType.ColorReflexScore, com.reflex.tr.game.ibrh.R.string.achievement_color_champion_title, com.reflex.tr.game.ibrh.R.string.achievement_color_champion_description, 20, 0, 200, 140, false, false),
            AchievementState("play_10", AchievementType.PlayGames, com.reflex.tr.game.ibrh.R.string.achievement_play_10_title, com.reflex.tr.game.ibrh.R.string.achievement_play_10_description, 10, 0, 260, 180, false, false),
            AchievementState("record_breaker", AchievementType.BreakRecord, com.reflex.tr.game.ibrh.R.string.achievement_record_breaker_title, com.reflex.tr.game.ibrh.R.string.achievement_record_breaker_description, 1, 0, 120, 90, false, false)
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
