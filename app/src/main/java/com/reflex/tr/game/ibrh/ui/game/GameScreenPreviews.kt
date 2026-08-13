package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.theme.Reflex_tr_game_ibrhTheme

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    Reflex_tr_game_ibrhTheme {
        GameScreen(
            uiState = GameUiState(bestScore = 27),
            rewardedAdUiState = RewardedAdUiState(isReady = true),
            selectedLanguage = AppLanguage.Turkish,
            isSoundEnabled = true,
            onStartClick = {},
            onPowerUpClick = { true },
            onModeStartClick = {},
            onHowToPlayClick = {},
            onLanguageSelected = {},
            onSoundEnabledChange = {},
            onDailyRewardClaim = {},
            onSeasonRewardClaim = {},
            onDailyStreakProtect = {},
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

@Preview(showBackground = true)
@Composable
private fun PlayingPreview() {
    Reflex_tr_game_ibrhTheme {
        GameScreen(
            uiState = GameUiState(
                score = 12,
                bestScore = 27,
                lives = 2,
                timeLeftSeconds = 18,
                hasGameStarted = true
            ),
            rewardedAdUiState = RewardedAdUiState(isReady = true),
            selectedLanguage = AppLanguage.Turkish,
            isSoundEnabled = true,
            onStartClick = {},
            onPowerUpClick = { true },
            onModeStartClick = {},
            onHowToPlayClick = {},
            onLanguageSelected = {},
            onSoundEnabledChange = {},
            onDailyRewardClaim = {},
            onSeasonRewardClaim = {},
            onDailyStreakProtect = {},
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

@Preview(showBackground = true)
@Composable
private fun GameOverPreview() {
    Reflex_tr_game_ibrhTheme {
        GameScreen(
            uiState = GameUiState(
                score = 16,
                bestScore = 27,
                isNewBestScore = true,
                lives = 0,
                timeLeftSeconds = 0,
                hasGameStarted = true,
                isGameOver = true,
                gameOverReasonRes = R.string.game_over_reason_no_lives,
                canContinueWithReward = true
            ),
            rewardedAdUiState = RewardedAdUiState(isReady = true),
            selectedLanguage = AppLanguage.Turkish,
            isSoundEnabled = true,
            onStartClick = {},
            onPowerUpClick = { true },
            onModeStartClick = {},
            onHowToPlayClick = {},
            onLanguageSelected = {},
            onSoundEnabledChange = {},
            onDailyRewardClaim = {},
            onSeasonRewardClaim = {},
            onDailyStreakProtect = {},
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
