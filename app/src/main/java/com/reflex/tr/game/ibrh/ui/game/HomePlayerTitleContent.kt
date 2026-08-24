package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun PlayerTitleCard(
    profile: PlayerProfile,
    progressionState: ProgressionState,
    onTitleSelect: (PlayerTitle) -> Unit,
    onTitlesOpened: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val activeTitle = profile.activeTitle
    val accent = activeTitle?.let { playerTitleAccent(it.rarity) } ?: ArcadeTeal
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(PremiumCardPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.player_titles_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.player_title_progress_value,
                        profile.unlockedTitleCount,
                        PlayerTitle.entries.size
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(R.string.player_title_active),
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = activeTitle?.let { stringResource(it.titleRes) }
                    ?: stringResource(R.string.player_title_none_selected),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            SecondaryGameButton(
                text = stringResource(
                    if (expanded) R.string.player_titles_hide else R.string.player_titles_open
                ),
                onClick = {
                    if (!expanded) onTitlesOpened()
                    expanded = !expanded
                },
                modifier = Modifier.height(46.dp)
            )
            if (expanded) {
                PlayerTitlesSection(
                    profile = profile,
                    progressionState = progressionState,
                    onTitleSelect = onTitleSelect
                )
            }
        }
    }
}

@Composable
private fun PlayerTitlesSection(
    profile: PlayerProfile,
    progressionState: ProgressionState,
    onTitleSelect: (PlayerTitle) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PlayerTitle.entries.forEach { title ->
            PlayerTitleRow(
                title = title,
                unlocked = title in profile.unlockedTitles,
                selected = title == profile.activeTitle,
                progressionState = progressionState,
                onSelectClick = { onTitleSelect(title) }
            )
        }
    }
}

@Composable
private fun PlayerTitleRow(
    title: PlayerTitle,
    unlocked: Boolean,
    selected: Boolean,
    progressionState: ProgressionState,
    onSelectClick: () -> Unit
) {
    val accent = playerTitleAccent(title.rarity)
    val nameColor = if (unlocked) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (unlocked && !selected) Modifier.clickable(onClick = onSelectClick) else Modifier),
        color = if (selected) accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(PremiumCompactRadius),
        border = BorderStroke(
            1.dp,
            accent.copy(alpha = if (selected) 0.52f else if (unlocked) 0.30f else 0.16f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(title.titleRes),
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.titleSmall,
                        color = nameColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    PlayerTitleRarityChip(rarity = title.rarity)
                }
                Text(
                    text = stringResource(title.requirementRes, title.requirementValue),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(title.category.titleRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = accent.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(
                    when {
                        selected -> R.string.player_title_selected
                        unlocked -> R.string.player_title_select
                        else -> R.string.player_title_locked
                    }
                ),
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    selected -> accent
                    unlocked -> ArcadeGold
                    else -> ReflexGamePalette.textSecondary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    // Kept out of the row above so an unmet requirement reads as a number, not a wall of text.
    if (!unlocked) {
        PlayerTitleProgressHint(title = title, progressionState = progressionState)
    }
}

@Composable
private fun PlayerTitleProgressHint(
    title: PlayerTitle,
    progressionState: ProgressionState
) {
    val progress = playerTitleProgressValue(title, progressionState)
    if (progress <= 0) return
    Text(
        text = stringResource(
            R.string.player_title_progress_value,
            progress,
            title.requirementValue.coerceAtLeast(1)
        ),
        modifier = Modifier.padding(start = 10.dp),
        style = MaterialTheme.typography.labelSmall,
        color = ReflexGamePalette.textSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun PlayerTitleRarityChip(rarity: PlayerTitleRarity) {
    val accent = playerTitleAccent(rarity)
    Surface(
        color = accent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(PremiumPillRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.34f))
    ) {
        Text(
            text = stringResource(rarity.titleRes),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = ReflexGamePalette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun PlayerTitleInlineLabel(
    title: PlayerTitle,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(title.titleRes),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = playerTitleAccent(title.rarity),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
internal fun NewPlayerTitlesCard(
    titles: List<PlayerTitle>,
    modifier: Modifier = Modifier
) {
    val accent = playerTitleAccent(titles.firstOrNull()?.rarity ?: return)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.44f))
    ) {
        Column(
            modifier = Modifier.padding(PremiumCardPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            titles.take(MAX_GAME_OVER_TITLE_LINES).forEach { title ->
                Text(
                    text = stringResource(
                        R.string.player_title_unlocked_value,
                        stringResource(title.titleRes)
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (titles.size > MAX_GAME_OVER_TITLE_LINES) {
                Text(
                    text = stringResource(R.string.player_titles_more_in_profile),
                    style = MaterialTheme.typography.labelSmall,
                    color = ArcadeGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private const val MAX_GAME_OVER_TITLE_LINES = 2
