package com.reflex.tr.game.ibrh.ui.game

/** Coin wallet arithmetic. Extracted from GameViewModel so the rules stay unit testable on their own. */

internal fun addCoins(progression: ProgressionState, coins: Int): ProgressionState {
    val safeCoins = coins.coerceAtLeast(0)
    val totalCoins = progression.coins.toLong() + safeCoins.toLong()
    val totalEarned = progression.totalCoinsEarned.toLong() + safeCoins.toLong()
    return progression.copy(
        coins = totalCoins.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(),
        totalCoinsEarned = totalEarned.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    )
}

internal fun addSpentCoins(currentSpent: Int, spentCoins: Int): Int {
    return (currentSpent.coerceAtLeast(0).toLong() + spentCoins.coerceAtLeast(0).toLong())
        .coerceAtMost(Int.MAX_VALUE.toLong())
        .toInt()
}
