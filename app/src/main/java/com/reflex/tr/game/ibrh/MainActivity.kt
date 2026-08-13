package com.reflex.tr.game.ibrh

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.review.ReviewManagerFactory
import com.reflex.tr.game.ibrh.ads.AdMobManager
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
import com.reflex.tr.game.ibrh.firebase.FirebaseParam
import com.reflex.tr.game.ibrh.notifications.LocalNotificationScheduler
import com.reflex.tr.game.ibrh.notifications.LocalNotificationType
import com.reflex.tr.game.ibrh.notifications.logNotificationEvent
import com.reflex.tr.game.ibrh.ui.game.AppLanguage
import com.reflex.tr.game.ibrh.ui.game.GameDialogScrimColor
import com.reflex.tr.game.ibrh.ui.game.GamePreferences
import com.reflex.tr.game.ibrh.ui.game.GameScreen
import com.reflex.tr.game.ibrh.ui.game.HowToPlayScreen
import com.reflex.tr.game.ibrh.ui.game.PolishedGameDialog
import com.reflex.tr.game.ibrh.ui.game.RewardedAction
import com.reflex.tr.game.ibrh.ui.theme.Reflex_tr_game_ibrhTheme
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SplashDurationMillis = 3_000L
private const val MainActivityLogTag = "MainActivityAds"
private const val GameRoute = "game"
private const val HowToPlayRoute = "how_to_play"

private enum class NotificationToggle {
    DailyReward,
    Streak,
    Mission
}

private enum class AppPopup {
    Onboarding,
    NotificationPermission
}

private data class OnboardingPage(
    val icon: String,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int
)

private val NotificationToggle.notificationType: LocalNotificationType
    get() = when (this) {
        NotificationToggle.DailyReward -> LocalNotificationType.DailyReward
        NotificationToggle.Streak -> LocalNotificationType.StreakRisk
        NotificationToggle.Mission -> LocalNotificationType.Mission
    }

class MainActivity : ComponentActivity() {
    private lateinit var adMobManager: AdMobManager
    private var rewardedAdUiState by mutableStateOf(RewardedAdUiState())
    private var activeRewardedAction: RewardedAction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashStartTime = SystemClock.uptimeMillis()
        installSplashScreen().setKeepOnScreenCondition {
            SystemClock.uptimeMillis() - splashStartTime < SplashDurationMillis
        }

        super.onCreate(savedInstanceState)
        logDebug("MainActivity created")
        FirebaseGameServices.initialize(this)
        logNotificationClickIfPresent(intent)
        adMobManager = AdMobManager(this)
        adMobManager.onRewardedUiStateChanged = { uiState ->
            logDebug(
                "Rewarded state -> ready=${uiState.isReady}, loading=${uiState.isLoading}, showing=${uiState.isShowing}, failed=${uiState.hasLoadFailed}"
            )
            if (activeRewardedAction != null && !uiState.isShowing && !uiState.rewardEarned) {
                activeRewardedAction = null
            }
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
                            if (activeRewardedAction != null || rewardedAdUiState.isShowing) {
                                logDebug("Rewarded request ignored while another ad is active")
                                return@AppRoot
                            }
                            var rewardHandled = false
                            activeRewardedAction = action
                            val showStarted = adMobManager.showRewardedAd(
                                placement = action.analyticsName,
                                onRewardEarned = {
                                    if (rewardHandled) {
                                        logDebug("Duplicate rewarded callback ignored: $action")
                                        return@showRewardedAd
                                    }
                                    rewardHandled = true
                                    logDebug("Reward earned callback triggered")
                                    try {
                                        onRewardEarned()
                                    } finally {
                                        activeRewardedAction = null
                                    }
                                }
                            )
                            if (!showStarted) {
                                activeRewardedAction = null
                            }
                        },
                        onInterstitialAdRequested = {
                            adMobManager.showInterstitialAd()
                        },
                        onInAppReviewRequested = { allowStoreFallback ->
                            requestInAppReview(allowStoreFallback = allowStoreFallback)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        logNotificationClickIfPresent(intent)
    }

