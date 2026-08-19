package com.reflex.tr.game.ibrh.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Interstitial pacing decides how often the game interrupts a player, so every guard and the
 * order they are checked in is pinned down here.
 */
class AdPacingManagerTest {

    private val config = AdConfig.Default

    /** A run that clears every guard, so each test can break exactly one thing. */
    private fun decide(
        completedGames: Int = 10,
        nextInterstitialGame: Int = 10,
        lastInterstitialElapsedMillis: Long = 0L,
        lastRewardedElapsedMillis: Long = 0L,
        score: Int = 40,
        bestScore: Int = 200,
        isNewBestScore: Boolean = false,
        gameDurationMillis: Long = 60_000L,
        hasNoAdsEntitlement: Boolean = false,
        nowElapsedMillis: Long = 1_000_000L
    ) = AdPacingManager.interstitialDecision(
        state = AdPacingState(
            completedGames = completedGames,
            nextInterstitialGame = nextInterstitialGame,
            lastInterstitialElapsedMillis = lastInterstitialElapsedMillis,
            lastRewardedElapsedMillis = lastRewardedElapsedMillis
        ),
        config = config,
        score = score,
        bestScore = bestScore,
        isNewBestScore = isNewBestScore,
        gameDurationMillis = gameDurationMillis,
        hasNoAdsEntitlement = hasNoAdsEntitlement,
        nowElapsedMillis = nowElapsedMillis
    )

    @Test
    fun `a normal run past every guard is eligible`() {
        val decision = decide()
        assertTrue(decision.eligible)
        assertNull(decision.skipReason)
    }

    @Test
    fun `an entitlement outranks every other guard`() {
        val decision = decide(hasNoAdsEntitlement = true, completedGames = 500)
        assertFalse(decision.eligible)
        assertEquals(InterstitialSkipReason.NoAdsEntitlement, decision.skipReason)
    }

    @Test
    fun `the opening games are free`() {
        repeat(config.firstInterstitialFreeGames) { index ->
            val decision = decide(completedGames = index + 1, nextInterstitialGame = 0)
            assertFalse(decision.eligible)
            assertEquals(InterstitialSkipReason.EarlyGames, decision.skipReason)
        }
        assertTrue(
            decide(completedGames = config.firstInterstitialFreeGames + 1, nextInterstitialGame = 0)
                .eligible
        )
    }

    @Test
    fun `runs before the next scheduled game are skipped`() {
        val decision = decide(completedGames = 9, nextInterstitialGame = 12)
        assertFalse(decision.eligible)
        assertEquals(InterstitialSkipReason.GameInterval, decision.skipReason)
    }

    @Test
    fun `an interstitial inside the cooldown is skipped`() {
        val now = 1_000_000L
        val decision = decide(
            nowElapsedMillis = now,
            lastInterstitialElapsedMillis = now - config.interstitialCooldownMillis + 1
        )
        assertEquals(InterstitialSkipReason.InterstitialCooldown, decision.skipReason)
        assertTrue(
            decide(
                nowElapsedMillis = now,
                lastInterstitialElapsedMillis = now - config.interstitialCooldownMillis
            ).eligible
        )
    }

    @Test
    fun `a recent rewarded ad buys the player quiet time`() {
        val now = 1_000_000L
        val decision = decide(
            nowElapsedMillis = now,
            lastRewardedElapsedMillis = now - config.interstitialCooldownMillis + 1
        )
        assertEquals(InterstitialSkipReason.RewardedCooldown, decision.skipReason)
    }

    @Test
    fun `short or low scoring runs are left alone`() {
        assertEquals(
            InterstitialSkipReason.ShortGame,
            decide(gameDurationMillis = config.shortGameThresholdMillis - 1).skipReason
        )
        assertEquals(
            InterstitialSkipReason.ShortGame,
            decide(score = config.shortGameScoreThreshold).skipReason
        )
    }

    @Test
    fun `the run a player wants to sit with is never interrupted`() {
        assertEquals(
            InterstitialSkipReason.HighValueRun,
            decide(isNewBestScore = true).skipReason
        )
        // 80% of the best score counts as a near miss worth savouring.
        assertEquals(
            InterstitialSkipReason.HighValueRun,
            decide(bestScore = 100, score = 80).skipReason
        )
        assertTrue(decide(bestScore = 100, score = 79).eligible)
    }

    @Test
    fun `a first best score does not count as a high value run on its own`() {
        assertTrue(decide(bestScore = 0, score = 40).eligible)
    }

    @Test
    fun `the next scheduled game always sits inside the configured interval`() {
        repeat(50) {
            val next = AdPacingManager.nextInterstitialGame(completedGames = 7, config = config)
            assertTrue(next >= 7 + config.interstitialMinGameInterval)
            assertTrue(next <= 7 + config.interstitialMaxGameInterval)
        }
    }

    @Test
    fun `an entitlement that has lapsed no longer hides ads`() {
        val lapsed = PremiumState(isPremiumUser = true, expiresAtMillis = 1_000L)
        assertFalse(lapsed.grants(PremiumFeature.NoInterstitials, nowMillis = 2_000L))
        assertTrue(lapsed.grants(PremiumFeature.NoInterstitials, nowMillis = 500L))
    }

    @Test
    fun `a default player owns nothing`() {
        val state = PremiumState()
        assertFalse(state.isActive())
        assertFalse(state.grants(PremiumFeature.NoInterstitials))
    }

    @Test
    fun `a no ads entitlement needs no expiry to work`() {
        val noAds = PremiumState(isNoAdsUser = true)
        assertTrue(noAds.grants(PremiumFeature.NoInterstitials))
    }
}
