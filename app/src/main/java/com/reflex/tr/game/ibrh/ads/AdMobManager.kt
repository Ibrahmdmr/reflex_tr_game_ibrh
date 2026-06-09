package com.reflex.tr.game.ibrh.ads

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.reflex.tr.game.ibrh.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices

data class RewardedAdUiState(
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
    private var isInterstitialLoading = false
    private var isInterstitialShowing = false
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
        val ad = rewardedAd
        if (ad == null) {
            logDebug("Ad not ready")
            AdAnalyticsTracker.track(
                eventName = "rewarded_failed",
                params = adParams("placement" to placement, "reason" to "not_ready")
            )
            loadRewardedAd()
            return false
        }

        rewardedAd = null
        mainHandler.removeCallbacks(rewardedRetryRunnable)
        updateRewardedUiState {
            it.copy(
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
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                updateRewardedUiState {
                    it.copy(
                        isShowing = false,
                        rewardEarned = rewardEarned
                    )
                }
                if (rewardEarned) {
                    logDebug("Rewarded ad dismissed after reward, granting continue")
                    onRewardEarned()
                }
                loadRewardedAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                logWarn("Rewarded ad failed to show: ${adError.message}")
                AdAnalyticsTracker.track(
                    eventName = "rewarded_failed",
                    params = adParams("placement" to placement, "reason" to adError.message)
                )
                updateRewardedUiState {
                    it.copy(
                        isShowing = false,
                        hasLoadFailed = true,
                        rewardEarned = false
                    )
                }
                loadRewardedAd()
            }
        }

        ad.show(activity) {
            rewardEarned = true
            AdAnalyticsTracker.track("rewarded_complete", adParams("placement" to placement))
            FirebaseGameServices.logEvent(
                event = FirebaseEvent.RewardedAdWatched,
                params = adParams("placement" to placement)
            )
            logDebug("User earned rewarded ad reward")
        }
        return true
    }

    fun showInterstitialAd(): Boolean {
        if (isInterstitialShowing) return false
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
        ad.show(activity)
        return true
    }

    private fun loadRewardedAd() {
        if (isRewardedLoading || rewardedAd != null) return

        mainHandler.removeCallbacks(rewardedRetryRunnable)
        logDebug("Rewarded ad is loading")
        isRewardedLoading = true
        updateRewardedUiState {
            it.copy(
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
        if (isInterstitialLoading || interstitialAd != null) return

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
    }
}
