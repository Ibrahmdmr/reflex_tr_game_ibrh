package com.reflex.tr.game.ibrh.ads

data class AdConfig(
    val firstInterstitialFreeGames: Int,
    val interstitialMinGameInterval: Int,
    val interstitialMaxGameInterval: Int,
    val interstitialCooldownMillis: Long,
    val shortGameThresholdMillis: Long,
    val shortGameScoreThreshold: Int,
    val highScoreDelayRatio: Float,
    val rewardedContinueSeconds: Int,
    val doubleCoinMultiplier: Int
) {
    companion object {
        val Default = AdConfig(
            firstInterstitialFreeGames = 3,
            interstitialMinGameInterval = 3,
            interstitialMaxGameInterval = 5,
            interstitialCooldownMillis = 120_000L,
            shortGameThresholdMillis = 15_000L,
            shortGameScoreThreshold = 2,
            highScoreDelayRatio = 0.8f,
            rewardedContinueSeconds = 10,
            doubleCoinMultiplier = 2
        )
    }
}
