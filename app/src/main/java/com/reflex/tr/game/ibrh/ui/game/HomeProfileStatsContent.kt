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
internal fun HomeQuickStats(
    bestScore: Int,
    progressionState: ProgressionState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickStatCard(
            title = stringResource(R.string.coin_wallet_title),
            value = stringResource(R.string.coin_wallet_value, progressionState.coins),
            accent = ArcadeGold,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = stringResource(R.string.profile_title),
            value = stringResource(R.string.level_value, progressionState.level),
            accent = ArcadeTeal,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = stringResource(R.string.best_score_label),
            value = bestScore.toString(),
            accent = ArcadeBlue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun HomeLevelProgressCard(
    progressionState: ProgressionState,
    modifier: Modifier = Modifier
) {
    val currentLevelXp = (progressionState.level - 1) * XP_PER_LEVEL
    val nextLevelXp = progressionState.level * XP_PER_LEVEL
    val levelProgress = ((progressionState.xp - currentLevelXp).toFloat() / XP_PER_LEVEL)
        .coerceIn(0f, 1f)
    val remainingXp = (nextLevelXp - progressionState.xp).coerceAtLeast(0)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ArcadeTeal.copy(alpha = 0.11f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_level_progress_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.level_value, progressionState.level),
                    style = MaterialTheme.typography.labelMedium,
                    color = ArcadeTeal
                )
            }
            LinearProgressIndicator(
                progress = { levelProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = ArcadeTeal,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Text(
                text = stringResource(R.string.xp_to_next_level_value, remainingXp),
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary
            )
        }
    }
}

@Composable
internal fun AchievementCounterCard(
    achievements: List<AchievementState>,
    modifier: Modifier = Modifier
) {
    val unlockedCount = achievements.count { it.unlocked }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ArcadeGold.copy(alpha = 0.10f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.28f))
    ) {
        Text(
            text = stringResource(R.string.home_achievement_counter, unlockedCount, achievements.size),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = ReflexGamePalette.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun QuickStatCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = ReflexGamePalette.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun AchievementSummaryCard(
    achievements: List<AchievementState>
) {
    val unlockedCount = achievements.count { it.unlocked }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ArcadeTeal.copy(alpha = 0.12f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "★",
                style = MaterialTheme.typography.titleMedium,
                color = ArcadeGold
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.achievements_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.achievement_summary_value, unlockedCount, achievements.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
        }
    }
}
