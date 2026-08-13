package com.reflex.tr.game.ibrh.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.GamePanelCard
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
fun HowToPlayScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf<GameMode?>(null) }
    val scrollState = rememberScrollState()

    BackHandler {
        if (selectedMode != null) {
            selectedMode = null
        } else {
            onBackClick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        ReflexGamePalette.homeGradientTop,
                        ReflexGamePalette.homeGradientBottom
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HowToPlayHeader(
                isDetailVisible = selectedMode != null,
                onBackClick = {
                    if (selectedMode != null) {
                        selectedMode = null
                    } else {
                        onBackClick()
                    }
                }
            )

            AnimatedContent(
                targetState = selectedMode,
                label = "how_to_play_content",
                transitionSpec = {
                    if (targetState == null) {
                        slideInHorizontally { -it / 3 } + fadeIn() togetherWith
                            slideOutHorizontally { it / 3 } + fadeOut()
                    } else {
                        slideInHorizontally { it / 3 } + fadeIn() togetherWith
                            slideOutHorizontally { -it / 3 } + fadeOut()
                    }
                }
            ) { mode ->
                if (mode == null) {
                    HowToPlayModeList(
                        onModeClick = { selectedMode = it }
                    )
                } else {
                    HowToPlayModeDetail(mode = mode)
                }
            }
        }
    }
}

@Composable
private fun HowToPlayHeader(
    isDetailVisible: Boolean,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onBackClick,
            shape = RoundedCornerShape(PremiumChipRadius)
        ) {
            Text(
                text = if (isDetailVisible) {
                    stringResource(R.string.back_to_modes)
                } else {
                    stringResource(R.string.back_to_home)
                }
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.how_to_play_title),
                style = MaterialTheme.typography.headlineSmall,
                color = ReflexGamePalette.textPrimary
            )
            Text(
                text = stringResource(R.string.how_to_play_screen_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = ReflexGamePalette.textSecondary
            )
        }
    }
}

@Composable
private fun HowToPlayModeList(
    onModeClick: (GameMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GameMode.entries.forEach { mode ->
            GameModeCard(
                mode = mode,
                onClick = { onModeClick(mode) }
            )
        }
    }
}

@Composable
private fun HowToPlayModeDetail(mode: GameMode) {
    val accentColor = modeAccentColor(mode)

    GamePanelCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = ReflexGamePalette.cardGlassStrong
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(PremiumCardRadius),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.34f))
                ) {
                    Text(
                        text = modeIcon(mode),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = accentColor,
                        textAlign = TextAlign.Center
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(mode.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        color = ReflexGamePalette.textPrimary
                    )
                    Text(
                        text = stringResource(mode.longDescriptionRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ReflexGamePalette.textSecondary
                    )
                }
                DifficultyChip(mode = mode)
            }

            RuleList(rules = stringArrayResource(mode.rulesArrayRes()))

            Spacer(modifier = Modifier.height(2.dp))

            Surface(
                color = Color.White.copy(alpha = 0.07f),
                shape = RoundedCornerShape(PremiumCardRadius),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.example_gameplay_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = ReflexGamePalette.textPrimary
                    )
                    Text(
                        text = stringResource(mode.exampleRes()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ReflexGamePalette.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyChip(mode: GameMode) {
    val accentColor = modeAccentColor(mode)
    Surface(
        color = accentColor.copy(alpha = 0.16f),
        shape = RoundedCornerShape(PremiumPillRadius)
    ) {
        Text(
            text = stringResource(mode.difficultyRes),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = ReflexGamePalette.textPrimary
        )
    }
}

@Composable
private fun RuleList(rules: Array<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rules.forEach { rule ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        color = ReflexGamePalette.targetRing,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = rule,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ReflexGamePalette.textSecondary
                    )
                }
            }
        }
    }
}

private fun GameMode.rulesArrayRes(): Int {
    return when (this) {
        GameMode.Classic -> R.array.rules_classic
        GameMode.MovingTarget -> R.array.rules_moving
        GameMode.FakeTarget -> R.array.rules_fake
        GameMode.ColorReflex -> R.array.rules_color
    }
}

private fun GameMode.exampleRes(): Int {
    return when (this) {
        GameMode.Classic -> R.string.example_classic
        GameMode.MovingTarget -> R.string.example_moving
        GameMode.FakeTarget -> R.string.example_fake
        GameMode.ColorReflex -> R.string.example_color
    }
}
