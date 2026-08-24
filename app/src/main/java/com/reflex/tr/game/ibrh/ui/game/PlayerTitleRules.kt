package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseParam

data class PlayerTitleUnlockResult(
    val profile: PlayerProfile,
    val newlyUnlocked: List<PlayerTitle>
)

/** Both the unlock check and the "x / y" hint read this, so they cannot drift apart. */
internal fun playerTitleProgressValue(
    title: PlayerTitle,
    progression: ProgressionState
): Int = when (title) {
    PlayerTitle.NewReflex -> progression.totalGames
    PlayerTitle.ComboHunter -> progression.lifetimeMaxCombo
    // Every mode runs 30 seconds, so the all-time best score is the 30-second best score.
    PlayerTitle.SpeedMaster -> progression.personalRecords.bestScore
    PlayerTitle.SharpTapper -> progression.personalRecords.bestAccuracyPercent
    PlayerTitle.Collector ->
        progression.unlockedThemes.size + progression.unlockedTargetSkins.size
    PlayerTitle.BossHunter -> progression.totalBossRoundHits
    PlayerTitle.UltraPlayer -> progression.totalUltraMomentHits
    // The badge is the sticky record of having reached Neon; the live tier covers the run that
    // gets there before the badge is written.
    PlayerTitle.NeonWarrior -> if (
        progression.neonLeagueBadgeUnlocked || progression.weeklyLeague.tier == LeagueTier.Neon
    ) {
        title.requirementValue
    } else {
        0
    }
    // The 30-day loyalty badge outranks the current streak, which resets.
    PlayerTitle.LoyalPlayer -> if (progression.dailyReward.loyalBadgeUnlocked) {
        title.requirementValue
    } else {
        progression.dailyReward.streakDay
    }
    PlayerTitle.ReflexLegend -> progression.totalGames
}.coerceAtLeast(0)

internal fun meetsPlayerTitleRequirement(
    title: PlayerTitle,
    progression: ProgressionState
): Boolean = playerTitleProgressValue(title, progression) >= title.requirementValue.coerceAtLeast(1)

/** Titles are sticky: a broken daily streak cannot take "Loyal Player" back. */
internal fun refreshedPlayerTitles(
    profile: PlayerProfile,
    progression: ProgressionState
): PlayerTitleUnlockResult {
    val newlyUnlocked = PlayerTitle.entries.filter {
        it !in profile.unlockedTitles && meetsPlayerTitleRequirement(it, progression)
    }
    val owned = profile.unlockedTitles + newlyUnlocked
    // Also repairs a stored title the player does not own.
    val active = profile.title?.takeIf { it in owned } ?: bestPlayerTitle(owned)
    if (newlyUnlocked.isEmpty() && active == profile.title) {
        return PlayerTitleUnlockResult(profile, emptyList())
    }
    return PlayerTitleUnlockResult(
        profile = profile.copy(title = active, unlockedTitles = owned),
        newlyUnlocked = newlyUnlocked
    )
}

internal fun bestPlayerTitle(titles: Set<PlayerTitle>): PlayerTitle? =
    titles.maxWithOrNull(compareBy<PlayerTitle>({ it.rarity.ordinal }, { it.ordinal }))

/** Carries only the title's own identity — never playerName or uid. */
internal fun logPlayerTitleEvent(
    event: FirebaseEvent,
    title: PlayerTitle? = null
) {
    logGameEvent(event) {
        title?.let {
            putString(FirebaseParam.TitleId.key, it.storageKey)
            putString(FirebaseParam.Rarity.key, it.rarity.name)
            putString(FirebaseParam.Category.key, it.category.name)
        }
    }
}
