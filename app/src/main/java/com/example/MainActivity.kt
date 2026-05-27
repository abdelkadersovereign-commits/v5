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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.work.*
import com.example.worker.NotificationWorker
import java.util.concurrent.TimeUnit
import com.example.ui.screens.* 
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
      MyApplicationTheme(darkTheme = true, dynamicColor = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          val navController = rememberNavController()
          val vm: DashboardViewModel = viewModel()
          val prayerVm: com.example.ui.viewmodel.PrayerViewModel = viewModel()
          val isAr by vm.isArabic.collectAsState()
          val calibrationCompleted by vm.calibrationCompleted.collectAsState()
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
              customApiKey.isBlank() -> "dashboard"
              !calibrationCompleted -> "calibration"
              else -> "dashboard"
          }

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
              // Mid-session: navigate to calibration when API key is saved for the first time
              LaunchedEffect(customApiKey, calibrationCompleted) {
                  if (customApiKey.isNotBlank() && !calibrationCompleted) {
                      navController.navigate("calibration") { popUpTo("dashboard") { inclusive = false } }
                  }
              }

              CompositionLocalProvider(LocalLayoutDirection provides if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                Scaffold(
                  modifier = Modifier.fillMaxSize(),
                  containerColor = VoidBlack,
                  contentWindowInsets = WindowInsets(0, 0, 0, 0),
                  bottomBar = { BottomNavBar(isAr, activeTab) { newTab -> activeTab = newTab; vm.setSettingsOpen(newTab == "settings") } }
                ) { innerPadding ->
                  Box(modifier = Modifier.padding(innerPadding)) {
                    when (activeTab) {
                      "home" -> DashboardScreen(
                        viewModel = vm,
                        prayerViewModel = prayerVm,
                        onNavigateToScanner = { navController.navigate("link_scanner") },
                        onVaultLockRequest = { title, sub, onOk -> 
                           triggerBiometricAuth(title, sub, onOk)
                        }
                      )
                      "academy" -> AcademyScreen(vm, onNavigateToGlossary = { navController.navigate("glossary") }, onNavigateToChecklist = { navController.navigate("checklist") })
                      "resources" -> ResourcesScreen(vm) { activeTab = "home" }
                      "settings" -> SettingsScreen(vm, { activeTab = "home" }, { navController.navigate("about") }) { title, sub, onOk -> triggerBiometricAuth(title, sub, onOk) }
                    }
                  }
                }
              }
            }
            composable("link_scanner") { LinkScannerScreen(vm) { navController.popBackStack() } }
            composable("about") { AboutScreen { navController.popBackStack() } }
            composable("glossary") { CyberGlossaryScreen(vm) { navController.popBackStack() } }
            composable("checklist") { SecurityChecklistScreen(vm) { navController.popBackStack() } }
          }
        }
      }
    }
  }
}

@Composable
private fun BottomNavBar(isAr: Boolean, activeTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF04070D).copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
    ) {
        val haptic = LocalHapticFeedback.current
        val tabs = listOf(
            "home" to if (isAr) "الرئيسية" else "Home",
            "academy" to if (isAr) "الأكاديمية" else "Academy",
            "resources" to if (isAr) "المصادر" else "Resources",
            "settings" to if (isAr) "الإعدادات" else "Settings"
        )
        val icons = mapOf("home" to Icons.Default.Home, "academy" to Icons.Default.Check, "resources" to Icons.Default.Search, "settings" to Icons.Default.Settings)

        tabs.forEach { (key, title) ->
            NavigationBarItem(
                selected = activeTab == key,
                onClick = { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove); onTabSelected(key) },
                icon = { 
                    Box(contentAlignment = Alignment.Center) {
                        if (activeTab == key) {
                            Box(modifier = Modifier.size(40.dp).background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)))
                        }
                        Icon(icons[key]!!, contentDescription = title, tint = if (activeTab == key) CyberCyan else Color.White.copy(alpha = 0.4f)) 
                    }
                },
                label = { Text(title, color = if (activeTab == key) CyberCyan else Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
        }
    }
}