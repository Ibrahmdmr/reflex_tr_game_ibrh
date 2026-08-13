package com.reflex.tr.game.ibrh.ui.game

internal enum class GameDifficultyTier {
    Easy,
    Medium,
    Hard,
    Extreme
}

internal data class ModeDifficultyConfig(
    val initialTimeSeconds: Int,
    val initialTargetSizeDp: Int,
    val minTargetSizeDp: Int,
    val shrinkStepScore: Int,
    val shrinkAmountDp: Int,
    val initialVisibleDurationMillis: Long,
    val minVisibleDurationMillis: Long,
    val visibleDurationStepScore: Int,
    val visibleDurationReductionMillis: Long,
    val initialMovementIntervalMillis: Long,
    val minMovementIntervalMillis: Long,
    val movementStepScore: Int,
    val movementReductionMillis: Long,
    val fakeTargetCountByTier: Map<GameDifficultyTier, Int>,
    val wrongColorCountByTier: Map<GameDifficultyTier, Int>,
    val colorRuleIntervalMillisByTier: Map<GameDifficultyTier, Long>
)

internal object GameDifficultyConfig {
    private const val FirstFiveVisibleBonusMillis = 360L
    private const val FirstFiveVisibleBonusDecayMillis = 35L
    private const val FirstFiveSizeBonusBaseDp = 10
    private const val FirstFiveSizeBonusMinDp = 6

    private val DefaultColorIntervals = mapOf(
        GameDifficultyTier.Easy to 6_000L,
        GameDifficultyTier.Medium to 5_000L,
        GameDifficultyTier.Hard to 4_500L,
        GameDifficultyTier.Extreme to 4_000L
    )

    private val NoFakeTargets = GameDifficultyTier.entries.associateWith { 0 }
    private val DefaultWrongColors = mapOf(
        GameDifficultyTier.Easy to 2,
        GameDifficultyTier.Medium to 2,
        GameDifficultyTier.Hard to 3,
        GameDifficultyTier.Extreme to 3
    )

    private val configs = mapOf(
        GameMode.Classic to ModeDifficultyConfig(
            initialTimeSeconds = 30,
            initialTargetSizeDp = 82,
            minTargetSizeDp = 50,
            shrinkStepScore = 3,
            shrinkAmountDp = 3,
            initialVisibleDurationMillis = 1_850L,
            minVisibleDurationMillis = 900L,
            visibleDurationStepScore = 2,
            visibleDurationReductionMillis = 72L,
            initialMovementIntervalMillis = 900L,
            minMovementIntervalMillis = 340L,
            movementStepScore = 2,
            movementReductionMillis = 60L,
            fakeTargetCountByTier = NoFakeTargets,
            wrongColorCountByTier = DefaultWrongColors,
            colorRuleIntervalMillisByTier = DefaultColorIntervals
        ),
        GameMode.MovingTarget to ModeDifficultyConfig(
            initialTimeSeconds = 30,
            initialTargetSizeDp = 80,
            minTargetSizeDp = 48,
            shrinkStepScore = 3,
            shrinkAmountDp = 3,
            initialVisibleDurationMillis = 1_780L,
            minVisibleDurationMillis = 860L,
            visibleDurationStepScore = 2,
            visibleDurationReductionMillis = 76L,
            initialMovementIntervalMillis = 920L,
            minMovementIntervalMillis = 330L,
            movementStepScore = 2,
            movementReductionMillis = 66L,
            fakeTargetCountByTier = NoFakeTargets,
            wrongColorCountByTier = DefaultWrongColors,
            colorRuleIntervalMillisByTier = DefaultColorIntervals
        ),
        GameMode.FakeTarget to ModeDifficultyConfig(
            initialTimeSeconds = 30,
            initialTargetSizeDp = 78,
            minTargetSizeDp = 48,
            shrinkStepScore = 3,
            shrinkAmountDp = 3,
            initialVisibleDurationMillis = 1_820L,
            minVisibleDurationMillis = 880L,
            visibleDurationStepScore = 2,
            visibleDurationReductionMillis = 74L,
            initialMovementIntervalMillis = 900L,
            minMovementIntervalMillis = 340L,
            movementStepScore = 2,
            movementReductionMillis = 60L,
            fakeTargetCountByTier = mapOf(
                GameDifficultyTier.Easy to 1,
                GameDifficultyTier.Medium to 1,
                GameDifficultyTier.Hard to 2,
                GameDifficultyTier.Extreme to 2
            ),
            wrongColorCountByTier = DefaultWrongColors,
            colorRuleIntervalMillisByTier = DefaultColorIntervals
        ),
        GameMode.ColorReflex to ModeDifficultyConfig(
            initialTimeSeconds = 30,
            initialTargetSizeDp = 80,
            minTargetSizeDp = 48,
            shrinkStepScore = 3,
            shrinkAmountDp = 3,
            initialVisibleDurationMillis = 1_800L,
            minVisibleDurationMillis = 870L,
            visibleDurationStepScore = 2,
            visibleDurationReductionMillis = 75L,
            initialMovementIntervalMillis = 900L,
            minMovementIntervalMillis = 340L,
            movementStepScore = 2,
            movementReductionMillis = 60L,
            fakeTargetCountByTier = NoFakeTargets,
            wrongColorCountByTier = DefaultWrongColors,
            colorRuleIntervalMillisByTier = DefaultColorIntervals
        )
    )

