package com.reflex.tr.game.ibrh.ui.game

data class TargetPosition(
    val xFraction: Float = 0.5f,
    val yFraction: Float = 0.5f
)

data class GameUiState(
    val score: Int = 0,
    val bestScore: Int = 0,
    val isNewBestScore: Boolean = false,
    val difficultyLevel: Int = 1,
    val lives: Int = 3,
    val timeLeftSeconds: Int = 30,
    val targetPosition: TargetPosition = TargetPosition(),
    val targetSizeDp: Int = 82,
    val targetVisibleDurationMillis: Long = 1_800L,
    val targetLifetimeKey: Int = 0,
    val hasGameStarted: Boolean = false,
    val isGameOver: Boolean = false,
    val gameOverReason: String? = null,
    val hasUsedRewardContinue: Boolean = false,
    val canContinueWithReward: Boolean = false,
    val shouldRequestInterstitialAd: Boolean = false
)
