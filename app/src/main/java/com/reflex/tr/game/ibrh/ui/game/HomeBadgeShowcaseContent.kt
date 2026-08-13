package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.InfoChip
import com.reflex.tr.game.ibrh.ui.game.components.PremiumSurfaceCard
import com.reflex.tr.game.ibrh.ui.game.components.SectionTitle
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun BadgeShowcaseCard(
    progressionState: ProgressionState,
    onBadgeSelected: (ProfileBadge) -> Unit
) {
    val unlockedBadges = unlockedProfileBadges(progressionState)
    val showcasedBadges = showcasedProfileBadges(progressionState)

    PremiumSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = ArcadeGold
    ) {
            SectionTitle(
                text = stringResource(R.string.badge_showcase_title),
                accentColor = ReflexGamePalette.textPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                showcasedBadges.forEach { badge ->
                    BadgePill(
                        badge = badge,
                        unlocked = badge in unlockedBadges,
                        selected = badge.storageKey in progressionState.selectedProfileBadgeIds,
                        modifier = Modifier.weight(1f),
                        onClick = { if (badge in unlockedBadges) onBadgeSelected(badge) }
                    )
                }
            }
            Text(
                text = stringResource(R.string.badge_showcase_helper),
                style = MaterialTheme.typography.bodySmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            ProfileBadge.entries.forEach { badge ->
                BadgeListRow(
                    badge = badge,
                    unlocked = badge in unlockedBadges,
                    selected = badge.storageKey in progressionState.selectedProfileBadgeIds,
                    onClick = { if (badge in unlockedBadges) onBadgeSelected(badge) }
                )
            }
    }
}

@Composable
private fun BadgePill(
    badge: ProfileBadge,
    unlocked: Boolean,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = badgeAccent(badge)
    Surface(
        modifier = modifier
            .graphicsLayer { alpha = if (unlocked) 1f else 0.42f }
            .clickable(enabled = unlocked, onClick = onClick),
        color = accent.copy(alpha = if (selected) 0.2f else 0.12f),
        shape = RoundedCornerShape(PremiumCompactRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = if (selected) 0.54f else 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = badge.icon,
                style = MaterialTheme.typography.titleMedium,
                color = accent
            )
            Text(
                text = stringResource(badge.titleRes),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BadgeListRow(
    badge: ProfileBadge,
    unlocked: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = badgeAccent(badge)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (unlocked) 1f else 0.46f }
            .clickable(enabled = unlocked, onClick = onClick),
        color = if (selected) accent.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.045f),
        shape = RoundedCornerShape(PremiumCompactRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = if (selected) 0.42f else 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                text = badge.icon,
                style = MaterialTheme.typography.titleSmall,
                color = accent
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(badge.titleRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (unlocked) {
                        stringResource(badge.descriptionRes)
                    } else {
                        stringResource(badge.lockedHintRes)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            InfoChip(
                text = when {
                    selected -> stringResource(R.string.badge_selected)
                    unlocked -> stringResource(R.string.badge_select)
                    else -> stringResource(R.string.badge_locked)
                },
                accentColor = if (unlocked) ArcadeGold else ReflexGamePalette.textSecondary,
                selected = selected
            )
        }
    }
}

private fun badgeAccent(badge: ProfileBadge): Color {
    return when (badge.rarityRank) {
        5 -> ArcadeGold
        4 -> ArcadeTeal
        3 -> Color(0xFF9F7BFF)
        2 -> ArcadeBlue
        else -> ReflexGamePalette.textSecondary
    }
}

