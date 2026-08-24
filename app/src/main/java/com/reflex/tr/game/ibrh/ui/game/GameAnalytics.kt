package com.reflex.tr.game.ibrh.ui.game

import android.os.Bundle
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices

/** Cannot throw. Callers pass only the event's own figures — never playerName or uid. */
internal fun logGameEvent(
    event: FirebaseEvent,
    params: Bundle.() -> Unit = {}
) {
    runCatching {
        FirebaseGameServices.logEvent(event = event, params = Bundle().apply(params))
    }
}
