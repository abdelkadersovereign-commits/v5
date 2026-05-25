package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.example.data.SovereignDataStore
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dataStore = SovereignDataStore(applicationContext)
        val isAr = dataStore.isArabic.first()
        val apiKey = dataStore.geminiApiKey.first().ifBlank {
            com.asyria.v4.BuildConfig.GEMINI_API_KEY
        }

        // 1. Precise Prayer Alert using Adhan library with saved coordinates
        checkAndNotifyPrayerPrecise(isAr)

        // 2. Gemini Security Alert (if API key available)
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val model = GenerativeModel(modelName = "gemini-2.5-flash-lite", apiKey = apiKey)
                val promptLocale = if (isAr) "Arabic" else "English"
                val prompt = "Provide one very brief (12 words max) high-priority cybersecurity tip in $promptLocale. Be direct. No intro."
                val res = model.generateContent(prompt).text ?: ""
                if (res.isNotBlank()) {
                    showNotification(
                        title = if (isAr) "تنبيه أمني سيادي" else "Sovereign Intelligence Alert",
                        message = res.trim(),
                        id = 1002
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return Result.success()
    }

    private fun checkAndNotifyPrayerPrecise(isAr: Boolean) {
        // Read saved location from SharedPreferences (saved by PrayerViewModel)
        val prefs = applicationContext.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("last_lat", 33.5138f).toDouble()
        val lon = prefs.getFloat("last_lon", 36.2765f).toDouble()

        val coords = Coordinates(lat, lon)
        val date = DateComponents.from(Date())
        val params = CalculationMethod.UMM_AL_QURA.parameters.also { it.madhab = Madhab.SHAFI }
        val prayerTimes = PrayerTimes(coords, date, params)

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val prayers = listOf(
            Prayer.FAJR to prayerTimes.fajr,
            Prayer.SUNRISE to prayerTimes.sunrise,
            Prayer.DHUHR to prayerTimes.dhuhr,
            Prayer.ASR to prayerTimes.asr,
            Prayer.MAGHRIB to prayerTimes.maghrib,
            Prayer.ISHA to prayerTimes.isha
        )

        val arabicNames = mapOf(
            Prayer.FAJR to "الفجر",
            Prayer.SUNRISE to "الشروق",
            Prayer.DHUHR to "الظهر",
            Prayer.ASR to "العصر",
            Prayer.MAGHRIB to "المغرب",
            Prayer.ISHA to "العشاء"
        )

        for ((prayer, prayerDate) in prayers) {
            if (prayerDate == null) continue
            val prayerCal = Calendar.getInstance().apply { time = prayerDate }
            val prayerMinutes = prayerCal.get(Calendar.HOUR_OF_DAY) * 60 + prayerCal.get(Calendar.MINUTE)

            // Notify 5 minutes before
            val minutesBefore = prayerMinutes - currentMinutes
            if (minutesBefore in 4..6) {
                val name = if (isAr) (arabicNames[prayer] ?: prayer.name) else prayer.name
                val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                val timeStr = timeFormat.format(prayerCal.time)
                showNotification(
                    title = if (isAr) "⏰ تنبيه الصلاة — $name" else "⏰ Prayer Alert — ${prayer.name}",
                    message = if (isAr) "بعد 5 دقائق حان وقت $name الساعة $timeStr. استعد للصلاة."
                              else "In 5 minutes: ${prayer.name} at $timeStr. Prepare for prayer.",
                    id = 2000 + prayer.ordinal()
                )
            }
            // Notify exactly at prayer time
            if (minutesBefore == 0) {
                val name = if (isAr) (arabicNames[prayer] ?: prayer.name) else prayer.name
                showNotification(
                    title = if (isAr) "🕌 حان وقت $name" else "🕌 Time for ${prayer.name}",
                    message = if (isAr) "اللهُ أكبر — حان وقت صلاة $name. توقف عن كل شيء وصلِّ."
                              else "Allahu Akbar — Time for ${prayer.name}. Stop everything and pray.",
                    id = 3000 + prayer.ordinal()
                )
            }
        }
    }

    private fun Prayer.ordinal(): Int = when (this) {
        Prayer.FAJR -> 0; Prayer.SUNRISE -> 1; Prayer.DHUHR -> 2
        Prayer.ASR -> 3; Prayer.MAGHRIB -> 4; Prayer.ISHA -> 5; else -> 99
    }

    private fun showNotification(title: String, message: String, id: Int) {
        val channelId = "sovereign_alerts"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SOVEREIGN TACTICAL ALERTS", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Prayer times and cybersecurity alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 100, 300, 100, 500)
                enableLights(true)
                lightColor = android.graphics.Color.CYAN
                val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                setSound(soundUri, android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            }
            notificationManager.createNotificationChannel(channel)
        }

        val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 100, 300, 100, 500))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
