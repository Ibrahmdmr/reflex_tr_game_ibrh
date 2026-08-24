package com.reflex.tr.game.ibrh.ads

import androidx.compose.runtime.Immutable
import com.reflex.tr.game.ibrh.ads.PremiumSource.None

/** [None] is the only value this build can produce. */
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

enum class PremiumFeature {
    NoInterstitials
}

/** No purchase flow fills this in yet. [expiresAtMillis] 0 means never expires. */
@Immutable
data class PremiumState(
    val isPremiumUser: Boolean = false,
    val isNoAdsUser: Boolean = false,
    val source: PremiumSource = PremiumSource.None,
    val expiresAtMillis: Long = 0L
) {
    fun isActive(nowMillis: Long = System.currentTimeMillis()): Boolean =
        isPremiumUser && (expiresAtMillis <= 0L || expiresAtMillis > nowMillis)

    fun grants(
        feature: PremiumFeature,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean = when (feature) {
        PremiumFeature.NoInterstitials -> isNoAdsUser || isActive(nowMillis)
    }
}
