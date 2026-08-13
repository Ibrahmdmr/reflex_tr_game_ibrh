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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
internal fun ProfileQuickMenu(
    onTabSelected: (HomeTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileQuickMenuCard(
                title = stringResource(R.string.season_title),
                description = stringResource(R.string.profile_quick_season_description),
                icon = "S",
                accent = ArcadeGold,
                onClick = { onTabSelected(HomeTab.Season) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileQuickMenuCard(
                    title = stringResource(R.string.leaderboard_title),
                    description = stringResource(R.string.profile_quick_leaderboard_description),
                    icon = "#",
                    accent = ArcadeBlue,
                    onClick = { onTabSelected(HomeTab.Leaderboard) },
                    modifier = Modifier.weight(1f)
                )
                ProfileQuickMenuCard(
                    title = stringResource(R.string.achievements_title),
                    description = stringResource(R.string.profile_quick_achievements_description),
                    icon = "◇",
                    accent = ArcadeGold,
                    onClick = { onTabSelected(HomeTab.Achievements) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileQuickMenuCard(
                    title = stringResource(R.string.statistics_title),
                    description = stringResource(R.string.profile_quick_statistics_description),
                    icon = "%",
                    accent = ArcadeBlue,
                    onClick = { onTabSelected(HomeTab.Statistics) },
                    modifier = Modifier.weight(1f)
                )
                ProfileQuickMenuCard(
                    title = stringResource(R.string.collection_title),
                    description = stringResource(R.string.profile_quick_collection_description),
                    icon = "▣",
                    accent = ArcadeGold,
                    onClick = { onTabSelected(HomeTab.Collection) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileQuickMenuCard(
                    title = stringResource(R.string.nav_missions),
                    description = stringResource(R.string.profile_quick_missions_description),
                    icon = "✓",
                    accent = ArcadeTeal,
                    onClick = { onTabSelected(HomeTab.Missions) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileQuickMenuCard(
                    title = stringResource(R.string.nav_settings),
                    description = stringResource(R.string.profile_quick_settings_description),
                    icon = "⚙",
                    accent = ArcadeCoral,
                    onClick = { onTabSelected(HomeTab.Settings) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun ProfileQuickMenuCard(
    title: String,
    description: String,
    icon: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = accent.copy(alpha = 0.11f),
        shape = RoundedCornerShape(PremiumCompactRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleSmall,
                color = accent
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.titleSmall,
                color = accent
            )
        }
    }
}
