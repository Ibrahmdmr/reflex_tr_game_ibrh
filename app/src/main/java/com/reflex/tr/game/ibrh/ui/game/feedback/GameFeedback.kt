package com.reflex.tr.game.ibrh.ui.game.feedback

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.ui.game.GameSoundController
import com.reflex.tr.game.ibrh.ui.game.GameSoundEffect
import com.reflex.tr.game.ibrh.ui.game.TargetPosition
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoralSoft
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun TargetMarker(
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    val popScale = remember { Animatable(1f) }
    val isPressed by interactionSource.collectIsPressedAsState()
    val currentOnTap by rememberUpdatedState(onTap)
    val infiniteTransition = rememberInfiniteTransition(label = "target_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "target_pulse_scale"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
        label = "target_press_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "target_glow_alpha"
    )

    Box(
        modifier = modifier
            .scale(animatedScale * popScale.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                scope.launch {
                    popScale.snapTo(0.94f)
                    popScale.animateTo(
                        targetValue = 1.08f,
                        animationSpec = tween(durationMillis = 70, easing = FastOutSlowInEasing)
                    )
                    popScale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
                    )
                }
                currentOnTap()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .scale(pulseScale * 1.02f)
                .alpha(glowAlpha)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ReflexGamePalette.targetRing.copy(alpha = 0.78f),
                            ReflexGamePalette.targetRing.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(82.dp)
                .scale(pulseScale)
                .shadow(16.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(ReflexGamePalette.targetRing.copy(alpha = 0.34f))
        )
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            ArcadeCoralSoft
                        )
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF9B89),
                            Color(0xFFFF7463),
                            ReflexGamePalette.targetCore
                        )
                    )
                )
                .border(2.5.dp, Color.White.copy(alpha = 0.82f), CircleShape)
        )
        GlossyHighlight(
            modifier = Modifier
                .offset(x = (-8).dp, y = (-12).dp)
                .size(width = 24.dp, height = 12.dp)
        )
        GlossyHighlight(
            modifier = Modifier
                .offset(x = (-11).dp, y = (-4).dp)
                .size(width = 11.dp, height = 11.dp),
            alpha = 0.84f
        )
    }
}

@Composable
private fun GlossyHighlight(
    modifier: Modifier = Modifier,
    alpha: Float = 0.32f,
    shape: Shape = CircleShape
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha),
                        Color.White.copy(alpha = alpha * 0.18f)
                    )
                )
            )
    )
}

@Composable
fun MissFeedbackEffect(trigger: Int) {
    val flashAlpha = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        flashAlpha.snapTo(0.22f)
        flashAlpha.animateTo(0f, tween(220, easing = FastOutLinearInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(flashAlpha.value)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        ArcadeCoral.copy(alpha = 0.55f),
                        ArcadeCoral.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun HitFeedbackEffect(
    trigger: Int,
    position: TargetPosition,
    targetSize: Dp
) {
    val burstScale = remember { Animatable(0.65f) }
    val burstAlpha = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        burstScale.snapTo(0.65f)
        burstAlpha.snapTo(0.4f)
        burstScale.animateTo(1.55f, tween(180))
        burstAlpha.animateTo(0f, tween(180, easing = FastOutLinearInEasing))
    }

    if (burstAlpha.value <= 0f) return

    val density = LocalDensity.current
    val targetSizePx = with(density) { targetSize.roundToPx() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxX = (constraints.maxWidth - targetSizePx).coerceAtLeast(0)
        val maxY = (constraints.maxHeight - targetSizePx).coerceAtLeast(0)

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (maxX * position.xFraction).roundToInt(),
                        y = (maxY * position.yFraction).roundToInt()
                    )
                }
                .size(targetSize)
                .scale(burstScale.value)
                .alpha(burstAlpha.value)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ArcadeCoralSoft.copy(alpha = 0.9f),
                            ArcadeCoral.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun rememberShakeTranslationX(trigger: Int): Float {
    val translation = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        translation.snapTo(0f)
        translation.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 220
                0f at 0
                -14f at 40
                12f at 85
                -8f at 130
                6f at 175
                0f at 220
            }
        )
    }

    return translation.value
}

internal data class GameSoundHooks(
    val onHit: () -> Unit = {},
    val onMiss: () -> Unit = {},
    val onGameOver: () -> Unit = {}
)

@Composable
fun rememberAnimatedPressScale(
    interactionSource: MutableInteractionSource
): Float {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "button_press_scale"
    )
    return scale
}

@Composable
internal fun rememberGameSoundHooks(): GameSoundHooks {
    val context = LocalContext.current
    val soundController = remember(context) { GameSoundController(context) }

    DisposableEffect(soundController) {
        onDispose { soundController.release() }
    }

    return remember(soundController) {
        GameSoundHooks(
            onHit = { soundController.play(GameSoundEffect.Hit) },
            onMiss = { soundController.play(GameSoundEffect.Miss) },
            onGameOver = { soundController.play(GameSoundEffect.GameOver) }
        )
    }
}
