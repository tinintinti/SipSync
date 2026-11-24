package com.example.sipsync.screens

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ---- Palette for the splash (pink/peach design) ----
private val SplashPink = Color(0xFFE8457F)
private val SplashRaspberry = Color(0xFFC2366B)
private val SplashOrange = Color(0xFFFF9A62)
private val SplashDeep = Color(0xFF5B1A33)
private val SplashMuted = Color(0xFF7D6470)

private const val WORDMARK = "SipSync"

// Animated launch screen (about 2.9 seconds). Calls onFinished when done.
//
// Logo: a gradient disc with a white drop inside, wrapped by two sync arrows.
// Animation: disc springs in with Wi-Fi style ripples -> drop pops -> arrows draw
// and spin once -> wordmark letters rise one by one -> zoom-through exit.
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val currentOnFinished by rememberUpdatedState(onFinished)

    // Reduced motion: if the system animation scale is 0, show a static splash
    val reducedMotion = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }

    val circle = remember { Animatable(0f) }    // logo disc spring-in
    val rings = remember { Animatable(0f) }     // ripple visibility
    val drop = remember { Animatable(0f) }      // drop pop
    val arcs = remember { Animatable(0f) }      // sync arrows drawing in
    val spin = remember { Animatable(0f) }      // one full turn of the arrows
    val word = remember { Animatable(0f) }      // wordmark letters
    val tagline = remember { Animatable(0f) }   // caption fade
    val progress = remember { Animatable(0f) }  // loading bar
    val exit = remember { Animatable(0f) }      // zoom-through before the next screen

    LaunchedEffect(Unit) {
        if (reducedMotion) {
            circle.snapTo(1f)
            rings.snapTo(0f)
            drop.snapTo(1f)
            arcs.snapTo(1f)
            word.snapTo(1f)
            tagline.snapTo(1f)
            progress.snapTo(1f)
            delay(1200)
            currentOnFinished()
            return@LaunchedEffect
        }

        launch {
            circle.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
            )
        }
        launch { rings.animateTo(1f, tween(600)) }
        launch {
            delay(350)
            drop.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        }
        launch {
            delay(750)
            arcs.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            delay(900)
            spin.animateTo(360f, tween(1200, easing = FastOutSlowInEasing))
        }
        launch {
            delay(1100)
            word.animateTo(1f, tween(700, easing = LinearEasing))
        }
        launch {
            delay(1700)
            tagline.animateTo(1f, tween(500))
        }
        launch { progress.animateTo(1f, tween(2300, easing = LinearEasing)) }

        delay(2450)
        exit.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        currentOnFinished()
    }

    // Endless gentle motion: ripples spreading out and the logo floating a little
    val infinite = rememberInfiniteTransition(label = "splash")
    val ripple by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "ripple"
    )
    val float by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFC9B0), Color(0xFFFDE0D6), Color(0xFFFEF4F0))
                )
            )
    ) {
        // Everything on top of the gradient zooms and fades out at the end,
        // so the screen stays peach (no flash) while the next screen appears.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val s = 1f + 0.12f * exit.value
                    scaleX = s
                    scaleY = s
                    alpha = 1f - exit.value
                }
        ) {
            // ===== Center: logo + wordmark =====
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Canvas(
                    modifier = Modifier
                        .size(260.dp)
                        .graphicsLayer { translationY = float * 5.dp.toPx() }
                ) {
                    val c = center
                    val r = 56.dp.toPx()

                    // Ripples spreading from the logo, like a Wi-Fi signal
                    for (k in 0..1) {
                        val phase = (ripple + k * 0.5f) % 1f
                        drawCircle(
                            color = SplashPink.copy(alpha = (1f - phase) * 0.30f * rings.value),
                            radius = r * 1.1f + phase * r * 1.2f,
                            center = c,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    drawSipSyncLogo(
                        center = c,
                        radius = r,
                        circleScale = circle.value,
                        dropScale = drop.value,
                        arcProgress = arcs.value,
                        rotation = spin.value
                    )
                }

                // Wordmark: letters rise in one after another
                Row(modifier = Modifier.offset(y = (-52).dp)) {
                    WORDMARK.forEachIndexed { i, ch ->
                        Text(
                            text = ch.toString(),
                            color = if (i < 3) SplashDeep else SplashRaspberry,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.graphicsLayer {
                                val p = ((word.value - i * 0.09f) / 0.4f).coerceIn(0f, 1f)
                                alpha = p
                                translationY = (1f - p) * 18.dp.toPx()
                            }
                        )
                    }
                }
            }

            // ===== Bottom: caption + progress bar =====
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Offline inventory, connected directly",
                    color = SplashMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.graphicsLayer { alpha = tagline.value }
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SplashRaspberry.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.value.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(SplashPink, SplashOrange)))
                    )
                }
            }
        }
    }
}

