package com.reflex.tr.game.ibrh.ui.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.feedback.rememberAnimatedPressScale
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCard
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
fun GamePanelCard(
    modifier: Modifier = Modifier,
    containerColor: Color = ReflexGamePalette.cardGlass,
    tonalElevation: Dp = 12.dp,
    contentPadding: Dp = 22.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(tonalElevation, RoundedCornerShape(30.dp), clip = false)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(30.dp)
            ),
        shape = RoundedCornerShape(30.dp),
        color = containerColor,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun GameStatCard(
    label: String,
    value: String,
    accentColor: Color,
    alertTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    val cardScale = remember { Animatable(1f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(alertTrigger) {
        if (alertTrigger == 0) return@LaunchedEffect

        cardScale.snapTo(0.96f)
        glowAlpha.snapTo(0.22f)
        cardScale.animateTo(1f, tween(180, easing = FastOutSlowInEasing))
        glowAlpha.animateTo(0f, tween(240, easing = FastOutLinearInEasing))
    }

    Surface(
        modifier = modifier
            .scale(cardScale.value)
            .shadow(10.dp, RoundedCornerShape(22.dp), clip = false)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        color = ReflexGamePalette.cardGlassStrong,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .background(accentColor.copy(alpha = glowAlpha.value * 0.65f))
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCardAccent(accentColor)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = ReflexGamePalette.textSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = ReflexGamePalette.textPrimary
            )
        }
    }
}

@Composable
fun LivesStatCard(
    label: String,
    lives: Int,
    accentColor: Color,
    alertTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    val cardScale = remember { Animatable(1f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(alertTrigger) {
        if (alertTrigger == 0) return@LaunchedEffect

        cardScale.snapTo(0.96f)
        glowAlpha.snapTo(0.22f)
        cardScale.animateTo(1f, tween(180, easing = FastOutSlowInEasing))
        glowAlpha.animateTo(0f, tween(240, easing = FastOutLinearInEasing))
    }

    Surface(
        modifier = modifier
            .scale(cardScale.value)
            .shadow(10.dp, RoundedCornerShape(22.dp), clip = false)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        color = ReflexGamePalette.cardGlassStrong,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .background(accentColor.copy(alpha = glowAlpha.value * 0.65f))
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCardAccent(accentColor)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = ReflexGamePalette.textSecondary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isFilled = index < lives
                    Icon(
                        imageVector = if (isFilled) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFilled) ArcadeCoral else ReflexGamePalette.textSecondary.copy(alpha = 0.42f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCardAccent(accentColor: Color) {
    Box(
        modifier = Modifier
            .size(width = 34.dp, height = 5.dp)
            .background(accentColor.copy(alpha = 0.9f), RoundedCornerShape(50))
    )
}

@Composable
fun PrimaryGameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberAnimatedPressScale(interactionSource)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SecondaryGameButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberAnimatedPressScale(interactionSource)

    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = ReflexGamePalette.textPrimary,
            disabledContainerColor = Color.White.copy(alpha = 0.06f),
            disabledContentColor = ReflexGamePalette.textSecondary.copy(alpha = 0.55f)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = if (enabled) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary
                )
            }
            Text(text = text, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
fun HowToPlayItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .background(ReflexGamePalette.targetRing, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = ReflexGamePalette.textSecondary
        )
    }
}

@Composable
fun BestScoreBadge(bestScore: Int) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ReflexGamePalette.neonBlue.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = ReflexGamePalette.neonBlue.copy(alpha = 0.24f)
        )
    ) {
        Text(
            text = stringResource(R.string.best_score_value, bestScore),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = ReflexGamePalette.textPrimary
        )
    }
}

@Composable
fun ScoreHighlightCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = accentColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.28f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = ReflexGamePalette.textSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = ReflexGamePalette.textPrimary
            )
        }
    }
}
