package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.R

@Immutable
data class TargetPosition(
    val xFraction: Float = 0.5f,
    val yFraction: Float = 0.5f
)

enum class ReflexTargetColor(@StringRes val labelRes: Int) {
    Red(R.string.target_color_red),
    Blue(R.string.target_color_blue),
    Gold(R.string.target_color_yellow),
    Teal(R.string.target_color_green)
}

enum class GameTargetRole {
    Correct,
    Fake,
    WrongColor
}

@Immutable
data class GameTarget(
    val id: Long,
    val position: TargetPosition,
    val role: GameTargetRole = GameTargetRole.Correct,
    val color: ReflexTargetColor = ReflexTargetColor.Red
)

enum class TimingGrade {
    Perfect,
    Great,
    Normal
}
