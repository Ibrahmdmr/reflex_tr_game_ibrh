package com.reflex.tr.game.ibrh.ads

/**
 * The seam a Play Billing client plugs into.
 *
 * Nothing in the app writes an entitlement today; the local implementation reads defaults and the
 * UI shows "coming soon" rather than a button that cannot work. Swapping in a billing-backed
 * implementation later means replacing this one object and nothing else.
 */
interface PremiumRepository {
    fun premiumState(): PremiumState

    fun savePremiumState(state: PremiumState)
}
