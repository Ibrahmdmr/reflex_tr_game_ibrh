package com.reflex.tr.game.ibrh.ads

import androidx.compose.runtime.Immutable
import kotlin.random.Random

enum class InterstitialSkipReason(val storageKey: String) {
    NoAdsEntitlement("no_ads_entitlement"),
    EarlyGames("early_games"),
    GameInterval("game_interval"),
    InterstitialCooldown("interstitial_cooldown"),
    RewardedCooldown("rewarded_cooldown"),
    ShortGame("short_game"),
    HighValueRun("high_value_run")
}

/** The counters persist so the "first N games free" grace cannot be farmed by restarting; the
 *  elapsed-time marks stay in memory, where they mean nothing across a reboot. */
@Immutable
data class AdPacingState(
    val completedGames: Int = 0,
    val nextInterstitialGame: Int = 0,
    val lastInterstitialElapsedMillis: Long = 0L,
    val lastRewardedElapsedMillis: Long = 0L
)

/** [skipReason] is null exactly when [eligible] is true. */
@Immutable
data class InterstitialDecision(
    val eligible: Boolean,
    val skipReason: InterstitialSkipReason?
)

object AdPacingManager {

    private val Skipped = { reason: InterstitialSkipReason ->
        InterstitialDecision(eligible = false, skipReason = reason)
    }

    // Most-absolute guard first, so the reported reason is the one that actually matters.
    fun interstitialDecision(
        state: AdPacingState,
        config: AdConfig,
        score: Int,
        bestScore: Int,
        isNewBestScore: Boolean,
        gameDurationMillis: Long,
        hasNoAdsEntitlement: Boolean,
        nowElapsedMillis: Long
    ): InterstitialDecision {
        if (hasNoAdsEntitlement) return Skipped(InterstitialSkipReason.NoAdsEntitlement)
        if (state.completedGames <= config.firstInterstitialFreeGames) {
            return Skipped(InterstitialSkipReason.EarlyGames)
        }
        if (state.completedGames < state.nextInterstitialGame) {
            return Skipped(InterstitialSkipReason.GameInterval)
        }
        if (!hasCooledDown(state.lastInterstitialElapsedMillis, nowElapsedMillis, config)) {
            return Skipped(InterstitialSkipReason.InterstitialCooldown)
        }
        // A player who just chose to watch a rewarded ad has already given their attention.
        if (!hasCooledDown(state.lastRewardedElapsedMillis, nowElapsedMillis, config)) {
            return Skipped(InterstitialSkipReason.RewardedCooldown)
        }
        val isShortGame = gameDurationMillis < config.shortGameThresholdMillis ||
            score <= config.shortGameScoreThreshold
        if (isShortGame) return Skipped(InterstitialSkipReason.ShortGame)
        // Never on the run the player most wants to sit with.
        val isHighValueRun = isNewBestScore ||
            (bestScore > 0 && score >= (bestScore * config.highScoreDelayRatio).toInt())
        if (isHighValueRun) return Skipped(InterstitialSkipReason.HighValueRun)

        return InterstitialDecision(eligible = true, skipReason = null)
    }

    /** A zero mark means "never happened", which counts as cooled down. */
    private fun hasCooledDown(
        markElapsedMillis: Long,
        nowElapsedMillis: Long,
        config: AdConfig
    ): Boolean = markElapsedMillis <= 0L ||
        nowElapsedMillis - markElapsedMillis >= config.interstitialCooldownMillis

    fun nextInterstitialGame(
        completedGames: Int,
        config: AdConfig,
        random: Random = Random
    ): Int {
        val from = config.interstitialMinGameInterval.coerceAtLeast(1)
        val until = config.interstitialMaxGameInterval.coerceAtLeast(from) + 1
        return completedGames + random.nextInt(from, until)
    }
}
