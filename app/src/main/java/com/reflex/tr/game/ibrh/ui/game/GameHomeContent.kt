package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
fun HomeContent(
    bestScore: Int,
    bestScoresByMode: Map<GameMode, Int>,
    selectedMode: GameMode,
    dailyChallengeState: DailyChallengeState,
    isSoundEnabled: Boolean,
    selectedLanguage: AppLanguage,
    onStartClick: () -> Unit,
    onModeStartClick: (GameMode) -> Unit,
    onHowToPlayClick: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSoundToggleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompactHeight = maxHeight <= 780.dp
        val contentScrollState = rememberScrollState()
        val panelPadding = if (isCompactHeight) 14.dp else 18.dp
        val contentSpacing = if (isCompactHeight) 10.dp else 14.dp

        GamePanelCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight),
            contentPadding = panelPadding,
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(contentSpacing)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(contentScrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(contentSpacing)
                    ) {
                        GameLogo(isCompactHeight = isCompactHeight)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(40.dp))
                            Text(
                                text = stringResource(R.string.game_title),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.headlineMedium,
                                color = ReflexGamePalette.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            SoundToggleButton(
                                isSoundEnabled = isSoundEnabled,
                                onClick = onSoundToggleClick
                            )
                        }

                        Text(
                            text = if (isSoundEnabled) {
                                stringResource(R.string.sound_on)
                            } else {
                                stringResource(R.string.sound_off)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSoundEnabled) ArcadeTeal else ReflexGamePalette.textSecondary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = stringResource(R.string.game_tagline),
                            style = MaterialTheme.typography.bodyLarge,
                            color = ReflexGamePalette.textSecondary,
                            textAlign = TextAlign.Center
                        )

                        BestScoreHero(
                            bestScore = bestScore,
                            isCompactHeight = isCompactHeight
                        )

                        GameModeSection(
                            bestScoresByMode = bestScoresByMode,
                            selectedMode = selectedMode,
                            onModeStartClick = onModeStartClick
                        )

                        DailyChallengeCard(
                            state = dailyChallengeState
                        )

                        HowToPlayEntryCard(onClick = onHowToPlayClick)

                        LanguageSelectionSection(
                            selectedLanguage = selectedLanguage,
                            onLanguageSelected = onLanguageSelected
                        )
                    }

                    PrimaryGameButton(
                        text = stringResource(selectedMode.startButtonRes),
                        onClick = onStartClick
                    )
                }
            }
        )
    }
}

