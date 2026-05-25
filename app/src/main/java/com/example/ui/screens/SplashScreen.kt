package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class QuranicVerse(val arabic: String, val reference: String)

private val quranicVerses = listOf(
    QuranicVerse("وَيَنْصُرَكَ اللَّهُ نَصْرًا عَزِيزًا", "الفتح [ 48:3 ]"),
    QuranicVerse("إِنَّ اللَّهَ مَعَ الصَّابِرِينَ", "البقرة [ 2:153 ]"),
    QuranicVerse("وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ", "الطلاق [ 65:3 ]"),
    QuranicVerse("فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", "الشرح [ 94:5 ]"),
    QuranicVerse("وَاللَّهُ خَيْرُ الْحَافِظِينَ", "يوسف [ 12:64 ]"),
    QuranicVerse("رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي", "طه [ 20:25-26 ]"),
    QuranicVerse("وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ", "الضحى [ 93:5 ]"),
    QuranicVerse("إِنَّا فَتَحْنَا لَكَ فَتْحًا مُّبِينًا", "الفتح [ 48:1 ]"),
    QuranicVerse("وَكَفَىٰ بِاللَّهِ وَلِيًّا وَكَفَىٰ بِاللَّهِ نَصِيرًا", "النساء [ 4:45 ]"),
    QuranicVerse("وَاللَّهُ يُحِبُّ الْمُحْسِنِينَ", "آل عمران [ 3:134 ]"),
    QuranicVerse("قُلْ لَن يُصِيبَنَا إِلَّا مَا كَتَبَ اللَّهُ لَنَا", "التوبة [ 9:51 ]"),
    QuranicVerse("وَمَا النَّصْرُ إِلَّا مِنْ عِندِ اللَّهِ", "آل عمران [ 3:126 ]")
)

private data class Particle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float,
    val phase: Float
)

