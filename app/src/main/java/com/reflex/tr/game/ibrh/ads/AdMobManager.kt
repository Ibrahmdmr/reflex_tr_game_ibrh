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

data class RewardedAdUiState(
    val isReady: Boolean = false,
    val isLoading: Boolean = false,
    val isShowing: Boolean = false,
    val hasLoadFailed: Boolean = false
)

class AdMobManager(
    private val activity: Activity
) {
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
            Log.d(TAG, "Ad not ready")
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
        Log.d(TAG, "Rewarded ad is showing")
        var rewardEarned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                updateRewardedUiState {
                    it.copy(
                        isShowing = false
                    )
                }
                if (rewardEarned) {
                    Log.d(TAG, "Rewarded ad dismissed after reward, granting continue")
                    onRewardEarned()
                }
                loadRewardedAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
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
            Log.d(TAG, "User earned rewarded ad reward")
        }
    }

    private var gameCount = 0

    fun showInterstitialAd() {
        gameCount++

        // 3-5 oyun arası random
        val shouldShow = (3..5).random()

        if (gameCount % shouldShow != 0) return

        val ad = interstitialAd ?: return

        interstitialAd = null

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadInterstitialAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                loadInterstitialAd()
            }
        }

        ad.show(activity)
    }

    private fun loadRewardedAd() {
        if (isRewardedLoading || rewardedAd != null) return

        mainHandler.removeCallbacks(rewardedRetryRunnable)
        Log.d(TAG, "Rewarded ad is loading")
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
            REWARDED_TEST_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded")
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
                    Log.w(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
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
            INTERSTITIAL_TEST_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isInterstitialLoading = false
                }
            }
        )
    }

    companion object {
        private const val TAG = "AdMobManager"
        private const val REWARDED_RETRY_DELAY_MS = 3_000L

        private const val REWARDED_TEST_AD_UNIT_ID = "ca-app-pub-2483444595618509/5335804928"
        private const val INTERSTITIAL_TEST_AD_UNIT_ID = "ca-app-pub-2483444595618509/3693129772"

    }
}
