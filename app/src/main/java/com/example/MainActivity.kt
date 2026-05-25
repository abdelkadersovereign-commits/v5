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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.ui.screens.SettingsScreen
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.AcademyScreen
import com.example.ui.screens.ResourcesScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.LinkScannerScreen
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.CyberGlossaryScreen
import com.example.ui.screens.SecurityChecklistScreen
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

  // Runtime permission launcher
  private val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { _ -> /* Permissions handled */ }

  fun requestEssentialPermissions() {
    val needed = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED) {
        needed.add(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
      != PackageManager.PERMISSION_GRANTED) {
      needed.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    if (needed.isNotEmpty()) {
      permissionLauncher.launch(needed.toTypedArray())
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Sovereign Pulse Protocol - Running every 15 minutes for better precision
    val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
      .setInitialDelay(1, TimeUnit.MINUTES)
      .build()
    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
      "sovereign_pulse",
      ExistingPeriodicWorkPolicy.REPLACE, // Use REPLACE to ensure the new frequency is applied
      workRequest
    )

    setContent {
      MyApplicationTheme(darkTheme = true, dynamicColor = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          val navController = rememberNavController()
          val vm: DashboardViewModel = viewModel()
          val prayerVm: com.example.ui.viewmodel.PrayerViewModel = viewModel()
          val isAr by vm.isArabic.collectAsState()

          fun triggerBiometricAuth(title: String, subtitle: String, onSuccess: () -> Unit, closeOnCancel: Boolean = false) {
            if (isAuthInProgress) return

            val biometricManager = BiometricManager.from(this@MainActivity)
            val canAuth = biometricManager.canAuthenticate(
              BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )

            if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
              isSessionAuthenticated = true
              onSuccess()
              return
            }

            isAuthInProgress = true
            val executor = ContextCompat.getMainExecutor(this@MainActivity)
            val biometricPrompt = BiometricPrompt(this@MainActivity, executor,
              object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                  super.onAuthenticationError(errorCode, errString)
                  isAuthInProgress = false
                  when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                      if (closeOnCancel) finish()
                      else Toast.makeText(applicationContext, if (isAr) "تم الإلغاء" else "Cancelled", Toast.LENGTH_SHORT).show()
                    }
                    BiometricPrompt.ERROR_CANCELED -> { /* System cancel, wait for next trigger */ }
                    else -> Toast.makeText(applicationContext, "Auth Error: $errString", Toast.LENGTH_SHORT).show()
                  }
                }
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                  super.onAuthenticationSucceeded(result)
                  isAuthInProgress = false
                  isSessionAuthenticated = true
                  onSuccess()
                }
                override fun onAuthenticationFailed() {
                  super.onAuthenticationFailed()
                  // Failed attempt, prompt stays open
                }
              })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
              .setTitle(title)
              .setSubtitle(subtitle)
              .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
              )
              .build()

            biometricPrompt.authenticate(promptInfo)
          }

          fun performSystemLock() {
            triggerBiometricAuth(
              title = if (isAr) "تأكيد الهوية السيادية" else "SOVEREIGN IDENTITY VERIFIED",
              subtitle = if (isAr) "مطلوب بصمة الدخول لفك تشفير النظام" else "Biometric uplink required to decrypt system",
              onSuccess = {
                Toast.makeText(applicationContext, "Neural Interface Unlocked", Toast.LENGTH_SHORT).show()
                // Permissions requested on next ON_RESUME to avoid ActivityResultLauncher state issues
              },
              closeOnCancel = true
            )
          }
          
          DisposableEffect(Unit) {
            val observer = LifecycleEventObserver { _, event ->
              when (event) {
                Lifecycle.Event.ON_START -> {
                  if (!isSessionAuthenticated && !isAuthInProgress) {
                    performSystemLock()
                  }
                }
                Lifecycle.Event.ON_RESUME -> {
                  vm.startSensors()
                  if (isSessionAuthenticated) {
                    prayerVm.updateLocation()
                    if (!hasRequestedPermissions) {
                      hasRequestedPermissions = true
                      requestEssentialPermissions()
                    }
                  }
                }
                Lifecycle.Event.ON_STOP -> { }
                Lifecycle.Event.ON_PAUSE -> vm.stopSensors()
                else -> {}
              }
            }
            lifecycle.addObserver(observer)
            onDispose {
              lifecycle.removeObserver(observer)
            }
          }

          LaunchedEffect(Unit) {
            Toast.makeText(applicationContext, "Neural Link Established", Toast.LENGTH_SHORT).show()
          }

          NavHost(
            navController = navController, 
            startDestination = "splash",
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
          ) {
            composable("splash") {
              SplashScreen(onNavigateToDashboard = {
                navController.navigate("dashboard") {
                  popUpTo("splash") { inclusive = true }
                }
              })
            }
            composable("dashboard") {
              val isAr by vm.isArabic.collectAsState()
              var activeTab by remember { mutableStateOf("home") }
              val haptic = LocalHapticFeedback.current

              val isAcademyOpen by vm.isAcademyOpen.collectAsState()
              val isResourcesOpen by vm.isResourcesOpen.collectAsState()
              val isSettingsOpen by vm.isSettingsOpen.collectAsState()

              LaunchedEffect(isAcademyOpen) {
                if (isAcademyOpen) {
                  activeTab = "academy"
                  vm.setAcademyOpen(false) 
                }
              }

              LaunchedEffect(isResourcesOpen) {
                if (isResourcesOpen) {
                  activeTab = "resources"
                  vm.setResourcesOpen(false) 
                }
              }

              val currentLayoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
              CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection) {
                Scaffold(
                  modifier = Modifier.fillMaxSize(),
                  containerColor = VoidBlack,
                  contentWindowInsets = WindowInsets(0, 0, 0, 0),
                  bottomBar = {
                    val infiniteTransition = rememberInfiniteTransition(label = "bottomBarGlow")
                    val glowAlpha by infiniteTransition.animateFloat(
                      initialValue = 0.1f,
                      targetValue = 0.4f,
                      animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                      ),
                      label = "glowAlpha"
                    )

                    NavigationBar(
                      containerColor = Color(0xFF04070D).copy(alpha = 0.95f),
                      tonalElevation = 8.dp,
                      windowInsets = WindowInsets.navigationBars,
                      modifier = Modifier
                        .border(
                          width = 0.5.dp,
                          color = CyberCyan.copy(alpha = glowAlpha),
                          shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                    ) {
                      NavigationBarItem(
                        selected = activeTab == "home",
                        onClick = {
                          haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                          activeTab = "home"
                          vm.setSettingsOpen(false)
                        },
                        icon = { 
                          Box(contentAlignment = Alignment.Center) {
                            if (activeTab == "home") {
                              Box(
                                modifier = Modifier
                                  .size(40.dp)
                                  .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                  .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                              )
                            }
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = if (activeTab == "home") CyberCyan else Color.White.copy(alpha = 0.4f)) 
                          }
                        },
                        label = {
                          Text(
                            text = if (isAr) "الرئيسية" else "Home",
                            color = if (activeTab == "home") CyberCyan else Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                          )
                        },
                        colors = NavigationBarItemDefaults.colors(
                          indicatorColor = CyberCyan.copy(alpha = 0.12f)
                        )
                      )

                      NavigationBarItem(
                        selected = activeTab == "academy",
                        onClick = {
                          haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                          activeTab = "academy"
                          vm.setSettingsOpen(false)
                        },
                        icon = { 
                          Box(contentAlignment = Alignment.Center) {
                            if (activeTab == "academy") {
                              Box(
                                modifier = Modifier
                                  .size(40.dp)
                                  .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                  .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                              )
                            }
                            Icon(Icons.Default.Check, contentDescription = "Academy", tint = if (activeTab == "academy") CyberCyan else Color.White.copy(alpha = 0.4f)) 
                          }
                        },
                        label = {
                          Text(
                            text = if (isAr) "الأكاديمية" else "Academy",
                            color = if (activeTab == "academy") CyberCyan else Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                          )
                        },
                        colors = NavigationBarItemDefaults.colors(
                          indicatorColor = CyberCyan.copy(alpha = 0.12f)
                        )
                      )

                      NavigationBarItem(
                        selected = activeTab == "resources",
                        onClick = {
                          haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                          activeTab = "resources"
                          vm.setSettingsOpen(false)
                        },
                        icon = { 
                          Box(contentAlignment = Alignment.Center) {
                            if (activeTab == "resources") {
                              Box(
                                modifier = Modifier
                                  .size(40.dp)
                                  .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                  .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                              )
                            }
                            Icon(Icons.Default.Search, contentDescription = "Resources", tint = if (activeTab == "resources") CyberCyan else Color.White.copy(alpha = 0.4f)) 
                          }
                        },
                        label = {
                          Text(
                            text = if (isAr) "المصادر" else "Resources",
                            color = if (activeTab == "resources") CyberCyan else Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                          )
                        },
                        colors = NavigationBarItemDefaults.colors(
                          indicatorColor = CyberCyan.copy(alpha = 0.12f)
                        )
                      )

                      NavigationBarItem(
                        selected = activeTab == "settings",
                        onClick = {
                          haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                          activeTab = "settings"
                          vm.setSettingsOpen(true)
                        },
                        icon = { 
                          Box(contentAlignment = Alignment.Center) {
                            if (activeTab == "settings") {
                              Box(
                                modifier = Modifier
                                  .size(40.dp)
                                  .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                  .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                              )
                            }
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = if (activeTab == "settings") CyberCyan else Color.White.copy(alpha = 0.4f)) 
                          }
                        },
                        label = {
                          Text(
                            text = if (isAr) "الإعدادات" else "Settings",
                            color = if (activeTab == "settings") CyberCyan else Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                          )
                        },
                        colors = NavigationBarItemDefaults.colors(
                          indicatorColor = CyberCyan.copy(alpha = 0.12f)
                        )
                      )
                    }
                  }
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
                      "academy" -> AcademyScreen(
                        viewModel = vm,
                        onNavigateToGlossary = { navController.navigate("glossary") },
                        onNavigateToChecklist = { navController.navigate("checklist") }
                      )
                      "resources" -> ResourcesScreen(
                        viewModel = vm,
                        onClose = { activeTab = "home" }
                      )
                      "settings" -> SettingsScreen(
                        viewModel = vm,
                        onClose = { activeTab = "home" },
                        onOpenAbout = { navController.navigate("about") },
                        onLockRequest = { title, sub, onOk ->
                           triggerBiometricAuth(title, sub, onOk)
                        }
                      )
                    }
                  }
                }
              }
            }
            composable("link_scanner") { LinkScannerScreen(viewModel = vm, onBack = { navController.popBackStack() }) }
            composable("about") { AboutScreen(onBack = { navController.popBackStack() }) }
            composable("glossary") {
              CyberGlossaryScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
              )
            }
            composable("checklist") {
              SecurityChecklistScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
              )
            }
          }
        }
      }
    }
  }
}
