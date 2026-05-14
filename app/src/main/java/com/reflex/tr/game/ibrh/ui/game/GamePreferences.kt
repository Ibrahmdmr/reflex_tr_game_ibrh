package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
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
