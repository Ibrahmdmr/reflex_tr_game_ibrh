package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import java.util.Locale
import kotlinx.coroutines.delay
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices

@Composable
fun HomeContent(
    bestScore: Int,
    bestScoresByMode: Map<GameMode, Int>,
    selectedMode: GameMode,
    dailyChallengeState: DailyChallengeState,
    progressionState: ProgressionState,
    playerProfile: PlayerProfile,
    leaderboardSnapshot: LeaderboardSnapshot,
    isSoundEnabled: Boolean,
    selectedLanguage: AppLanguage,
    onStartClick: () -> Unit,
    onModeStartClick: (GameMode) -> Unit,
    onHowToPlayClick: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSoundToggleClick: () -> Unit,
    onDailyRewardClaim: () -> Unit,
    onDailyStreakProtect: () -> Unit,
    onAchievementClaim: (String) -> Unit,
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onThemeTrial: (PlayerTheme) -> Unit,
    onPlayerNameChange: (String) -> Boolean,
    onPlayerTitleSelect: (PlayerTitle) -> Unit,
    onLeaderboardModeSelected: (GameMode) -> Unit,
    onLeaderboardPeriodSelected: (LeaderboardPeriod) -> Unit,
    onLeaderboardRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompactHeight = maxHeight <= 780.dp
        val contentScrollState = rememberScrollState()
        val panelPadding = if (isCompactHeight) 10.dp else 14.dp
        val contentSpacing = if (isCompactHeight) 8.dp else 10.dp
        var selectedHomeTab by remember { mutableStateOf(HomeTab.Play) }
        var showDailyRewardPopup by remember(
            progressionState.dailyReward.canClaim,
            progressionState.dailyReward.canProtectStreak
        ) {
            mutableStateOf(progressionState.dailyReward.canClaim || progressionState.dailyReward.canProtectStreak)
        }
        var showPlayerNameDialog by remember(playerProfile.name, playerProfile.hasCompletedNamePrompt) {
            mutableStateOf(!playerProfile.hasName && !playerProfile.hasCompletedNamePrompt)
        }

        if (showDailyRewardPopup && (progressionState.dailyReward.canClaim || progressionState.dailyReward.canProtectStreak)) {
            DailyRewardPopup(
                state = progressionState.dailyReward,
                selectedLanguage = selectedLanguage,
                onClaimClick = {
                    onDailyRewardClaim()
                    showDailyRewardPopup = false
                },
                onProtectClick = {
                    onDailyStreakProtect()
                    showDailyRewardPopup = false
                },
                onDismiss = { showDailyRewardPopup = false }
            )
        }

        if (showPlayerNameDialog) {
            PlayerNameDialog(
                currentName = playerProfile.name,
                onSave = { name ->
                    val saved = onPlayerNameChange(name)
                    if (saved) showPlayerNameDialog = false
                    saved
                },
                onDismiss = {
                    if (playerProfile.hasName) showPlayerNameDialog = false
                }
            )
        }

        GamePanelCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight),
            contentPadding = panelPadding,
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(contentSpacing)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(contentScrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(contentSpacing)
                    ) {
                        HomeHeader(
                            isCompactHeight = isCompactHeight,
                            isSoundEnabled = isSoundEnabled,
                            onSoundToggleClick = onSoundToggleClick
                        )

                        when (selectedHomeTab) {
                            HomeTab.Play -> PlayTabContent(
                                bestScore = bestScore,
                                bestScoresByMode = bestScoresByMode,
                                selectedMode = selectedMode,
                                dailyChallengeState = dailyChallengeState,
                                progressionState = progressionState,
                                onModeStartClick = onModeStartClick,
                                onHowToPlayClick = onHowToPlayClick,
                                onDailyStreakProtect = onDailyStreakProtect
                            )

                            HomeTab.Profile -> ProfileTabContent(
                                bestScore = bestScore,
                                playerProfile = playerProfile,
                                progressionState = progressionState,
                                selectedLanguage = selectedLanguage,
                                onEditNameClick = { showPlayerNameDialog = true },
                                onTitleSelect = onPlayerTitleSelect,
                                onLanguageSelected = onLanguageSelected
                            )

                            HomeTab.Missions -> MissionsTabContent(
                                dailyChallengeState = dailyChallengeState,
                                progressionState = progressionState,
                                onDailyRewardClaim = onDailyRewardClaim,
                                onDailyStreakProtect = onDailyStreakProtect,
                                onAchievementClaim = onAchievementClaim
                            )

                            HomeTab.Shop -> ShopTabContent(
                        progressionState = progressionState,
                        selectedLanguage = selectedLanguage,
                        onThemeSelect = onThemeSelect,
                        onThemeBuy = onThemeBuy,
                        onThemeTrial = onThemeTrial
                            )

                            HomeTab.Leaderboard -> LeaderboardTabContent(
                                leaderboardSnapshot = leaderboardSnapshot,
                                onModeSelected = onLeaderboardModeSelected,
                                onPeriodSelected = onLeaderboardPeriodSelected,
                                onRefreshClick = onLeaderboardRefresh
                            )
                        }
                    }

                    HomeBottomNavigation(
                        selectedTab = selectedHomeTab,
                        onTabSelected = { selectedHomeTab = it }
                    )

                    if (selectedHomeTab == HomeTab.Play) {
                        PrimaryGameButton(
                            text = stringResource(selectedMode.startButtonRes),
                            onClick = onStartClick,
                            height = if (isCompactHeight) 50.dp else 54.dp
                        )
                    }
                }
            }
        )
    }
}

