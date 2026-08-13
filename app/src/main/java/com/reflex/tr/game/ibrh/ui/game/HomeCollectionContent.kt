package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun CollectionTabContent(
    progressionState: ProgressionState
) {
    val unlockedThemeCount = PlayerTheme.entries.count { it in progressionState.unlockedThemes }
    val unlockedSkinCount = TargetSkin.entries.count { it in progressionState.unlockedTargetSkins }
    val totalThemes = PlayerTheme.entries.size
    val totalSkins = TargetSkin.entries.size
    val totalUnlocked = unlockedThemeCount + unlockedSkinCount
    val totalItems = totalThemes + totalSkins

    Text(
        text = stringResource(R.string.collection_title),
        style = MaterialTheme.typography.titleLarge,
        color = ReflexGamePalette.textPrimary
    )
    CollectionProgressCard(
        unlockedThemeCount = unlockedThemeCount,
        totalThemes = totalThemes,
        unlockedSkinCount = unlockedSkinCount,
        totalSkins = totalSkins,
        totalUnlocked = totalUnlocked,
        totalItems = totalItems
    )
    CollectionSection(
        title = stringResource(R.string.collection_unlocked_themes),
        emptyText = stringResource(R.string.collection_empty_unlocked_themes),
        items = PlayerTheme.entries.filter { it in progressionState.unlockedThemes }
    ) { theme ->
        CollectionThemeRow(
            theme = theme,
            unlocked = true,
            currentCoins = progressionState.coins
        )
    }
    CollectionSection(
        title = stringResource(R.string.collection_locked_themes),
        emptyText = stringResource(R.string.collection_empty_locked_themes),
        items = PlayerTheme.entries.filterNot { it in progressionState.unlockedThemes }
    ) { theme ->
        CollectionThemeRow(
            theme = theme,
            unlocked = false,
            currentCoins = progressionState.coins
        )
    }
    CollectionSection(
        title = stringResource(R.string.collection_unlocked_skins),
        emptyText = stringResource(R.string.collection_empty_unlocked_skins),
        items = TargetSkin.entries.filter { it in progressionState.unlockedTargetSkins }
    ) { skin ->
        CollectionSkinRow(
            skin = skin,
            unlocked = true,
            currentCoins = progressionState.coins
        )
    }
    CollectionSection(
        title = stringResource(R.string.collection_locked_skins),
        emptyText = stringResource(R.string.collection_empty_locked_skins),
        items = TargetSkin.entries.filterNot { it in progressionState.unlockedTargetSkins }
    ) { skin ->
        CollectionSkinRow(
            skin = skin,
            unlocked = false,
            currentCoins = progressionState.coins
        )
    }
}

@Composable
private fun CollectionProgressCard(
    unlockedThemeCount: Int,
    totalThemes: Int,
    unlockedSkinCount: Int,
    totalSkins: Int,
    totalUnlocked: Int,
    totalItems: Int
) {
    val progress = if (totalItems > 0) {
        (totalUnlocked.toFloat() / totalItems.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CollectionProgressPill(
                    label = stringResource(R.string.collection_themes_progress),
                    value = stringResource(R.string.collection_count_value, unlockedThemeCount, totalThemes),
                    modifier = Modifier.weight(1f)
                )
                CollectionProgressPill(
                    label = stringResource(R.string.collection_skins_progress),
                    value = stringResource(R.string.collection_count_value, unlockedSkinCount, totalSkins),
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = stringResource(R.string.collection_total_progress, totalUnlocked, totalItems),
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = ArcadeGold,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            if (totalItems > 0 && totalUnlocked >= totalItems) {
                CollectionMasterBadge(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun CollectionProgressPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ArcadeBlue.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ArcadeBlue.copy(alpha = 0.26f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ReflexGamePalette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun <T> CollectionSection(
    title: String,
    emptyText: String,
    items: List<T>,
    itemContent: @Composable (T) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = ReflexGamePalette.textPrimary
        )
        if (items.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ReflexGamePalette.cardGlassStrong,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Text(
                    text = emptyText,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
        } else {
            items.forEach { itemContent(it) }
        }
    }
}

@Composable
private fun CollectionThemeRow(
    theme: PlayerTheme,
    unlocked: Boolean,
    currentCoins: Int
) {
    val accent = collectionThemeAccent(theme)
    CollectionItemRow(
        name = stringResource(theme.titleRes),
        price = theme.coinPrice,
        currentCoins = currentCoins,
        rarity = stringResource(theme.rarity.titleRes),
        unlocked = unlocked,
        accent = accent
    ) {
        CollectionThemePreview(accent = accent)
    }
}

@Composable
private fun CollectionSkinRow(
    skin: TargetSkin,
    unlocked: Boolean,
    currentCoins: Int
) {
    val accent = collectionSkinAccent(skin)
    CollectionItemRow(
        name = stringResource(skin.titleRes),
        price = skin.coinPrice,
        currentCoins = currentCoins,
        rarity = stringResource(R.string.collection_skin_rarity),
        unlocked = unlocked,
        accent = accent
    ) {
        CollectionSkinPreview(skin = skin, accent = accent)
    }
}

@Composable
private fun CollectionItemRow(
    name: String,
    price: Int,
    currentCoins: Int,
    rarity: String,
    unlocked: Boolean,
    accent: Color,
    preview: @Composable () -> Unit
) {
    val remainingCoins = (price - currentCoins.coerceAtLeast(0)).coerceAtLeast(0)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (unlocked) accent.copy(alpha = 0.13f) else ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = if (unlocked) 0.46f else 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(PremiumCardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            preview()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (unlocked) {
                        stringResource(R.string.collection_item_unlocked)
                    } else {
                        stringResource(R.string.collection_locked_item_meta, price, remainingCoins, rarity)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (unlocked) ArcadeGold else ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = if (unlocked) {
                    stringResource(R.string.collection_item_open)
                } else {
                    stringResource(R.string.collection_item_locked)
                },
                style = MaterialTheme.typography.labelMedium,
                color = if (unlocked) ArcadeTeal else ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun CollectionMasterBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ArcadeGold.copy(alpha = 0.16f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.42f))
    ) {
        Text(
            text = stringResource(R.string.collection_master_badge),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = ArcadeGold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

internal fun isCollectionComplete(progressionState: ProgressionState): Boolean {
    return PlayerTheme.entries.all { it in progressionState.unlockedThemes } &&
        TargetSkin.entries.all { it in progressionState.unlockedTargetSkins }
}

@Composable
private fun CollectionThemePreview(accent: Color) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(accent.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(23.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.88f),
                            accent.copy(alpha = 0.82f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun CollectionSkinPreview(
    skin: TargetSkin,
    accent: Color
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (skin) {
                TargetSkin.ClassicTarget -> "○"
                TargetSkin.NeonRing -> "◎"
                TargetSkin.CyberDot -> "•"
                TargetSkin.FireCore -> "◆"
                TargetSkin.MatrixOrb -> "◇"
            },
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

private fun collectionThemeAccent(theme: PlayerTheme): Color {
    return themeVisualSpec(theme).primary
}

private fun collectionSkinAccent(skin: TargetSkin): Color {
    return when (skin) {
        TargetSkin.ClassicTarget -> ArcadeGold
        TargetSkin.NeonRing -> Color(0xFF41F2FF)
        TargetSkin.CyberDot -> Color(0xFF9F7BFF)
        TargetSkin.FireCore -> Color(0xFFFF6B3D)
        TargetSkin.MatrixOrb -> Color(0xFF49FF91)
    }
}
