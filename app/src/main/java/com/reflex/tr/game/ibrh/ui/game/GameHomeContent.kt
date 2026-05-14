package com.reflex.tr.game.ibrh.ui.game

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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
import com.reflex.tr.game.ibrh.ui.game.components.HowToPlayItem
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
fun HomeContent(
    bestScore: Int,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isCompactHeight = maxHeight <= 780.dp
        val contentScrollState = rememberScrollState()
        val howToPlayScrollState = rememberScrollState()
        val panelPadding = if (isCompactHeight) 16.dp else 22.dp
        val contentSpacing = if (isCompactHeight) 12.dp else 18.dp
        val howToPlaySpacing = if (isCompactHeight) 10.dp else 12.dp
        val contentPadding = if (isCompactHeight) 16.dp else 18.dp
        val howToPlayHeight = if (isCompactHeight) 132.dp else 236.dp

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

                        Text(
                            text = stringResource(R.string.game_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = ReflexGamePalette.textPrimary,
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

                        HowToPlayPanel(
                            scrollState = howToPlayScrollState,
                            height = howToPlayHeight,
                            contentPadding = contentPadding,
                            itemSpacing = howToPlaySpacing,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    PrimaryGameButton(
                        text = stringResource(R.string.start_game),
                        onClick = onStartClick
                    )
                }
            }
        )
    }
}

@Composable
private fun HowToPlayPanel(
    scrollState: ScrollState,
    height: Dp,
    contentPadding: Dp,
    itemSpacing: Dp,
    modifier: Modifier = Modifier
) {
    GamePanelCard(
        modifier = modifier,
        containerColor = ReflexGamePalette.cardGlassStrong,
        tonalElevation = 0.dp,
        contentPadding = contentPadding
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(itemSpacing)) {
            Text(
                text = stringResource(R.string.how_to_play_title),
                style = MaterialTheme.typography.titleMedium,
                color = ReflexGamePalette.textPrimary
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(end = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(itemSpacing)
                ) {
                    HowToPlayItem(text = stringResource(R.string.how_to_play_target))
                    HowToPlayItem(text = stringResource(R.string.how_to_play_score))
                    HowToPlayItem(text = stringResource(R.string.how_to_play_lives))
                    HowToPlayItem(text = stringResource(R.string.how_to_play_end))
                }

                if (scrollState.maxValue > 0) {
                    val viewportHeight = constraints.maxHeight
                    val contentHeight = viewportHeight + scrollState.maxValue
                    val thumbHeight = (maxHeight * viewportHeight / contentHeight)
                        .coerceAtLeast(28.dp)
                    val scrollProgress = scrollState.value.toFloat() / scrollState.maxValue
                    val thumbOffset = (maxHeight - thumbHeight) * scrollProgress

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(5.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.18f))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(y = thumbOffset)
                            .width(5.dp)
                            .heightIn(min = thumbHeight, max = thumbHeight)
                            .clip(RoundedCornerShape(50))
                            .background(ArcadeGold)
                    )
                }
            }
        }
    }
}

@Composable
private fun GameLogo(isCompactHeight: Boolean) {
    val containerSize = if (isCompactHeight) 98.dp else 142.dp
    val iconSize = if (isCompactHeight) 76.dp else 106.dp
    val badgeOffsetX = if (isCompactHeight) 30.dp else 42.dp
    val badgeOffsetY = if (isCompactHeight) (-28).dp else (-40).dp
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
