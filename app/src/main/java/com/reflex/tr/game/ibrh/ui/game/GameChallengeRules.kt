package com.reflex.tr.game.ibrh.ui.game

/** Weekly-challenge and personal-record rules. Extracted from GameViewModel so the rules stay unit testable on their own. */

internal fun advanceWeeklyChallenge(
    challenge: ChallengeState,
    mode: GameMode,
    score: Int,
    maxCombo: Int
): ChallengeState {
    if (challenge.claimed) return challenge
    val nextProgress = when (challenge.type) {
        WeeklyChallengeType.ClassicScore50 ->
            if (mode == GameMode.Classic) maxOf(challenge.progress, score) else challenge.progress
        WeeklyChallengeType.ColorReflexScore30 ->
            if (mode == GameMode.ColorReflex) maxOf(challenge.progress, score) else challenge.progress
        WeeklyChallengeType.FakeTargetScore20 ->
            if (mode == GameMode.FakeTarget) maxOf(challenge.progress, score) else challenge.progress
        WeeklyChallengeType.Play20Games ->
            challenge.progress + 1
        WeeklyChallengeType.Combo10 ->
            maxOf(challenge.progress, maxCombo)
    }.coerceIn(0, challenge.target)
    return challenge.copy(
        progress = nextProgress,
        completed = nextProgress >= challenge.target
    )
}

internal fun personalRecordsBrokenByGame(
    progression: ProgressionState,
    bestScoresByMode: Map<GameMode, Int>,
    mode: GameMode,
    score: Int,
    maxCombo: Int,
    accuracyPercent: Int,
    survivalSeconds: Int,
    earnedCoins: Int
): Set<PersonalRecordType> {
    val records = mutableSetOf<PersonalRecordType>()
    if (score > progression.personalRecords.bestScore.coerceAtLeast(0)) {
        records += PersonalRecordType.HighestScore
    }
    if (maxCombo > progression.personalRecords.bestCombo.coerceAtLeast(0)) {
        records += PersonalRecordType.HighestCombo
    }
    if (accuracyPercent.coerceIn(0, 100) > progression.personalRecords.bestAccuracyPercent.coerceIn(0, 100)) {
        records += PersonalRecordType.BestAccuracy
    }
    if (survivalSeconds > progression.personalRecords.longestSurvivalSeconds.coerceAtLeast(0)) {
        records += PersonalRecordType.LongestSurvival
    }
    if (earnedCoins > progression.personalRecords.mostCoinsInGame.coerceAtLeast(0)) {
        records += PersonalRecordType.MostCoinsInGame
    }
    if (score > (bestScoresByMode[mode] ?: 0).coerceAtLeast(0)) {
        records += when (mode) {
            GameMode.Classic -> PersonalRecordType.ClassicBest
            GameMode.MovingTarget -> PersonalRecordType.MovingTargetBest
            GameMode.FakeTarget -> PersonalRecordType.FakeTargetBest
            GameMode.ColorReflex -> PersonalRecordType.ColorReflexBest
        }
    }
    return records
}
