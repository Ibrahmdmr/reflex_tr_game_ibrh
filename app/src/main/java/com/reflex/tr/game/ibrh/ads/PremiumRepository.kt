package com.reflex.tr.game.ibrh.ads

/** The seam a Play Billing client plugs into. Nothing writes an entitlement yet. */
interface PremiumRepository {
    fun premiumState(): PremiumState

    fun savePremiumState(state: PremiumState)
}
