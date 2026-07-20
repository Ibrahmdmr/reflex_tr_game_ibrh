package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun DailyStreakMiniCard(
    state: DailyRewardState,
    onClick: () -> Unit,
    onProtectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = when {
        state.isStreakAtRisk -> ArcadeCoral
        state.claimedToday -> ArcadeTeal
        else -> ArcadeGold
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
internal fun DailyRewardCard(
    state: DailyRewardState,
    onClaimClick: () -> Unit,
    onProtectClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = when {
        state.isStreakAtRisk -> ArcadeCoral
        state.canClaim -> ArcadeGold
        else -> ArcadeTeal
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
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
                    Text(
                        text = stringResource(R.string.daily_reward_next_value, state.nextRewardCoins),
                        style = MaterialTheme.typography.bodySmall,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
internal fun DailyRewardProgressLine(
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
internal fun DailyRewardPopup(
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
        state.claimedToday -> localizedHomeStringResource(R.string.daily_reward_claimed_today, selectedLanguage)
        else -> localizedHomeStringResource(
            id = R.string.daily_reward_popup_message,
            selectedLanguage = selectedLanguage,
            state.rewardCoins
        )
    }
    val claimText = when {
        state.isStreakAtRisk -> localizedHomeStringResource(R.string.daily_reward_protect_button, selectedLanguage)
        state.isSuperReward -> localizedHomeStringResource(R.string.daily_reward_super_claim, selectedLanguage)
        state.claimedToday -> localizedHomeStringResource(R.string.daily_reward_continue, selectedLanguage)
        else -> localizedHomeStringResource(R.string.daily_reward_continue, selectedLanguage)
    }

    PolishedGameDialog(
        onDismissRequest = onDismiss,
        title = title,
        confirmButton = {
            PrimaryGameButton(
                text = claimText,
                onClick = when {
                    state.isStreakAtRisk -> onProtectClick
                    state.canClaim -> onClaimClick
                    else -> onDismiss
                }
            )
        }
    ) {
        Text(
            text = message,
            modifier = Modifier.fillMaxWidth(),
            color = ReflexGamePalette.textSecondary,
            textAlign = TextAlign.Center
        )
        DailyRewardProgressLine(state = state)
    }
}

@Composable
internal fun dailyRewardText(state: DailyRewardState): String {
    return when (state.rewardType) {
        DailyRewardType.Coins -> stringResource(R.string.daily_reward_coin_value, state.rewardCoins)
        DailyRewardType.SuperBox -> stringResource(R.string.daily_reward_super_value, state.rewardCoins)
    }
}
