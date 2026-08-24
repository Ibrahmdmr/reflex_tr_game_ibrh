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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

/** Rendered only while [StarterJourneyState.isActive]. */
@Composable
internal fun StarterJourneyCard(
    state: StarterJourneyState,
    onClaimClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val day = state.activeDay ?: return
    val ready = state.hasClaimableReward
    val accent = if (ready) ArcadeGold else ArcadeTeal
    LaunchedEffect(day) {
        logStarterJourneyEvent(FirebaseEvent.StarterJourneyViewed, day = day)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.40f))
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
                    text = stringResource(R.string.starter_journey_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (ready) {
                        stringResource(R.string.starter_all_tasks_done)
                    } else {
                        stringResource(
                            R.string.starter_task_progress_value,
                            state.completedTaskCount(day),
                            day.tasks.size
                        )
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(day.titleRes),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // One next step, never the whole checklist: crowding is what this card prevents.
            Text(
                text = day.tasks.firstOrNull { !state.isTaskCompleted(it) }
                    ?.let { stringResource(it.titleRes) }
                    ?: stringResource(R.string.starter_reward_ready),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            QuestProgressBar(percent = state.dayProgressPercent(day), accent = accent)
            Text(
                text = starterRewardText(day),
                style = MaterialTheme.typography.labelMedium,
                color = ArcadeGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            SecondaryGameButton(
                text = stringResource(R.string.starter_claim),
                onClick = onClaimClick,
                enabled = ready,
                modifier = Modifier.height(46.dp)
            )
        }
    }
}

@Composable
private fun starterRewardText(day: StarterJourneyDay): String {
    val coins = stringResource(R.string.starter_reward_value, day.rewardCoins)
    val chest = day.rewardChest ?: return coins
    return "$coins · ${stringResource(chest.titleRes)}"
}

@Composable
internal fun StarterJourneyCompletedNote(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.starter_journey_completed),
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelMedium,
        color = ArcadeTeal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
internal fun StarterJourneyGameOverNote(
    state: StarterJourneyState,
    taskCompletedThisGame: Boolean,
    modifier: Modifier = Modifier
) {
    if (!state.isActive) return
    val messageRes = when {
        state.hasClaimableReward -> R.string.starter_reward_ready
        taskCompletedThisGame -> R.string.starter_game_over_task
        else -> return
    }
    Text(
        text = stringResource(messageRes),
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelMedium,
        color = ArcadeGold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
