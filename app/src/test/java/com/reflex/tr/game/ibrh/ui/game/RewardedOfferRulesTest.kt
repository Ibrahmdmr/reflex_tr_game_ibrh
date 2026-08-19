package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The offers decide what the player is asked to watch an ad for. What matters is that nothing is
 * offered that cannot pay out, and that the Game Over panel never turns into an ad board.
 */
class RewardedOfferRulesTest {

    private val adReady = RewardedAdUiState(isReady = true)
    private val adNotReady = RewardedAdUiState(isReady = false)

    // --- bonuses ---

    @Test
    fun `every offer is listed so the player can see what a run is worth`() {
        val offers = bonusOffers(ProgressionState(), adReady)
        assertEquals(RewardedOfferType.entries.size, offers.size)
        assertEquals(RewardedOfferType.entries.toSet(), offers.map { it.type }.toSet())
    }

    @Test
    fun `run-only offers are marked rather than shown as available`() {
        val offers = bonusOffers(ProgressionState(), adReady).associateBy { it.type }
        assertEquals(
            RewardedOfferAvailability.DuringGameOnly,
            offers.getValue(RewardedOfferType.ContinueGame).availability
        )
        assertEquals(
            RewardedOfferAvailability.DuringGameOnly,
            offers.getValue(RewardedOfferType.DoubleGameCoins).availability
        )
    }

    @Test
    fun `a spent daily chest reads as spent, not as a broken button`() {
        val progression = ProgressionState(
            coinChest = CoinChestState(openedToday = 3, maxOpensPerDay = 3)
        )
        val chest = bonusOffers(progression, adReady).first {
            it.type == RewardedOfferType.FreeDailyChest
        }
        assertEquals(RewardedOfferAvailability.LimitReached, chest.availability)
        assertFalse(chest.isAvailable)
        assertEquals(0, chest.remaining)
        assertEquals(3, chest.usedToday)
    }

    @Test
    fun `an offer with chances left is available only while an ad is`() {
        val progression = ProgressionState()
        assertTrue(
            bonusOffers(progression, adReady)
                .first { it.type == RewardedOfferType.FreeDailyChest }
                .isAvailable
        )
        assertEquals(
            RewardedOfferAvailability.AdNotReady,
            bonusOffers(progression, adNotReady)
                .first { it.type == RewardedOfferType.FreeDailyChest }
                .availability
        )
    }

    @Test
    fun `streak protection waits for a streak that is actually at risk`() {
        val safe = bonusOffers(ProgressionState(), adReady)
            .first { it.type == RewardedOfferType.StreakProtect }
        assertEquals(RewardedOfferAvailability.NotApplicable, safe.availability)

        val atRisk = bonusOffers(
            ProgressionState(dailyReward = DailyRewardState(canProtectStreak = true)),
            adReady
        ).first { it.type == RewardedOfferType.StreakProtect }
        assertTrue(atRisk.isAvailable)
    }

    @Test
    fun `an ad showing right now blocks every offer`() {
        val showing = RewardedAdUiState(isReady = true, isShowing = true)
        assertTrue(bonusOffers(ProgressionState(), showing).none { it.isAvailable })
    }

    // --- game over ---

    @Test
    fun `nothing is offered when nothing can pay out`() {
        val offers = gameOverRewardedOffers(
            canContinue = false,
            isContinueReady = false,
            baseCoinsThisGame = 0,
            isCoinDoubleClaimed = false,
            rewardedAdUiState = adReady
        )
        assertTrue(offers.isEmpty())
    }

    @Test
    fun `continue comes first because it is the only one that changes the run`() {
        val offers = gameOverRewardedOffers(
            canContinue = true,
            isContinueReady = false,
            baseCoinsThisGame = 40,
            isCoinDoubleClaimed = false,
            rewardedAdUiState = adReady
        )
        assertEquals(RewardedOfferType.ContinueGame, offers.first().type)
        assertTrue(offers.size <= MAX_GAME_OVER_OFFERS)
    }

    @Test
    fun `doubling is left once continuing is off the table`() {
        val offers = gameOverRewardedOffers(
            canContinue = false,
            isContinueReady = false,
            baseCoinsThisGame = 40,
            isCoinDoubleClaimed = false,
            rewardedAdUiState = adReady
        )
        assertEquals(listOf(RewardedOfferType.DoubleGameCoins), offers.map { it.type })
        assertEquals(40, offers.first().rewardCoins)
    }

    @Test
    fun `an already doubled run is not asked twice`() {
        val offers = gameOverRewardedOffers(
            canContinue = false,
            isContinueReady = false,
            baseCoinsThisGame = 40,
            isCoinDoubleClaimed = true,
            rewardedAdUiState = adReady
        )
        assertTrue(offers.isEmpty())
    }

    @Test
    fun `a reward already paid for survives an ad that has since unloaded`() {
        val offers = gameOverRewardedOffers(
            canContinue = true,
            isContinueReady = true,
            baseCoinsThisGame = 40,
            isCoinDoubleClaimed = false,
            rewardedAdUiState = adNotReady
        )
        assertEquals(listOf(RewardedOfferType.ContinueGame), offers.map { it.type })
    }

    @Test
    fun `every offer maps onto an action that already existed`() {
        assertEquals(
            RewardedOfferType.entries.size,
            RewardedOfferType.entries.map { it.action }.distinct().size
        )
    }
}
