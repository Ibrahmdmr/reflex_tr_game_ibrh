package com.reflex.tr.game.ibrh.ui.game

import android.os.Bundle
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices

/**
 * The one way game rules report to analytics.
 *
 * Every reporting site used to repeat the same `runCatching { logEvent(Bundle().apply { … }) }`
 * wrapper, which made "reporting must never break gameplay" a promise each caller had to remember.
 * It is enforced here instead.
 *
 * Callers pass only the figures the event is about: never a player name, never a uid.
 */
internal fun logGameEvent(
    event: FirebaseEvent,
    params: Bundle.() -> Unit = {}
) {
    runCatching {
        FirebaseGameServices.logEvent(event = event, params = Bundle().apply(params))
    }
}
