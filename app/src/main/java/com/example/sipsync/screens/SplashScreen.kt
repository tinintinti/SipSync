package com.example.sipsync.screens

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SplashGold = Color(0xFFE3B04B)

// Animated launch screen (about 2.4 seconds). Calls onFinished when done.
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

    val ring = remember { Animatable(0f) }      // expanding background rings
    val mono = remember { Animatable(0f) }      // S & S reveal
    val amp = remember { Animatable(0f) }       // ampersand pop
    val sweep = remember { Animatable(0f) }     // accent line sweep
    val word = remember { Animatable(0f) }      // wordmark fade in
    val progress = remember { Animatable(0f) }  // loading bar
    val exit = remember { Animatable(0f) }      // scale-up before the fade to login

    LaunchedEffect(Unit) {
        if (reducedMotion) {
            ring.snapTo(1f)
            mono.snapTo(1f)
            amp.snapTo(1f)
            sweep.snapTo(1f)
            word.snapTo(1f)
            progress.snapTo(1f)
            delay(1200)
            currentOnFinished()
            return@LaunchedEffect
        }

        launch { ring.animateTo(1f, tween(1400, easing = FastOutSlowInEasing)) }
        launch {
            delay(250)
            mono.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
        launch {
            delay(700)
            amp.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        }
        launch {
            delay(900)
            sweep.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
        launch {
            delay(1100)
            word.animateTo(1f, tween(500))
        }
        launch { progress.animateTo(1f, tween(2100, easing = LinearEasing)) }

        delay(2100)
        exit.animateTo(1f, tween(300))
        currentOnFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF062F2E), Color(0xFF073B3A), Color(0xFF0A5C52))
                )
            )
            .graphicsLayer {
                val s = 1f + 0.06f * exit.value
                scaleX = s
                scaleY = s
            }
    ) {
        // ===== Center: rings + monogram + wordmark =====
        Box(modifier = Modifier.align(Alignment.Center), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(300.dp)) {
                val r = ring.value
                val glowRadius = 150.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x3329C9A8), Color.Transparent),
                        center = center,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    alpha = r
                )
                val stroke = Stroke(width = 1.dp.toPx())
                val grow = 0.55f + 0.45f * r
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f * r),
                    radius = 140.dp.toPx() * grow,
                    style = stroke
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f * r),
                    radius = 100.dp.toPx() * grow,
                    style = stroke
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // S & S monogram
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "S",
                        color = Color.White,
                        fontSize = 96.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-4).sp,
                        modifier = Modifier.graphicsLayer {
                            val m = mono.value
                            alpha = m
                            translationX = -(1f - m) * 28.dp.toPx()
                        }
                    )
                    Text(
                        text = "&",
                        color = SplashGold,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .offset(y = 8.dp)
                            .graphicsLayer {
                                val a = amp.value
                                alpha = a.coerceIn(0f, 1f)
                                scaleX = a
                                scaleY = a
                                rotationZ = (1f - a) * -20f
                            }
                    )
                    Text(
                        text = "S",
                        color = SipMintBright,
                        fontSize = 96.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-4).sp,
                        modifier = Modifier.graphicsLayer {
                            val m = mono.value
                            alpha = m
                            translationX = (1f - m) * 28.dp.toPx()
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Accent line sweep
                Box(
                    modifier = Modifier
                        .width(170.dp * sweep.value.coerceIn(0f, 1f))
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(SipMintBright, SplashGold, Color.Transparent)
                            )
                        )
                )

                Spacer(Modifier.height(24.dp))

                // Wordmark + tagline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        alpha = word.value
                        translationY = (1f - word.value) * 10.dp.toPx()
                    }
                ) {
                    Text(
                        text = "SipSync",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = SipMintSoft)) { append("SIP ") }
                            withStyle(SpanStyle(color = SplashGold)) { append("& ") }
                            withStyle(SpanStyle(color = SipMintSoft)) { append("SYNC") }
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                }
            }
        }

        // ===== Bottom: progress bar + caption =====
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.value.coerceIn(0f, 1f))
                        .background(Brush.horizontalGradient(listOf(SipMintBright, SplashGold)))
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Offline inventory, connected directly",
                color = SipMintSoft.copy(alpha = 0.8f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}