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
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.ui.game.GameSoundController
import com.reflex.tr.game.ibrh.ui.game.GameSoundEffect
import com.reflex.tr.game.ibrh.ui.game.GameTargetRole
import com.reflex.tr.game.ibrh.ui.game.PlayerTheme
import com.reflex.tr.game.ibrh.ui.game.ReflexTargetColor
import com.reflex.tr.game.ibrh.ui.game.TargetPosition
import com.reflex.tr.game.ibrh.ui.game.themeVisualSpec
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoralSoft
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun TargetMarker(
    modifier: Modifier = Modifier,
    targetColor: ReflexTargetColor = ReflexTargetColor.Red,
    role: GameTargetRole = GameTargetRole.Correct,
    spawnKey: Any = Unit,
    comboLevel: Int = 0,
    theme: PlayerTheme = PlayerTheme.NeonRed,
    onTap: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    val popScale = remember { Animatable(1f) }
    val spawnScale = remember { Animatable(0.72f) }
    val isPressed by interactionSource.collectIsPressedAsState()
    val currentOnTap by rememberUpdatedState(onTap)
    val infiniteTransition = rememberInfiniteTransition(label = "target_pulse")
    val shouldPulse = comboLevel >= 3 || role == GameTargetRole.Correct
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = if (shouldPulse) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (shouldPulse) 1180 else 1800, easing = FastOutSlowInEasing),
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
        initialValue = 0.24f,
        targetValue = if (shouldPulse) 0.44f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (shouldPulse) 1400 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "target_glow_alpha"
    )
    LaunchedEffect(spawnKey) {
        spawnScale.snapTo(0.72f)
        spawnScale.animateTo(1.08f, tween(130, easing = FastOutSlowInEasing))
        spawnScale.animateTo(1f, tween(90, easing = FastOutSlowInEasing))
    }
    val baseColor = remember(targetColor) { targetColor.toComposeColor() }
    val themeGlowColor = remember(theme) { themeVisualSpec(theme).primary }
    val ringAlpha = if (role == GameTargetRole.Correct) 0.9f else 0.62f
    val coreAlpha = if (role == GameTargetRole.Correct) 1f else 0.78f
    val borderAlpha = if (role == GameTargetRole.Correct) 0.92f else 0.5f
    val comboBoost = (comboLevel / 5f).coerceIn(0f, 1f)
    val highComboBoost = (comboLevel / 20f).coerceIn(0f, 1f)
    val roleScale = if (role == GameTargetRole.Correct) 1f else 0.96f
    val glowSize = 1.42f + highComboBoost * 0.2f
    val haloSize = 1.64f + highComboBoost * 0.24f

    Box(
        modifier = modifier
            .scale(animatedScale * popScale.value * spawnScale.value * roleScale)
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
                .fillMaxSize(glowSize)
                .scale(pulseScale * (1.02f + highComboBoost * 0.05f))
                .alpha((glowAlpha + comboBoost * 0.24f + highComboBoost * 0.2f).coerceAtMost(0.86f))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            baseColor.copy(alpha = ringAlpha),
                            themeGlowColor.copy(alpha = 0.42f + highComboBoost * 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )
        if (comboLevel >= 5) {
            Box(
                modifier = Modifier
                    .fillMaxSize(haloSize)
                    .scale(1.08f + highComboBoost * 0.08f)
                    .alpha(0.16f + comboBoost * 0.12f + highComboBoost * 0.18f)
                    .border(1.5.dp, themeGlowColor.copy(alpha = 0.78f), CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize(0.98f)
                .scale(pulseScale)
                .shadow(22.dp + (comboBoost * 16).dp + (highComboBoost * 20).dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(themeGlowColor.copy(alpha = 0.34f + comboBoost * 0.16f + highComboBoost * 0.14f))
        )
        Box(
            modifier = Modifier
                .fillMaxSize(0.78f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (role == GameTargetRole.Correct) 0.95f else 0.62f),
                            baseColor.copy(alpha = if (role == GameTargetRole.Correct) 0.32f else 0.2f)
                        )
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = borderAlpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .fillMaxSize(0.56f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (role == GameTargetRole.Correct) 0.28f else 0.14f),
                            baseColor.copy(alpha = 0.76f),
                            baseColor.copy(alpha = coreAlpha)
                        )
                    )
                )
                .border(2.5.dp, Color.White.copy(alpha = borderAlpha), CircleShape)
        )
        Canvas(modifier = Modifier.fillMaxSize(0.72f)) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.minDimension * 0.42f
            val innerRadius = size.minDimension * if (role == GameTargetRole.Correct) 0.2f else 0.24f
            drawCircle(
                color = Color.White.copy(alpha = if (role == GameTargetRole.Correct) 0.54f else 0.34f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = size.minDimension * 0.04f)
            )
            drawCircle(
                color = Color.White.copy(alpha = if (role == GameTargetRole.Correct) 0.48f else 0.26f),
                radius = innerRadius,
                center = center,
                style = Stroke(width = size.minDimension * 0.045f)
            )
            if (role == GameTargetRole.Fake) {
                drawLine(
                    color = Color.White.copy(alpha = 0.34f),
                    start = androidx.compose.ui.geometry.Offset(size.width * 0.26f, size.height * 0.28f),
                    end = androidx.compose.ui.geometry.Offset(size.width * 0.74f, size.height * 0.72f),
                    strokeWidth = size.minDimension * 0.055f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = baseColor.copy(alpha = 0.42f),
                    start = androidx.compose.ui.geometry.Offset(size.width * 0.74f, size.height * 0.28f),
                    end = androidx.compose.ui.geometry.Offset(size.width * 0.26f, size.height * 0.72f),
                    strokeWidth = size.minDimension * 0.04f,
                    cap = StrokeCap.Round
                )
            } else {
                drawCircle(
                    color = Color.White.copy(alpha = 0.68f),
                    radius = size.minDimension * 0.055f,
                    center = center
                )
            }
        }
        GlossyHighlight(
            modifier = Modifier
                .offset(x = (-7).dp, y = (-11).dp)
                .fillMaxSize(0.28f)
        )
        GlossyHighlight(
            modifier = Modifier
                .offset(x = (-13).dp, y = (-4).dp)
                .fillMaxSize(0.12f),
            alpha = 0.84f
        )
    }
}

