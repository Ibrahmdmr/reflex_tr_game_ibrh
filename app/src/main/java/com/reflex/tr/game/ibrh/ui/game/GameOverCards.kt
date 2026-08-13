package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.PremiumSurfaceCard
import com.reflex.tr.game.ibrh.ui.game.components.SectionTitle
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun UnlockedProfileBadgesCard(
    badges: Set<ProfileBadge>,
    modifier: Modifier = Modifier
) {
    PremiumSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = ArcadeTeal,
        containerColor = ArcadeTeal.copy(alpha = 0.11f)
    ) {
            SectionTitle(
                text = stringResource(R.string.badge_unlocked_label),
                accentColor = ArcadeTeal
            )
            badges.forEach { badge ->
                Text(
                    text = stringResource(badge.titleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
    }
}

@Composable
internal fun NewPersonalRecordsCard(
    records: Set<PersonalRecordType>,
    modifier: Modifier = Modifier
) {
    PremiumSurfaceCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = ArcadeGold,
        containerColor = ArcadeGold.copy(alpha = 0.11f)
    ) {
            SectionTitle(
                text = stringResource(R.string.personal_record_new_label),
                accentColor = ArcadeGold
            )
            records.forEach { record ->
                Text(
                    text = stringResource(record.titleRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
    }
}

@Composable
internal fun ComboChallengeProgressCard(
    state: ComboChallengeState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ArcadeBlue.copy(alpha = 0.10f),
        shape = RoundedCornerShape(PremiumChipRadius)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.combo_challenge_title),
                style = MaterialTheme.typography.labelLarge,
                color = ArcadeGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    R.string.combo_challenge_game_over_progress,
                    state.progress,
                    state.target
                ),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun DailyMiniTournamentGameOverCard(
    state: DailyMiniTournamentState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (state.completed) ArcadeTeal.copy(alpha = 0.12f) else ArcadeGold.copy(alpha = 0.10f),
        shape = RoundedCornerShape(PremiumChipRadius),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (state.completed) ArcadeTeal.copy(alpha = 0.34f) else ArcadeGold.copy(alpha = 0.28f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.daily_mini_tournament_title),
                style = MaterialTheme.typography.labelLarge,
                color = if (state.completed) ArcadeTeal else ArcadeGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (state.completed) {
                    if (state.rewardClaimedThisGame) {
                        stringResource(R.string.daily_mini_tournament_completed_reward, state.rewardCoins)
                    } else {
                        stringResource(R.string.daily_mini_tournament_claimed)
                    }
                } else {
                    stringResource(R.string.daily_mini_tournament_game_over_remaining, state.bestScore, state.remainingScore)
                },
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun SmartGameSuggestionCard(
    @StringRes suggestionRes: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ArcadeTeal.copy(alpha = 0.10f),
        shape = RoundedCornerShape(PremiumChipRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.smart_suggestion_title),
                style = MaterialTheme.typography.labelLarge,
                color = ArcadeGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(suggestionRes),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@StringRes
internal fun smartGameSuggestionRes(
    score: Int,
    bestScore: Int,
    isNewBestScore: Boolean,
    maxCombo: Int,
    accuracyPercent: Int
): Int {
    val safeScore = score.coerceAtLeast(0)
    val safeCombo = maxCombo.coerceAtLeast(0)
    return when {
        isNearRecordScore(
            score = safeScore,
            bestScore = bestScore,
            isNewBestScore = isNewBestScore
        ) -> R.string.smart_suggestion_near_record
        safeScore <= 5 && safeCombo <= 1 -> R.string.smart_suggestion_fast_loss
        accuracyPercent.coerceIn(0, 100) < 55 -> R.string.smart_suggestion_low_accuracy
        safeCombo < 3 -> R.string.smart_suggestion_low_combo
        else -> R.string.smart_suggestion_keep_going
    }
}

@Composable
internal fun bonusIncludedText(
    earnedCoins: Int,
    baseCoins: Int
): String? {
    val bonusCoins = (earnedCoins - baseCoins).coerceAtLeast(0)
    return if (bonusCoins > 0) stringResource(R.string.coin_bonus_included, bonusCoins) else null
}

@Composable
internal fun CoinEarnedCard(
    earnedCoins: Int,
    baseCoins: Int,
    totalCoins: Int,
    isCoinDoubleClaimed: Boolean,
    bonusText: String?
) {
    val coinScale by animateFloatAsState(
        targetValue = if (earnedCoins > baseCoins || isCoinDoubleClaimed) 1.05f else 1f,
        animationSpec = tween(durationMillis = 260),
        label = "coin_earned_scale"
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = coinScale
                scaleY = coinScale
            },
        shape = RoundedCornerShape(PremiumCardRadius),
        color = ArcadeGold.copy(alpha = 0.14f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(ArcadeGold.copy(alpha = 0.24f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "◉", color = ArcadeGold, style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.coins_earned_value, earnedCoins),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary
                )
                val detailText = listOfNotNull(
                    bonusText,
                    stringResource(R.string.coin_wallet_value, totalCoins)
                ).joinToString(" • ")
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (bonusText == null) ReflexGamePalette.textSecondary else ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