private enum class HomeTab(val titleRes: Int, val icon: String) {
    Play(R.string.nav_play, "▶"),
    Profile(R.string.nav_profile, "◆"),
    Missions(R.string.nav_missions, "✓"),
    Shop(R.string.nav_shop, "◉"),
    Leaderboard(R.string.nav_leaderboard, "#")
}

@Composable
private fun HomeHeader(
    isCompactHeight: Boolean,
    isSoundEnabled: Boolean,
    onSoundToggleClick: () -> Unit
) {
    GameLogo(isCompactHeight = isCompactHeight)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(34.dp))
        Text(
            text = stringResource(R.string.game_title),
            modifier = Modifier.weight(1f),
            style = if (isCompactHeight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
            color = ReflexGamePalette.textPrimary,
            textAlign = TextAlign.Center
        )
        SoundToggleButton(
            isSoundEnabled = isSoundEnabled,
            onClick = onSoundToggleClick
        )
    }
}

@Composable
private fun PlayTabContent(
    bestScore: Int,
    bestScoresByMode: Map<GameMode, Int>,
    selectedMode: GameMode,
    dailyChallengeState: DailyChallengeState,
    progressionState: ProgressionState,
    onModeStartClick: (GameMode) -> Unit,
    onHowToPlayClick: () -> Unit,
    onDailyStreakProtect: () -> Unit
) {
    Text(
        text = stringResource(R.string.game_tagline),
        style = MaterialTheme.typography.bodyMedium,
        color = ReflexGamePalette.textSecondary,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    HomeQuickStats(
        bestScore = bestScore,
        progressionState = progressionState
    )
    DailyStreakMiniCard(
        state = progressionState.dailyReward,
        onProtectClick = onDailyStreakProtect
    )
    DailyChallengeCard(state = dailyChallengeState)
    GameModeSection(
        bestScoresByMode = bestScoresByMode,
        selectedMode = selectedMode,
        onModeStartClick = onModeStartClick
    )
    HowToPlayEntryCard(onClick = onHowToPlayClick)
}

@Composable
private fun ProfileTabContent(
    bestScore: Int,
    playerProfile: PlayerProfile,
    progressionState: ProgressionState,
    selectedLanguage: AppLanguage,
    onEditNameClick: () -> Unit,
    onTitleSelect: (PlayerTitle) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    ProfileProgressCard(
        playerProfile = playerProfile,
        progressionState = progressionState,
        bestScore = bestScore,
        onEditNameClick = onEditNameClick,
        onTitleSelect = onTitleSelect
    )
    CoinWalletCard(
        coins = progressionState.coins,
        selectedTheme = progressionState.selectedTheme
    )
    AchievementSummaryCard(achievements = progressionState.achievements)
    LanguageSelectionSection(
        selectedLanguage = selectedLanguage,
        onLanguageSelected = onLanguageSelected
    )
}

@Composable
private fun MissionsTabContent(
    dailyChallengeState: DailyChallengeState,
    progressionState: ProgressionState,
    onDailyRewardClaim: () -> Unit,
    onDailyStreakProtect: () -> Unit,
    onAchievementClaim: (String) -> Unit
) {
    DailyChallengeCard(state = dailyChallengeState)
    WeeklyChallengeCard(state = progressionState.weeklyChallenge)
    DailyRewardCard(
        state = progressionState.dailyReward,
        onClaimClick = onDailyRewardClaim,
        onProtectClick = onDailyStreakProtect
    )
    AchievementSection(
        achievements = progressionState.achievements,
        unlockedIds = progressionState.latestUnlockedAchievementIds,
        onClaimClick = onAchievementClaim
    )
}

@Composable
private fun ShopTabContent(
    progressionState: ProgressionState,
    selectedLanguage: AppLanguage,
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onThemeTrial: (PlayerTheme) -> Unit
) {
    Text(
        text = stringResource(R.string.theme_shop_title),
        style = MaterialTheme.typography.titleLarge,
        color = ReflexGamePalette.textPrimary
    )
    Text(
        text = stringResource(R.string.theme_shop_description),
        style = MaterialTheme.typography.bodyMedium,
        color = ReflexGamePalette.textSecondary,
        textAlign = TextAlign.Center
    )
    ThemeShopSection(
        progressionState = progressionState,
        selectedLanguage = selectedLanguage,
        onThemeSelect = onThemeSelect,
        onThemeBuy = onThemeBuy,
        onThemeTrial = onThemeTrial
    )
}

@Composable
private fun LeaderboardTabContent(
    leaderboardSnapshot: LeaderboardSnapshot,
    onModeSelected: (GameMode) -> Unit,
    onPeriodSelected: (LeaderboardPeriod) -> Unit,
    onRefreshClick: () -> Unit
) {
    LaunchedEffect(Unit) {
        FirebaseGameServices.logEvent(FirebaseEvent.LeaderboardOpen)
    }
    LeaderboardSection(
        snapshot = leaderboardSnapshot,
        onModeSelected = onModeSelected,
        onPeriodSelected = onPeriodSelected,
        onRefreshClick = onRefreshClick
    )
}

@Composable
private fun HomeQuickStats(
    bestScore: Int,
    progressionState: ProgressionState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickStatCard(
            title = stringResource(R.string.coin_wallet_title),
            value = stringResource(R.string.coin_wallet_value, progressionState.coins),
            accent = ArcadeGold,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = stringResource(R.string.profile_title),
            value = stringResource(R.string.level_value, progressionState.level),
            accent = ArcadeTeal,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = stringResource(R.string.best_score_label),
            value = bestScore.toString(),
            accent = ArcadeBlue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = ReflexGamePalette.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AchievementSummaryCard(
    achievements: List<AchievementState>
) {
    val unlockedCount = achievements.count { it.unlocked }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ArcadeTeal.copy(alpha = 0.12f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "★",
                style = MaterialTheme.typography.titleMedium,
                color = ArcadeGold
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.achievements_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.achievement_summary_value, unlockedCount, achievements.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun HomeBottomNavigation(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.06f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            HomeTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) },
                    color = if (selected) ArcadeBlue.copy(alpha = 0.24f) else Color.Transparent,
                    shape = RoundedCornerShape(15.dp),
                    border = if (selected) {
                        BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.5f))
                    } else {
                        null
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = tab.icon,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary
                        )
                        Text(
                            text = stringResource(tab.titleRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SoundToggleButton(
    isSoundEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isSoundEnabled) ArcadeTeal else ReflexGamePalette.textSecondary
    val scale by animateFloatAsState(
        targetValue = if (isSoundEnabled) 1f else 0.94f,
        animationSpec = tween(durationMillis = 160),
        label = "sound_toggle_scale"
    )
    val contentDescription = if (isSoundEnabled) {
        stringResource(R.string.sound_on)
    } else {
        stringResource(R.string.sound_off)
    }

    Surface(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (isSoundEnabled) 10f else 2f
            }
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        color = accentColor.copy(alpha = if (isSoundEnabled) 0.18f else 0.1f),
        shape = CircleShape,
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (isSoundEnabled) 0.52f else 0.26f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(
                    if (isSoundEnabled) {
                        R.drawable.ic_volume_up_24
                    } else {
                        R.drawable.ic_volume_off_24
                    }
                ),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HowToPlayEntryCard(
    onClick: () -> Unit
) {
    val interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(ArcadeGold)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.how_to_play_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.how_to_play_home_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textSecondary
                )
            }
            Text(
                text = stringResource(R.string.open_details),
                style = MaterialTheme.typography.labelMedium,
                color = ArcadeGold
            )
        }
    }
}

