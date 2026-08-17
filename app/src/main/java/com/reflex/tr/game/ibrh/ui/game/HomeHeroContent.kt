package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
            painter = painterResource(R.drawable.app_logo),
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
