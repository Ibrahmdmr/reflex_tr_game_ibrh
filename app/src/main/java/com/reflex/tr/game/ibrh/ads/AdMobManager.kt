package com.reflex.tr.game.ibrh.ads

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.reflex.tr.game.ibrh.BuildConfig
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
import com.reflex.tr.game.ibrh.firebase.FirebaseParam

sealed class AdPresentationState {
    data object Idle : AdPresentationState()
    data object Loading : AdPresentationState()
    data object Ready : AdPresentationState()
    data object Showing : AdPresentationState()
    data object RewardEarned : AdPresentationState()
    data class Failed(val message: String? = null) : AdPresentationState()
}

data class RewardedAdUiState(
    val status: AdPresentationState = AdPresentationState.Idle,
    val isReady: Boolean = false,
    val isLoading: Boolean = false,
    val isShowing: Boolean = false,
    val hasLoadFailed: Boolean = false,
    val rewardEarned: Boolean = false
)

class AdMobManager(
    private val activity: Activity
) {
    private val rewardedAdUnitId = BuildConfig.REWARDED_AD_UNIT_ID
    private val interstitialAdUnitId = BuildConfig.INTERSTITIAL_AD_UNIT_ID
    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isRewardedLoading = false
    private var isRewardedShowing = false
    private var isInterstitialLoading = false
    private var isInterstitialShowing = false
    private var lastRewardedClosedElapsedMillis = 0L
    private val mainHandler = Handler(Looper.getMainLooper())
    private val rewardedRetryRunnable = Runnable { loadRewardedAd() }
    private val interstitialRetryRunnable = Runnable { loadInterstitialAd() }
    private var rewardedUiState = RewardedAdUiState()
    val isRewardedReady: Boolean
        get() = rewardedAd != null
    var onRewardedUiStateChanged: ((RewardedAdUiState) -> Unit)? = null

    private fun updateRewardedUiState(
        transform: (RewardedAdUiState) -> RewardedAdUiState
    ) {
        rewardedUiState = transform(rewardedUiState)
        if (activity.isFinishing || activity.isDestroyed) return
        activity.runOnUiThread {
            onRewardedUiStateChanged?.invoke(rewardedUiState)
        }
    }

    fun preloadAds() {
        loadRewardedAd()
        loadInterstitialAd()
    }

    fun showRewardedAd(
        placement: String,
        onRewardEarned: () -> Unit
    ): Boolean {
        if (activity.isFinishing || activity.isDestroyed) {
            logWarn("Rewarded ad ignored because activity is not active")
            updateRewardedUiState {
                it.copy(
                    status = AdPresentationState.Failed("activity_not_active"),
                    isReady = rewardedAd != null,
                    isLoading = false,
                    isShowing = false,
                    hasLoadFailed = true,
                    rewardEarned = false
                )
            }
            return false
        }
        if (isRewardedShowing || isInterstitialShowing) {
            logDebug("Another fullscreen ad is already showing")
            updateRewardedUiState {
                it.copy(
                    status = AdPresentationState.Failed("ad_already_showing"),
                    isReady = rewardedAd != null,
                    isLoading = false,
                    isShowing = isRewardedShowing,
                    hasLoadFailed = true,
                    rewardEarned = false
                )
            }
            return false
        }
        val ad = rewardedAd
        if (ad == null) {
            logDebug("Ad not ready")
            AdAnalyticsTracker.track(
                eventName = "rewarded_failed",
                params = adParams("placement" to placement, "reason" to "not_ready")
            )
            updateRewardedUiState {
                it.copy(
                    status = AdPresentationState.Failed("not_ready"),
                    isReady = false,
                    isLoading = false,
                    isShowing = false,
                    hasLoadFailed = true,
                    rewardEarned = false
                )
            }
            loadRewardedAd()
            return false
        }

        rewardedAd = null
        isRewardedShowing = true
        mainHandler.removeCallbacks(rewardedRetryRunnable)
        updateRewardedUiState {
            it.copy(
                status = AdPresentationState.Showing,
                isReady = false,
                isLoading = false,
                isShowing = true,
                hasLoadFailed = false,
                rewardEarned = false
            )
        }
        AdAnalyticsTracker.track("rewarded_open", adParams("placement" to placement))
        logDebug("Rewarded ad is showing")
        var rewardEarned = false
        var rewardGranted = false
        var fullscreenFinished = false

        fun finishRewardedAd(
            status: AdPresentationState,
            hasLoadFailed: Boolean,
            shouldGrantReward: Boolean
        ) {
            if (fullscreenFinished) return
            fullscreenFinished = true
            isRewardedShowing = false
            lastRewardedClosedElapsedMillis = android.os.SystemClock.elapsedRealtime()
            updateRewardedUiState {
                it.copy(
                    status = status,
                    isShowing = false,
                    isLoading = false,
                    hasLoadFailed = hasLoadFailed,
                    rewardEarned = rewardEarned
                )
            }
            if (shouldGrantReward && rewardEarned && !rewardGranted) {
                rewardGranted = true
                logDebug("Rewarded ad dismissed after reward, granting reward")
                onRewardEarned()
            }
            loadRewardedAd()
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                finishRewardedAd(
                    status = if (rewardEarned) AdPresentationState.RewardEarned else AdPresentationState.Idle,
                    hasLoadFailed = false,
                    shouldGrantReward = true
                )
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                logWarn("Rewarded ad failed to show: ${adError.message}")
                AdAnalyticsTracker.track(
                    eventName = "rewarded_failed",
                    params = adParams("placement" to placement, "reason" to adError.message)
                )
                rewardEarned = false
                finishRewardedAd(
                    status = AdPresentationState.Failed(adError.message),
                    hasLoadFailed = true,
                    shouldGrantReward = false
                )
            }
        }

        return runCatching {
            ad.show(activity) {
                if (!rewardEarned) {
                    rewardEarned = true
                    updateRewardedUiState {
                        it.copy(
                            status = AdPresentationState.RewardEarned,
                            rewardEarned = true
                        )
                    }
                    AdAnalyticsTracker.track("rewarded_complete", adParams("placement" to placement))
                    FirebaseGameServices.logEvent(
                        event = FirebaseEvent.RewardedAdWatched,
                        params = adParams(FirebaseParam.Placement.key to placement)
                    )
                    logDebug("User earned rewarded ad reward")
                }
            }
            true
        }.onFailure { throwable ->
            logWarn("Rewarded ad show threw: ${throwable.message}")
            AdAnalyticsTracker.track(
                eventName = "rewarded_failed",
                params = adParams("placement" to placement, "reason" to (throwable.message ?: "show_exception"))
            )
            rewardEarned = false
            finishRewardedAd(
                status = AdPresentationState.Failed(throwable.message),
                hasLoadFailed = true,
                shouldGrantReward = false
            )
        }.getOrDefault(false)
    }

    fun showInterstitialAd(): Boolean {
        if (activity.isFinishing || activity.isDestroyed) return false
        if (isInterstitialShowing || isRewardedShowing) return false
        val now = android.os.SystemClock.elapsedRealtime()
        if (
            lastRewardedClosedElapsedMillis > 0L &&
            now - lastRewardedClosedElapsedMillis < REWARDED_TO_INTERSTITIAL_GRACE_MS
        ) return false
        val ad = interstitialAd ?: run {
            loadInterstitialAd()
            return false
        }

        interstitialAd = null
        isInterstitialShowing = true

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isInterstitialShowing = false
                loadInterstitialAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                logWarn("Interstitial ad failed to show: ${adError.message}")
                isInterstitialShowing = false
                loadInterstitialAd()
            }
        }

        logDebug("Interstitial ad is showing")
        AdAnalyticsTracker.track("interstitial_show")
        AdAnalyticsTracker.track("ad_revenue_estimate", adParams("format" to "interstitial"))
        return runCatching {
            ad.show(activity)
            true
        }.getOrElse { throwable ->
            logWarn("Interstitial ad show threw: ${throwable.message}")
            isInterstitialShowing = false
            loadInterstitialAd()
            false
        }
    }

    private fun loadRewardedAd() {
        if (activity.isFinishing || activity.isDestroyed) {
            isRewardedLoading = false
            updateRewardedUiState {
                it.copy(
                    status = AdPresentationState.Idle,
                    isLoading = false,
                    isShowing = false
                )
            }
            return
        }
        if (isRewardedLoading || rewardedAd != null || isRewardedShowing) return

        mainHandler.removeCallbacks(rewardedRetryRunnable)
        logDebug("Rewarded ad is loading")
        isRewardedLoading = true
        updateRewardedUiState {
            it.copy(
                status = AdPresentationState.Loading,
                isReady = false,
                isLoading = true,
                isShowing = false,
                rewardEarned = false
            )
        }
        RewardedAd.load(
            activity,
            rewardedAdUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    logDebug("Rewarded ad loaded")
                    rewardedAd = ad
                    isRewardedLoading = false
                    updateRewardedUiState {
                        it.copy(
                            status = AdPresentationState.Ready,
                            isReady = true,
                            isLoading = false,
                            isShowing = false,
                            hasLoadFailed = false,
                            rewardEarned = false
                        )
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    logWarn("Rewarded ad failed to load: ${loadAdError.message}")
                    rewardedAd = null
                    isRewardedLoading = false
                    updateRewardedUiState {
                        it.copy(
                            status = AdPresentationState.Failed(loadAdError.message),
                            isReady = false,
                            isLoading = false,
                            isShowing = false,
                            hasLoadFailed = true
                        )
                    }
                    mainHandler.postDelayed(rewardedRetryRunnable, REWARDED_RETRY_DELAY_MS)
                }
            }
        )
    }

    private fun loadInterstitialAd() {
        if (activity.isFinishing || activity.isDestroyed) {
            isInterstitialLoading = false
            return
        }
        if (isInterstitialLoading || interstitialAd != null || isInterstitialShowing) return

        mainHandler.removeCallbacks(interstitialRetryRunnable)
        isInterstitialLoading = true
        InterstitialAd.load(
            activity,
            interstitialAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    logDebug("Interstitial ad loaded")
                    interstitialAd = ad
                    isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    logWarn("Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isInterstitialLoading = false
                    mainHandler.postDelayed(interstitialRetryRunnable, INTERSTITIAL_RETRY_DELAY_MS)
                }
            }
        )
    }

    private fun logDebug(message: String) {
        if (BuildConfig.AD_LOGGING_ENABLED) {
            Log.d(TAG, message)
        }
    }

    private fun logWarn(message: String) {
        if (BuildConfig.AD_LOGGING_ENABLED) {
            Log.w(TAG, message)
        }
    }

    companion object {
        private const val TAG = "AdMobManager"
        private const val REWARDED_RETRY_DELAY_MS = 3_000L
        private const val INTERSTITIAL_RETRY_DELAY_MS = 8_000L
        private const val REWARDED_TO_INTERSTITIAL_GRACE_MS = 3_000L
    }
}