@Composable
fun SplashScreen(onNavigateToDashboard: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val shieldScale = remember { Animatable(0.4f) }
    val logs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()
    var verseVisible by remember { mutableStateOf(false) }
    var neuralUnlocked by remember { mutableStateOf(false) }

    val selectedVerse = remember { quranicVerses[Random.nextInt(quranicVerses.size)] }

    val particles = remember {
        List(30) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 2f + 0.5f,
                speed = Random.nextFloat() * 0.3f + 0.1f,
                alpha = Random.nextFloat() * 0.4f + 0.05f,
                phase = Random.nextFloat() * 2f * PI.toFloat()
            )
        }
    }

    val systemChecks = listOf(
        "[OK] CORE_INIT // SOVEREIGN ENGINE START",
        "[OK] HW_ENCRYPT // BIOMETRIC SHIELD SYNCING",
        "[OK] SAT_GRID // PRAYER TIMES CALIBRATION",
        "[OK] VAULT_BOOT // DECRYPTING DESIGN BLUEPRINTS",
        "[OK] NEURAL_HUD // CALIBRATING SENSORS",
        "[OK] CYBER_SYNC // ESTABLISHING AI UPLINK",
        "[OK] SECURITY_CHECK // OPTIMIZING FIREWALLS",
        "[OK] SYSTEM_READY // ASYRIA V4 STABLE",
        "[OK] SYSTEM_OPTIMIZED // ALL CORES LOCKED"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glow")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )
    val shieldPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "shieldPulse"
    )
    val scanLine by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "scanLine"
    )
    val neonPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "neonPulse"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "ringRotation"
    )
    val breathRing by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathRing"
    )
    val particleTime by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(60000, easing = LinearEasing), RepeatMode.Restart),
        label = "particleTime"
    )
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "wavePhase"
    )

    LaunchedEffect(Unit) {
        shieldScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f))
        alpha.animateTo(1f, animationSpec = tween(800))
        delay(200)
        verseVisible = true
        for (check in systemChecks) {
            logs.add("> $check")
            if (logs.size > 0) listState.animateScrollToItem(logs.size - 1)
            delay(300)
        }
        delay(400)
        neuralUnlocked = true
        delay(800)
        onNavigateToDashboard()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF020E1A), Color(0xFF000509)),
                    radius = 1800f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Background grid
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.08f)) {
            val gridSpacing = 30.dp.toPx()
            var x = 0f
            while (x <= size.width) {
                drawLine(Color.Cyan, Offset(x, 0f), Offset(x, size.height), strokeWidth = 0.5f)
                x += gridSpacing
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(Color.Cyan, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5f)
                y += gridSpacing
            }
        }

        // Floating particles
        Canvas(modifier = Modifier.fillMaxSize().alpha(alpha.value)) {
            particles.forEach { p ->
                val time = particleTime
                val px = ((p.x + sin(time * p.speed * 0.01f + p.phase) * 0.05f) % 1f) * size.width
                val py = ((p.y - (time * p.speed * 0.001f) % 1f + 1f) % 1f) * size.height
                val pAlpha = p.alpha * (0.5f + 0.5f * sin(time * 0.05f + p.phase))
                drawCircle(
                    color = CyberCyan.copy(alpha = pAlpha.coerceIn(0f, 1f)),
                    radius = p.radius.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Corner frame brackets
        Canvas(modifier = Modifier.fillMaxSize().alpha(alpha.value * 0.6f)) {
            val strokeWidth = 1.5.dp.toPx()
            val cornerSize = 50.dp.toPx()
            val pad = 16.dp.toPx()
            val framePath = Path().apply {
                moveTo(pad + cornerSize, pad); lineTo(pad, pad); lineTo(pad, pad + cornerSize)
                moveTo(pad, size.height - pad - cornerSize); lineTo(pad, size.height - pad); lineTo(pad + cornerSize, size.height - pad)
                moveTo(size.width - pad - cornerSize, size.height - pad); lineTo(size.width - pad, size.height - pad); lineTo(size.width - pad, size.height - pad - cornerSize)
                moveTo(size.width - pad, pad + cornerSize); lineTo(size.width - pad, pad); lineTo(size.width - pad - cornerSize, pad)
            }
            drawPath(framePath, CyberCyan, style = Stroke(strokeWidth, cap = StrokeCap.Round))
        }

        // Gradient wave at bottom
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .alpha(alpha.value * 0.35f)
        ) {
            val waveHeight = size.height
            val waveWidth = size.width
            for (layer in 0..2) {
                val layerPhase = wavePhase + layer * 1.2f
                val layerAlpha = 0.15f - layer * 0.04f
                val path = Path().apply {
                    moveTo(0f, waveHeight)
                    for (i in 0..waveWidth.toInt() step 4) {
                        val xPos = i.toFloat()
                        val yPos = waveHeight * 0.5f + sin(xPos * 0.015f + layerPhase + layer) * (15f + layer * 8f)
                        lineTo(xPos, yPos)
                    }
                    lineTo(waveWidth, waveHeight)
                    close()
                }
                drawPath(
                    path,
                    Brush.verticalGradient(
                        colors = listOf(CyberCyan.copy(alpha = layerAlpha), Color.Transparent)
                    )
                )
            }
        }

        // Main content
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App name badge
            Box(
                modifier = Modifier
                    .alpha(alpha.value)
                    .border(0.5.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .background(CyberCyan.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "A.SYRIA SOVEREIGN OS v4.0.0",
                    color = CyberCyan.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Shield with breathing glow
            Box(
                modifier = Modifier.size(180.dp).scale(shieldScale.value * shieldPulse),
                contentAlignment = Alignment.Center
            ) {
                // Breathing glow ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val breathRadius = size.minDimension / 2f + 4.dp.toPx() + breathRing * 6.dp.toPx()
                    val breathAlpha = 0.08f + breathRing * 0.18f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CyberCyan.copy(alpha = breathAlpha),
                                CyberCyan.copy(alpha = breathAlpha * 0.3f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = breathRadius
                        ),
                        radius = breathRadius,
                        center = center
                    )
                    drawCircle(
                        color = CyberCyan.copy(alpha = 0.12f * (1f - breathRing)),
                        radius = breathRadius + 4.dp.toPx(),
                        center = center,
                        style = Stroke(0.8.dp.toPx())
                    )
                }

                // Outer rotating rings
                Canvas(modifier = Modifier.size(160.dp)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val outerRadius = size.minDimension / 2f - 4.dp.toPx()
                    drawCircle(
                        color = CyberCyan.copy(alpha = 0.2f * glowAlpha),
                        radius = outerRadius, style = Stroke(1.5.dp.toPx())
                    )
                    drawCircle(
                        color = CyberCyan.copy(alpha = 0.06f),
                        radius = outerRadius - 8.dp.toPx(),
                        style = Stroke(0.8.dp.toPx())
                    )
                    val sweepAngle = 60f
                    listOf(0f, 120f, 240f).forEach { startAngle ->
                        drawArc(
                            color = CyberCyan.copy(alpha = 0.7f * glowAlpha),
                            startAngle = startAngle + ringRotation,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
                            size = Size(outerRadius * 2, outerRadius * 2),
                            topLeft = Offset(center.x - outerRadius, center.y - outerRadius)
                        )
                    }
                    // Counter-rotating inner arcs
                    val innerRadius = outerRadius - 14.dp.toPx()
                    listOf(30f, 150f, 270f).forEach { startAngle ->
                        drawArc(
                            color = CyberCyan.copy(alpha = 0.3f * glowAlpha),
                            startAngle = startAngle - ringRotation * 0.5f,
                            sweepAngle = 40f,
                            useCenter = false,
                            style = Stroke(1.dp.toPx(), cap = StrokeCap.Round),
                            size = Size(innerRadius * 2, innerRadius * 2),
                            topLeft = Offset(center.x - innerRadius, center.y - innerRadius)
                        )
                    }
                }

                // Shield shape
                Canvas(modifier = Modifier.size(100.dp)) {
                    val shieldPath = Path().apply {
                        val w = size.width; val h = size.height
                        moveTo(w / 2f, 0f)
                        lineTo(w * 0.9f, h * 0.2f)
                        lineTo(w * 0.9f, h * 0.55f)
                        cubicTo(w * 0.9f, h * 0.8f, w / 2f, h, w / 2f, h)
                        cubicTo(w / 2f, h, w * 0.1f, h * 0.8f, w * 0.1f, h * 0.55f)
                        lineTo(w * 0.1f, h * 0.2f)
                        close()
                    }
                    drawPath(shieldPath, CyberCyan.copy(alpha = 0.12f))
                    drawPath(shieldPath, CyberCyan.copy(alpha = 0.8f * glowAlpha), style = Stroke(2.dp.toPx()))

                    val cx = size.width / 2; val cy = size.height / 2
                    drawLine(CyberCyan.copy(alpha = 0.6f), Offset(cx, cy - 22f), Offset(cx, cy + 22f), strokeWidth = 1.5f)
                    drawLine(CyberCyan.copy(alpha = 0.6f), Offset(cx - 22f, cy), Offset(cx + 22f, cy), strokeWidth = 1.5f)
                    drawCircle(CyberCyan.copy(alpha = 0.5f * glowAlpha), radius = 8f, center = Offset(cx, cy), style = Stroke(1.5f))
                    drawCircle(CyberCyan.copy(alpha = 0.25f), radius = 16f, center = Offset(cx, cy), style = Stroke(0.8f))
                }

                // Scan line
                Canvas(modifier = Modifier.size(100.dp)) {
                    val scanY = size.height * scanLine
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, CyberCyan.copy(alpha = 0.5f), Color.Transparent)
                        ),
                        start = Offset(0f, scanY), end = Offset(size.width, scanY),
                        strokeWidth = 1.5f
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quranic verse with crossfade
            AnimatedVisibility(
                visible = verseVisible,
                enter = fadeIn(animationSpec = tween(1200)),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    AmberZen.copy(alpha = 0.1f),
                                    AmberZen.copy(alpha = 0.45f * glowAlpha),
                                    AmberZen.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    AmberZen.copy(alpha = 0.06f),
                                    AmberZen.copy(alpha = 0.02f)
                                )
                            ),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            AmberZen.copy(alpha = 0.5f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "\u2756",
                            color = AmberZen.copy(alpha = 0.4f * glowAlpha),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedVerse.arabic,
                            color = AmberZen.copy(alpha = glowAlpha),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = AmberZen.copy(alpha = 0.5f),
                                    blurRadius = 20f
                                )
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\u2756",
                            color = AmberZen.copy(alpha = 0.4f * glowAlpha),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            AmberZen.copy(alpha = 0.5f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selectedVerse.reference,
                            color = AmberZen.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // System diagnostics log
            Box(
                modifier = Modifier
                    .alpha(alpha.value)
                    .fillMaxWidth()
                    .height(80.dp)
                    .border(
                        width = 0.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                CyberCyan.copy(alpha = 0.08f),
                                CyberCyan.copy(alpha = 0.2f),
                                CyberCyan.copy(alpha = 0.08f)
                            )
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    items(logs) { log ->
                        val isSuccess = log.contains("READY") || log.contains("STABLE") || log.contains("OPTIMIZED")
                        val isOk = log.contains("[OK]")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isOk) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(
                                            if (isSuccess) CyberCyan.copy(alpha = 0.9f)
                                            else Color(0xFF2ECC71).copy(alpha = 0.6f),
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = log,
                                color = (if (isSuccess) CyberCyan else Color.White).copy(
                                    alpha = if (isSuccess) 0.8f else 0.4f
                                ),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 0.5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom section: Neural Unlock + Signature
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = neuralUnlocked,
                enter = fadeIn(animationSpec = tween(600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Canvas(modifier = Modifier.size(12.dp)) {
                            val w = size.width; val h = size.height
                            val miniShield = Path().apply {
                                moveTo(w / 2f, 0f)
                                lineTo(w * 0.85f, h * 0.2f)
                                lineTo(w * 0.85f, h * 0.55f)
                                cubicTo(w * 0.85f, h * 0.8f, w / 2f, h, w / 2f, h)
                                cubicTo(w / 2f, h, w * 0.15f, h * 0.8f, w * 0.15f, h * 0.55f)
                                lineTo(w * 0.15f, h * 0.2f)
                                close()
                            }
                            drawPath(miniShield, CyberCyan.copy(alpha = 0.7f), style = Stroke(1.dp.toPx()))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NEURAL INTERFACE UNLOCKED",
                            color = CyberCyan.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(0.5.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        CyberCyan.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Column(
                modifier = Modifier.alpha(alpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DESIGNED BY",
                    color = Color.White.copy(alpha = 0.25f * neonPulseAlpha),
                    fontSize = 7.sp,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "ABOUDA.AL.SHEKH.YOSSEF",
                    color = CyberCyan.copy(alpha = 0.5f * neonPulseAlpha),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 4.sp,
                    fontFamily = FontFamily.Monospace,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = CyberCyan.copy(alpha = 0.4f),
                            blurRadius = 10f
                        )
                    )
                )
            }
        }
    }
}
