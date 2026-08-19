package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.ui.graphics.Color
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal

/**
 * Accent colours keyed off gameplay models.
 *
 * These tables used to be copied into the shop, collection, arena and feedback layers, so a skin
 * recolour had to be repeated in four places to stay consistent.
 */

/** Accent for a target skin. [classicFallback] lets the arena tint the default skin with its own base. */
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

/** The drawn colour of a colour-reflex target. */
internal fun ReflexTargetColor.toTaskColor(): Color = when (this) {
    ReflexTargetColor.Red -> Color(0xFFFF335F)
    ReflexTargetColor.Blue -> Color(0xFF39A8FF)
    ReflexTargetColor.Gold -> Color(0xFFFFD84D)
    ReflexTargetColor.Teal -> Color(0xFF22F2A6)
}

/** Accent for a reward-chest tier: the richer the chest, the warmer the glow. */
internal fun rewardChestAccent(type: RewardChestType): Color = when (type) {
    RewardChestType.Small -> ArcadeTeal
    RewardChestType.Neon -> Color(0xFF9F7BFF)
    RewardChestType.Legendary -> ArcadeGold
}

/** Accent for a title's rarity band. Muted on purpose: the list shows ten of these at once. */
internal fun playerTitleAccent(rarity: PlayerTitleRarity): Color = when (rarity) {
    PlayerTitleRarity.Common -> ArcadeBlue
    PlayerTitleRarity.Rare -> ArcadeTeal
    PlayerTitleRarity.Epic -> Color(0xFF9F7BFF)
    PlayerTitleRarity.Legendary -> ArcadeGold
}

/** Accent for a rewarded offer, keyed off what it pays rather than which ad unit serves it. */
internal fun rewardedOfferAccent(kind: RewardedOfferKind): Color = when (kind) {
    RewardedOfferKind.Coins -> ArcadeGold
    RewardedOfferKind.Continue -> Color(0xFF9F7BFF)
    RewardedOfferKind.Chest -> ArcadeTeal
    RewardedOfferKind.StreakProtect -> ArcadeBlue
}
