package com.reflex.tr.game.ibrh

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.reflex.tr.game.ibrh.ads.AdMobManager
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.GameScreen
import com.reflex.tr.game.ibrh.ui.theme.Reflex_tr_game_ibrhTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SplashDurationMillis = 3_000L
private const val MainActivityLogTag = "MainActivityAds"

class MainActivity : ComponentActivity() {
    private lateinit var adMobManager: AdMobManager
    private var rewardedAdUiState by mutableStateOf(RewardedAdUiState())

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashStartTime = SystemClock.uptimeMillis()
        installSplashScreen().setKeepOnScreenCondition {
            SystemClock.uptimeMillis() - splashStartTime < SplashDurationMillis
        }

        super.onCreate(savedInstanceState)
        Log.d(MainActivityLogTag, "MainActivity created")
        adMobManager = AdMobManager(this)
        adMobManager.onRewardedUiStateChanged = { uiState ->
            Log.d(
                MainActivityLogTag,
                "Rewarded state -> ready=${uiState.isReady}, loading=${uiState.isLoading}, showing=${uiState.isShowing}, failed=${uiState.hasLoadFailed}"
            )
            rewardedAdUiState = uiState
        }
        initializeMobileAds()

        enableEdgeToEdge()
        setContent {
            Reflex_tr_game_ibrhTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(
                        rewardedAdUiState = rewardedAdUiState,
                        onRewardedContinueRequested = { onRewardEarned ->
                            Log.d(MainActivityLogTag, "Continue button pressed")
                            adMobManager.showRewardedAd(
                                onRewardEarned = {
                                    Log.d(MainActivityLogTag, "Reward earned callback triggered")
                                    onRewardEarned()
                                }
                            )
                        },
                        onInterstitialAdRequested = {
                            adMobManager.showInterstitialAd()
                        }
                    )
                }
            }
        }
    }

    private fun initializeMobileAds() {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(MainActivityLogTag, "MobileAds initialization started")
            MobileAds.initialize(this@MainActivity) {}
            withContext(Dispatchers.Main) {
                Log.d(MainActivityLogTag, "MobileAds initialized, preloading ads")
                adMobManager.preloadAds()
            }
        }
    }
}

@Composable
fun AppRoot(
    modifier: Modifier = Modifier,
    rewardedAdUiState: RewardedAdUiState = RewardedAdUiState(),
    onRewardedContinueRequested: (onRewardEarned: () -> Unit) -> Unit = { onRewardEarned ->
        onRewardEarned()
    },
    onInterstitialAdRequested: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        GameScreen(
            rewardedAdUiState = rewardedAdUiState,
            onRewardedContinueRequested = onRewardedContinueRequested,
            onInterstitialAdRequested = onInterstitialAdRequested
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppRootPreview() {
    Reflex_tr_game_ibrhTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            GameScreen(
                uiState = com.reflex.tr.game.ibrh.ui.game.GameUiState(),
                rewardedAdUiState = RewardedAdUiState(isReady = true),
                onStartClick = {},
                onHomeClick = {},
                onTargetTap = {},
                onMissTap = {},
                onContinueClick = {},
                onRetryClick = {}
            )
        }
    }
}
