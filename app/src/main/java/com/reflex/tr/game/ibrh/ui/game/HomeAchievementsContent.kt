package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.BuildConfig
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.RewardedAdUiState
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import java.util.Locale
import kotlinx.coroutines.delay
import com.reflex.tr.game.ibrh.firebase.FirebaseEvent
import com.reflex.tr.game.ibrh.firebase.FirebaseGameServices
import com.reflex.tr.game.ibrh.firebase.FirebaseParam

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


