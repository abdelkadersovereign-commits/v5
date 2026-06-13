package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.work.*
import com.example.worker.NotificationWorker
import java.util.concurrent.TimeUnit
import com.example.adaptive.LocalAdaptiveConfig
import com.example.ui.screens.* 
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.AmberZen
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay

class MainActivity : FragmentActivity() {
  private var isSessionAuthenticated = false
  private var isAuthInProgress = false
  private var hasRequestedPermissions = false

  private val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { /* Permissions handled */ }

  private fun requestEssentialPermissions() {
    val needed = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        needed.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    if (needed.isNotEmpty()) {
        permissionLauncher.launch(needed.toTypedArray())
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES).build()
    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork("sovereign_pulse", ExistingPeriodicWorkPolicy.UPDATE, workRequest)

    setContent {
      val vm: DashboardViewModel = viewModel()
      val prayerVm: com.example.ui.viewmodel.PrayerViewModel = viewModel()
      val uiConfig by vm.uiConfig.collectAsState()

      MyApplicationTheme(darkTheme = true, dynamicColor = false, uiConfig = uiConfig) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          val navController = rememberNavController()
          val isAr by vm.isArabic.collectAsState()
          val calibrationCompleted by vm.calibrationCompleted.collectAsState()
            val onboardingCompleted by vm.onboardingCompleted.collectAsState()
          val customApiKey by vm.customApiKey.collectAsState()

          fun triggerBiometricAuth(title: String, subtitle: String, onSuccess: () -> Unit, closeOnCancel: Boolean = false) {
            if (isAuthInProgress) return
            val biometricManager = BiometricManager.from(this@MainActivity)
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) != BiometricManager.BIOMETRIC_SUCCESS) {
              isSessionAuthenticated = true; onSuccess(); return
            }
            isAuthInProgress = true
            val executor = ContextCompat.getMainExecutor(this@MainActivity)
            val biometricPrompt = BiometricPrompt(this@MainActivity, executor,
              object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                  isAuthInProgress = false
                  if ((errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) && closeOnCancel) finish()
                }
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                  isAuthInProgress = false; isSessionAuthenticated = true; onSuccess()
                }
              })
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
              .setTitle(title).setSubtitle(subtitle)
              .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL).build()
            biometricPrompt.authenticate(promptInfo)
          }

          DisposableEffect(Unit) {
            val observer = LifecycleEventObserver { _, event ->
              when (event) {
                Lifecycle.Event.ON_START -> {
                  if (!isSessionAuthenticated && !isAuthInProgress) {
                    triggerBiometricAuth(if (isAr) "تأكيد الهوية" else "Identity Verified", if (isAr) "مطلوب بصمة الدخول" else "Biometric uplink required", { }, true)
                  }
                }
                Lifecycle.Event.ON_RESUME -> {
                  vm.startSensors()
                  if (isSessionAuthenticated) {
                    prayerVm.updateLocation()
                    if (!hasRequestedPermissions) { hasRequestedPermissions = true; requestEssentialPermissions() }
                  }
                }
                Lifecycle.Event.ON_PAUSE -> vm.stopSensors()
                else -> {}
              }
            }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
          }

          val startDestination = when {
              !onboardingCompleted -> "onboarding"
              customApiKey.isBlank() -> "dashboard"
              !calibrationCompleted -> "calibration"
              else -> "dashboard"
          }

          CompositionLocalProvider(LocalAdaptiveConfig provides uiConfig) {
          NavHost(
            navController = navController, 
            startDestination = "splash",
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
          ) {
            composable("splash") {
                SplashScreen(onNavigateToDashboard = {
                    navController.navigate(startDestination) {
                        popUpTo("splash") { inclusive = true }
                    }
                })
            }
            composable("onboarding") {
                  OnboardingScreen {
                      vm.completeOnboarding()
                      navController.navigate("splash") {
                          popUpTo("onboarding") { inclusive = true }
                      }
                  }
              }
              composable("calibration") {
                CalibrationScreen(viewModel = vm, onCalibrationComplete = {
                    navController.navigate("dashboard") { popUpTo("calibration") { inclusive = true } }
                })
            }
            composable("dashboard") {
              val isAr by vm.isArabic.collectAsState()
              var activeTab by remember { mutableStateOf("home") }

              val isAcademyOpen by vm.isAcademyOpen.collectAsState()
              val isResourcesOpen by vm.isResourcesOpen.collectAsState()

              LaunchedEffect(isAcademyOpen) { if (isAcademyOpen) { activeTab = "academy"; vm.setAcademyOpen(false) } }
              LaunchedEffect(isResourcesOpen) { if (isResourcesOpen) { activeTab = "resources"; vm.setResourcesOpen(false) } }
              LaunchedEffect(Unit) { if (vm.customApiKey.value.isBlank()) { activeTab = "settings"; vm.setSettingsOpen(true) } }
              LaunchedEffect(customApiKey, calibrationCompleted) {
                  if (customApiKey.isNotBlank() && !calibrationCompleted) {
                      navController.navigate("calibration") { popUpTo("dashboard") { inclusive = false } }
                  }
              }

              Scaffold(
                  modifier = Modifier.fillMaxSize(),
                  containerColor = VoidBlack,
                  contentWindowInsets = WindowInsets(0, 0, 0, 0),
                  bottomBar = {
                    CompositionLocalProvider(LocalLayoutDirection provides if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                      BottomNavBar(isAr, activeTab) { newTab ->
                          activeTab = newTab
                          vm.setSettingsOpen(newTab == "settings")
                      }
                    }
                  }
                ) { innerPadding ->
                  Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    when (activeTab) {
                      "home" -> DashboardScreen(
                        viewModel = vm,
                        prayerViewModel = prayerVm,
                        onNavigateToScanner = { navController.navigate("link_scanner") },
                        onVaultLockRequest = { title, sub, onOk -> 
                           triggerBiometricAuth(title, sub, onOk)
                        }
                      )
                      "library"  -> LibraryScreen(viewModel = vm)
                      "prayer"   -> PrayerPointsScreen(viewModel = vm)
                      "academy"  -> AcademyScreen(vm, onNavigateToGlossary = { navController.navigate("glossary") }, onNavigateToChecklist = { navController.navigate("checklist") })
                      "resources"-> ResourcesScreen(vm) { activeTab = "home" }
                      "settings" -> SettingsScreen(vm, { activeTab = "home" }, { navController.navigate("about") }, { navController.navigate("adaptive_feedback") }) { title, sub, onOk -> triggerBiometricAuth(title, sub, onOk) }
                    }
                  }
                }
            }
            composable("link_scanner") { LinkScannerScreen(vm) { navController.popBackStack() } }
            composable("about") { AboutScreen { navController.popBackStack() } }
            composable("glossary") { CyberGlossaryScreen(vm) { navController.popBackStack() } }
            composable("checklist") { SecurityChecklistScreen(vm) { navController.popBackStack() } }
              composable("adaptive_feedback") {
                  val isAr by vm.isArabic.collectAsState()
                  FeedbackScreen(vm, isAr) { navController.popBackStack() }
              }
            } // end NavHost
            } // end CompositionLocalProvider
        }
      }
    }
  }
}

