package com.reflex.tr.game.ibrh.ui.game

internal data class ModeMasteryAdvanceResult(
    val xpByMode: Map<GameMode, Int>,
    val coinBonus: Int,
    val levelUp: ModeMasteryLevelUp?
)

internal fun advanceSeasonQuestsAfterGame(
    season: SeasonState,
    score: Int,
    maxCombo: Int,
    theme: PlayerTheme,
    targetSkin: TargetSkin
): SeasonState {
    val currentSeason = seasonForToday(season)
    val usedCosmetics = currentSeason.usedCosmeticKeys +
        "theme:${theme.storageKey}" +
        "skin:${targetSkin.storageKey}"
    val updatedQuests = currentSeason.quests.map { quest ->
        val nextProgress = when (quest.type) {
            SeasonQuestType.Play100Games -> quest.progress + 1
            SeasonQuestType.Score3000 -> quest.progress + score.coerceAtLeast(0)
            SeasonQuestType.Combo10TwentyFiveTimes -> quest.progress + if (maxCombo >= 10) 1 else 0
            SeasonQuestType.Complete10DailyMissions -> quest.progress
            SeasonQuestType.Use5Cosmetics -> usedCosmetics.size
        }.coerceIn(0, quest.target)
        val shouldClaimReward = nextProgress >= quest.target && !quest.claimed
        quest.copy(
            progress = nextProgress,
            claimed = quest.claimed || shouldClaimReward,
            rewardClaimedThisGame = shouldClaimReward
        )
    }
    return currentSeason.copy(
        quests = updatedQuests,
        usedCosmeticKeys = usedCosmetics
    )
}

internal fun advanceSeasonQuestForDailyMissionClaim(progression: ProgressionState): ProgressionState {
    val season = seasonForToday(progression.season)
    val updatedQuests = season.quests.map { quest ->
        if (quest.type != SeasonQuestType.Complete10DailyMissions) {
            return@map quest.copy(rewardClaimedThisGame = false)
        }
        val nextProgress = (quest.progress + 1).coerceIn(0, quest.target)
        val shouldClaimReward = nextProgress >= quest.target && !quest.claimed
        quest.copy(
            progress = nextProgress,
            claimed = quest.claimed || shouldClaimReward,
            rewardClaimedThisGame = shouldClaimReward
        )
    }
    val updatedSeason = season.copy(quests = updatedQuests)
    val seasonHunterUnlocked = progression.seasonHunterBadgeUnlocked ||
        updatedSeason.seasonQuestsCompleted
    return addCoins(
        progression.copy(
            season = updatedSeason,
            seasonHunterBadgeUnlocked = seasonHunterUnlocked
        ),
        updatedSeason.seasonQuestRewardCoinsThisGame
    )
}

internal fun advanceModeMastery(
    progression: ProgressionState,
    mode: GameMode,
    score: Int,
    maxCombo: Int
): ModeMasteryAdvanceResult {
    val currentXp = progression.modeMasteryXpByMode[mode]?.coerceAtLeast(0) ?: 0
    val currentProgress = ModeMasteryProgress(currentXp)
    val earnedXp = calculateModeMasteryXp(score = score, maxCombo = maxCombo)
    val maxXp = (MODE_MASTERY_MAX_LEVEL - 1) * MODE_MASTERY_XP_PER_LEVEL
    val nextXp = (currentXp.toLong() + earnedXp.toLong())
        .coerceIn(0L, maxXp.toLong())
        .toInt()
    val nextProgress = ModeMasteryProgress(nextXp)
    val crossedLevels = ((currentProgress.level + 1)..nextProgress.level).toList()
    val coinBonus = crossedLevels.sumOf { level -> modeMasteryLevelReward(level) }
    return ModeMasteryAdvanceResult(
        xpByMode = progression.modeMasteryXpByMode + (mode to nextXp),
        coinBonus = coinBonus,
        levelUp = crossedLevels.lastOrNull()?.let { level ->
            ModeMasteryLevelUp(
                mode = mode,
                level = level,
                coinBonus = coinBonus
            )
        }
    )
}

internal fun modeMasteryLevelReward(level: Int): Int {
    return when (level) {
        10 -> 750
        5 -> 300
        else -> 100
    }
}

internal fun calculateModeMasteryXp(score: Int, maxCombo: Int): Int {
    return 20 + score.coerceIn(0, 30) + maxCombo.coerceIn(0, 10)
}
