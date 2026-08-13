package com.reflex.tr.game.ibrh.ui.game

import kotlin.math.abs
import kotlin.random.Random

internal class GameTargetEngine {
    private var nextTargetId = 0L

    fun generateTargets(
        mode: GameMode,
        score: Int,
        currentTargets: List<GameTarget> = emptyList(),
        activeColor: ReflexTargetColor = ReflexTargetColor.Red,
        progression: ProgressionState = ProgressionState()
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
                val fakeCount = GameDifficultyConfig.fakeTargetCount(
                    score = score,
                    mode = mode,
                    progression = progression
                )
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
                    .take(
                        GameDifficultyConfig.wrongColorCount(
                            score = score,
                            mode = mode,
                            progression = progression
                        )
                    )
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
        return colors.randomOrNull() ?: ReflexTargetColor.Red
    }

    fun calculateMovementIntervalMillis(
        score: Int,
        mode: GameMode = GameMode.MovingTarget,
        progression: ProgressionState = ProgressionState()
    ): Long {
        return GameDifficultyConfig.movementIntervalMillis(
            score = score,
            mode = mode,
            progression = progression
        )
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
    return when (GameDifficultyConfig.tierForScore(score)) {
        GameDifficultyTier.Easy -> 1
        GameDifficultyTier.Medium -> 2
        GameDifficultyTier.Hard -> 3
        GameDifficultyTier.Extreme -> 4
    }
}

internal fun calculateTargetSizeDp(
    score: Int,
    mode: GameMode,
    progression: ProgressionState
): Int {
    return GameDifficultyConfig.targetSizeDp(
        score = score,
        mode = mode,
        progression = progression
    )
}

internal fun calculateTargetVisibleDurationMillis(
    score: Int,
    mode: GameMode,
    progression: ProgressionState
): Long {
    return GameDifficultyConfig.visibleDurationMillis(
        score = score,
        mode = mode,
        progression = progression
    )
}

internal fun List<GameTarget>.firstCorrectPosition(): TargetPosition {
    return firstOrNull { it.role == GameTargetRole.Correct }?.position ?: TargetPosition()
}
