package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

// ─── Data ───────────────────────────────────────────────────────────

data class PrayerInfo(
    val key: String,
    val nameAr: String,
    val timeAr: String,
    val icon: String,
    val color: Color,
    val points: Int = 200
)

private val prayers = listOf(
    PrayerInfo("fajr",   "الفجر",   "قبل الشروق", "🌙", Color(0xFF7986CB), 200),
    PrayerInfo("dhuhr",  "الظهر",   "بعد الزوال",  "☀️", Color(0xFFFFA726), 200),
    PrayerInfo("asr",    "العصر",   "العصر",        "🌤", Color(0xFF26C6DA), 200),
    PrayerInfo("maghrib","المغرب",  "عند الغروب",  "🌅", Color(0xFFEF5350), 200),
    PrayerInfo("isha",   "العشاء",  "بعد الغروب",  "🌟", Color(0xFF66BB6A), 200)
)

private val endOfDayDuas = listOf(
    "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلَّا أَنْتَ",
    "رَبَّنَا تَقَبَّلْ مِنَّا إِنَّكَ أَنْتَ السَّمِيعُ الْعَلِيمُ",
    "اللَّهُمَّ بَارِكْ لَنَا فِيمَا رَزَقْتَنَا، وَقِنَا عَذَابَ النَّارِ",
    "اللَّهُمَّ اجْعَلْنَا مِنَ الَّذِينَ يُقِيمُونَ الصَّلَاةَ وَيُؤْتُونَ الزَّكَاةَ وَهُمْ بِالْآخِرَةِ هُمْ يُوقِنُونَ"
)

