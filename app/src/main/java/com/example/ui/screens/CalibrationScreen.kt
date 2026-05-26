package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VoidBlack
import com.example.ui.theme.cascadiaCode
import com.example.ui.viewmodel.DashboardViewModel

@Composable
fun CalibrationScreen(
    viewModel: DashboardViewModel,
    onCalibrationComplete: () -> Unit
) {
    val isArabic by viewModel.isArabic.collectAsState()

    val levels = if (isArabic) {
        listOf("مبتدئ", "متوسط", "متقدم")
    } else {
        listOf("Beginner", "Intermediate", "Advanced")
    }

    val interests = if (isArabic) {
        mapOf(
            "Account Security" to "أمن الحسابات",
            "Network Security" to "أمن الشبكات",
            "Phishing & Malware" to "التصيد والبرمجيات الخبيثة",
            "Ethical Hacking" to "الاختراق الأخلاقي",
            "Digital Privacy" to "الخصوصية الرقمية"
        )
    } else {
        mapOf(
            "Account Security" to "Account Security",
            "Network Security" to "Network Security",
            "Phishing & Malware" to "Phishing & Malware",
            "Ethical Hacking" to "Ethical Hacking",
            "Digital Privacy" to "Digital Privacy"
        )
    }
    val interestKeys = interests.keys.toList()

    var selectedLevel by remember { mutableStateOf(levels.first()) }
    val selectedInterests = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack.copy(alpha = 0.9f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isArabic) "معايرة النظام" else "System Calibration",
            color = CyberCyan,
            fontSize = 28.sp,
            fontFamily = cascadiaCode,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isArabic) "يرجى تحديد مستواك واهتماماتك لتخصيص التجربة" else "Please select your level and interests to personalize your experience",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = cascadiaCode,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Level Selection
        Text(
            text = if (isArabic) "المستوى" else "Level",
            color = CyberCyan,
            fontSize = 20.sp,
            fontFamily = cascadiaCode,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            levels.forEach { level ->
                Chip(
                    text = level,
                    selected = selectedLevel == level,
                    onClick = { selectedLevel = level }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Interest Selection
        Text(
            text = if (isArabic) "الاهتمامات (اختر حتى 3)" else "Interests (Choose up to 3)",
            color = CyberCyan,
            fontSize = 20.sp,
            fontFamily = cascadiaCode,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            interestKeys.chunked(2).forEach { rowInterests ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    rowInterests.forEach { interestKey ->
                        Chip(
                            text = interests[interestKey] ?: "",
                            selected = selectedInterests.contains(interestKey),
                            onClick = {
                                if (selectedInterests.contains(interestKey)) {
                                    selectedInterests.remove(interestKey)
                                } else {
                                    if (selectedInterests.size < 3) {
                                        selectedInterests.add(interestKey)
                                    }
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Save Button
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .clip(RoundedCornerShape(8.dp))
                .background(CyberCyan)
                .clickable {
                    viewModel.saveCalibrationData(selectedLevel, selectedInterests.toSet())
                    onCalibrationComplete()
                }
                .padding(12.dp)
        ) {
            Text(
                text = if (isArabic) "بدء" else "BEGIN",
                color = Color.Black,
                fontSize = 20.sp,
                fontFamily = cascadiaCode,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun Chip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) CyberCyan else Color.Transparent
    val textColor = if (selected) Color.Black else Color.White
    val borderColor = if (selected) CyberCyan else Color.White

    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = cascadiaCode,
            fontSize = 14.sp
        )
    }
}
