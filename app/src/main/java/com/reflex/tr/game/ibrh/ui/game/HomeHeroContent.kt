package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette


@Composable
internal fun GameLogo(isCompactHeight: Boolean) {
    val containerSize = if (isCompactHeight) 50.dp else 82.dp
    val iconSize = if (isCompactHeight) 42.dp else 66.dp
    val badgeOffsetX = if (isCompactHeight) 17.dp else 28.dp
    val badgeOffsetY = if (isCompactHeight) (-15).dp else (-24).dp
    val badgeSize = if (isCompactHeight) 12.dp else 14.dp

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
internal fun BestScoreHero(
    bestScore: Int,
    isCompactHeight: Boolean
) {
    val rowHorizontalPadding = if (isCompactHeight) 14.dp else 16.dp
    val rowVerticalPadding = if (isCompactHeight) 12.dp else 14.dp
    val rowSpacing = if (isCompactHeight) 10.dp else 12.dp
    val iconSize = if (isCompactHeight) 38.dp else 42.dp

    Surface(
        color = ReflexGamePalette.neonBlue.copy(alpha = 0.18f),
        shape = RoundedCornerShape(PremiumOverlayRadius),
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
