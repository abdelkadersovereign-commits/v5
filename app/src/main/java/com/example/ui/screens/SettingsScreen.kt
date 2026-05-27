package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel

import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel,
    onClose: () -> Unit,
    onOpenAbout: () -> Unit,
    onLockRequest: (String, String, () -> Unit) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isAr by viewModel.isArabic.collectAsState()
    val isStealth by viewModel.isStealthMode.collectAsState()
    val isNeuralProxy by viewModel.isNeuralProxy.collectAsState()
    val apiKey by viewModel.customApiKey.collectAsState()
    val projectName by viewModel.projectName.collectAsState()
    val projectId by viewModel.projectId.collectAsState()
    val projectNumber by viewModel.projectNumber.collectAsState()
    val operatorName by viewModel.operatorName.collectAsState()
    val neuralRole by viewModel.neuralRole.collectAsState()
    
    val isTestingKey by viewModel.isTestingKey.collectAsState()
    val isNeuralLinkOffline by viewModel.isNeuralLinkOffline.collectAsState()
    
    val uriHandler = LocalUriHandler.current
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
    
    BackHandler {
        onClose()
    }
    
    var tempOperatorName by remember(operatorName) { mutableStateOf(operatorName) }
    var tempNeuralRole by remember(neuralRole) { mutableStateOf(neuralRole) }
    var tempApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var tempProjectName by remember(projectName) { mutableStateOf(projectName) }
    var tempProjectId by remember(projectId) { mutableStateOf(projectId) }
    var tempProjectNumber by remember(projectNumber) { mutableStateOf(projectNumber) }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isAr) "وحدة التحكم السيادية" else "SOVEREIGN CONTROL UNIT",
                        color = AmberZen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    IconButton(
                        onClick = { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onClose() 
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close",
                            tint = AmberZen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Neural Profile
                SettingsSectionHeader(if (isAr) "الملف العصبي" else "NEURAL PROFILE")
                
                CyberTextField(
                    label = if (isAr) "اسم المشغل" else "OPERATOR NAME",
                    value = tempOperatorName,
                    onValueChange = { 
                        tempOperatorName = it
                        viewModel.updateOperatorName(it)
                    },
                    isAr = isAr
                )
                
                CyberTextField(
                    label = if (isAr) "الدور العصبي" else "NEURAL ROLE",
                    value = tempNeuralRole,
                    onValueChange = { 
                        tempNeuralRole = it
                        viewModel.updateNeuralRole(it)
                    },
                    isAr = isAr
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Core Security
                SettingsSectionHeader(if (isAr) "الأمن الجوهري" else "CORE SECURITY")

                // Neural Key Instruction Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(1.dp, AmberZen.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .background(AmberZen.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AmberZen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAr) "دليل المفتاح العصبي" else "NEURAL KEY GUIDANCE",
                                color = AmberZen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isAr) 
                                "لتمثيل الذكاء الاصطناعي، يرجى الحصول على مفتاح API من Google AI Studio ووضعه أدناه. هذا يسمح للنظام بفك تشفير التهديدات وتحليل الروابط."
                            else 
                                "To activate sovereign intelligence, obtain a Gemini API Key from Google AI Studio and enter it below. This enables threat decoding and link analysis.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { uriHandler.openUri("https://aistudio.google.com/app/apikey") },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberZen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text(if (isAr) "احصل على المفتاح 🔗" else "GET API KEY 🔗", fontSize = 10.sp, color = VoidBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                CyberTextField(
                    label = if (isAr) "مفتاح Gemini API" else "GEMINI API KEY",
                    value = tempApiKey,
                    onValueChange = { 
                        tempApiKey = it
                        viewModel.updateCustomApiKey(it)
                    },
                    isAr = isAr,
                    isPassword = true
                )

                // Project fields removed - AI Studio keys work with API key only

                Button(
                    onClick = { viewModel.testNeuralLink() },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, if (isNeuralLinkOffline) Color.Red.copy(alpha = 0.5f) else CyberCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isTestingKey
                ) {
                    if (isTestingKey) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberCyan, strokeWidth = 2.dp)
                    } else {
                        val statusText = if (isNeuralLinkOffline) 
                            (if (isAr) "فشل الاتصال: اختبار الرابط" else "LINK OFFLINE: TEST UPLINK")
                            else "FULL NEURAL LINK ESTABLISHED ✅"
                            
                        Text(
                            text = statusText,
                            color = if (isNeuralLinkOffline) Color.Red else CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: System Preferences
                SettingsSectionHeader(if (isAr) "تفضيلات النظام" else "SYSTEM PREFERENCES")
                
                SettingsToggle(
                    label = if (isAr) "تبديل اللغة (English)" else "Language Toggle (العربية)",
                    checked = isAr,
                    onCheckedChange = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.setArabic(!isAr) 
                    },
                    icon = Icons.Default.Language
                )

                // VPN notice — API key requires external VPN in restricted regions
                  Box(
                      modifier = Modifier
                          .fillMaxWidth()
                          .padding(vertical = 8.dp)
                          .border(1.dp, Color(0xFFFFAA00).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                          .background(Color(0xFFFFAA00).copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                          .padding(12.dp)
                  ) {
                      Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                          Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFAA00), modifier = Modifier.size(16.dp))
                          Text(
                              text = if (isAr)
                                  "⚠️ لاستخدام مفتاح API في المناطق المحجوبة، يجب تفعيل تطبيق VPN خارجي قبل تشغيل هذا التطبيق. مفتاح API لا يعمل بدون VPN في بعض الدول."
                              else
                                  "⚠️ To use the API key in restricted regions, activate an external VPN app before launching this app. The API key will not function without a VPN in blocked countries.",
                              color = Color(0xFFFFAA00).copy(alpha = 0.9f),
                              fontSize = 10.sp,
                              lineHeight = 14.sp,
                              fontFamily = FontFamily.Monospace
                          )
                      }
                  }

                  Spacer(modifier = Modifier.height(24.dp))
                
                // Section: Operator Manual
                SettingsSectionHeader(if (isAr) "دليل التشغيل" else "SYSTEM DOCUMENTATION")
                
                Button(
                    onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onOpenAbout() 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isAr) "عرض دليل التشغيل" else "OPEN OPERATOR MANUAL",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section: Data Management
                SettingsSectionHeader(if (isAr) "إدارة البيانات" else "DATA MANAGEMENT")
                
                Text(
                    text = if (isAr) "⚠️ يحذف جميع الأفكار المحفوظة في السجل بشكل نهائي ولا يمكن التراجع عنه."
                           else "⚠️ Permanently deletes all saved records. This action cannot be undone.",
                    color = Color.Red.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Button(
                    onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        viewModel.wipeVault() 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) "حذف جميع البيانات المحفوظة" else "DELETE ALL SAVED DATA",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = "A.SYRIA | SOVEREIGN OS v5.0.0",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontFamily = FontFamily.Monospace
                )
            }
            // Footer: Version Information
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "A.SYRIA SOVEREIGN OS v5.0.0 | SECURE BUILD",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ENCRYPTED CORE STABLE",
                    color = CyberCyan.copy(alpha = 0.2f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Column {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White.copy(alpha = 0.1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isAr: Boolean,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            color = CyberCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp),
            fontFamily = FontFamily.Monospace
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = CyberCyan
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                textAlign = if (isAr) androidx.compose.ui.text.style.TextAlign.Right else androidx.compose.ui.text.style.TextAlign.Left
            ),
            singleLine = true
        )
    }
}

@Composable
fun SettingsToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = CyberCyan
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = tint,
                checkedTrackColor = tint.copy(alpha = 0.3f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.1f)
            )
        )
    }
}
