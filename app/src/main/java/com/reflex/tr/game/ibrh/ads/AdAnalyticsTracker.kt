package com.reflex.tr.game.ibrh.ads

import android.os.Bundle
import android.util.Log
import com.reflex.tr.game.ibrh.BuildConfig
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices

object AdAnalyticsTracker {
    private const val TAG = "AdAnalytics"

    /** Debug breadcrumb only. Use [report] for anything that has to survive into release. */
    fun track(eventName: String, params: Bundle = Bundle.EMPTY) {
        if (!BuildConfig.AD_LOGGING_ENABLED) return

        Log.d(TAG, "$eventName $params")
    }

    /**
     * Ad-funnel steps that must be visible in production.
     *
     * [track] writes to logcat and only when ad logging is on, so an ad opening or failing left no
     * trace at all in a release build — the two ends of the rewarded funnel were invisible.
     * [FirebaseGameServices.logEvent] already swallows its own failures, so this cannot throw.
     */
    fun report(event: FirebaseEvent, params: Bundle = Bundle.EMPTY) {
        track(event.eventName, params)
        FirebaseGameServices.logEvent(event = event, params = params)
    }
}

fun adParams(vararg values: Pair<String, Any>): Bundle {
    return Bundle().apply {
        values.forEach { (key, value) ->
            when (value) {
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Double -> putDouble(key, value)
                is Boolean -> putBoolean(key, value)
                else -> putString(key, value.toString())
            }
        }
    }
}
