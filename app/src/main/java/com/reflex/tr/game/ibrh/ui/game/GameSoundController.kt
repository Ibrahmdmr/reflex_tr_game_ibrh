package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class GameSoundController(
    private val context: Context
) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = mutableMapOf<GameSoundEffect, Int>()

    init {
        preloadIfExists(GameSoundEffect.Hit, "sfx_hit")
        preloadIfExists(GameSoundEffect.Miss, "sfx_miss")
        preloadIfExists(GameSoundEffect.GameOver, "sfx_game_over")
    }

    fun play(effect: GameSoundEffect) {
        val soundId = soundIds[effect] ?: return
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }

    private fun preloadIfExists(
        effect: GameSoundEffect,
        resourceName: String
    ) {
        val resourceId = context.resources.getIdentifier(
            resourceName,
            "raw",
            context.packageName
        )
        if (resourceId == 0) return

        soundIds[effect] = soundPool.load(context, resourceId, 1)
    }
}

enum class GameSoundEffect {
    Hit,
    Miss,
    GameOver
}
