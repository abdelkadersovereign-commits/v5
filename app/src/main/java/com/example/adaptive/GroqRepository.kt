package com.example.adaptive

  import kotlinx.coroutines.Dispatchers
  import kotlinx.coroutines.withContext
  import okhttp3.MediaType.Companion.toMediaType
  import okhttp3.OkHttpClient
  import okhttp3.Request
  import okhttp3.RequestBody.Companion.toRequestBody
  import org.json.JSONArray
  import org.json.JSONObject
  import java.util.concurrent.TimeUnit

  /**
   * GroqRepository — Sole entry-point for all Groq LLM API calls.
   *
   * Endpoint : POST https://api.groq.com/openai/v1/chat/completions
   * Model    : llama-3.3-70b-versatile
   *
   * FeedbackService delegates network I/O here so the HTTP layer stays
   * cleanly separated from business / parsing logic.
   */
  class GroqRepository {

      private val client = OkHttpClient.Builder()
          .connectTimeout(20, TimeUnit.SECONDS)
          .readTimeout(30, TimeUnit.SECONDS)
          .build()

      private companion object {
          const val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
          const val MODEL    = "llama-3.3-70b-versatile"
      }

      /**
       * Send a chat-completion request to Groq and return the model reply.
       *
       * @param systemPrompt  System instruction enforcing structured JSON output.
       * @param userContent   User feedback or customization request (already sanitised).
       * @param groqApiKey    User-supplied Groq API key (stored in DataStore, never hardcoded).
       * @return The raw reply string extracted from choices[0].message.content.
       * @throws Exception on network failure or non-2xx HTTP status.
       */
      suspend fun complete(
          systemPrompt: String,
          userContent: String,
          groqApiKey: String
      ): String = withContext(Dispatchers.IO) {

          val requestBody = JSONObject().apply {
              put("model", MODEL)
              put("temperature", 0.2)
              put("messages", JSONArray().apply {
                  put(JSONObject().apply {
                      put("role", "system")
                      put("content", systemPrompt)
                  })
                  put(JSONObject().apply {
                      put("role", "user")
                      put("content", userContent)
                  })
              })
          }.toString()

          val request = Request.Builder()
              .url(ENDPOINT)
              .post(requestBody.toRequestBody("application/json".toMediaType()))
              .header("Authorization", "Bearer $groqApiKey")
              .header("Content-Type", "application/json")
              .build()

          val response = client.newCall(request).execute()
          val rawBody  = response.body?.string()
              ?: throw Exception("Empty response from Groq API.")

          if (!response.isSuccessful) {
              val errorMsg = try {
                  JSONObject(rawBody)
                      .optJSONObject("error")
                      ?.optString("message")
                      ?: "Unknown Groq error"
              } catch (_: Exception) { rawBody.take(200) }
              throw Exception("Groq Error ${response.code}: $errorMsg")
          }

          // OpenAI-compatible response: choices[0].message.content
          JSONObject(rawBody)
              .getJSONArray("choices")
              .getJSONObject(0)
              .getJSONObject("message")
              .getString("content")
              .trim()
      }
  }
  