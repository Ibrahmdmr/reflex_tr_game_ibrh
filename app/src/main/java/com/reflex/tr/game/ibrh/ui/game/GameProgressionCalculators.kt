package com.reflex.tr.game.ibrh.ui.game

internal fun advanceDailyChallengeForHit(
    state: DailyChallengeState,
    mode: GameMode,
    score: Int,
    combo: Int
): DailyChallengeState {
    if (state.completed) return state

    val nextProgress = when (state.type) {
        DailyChallenge.ClassicScore20 -> if (mode == GameMode.Classic) {
            score.coerceAtMost(state.target)
        } else {
            state.progress
        }
        DailyChallenge.MovingTargetHits10 -> if (mode == GameMode.MovingTarget) {
            score.coerceAtMost(state.target)
        } else {
            state.progress
        }
        DailyChallenge.FakeTargetScore5 -> if (mode == GameMode.FakeTarget) {
            score.coerceAtMost(state.target)
        } else {
            state.progress
        }
        DailyChallenge.ColorReflexHits10 -> if (mode == GameMode.ColorReflex) {
            score.coerceAtMost(state.target)
        } else {
            state.progress
        }
        DailyChallenge.Combo5 -> combo.coerceAtMost(state.target)
        DailyChallenge.Play3Games,
        DailyChallenge.OpenLeaderboard,
        DailyChallenge.VisitShop -> state.progress
    }
    val next = state.copy(
        progress = nextProgress,
        completed = nextProgress >= state.target
    )
    // Same instance when nothing moved, so the caller can skip persisting with an identity check.
    return if (next == state) state else next
}

internal fun advanceDailyChallengeForGameCompleted(
    state: DailyChallengeState
): DailyChallengeState {
    if (state.completed || state.type != DailyChallenge.Play3Games) return state

    val nextProgress = (state.progress + 1).coerceAtMost(state.target)
    return state.copy(
        progress = nextProgress,
        completed = nextProgress >= state.target
    )
}

internal fun advanceOneMoreGameBonusAfterCompletedGame(
    state: OneMoreGameBonusState,
    bonusAwarded: Boolean
): OneMoreGameBonusState {
    return state.copy(
        gamesPlayedToday = state.gamesPlayedToday + 1,
        bonusClaimedToday = state.bonusClaimedToday || bonusAwarded
    )
}

internal fun calculateEarnedCoins(
    score: Int,
    maxCombo: Int,
    isNewBestScore: Boolean
): Int {
    val scoreCoins = score * 5
    val comboBonus = when {
        maxCombo >= 20 -> 120
        maxCombo >= 10 -> 70
        maxCombo >= 5 -> 35
        maxCombo >= 2 -> 15
        else -> 0
    }
    val recordBonus = if (isNewBestScore) 80 else 0
    return (scoreCoins + comboBonus + recordBonus).coerceAtLeast(if (score > 0) 10 else 0)
}

internal fun calculateEarnedXp(
    score: Int,
    hits: Int,
    maxCombo: Int,
    isNewBestScore: Boolean
): Int {
    val comboXp = when {
        maxCombo >= 10 -> 45
        maxCombo >= 5 -> 25
        else -> 0
    }
    return 20 + (score * 3) + hits + comboXp + if (isNewBestScore) 60 else 0
}

internal fun updateAchievementProgress(
    progression: ProgressionState,
    score: Int? = null,
    isNewBestScore: Boolean = false
): ProgressionState {
    val previousAchievements = progression.achievements
    val unlockedPaidThemes = progression.unlockedThemes.count { it.coinPrice > 0 }
    val updatedAchievements = previousAchievements.map { achievement ->
        val metricProgress = when (achievement.type) {
            AchievementType.BreakRecord -> if (isNewBestScore) 1 else achievement.progress
            AchievementType.ScoreInSingleGame -> maxOf(achievement.progress, score ?: 0)
            AchievementType.PlayGames -> progression.totalGames
            AchievementType.ReachCombo -> progression.lifetimeMaxCombo
            AchievementType.RewardedAds -> progression.rewardedAdWatchCount
            AchievementType.ThemesUnlocked -> unlockedPaidThemes
        }
        val nextProgress = maxOf(achievement.progress, metricProgress).coerceAtMost(achievement.target)
        achievement.copy(
            progress = nextProgress,
            unlocked = nextProgress >= achievement.target
        )
    }
    val newlyUnlocked = updatedAchievements.filter { updated ->
        updated.unlocked && previousAchievements.none { it.id == updated.id && it.unlocked }
    }.map { it.id }
    return progression.copy(
        achievements = updatedAchievements,
        latestUnlockedAchievementIds = newlyUnlocked
    )
}

internal fun calculateProgressionLevel(xp: Int): Int {
    return (xp / XP_PER_LEVEL + 1).coerceAtLeast(1)
}
