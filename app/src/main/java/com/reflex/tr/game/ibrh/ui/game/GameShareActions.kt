package com.reflex.tr.game.ibrh.ui.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
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

internal fun shareScore(
    context: Context,
    text: String,
    chooserTitle: String,
    score: Int,
    mode: GameMode,
    isNewRecord: Boolean
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
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.ScoreShared,
            params = Bundle().apply {
                putInt(FirebaseParam.Score.key, score.coerceAtLeast(0))
                putString(FirebaseParam.Mode.key, mode.storageKey)
                putBoolean(FirebaseParam.IsNewRecord.key, isNewRecord)
            }
        )
    }
}
