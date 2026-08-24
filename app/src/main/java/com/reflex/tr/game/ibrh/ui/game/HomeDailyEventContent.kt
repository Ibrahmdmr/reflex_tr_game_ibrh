package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun DailyEventCard(
    state: DailyEventState,
    onPlayClick: () -> Unit,
    onClaimClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (state.completed) ArcadeTeal else ArcadeGold
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(PremiumCardPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.daily_event_section_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.daily_event_reward_value, state.rewardCoins),
                    style = MaterialTheme.typography.labelMedium,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(state.type.titleRes),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(state.type.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            DailyEventProgressBar(state = state, accent = accent)
            SecondaryGameButton(
                text = stringResource(state.actionTextRes),
                onClick = if (state.canClaim) onClaimClick else onPlayClick,
                enabled = !state.claimed,
                modifier = Modifier.height(46.dp)
            )
        }
    }
}

@Composable
internal fun DailyEventDetailSection(
    state: DailyEventState,
    remainingMinutes: Int,
    onPlayClick: () -> Unit,
    onClaimClick: () -> Unit,
    onViewed: () -> Unit
) {
    // Reported from the detail view only, so opening the rewards tab counts once per event
    // rather than every time the home card scrolls past.
    LaunchedEffect(state.type) {
        logDailyEventEvent(FirebaseEvent.DailyEventViewed, state)
        onViewed()
    }
    val accent = if (state.completed) ArcadeTeal else ArcadeGold
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DailyEventCard(
            state = state,
            onPlayClick = onPlayClick,
            onClaimClick = onClaimClick
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(state.statusTextRes),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    R.string.daily_event_time_left,
                    remainingMinutes / 60,
                    remainingMinutes % 60
                ),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DailyEventProgressBar(state: DailyEventState, accent: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        QuestProgressBar(percent = state.progressPercent, accent = accent)
        Text(
            text = stringResource(
                R.string.daily_event_progress_value,
                state.progress.coerceIn(0, state.target),
                state.target
            ),
            style = MaterialTheme.typography.labelSmall,
            color = ReflexGamePalette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private val DailyEventState.actionTextRes: Int
    get() = when {
        claimed -> R.string.daily_event_claimed
        canClaim -> R.string.daily_event_claim
        else -> R.string.daily_event_play
    }

private val DailyEventState.statusTextRes: Int
    get() = when {
        claimed -> R.string.daily_event_claimed
        completed -> R.string.daily_event_completed_status
        else -> R.string.daily_event_in_progress
    }