private fun ReflexTargetColor.toComposeColor(): Color {
    return when (this) {
        ReflexTargetColor.Red -> Color(0xFFFF335F)
        ReflexTargetColor.Blue -> Color(0xFF39A8FF)
        ReflexTargetColor.Gold -> Color(0xFFFFD84D)
        ReflexTargetColor.Teal -> Color(0xFF22F2A6)
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
        burstAlpha.snapTo(0.72f)
        burstScale.animateTo(1.85f, tween(220, easing = FastOutSlowInEasing))
        burstAlpha.animateTo(0f, tween(190, easing = FastOutLinearInEasing))
    }

    if (burstAlpha.value <= 0f) return

    val density = LocalDensity.current
    val targetSizePx = with(density) { targetSize.roundToPx() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxX = (constraints.maxWidth - targetSizePx).coerceAtLeast(0)
        val maxY = (constraints.maxHeight - targetSizePx).coerceAtLeast(0)

        Canvas(
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
        ) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.34f
            drawCircle(
                color = ArcadeGold.copy(alpha = 0.72f),
                radius = radius,
                center = center,
                style = Stroke(width = size.minDimension * 0.06f)
            )
            drawCircle(
                color = ArcadeCoralSoft.copy(alpha = 0.46f),
                radius = size.minDimension * 0.46f,
                center = center,
                style = Stroke(width = size.minDimension * 0.035f)
            )
            repeat(10) { index ->
                val angle = (index / 10f) * 2f * PI.toFloat()
                val startRadius = size.minDimension * 0.24f
                val endRadius = size.minDimension * 0.52f
                val start = androidx.compose.ui.geometry.Offset(
                    x = center.x + cos(angle) * startRadius,
                    y = center.y + sin(angle) * startRadius
                )
                val end = androidx.compose.ui.geometry.Offset(
                    x = center.x + cos(angle) * endRadius,
                    y = center.y + sin(angle) * endRadius
                )
                drawLine(
                    color = ArcadeGold.copy(alpha = 0.58f),
                    start = start,
                    end = end,
                    strokeWidth = size.minDimension * 0.025f,
                    cap = StrokeCap.Round
                )
            }
        }
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
    val onCombo: () -> Unit = {},
    val onCountdown: () -> Unit = {},
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
internal fun rememberGameSoundHooks(
    isSoundEnabled: Boolean
): GameSoundHooks {
    val context = LocalContext.current
    val soundController = remember(context) { GameSoundController(context) }

    LaunchedEffect(soundController, isSoundEnabled) {
        soundController.isEnabled = isSoundEnabled
    }

    DisposableEffect(soundController) {
        onDispose { soundController.release() }
    }

    return remember(soundController, isSoundEnabled) {
        GameSoundHooks(
            onHit = { soundController.play(GameSoundEffect.Hit) },
            onMiss = { soundController.play(GameSoundEffect.Miss) },
            onCombo = { soundController.play(GameSoundEffect.Combo) },
            onCountdown = { soundController.play(GameSoundEffect.Countdown) },
            onGameOver = { soundController.play(GameSoundEffect.GameOver) }
        )
    }
}
