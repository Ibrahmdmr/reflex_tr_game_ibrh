package com.reflex.tr.game.ibrh.ui.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
import kotlin.random.Random

internal fun chooseQuickGameMode(dailyFeaturedMode: GameMode): GameMode {
    val modes = GameMode.entries.toList()
    val weightedModes = modes + listOf(dailyFeaturedMode, dailyFeaturedMode)
    return weightedModes.getOrElse(Random.nextInt(weightedModes.size.coerceAtLeast(1))) {
        GameMode.Classic
    }
}

internal fun shareInvite(
    context: Context,
    text: String,
    chooserTitle: String,
    onShareLaunched: () -> Unit
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooserIntent = Intent.createChooser(sendIntent, chooserTitle).apply {
        if (context !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    val launched = runCatching {
        context.startActivity(chooserIntent)
    }.isSuccess
    if (launched) {
        onShareLaunched()
    }
}

/** Coins paid for the first score share of the day. Mirrors GameViewModel's own constant. */
internal const val ScoreShareRewardCoins = 50

internal fun Context.showShortToast(message: String) {
    runCatching { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
}

/** Carries only gameplay figures — never playerName or uid. */
internal fun logScoreShareEvent(
    event: FirebaseEvent,
    uiState: GameUiState,
    score: Int
) {
    logGameEvent(event) {
        putString(FirebaseParam.Mode.key, uiState.selectedMode.storageKey)
        putInt(FirebaseParam.Score.key, score.coerceAtLeast(0))
        putInt(FirebaseParam.BestScore.key, uiState.bestScore.coerceAtLeast(0))
        putInt(FirebaseParam.MaxCombo.key, uiState.maxCombo.coerceAtLeast(0))
        putInt(FirebaseParam.EarnedCoin.key, uiState.earnedCoinsThisGame.coerceAtLeast(0))
        putInt(
            FirebaseParam.Accuracy.key,
            if (uiState.totalAttempts > 0) {
                ((uiState.successfulHits * 100f) / uiState.totalAttempts).toInt().coerceIn(0, 100)
            } else {
                0
            }
        )
    }
}
