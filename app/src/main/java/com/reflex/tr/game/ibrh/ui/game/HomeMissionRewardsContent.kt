package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun MissionRewardsCard(
    dailyChallengeState: DailyChallengeState,
    weeklyChallengeState: ChallengeState,
    comboChallengeState: ComboChallengeState,
    achievements: List<AchievementState>,
    rewardedAdUiState: RewardedAdUiState,
    onDailyChallengeClaim: () -> Unit,
    onComboChallengeClaim: () -> Unit,
    onWeeklyChallengeClaim: () -> Unit,
    onDailyChallengeDoubleRewardClick: () -> Unit,
    onAchievementClaim: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedAchievements = achievements
        .filter { it.unlocked }
        .sortedWith(compareBy<AchievementState> { it.claimed }.thenBy { it.id })
    val hasCompletedReward = dailyChallengeState.completed ||
        weeklyChallengeState.completed ||
        comboChallengeState.completed ||
        completedAchievements.isNotEmpty()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ArcadeTeal.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.titleMedium,
                        color = ArcadeTeal
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.mission_rewards_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.mission_rewards_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            MissionRewardRow(
                icon = if (dailyChallengeState.completed) "✓" else "!",
                title = stringResource(R.string.daily_challenge_title),
                detail = when {
                    dailyChallengeState.doubleRewardClaimed -> stringResource(R.string.mission_reward_daily_doubled)
                    dailyChallengeState.rewardClaimed -> stringResource(
                        R.string.mission_reward_daily_claimed,
                        dailyChallengeState.rewardCoins
                    )
                    else -> stringResource(
                        R.string.daily_challenge_progress,
                        dailyChallengeState.progress,
                        dailyChallengeState.target
                    )
                },
                accent = if (dailyChallengeState.completed) ArcadeTeal else ArcadeGold
            ) {
                if (dailyChallengeState.completed && !dailyChallengeState.rewardClaimed) {
                    SecondaryGameButton(
                        text = stringResource(R.string.claim_reward),
                        onClick = onDailyChallengeClaim,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (dailyChallengeState.completed && dailyChallengeState.rewardClaimed && !dailyChallengeState.doubleRewardClaimed) {
                    SecondaryGameButton(
                        text = when {
                            rewardedAdUiState.isReady -> stringResource(R.string.daily_challenge_double_reward)
                            rewardedAdUiState.isLoading || rewardedAdUiState.isShowing -> stringResource(R.string.rewarded_loading)
                            else -> stringResource(R.string.rewarded_not_ready)
                        },
                        enabled = rewardedAdUiState.isReady && !rewardedAdUiState.isShowing,
                        onClick = onDailyChallengeDoubleRewardClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            MissionRewardRow(
                icon = if (weeklyChallengeState.completed) "✓" else "#",
                title = stringResource(R.string.weekly_challenge_title),
                detail = when {
                    weeklyChallengeState.claimed -> stringResource(R.string.weekly_challenge_claimed)
                    weeklyChallengeState.completed ->
                        stringResource(R.string.mission_reward_weekly_completed, weeklyChallengeState.rewardCoins)
                    else -> stringResource(
                        R.string.weekly_challenge_progress,
                        weeklyChallengeState.progress,
                        weeklyChallengeState.target,
                        weeklyChallengeState.rewardCoins
                    )
                },
                accent = if (weeklyChallengeState.completed) ArcadeTeal else ArcadeBlue
            ) {
                if (weeklyChallengeState.completed && !weeklyChallengeState.claimed) {
                    SecondaryGameButton(
                        text = stringResource(R.string.claim_reward),
                        onClick = onWeeklyChallengeClaim,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            MissionRewardRow(
                icon = if (comboChallengeState.completed) "✓" else "x",
                title = stringResource(R.string.combo_challenge_title),
                detail = when {
                    comboChallengeState.claimed -> stringResource(R.string.combo_challenge_claimed)
                    comboChallengeState.completed ->
                        stringResource(R.string.mission_reward_ready_value, comboChallengeState.rewardCoins)
                    else -> stringResource(
                        R.string.combo_challenge_progress,
                        comboChallengeState.progress,
                        comboChallengeState.target,
                        comboChallengeState.rewardCoins
                    )
                },
                accent = if (comboChallengeState.completed) ArcadeTeal else ArcadeGold
            ) {
                if (comboChallengeState.completed && !comboChallengeState.claimed) {
                    SecondaryGameButton(
                        text = stringResource(R.string.claim_reward),
                        onClick = onComboChallengeClaim,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            completedAchievements.forEach { achievement ->
                MissionRewardRow(
                    icon = if (achievement.claimed) "✓" else "★",
                    title = stringResource(achievement.titleRes),
                    detail = if (achievement.claimed) {
                        stringResource(R.string.mission_reward_claimed_value, achievement.rewardCoins)
                    } else {
                        stringResource(R.string.mission_reward_ready_value, achievement.rewardCoins)
                    },
                    accent = if (achievement.claimed) ArcadeTeal else ArcadeGold
                ) {
                    if (!achievement.claimed) {
                        SecondaryGameButton(
                            text = stringResource(R.string.claim_reward),
                            onClick = { onAchievementClaim(achievement.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (!hasCompletedReward) {
                Text(
                    text = stringResource(R.string.mission_rewards_empty),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
internal fun MissionRewardRow(
    icon: String,
    title: String,
    detail: String,
    accent: Color,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        action?.invoke()
    }
}
