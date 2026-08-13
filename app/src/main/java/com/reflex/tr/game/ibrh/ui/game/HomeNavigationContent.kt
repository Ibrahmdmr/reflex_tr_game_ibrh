package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun HomeBottomNavigation(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    val bottomTabs = HomeTab.entries.filter { it.showInBottomNav }
    val selectedBottomTab = if (selectedTab.showInBottomNav) selectedTab else HomeTab.Profile
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.065f),
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            bottomTabs.forEach { tab ->
                val selected = tab == selectedBottomTab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) },
                    color = if (selected) ArcadeBlue.copy(alpha = 0.20f) else Color.Transparent,
                    shape = RoundedCornerShape(PremiumCompactRadius),
                    border = if (selected) {
                        BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.48f))
                    } else {
                        null
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = tab.icon,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary
                        )
                        Text(
                            text = stringResource(tab.titleRes),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(start = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SoundToggleButton(
    isSoundEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isSoundEnabled) ArcadeTeal else ReflexGamePalette.textSecondary
    val scale by animateFloatAsState(
        targetValue = if (isSoundEnabled) 1f else 0.94f,
        animationSpec = tween(durationMillis = 160),
        label = "sound_toggle_scale"
    )
    val contentDescription = if (isSoundEnabled) {
        stringResource(R.string.sound_on)
    } else {
        stringResource(R.string.sound_off)
    }

    Surface(
        modifier = modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (isSoundEnabled) 10f else 2f
            }
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        color = accentColor.copy(alpha = if (isSoundEnabled) 0.18f else 0.1f),
        shape = CircleShape,
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (isSoundEnabled) 0.52f else 0.26f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(
                    if (isSoundEnabled) {
                        R.drawable.ic_volume_up_24
                    } else {
                        R.drawable.ic_volume_off_24
                    }
                ),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun HowToPlayEntryCard(
    onClick: () -> Unit
) {
    val interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(ArcadeGold)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.how_to_play_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.how_to_play_home_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textSecondary
                )
            }
            Text(
                text = stringResource(R.string.open_details),
                style = MaterialTheme.typography.labelMedium,
                color = ArcadeGold
            )
        }
    }
}

@Composable
internal fun GameModeSection(
    bestScoresByMode: Map<GameMode, Int>,
    modeMasteryXpByMode: Map<GameMode, Int>,
    selectedMode: GameMode,
    onModeStartClick: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.game_modes_title),
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
        GameMode.entries.forEach { mode ->
            GameModeCard(
                mode = mode,
                bestScore = bestScoresByMode[mode] ?: 0,
                masteryProgress = modeMasteryProgressFor(modeMasteryXpByMode, mode),
                selected = mode == selectedMode,
                onClick = { onModeStartClick(mode) }
            )
        }
    }
}

@Composable
internal fun GameModeCard(
    mode: GameMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bestScore: Int? = null,
    masteryProgress: ModeMasteryProgress? = null,
    selected: Boolean = false
) {
    val accentColor = modeAccentColor(mode)
    val interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.01f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "mode_card_selected_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (selected) 14f else 4f
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = if (selected) accentColor.copy(alpha = 0.18f) else ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (selected) 0.62f else 0.28f))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = modeIcon(mode),
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(mode.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(mode.descriptionRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ReflexGamePalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    color = accentColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(PremiumPillRadius)
                ) {
                    Text(
                        text = stringResource(mode.difficultyRes),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ReflexGamePalette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (bestScore != null) {
                Column(
                    modifier = Modifier.padding(start = 36.dp, end = 12.dp, bottom = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = if (masteryProgress != null) {
                            stringResource(
                                R.string.mode_card_best_mastery_value,
                                bestScore,
                                masteryProgress.level
                            )
                        } else {
                            stringResource(R.string.mode_best_score_value, bestScore)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = ReflexGamePalette.textSecondary
                    )
                    if (masteryProgress != null) {
                        LinearProgressIndicator(
                            progress = { masteryProgress.progressFraction },
                            modifier = Modifier.fillMaxWidth(),
                            color = accentColor,
                            trackColor = ReflexGamePalette.textPrimary.copy(alpha = 0.08f)
                        )
                    }
                }
            }
        }
    }
}
