package com.reflex.tr.game.ibrh

import android.content.Context
import android.content.res.Configuration
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.reflex.tr.game.ibrh.ads.AdMobManager
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.RewardedAction
import com.reflex.tr.game.ibrh.ui.game.AppLanguage
import com.reflex.tr.game.ibrh.ui.game.GamePreferences
import com.reflex.tr.game.ibrh.ui.game.GameScreen
import com.reflex.tr.game.ibrh.ui.game.HowToPlayScreen
import com.reflex.tr.game.ibrh.ui.theme.Reflex_tr_game_ibrhTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private const val SplashDurationMillis = 3_000L
private const val MainActivityLogTag = "MainActivityAds"
private const val GameRoute = "game"
private const val HowToPlayRoute = "how_to_play"

class MainActivity : ComponentActivity() {
    private lateinit var adMobManager: AdMobManager
    private var rewardedAdUiState by mutableStateOf(RewardedAdUiState())

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashStartTime = SystemClock.uptimeMillis()
        installSplashScreen().setKeepOnScreenCondition {
            SystemClock.uptimeMillis() - splashStartTime < SplashDurationMillis
        }

        super.onCreate(savedInstanceState)
        logDebug("MainActivity created")
        adMobManager = AdMobManager(this)
        adMobManager.onRewardedUiStateChanged = { uiState ->
            logDebug(
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
                        onRewardedAdRequested = { action, onRewardEarned ->
                            logDebug("Rewarded button pressed: $action")
                            adMobManager.showRewardedAd(
                                placement = action.analyticsName,
                                onRewardEarned = {
                                    logDebug("Reward earned callback triggered")
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
            logDebug("MobileAds initialization started")
            MobileAds.initialize(this@MainActivity) {}
            withContext(Dispatchers.Main) {
                logDebug("MobileAds initialized, preloading ads")
                adMobManager.preloadAds()
            }
        }
    }

    private fun logDebug(message: String) {
        if (BuildConfig.AD_LOGGING_ENABLED) {
            Log.d(MainActivityLogTag, message)
        }
    }
}

@Composable
fun AppRoot(
    modifier: Modifier = Modifier,
    rewardedAdUiState: RewardedAdUiState = RewardedAdUiState(),
    onRewardedAdRequested: (RewardedAction, onRewardEarned: () -> Unit) -> Unit = { _, onRewardEarned ->
        onRewardEarned()
    },
    onInterstitialAdRequested: () -> Boolean = { false }
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val gamePreferences = remember(context) { GamePreferences(context.applicationContext) }
    val selectedLanguage by gamePreferences.languageFlow.collectAsStateWithLifecycle(
        initialValue = AppLanguage.Turkish
    )
    val isSoundEnabled by gamePreferences.soundEnabledFlow.collectAsStateWithLifecycle(
        initialValue = true
    )
    val coroutineScope = rememberCoroutineScope()
    val localizedContext = remember(context, selectedLanguage) {
        context.createLocalizedContext(selectedLanguage)
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            NavHost(
                navController = navController,
                startDestination = GameRoute
            ) {
                composable(GameRoute) {
                    GameScreen(
                        rewardedAdUiState = rewardedAdUiState,
                        selectedLanguage = selectedLanguage,
                        isSoundEnabled = isSoundEnabled,
                        onLanguageSelected = { language ->
                            coroutineScope.launch {
                                gamePreferences.saveLanguage(language)
                            }
                        },
                        onSoundEnabledChange = { enabled ->
                            coroutineScope.launch {
                                gamePreferences.saveSoundEnabled(enabled)
                            }
                        },
                        onRewardedAdRequested = onRewardedAdRequested,
                        onInterstitialAdRequested = onInterstitialAdRequested,
                        onHowToPlayClick = {
                            navController.navigate(HowToPlayRoute)
                        }
                    )
                }
                composable(HowToPlayRoute) {
                    HowToPlayScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

private val RewardedAction.analyticsName: String
    get() = when (this) {
        RewardedAction.Continue -> "continue"
        RewardedAction.DoubleCoins -> "double_coin"
        RewardedAction.UnlockTheme -> "theme_unlock"
        RewardedAction.ProtectStreak -> "protect_streak"
    }

private fun Context.createLocalizedContext(language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.code)
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
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
                selectedLanguage = AppLanguage.Turkish,
                isSoundEnabled = true,
                onStartClick = {},
                onModeStartClick = {},
                onHowToPlayClick = {},
                onLanguageSelected = {},
                onSoundEnabledChange = {},
                onDailyRewardClaim = {},
                onDailyStreakProtect = {},
                onAchievementClaim = {},
                onThemeSelect = {},
                onThemeBuy = {},
                onThemeTrial = {},
                onPlayerNameChange = { true },
                onPlayerTitleSelect = {},
                onLeaderboardModeSelected = {},
                onLeaderboardPeriodSelected = {},
                onLeaderboardRefresh = {},
                onHomeClick = {},
                onPauseGame = {},
                onResumeGame = {},
                onTargetTap = {},
                onMissTap = {},
                onContinueClick = {},
                onDoubleCoinsClick = {},
                onRetryClick = {}
            )
        }
    }
}