    override fun onDestroy() {
        // AdMobManager posts delayed retry runnables and holds this activity through its ad
        // callbacks; without this the activity stays reachable for seconds after finishing.
        adMobManager.release()
        super.onDestroy()
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

    private fun logNotificationClickIfPresent(intent: Intent?) {
        val notificationType = intent?.getStringExtra(EXTRA_NOTIFICATION_TYPE) ?: return
        FirebaseGameServices.logEvent(
            event = FirebaseEvent.NotificationClicked,
            params = Bundle().apply {
                putString(FirebaseParam.NotificationType.key, notificationType)
                putString(FirebaseParam.SourceScreen.key, "notification")
            }
        )
        intent.removeExtra(EXTRA_NOTIFICATION_TYPE)
    }

    private fun requestInAppReview(allowStoreFallback: Boolean) {
        val reviewManager = ReviewManagerFactory.create(this)
        reviewManager.requestReviewFlow()
            .addOnCompleteListener { requestTask ->
                if (requestTask.isSuccessful) {
                    reviewManager.launchReviewFlow(this, requestTask.result)
                        .addOnCompleteListener {
                            lifecycleScope.launch {
                                GamePreferences(applicationContext).markInAppReviewRequested(autoCompleted = true)
                            }
                        }
                } else {
                    lifecycleScope.launch {
                        GamePreferences(applicationContext).markInAppReviewRequested(autoCompleted = false)
                    }
                    if (allowStoreFallback) {
                        openPlayStorePage()
                    }
                }
            }
    }

    private fun openPlayStorePage() {
        val packageName = BuildConfig.APPLICATION_ID
        val marketIntent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()).apply {
            setPackage("com.android.vending")
        }
        runCatching {
            startActivity(marketIntent)
        }.onFailure {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri()
            )
            runCatching { startActivity(webIntent) }
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
    }
}

