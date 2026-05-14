package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import kotlin.math.PI
import kotlin.math.sin

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
    private val generatedTonePlayer = GeneratedTonePlayer()
    var isEnabled: Boolean = true

    init {
        preloadIfExists(GameSoundEffect.Hit, "sfx_hit")
        preloadIfExists(GameSoundEffect.Miss, "sfx_miss")
        preloadIfExists(GameSoundEffect.Combo, "sfx_combo")
        preloadIfExists(GameSoundEffect.Countdown, "sfx_countdown")
        preloadIfExists(GameSoundEffect.GameOver, "sfx_game_over")
    }

    fun play(effect: GameSoundEffect) {
        if (!isEnabled) return

        val soundId = soundIds[effect]
        if (soundId == null) {
            generatedTonePlayer.play(effect)
            return
        }
        soundPool.play(soundId, 0.42f, 0.42f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
        generatedTonePlayer.release()
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
    Combo,
    Countdown,
    GameOver
}

private class GeneratedTonePlayer {
    private val tracks = GameSoundEffect.entries.associateWith { effect ->
        createToneTrack(effect)
    }

    fun play(effect: GameSoundEffect) {
        val track = tracks[effect] ?: return
        runCatching {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.pause()
            }
            track.setPlaybackHeadPosition(0)
            track.play()
        }
    }

    fun release() {
        tracks.values.forEach { track ->
            runCatching {
                track.stop()
            }
            track.release()
        }
    }

    private fun createToneTrack(effect: GameSoundEffect): AudioTrack {
        val pcm = effect.toPcmTone()
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size)
            .build()
            .also { track ->
                track.write(pcm, 0, pcm.size)
                track.setVolume(effect.volume)
            }
    }

    private fun GameSoundEffect.toPcmTone(): ByteArray {
        val spec = when (this) {
            GameSoundEffect.Hit -> ToneSpec(frequencies = listOf(880.0, 1175.0), durationMs = 95)
            GameSoundEffect.Miss -> ToneSpec(frequencies = listOf(220.0, 165.0), durationMs = 150)
            GameSoundEffect.Combo -> ToneSpec(frequencies = listOf(980.0, 1318.0, 1568.0), durationMs = 190)
            GameSoundEffect.Countdown -> ToneSpec(frequencies = listOf(740.0), durationMs = 65)
            GameSoundEffect.GameOver -> ToneSpec(frequencies = listOf(392.0, 330.0, 262.0), durationMs = 260)
        }
        val samplesPerSegment = (SAMPLE_RATE * spec.durationMs / 1000) / spec.frequencies.size
        val sampleCount = samplesPerSegment * spec.frequencies.size
        val bytes = ByteArray(sampleCount * BYTES_PER_SAMPLE)
        var sampleIndex = 0

        spec.frequencies.forEach { frequency ->
            repeat(samplesPerSegment) { segmentIndex ->
                val envelope = calculateEnvelope(segmentIndex, samplesPerSegment)
                val value = (sin(2.0 * PI * frequency * segmentIndex / SAMPLE_RATE) *
                    Short.MAX_VALUE *
                    envelope *
                    MASTER_GAIN).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                bytes[sampleIndex * BYTES_PER_SAMPLE] = (value and 0xFF).toByte()
                bytes[sampleIndex * BYTES_PER_SAMPLE + 1] = ((value shr 8) and 0xFF).toByte()
                sampleIndex += 1
            }
        }
        return bytes
    }

    private fun calculateEnvelope(index: Int, total: Int): Double {
        val attack = (total * 0.12).toInt().coerceAtLeast(1)
        val release = (total * 0.22).toInt().coerceAtLeast(1)
        return when {
            index < attack -> index.toDouble() / attack
            index > total - release -> (total - index).toDouble() / release
            else -> 1.0
        }.coerceIn(0.0, 1.0)
    }

    private data class ToneSpec(
        val frequencies: List<Double>,
        val durationMs: Int
    )

    private val GameSoundEffect.volume: Float
        get() = when (this) {
            GameSoundEffect.Hit -> 0.32f
            GameSoundEffect.Miss -> 0.34f
            GameSoundEffect.Combo -> 0.36f
            GameSoundEffect.Countdown -> 0.24f
            GameSoundEffect.GameOver -> 0.34f
        }

    private companion object {
        private const val SAMPLE_RATE = 22_050
        private const val BYTES_PER_SAMPLE = 2
        private const val MASTER_GAIN = 0.45
    }
}
