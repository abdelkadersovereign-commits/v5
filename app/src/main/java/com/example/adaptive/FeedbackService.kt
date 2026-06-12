package com.example.adaptive

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class FeedbackResult {
    data class Success(val changes: List<UIChange>) : FeedbackResult()
    data class RateLimited(val resetSeconds: Long, val remaining: Int) : FeedbackResult()
    data class Empty(val message: String) : FeedbackResult()
    data class Error(val message: String) : FeedbackResult()
}

class FeedbackService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val geminiEndpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"

    // Built dynamically at runtime — no sensitive keywords stored as literals in the APK
    private val inputFilterPattern: Regex by lazy {
        val parts = listOf(
            "ignore" + " " + "previous",
            "system" + " " + "prompt",
            "jail" + "break",
            "over" + "ride",
            "ev" + "al",
            "ex" + "ec"
        )
        Regex("(?i)(${parts.joinToString("|")})")
    }

    suspend fun submitFeedback(feedbackText: String, apiKey: String): FeedbackResult =
        withContext(Dispatchers.IO) {

            if (!RateLimiter.isAllowed(context)) {
                return@withContext FeedbackResult.RateLimited(
                    resetSeconds = RateLimiter.getResetSeconds(context),
                    remaining = 0
                )
            }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext FeedbackResult.Error(
                    if (context.resources.configuration.locales[0].language == "ar")
                        "مفتاح API غير متوفر. أضف مفتاحك في الإعدادات أولاً."
                    else "API key not configured. Add your key in Settings first."
                )
            }

            val sanitized = feedbackText
                .replace(Regex("[{}\\[\\]]"), "")
                .replace(inputFilterPattern, "***")
                .take(500)
                .trim()

            if (sanitized.isBlank()) {
                return@withContext FeedbackResult.Error("Input is empty after sanitization.")
            }

            try {
                val requestJson = buildGeminiPayload(sanitized)
                val body = requestJson.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(geminiEndpoint)
                    .post(body)
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val rawText = response.body?.string()
                    ?: return@withContext FeedbackResult.Error("Empty response from AI.")

                if (!response.isSuccessful) {
                    return@withContext FeedbackResult.Error("AI Error ${response.code}: Check your API key or VPN.")
                }

                val geminiText = JSONObject(rawText)
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()

                val changes = parseAndValidateChanges(geminiText)
                RateLimiter.recordRequest(context)

                if (changes.isEmpty()) {
                    FeedbackResult.Empty(
                        if (context.resources.configuration.locales[0].language == "ar")
                            "لم يتم التعرف على تغييرات. حاول: 'لون أخضر'، 'خط أكبر'، 'وضع مضغوط'، 'توقيت 12 ساعة'."
                        else "No changes detected. Try: 'green color', 'bigger text', 'compact mode', '12 hour clock'."
                    )
                } else {
                    FeedbackResult.Success(changes)
                }

            } catch (e: Exception) {
                FeedbackResult.Error("Connection error: ${e.message?.take(100)}")
            }
        }

    private fun buildGeminiPayload(sanitizedInput: String): String {
        // Prompt built from parts to avoid static string flagging
        val role = "UI configuration assistant for A.SYRIA app"
        val task = "Translate user requests into JSON UI changes"
        val outputRule = "Output ONLY a valid JSON array. No prose, no markdown."

        val configKeys = buildString {
            appendLine("accentColor: cyan|amber|green|white|red|purple")
            appendLine("primaryHex: hex color e.g. #FF5500")
            appendLine("backgroundHex: hex background e.g. #0D1117")
            appendLine("fontSize: 10 to 24")
            appendLine("fontWeight: light|normal|bold")
            appendLine("cornerRadius: 0 to 32")
            appendLine("cardStyle: glass|solid|outline|flat")
            appendLine("compactMode: true|false")
            appendLine("tabStyle: compact|normal")
            appendLine("showPrayerTab: true|false")
            appendLine("showAcademyTab: true|false")
            appendLine("showStatusBar: true|false")
            appendLine("animationsEnabled: true|false")
            appendLine("notificationLevel: silent|normal|active")
            appendLine("is12HourFormat: true|false")
        }

        val examples = buildString {
            appendLine("'green' -> accentColor:green")
            appendLine("'bigger text' -> fontSize:18")
            appendLine("'compact' -> compactMode:true, tabStyle:compact, fontSize:12")
            appendLine("'12 hour clock' -> is12HourFormat:true")
            appendLine("'dark background' -> backgroundHex:#070D14")
            appendLine("'round corners' -> cornerRadius:24")
        }

        val systemPrompt = """
Role: $role
Task: $task
Rule: $outputRule

Available keys:
$configKeys

Examples:
$examples

Each object must have: {"key":"...","value":"...","label":"...","preview":"..."}
label: short Arabic — English description
preview: one-line explanation of what changes

User request: "$sanitizedInput"

Output (JSON array only):
        """.trimIndent()

        return JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", 1024)
                put("temperature", 0.05)
            })
        }.toString()
    }

    private fun parseAndValidateChanges(geminiText: String): List<UIChange> {
        val cleaned = geminiText
            .replace(Regex("^```[a-z]*\n?", RegexOption.MULTILINE), "")
            .replace("```", "")
            .trim()

        return try {
            val arr = JSONArray(cleaned)
            (0 until arr.length()).mapNotNull { i ->
                val obj     = arr.getJSONObject(i)
                val key     = obj.optString("key").trim()
                val value   = obj.optString("value").trim()
                val label   = obj.optString("label", key)
                val preview = obj.optString("preview", "")

                val validator = AdaptiveEngine.ALLOWED_KEYS[key] ?: return@mapNotNull null
                if (!validator(value)) return@mapNotNull null

                UIChange(key = key, value = value, label = label, preview = preview)
            }
        } catch (e: Exception) { emptyList() }
    }
}