@Composable
fun AppRoot(
    modifier: Modifier = Modifier,
    rewardedAdUiState: RewardedAdUiState = RewardedAdUiState(),
    onRewardedAdRequested: (RewardedAction, onRewardEarned: () -> Unit) -> Unit = { _, onRewardEarned ->
        onRewardEarned()
    },
    onInterstitialAdRequested: () -> Boolean = { false },
    onInAppReviewRequested: (allowStoreFallback: Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as? Activity
    val gamePreferences = remember(context) { GamePreferences(context.applicationContext) }
    val selectedLanguage by gamePreferences.languageFlow.collectAsStateWithLifecycle(
        initialValue = AppLanguage.Turkish
    )
    val isSoundEnabled by gamePreferences.soundEnabledFlow.collectAsStateWithLifecycle(
        initialValue = true
    )
    val isEffectSoundEnabled by gamePreferences.effectSoundEnabledFlow.collectAsStateWithLifecycle(
        initialValue = true
    )
    val isVibrationEnabled by gamePreferences.vibrationEnabledFlow.collectAsStateWithLifecycle(
        initialValue = true
    )
    val isDailyRewardNotificationEnabled by gamePreferences.dailyRewardNotificationFlow.collectAsStateWithLifecycle(
        initialValue = false
    )
    val isStreakNotificationEnabled by gamePreferences.streakNotificationFlow.collectAsStateWithLifecycle(
        initialValue = false
    )
    val isNewMissionNotificationEnabled by gamePreferences.newMissionNotificationFlow.collectAsStateWithLifecycle(
        initialValue = false
    )
    val isOnboardingCompleted by gamePreferences.onboardingCompletedFlow.collectAsStateWithLifecycle(
        initialValue = true
    )
    val coroutineScope = rememberCoroutineScope()
    val localizedContext = remember(context, selectedLanguage) {
        context.createLocalizedContext(selectedLanguage)
    }
    var pendingNotificationToggle by remember { mutableStateOf<NotificationToggle?>(null) }
    var permissionRequestToggle by remember { mutableStateOf<NotificationToggle?>(null) }
    var notificationPermissionGranted by remember { mutableStateOf(false) }
    var showOnboarding by remember(isOnboardingCompleted) { mutableStateOf(!isOnboardingCompleted) }
    val activeAppPopup = when {
        showOnboarding -> AppPopup.Onboarding
        pendingNotificationToggle != null -> AppPopup.NotificationPermission
        else -> null
    }
    fun saveNotificationPreference(toggle: NotificationToggle, enabled: Boolean) {
        coroutineScope.launch {
            when (toggle) {
                NotificationToggle.DailyReward -> gamePreferences.saveDailyRewardNotificationEnabled(enabled)
                NotificationToggle.Streak -> gamePreferences.saveStreakNotificationEnabled(enabled)
                NotificationToggle.Mission -> gamePreferences.saveNewMissionNotificationEnabled(enabled)
            }
            logNotificationEvent(
                event = if (enabled) {
                    FirebaseEvent.NotificationToggleEnabled
                } else {
                    FirebaseEvent.NotificationToggleDisabled
                },
                type = toggle.notificationType,
                permissionStatus = if (LocalNotificationScheduler.hasNotificationPermission(context)) "granted" else "denied"
            )
            if (!enabled) {
                logNotificationEvent(
                    event = FirebaseEvent.NotificationCancelled,
                    type = toggle.notificationType,
                    permissionStatus = if (LocalNotificationScheduler.hasNotificationPermission(context)) "granted" else "denied"
                )
            }
            LocalNotificationScheduler.sync(context.applicationContext, force = true)
        }
    }
    fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }
    fun maybeRequestInAppReview(
        totalGames: Int,
        isNewBestScore: Boolean,
        score: Int,
        maxCombo: Int
    ) {
        if (
            gamePreferences.shouldRequestInAppReviewAfterGame(
                totalGames = totalGames,
                isNewBestScore = isNewBestScore,
                score = score,
                maxCombo = maxCombo
            )
        ) {
            onInAppReviewRequested(false)
        }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val toggle = permissionRequestToggle
        permissionRequestToggle = null
        notificationPermissionGranted = granted
        if (toggle != null) {
            logNotificationEvent(
                event = if (granted) {
                    FirebaseEvent.NotificationPermissionGranted
                } else {
                    FirebaseEvent.NotificationPermissionDenied
                },
                type = toggle.notificationType,
                permissionStatus = if (granted) "granted" else "denied"
            )
            saveNotificationPreference(toggle, granted)
        }
    }

    // Re-read on every resume: the permission can be flipped from system settings while the app
    // is in the background, and a one-shot LaunchedEffect(Unit) would keep showing a stale value.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificationPermissionGranted = hasNotificationPermission()
    }

    LaunchedEffect(
        isDailyRewardNotificationEnabled,
        isStreakNotificationEnabled,
        isNewMissionNotificationEnabled
    ) {
        LocalNotificationScheduler.sync(context.applicationContext)
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        if (activeAppPopup == AppPopup.Onboarding) {
            FirstLaunchOnboardingDialog(
                onFinish = {
                    showOnboarding = false
                    coroutineScope.launch {
                        gamePreferences.saveOnboardingCompleted(true)
                    }
                }
            )
        }
        if (activeAppPopup == AppPopup.NotificationPermission) pendingNotificationToggle?.let { toggle ->
            PolishedGameDialog(
                onDismissRequest = { pendingNotificationToggle = null },
                title = localizedContext.getString(R.string.notification_permission_title),
                confirmButton = {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        onClick = {
                            pendingNotificationToggle = null
                            if (hasNotificationPermission()) {
                                notificationPermissionGranted = true
                                logNotificationEvent(
                                    event = FirebaseEvent.NotificationPermissionGranted,
                                    type = toggle.notificationType,
                                    permissionStatus = "granted"
                                )
                                saveNotificationPreference(toggle, true)
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionRequestToggle = toggle
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    ) {
                        Text(
                            text = localizedContext.getString(R.string.notification_permission_allow),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { pendingNotificationToggle = null }
                    ) {
                        Text(
                            text = localizedContext.getString(R.string.notification_permission_not_now),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            ) {
                Text(
                    text = localizedContext.getString(R.string.notification_permission_message),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
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
                        isEffectSoundEnabled = isEffectSoundEnabled,
                        isVibrationEnabled = isVibrationEnabled,
                        isDailyRewardNotificationEnabled = isDailyRewardNotificationEnabled,
                        isStreakNotificationEnabled = isStreakNotificationEnabled,
                        isNewMissionNotificationEnabled = isNewMissionNotificationEnabled,
                        isNotificationPermissionGranted = notificationPermissionGranted,
                        isOnboardingCompleted = isOnboardingCompleted,
                        onOpenOnboarding = { showOnboarding = true },
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
                        onEffectSoundEnabledChange = { enabled ->
                            coroutineScope.launch {
                                gamePreferences.saveEffectSoundEnabled(enabled)
                            }
                        },
                        onVibrationEnabledChange = { enabled ->
                            coroutineScope.launch {
                                gamePreferences.saveVibrationEnabled(enabled)
                            }
                        },
                        onDailyRewardNotificationChange = { enabled ->
                            if (enabled && !hasNotificationPermission()) {
                                logNotificationEvent(
                                    event = FirebaseEvent.NotificationPermissionShown,
                                    type = NotificationToggle.DailyReward.notificationType,
                                    permissionStatus = "not_requested"
                                )
                                pendingNotificationToggle = NotificationToggle.DailyReward
                            } else {
                                saveNotificationPreference(NotificationToggle.DailyReward, enabled)
                            }
                        },
                        onStreakNotificationChange = { enabled ->
                            if (enabled && !hasNotificationPermission()) {
                                logNotificationEvent(
                                    event = FirebaseEvent.NotificationPermissionShown,
                                    type = NotificationToggle.Streak.notificationType,
                                    permissionStatus = "not_requested"
                                )
                                pendingNotificationToggle = NotificationToggle.Streak
                            } else {
                                saveNotificationPreference(NotificationToggle.Streak, enabled)
                            }
                        },
                        onNewMissionNotificationChange = { enabled ->
                            if (enabled && !hasNotificationPermission()) {
                                logNotificationEvent(
                                    event = FirebaseEvent.NotificationPermissionShown,
                                    type = NotificationToggle.Mission.notificationType,
                                    permissionStatus = "not_requested"
                                )
                                pendingNotificationToggle = NotificationToggle.Mission
                            } else {
                                saveNotificationPreference(NotificationToggle.Mission, enabled)
                            }
                        },
                        onRewardedAdRequested = onRewardedAdRequested,
                        onInterstitialAdRequested = onInterstitialAdRequested,
                        onInAppReviewRequested = ::maybeRequestInAppReview,
                        onRateAppClick = { onInAppReviewRequested(true) },
                        onExitAppRequested = { activity?.finish() },
                        onHowToPlayClick = {
                            if (navController.currentDestination?.route != HowToPlayRoute) {
                                navController.navigate(HowToPlayRoute) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
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

@Composable
private fun FirstLaunchOnboardingDialog(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val pages = remember {
        listOf(
            OnboardingPage(
                icon = "🎯",
                titleRes = R.string.onboarding_reflex_title,
                bodyRes = R.string.onboarding_reflex_body
            ),
            OnboardingPage(
                icon = "⚡",
                titleRes = R.string.onboarding_modes_title,
                bodyRes = R.string.onboarding_modes_body
            ),
            OnboardingPage(
                icon = "🪙",
                titleRes = R.string.onboarding_rewards_title,
                bodyRes = R.string.onboarding_rewards_body
            )
        )
    }
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }
    val safePageIndex = pageIndex.coerceIn(0, pages.lastIndex.coerceAtLeast(0))
    val page = pages.getOrNull(safePageIndex) ?: return

    // Same reason as PolishedGameDialog: a dialog window brings its own context, which would
    // override the in-app language for every string resolved inside it.
    val hostContext = LocalContext.current

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalContext provides hostContext) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GameDialogScrimColor)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 380.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
                    ),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = context.getString(R.string.onboarding_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = page.icon,
                            style = MaterialTheme.typography.displayMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = context.getString(page.titleRes),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = context.getString(page.bodyRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = context.getString(R.string.onboarding_step_value, safePageIndex + 1, pages.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                modifier = Modifier.weight(1f),
                                onClick = onFinish
                            ) {
                                Text(
                                    text = context.getString(R.string.onboarding_skip),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                onClick = {
                                    if (safePageIndex == pages.lastIndex) {
                                        onFinish()
                                    } else {
                                        pageIndex = safePageIndex + 1
                                    }
                                }
                            ) {
                                Text(
                                    text = if (safePageIndex == pages.lastIndex) {
                                        context.getString(R.string.onboarding_start)
                                    } else {
                                        context.getString(R.string.onboarding_next)
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
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
        RewardedAction.CoinChest -> "coin_chest"
        RewardedAction.ShopCoinReward -> "shop_coin_reward"
        RewardedAction.DailyChallengeDoubleReward -> "daily_challenge_double_reward"
        RewardedAction.Boost -> "boost"
        RewardedAction.SeasonXpBoost -> "season_xp_boost"
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
                onSeasonRewardClaim = {},
                onDailyStreakProtect = {},
                onPowerUpClick = { true },
                onAchievementClaim = {},
                onThemeSelect = {},
                onThemeBuy = {},
                onThemeTrial = {},
                onPlayerNameChange = { true },
                onPlayerTitleSelect = {},
                onProfileBadgeSelect = {},
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
