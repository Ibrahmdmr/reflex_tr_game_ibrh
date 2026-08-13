package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import com.reflex.tr.game.ibrh.R

enum class ThemeRarity(@StringRes val titleRes: Int) {
    Common(R.string.theme_rarity_common),
    Rare(R.string.theme_rarity_rare),
    Epic(R.string.theme_rarity_epic),
    Legendary(R.string.theme_rarity_legendary),
    Mythic(R.string.theme_rarity_mythic)
}

enum class PlayerTheme(
    val storageKey: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val coinPrice: Int,
    val rarity: ThemeRarity
) {
    NeonRed(
        storageKey = "neon_red",
        titleRes = R.string.theme_neon_red,
        descriptionRes = R.string.theme_neon_red_description,
        coinPrice = 0,
        rarity = ThemeRarity.Common
    ),
    CyberBlue(
        storageKey = "cyber_blue",
        titleRes = R.string.theme_cyber_blue,
        descriptionRes = R.string.theme_cyber_blue_description,
        coinPrice = 600,
        rarity = ThemeRarity.Rare
    ),
    PurpleStorm(
        storageKey = "purple_storm",
        titleRes = R.string.theme_purple_storm,
        descriptionRes = R.string.theme_purple_storm_description,
        coinPrice = 1500,
        rarity = ThemeRarity.Rare
    ),
    IceNeon(
        storageKey = "ice_neon",
        titleRes = R.string.theme_ice_neon,
        descriptionRes = R.string.theme_ice_neon_description,
        coinPrice = 3500,
        rarity = ThemeRarity.Epic
    ),
    LavaCore(
        storageKey = "lava_core",
        titleRes = R.string.theme_lava_core,
        descriptionRes = R.string.theme_lava_core_description,
        coinPrice = 6500,
        rarity = ThemeRarity.Epic
    ),
    ToxicPulse(
        storageKey = "toxic_pulse",
        titleRes = R.string.theme_toxic_pulse,
        descriptionRes = R.string.theme_toxic_pulse_description,
        coinPrice = 10000,
        rarity = ThemeRarity.Epic
    ),
    MatrixGreen(
        storageKey = "matrix_green",
        titleRes = R.string.theme_matrix_green,
        descriptionRes = R.string.theme_matrix_green_description,
        coinPrice = 90000,
        rarity = ThemeRarity.Mythic
    ),
    GoldFire(
        storageKey = "gold_fire",
        titleRes = R.string.theme_gold_fire,
        descriptionRes = R.string.theme_gold_fire_description,
        coinPrice = 18000,
        rarity = ThemeRarity.Mythic
    ),
    ShadowBlack(
        storageKey = "shadow_black",
        titleRes = R.string.theme_shadow_black,
        descriptionRes = R.string.theme_shadow_black_description,
        coinPrice = 26000,
        rarity = ThemeRarity.Legendary
    ),
    GalaxyWave(
        storageKey = "galaxy_wave",
        titleRes = R.string.theme_galaxy_wave,
        descriptionRes = R.string.theme_galaxy_wave_description,
        coinPrice = 42000,
        rarity = ThemeRarity.Mythic
    ),
    RainbowFlux(
        storageKey = "rainbow_flux",
        titleRes = R.string.theme_rainbow_flux,
        descriptionRes = R.string.theme_rainbow_flux_description,
        coinPrice = 60000,
        rarity = ThemeRarity.Legendary
    )
}

enum class TargetSkin(
    val storageKey: String,
    @StringRes val titleRes: Int,
    val coinPrice: Int
) {
    ClassicTarget(
        storageKey = "classic_target",
        titleRes = R.string.target_skin_classic,
        coinPrice = 0
    ),
    NeonRing(
        storageKey = "neon_ring",
        titleRes = R.string.target_skin_neon_ring,
        coinPrice = 750
    ),
    CyberDot(
        storageKey = "cyber_dot",
        titleRes = R.string.target_skin_cyber_dot,
        coinPrice = 1500
    ),
    FireCore(
        storageKey = "fire_core",
        titleRes = R.string.target_skin_fire_core,
        coinPrice = 3000
    ),
    MatrixOrb(
        storageKey = "matrix_orb",
        titleRes = R.string.target_skin_matrix_orb,
        coinPrice = 7500
    )
}
