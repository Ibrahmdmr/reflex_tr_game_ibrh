package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun AchievementsTabContent(
    progressionState: ProgressionState,
    onAchievementClaim: (String) -> Unit
) {
    val unlockedCount = progressionState.achievements.count { it.unlocked }
    Text(
        text = stringResource(R.string.achievements_title),
        style = MaterialTheme.typography.titleLarge,
        color = ReflexGamePalette.textPrimary,
        textAlign = TextAlign.Center
    )
    AchievementCounterCard(achievements = progressionState.achievements)
    AchievementCategory.entries.forEach { category ->
        val categoryAchievements = progressionState.achievements
            .filter { it.category == category }
            .let(::sortedAchievementsForDisplay)
        if (categoryAchievements.isNotEmpty()) {
            AchievementCategorySection(
                category = category,
                achievements = categoryAchievements,
                unlockedIds = progressionState.latestUnlockedAchievementIds,
                onClaimClick = onAchievementClaim
            )
        }
    }
    if (progressionState.achievements.isEmpty()) {
        Text(
            text = stringResource(R.string.achievements_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = ReflexGamePalette.textSecondary,
            textAlign = TextAlign.Center
        )
    } else {
        Text(
            text = stringResource(R.string.achievement_summary_value, unlockedCount, progressionState.achievements.size),
            style = MaterialTheme.typography.labelMedium,
            color = ReflexGamePalette.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
internal fun AchievementSection(
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
        if (achievements.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ReflexGamePalette.cardGlassStrong,
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.28f))
            ) {
                Text(
                    text = stringResource(R.string.achievements_empty),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textPrimary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            sortedAchievementsForDisplay(achievements).forEach { achievement ->
                AchievementCard(
                    achievement = achievement,
                    highlighted = achievement.id in unlockedIds,
                    onClaimClick = { onClaimClick(achievement.id) }
                )
            }
        }
    }
}

@Composable
internal fun AchievementCategorySection(
    category: AchievementCategory,
    achievements: List<AchievementState>,
    unlockedIds: List<String>,
    onClaimClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(category.titleRes),
            style = MaterialTheme.typography.titleSmall,
            color = ArcadeGold
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
internal fun AchievementCard(
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
                        modifier = Modifier.width(132.dp)
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
                    R.string.achievement_progress_percent_value,
                    achievement.progressPercent,
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

internal fun sortedAchievementsForDisplay(
    achievements: List<AchievementState>
): List<AchievementState> {
    return achievements.sortedWith(
        compareBy<AchievementState> {
            when {
                it.unlocked && !it.claimed -> 0
                !it.claimed -> 1
                else -> 2
            }
        }.thenBy { it.category.ordinal }
            .thenBy { it.target }
            .thenBy { it.id }
    )
}


