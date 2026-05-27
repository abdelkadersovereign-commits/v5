package com.example.ui.screens

  import androidx.compose.animation.core.*
  import androidx.compose.foundation.ExperimentalFoundationApi
  import androidx.compose.foundation.background
  import androidx.compose.foundation.border
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.pager.HorizontalPager
  import androidx.compose.foundation.pager.rememberPagerState
  import androidx.compose.foundation.shape.CircleShape
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.Lock
  import androidx.compose.material.icons.filled.School
  import androidx.compose.material.icons.filled.Security
  import androidx.compose.material3.Button
  import androidx.compose.material3.ButtonDefaults
  import androidx.compose.material3.Icon
  import androidx.compose.material3.Text
  import androidx.compose.material3.TextButton
  import androidx.compose.runtime.*
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.alpha
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.graphics.Brush
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.graphics.vector.ImageVector
  import androidx.compose.ui.text.font.FontFamily
  import androidx.compose.ui.text.font.FontWeight
  import androidx.compose.ui.text.style.TextAlign
  import androidx.compose.ui.unit.dp
  import androidx.compose.ui.unit.sp
  import com.example.ui.theme.CyberCyan
  import kotlinx.coroutines.launch

  private val CyberAmber = Color(0xFFFFAA00)
  private val CyberGreen = Color(0xFF00FF88)

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  fun OnboardingScreen(onFinish: () -> Unit) {
      val pagerState = rememberPagerState(pageCount = { 3 })
      val scope = rememberCoroutineScope()

      val pulse = rememberInfiniteTransition(label = "pulse")
      val glowAlpha by pulse.animateFloat(
          initialValue = 0.3f, targetValue = 0.7f,
          animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
          label = "glow"
      )

      Box(
          modifier = Modifier
              .fillMaxSize()
              .background(Color(0xFF040D14))
      ) {
          // Cyber grid background
          Box(
              modifier = Modifier
                  .fillMaxSize()
                  .background(
                      Brush.radialGradient(
                          colors = listOf(CyberCyan.copy(alpha = 0.04f), Color.Transparent),
                          radius = 900f
                      )
                  )
          )

          Column(modifier = Modifier.fillMaxSize()) {
              // Skip button
              Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, end = 16.dp), contentAlignment = Alignment.TopEnd) {
                  TextButton(onClick = onFinish) {
                      Text(
                          text = "تخطى / SKIP",
                          color = Color.White.copy(alpha = 0.4f),
                          fontSize = 11.sp,
                          fontFamily = FontFamily.Monospace
                      )
                  }
              }

              // Pages
              HorizontalPager(
                  state = pagerState,
                  modifier = Modifier.weight(1f)
              ) { page ->
                  when (page) {
                      0 -> OnboardingPage(
                          icon = Icons.Default.Security,
                          iconTint = CyberCyan,
                          title = "SOVEREIGN INTELLIGENCE",
                          titleAr = "سيادة الاستخبارات",
                          subtitle = "Your AI-powered cybersecurity advisor. Stay protected, educated, and spiritually grounded — all in one command center.",
                          subtitleAr = "مستشارك الأمني المدعوم بالذكاء الاصطناعي. ابقَ محمياً ومتعلماً وروحانياً في مركز قيادة واحد.",
                          features = listOf("🛡️ AI Security Alerts / تنبيهات أمنية ذكية", "🕌 Prayer Times / مواقيت الصلاة", "🎓 Cyber Academy / الأكاديمية الأمنية"),
                          accentColor = CyberCyan,
                          glowAlpha = glowAlpha
                      )
                      1 -> OnboardingPage(
                          icon = Icons.Default.Lock,
                          iconTint = CyberAmber,
                          title = "API KEY REQUIRED",
                          titleAr = "مفتاح API ضروري",
                          subtitle = "To unlock AI features, you need a free Gemini API key from ai.google.dev",
                          subtitleAr = "لتفعيل ميزات الذكاء الاصطناعي، تحتاج مفتاح Gemini مجاني من ai.google.dev",
                          features = listOf(
                              "1. Visit ai.google.dev / زر ai.google.dev",
                              "2. Create free API key / أنشئ مفتاحاً مجانياً",
                              "⚠️ VPN required in restricted regions / VPN ضروري في بعض الدول"
                          ),
                          accentColor = CyberAmber,
                          glowAlpha = glowAlpha
                      )
                      2 -> OnboardingPage(
                          icon = Icons.Default.School,
                          iconTint = CyberGreen,
                          title = "PERSONALIZED FOR YOU",
                          titleAr = "مخصص لك تماماً",
                          subtitle = "After setup, a calibration wizard will adapt all content to your skill level, interests, and goals.",
                          subtitleAr = "بعد الإعداد، معالج المعايرة يُكيّف كل المحتوى حسب مستواك واهتماماتك وهدفك.",
                          features = listOf(
                              "📊 Skill calibration / معايرة المستوى",
                              "🎯 Goal-based learning / تعلم حسب الهدف",
                              "⚡ Daily XP & streaks / نقاط يومية وسلاسل"
                          ),
                          accentColor = CyberGreen,
                          glowAlpha = glowAlpha
                      )
                  }
              }

              // Bottom — page indicators + button
              Column(
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 24.dp)
                      .padding(bottom = 40.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(20.dp)
              ) {
                  // Dot indicators
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      repeat(3) { idx ->
                          val isActive = pagerState.currentPage == idx
                          val dotColor = when (idx) {
                              0 -> CyberCyan; 1 -> CyberAmber; else -> CyberGreen
                          }
                          Box(
                              modifier = Modifier
                                  .size(if (isActive) 10.dp else 6.dp)
                                  .clip(CircleShape)
                                  .background(if (isActive) dotColor else Color.White.copy(alpha = 0.2f))
                          )
                      }
                  }

                  // Action button
                  Button(
                      onClick = {
                          if (pagerState.currentPage < 2) {
                              scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                          } else {
                              onFinish()
                          }
                      },
                      colors = ButtonDefaults.buttonColors(
                          containerColor = when (pagerState.currentPage) {
                              0 -> CyberCyan.copy(alpha = 0.15f)
                              1 -> CyberAmber.copy(alpha = 0.15f)
                              else -> CyberGreen.copy(alpha = 0.15f)
                          }
                      ),
                      border = androidx.compose.foundation.BorderStroke(
                          1.dp,
                          when (pagerState.currentPage) {
                              0 -> CyberCyan; 1 -> CyberAmber; else -> CyberGreen
                          }
                      ),
                      shape = RoundedCornerShape(12.dp),
                      modifier = Modifier.fillMaxWidth().height(52.dp)
                  ) {
                      Text(
                          text = if (pagerState.currentPage < 2) {
                              "التالي  ▶  NEXT"
                          } else {
                              "ابدأ الآن  ⚡  LAUNCH"
                          },
                          color = when (pagerState.currentPage) {
                              0 -> CyberCyan; 1 -> CyberAmber; else -> CyberGreen
                          },
                          fontSize = 13.sp,
                          fontWeight = FontWeight.Bold,
                          fontFamily = FontFamily.Monospace
                      )
                  }
              }
          }
      }
  }

  @Composable
  private fun OnboardingPage(
      icon: ImageVector,
      iconTint: Color,
      title: String,
      titleAr: String,
      subtitle: String,
      subtitleAr: String,
      features: List<String>,
      accentColor: Color,
      glowAlpha: Float
  ) {
      Column(
          modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 28.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
      ) {
          // Icon with glow ring
          Box(contentAlignment = Alignment.Center) {
              Box(
                  modifier = Modifier
                      .size(100.dp)
                      .background(accentColor.copy(alpha = glowAlpha * 0.08f), CircleShape)
                      .border(1.dp, accentColor.copy(alpha = glowAlpha * 0.4f), CircleShape)
              )
              Icon(
                  imageVector = icon,
                  contentDescription = null,
                  tint = accentColor,
                  modifier = Modifier.size(44.dp)
              )
          }

          Spacer(Modifier.height(24.dp))

          // Title
          Text(
              text = title,
              color = accentColor,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 2.sp,
              textAlign = TextAlign.Center
          )
          Text(
              text = titleAr,
              color = accentColor.copy(alpha = 0.7f),
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 4.dp)
          )

          Spacer(Modifier.height(16.dp))

          // Subtitle — Arabic line
          Text(
              text = subtitleAr,
              color = Color.White.copy(alpha = 0.75f),
              fontSize = 13.sp,
              lineHeight = 20.sp,
              textAlign = TextAlign.Center
          )
          Spacer(Modifier.height(4.dp))
          Text(
              text = subtitle,
              color = Color.White.copy(alpha = 0.45f),
              fontSize = 11.sp,
              lineHeight = 17.sp,
              fontFamily = FontFamily.Monospace,
              textAlign = TextAlign.Center
          )

          Spacer(Modifier.height(28.dp))

          // Features list
          Column(
              modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                  .background(accentColor.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                  .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
              features.forEach { feature ->
                  Text(
                      text = feature,
                      color = Color.White.copy(alpha = 0.8f),
                      fontSize = 12.sp,
                      lineHeight = 16.sp,
                      fontFamily = FontFamily.Monospace
                  )
              }
          }
      }
  }
  