    fun tierForScore(score: Int): GameDifficultyTier {
        return when (score.coerceAtLeast(0)) {
            in 0..10 -> GameDifficultyTier.Easy
            in 11..25 -> GameDifficultyTier.Medium
            in 26..50 -> GameDifficultyTier.Hard
            else -> GameDifficultyTier.Extreme
        }
    }

    fun targetSizeDp(
        score: Int,
        mode: GameMode,
        progression: ProgressionState
    ): Int {
        val config = configFor(mode)
        val reduction = (score.coerceAtLeast(0) / config.shrinkStepScore.coerceAtLeast(1)) *
            config.shrinkAmountDp
        val baseSize = (config.initialTargetSizeDp - reduction).coerceAtLeast(config.minTargetSizeDp)
        if (!isFirstFiveGames(progression)) return baseSize

        val softBonus = (FirstFiveSizeBonusBaseDp - progression.totalGames.coerceAtLeast(0))
            .coerceAtLeast(FirstFiveSizeBonusMinDp)
        return (baseSize + softBonus).coerceAtMost(config.initialTargetSizeDp + FirstFiveSizeBonusBaseDp)
    }

    fun initialTimeSeconds(mode: GameMode): Int {
        return configFor(mode).initialTimeSeconds.coerceAtLeast(10)
    }

    fun visibleDurationMillis(
        score: Int,
        mode: GameMode,
        progression: ProgressionState
    ): Long {
        val config = configFor(mode)
        val reduction = (score.coerceAtLeast(0) / config.visibleDurationStepScore.coerceAtLeast(1)) *
            config.visibleDurationReductionMillis
        val baseDuration = (config.initialVisibleDurationMillis - reduction)
            .coerceAtLeast(config.minVisibleDurationMillis)
        if (!isFirstFiveGames(progression)) return baseDuration

        val softBonus = FirstFiveVisibleBonusMillis -
            (progression.totalGames.coerceAtLeast(0) * FirstFiveVisibleBonusDecayMillis)
        return (baseDuration + softBonus)
            .coerceAtMost(config.initialVisibleDurationMillis + FirstFiveVisibleBonusMillis)
    }

    fun movementIntervalMillis(
        score: Int,
        mode: GameMode,
        progression: ProgressionState
    ): Long {
        val config = configFor(mode)
        val reduction = (score.coerceAtLeast(0) / config.movementStepScore.coerceAtLeast(1)) *
            config.movementReductionMillis
        val baseInterval = (config.initialMovementIntervalMillis - reduction)
            .coerceAtLeast(config.minMovementIntervalMillis)
        return if (isFirstFiveGames(progression)) {
            (baseInterval + 120L).coerceAtMost(config.initialMovementIntervalMillis + 120L)
        } else {
            baseInterval
        }
    }

    fun fakeTargetCount(
        score: Int,
        mode: GameMode,
        progression: ProgressionState
    ): Int {
        val count = configFor(mode).fakeTargetCountByTier[tierForScore(score)] ?: 0
        return if (isFirstFiveGames(progression)) count.coerceAtMost(1) else count
    }

    fun wrongColorCount(
        score: Int,
        mode: GameMode,
        progression: ProgressionState
    ): Int {
        val count = configFor(mode).wrongColorCountByTier[tierForScore(score)] ?: 2
        return if (isFirstFiveGames(progression)) count.coerceAtMost(2) else count
    }

    fun colorRuleIntervalMillis(
        score: Int,
        mode: GameMode,
        progression: ProgressionState
    ): Long {
        val interval = configFor(mode).colorRuleIntervalMillisByTier[tierForScore(score)] ?: 5_000L
        return if (isFirstFiveGames(progression)) interval + 500L else interval
    }

    private fun configFor(mode: GameMode): ModeDifficultyConfig {
        return configs[mode] ?: configs.getValue(GameMode.Classic)
    }

    private fun isFirstFiveGames(progression: ProgressionState): Boolean {
        return progression.totalGames.coerceAtLeast(0) < FirstFiveExperienceGameLimit
    }
}
