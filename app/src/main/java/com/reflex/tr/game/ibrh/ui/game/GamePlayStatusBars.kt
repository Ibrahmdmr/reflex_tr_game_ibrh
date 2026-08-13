package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun ColorTaskBadge(activeColor: ReflexTargetColor) {
    val color = activeColor.toTaskColor()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .graphicsLayer {
                shadowElevation = 12f
            },
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(PremiumChipRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.68f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(color, RoundedCornerShape(PremiumPillRadius))
                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(PremiumPillRadius))
            )
            Text(
                text = stringResource(
                    R.string.mode_color_task_badge,
                    stringResource(activeColor.labelRes).uppercase()
                ),
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun ComboStatusBar(combo: Int) {
    val tier = comboTierFor(combo)
    val accent = when {
        combo >= 20 -> Color(0xFFFF4FD8)
        combo >= 10 -> ArcadeGold
        combo >= 5 -> ReflexGamePalette.targetRing
        else -> ArcadeBlue
    }
    val transition = rememberInfiniteTransition(label = "combo_status_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "combo_status_scale"
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (combo >= 5) pulse else 1f
                scaleY = if (combo >= 5) pulse else 1f
                shadowElevation = 12f + tier.glowBoost * 22f
            },
        color = accent.copy(alpha = 0.14f + tier.glowBoost * 0.12f),
        shape = RoundedCornerShape(PremiumCardRadius),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.36f + tier.glowBoost * 0.34f))
    ) {
        Text(
            text = if (tier.labelRes == null) {
                stringResource(R.string.combo_value, combo)
            } else {
                "${stringResource(R.string.combo_value, combo)}  •  ${stringResource(tier.labelRes)}"
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            color = ReflexGamePalette.textPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun BossRoundStatusBar(
    isActive: Boolean,
    timeLeftSeconds: Int,
    hits: Int,
    bonusCoins: Int
) {
    val transition = rememberInfiniteTransition(label = "boss_round_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "boss_round_scale"
    )
    val title = if (isActive) {
        stringResource(R.string.boss_round_started)
    } else {
        stringResource(R.string.boss_round_result, hits.coerceAtLeast(0))
    }
    val subtitle = if (isActive) {
        stringResource(
            R.string.boss_round_active_summary,
            timeLeftSeconds.coerceAtLeast(0),
            hits.coerceAtLeast(0),
            bonusCoins.coerceAtLeast(0)
        )
    } else {
        stringResource(R.string.boss_round_bonus_value, bonusCoins.coerceAtLeast(0))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (isActive) pulse else 1f
                scaleY = if (isActive) pulse else 1f
                shadowElevation = if (isActive) 18f else 8f
            },
        color = ArcadeCoral.copy(alpha = if (isActive) 0.15f else 0.1f),
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = if (isActive) 0.46f else 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = ArcadeGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun UltraMomentStatusBar(
    isActive: Boolean,
    timeLeftSeconds: Int,
    hits: Int,
    bonusCoins: Int
) {
    val transition = rememberInfiniteTransition(label = "ultra_moment_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 560, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ultra_moment_scale"
    )
    val title = if (isActive) {
        stringResource(R.string.ultra_moment_started)
    } else {
        stringResource(R.string.ultra_moment_result, hits.coerceAtLeast(0))
    }
    val subtitle = if (isActive) {
        stringResource(
            R.string.ultra_moment_active_summary,
            timeLeftSeconds.coerceAtLeast(0),
            hits.coerceAtLeast(0),
            bonusCoins.coerceAtLeast(0)
        )
    } else {
        stringResource(R.string.ultra_moment_bonus_value, bonusCoins.coerceAtLeast(0))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (isActive) pulse else 1f
                scaleY = if (isActive) pulse else 1f
                shadowElevation = if (isActive) 18f else 8f
            },
        color = ArcadeTeal.copy(alpha = if (isActive) 0.15f else 0.1f),
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = if (isActive) 0.46f else 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = ArcadeTeal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
