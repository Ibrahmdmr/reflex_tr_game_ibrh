package com.reflex.tr.game.ibrh.ui.game.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.PremiumCardPadding
import com.reflex.tr.game.ibrh.ui.game.PremiumCardRadius
import com.reflex.tr.game.ibrh.ui.game.PremiumChipRadius
import com.reflex.tr.game.ibrh.ui.game.PremiumCompactRadius
import com.reflex.tr.game.ibrh.ui.game.PremiumPanelRadius
import com.reflex.tr.game.ibrh.ui.game.PremiumSectionSpacing
import com.reflex.tr.game.ibrh.ui.game.feedback.rememberAnimatedPressScale
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
fun PremiumSurfaceCard(
    modifier: Modifier = Modifier,
    accentColor: Color = ArcadeBlue,
    containerColor: Color = ReflexGamePalette.cardGlassStrong,
    selected: Boolean = false,
    contentPadding: Dp = PremiumCardPadding,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (selected) accentColor.copy(alpha = 0.14f) else containerColor,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = if (selected) 0.48f else 0.26f)
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(PremiumSectionSpacing),
            content = content
        )
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = ArcadeGold,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleSmall,
        color = accentColor,
        textAlign = textAlign,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun InfoChip(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = accentColor.copy(alpha = if (selected) 0.18f else 0.1f),
        shape = RoundedCornerShape(PremiumCompactRadius),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = if (selected) 0.42f else 0.24f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CompactStatCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    prominent: Boolean = false
) {
    PremiumSurfaceCard(
        modifier = modifier,
        accentColor = accentColor,
        containerColor = accentColor.copy(alpha = if (prominent) 0.17f else 0.11f),
        selected = prominent,
        contentPadding = if (prominent) 10.dp else 8.dp
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = ReflexGamePalette.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = value,
            style = if (prominent) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelLarge,
            color = ReflexGamePalette.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

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
            .shadow(tonalElevation, RoundedCornerShape(PremiumPanelRadius), clip = false)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(PremiumPanelRadius)
            ),
        shape = RoundedCornerShape(PremiumPanelRadius),
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
fun PrimaryGameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 56.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberAnimatedPressScale(interactionSource)
    val pulse by rememberInfiniteTransition(label = "primary_button_pulse").animateFloat(
        initialValue = 0.86f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "primary_button_pulse_value"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .scale(scale)
            .graphicsLayer {
                shadowElevation = 10f + pulse * 12f
            },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(PremiumCardRadius),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            ArcadeBlue.copy(alpha = 0.92f),
                            ReflexGamePalette.neonPurple.copy(alpha = 0.9f),
                            ArcadeGold.copy(alpha = 0.76f + pulse * 0.08f)
                        )
                    ),
                    RoundedCornerShape(PremiumChipRadius)
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.24f + pulse * 0.12f),
                    RoundedCornerShape(PremiumChipRadius)
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Full-width secondary action, sized for stacking in a [Column]. It fills its width internally,
 * so inside a [Row] always pass a bounded modifier — otherwise it collapses its siblings.
 */
@Composable
fun SecondaryGameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
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
        shape = RoundedCornerShape(PremiumCardRadius),
        // Tighter than the Material3 default, which left no room for the label in a narrow row.
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.095f),
            contentColor = ReflexGamePalette.textPrimary,
            disabledContainerColor = Color.White.copy(alpha = 0.075f),
            disabledContentColor = ReflexGamePalette.textSecondary.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = if (enabled) ReflexGamePalette.textPrimary else ReflexGamePalette.textSecondary
                )
            }
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BestScoreBadge(bestScore: Int) {
    Surface(
        shape = RoundedCornerShape(PremiumCardRadius),
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
