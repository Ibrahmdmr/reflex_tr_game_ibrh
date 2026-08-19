package com.reflex.tr.game.ibrh.ui.game

import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseParam

/** Every rule the player-title system needs. UI reads state; only these functions decide it. */

/** What one title check produced: the profile to persist, and what there is to announce. */
data class PlayerTitleUnlockResult(
    val profile: PlayerProfile,
    val newlyUnlocked: List<PlayerTitle>
)

/**
 * How far [progression] has come toward [title], on the same scale as
 * [PlayerTitle.requirementValue] — the one place a condition is written down.
 *
 * Every figure comes from [ProgressionState], which defaults each of them to zero or empty, so a
 * player with no league, collection or streak history reads as 0 rather than failing. Both the
 * unlock check and the "x / y" hint read this, so they cannot drift apart.
 */
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

/** Whether [progression] satisfies [title] right now. */
internal fun meetsPlayerTitleRequirement(
    title: PlayerTitle,
    progression: ProgressionState
): Boolean = playerTitleProgressValue(title, progression) >= title.requirementValue.coerceAtLeast(1)

/**
 * Re-checks every title against [progression] and folds the result into [profile].
 *
 * Titles are sticky: once earned they stay owned, which is what stops a broken daily streak from
 * taking "Loyal Player" back. Returns [profile] unchanged when nothing moved, so callers can skip
 * the write — this runs after every progression save.
 */
internal fun refreshedPlayerTitles(
    profile: PlayerProfile,
    progression: ProgressionState
): PlayerTitleUnlockResult {
    val newlyUnlocked = PlayerTitle.entries.filter {
        it !in profile.unlockedTitles && meetsPlayerTitleRequirement(it, progression)
    }
    val owned = profile.unlockedTitles + newlyUnlocked
    // Also repairs a stored title the player does not own, and hands a first-time player their
    // starter title without a trip to the profile screen.
    val active = profile.title?.takeIf { it in owned } ?: bestPlayerTitle(owned)
    if (newlyUnlocked.isEmpty() && active == profile.title) {
        return PlayerTitleUnlockResult(profile, emptyList())
    }
    return PlayerTitleUnlockResult(
        profile = profile.copy(title = active, unlockedTitles = owned),
        newlyUnlocked = newlyUnlocked
    )
}

/** The title worth wearing out of [titles]: rarest first, then the hardest to earn. */
internal fun bestPlayerTitle(titles: Set<PlayerTitle>): PlayerTitle? =
    titles.maxWithOrNull(compareBy<PlayerTitle>({ it.rarity.ordinal }, { it.ordinal }))

/**
 * Player-title analytics. Carries only the title's own identity — never the player name or uid.
 */
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
