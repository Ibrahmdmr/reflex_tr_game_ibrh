package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val GAME_PREFERENCES_NAME = "game_preferences"
private const val BEST_SCORE_KEY = "best_score"

class GamePreferences(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences(
        GAME_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val bestScoreState = MutableStateFlow(
        sharedPreferences.getInt(BEST_SCORE_KEY, 0)
    )

    val bestScoreFlow: Flow<Int> = bestScoreState.asStateFlow()

    suspend fun saveBestScore(score: Int) {
        val currentBestScore = sharedPreferences.getInt(BEST_SCORE_KEY, 0)
        if (score <= currentBestScore) return

        sharedPreferences.edit()
            .putInt(BEST_SCORE_KEY, score)
            .apply()

        bestScoreState.value = score
    }
}