private fun getTodayKey(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

private const val PREFS_NAME = "prayer_points_sovereign"

// ─── Main Screen ─────────────────────────────────────────────────────────

@Composable
fun PrayerPointsScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isAr by viewModel.isArabic.collectAsState()

    val prefs = remember { context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE) }
    val todayKey = remember { getTodayKey() }

    val completedToday = remember {
        mutableStateMapOf<String, Boolean>().apply {
            prayers.forEach { p ->
                put(p.key, prefs.getBoolean("${todayKey}_${p.key}", false))
            }
        }
    }

    val totalPoints = remember { mutableIntStateOf(prefs.getInt("total_points", 0)) }
    val streak = remember { mutableIntStateOf(prefs.getInt("streak", 0)) }
    val lastCompleteDate = remember { prefs.getString("last_complete_date", "") ?: "" }

    val completedCount = completedToday.count { it.value }
    val allDone = completedCount == 5
    val todayPoints = completedToday.count { it.value } * 200 + if (allDone) 300 else 0

    fun markPrayer(key: String, done: Boolean) {
        completedToday[key] = done
        prefs.edit().putBoolean("${todayKey}_${key}", done).apply()

        val newTotal = prayers.count { completedToday[it.key] == true } * 200 +
            (if (completedToday.count { it.value } == 5) 300 else 0)
        totalPoints.intValue = prefs.getInt("total_points", 0) +
            (if (done) 200 else -200).coerceAtLeast(-prefs.getInt("total_points", 0))
        prefs.edit().putInt("total_points", totalPoints.intValue.coerceAtLeast(0)).apply()

        if (completedToday.count { it.value } == 5 && lastCompleteDate != todayKey) {
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(System.currentTimeMillis() - 86_400_000L))
            val newStreak = if (lastCompleteDate == yesterday) streak.intValue + 1 else 1
            streak.intValue = newStreak
            prefs.edit()
                .putInt("streak", newStreak)
                .putString("last_complete_date", todayKey)
                .apply()
        }
    }

    val animProgress by animateFloatAsState(
        targetValue = completedCount / 5f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "prayerPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val dailyDua = remember { endOfDayDuas[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % endOfDayDuas.size] }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF040B18), VoidBlack)))
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Header ───────────────────────────────────────────────────────
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("نقاط الصلاة", color = AmberZen, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text("صلِّ واكسب النقاط • ابنِ عادة يومية", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                }
            }

            // ── Progress Circle + Stats ─────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Progress
                    Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 10.dp.toPx()
                            drawArc(
                                color = AmberZen.copy(alpha = 0.1f),
                                startAngle = -90f, sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round)
                            )
                            if (animProgress > 0f) {
                                drawArc(
                                    brush = Brush.sweepGradient(listOf(AmberZen, Color(0xFFFF8F00), AmberZen)),
                                    startAngle = -90f, sweepAngle = animProgress * 360f,
                                    useCenter = false,
                                    style = Stroke(stroke, cap = StrokeCap.Round)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$completedCount/5", color = AmberZen, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                            Text("صلاة", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }

                    // Stats
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatBox("$todayPoints", "نقاط اليوم", AmberZen)
                        StatBox("${totalPoints.intValue}", "إجمالي النقاط", CyberCyan)
                        StatBox("${streak.intValue} 🔥", "أيام متتالية", Color(0xFFEF5350))
                    }
                }
            }

            // ── All Done Banner ─────────────────────────────────────────────────
            if (allDone) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AmberZen.copy(alpha = pulseAlpha), RoundedCornerShape(14.dp))
                            .background(AmberZen.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎉 ما شاء الله!", color = AmberZen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("أتممتَ صلواتك اليوم كاملةً", color = Color.White, fontSize = 14.sp)
                            Text("+300 نقطة مكافأة إتمام", color = AmberZen, fontSize = 12.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                dailyDua,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ── Prayer Cards ────────────────────────────────────────────────────
            item {
                Text("صلوات اليوم", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            items(prayers.size) { idx ->
                val prayer = prayers[idx]
                val isDone = completedToday[prayer.key] == true
                PrayerCard(
                    prayer = prayer,
                    isDone = isDone,
                    pulseAlpha = pulseAlpha,
                    onToggle = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        markPrayer(prayer.key, !isDone)
                    }
                )
            }

            // ── Encouragement ──────────��────────────────────────────────────────
            item {
                val remaining = 5 - completedCount
                val message = when {
                    allDone -> "بارك الله فيك. صلواتك نور في قلبك ودنياك وآخرتك."
                    remaining == 4 -> "بدأتَ رحلة اليوم. الفجر أشرق وانتظر صلاتك 🌙"
                    remaining == 3 -> "أنت في منتصف الطريق. استمر! كل صلاة تقربك من الله أكثر"
                    remaining == 2 -> "رائع! صلاتان تبقّيان. أتممها وانل المكافأة الكبرى"
                    remaining == 1 -> "صلاة واحدة فقط! أتممها وستنال +300 نقطة مكافأة ✨"
                    else -> "صلِّ واحفظ. كل صلاة تُكتب لك عند الله بإذنه."
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                        .border(0.7.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(message, color = Color.White.copy(alpha = 0.65f), fontSize = 13.sp, lineHeight = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }

            // ── Quranic Verse ───────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.8.dp, AmberZen.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        .background(AmberZen.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("﴿ إِنَّ الصَّلَاةَ تَنْهَى عَنِ الْفَحْشَاءِ وَالْمُنكَرِ ﴾", color = AmberZen.copy(alpha = 0.9f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("سورة العنكبوت — الآية 45", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, color: Color) {
    Column(
        modifier = Modifier
            .border(0.7.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
    }
}

@Composable
private fun PrayerCard(prayer: PrayerInfo, isDone: Boolean, pulseAlpha: Float, onToggle: () -> Unit) {
    val animBg by animateColorAsState(
        targetValue = if (isDone) prayer.color.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.02f),
        animationSpec = tween(400), label = "prayerBg"
    )
    val animBorder by animateColorAsState(
        targetValue = if (isDone) prayer.color.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
        animationSpec = tween(400), label = "prayerBorder"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, animBorder, RoundedCornerShape(14.dp))
            .background(animBg, RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Prayer icon circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(prayer.color.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, prayer.color.copy(alpha = if (isDone) 0.7f else 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(prayer.icon, fontSize = 20.sp)
            }

            Column {
                Text(prayer.nameAr, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(prayer.timeAr, color = prayer.color.copy(alpha = 0.7f), fontSize = 11.sp)
                Spacer(Modifier.height(2.dp))
                Text("+${prayer.points} نقطة", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
            }
        }

        // Checkbox
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.5.dp, if (isDone) prayer.color else Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .background(if (isDone) prayer.color.copy(alpha = 0.2f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Text("✓", color = prayer.color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
