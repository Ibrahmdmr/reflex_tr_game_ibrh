package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.os.SystemClock
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
        preloadIfExists(GameSoundEffect.Perfect, "sfx_perfect")
        preloadIfExists(GameSoundEffect.Great, "sfx_great")
        preloadIfExists(GameSoundEffect.Combo, "sfx_combo")
        preloadIfExists(GameSoundEffect.ComboBig, "sfx_combo_big")
        preloadIfExists(GameSoundEffect.NewRecord, "sfx_new_record")
        preloadIfExists(GameSoundEffect.Unlock, "sfx_unlock")
        preloadIfExists(GameSoundEffect.Reward, "sfx_reward")
        preloadIfExists(GameSoundEffect.Countdown, "sfx_countdown")
        preloadIfExists(GameSoundEffect.GameOver, "sfx_game_over")
    }

    fun play(effect: GameSoundEffect) {
        if (!isEnabled) return
        if (!canPlay(effect)) return

        val soundId = soundIds[effect]
        if (soundId == null) {
            generatedTonePlayer.play(effect)
            return
        }
        runCatching {
            soundPool.play(soundId, effect.volume, effect.volume, 1, 0, 1f)
        }
    }

    fun release() {
        runCatching { soundPool.release() }
        generatedTonePlayer.release()
    }

    /**
     * Resolves sound effects by name so that files can be dropped into `res/raw` later without
     * touching this class; when a file is absent the caller falls back to generated tones.
     *
     * Because there is no static `R.raw` reference, resource shrinking would strip these files
     * from release builds — `res/raw/keep.xml` holds the matching keep rule. Remove that rule
     * only if this lookup is replaced with direct `R.raw.*` references.
     */
    private fun preloadIfExists(
        effect: GameSoundEffect,
        resourceName: String
    ) {
        @Suppress("DiscouragedApi")
        val resourceId = context.resources.getIdentifier(
            resourceName,
            "raw",
            context.packageName
        )
        if (resourceId == 0) return

        runCatching {
            soundPool.load(context, resourceId, 1)
        }.getOrNull()?.let { soundId ->
            soundIds[effect] = soundId
        }
    }

    private fun canPlay(effect: GameSoundEffect): Boolean {
        val now = SystemClock.elapsedRealtime()
        val lastPlayedAt = lastPlayedAtByEffect[effect] ?: 0L
        if (now - lastPlayedAt < effect.cooldownMillis) return false

        lastPlayedAtByEffect[effect] = now
        return true
    }

    private val lastPlayedAtByEffect = mutableMapOf<GameSoundEffect, Long>()
}

enum class GameSoundEffect {
    Hit,
    Miss,
    Perfect,
    Great,
    Combo,
    ComboBig,
    NewRecord,
    Unlock,
    Reward,
    Countdown,
    GameOver
}

private val GameSoundEffect.volume: Float
    get() = when (this) {
        GameSoundEffect.Hit -> 0.32f
        GameSoundEffect.Miss -> 0.34f
        GameSoundEffect.Perfect -> 0.34f
        GameSoundEffect.Great -> 0.32f
        GameSoundEffect.Combo -> 0.36f
        GameSoundEffect.ComboBig -> 0.38f
        GameSoundEffect.NewRecord -> 0.38f
        GameSoundEffect.Unlock -> 0.34f
        GameSoundEffect.Reward -> 0.34f
        GameSoundEffect.Countdown -> 0.24f
        GameSoundEffect.GameOver -> 0.34f
    }

private val GameSoundEffect.cooldownMillis: Long
    get() = when (this) {
        GameSoundEffect.Hit -> 42L
        GameSoundEffect.Miss -> 120L
        GameSoundEffect.Perfect,
        GameSoundEffect.Great -> 90L
        GameSoundEffect.Combo -> 180L
        GameSoundEffect.ComboBig -> 260L
        GameSoundEffect.NewRecord -> 800L
        GameSoundEffect.Unlock,
        GameSoundEffect.Reward -> 220L
        GameSoundEffect.Countdown -> 480L
        GameSoundEffect.GameOver -> 800L
    }

/**
 * Synthesises the fallback tones used when no `res/raw` sound file is bundled.
 *
 * Building the tracks means ~45k sine samples and eleven [AudioTrack] allocations, each of which
 * round-trips to the audio server. That is far too slow to run during composition, so the warm-up
 * happens on a background thread and playback stays silent until it finishes. [play] and [release]
 * are called from the main thread; the lock only guards publication of the warmed-up map.
 */
private class GeneratedTonePlayer {
    private val lock = Any()
    private var tracks: Map<GameSoundEffect, AudioTrack> = emptyMap()
    private var isReleased = false

    init {
        Thread(::warmUp, "reflex-tone-warmup").apply {
            isDaemon = true
            start()
        }
    }

    private fun warmUp() {
        val built = GameSoundEffect.entries.mapNotNull { effect ->
            runCatching { effect to createToneTrack(effect) }.getOrNull()
        }.toMap()

        val discard = synchronized(lock) {
            if (isReleased) {
                built.values
            } else {
                tracks = built
                emptyList()
            }
        }
        discard.forEach(::releaseTrack)
    }

    fun play(effect: GameSoundEffect) {
        val track = synchronized(lock) { tracks[effect] } ?: return
        runCatching {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.pause()
            }
            track.setPlaybackHeadPosition(0)
            track.play()
        }
    }

    fun release() {
        val toRelease = synchronized(lock) {
            isReleased = true
            val current = tracks
            tracks = emptyMap()
            current.values
        }
        toRelease.forEach(::releaseTrack)
    }

    private fun releaseTrack(track: AudioTrack) {
        runCatching {
            track.stop()
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
            GameSoundEffect.Perfect -> ToneSpec(frequencies = listOf(1046.0, 1568.0, 2093.0), durationMs = 170)
            GameSoundEffect.Great -> ToneSpec(frequencies = listOf(880.0, 1318.0), durationMs = 135)
            GameSoundEffect.Combo -> ToneSpec(frequencies = listOf(980.0, 1318.0, 1568.0), durationMs = 190)
            GameSoundEffect.ComboBig -> ToneSpec(frequencies = listOf(784.0, 1175.0, 1568.0, 2093.0), durationMs = 240)
            GameSoundEffect.NewRecord -> ToneSpec(frequencies = listOf(988.0, 1318.0, 1760.0, 2349.0), durationMs = 300)
            GameSoundEffect.Unlock -> ToneSpec(frequencies = listOf(659.0, 988.0, 1318.0), durationMs = 220)
            GameSoundEffect.Reward -> ToneSpec(frequencies = listOf(740.0, 988.0, 1480.0), durationMs = 210)
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

    private companion object {
        private const val SAMPLE_RATE = 22_050
        private const val BYTES_PER_SAMPLE = 2
        private const val MASTER_GAIN = 0.45
    }
}
