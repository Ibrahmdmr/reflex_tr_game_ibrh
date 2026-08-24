package com.reflex.tr.game.ibrh.share

data class ScoreShareLabels(
    val title: String,
    val slogan: String,
    val score: String,
    val bestScore: String,
    val accuracy: String,
    val combo: String,
    val coins: String,
    val theme: String,
    val newRecord: String,
    val challenge: String,
    val storeHint: String
)

/** Clamped here so the generator need not re-check; a blank label drops its row. */
data class ScoreShareData(
    val score: Int,
    val modeName: String,
    val bestScore: Int,
    val accuracyText: String,
    val maxCombo: Int,
    val earnedCoins: Int,
    val isNewBestScore: Boolean,
    val labels: ScoreShareLabels
) {
    init {
        require(score >= 0 && bestScore >= 0 && maxCombo >= 0 && earnedCoins >= 0) {
            "Score card figures must not be negative"
        }
    }

    companion object {
        fun of(
            score: Int,
            modeName: String,
            bestScore: Int,
            accuracyText: String,
            maxCombo: Int,
            earnedCoins: Int,
            isNewBestScore: Boolean,
            labels: ScoreShareLabels
        ) = ScoreShareData(
            score = score.coerceAtLeast(0),
            modeName = modeName.trim(),
            bestScore = bestScore.coerceAtLeast(0),
            accuracyText = accuracyText.trim(),
            maxCombo = maxCombo.coerceAtLeast(0),
            earnedCoins = earnedCoins.coerceAtLeast(0),
            isNewBestScore = isNewBestScore,
            labels = labels
        )
    }
}