// ─── Bottom Nav Data ───────────────────────────────────────────────────────

private data class NavTab(
    val key: String,
    val labelAr: String,
    val labelEn: String,
    val emoji: String,
    val icon: ImageVector? = null,
    val activeColor: Color = CyberCyan
)

private val navTabs = listOf(
    NavTab("home",      "الرئيسية",  "Home",      "🏠", Icons.Default.Home,     CyberCyan),
    NavTab("library",   "المكتبة",   "Library",   "📚", null,                   AmberZen),
    NavTab("prayer",    "الصلاة",    "Prayer",    "🕌", null,                   Color(0xFF81C784)),
    NavTab("academy",   "الأكاديمية","Academy",   "🎓", Icons.Default.Check,    CyberCyan),
    NavTab("resources", "المصادر",   "Resources", "🔍", Icons.Default.Search,   Color(0xFF64B5F6)),
    NavTab("settings",  "الإعدادات", "Settings",  "⚙️", Icons.Default.Settings, Color(0xFFB0BEC5))
)

// ─── Scrollable Bottom Nav with Swipe Hint ────────────────────────────────────

@Composable
private fun BottomNavBar(isAr: Boolean, activeTab: String, onTabSelected: (String) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    // One-time swipe hint animation: pulses the hint arrow for 3 seconds on first open
    var showSwipeHint by remember { mutableStateOf(true) }
    val hintAlpha by animateFloatAsState(
        targetValue = if (showSwipeHint) 1f else 0f,
        animationSpec = tween(600),
        label = "hintAlpha"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "navPulse")
    val hintPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hintPulse"
    )

    LaunchedEffect(Unit) {
        delay(3000)
        showSwipeHint = false
    }

    // Auto-scroll to show active tab
    LaunchedEffect(activeTab) {
        val tabIndex = navTabs.indexOfFirst { it.key == activeTab }
        if (tabIndex >= 4) {
            scrollState.animateScrollTo(scrollState.maxValue)
        } else if (tabIndex <= 1) {
            scrollState.animateScrollTo(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF04070D).copy(alpha = 0.0f), Color(0xFF04070D).copy(alpha = 0.98f))
                )
            )
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Scrollable nav row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navTabs.forEach { tab ->
                val isSelected = activeTab == tab.key
                NavItem(
                    tab = tab,
                    isSelected = isSelected,
                    labelText = if (isAr) tab.labelAr else tab.labelEn,
                    onSelect = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onTabSelected(tab.key)
                    }
                )
                if (tab.key != navTabs.last().key) {
                    Spacer(Modifier.width(4.dp))
                }
            }
        }

        // Right-side gradient fade — indicates more content to the right
        val canScrollRight = scrollState.value < scrollState.maxValue
        if (canScrollRight) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFF04070D).copy(alpha = 0.95f))
                        )
                    )
            )
        }

        // Left-side gradient fade — indicates content to the left
        val canScrollLeft = scrollState.value > 0
        if (canScrollLeft) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(32.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF04070D).copy(alpha = 0.95f), Color.Transparent)
                        )
                    )
            )
        }

        // Swipe hint overlay — shown first 3 seconds
        if (showSwipeHint || hintAlpha > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp, bottom = 4.dp)
                    .alpha(hintAlpha * hintPulse)
                    .border(0.7.dp, AmberZen.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .background(Color(0xFF04070D).copy(alpha = 0.9f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "← اسحب",
                    color = AmberZen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NavItem(tab: NavTab, isSelected: Boolean, labelText: String, onSelect: () -> Unit) {
    val animBg by animateColorAsState(
        targetValue = if (isSelected) tab.activeColor.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(250), label = "navBg_${tab.key}"
    )
    val animBorder by animateColorAsState(
        targetValue = if (isSelected) tab.activeColor.copy(alpha = 0.45f) else Color.Transparent,
        animationSpec = tween(250), label = "navBorder_${tab.key}"
    )
    val animLabelColor by animateColorAsState(
        targetValue = if (isSelected) tab.activeColor else Color.White.copy(alpha = 0.4f),
        animationSpec = tween(250), label = "navLabel_${tab.key}"
    )

    Column(
        modifier = Modifier
            .width(68.dp)
            .border(0.8.dp, animBorder, RoundedCornerShape(14.dp))
            .background(animBg, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (tab.icon != null) {
            Icon(
                imageVector = tab.icon,
                contentDescription = labelText,
                tint = animLabelColor,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(tab.emoji, fontSize = 20.sp)
        }
        Text(
            text = labelText,
            color = animLabelColor,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
