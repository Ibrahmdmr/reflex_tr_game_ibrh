package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
    GamePanelCard(
        modifier = modifier.fillMaxWidth(),
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                GameLogo()

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

                BestScoreHero(bestScore = bestScore)

                GamePanelCard(
                    containerColor = ReflexGamePalette.cardGlassStrong,
                    tonalElevation = 0.dp,
                    contentPadding = 18.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.how_to_play_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = ReflexGamePalette.textPrimary
                        )
                        HowToPlayItem(text = stringResource(R.string.how_to_play_target))
                        HowToPlayItem(text = stringResource(R.string.how_to_play_score))
                        HowToPlayItem(text = stringResource(R.string.how_to_play_lives))
                        HowToPlayItem(text = stringResource(R.string.how_to_play_end))
                    }
                }

                PrimaryGameButton(
                    text = stringResource(R.string.start_game),
                    onClick = onStartClick,
                    modifier = Modifier
                )
            }
        }
    )
}

@Composable
private fun GameLogo() {
    Box(
        modifier = Modifier.size(142.dp),
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
                .size(106.dp)
                .shadow(22.dp, CircleShape, clip = false)
                .clip(CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = 42.dp, y = (-40).dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(ArcadeGold)
        )
    }
}

@Composable
private fun BestScoreHero(bestScore: Int) {
    Surface(
        color = ReflexGamePalette.neonBlue.copy(alpha = 0.18f),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = ReflexGamePalette.neonBlue.copy(alpha = 0.26f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
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