@Composable
private fun SoundToggleButton(
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
private fun HowToPlayEntryCard(
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
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
private fun GameModeSection(
    bestScoresByMode: Map<GameMode, Int>,
    selectedMode: GameMode,
    onModeStartClick: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
    bestScore: Int? = null,
    selected: Boolean = false,
    modifier: Modifier = Modifier
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
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (selected) 0.62f else 0.28f))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                        color = ReflexGamePalette.textPrimary
                    )
                    Text(
                        text = stringResource(mode.descriptionRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ReflexGamePalette.textSecondary
                    )
                }
                Surface(
                    color = accentColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = stringResource(mode.difficultyRes),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ReflexGamePalette.textPrimary
                    )
                }
            }
            if (bestScore != null) {
                Text(
                    text = stringResource(R.string.mode_best_score_value, bestScore),
                    modifier = Modifier.padding(start = 40.dp, end = 14.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun DailyChallengeCard(
    state: DailyChallengeState,
    modifier: Modifier = Modifier
) {
    val accent = if (state.completed) ArcadeTeal else ArcadeGold
    val scale by animateFloatAsState(
        targetValue = if (state.completed) 1.01f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "daily_challenge_complete_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (state.completed) 16f else 6f
            },
        color = if (state.completed) {
            ArcadeTeal.copy(alpha = 0.12f)
        } else {
            ReflexGamePalette.cardGlassStrong
        },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = if (state.completed) 0.42f else 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.completed) "✓" else "!",
                    style = MaterialTheme.typography.titleMedium,
                    color = accent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.daily_challenge_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = accent
                )
                Text(
                    text = if (state.completed) {
                        stringResource(R.string.daily_challenge_completed_title)
                    } else {
                        stringResource(state.type.titleRes)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = if (state.completed) {
                        stringResource(R.string.daily_challenge_completed_description)
                    } else {
                        stringResource(state.type.descriptionRes)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
            Surface(
                color = accent.copy(alpha = if (state.completed) 0.2f else 0.14f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, accent.copy(alpha = if (state.completed) 0.42f else 0.24f))
            ) {
                Text(
                    text = if (state.completed) {
                        stringResource(R.string.daily_challenge_completed_badge)
                    } else {
                        stringResource(R.string.daily_challenge_progress, state.progress, state.target)
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textPrimary
                )
            }
        }
    }
}

@Composable
private fun LanguageSelectionSection(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.language_selection_title),
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LanguageChip(
                text = stringResource(R.string.language_turkish),
                selected = selectedLanguage == AppLanguage.Turkish,
                onClick = { onLanguageSelected(AppLanguage.Turkish) },
                modifier = Modifier.weight(1f)
            )
            LanguageChip(
                text = stringResource(R.string.language_english),
                selected = selectedLanguage == AppLanguage.English,
                onClick = { onLanguageSelected(AppLanguage.English) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LanguageChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) ArcadeGold else ReflexGamePalette.neonBlue
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = color.copy(alpha = if (selected) 0.22f else 0.1f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, color.copy(alpha = if (selected) 0.56f else 0.24f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleSmall,
            color = ReflexGamePalette.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

internal fun modeAccentColor(mode: GameMode): Color {
    return when (mode) {
        GameMode.Classic -> ArcadeGold
        GameMode.MovingTarget -> ArcadeBlue
        GameMode.FakeTarget -> ReflexGamePalette.targetRing
        GameMode.ColorReflex -> ArcadeTeal
    }
}

internal fun modeIcon(mode: GameMode): String {
    return when (mode) {
        GameMode.Classic -> "◎"
        GameMode.MovingTarget -> "↗"
        GameMode.FakeTarget -> "◇"
        GameMode.ColorReflex -> "◆"
    }
}

@Composable
private fun GameLogo(isCompactHeight: Boolean) {
    val containerSize = if (isCompactHeight) 72.dp else 104.dp
    val iconSize = if (isCompactHeight) 58.dp else 82.dp
    val badgeOffsetX = if (isCompactHeight) 24.dp else 34.dp
    val badgeOffsetY = if (isCompactHeight) (-22).dp else (-32).dp
    val badgeSize = if (isCompactHeight) 14.dp else 16.dp

    Box(
        modifier = Modifier.size(containerSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ReflexGamePalette.targetRing.copy(alpha = 0.34f),
                            ReflexGamePalette.neonPurple.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
        Image(
            painter = painterResource(R.drawable.refleks_avi_icon_full),
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .shadow(22.dp, CircleShape, clip = false)
                .clip(CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = badgeOffsetX, y = badgeOffsetY)
                .size(badgeSize)
                .clip(CircleShape)
                .background(ArcadeGold)
        )
    }
}

@Composable
private fun BestScoreHero(
    bestScore: Int,
    isCompactHeight: Boolean
) {
    val rowHorizontalPadding = if (isCompactHeight) 14.dp else 16.dp
    val rowVerticalPadding = if (isCompactHeight) 12.dp else 14.dp
    val rowSpacing = if (isCompactHeight) 10.dp else 12.dp
    val iconSize = if (isCompactHeight) 38.dp else 42.dp

    Surface(
        color = ReflexGamePalette.neonBlue.copy(alpha = 0.18f),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = ReflexGamePalette.neonBlue.copy(alpha = 0.26f)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = rowHorizontalPadding,
                vertical = rowVerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(ArcadeGold.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    color = ArcadeGold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.best_score),
                    style = MaterialTheme.typography.labelLarge,
                    color = ReflexGamePalette.textSecondary
                )
                Text(
                    text = bestScore.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = ReflexGamePalette.textPrimary
                )
            }
        }
    }
}
