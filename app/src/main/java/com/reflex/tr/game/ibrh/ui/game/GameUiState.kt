package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import com.reflex.tr.game.ibrh.R

enum class GameMode(
    val storageKey: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val longDescriptionRes: Int,
    @StringRes val difficultyRes: Int,
    @StringRes val arenaTitleRes: Int,
    @StringRes val startButtonRes: Int
) {
    Classic(
        storageKey = "classic",
        titleRes = R.string.mode_classic_title,
        descriptionRes = R.string.mode_classic_description,
        longDescriptionRes = R.string.mode_classic_long_description,
        difficultyRes = R.string.mode_difficulty_easy,
        arenaTitleRes = R.string.arena_reflex,
        startButtonRes = R.string.start_classic_game
    ),
    MovingTarget(
        storageKey = "moving_target",
        titleRes = R.string.mode_moving_title,
        descriptionRes = R.string.mode_moving_description,
        longDescriptionRes = R.string.mode_moving_long_description,
        difficultyRes = R.string.mode_difficulty_medium,
        arenaTitleRes = R.string.arena_target,
        startButtonRes = R.string.start_moving_game
    ),
    FakeTarget(
        storageKey = "fake_target",
        titleRes = R.string.mode_fake_title,
        descriptionRes = R.string.mode_fake_description,
        longDescriptionRes = R.string.mode_fake_long_description,
        difficultyRes = R.string.mode_difficulty_hard,
        arenaTitleRes = R.string.arena_challenge,
        startButtonRes = R.string.start_fake_game
    ),
    ColorReflex(
        storageKey = "color_reflex",
        titleRes = R.string.mode_color_title,
        descriptionRes = R.string.mode_color_description,
        longDescriptionRes = R.string.mode_color_long_description,
        difficultyRes = R.string.mode_difficulty_medium,
        arenaTitleRes = R.string.arena_challenge,
        startButtonRes = R.string.start_color_game
    )
}

enum class DailyChallenge(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val targetValue: Int
) {
    Score20(
        titleRes = R.string.daily_challenge_score_20_title,
        descriptionRes = R.string.daily_challenge_score_20_description,
        targetValue = 20
    ),
    Play3Games(
        titleRes = R.string.daily_challenge_play_3_title,
        descriptionRes = R.string.daily_challenge_play_3_description,
        targetValue = 3
    ),
    FakeTarget10(
        titleRes = R.string.daily_challenge_fake_10_title,
        descriptionRes = R.string.daily_challenge_fake_10_description,
        targetValue = 10
    )
}

data class DailyChallengeState(
    val id: String,
    val type: DailyChallenge,
    val target: Int,
    val progress: Int,
    val completed: Boolean,
    val createdDate: String
) {
    companion object {
        fun default(): DailyChallengeState {
            return DailyChallengeState(
                id = "default_score20",
                type = DailyChallenge.Score20,
                target = DailyChallenge.Score20.targetValue,
                progress = 0,
                completed = false,
                createdDate = ""
            )
        }
    }
}

data class TargetPosition(
    val xFraction: Float = 0.5f,
    val yFraction: Float = 0.5f
)

enum class ReflexTargetColor(@StringRes val labelRes: Int) {
    Red(R.string.target_color_red),
    Blue(R.string.target_color_blue),
    Gold(R.string.target_color_yellow),
    Teal(R.string.target_color_green)
}

enum class GameTargetRole {
    Correct,
    Fake,
    WrongColor
}

data class GameTarget(
    val id: Long,
    val position: TargetPosition,
    val role: GameTargetRole = GameTargetRole.Correct,
    val color: ReflexTargetColor = ReflexTargetColor.Red
)

data class GameUiState(
    val score: Int = 0,
    val bestScore: Int = 0,
    val bestScoresByMode: Map<GameMode, Int> = GameMode.entries.associateWith { 0 },
    val isNewBestScore: Boolean = false,
    val difficultyLevel: Int = 1,
    val lives: Int = 3,
    val timeLeftSeconds: Int = 30,
    val targetPosition: TargetPosition = TargetPosition(),
    val targetSizeDp: Int = 82,
    val targetVisibleDurationMillis: Long = 1_800L,
    val targetLifetimeKey: Int = 0,
    val hasGameStarted: Boolean = false,
    val selectedMode: GameMode = GameMode.Classic,
    val targets: List<GameTarget> = emptyList(),
    val activeColor: ReflexTargetColor = ReflexTargetColor.Red,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val successfulHits: Int = 0,
    val totalAttempts: Int = 0,
    val dailyChallengeState: DailyChallengeState = DailyChallengeState.default(),
    val isPaused: Boolean = false,
    val isResumeGracePeriod: Boolean = false,
    val isGameOver: Boolean = false,
    val gameOverReason: String? = null,
    val hasUsedRewardContinue: Boolean = false,
    val isRewardContinueReady: Boolean = false,
    val canContinueWithReward: Boolean = false,
    val shouldRequestInterstitialAd: Boolean = false
)
