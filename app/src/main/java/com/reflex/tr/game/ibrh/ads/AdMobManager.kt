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

data class RewardedAdUiState(
    val isReady: Boolean = false,
    val isLoading: Boolean = false,
    val isShowing: Boolean = false,
    val hasLoadFailed: Boolean = false
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
    private val mainHandler = Handler(Looper.getMainLooper())
    private val rewardedRetryRunnable = Runnable { loadRewardedAd() }
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
        onRewardEarned: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            logDebug("Ad not ready")
            loadRewardedAd()
            return
        }

        rewardedAd = null
        mainHandler.removeCallbacks(rewardedRetryRunnable)
        updateRewardedUiState {
            it.copy(
                isReady = false,
                isLoading = false,
                isShowing = true,
                hasLoadFailed = false
            )
        }
        logDebug("Rewarded ad is showing")
        var rewardEarned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                updateRewardedUiState {
                    it.copy(
                        isShowing = false
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
                updateRewardedUiState {
                    it.copy(
                        isShowing = false,
                        hasLoadFailed = true
                    )
                }
                loadRewardedAd()
            }
        }

        ad.show(activity) {
            rewardEarned = true
            logDebug("User earned rewarded ad reward")
        }
    }

    private var gameCount = 0
    private val interstitialInterval = 4

    fun showInterstitialAd() {
        gameCount++
        if (gameCount % interstitialInterval != 0) return

        val ad = interstitialAd ?: return

        interstitialAd = null

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadInterstitialAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                logWarn("Interstitial ad failed to show: ${adError.message}")
                loadInterstitialAd()
            }
        }

        logDebug("Interstitial ad is showing")
        ad.show(activity)
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
                isShowing = false
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
                            hasLoadFailed = false
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
    }
}