// The SipSync logo: gradient disc, white drop, two sync arrows around it.
private fun DrawScope.drawSipSyncLogo(
    center: Offset,
    radius: Float,
    circleScale: Float,
    dropScale: Float,
    arcProgress: Float,
    rotation: Float
) {
    // Disc with a soft glow
    scale(circleScale, center) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x55E8457F), Color.Transparent),
                center = center,
                radius = radius * 1.9f
            ),
            radius = radius * 1.9f,
            center = center
        )
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(SplashPink, SplashOrange),
                start = Offset(center.x - radius, center.y - radius),
                end = Offset(center.x + radius, center.y + radius)
            ),
            radius = radius,
            center = center
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.22f),
            radius = radius * 0.95f,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }

    // White drop in the middle
    scale(dropScale, center) {
        drawPath(
            path = dropPath(center, w = radius * 0.50f, h = radius * 0.68f),
            color = Color.White
        )
    }

    // Two sync arrows circling the drop
    val arcRadius = radius * 0.74f
    val sweep = 120f * arcProgress.coerceIn(0f, 1f)
    val strokeWidth = 4.dp.toPx()
    rotate(rotation, center) {
        for (start in listOf(200f, 20f)) {
            drawArc(
                color = Color.White,
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                size = Size(arcRadius * 2f, arcRadius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            if (arcProgress > 0.05f) {
                drawArrowHead(center, arcRadius, start + sweep, strokeWidth)
            }
        }
    }
}

// Arrow tip at the end of an arc, pointing clockwise along the circle
private fun DrawScope.drawArrowHead(center: Offset, radius: Float, angleDeg: Float, strokeWidth: Float) {
    val a = angleDeg * (Math.PI / 180.0)
    val px = center.x + radius * cos(a).toFloat()
    val py = center.y + radius * sin(a).toFloat()
    val tx = (-sin(a)).toFloat() // clockwise tangent
    val ty = cos(a).toFloat()
    val len = 9.dp.toPx()
    for (wingDeg in listOf(150.0, -150.0)) {
        val w = wingDeg * (Math.PI / 180.0)
        val wx = (tx * cos(w) - ty * sin(w)).toFloat()
        val wy = (tx * sin(w) + ty * cos(w)).toFloat()
        drawLine(
            color = Color.White,
            start = Offset(px, py),
            end = Offset(px + wx * len, py + wy * len),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun dropPath(c: Offset, w: Float, h: Float): Path = Path().apply {
    val top = c.y - h * 0.5f
    val bottom = c.y + h * 0.5f
    moveTo(c.x, top)
    cubicTo(c.x + w * 0.10f, top + h * 0.25f, c.x + w * 0.50f, c.y - h * 0.02f, c.x + w * 0.50f, c.y + h * 0.20f)
    cubicTo(c.x + w * 0.50f, c.y + h * 0.40f, c.x + w * 0.28f, bottom, c.x, bottom)
    cubicTo(c.x - w * 0.28f, bottom, c.x - w * 0.50f, c.y + h * 0.40f, c.x - w * 0.50f, c.y + h * 0.20f)
    cubicTo(c.x - w * 0.50f, c.y - h * 0.02f, c.x - w * 0.10f, top + h * 0.25f, c.x, top)
    close()
}