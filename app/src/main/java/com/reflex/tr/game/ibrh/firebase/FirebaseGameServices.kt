package com.reflex.tr.game.ibrh.firebase

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.reflex.tr.game.ibrh.BuildConfig

object FirebaseGameServices {
    private const val TAG = "FirebaseGameServices"

    private var initialized = false
    private var appContext: Context? = null

    fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        appContext = context.applicationContext

        runCatching {
            FirebaseApp.initializeApp(context.applicationContext)
            log("Firebase app initialized")
            if (BuildConfig.DEBUG) {
                recordNonFatal("Firebase debug startup check")
            }
            signInAnonymously()
            logEvent(FirebaseEvent.AppOpen)
        }.onFailure { error ->
            logError("Firebase initialization failed", error)
        }
    }

    fun logEvent(event: FirebaseEvent, params: Bundle = Bundle.EMPTY) {
        runCatching {
            val context = appContext ?: return@runCatching
            FirebaseAnalytics.getInstance(context).logEvent(event.eventName, params)
            log("Analytics event sent: ${event.eventName} $params")
        }.onFailure { error ->
            logError("Analytics event failed: ${event.eventName}", error)
        }
    }

    fun recordNonFatal(message: String, error: Throwable = IllegalStateException(message)) {
        runCatching {
            FirebaseCrashlytics.getInstance().recordException(error)
            log("Crashlytics non-fatal recorded: $message")
        }.onFailure { crashlyticsError ->
            logError("Crashlytics non-fatal failed", crashlyticsError)
        }
    }

    private fun signInAnonymously() {
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener { result ->
                val userId = result.user?.uid.orEmpty()
                log("Anonymous Auth success userId=$userId")
                if (userId.isBlank()) {
                    logError("Anonymous Auth returned blank userId", null)
                    return@addOnSuccessListener
                }
            }
            .addOnFailureListener { error ->
                logError("Anonymous Auth failed", error)
                recordNonFatal("Anonymous Auth failed", error)
            }
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }

    private fun logError(message: String, error: Throwable?) {
        if (error == null) {
            Log.e(TAG, message)
        } else {
            Log.e(TAG, message, error)
        }
    }
}

enum class FirebaseEvent(val eventName: String) {
    AppOpen("app_open"),
    GameStart("game_start"),
    GameOver("game_over"),
    RewardedAdWatched("rewarded_ad_watched"),
    ModeSelected("mode_selected"),
    ThemeSelected("theme_selected"),
    ThemePurchased("theme_purchased"),
    DailyRewardClaimed("daily_reward_claimed"),
    StreakProtected("streak_protected"),
    ChallengeCompleted("challenge_completed"),
    ChallengeRewardDoubled("challenge_reward_doubled"),
    LeaderboardOpened("leaderboard_opened"),
    LeaderboardRefreshed("leaderboard_refreshed"),
    ProfileOpened("profile_opened"),
    ShopOpened("shop_opened"),
    LeaderboardScoreUpload("leaderboard_score_upload"),
    LeaderboardUploadFailed("leaderboard_upload_failed"),
    PlayerNameChanged("player_name_changed"),
    AchievementUnlocked("achievement_unlocked"),
    AchievementClaimed("achievement_claimed"),
    LevelUp("level_up"),
    RankChanged("rank_changed"),
    NotificationPermissionShown("notification_permission_shown"),
    NotificationPermissionGranted("notification_permission_granted"),
    NotificationPermissionDenied("notification_permission_denied"),
    NotificationToggleEnabled("notification_toggle_enabled"),
    NotificationToggleDisabled("notification_toggle_disabled"),
    NotificationScheduled("notification_scheduled"),
    NotificationClicked("notification_clicked"),
    NotificationCancelled("notification_cancelled")
}

enum class FirebaseParam(val key: String) {
    ModeName("mode_name"),
    ThemeName("theme_name"),
    CoinAmount("coin_amount"),
    ChallengeName("challenge_name"),
    StreakDay("streak_day"),
    Score("score"),
    MaxCombo("max_combo"),
    NewBest("new_best"),
    NameLength("name_length"),
    Period("period"),
    Placement("placement"),
    AchievementId("achievement_id"),
    Level("level"),
    RankName("rank_name"),
    NotificationType("notification_type"),
    PermissionStatus("permission_status"),
    SourceScreen("source_screen")
}
