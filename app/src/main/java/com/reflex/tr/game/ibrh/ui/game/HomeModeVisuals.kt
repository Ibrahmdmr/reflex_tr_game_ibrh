package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.ui.graphics.Color
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette


internal fun modeAccentColor(mode: GameMode): Color {
    return when (mode) {
        GameMode.Classic -> ArcadeGold
        GameMode.MovingTarget -> ArcadeBlue
        GameMode.FakeTarget -> ReflexGamePalette.targetRing
        GameMode.ColorReflex -> ArcadeTeal
    }
}

internal fun modeIcon(mode: GameMode): String {
    return when (mode) {
        GameMode.Classic -> "◎"
        GameMode.MovingTarget -> "↗"
        GameMode.FakeTarget -> "◇"
        GameMode.ColorReflex -> "◆"
    }
}