@Composable
private fun GameModeSection(
    bestScoresByMode: Map<GameMode, Int>,
    selectedMode: GameMode,
    onModeStartClick: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.game_modes_title),
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
        GameMode.entries.forEach { mode ->
            GameModeCard(
                mode = mode,
                bestScore = bestScoresByMode[mode] ?: 0,
                selected = mode == selectedMode,
                onClick = { onModeStartClick(mode) }
            )
        }
    }
}

@Composable
internal fun GameModeCard(
    mode: GameMode,
    onClick: () -> Unit,
    bestScore: Int? = null,
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val accentColor = modeAccentColor(mode)
    val interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.01f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "mode_card_selected_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (selected) 14f else 4f
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = if (selected) accentColor.copy(alpha = 0.18f) else ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (selected) 0.62f else 0.28f))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = modeIcon(mode),
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(mode.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(mode.descriptionRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    color = accentColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = stringResource(mode.difficultyRes),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (bestScore != null) {
                Text(
                    text = stringResource(R.string.mode_best_score_value, bestScore),
                    modifier = Modifier.padding(start = 36.dp, end = 12.dp, bottom = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun DailyChallengeCard(
    state: DailyChallengeState,
    modifier: Modifier = Modifier
) {
    val accent = if (state.completed) ArcadeTeal else ArcadeGold
    val scale by animateFloatAsState(
        targetValue = if (state.completed) 1.01f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "daily_challenge_complete_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (state.completed) 16f else 6f
            },
        color = if (state.completed) {
            ArcadeTeal.copy(alpha = 0.12f)
        } else {
            ReflexGamePalette.cardGlassStrong
        },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = if (state.completed) 0.42f else 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.completed) "✓" else "!",
                    style = MaterialTheme.typography.titleMedium,
                    color = accent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.daily_challenge_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent
                )
                Text(
                    text = if (state.completed) {
                        stringResource(R.string.daily_challenge_completed_title)
                    } else {
                        stringResource(state.type.titleRes)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (state.completed) {
                        stringResource(R.string.daily_challenge_completed_description)
                    } else {
                        stringResource(state.type.descriptionRes)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                color = accent.copy(alpha = if (state.completed) 0.2f else 0.14f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = if (state.completed) 0.42f else 0.24f))
            ) {
                Text(
                    text = if (state.completed) {
                        stringResource(R.string.daily_challenge_completed_badge)
                    } else {
                        stringResource(R.string.daily_challenge_progress, state.progress, state.target)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CoinWalletCard(
    coins: Int,
    selectedTheme: PlayerTheme,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ArcadeGold.copy(alpha = 0.13f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.34f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ArcadeGold.copy(alpha = 0.24f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "◉", color = ArcadeGold, style = MaterialTheme.typography.titleMedium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.coin_wallet_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcadeGold
                )
                Text(
                    text = stringResource(R.string.coin_wallet_value, coins),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.selected_theme_value, stringResource(selectedTheme.titleRes)),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun DailyStreakMiniCard(
    state: DailyRewardState,
    onProtectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = when {
        state.isStreakAtRisk -> ArcadeCoral
        state.claimedToday -> ArcadeTeal
        else -> ArcadeGold
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "★", color = accent)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.isStreakAtRisk) {
                            stringResource(R.string.daily_reward_streak_at_risk_title)
                        } else {
                            stringResource(R.string.daily_reward_streak_value, state.streakDay)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.daily_reward_next_value, state.nextRewardCoins),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            DailyRewardProgressLine(state = state)
            if (state.isStreakAtRisk) {
                SecondaryGameButton(
                    text = stringResource(R.string.daily_reward_protect_button),
                    onClick = onProtectClick
                )
            }
        }
    }
}

@Composable
private fun DailyRewardCard(
    state: DailyRewardState,
    onClaimClick: () -> Unit,
    onProtectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = when {
        state.isStreakAtRisk -> ArcadeCoral
        state.canClaim -> ArcadeGold
        else -> ArcadeTeal
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (state.canClaim || state.isStreakAtRisk) ReflexGamePalette.cardGlassStrong else ArcadeTeal.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (state.isStreakAtRisk) "!" else if (state.canClaim) "★" else "✓", color = accent)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.daily_reward_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = accent
                    )
                    Text(
                        text = stringResource(R.string.daily_reward_streak, state.streakDay),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary
                    )
                    Text(
                        text = dailyRewardText(state),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary
                    )
                }
            }
            DailyRewardProgressLine(state = state)
            if (state.canClaim) {
                PrimaryGameButton(
                    text = if (state.isSuperReward) {
                        stringResource(R.string.daily_reward_super_claim)
                    } else {
                        stringResource(R.string.claim_reward)
                    },
                    onClick = onClaimClick
                )
            } else if (state.isStreakAtRisk) {
                Text(
                    text = stringResource(R.string.daily_reward_protect_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
                SecondaryGameButton(
                    text = stringResource(R.string.daily_reward_protect_button),
                    onClick = onProtectClick
                )
            } else {
                Text(
                    text = stringResource(R.string.daily_reward_claimed_today),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun DailyRewardProgressLine(
    state: DailyRewardState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        DailyRewardCoinPlan.forEachIndexed { index, coins ->
            val dayNumber = index + 1
            val active = dayNumber <= state.dayInCycle && !state.isStreakAtRisk
            val isToday = dayNumber == state.dayInCycle
            val color = when {
                isToday && state.isSuperReward -> ArcadeGold
                active -> ArcadeTeal
                else -> Color.White.copy(alpha = 0.18f)
            }
            Surface(
                modifier = Modifier.weight(1f),
                color = color.copy(alpha = if (active) 0.22f else 0.08f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, color.copy(alpha = 0.42f))
            ) {
                Text(
                    text = if (dayNumber == DailyRewardCoinPlan.size) {
                        stringResource(R.string.daily_reward_day_super)
                    } else {
                        stringResource(R.string.daily_reward_day_short, dayNumber)
                    },
                    modifier = Modifier.padding(horizontal = 1.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProfileProgressCard(
    playerProfile: PlayerProfile,
    progressionState: ProgressionState,
    bestScore: Int,
    onEditNameClick: () -> Unit,
    onTitleSelect: (PlayerTitle) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLevelXp = ((progressionState.level - 1) * 250)
    val nextLevelXp = progressionState.level * 250
    val levelProgress = ((progressionState.xp - currentLevelXp).toFloat() / 250f).coerceIn(0f, 1f)
    val rank = rankFor(score = bestScore, level = progressionState.level)
    val achievementCount = progressionState.achievements.count { it.unlocked }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.neonBlue.copy(alpha = 0.14f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ReflexGamePalette.neonBlue.copy(alpha = 0.32f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = ArcadeTeal
                    )
                    Text(
                        text = playerProfile.name.ifBlank { stringResource(R.string.leaderboard_you) },
                        style = MaterialTheme.typography.titleMedium,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(playerProfile.title.titleRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = onEditNameClick) {
                        Text(
                            text = stringResource(R.string.profile_change_name),
                            color = ArcadeGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = stringResource(R.string.level_value, progressionState.level),
                        style = MaterialTheme.typography.labelMedium,
                        color = ArcadeTeal
                    )
                }
            }
            LinearProgressIndicator(
                progress = { levelProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = ArcadeTeal,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = stringResource(R.string.xp_value, progressionState.xp, nextLevelXp),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_games_value, progressionState.totalGames),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.profile_best_value, bestScore),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.profile_combo_value, progressionState.lifetimeMaxCombo),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_rank_value, stringResource(rank.titleRes)),
                    modifier = Modifier.weight(1.2f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.profile_achievements_value, achievementCount),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.coin_wallet_value, progressionState.coins),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (progressionState.dailyReward.loyalBadgeUnlocked) {
                Surface(
                    color = ArcadeGold.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.38f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.daily_reward_loyal_badge),
                            style = MaterialTheme.typography.labelLarge,
                            color = ArcadeGold
                        )
                        Text(
                            text = stringResource(R.string.daily_reward_loyal_badge_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = ReflexGamePalette.textSecondary
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.profile_title_select),
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary
                )
                PlayerTitle.entries.chunked(2).forEach { rowTitles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTitles.forEach { title ->
                            val selected = title == playerProfile.title
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onTitleSelect(title) },
                                color = if (selected) ArcadeTeal.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(
                                    1.dp,
                                    (if (selected) ArcadeTeal else ArcadeBlue).copy(alpha = 0.34f)
                                )
                            ) {
                                Text(
                                    text = stringResource(title.titleRes),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ReflexGamePalette.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            if (progressionState.lastLevelUp != null) {
                Text(
                    text = stringResource(R.string.level_up_value, progressionState.lastLevelUp),
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcadeGold
                )
            }
        }
    }
}

@Composable
private fun AchievementSection(
    achievements: List<AchievementState>,
    unlockedIds: List<String>,
    onClaimClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.achievements_title),
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
        achievements.forEach { achievement ->
            AchievementCard(
                achievement = achievement,
                highlighted = achievement.id in unlockedIds,
                onClaimClick = { onClaimClick(achievement.id) }
            )
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: AchievementState,
    highlighted: Boolean,
    onClaimClick: () -> Unit
) {
    val accent = when {
        achievement.claimed -> ArcadeTeal
        achievement.unlocked -> ArcadeGold
        else -> ArcadeBlue
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (highlighted || achievement.unlocked) accent.copy(alpha = 0.13f) else ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = if (highlighted) 0.62f else 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (achievement.unlocked) "★" else "◇",
                    style = MaterialTheme.typography.titleMedium,
                    color = accent
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(achievement.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary
                    )
                    Text(
                        text = stringResource(achievement.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary
                    )
                }
                if (achievement.unlocked && !achievement.claimed) {
                    SecondaryGameButton(
                        text = stringResource(R.string.claim_reward),
                        onClick = onClaimClick,
                        modifier = Modifier.width(116.dp)
                    )
                }
            }
            LinearProgressIndicator(
                progress = { achievement.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = accent,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = stringResource(
                    R.string.achievement_progress_value,
                    achievement.progress.coerceAtMost(achievement.target),
                    achievement.target,
                    achievement.rewardCoins
                ),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textSecondary
            )
        }
    }
}

@Composable
private fun LeaderboardSection(
    snapshot: LeaderboardSnapshot,
    onModeSelected: (GameMode) -> Unit,
    onPeriodSelected: (LeaderboardPeriod) -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var showRefreshMessage by remember(snapshot.refreshedTick, snapshot.statusMessageRes) {
        mutableStateOf(snapshot.statusMessageRes != null)
    }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(450)
            isRefreshing = false
        }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.leaderboard_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.leaderboard_local_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
            Surface(
                modifier = Modifier.clickable {
                    isRefreshing = true
                    onRefreshClick()
                },
                color = ArcadeBlue.copy(alpha = 0.18f),
                shape = CircleShape,
                border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.4f))
            ) {
                Text(
                    text = if (isRefreshing || snapshot.isLoading) "…" else "↻",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
        LeaderboardPeriodSelector(
            selectedPeriod = snapshot.selectedPeriod,
            onPeriodSelected = onPeriodSelected
        )
        LeaderboardModeSelector(
            selectedMode = snapshot.selectedMode,
            onModeSelected = onModeSelected
        )
        val statusMessageRes = snapshot.statusMessageRes
        if (showRefreshMessage && statusMessageRes != null) {
            Text(
                text = stringResource(statusMessageRes),
                style = MaterialTheme.typography.bodySmall,
                color = if (snapshot.isOffline) ArcadeGold else ArcadeTeal
            )
        }
        Text(
            text = if (snapshot.motivationRes == R.string.leaderboard_motivation_pass_player) {
                stringResource(
                    snapshot.motivationRes,
                    snapshot.motivationPlayerName,
                    snapshot.motivationScoreGap
                )
            } else {
                stringResource(snapshot.motivationRes)
            },
            style = MaterialTheme.typography.bodySmall,
            color = ArcadeGold
        )
        snapshot.entries.forEach { entry ->
            val accent = when {
                entry.isPlayer -> ArcadeGold
                entry.rank <= 3 -> ArcadeTeal
                else -> themeAccentColor(entry.theme)
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (entry.isPlayer) ArcadeGold.copy(alpha = 0.18f) else ReflexGamePalette.cardGlassStrong,
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = if (entry.rank <= 3 || entry.isPlayer) 0.58f else 0.26f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text(
                        text = "#${entry.rank}",
                        modifier = Modifier.width(34.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (entry.rank <= 3 || entry.isPlayer) ArcadeGold else ReflexGamePalette.textSecondary
                    )
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(themeAccentColor(entry.theme))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (entry.isPlayer) {
                                stringResource(R.string.leaderboard_you_named, entry.name)
                            } else {
                                entry.name
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = ReflexGamePalette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(entry.rankTier.titleRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = ReflexGamePalette.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = entry.score.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (entry.isPlayer) ArcadeGold else ReflexGamePalette.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardPeriodSelector(
    selectedPeriod: LeaderboardPeriod,
    onPeriodSelected: (LeaderboardPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(LeaderboardPeriod.AllTime).forEach { period ->
            val selected = period == selectedPeriod
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPeriodSelected(period) },
                color = if (selected) ArcadeGold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, (if (selected) ArcadeGold else ArcadeBlue).copy(alpha = 0.36f))
            ) {
                Text(
                    text = stringResource(
                        when (period) {
                            LeaderboardPeriod.Weekly -> R.string.leaderboard_period_weekly
                            LeaderboardPeriod.AllTime -> R.string.leaderboard_period_all_time
                        }
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) ArcadeGold else ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LeaderboardModeSelector(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit
) {
    GameMode.entries.chunked(2).forEach { rowModes ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rowModes.forEach { mode ->
                val selected = mode == selectedMode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onModeSelected(mode) },
                    color = if (selected) themeAccentColor(PlayerTheme.CyberBlue).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, (if (selected) ArcadeTeal else ArcadeBlue).copy(alpha = 0.34f))
                ) {
                    Text(
                        text = stringResource(mode.titleRes),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyChallengeCard(
    state: ChallengeState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (state.completed) ArcadeTeal.copy(alpha = 0.12f) else ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.weekly_challenge_title),
                style = MaterialTheme.typography.labelLarge,
                color = ArcadeTeal
            )
            Text(
                text = stringResource(state.titleRes),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary
            )
            Text(
                text = stringResource(state.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary
            )
            LinearProgressIndicator(
                progress = { state.progress.toFloat() / state.target.toFloat().coerceAtLeast(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = ArcadeTeal,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = stringResource(R.string.weekly_challenge_progress, state.progress, state.target, state.rewardCoins),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textSecondary
            )
        }
    }
}

@Composable
private fun PlayerNameDialog(
    currentName: String,
    onSave: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    var name by remember(currentName) { mutableStateOf(currentName.ifBlank { randomPlayerNameSuggestion() }) }
    var hasError by remember { mutableStateOf(false) }
    val titleText = stringResource(R.string.player_name_dialog_title)
    val descriptionText = stringResource(R.string.player_name_dialog_description)
    val hintText = stringResource(R.string.player_name_dialog_hint)
    val saveText = stringResource(R.string.player_name_save)
    val errorText = stringResource(R.string.player_name_error)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ReflexGamePalette.cardGlassStrong,
        title = {
            Text(
                text = titleText,
                color = ReflexGamePalette.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textSecondary
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(12)
                        hasError = false
                    },
                    singleLine = true,
                    isError = hasError,
                    label = {
                        Text(text = hintText)
                    }
                )
                if (hasError) {
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArcadeCoral
                    )
                }
            }
        },
        confirmButton = {
            PrimaryGameButton(
                text = saveText,
                onClick = {
                    hasError = !onSave(name)
                }
            )
        }
    )
}

private fun randomPlayerNameSuggestion(): String {
    return listOf("Nova", "Blitz", "Echo", "Pulse", "Shadow", "CyberX").random()
}

@Composable
private fun DailyRewardPopup(
    state: DailyRewardState,
    selectedLanguage: AppLanguage,
    onClaimClick: () -> Unit,
    onProtectClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = when {
        state.isStreakAtRisk -> localizedHomeStringResource(R.string.daily_reward_streak_at_risk_title, selectedLanguage)
        state.isSuperReward -> localizedHomeStringResource(R.string.daily_reward_super_title, selectedLanguage)
        else -> localizedHomeStringResource(R.string.daily_reward_title, selectedLanguage)
    }
    val message = when {
        state.isStreakAtRisk -> localizedHomeStringResource(R.string.daily_reward_protect_message, selectedLanguage)
        state.isSuperReward -> localizedHomeStringResource(
            id = R.string.daily_reward_super_message,
            selectedLanguage = selectedLanguage,
            state.rewardCoins
        )
        else -> localizedHomeStringResource(
            id = R.string.daily_reward_popup_message,
            selectedLanguage = selectedLanguage,
            state.rewardCoins
        )
    }
    val claimText = when {
        state.isStreakAtRisk -> localizedHomeStringResource(R.string.daily_reward_protect_button, selectedLanguage)
        state.isSuperReward -> localizedHomeStringResource(R.string.daily_reward_super_claim, selectedLanguage)
        else -> localizedHomeStringResource(R.string.daily_reward_continue, selectedLanguage)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ReflexGamePalette.cardGlassStrong,
        title = {
            Text(
                text = title,
                color = ReflexGamePalette.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = message,
                    color = ReflexGamePalette.textSecondary
                )
                DailyRewardProgressLine(state = state)
            }
        },
        confirmButton = {
            PrimaryGameButton(
                text = claimText,
                onClick = if (state.isStreakAtRisk) onProtectClick else onClaimClick
            )
        }
    )
}

@Composable
private fun dailyRewardText(state: DailyRewardState): String {
    return when (state.rewardType) {
        DailyRewardType.Coins -> stringResource(R.string.daily_reward_coin_value, state.rewardCoins)
        DailyRewardType.SuperBox -> stringResource(R.string.daily_reward_super_value, state.rewardCoins)
    }
}

@Composable
private fun ThemeShopSection(
    progressionState: ProgressionState,
    selectedLanguage: AppLanguage,
    onThemeSelect: (PlayerTheme) -> Unit,
    onThemeBuy: (PlayerTheme) -> Unit,
    onThemeTrial: (PlayerTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    var unlockedThemePopup by remember { mutableStateOf<PlayerTheme?>(null) }

    unlockedThemePopup?.let { theme ->
        ThemeUnlockDialog(
            theme = theme,
            selectedLanguage = selectedLanguage,
            onDismiss = { unlockedThemePopup = null },
            onSelectClick = {
                onThemeSelect(theme)
                unlockedThemePopup = null
            }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.theme_shop_title),
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
        PlayerTheme.entries.forEach { theme ->
            ThemeCard(
                theme = theme,
                selected = progressionState.activeTheme == theme,
                trialActive = progressionState.trialTheme == theme,
                unlocked = theme in progressionState.unlockedThemes,
                canBuy = progressionState.coins >= theme.coinPrice,
                onSelect = { onThemeSelect(theme) },
                onBuy = {
                    onThemeBuy(theme)
                    if (progressionState.coins >= theme.coinPrice) {
                        unlockedThemePopup = theme
                    }
                },
                onTrial = { onThemeTrial(theme) }
            )
        }
    }
}

@Composable
private fun ThemeCard(
    theme: PlayerTheme,
    selected: Boolean,
    trialActive: Boolean,
    unlocked: Boolean,
    canBuy: Boolean,
    onSelect: () -> Unit,
    onBuy: () -> Unit,
    onTrial: () -> Unit
) {
    val spec = themeVisualSpec(theme)
    val accent = spec.primary
    val rarityGlow = when (theme.rarity) {
        ThemeRarity.Common -> 0.18f
        ThemeRarity.Rare -> 0.28f
        ThemeRarity.Epic -> 0.42f
        ThemeRarity.Legendary -> 0.58f
        ThemeRarity.Mythic -> 0.82f
    }
    val pulse by rememberInfiniteTransition(label = "theme_card_pulse").animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "theme_card_pulse_value"
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                shadowElevation = if (theme.rarity == ThemeRarity.Mythic) 24f * pulse else 8f
            },
        color = if (selected || trialActive) accent.copy(alpha = 0.16f + rarityGlow * 0.08f) else ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            width = if (theme.rarity == ThemeRarity.Mythic) 1.8.dp else 1.dp,
            color = accent.copy(alpha = if (selected || trialActive) 0.72f else 0.26f + rarityGlow * 0.38f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemePreview(theme = theme, pulse = pulse)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(theme.titleRes),
                            style = MaterialTheme.typography.titleSmall,
                            color = ReflexGamePalette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            color = accent.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
                        ) {
                            Text(
                                text = stringResource(theme.rarity.titleRes),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = ReflexGamePalette.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Text(
                        text = stringResource(theme.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            trialActive -> stringResource(R.string.theme_trial_active)
                            selected -> stringResource(R.string.theme_selected)
                            unlocked -> stringResource(R.string.theme_unlocked)
                            else -> stringResource(R.string.theme_price, theme.coinPrice)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (canBuy || unlocked || selected || trialActive) accent else ReflexGamePalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(
                    modifier = Modifier.width(132.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecondaryGameButton(
                        text = when {
                            selected -> stringResource(R.string.theme_selected)
                            unlocked -> stringResource(R.string.select_theme)
                            else -> stringResource(R.string.buy_theme)
                        },
                        enabled = when {
                            selected -> false
                            unlocked -> true
                            else -> canBuy
                        },
                        onClick = when {
                            unlocked -> onSelect
                            else -> onBuy
                        }
                    )
                    if (!unlocked && !trialActive) {
                        SecondaryGameButton(
                            text = stringResource(R.string.theme_try_ad),
                            onClick = onTrial
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePreview(
    theme: PlayerTheme,
    pulse: Float
) {
    val spec = themeVisualSpec(theme)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        spec.backgroundTop,
                        spec.primary.copy(alpha = 0.78f),
                        spec.backgroundBottom
                    )
                )
            )
    ) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .offset(x = (24 + index * 52).dp, y = (12 + (index % 3) * 13).dp)
                    .size((10 + index * 2).dp)
                    .clip(CircleShape)
                    .background(spec.secondary.copy(alpha = 0.16f + pulse * 0.18f))
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 26.dp)
                .size(46.dp)
                .graphicsLayer {
                    scaleX = 0.92f + pulse * 0.08f
                    scaleY = 0.92f + pulse * 0.08f
                    shadowElevation = 28f
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.88f),
                            spec.primary.copy(alpha = 0.72f),
                            spec.secondary
                        )
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.62f), CircleShape)
        )
        Text(
            text = stringResource(spec.previewLabelRes),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            style = MaterialTheme.typography.labelLarge,
            color = ReflexGamePalette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ThemeUnlockDialog(
    theme: PlayerTheme,
    selectedLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSelectClick: () -> Unit
) {
    val spec = themeVisualSpec(theme)
    val title = localizedHomeStringResource(R.string.theme_unlocked_popup_title, selectedLanguage)
    val themeTitle = localizedHomeStringResource(theme.titleRes, selectedLanguage)
    val message = localizedHomeStringResource(
        id = R.string.theme_unlocked_popup_message,
        selectedLanguage = selectedLanguage,
        themeTitle
    )
    val selectText = localizedHomeStringResource(R.string.select_theme, selectedLanguage)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ReflexGamePalette.cardGlassStrong,
        title = {
            Text(
                text = title,
                color = spec.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemePreview(theme = theme, pulse = 1f)
                Text(
                    text = message,
                    color = ReflexGamePalette.textSecondary
                )
            }
        },
        confirmButton = {
            PrimaryGameButton(
                text = selectText,
                onClick = onSelectClick
            )
        }
    )
}

@Composable
private fun localizedHomeStringResource(
    @StringRes id: Int,
    selectedLanguage: AppLanguage,
    vararg args: Any
): String {
    val context = LocalContext.current
    val localizedContext = remember(context, selectedLanguage) {
        context.createHomeLanguageContext(selectedLanguage)
    }
    return if (args.isEmpty()) {
        localizedContext.getString(id)
    } else {
        localizedContext.getString(id, *args)
    }
}

private fun Context.createHomeLanguageContext(language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.code)
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}

internal fun themeAccentColor(theme: PlayerTheme): Color {
    return themeVisualSpec(theme).primary
}

internal data class ThemeVisualSpec(
    val primary: Color,
    val secondary: Color,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    @StringRes val previewLabelRes: Int
)

internal fun themeVisualSpec(theme: PlayerTheme): ThemeVisualSpec {
    return when (theme) {
        PlayerTheme.NeonRed -> ThemeVisualSpec(
            primary = ArcadeCoral,
            secondary = Color(0xFFFF7A8A),
            backgroundTop = Color(0xFF170816),
            backgroundBottom = Color(0xFF3A1022),
            previewLabelRes = R.string.theme_preview_neon
        )
        PlayerTheme.CyberBlue -> ThemeVisualSpec(
            primary = ArcadeBlue,
            secondary = Color(0xFF49F3FF),
            backgroundTop = Color(0xFF06142E),
            backgroundBottom = Color(0xFF0D3B7A),
            previewLabelRes = R.string.theme_preview_cyber
        )
        PlayerTheme.PurpleStorm -> ThemeVisualSpec(
            primary = ReflexGamePalette.neonPurple,
            secondary = Color(0xFFFF4FD8),
            backgroundTop = Color(0xFF160826),
            backgroundBottom = Color(0xFF45209B),
            previewLabelRes = R.string.theme_preview_storm
        )
        PlayerTheme.IceNeon -> ThemeVisualSpec(
            primary = Color(0xFF8DEBFF),
            secondary = Color(0xFFB9F8FF),
            backgroundTop = Color(0xFF061927),
            backgroundBottom = Color(0xFF1E6B88),
            previewLabelRes = R.string.theme_preview_ice
        )
        PlayerTheme.LavaCore -> ThemeVisualSpec(
            primary = Color(0xFFFF5A1F),
            secondary = Color(0xFFFFC857),
            backgroundTop = Color(0xFF230606),
            backgroundBottom = Color(0xFF7A210B),
            previewLabelRes = R.string.theme_preview_lava
        )
        PlayerTheme.ToxicPulse -> ThemeVisualSpec(
            primary = Color(0xFFB9FF2F),
            secondary = Color(0xFF2CFFB7),
            backgroundTop = Color(0xFF071907),
            backgroundBottom = Color(0xFF245B18),
            previewLabelRes = R.string.theme_preview_toxic
        )
        PlayerTheme.MatrixGreen -> ThemeVisualSpec(
            primary = Color(0xFF21FF72),
            secondary = Color(0xFF00D46A),
            backgroundTop = Color(0xFF020D08),
            backgroundBottom = Color(0xFF06351C),
            previewLabelRes = R.string.theme_preview_matrix
        )
        PlayerTheme.GoldFire -> ThemeVisualSpec(
            primary = ArcadeGold,
            secondary = Color(0xFFFF8A2A),
            backgroundTop = Color(0xFF1C1202),
            backgroundBottom = Color(0xFF6C3C05),
            previewLabelRes = R.string.theme_preview_gold
        )
        PlayerTheme.ShadowBlack -> ThemeVisualSpec(
            primary = Color(0xFF8A94A6),
            secondary = Color(0xFF30384A),
            backgroundTop = Color(0xFF02030A),
            backgroundBottom = Color(0xFF151827),
            previewLabelRes = R.string.theme_preview_shadow
        )
        PlayerTheme.GalaxyWave -> ThemeVisualSpec(
            primary = Color(0xFF6F8CFF),
            secondary = Color(0xFFFF5BEF),
            backgroundTop = Color(0xFF050421),
            backgroundBottom = Color(0xFF23116D),
            previewLabelRes = R.string.theme_preview_galaxy
        )
        PlayerTheme.RainbowFlux -> ThemeVisualSpec(
            primary = Color(0xFFFF4FD8),
            secondary = Color(0xFF49F3FF),
            backgroundTop = Color(0xFF15051F),
            backgroundBottom = Color(0xFF123A62),
            previewLabelRes = R.string.theme_preview_flux
        )
    }
}

@Composable
private fun LanguageSelectionSection(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.language_selection_title),
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LanguageChip(
                text = stringResource(R.string.language_turkish),
                selected = selectedLanguage == AppLanguage.Turkish,
                onClick = { onLanguageSelected(AppLanguage.Turkish) },
                modifier = Modifier.weight(1f)
            )
            LanguageChip(
                text = stringResource(R.string.language_english),
                selected = selectedLanguage == AppLanguage.English,
                onClick = { onLanguageSelected(AppLanguage.English) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LanguageChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) ArcadeGold else ReflexGamePalette.neonBlue
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = color.copy(alpha = if (selected) 0.22f else 0.1f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, color.copy(alpha = if (selected) 0.56f else 0.24f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleSmall,
            color = ReflexGamePalette.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

internal fun modeAccentColor(mode: GameMode): Color {
    return when (mode) {
        GameMode.Classic -> ArcadeGold
        GameMode.MovingTarget -> ArcadeBlue
        GameMode.FakeTarget -> ReflexGamePalette.targetRing
        GameMode.ColorReflex -> ArcadeTeal
    }
}

internal fun modeIcon(mode: GameMode): String {
    return when (mode) {
        GameMode.Classic -> "◎"
        GameMode.MovingTarget -> "↗"
        GameMode.FakeTarget -> "◇"
        GameMode.ColorReflex -> "◆"
    }
}

@Composable
private fun GameLogo(isCompactHeight: Boolean) {
    val containerSize = if (isCompactHeight) 50.dp else 82.dp
    val iconSize = if (isCompactHeight) 42.dp else 66.dp
    val badgeOffsetX = if (isCompactHeight) 17.dp else 28.dp
    val badgeOffsetY = if (isCompactHeight) (-15).dp else (-24).dp
    val badgeSize = if (isCompactHeight) 12.dp else 14.dp

    Box(
        modifier = Modifier.size(containerSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ReflexGamePalette.targetRing.copy(alpha = 0.34f),
                            ReflexGamePalette.neonPurple.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
        Image(
            painter = painterResource(R.drawable.refleks_avi_icon_full),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .shadow(22.dp, CircleShape, clip = false)
                .clip(CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = badgeOffsetX, y = badgeOffsetY)
                .size(badgeSize)
                .clip(CircleShape)
                .background(ArcadeGold)
        )
    }
}

@Composable
private fun BestScoreHero(
    bestScore: Int,
    isCompactHeight: Boolean
) {
    val rowHorizontalPadding = if (isCompactHeight) 14.dp else 16.dp
    val rowVerticalPadding = if (isCompactHeight) 12.dp else 14.dp
    val rowSpacing = if (isCompactHeight) 10.dp else 12.dp
    val iconSize = if (isCompactHeight) 38.dp else 42.dp

    Surface(
        color = ReflexGamePalette.neonBlue.copy(alpha = 0.18f),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = ReflexGamePalette.neonBlue.copy(alpha = 0.26f)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = rowHorizontalPadding,
                vertical = rowVerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(ArcadeGold.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    color = ArcadeGold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.best_score),
                    style = MaterialTheme.typography.labelLarge,
                    color = ReflexGamePalette.textSecondary
                )
                Text(
                    text = bestScore.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = ReflexGamePalette.textPrimary
                )
            }
        }
    }
}
