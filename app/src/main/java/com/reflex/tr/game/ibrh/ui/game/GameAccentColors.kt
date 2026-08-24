package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.ui.graphics.Color
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal

/** [classicFallback] lets the arena tint the default skin with its own base. */
internal fun targetSkinAccent(
    skin: TargetSkin,
    classicFallback: Color = ArcadeGold
): Color = when (skin) {
    TargetSkin.ClassicTarget -> classicFallback
    TargetSkin.NeonRing -> Color(0xFF41F2FF)
    TargetSkin.CyberDot -> Color(0xFF9F7BFF)
    TargetSkin.FireCore -> Color(0xFFFF6B3D)
    TargetSkin.MatrixOrb -> Color(0xFF49FF91)
}

internal fun ReflexTargetColor.toTaskColor(): Color = when (this) {
    ReflexTargetColor.Red -> Color(0xFFFF335F)
    ReflexTargetColor.Blue -> Color(0xFF39A8FF)
    ReflexTargetColor.Gold -> Color(0xFFFFD84D)
    ReflexTargetColor.Teal -> Color(0xFF22F2A6)
}

internal fun rewardChestAccent(type: RewardChestType): Color = when (type) {
    RewardChestType.Small -> ArcadeTeal
    RewardChestType.Neon -> Color(0xFF9F7BFF)
    RewardChestType.Legendary -> ArcadeGold
}

internal fun playerTitleAccent(rarity: PlayerTitleRarity): Color = when (rarity) {
    PlayerTitleRarity.Common -> ArcadeBlue
    PlayerTitleRarity.Rare -> ArcadeTeal
    PlayerTitleRarity.Epic -> Color(0xFF9F7BFF)
    PlayerTitleRarity.Legendary -> ArcadeGold
}

internal fun rewardedOfferAccent(kind: RewardedOfferKind): Color = when (kind) {
    RewardedOfferKind.Coins -> ArcadeGold
    RewardedOfferKind.Continue -> Color(0xFF9F7BFF)
    RewardedOfferKind.Chest -> ArcadeTeal
    RewardedOfferKind.StreakProtect -> ArcadeBlue
}
