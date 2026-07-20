package com.reflex.tr.game.ibrh.ui.game

import kotlin.math.abs
import kotlin.random.Random

private object GameTargetTuning {
    const val InitialTargetSizeDp = 82
    const val MinTargetSizeDp = 48
    const val InitialVisibleDurationMillis = 1_800L
    const val MinVisibleDurationMillis = 850L
}

internal class GameTargetEngine {
    private var nextTargetId = 0L

    fun generateTargets(
        mode: GameMode,
        score: Int,
        currentTargets: List<GameTarget> = emptyList(),
        activeColor: ReflexTargetColor = ReflexTargetColor.Red
    ): List<GameTarget> {
        val currentCorrect = currentTargets.firstOrNull { it.role == GameTargetRole.Correct }?.position
        val usedPositions = mutableListOf<TargetPosition>()
        val correctPosition = generateRandomTargetPosition(
            currentX = currentCorrect?.xFraction,
            currentY = currentCorrect?.yFraction
        )
        usedPositions += correctPosition
        return when (mode) {
            GameMode.Classic,
            GameMode.MovingTarget -> listOf(
                GameTarget(
                    id = nextTargetId++,
                    position = correctPosition,
                    role = GameTargetRole.Correct,
                    color = ReflexTargetColor.Red
                )
            )
            GameMode.FakeTarget -> {
                val fakeCount = if (score >= 8) 2 else 1
                buildList {
                    add(
                        GameTarget(
                            id = nextTargetId++,
                            position = correctPosition,
                            role = GameTargetRole.Correct,
                            color = ReflexTargetColor.Red
                        )
                    )
                    repeat(fakeCount) {
                        add(
                            GameTarget(
                                id = nextTargetId++,
                                position = generateRandomTargetPositionAwayFrom(usedPositions),
                                role = GameTargetRole.Fake,
                                color = ReflexTargetColor.Red
                            ).also { usedPositions += it.position }
                        )
                    }
                }
            }
            GameMode.ColorReflex -> {
                val wrongColors = ReflexTargetColor.entries
                    .filterNot { it == activeColor }
                    .sortedBy { it.ordinal }
                    .take(if (score >= 12) 3 else 2)
                buildList {
                    wrongColors.forEach { wrongColor ->
                        add(
                            GameTarget(
                                id = nextTargetId++,
                                position = generateRandomTargetPositionAwayFrom(usedPositions),
                                role = GameTargetRole.WrongColor,
                                color = wrongColor
                            ).also { usedPositions += it.position }
                        )
                    }
                    add(
                        GameTarget(
                            id = nextTargetId++,
                            position = correctPosition,
                            role = GameTargetRole.Correct,
                            color = activeColor
                        )
                    )
                }
            }
        }
    }

    fun nextColorRule(currentColor: ReflexTargetColor): ReflexTargetColor {
        val colorOrder = listOf(
            ReflexTargetColor.Red,
            ReflexTargetColor.Blue,
            ReflexTargetColor.Teal,
            ReflexTargetColor.Gold
        )
        val currentIndex = colorOrder.indexOf(currentColor).coerceAtLeast(0)
        return colorOrder[(currentIndex + 1) % colorOrder.size]
    }

    fun randomTargetColor(except: ReflexTargetColor? = null): ReflexTargetColor {
        val colors = ReflexTargetColor.entries.filterNot { it == except }
        return colors.random()
    }

    fun calculateMovementIntervalMillis(score: Int): Long {
        return (900L - (score / 2) * 70L).coerceAtLeast(320L)
    }

    private fun generateRandomTargetPositionAwayFrom(
        existingPositions: List<TargetPosition>
    ): TargetPosition {
        repeat(20) {
            val candidate = generateRandomTargetPosition()
            val isFarEnough = existingPositions.all { existing ->
                abs(candidate.xFraction - existing.xFraction) > 0.16f ||
                    abs(candidate.yFraction - existing.yFraction) > 0.16f
            }
            if (isFarEnough) return candidate
        }
        return generateRandomTargetPosition()
    }

    fun generateRandomTargetPosition(
        currentX: Float? = null,
        currentY: Float? = null
    ): TargetPosition {
        repeat(20) {
            val newPosition = TargetPosition(
                xFraction = Random.nextFloat().coerceIn(0.15f, 0.85f),
                yFraction = Random.nextFloat().coerceIn(0.2f, 0.8f)
            )

            val isFarEnough =
                currentX == null || currentY == null ||
                    abs(newPosition.xFraction - currentX) > 0.12f ||
                    abs(newPosition.yFraction - currentY) > 0.12f

            if (isFarEnough) {
                return newPosition
            }
        }

        return TargetPosition(
            xFraction = Random.nextFloat().coerceIn(0.15f, 0.85f),
            yFraction = Random.nextFloat().coerceIn(0.2f, 0.8f)
        )
    }
}

internal fun calculateDifficultyLevel(score: Int): Int {
    return (score / 5 + 1).coerceIn(1, 8)
}

internal fun calculateTargetSizeDp(
    score: Int,
    mode: GameMode,
    progression: ProgressionState
): Int {
    val modeExtraReduction = when (mode) {
        GameMode.Classic -> 0
        GameMode.MovingTarget -> 2
        GameMode.FakeTarget -> 4
        GameMode.ColorReflex -> 2
    }
    val sizeReduction = (score / 3) * 4 + modeExtraReduction
    val baseSize = (GameTargetTuning.InitialTargetSizeDp - sizeReduction)
        .coerceAtLeast(GameTargetTuning.MinTargetSizeDp)
    if (progression.totalGames >= FirstFiveExperienceGameLimit) return baseSize

    val softBonus = (10 - progression.totalGames).coerceAtLeast(6)
    return (baseSize + softBonus).coerceAtMost(GameTargetTuning.InitialTargetSizeDp + 10)
}

internal fun calculateTargetVisibleDurationMillis(
    score: Int,
    mode: GameMode,
    progression: ProgressionState
): Long {
    val modeExtraReduction = when (mode) {
        GameMode.Classic -> 0L
        GameMode.MovingTarget -> 80L
        GameMode.FakeTarget -> 40L
        GameMode.ColorReflex -> 60L
    }
    val durationReduction = (score / 2) * 80L + modeExtraReduction
    val baseDuration = (GameTargetTuning.InitialVisibleDurationMillis - durationReduction)
        .coerceAtLeast(GameTargetTuning.MinVisibleDurationMillis)
    if (progression.totalGames >= FirstFiveExperienceGameLimit) return baseDuration

    val softBonusMillis = 360L - (progression.totalGames * 35L)
    return (baseDuration + softBonusMillis)
        .coerceAtMost(GameTargetTuning.InitialVisibleDurationMillis + 360L)
}

internal fun List<GameTarget>.firstCorrectPosition(): TargetPosition {
    return firstOrNull { it.role == GameTargetRole.Correct }?.position ?: TargetPosition()
}
