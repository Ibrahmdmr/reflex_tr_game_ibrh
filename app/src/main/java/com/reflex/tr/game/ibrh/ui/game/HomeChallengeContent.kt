package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun DailyChallengeCard(
    state: DailyChallengeState,
    rewardedAdUiState: RewardedAdUiState = RewardedAdUiState(),
    onClaimClick: () -> Unit = {},
    onDoubleRewardClick: () -> Unit = {},
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
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                            if (state.rewardClaimed) {
                                stringResource(R.string.mission_reward_daily_claimed, state.rewardCoins)
                            } else {
                                stringResource(R.string.daily_challenge_completed_description)
                            }
                        } else {
                            stringResource(state.type.descriptionRes)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
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
            Text(
                text = stringResource(R.string.mission_reward_ready_value, state.rewardCoins),
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (state.completed && !state.rewardClaimed) {
                SecondaryGameButton(
                    text = stringResource(R.string.claim_reward),
                    onClick = onClaimClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (state.completed && state.rewardClaimed && !state.doubleRewardClaimed) {
                SecondaryGameButton(
                    text = when {
                        rewardedAdUiState.isReady -> stringResource(R.string.daily_challenge_double_reward)
                        rewardedAdUiState.isLoading || rewardedAdUiState.isShowing -> stringResource(R.string.rewarded_loading)
                        else -> stringResource(R.string.rewarded_not_ready)
                    },
                    enabled = rewardedAdUiState.isReady && !rewardedAdUiState.isShowing,
                    onClick = onDoubleRewardClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
internal fun WeeklyChallengeCard(
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
