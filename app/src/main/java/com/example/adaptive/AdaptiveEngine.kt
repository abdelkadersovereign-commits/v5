package com.example.adaptive

  import android.content.Context
  import androidx.compose.runtime.compositionLocalOf
  import kotlinx.coroutines.flow.MutableStateFlow
  import kotlinx.coroutines.flow.StateFlow
  import kotlinx.coroutines.flow.asStateFlow
  import org.json.JSONObject

  /** Immutable snapshot of all dynamically-configurable UI properties. */
  data class UIConfig(
      val fontSize: Float        = 14f,
      val accentColor: String    = "cyan",
      val showPrayerTab: Boolean  = true,
      val showAcademyTab: Boolean = true,
      val tabStyle: String       = "normal"
  )

  /** Proposed UI change returned by FeedbackService before the user confirms. */
  data class UIChange(
      val key: String,
      val value: String,
      val label: String,
      val preview: String
  )

  /** Composition local — any Composable can read current UIConfig without prop-drilling. */
  val LocalAdaptiveConfig = compositionLocalOf { UIConfig() }

  /**
   * AdaptiveEngine — Single source of truth for dynamic UI configuration.
   *
   * Persists UIConfig as JSON in SharedPreferences.
   * Exposes [StateFlow<UIConfig>] so Compose re-renders on any change.
   * Validates every change against a strict whitelist — injection-proof.
   */
  class AdaptiveEngine(private val context: Context) {

      companion object {
          private const val PREFS_NAME = "sovereign_adaptive_ui"
          private const val KEY_CONFIG  = "ui_config_json"

          /** Whitelisted keys with validation lambdas. Used by FeedbackService too. */
          val ALLOWED_KEYS: Map<String, (String) -> Boolean> = mapOf(
              "fontSize"       to { v -> v.toFloatOrNull()?.let { it in 12f..22f } ?: false },
              "accentColor"    to { v -> v in setOf("cyan", "amber", "green", "white") },
              "showPrayerTab"  to { v -> v == "true" || v == "false" },
              "showAcademyTab" to { v -> v == "true" || v == "false" },
              "tabStyle"       to { v -> v == "compact" || v == "normal" }
          )
      }

      private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      private val _uiConfig = MutableStateFlow(loadFromPrefs())
      val uiConfig: StateFlow<UIConfig> = _uiConfig.asStateFlow()

      /** Atomically validate and apply confirmed [UIChange] items. Drops invalid keys silently. */
      fun applyChanges(changes: List<UIChange>) {
          var updated = _uiConfig.value
          for (change in changes) {
              val validator = ALLOWED_KEYS[change.key] ?: continue
              if (!validator(change.value)) continue
              updated = when (change.key) {
                  "fontSize"       -> updated.copy(fontSize = change.value.toFloat())
                  "accentColor"    -> updated.copy(accentColor = change.value)
                  "showPrayerTab"  -> updated.copy(showPrayerTab = change.value == "true")
                  "showAcademyTab" -> updated.copy(showAcademyTab = change.value == "true")
                  "tabStyle"       -> updated.copy(tabStyle = change.value)
                  else             -> updated
              }
          }
          saveToPrefs(updated)
          _uiConfig.value = updated
      }

      /** Reset everything to factory defaults. */
      fun resetToDefaults() {
          val default = UIConfig()
          saveToPrefs(default)
          _uiConfig.value = default
      }

      private fun loadFromPrefs(): UIConfig {
          val json = prefs.getString(KEY_CONFIG, null) ?: return UIConfig()
          return try {
              val o = JSONObject(json)
              UIConfig(
                  fontSize       = o.optDouble("fontSize", 14.0).toFloat(),
                  accentColor    = o.optString("accentColor", "cyan"),
                  showPrayerTab  = o.optBoolean("showPrayerTab",  true),
                  showAcademyTab = o.optBoolean("showAcademyTab", true),
                  tabStyle       = o.optString("tabStyle", "normal")
              )
          } catch (e: Exception) { UIConfig() }
      }

      private fun saveToPrefs(config: UIConfig) {
          val json = JSONObject().apply {
              put("fontSize",       config.fontSize)
              put("accentColor",    config.accentColor)
              put("showPrayerTab",  config.showPrayerTab)
              put("showAcademyTab", config.showAcademyTab)
              put("tabStyle",       config.tabStyle)
          }.toString()
          prefs.edit().putString(KEY_CONFIG, json).apply()
      }
  }
  