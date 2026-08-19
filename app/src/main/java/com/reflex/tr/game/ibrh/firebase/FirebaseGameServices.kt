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
        runCatching {
            FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener { result ->
                    // The uid itself is never logged: it identifies the player, and a debug build
                    // still writes to a log anyone with the device can read.
                    if (result.user?.uid.isNullOrBlank()) {
                        logError("Anonymous Auth returned blank userId", null)
                        return@addOnSuccessListener
                    }
                    log("Anonymous Auth success")
                }
                .addOnFailureListener { error ->
                    logError("Anonymous Auth failed", error)
                    recordNonFatal("Anonymous Auth failed", error)
                }
        }.onFailure { error ->
            logError("Anonymous Auth start failed", error)
        }
    }

    private fun log(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
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
    RewardedAdOpened("rewarded_open"),
    RewardedAdFailed("rewarded_failed"),
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
    PlayerTitleUnlocked("player_title_unlocked"),
    PlayerTitleSelected("player_title_selected"),
    PlayerTitlesOpened("player_titles_opened"),
    StarterJourneyViewed("starter_journey_viewed"),
    StarterTaskProgressed("starter_task_progressed"),
    StarterTaskCompleted("starter_task_completed"),
    StarterRewardClaimed("starter_reward_claimed"),
    StarterJourneyCompleted("starter_journey_completed"),
    RewardedOfferViewed("rewarded_offer_viewed"),
    RewardedOfferClicked("rewarded_offer_clicked"),
    RewardedOfferAdLoaded("rewarded_offer_ad_loaded"),
    RewardedOfferAdFailed("rewarded_offer_ad_failed"),
    RewardedOfferCompleted("rewarded_offer_completed"),
    RewardedOfferRewardGranted("rewarded_offer_reward_granted"),
    InterstitialEligible("interstitial_eligible"),
    InterstitialSkippedByPacing("interstitial_skipped_by_pacing"),
    InterstitialShown("interstitial_shown"),
    InterstitialFailed("interstitial_failed"),
    PremiumCardViewed("premium_card_viewed"),
    PremiumComingSoonClicked("premium_coming_soon_clicked"),
    NoAdsStateChecked("no_ads_state_checked"),
    BonusesOpened("bonuses_opened"),
    DailyBonusClaimed("daily_bonus_claimed"),
    BonusLimitReached("bonus_limit_reached"),
    AchievementUnlocked("achievement_unlocked"),
    AchievementClaimed("achievement_claimed"),
    ScoreShared("score_shared"),
    ScoreShareClicked("score_share_clicked"),
    ScoreShareGenerated("score_share_generated"),
    ScoreShareFailed("score_share_failed"),
    ScoreShareSheetOpened("score_share_sheet_opened"),
    DailyEventViewed("daily_event_viewed"),
    DailyEventProgress("daily_event_progress"),
    DailyEventCompleted("daily_event_completed"),
    DailyEventRewardClaimed("daily_event_reward_claimed"),
    WeeklyLeagueViewed("weekly_league_viewed"),
    WeeklyLeaguePointsEarned("weekly_league_points_earned"),
    WeeklyLeagueUpgraded("weekly_league_upgraded"),
    WeeklyLeagueRewardClaimed("weekly_league_reward_claimed"),
    RewardChestEarned("reward_chest_earned"),
    RewardChestOpened("reward_chest_opened"),
    RewardChestRewardGranted("reward_chest_reward_granted"),
    QuestHubOpened("quest_hub_opened"),
    QuestSectionViewed("quest_section_viewed"),
    QuestRewardClicked("quest_reward_clicked"),
    QuestRecommendationClicked("quest_recommendation_clicked"),
    ClaimableRewardsViewed("claimable_rewards_viewed"),
    InviteShareClicked("invite_share_clicked"),
    InviteRewardClaimed("invite_reward_claimed"),
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
    Mode("mode"),
    ThemeName("theme_name"),
    CoinAmount("coin_amount"),
    ChallengeName("challenge_name"),
    StreakDay("streak_day"),
    Score("score"),
    MaxCombo("max_combo"),
    BestScore("best_score"),
    Accuracy("accuracy"),
    EarnedCoin("earned_coin"),
    EventType("event_type"),
    Progress("progress"),
    Target("target"),
    RewardCoin("reward_coin"),
    RewardXp("reward_xp"),
    ChestType("chest_type"),
    TitleId("title_id"),
    Rarity("rarity"),
    Category("category"),
    Day("day"),
    TaskId("task_id"),
    OfferType("offer_type"),
    RewardType("reward_type"),
    Reason("reason"),
    IsPremium("is_premium"),
    Source("source"),
    League("league"),
    PointsEarned("points_earned"),
    TotalPoints("total_points"),
    Section("section"),
    RewardCount("reward_count"),
    TotalRewardCoin("total_reward_coin"),
    RecommendationType("recommendation_type"),
    NewBest("new_best"),
    IsNewRecord("is_new_record"),
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
