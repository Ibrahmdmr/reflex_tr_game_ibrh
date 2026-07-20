package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
internal fun SeasonMiniCard(
    season: SeasonState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.season_card_title, season.seasonNumber, season.level),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.season_days_left, season.remainingDays),
                    style = MaterialTheme.typography.labelMedium,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (season.isXpBoostActive) {
                Text(
                    text = stringResource(R.string.season_xp_boost_active_short),
                    style = MaterialTheme.typography.labelMedium,
                    color = ArcadeTeal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LinearProgressIndicator(
                progress = { season.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = ArcadeGold,
                trackColor = Color.White.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
internal fun SeasonTabContent(
    season: SeasonState,
    rewardedAdUiState: RewardedAdUiState,
    onClaimClick: (Int) -> Unit,
    onBoostClick: () -> Unit,
    onMissionClaim: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.season_title),
        style = MaterialTheme.typography.titleLarge,
        color = ReflexGamePalette.textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.season_card_title, season.seasonNumber, season.level),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.season_days_left, season.remainingDays),
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcadeGold
                )
            }
            LinearProgressIndicator(
                progress = { season.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = ArcadeGold,
                trackColor = Color.White.copy(alpha = 0.12f)
            )
            Text(
                text = stringResource(R.string.season_next_reward, stringResource(season.nextReward.kind.titleRes)),
                style = MaterialTheme.typography.bodyMedium,
                color = ReflexGamePalette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    SeasonXpBoostCard(
        season = season,
        rewardedAdUiState = rewardedAdUiState,
        onBoostClick = onBoostClick
    )
    SeasonMissionSection(
        missions = season.missions,
        onMissionClaim = onMissionClaim
    )
    season.rewards.forEach { reward ->
        SeasonRewardCard(
            reward = reward,
            unlocked = reward.level <= season.level,
            onClaimClick = onClaimClick
        )
    }
}

@Composable
internal fun SeasonXpBoostCard(
    season: SeasonState,
    rewardedAdUiState: RewardedAdUiState,
    onBoostClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.38f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.season_xp_boost_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (season.isXpBoostActive) {
                            stringResource(R.string.season_xp_boost_active, season.xpBoostRemainingMinutes)
                        } else {
                            stringResource(R.string.season_xp_boost_description, SeasonXpBoostBonusPercent)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = stringResource(R.string.season_xp_boost_bonus, SeasonXpBoostBonusPercent),
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcadeTeal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SecondaryGameButton(
                text = when {
                    rewardedAdUiState.isShowing || rewardedAdUiState.isLoading -> stringResource(R.string.rewarded_loading)
                    season.isXpBoostActive -> stringResource(R.string.season_xp_boost_refresh)
                    else -> stringResource(R.string.season_xp_boost_watch_ad)
                },
                enabled = rewardedAdUiState.isReady && !rewardedAdUiState.isShowing && !rewardedAdUiState.isLoading,
                isLoading = rewardedAdUiState.isShowing || rewardedAdUiState.isLoading,
                onClick = onBoostClick
            )
        }
    }
}

@Composable
internal fun SeasonMissionSection(
    missions: List<SeasonMissionState>,
    onMissionClaim: (String) -> Unit
) {
    Text(
        text = stringResource(R.string.season_missions_title),
        style = MaterialTheme.typography.titleMedium,
        color = ReflexGamePalette.textPrimary,
        modifier = Modifier.fillMaxWidth()
    )
    missions.forEach { mission ->
        SeasonMissionCard(
            mission = mission,
            onClaimClick = { onMissionClaim(mission.id) }
        )
    }
}

@Composable
internal fun SeasonMissionCard(
    mission: SeasonMissionState,
    onClaimClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.07f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (mission.completed && !mission.claimed) ArcadeGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(mission.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(mission.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = stringResource(R.string.season_mission_reward_value, mission.rewardSeasonXp),
                    style = MaterialTheme.typography.labelMedium,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            LinearProgressIndicator(
                progress = { mission.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = if (mission.completed) ArcadeGold else ArcadeBlue,
                trackColor = Color.White.copy(alpha = 0.12f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.season_mission_progress,
                        mission.progress.coerceAtMost(mission.target),
                        mission.target
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SecondaryGameButton(
                    text = when {
                        mission.claimed -> stringResource(R.string.claimed)
                        mission.completed -> stringResource(R.string.claim_reward)
                        else -> stringResource(R.string.season_mission_in_progress)
                    },
                    enabled = mission.completed && !mission.claimed,
                    onClick = onClaimClick,
                    modifier = Modifier.width(132.dp)
                )
            }
        }
    }
}

@Composable
internal fun SeasonRewardCard(
    reward: SeasonRewardState,
    unlocked: Boolean,
    onClaimClick: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (reward.premium) ArcadeGold.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.07f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (reward.premium) ArcadeGold.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reward.level.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (reward.premium) ArcadeGold else ReflexGamePalette.textPrimary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(reward.kind.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.season_reward_coin_value, reward.coinReward),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SecondaryGameButton(
                text = when {
                    reward.claimed -> stringResource(R.string.claimed)
                    unlocked -> stringResource(R.string.claim_reward)
                    else -> stringResource(R.string.season_locked)
                },
                enabled = unlocked && !reward.claimed,
                onClick = { onClaimClick(reward.level) },
                modifier = Modifier.width(120.dp)
            )
        }
    }
}


