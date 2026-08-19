package com.reflex.tr.game.ibrh.ads

import androidx.compose.runtime.Immutable

/** Where an entitlement came from. [None] is the only value this build can produce. */
enum class PremiumSource(val storageKey: String) {
    None("none"),
    Purchase("purchase"),
    Promo("promo"),
    Debug("debug");

    companion object {
        fun fromStorageKey(key: String): PremiumSource? =
            entries.firstOrNull { it.storageKey == key }
    }
}

/** What an entitlement can unlock. Kept separate from the state so a future tier can add to it. */
enum class PremiumFeature {
    NoInterstitials
}

/**
 * The player's entitlement, read locally and defaulting to nothing.
 *
 * No purchase flow exists yet: this is the shape a Play Billing client would fill in later, and
 * until then every field stays at its default so the app behaves exactly as it does today.
 *
 * @property expiresAtMillis 0 means "never expires"; a non-zero value in the past means lapsed.
 */
@Immutable
data class PremiumState(
    val isPremiumUser: Boolean = false,
    val isNoAdsUser: Boolean = false,
    val source: PremiumSource = PremiumSource.None,
    val expiresAtMillis: Long = 0L
) {
    fun isActive(nowMillis: Long = System.currentTimeMillis()): Boolean =
        isPremiumUser && (expiresAtMillis <= 0L || expiresAtMillis > nowMillis)

    /** The only entitlement that changes behaviour today. */
    fun grants(
        feature: PremiumFeature,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean = when (feature) {
        PremiumFeature.NoInterstitials -> isNoAdsUser || isActive(nowMillis)
    }
}
