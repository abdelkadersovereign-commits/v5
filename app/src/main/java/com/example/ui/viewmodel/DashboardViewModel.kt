package com.example.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.InventorIdea
import com.example.data.database.InventorIdeaRepository
import com.example.data.ContextualVerseEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import com.example.data.SovereignDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class DashboardViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _roll = MutableStateFlow(0f)
    val roll: StateFlow<Float> = _roll.asStateFlow()

    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val alpha = 0.12f
    private var currentRoll = 0f
    private var currentPitch = 0f
    
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    private val _isAcademyGenerating = MutableStateFlow(false)
    val isAcademyGenerating: StateFlow<Boolean> = _isAcademyGenerating.asStateFlow()

    fun startSensors() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    private val _connectionType = MutableStateFlow("Determining...")
    val connectionType: StateFlow<String> = _connectionType.asStateFlow()

    private val _ipAddress = MutableStateFlow("127.0.0.1")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _batteryPercentage = MutableStateFlow(100)
    val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()

    private val _chargingStatus = MutableStateFlow("UNKNOWN")
    val chargingStatus: StateFlow<String> = _chargingStatus.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _intelligenceBrief = MutableStateFlow("[INTEL_UPLINK] > Initializing satellite intelligence stream...")
    val intelligenceBrief: StateFlow<String> = _intelligenceBrief.asStateFlow()

    private val _terminalInput = MutableStateFlow("")
    val terminalInput: StateFlow<String> = _terminalInput.asStateFlow()

    private val _terminalResponse = MutableStateFlow("")
    val terminalResponse: StateFlow<String> = _terminalResponse.asStateFlow()

    private val _isTerminalExpanded = MutableStateFlow(false)
    val isTerminalExpanded: StateFlow<Boolean> = _isTerminalExpanded.asStateFlow()

    private val _isNeuralLinkOffline = MutableStateFlow(false)
    val isNeuralLinkOffline: StateFlow<Boolean> = _isNeuralLinkOffline.asStateFlow()

    private val _hasInternetConnection = MutableStateFlow(true)
    val hasInternetConnection: StateFlow<Boolean> = _hasInternetConnection.asStateFlow()

    private val _isTestingKey = MutableStateFlow(false)
    val isTestingKey: StateFlow<Boolean> = _isTestingKey.asStateFlow()

    private val _linkAnalysisResult = MutableStateFlow<String?>(null)
    val linkAnalysisResult: StateFlow<String?> = _linkAnalysisResult.asStateFlow()

    private val _isAnalyzingLink = MutableStateFlow(false)
    val isAnalyzingLink: StateFlow<Boolean> = _isAnalyzingLink.asStateFlow()

    private val database = AppDatabase.getDatabase(application)
    private val repository = InventorIdeaRepository(database.inventorIdeaDao())

    val savedIdeas: StateFlow<List<InventorIdea>> = repository.allIdeas
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val ambientInsight: StateFlow<ContextualVerseEngine.SpiritualInsight> = combine(
        _batteryPercentage, _chargingStatus, _connectionType
    ) { battery, charging, connection ->
        ContextualVerseEngine.getInsight(battery, charging == "CHARGING", connection)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContextualVerseEngine.getInsight(100, false, "Determining..."))

    private val _isForgePanelOpen = MutableStateFlow(false)
    val isForgePanelOpen: StateFlow<Boolean> = _isForgePanelOpen.asStateFlow()

    private val _isVaultViewOpen = MutableStateFlow(false)
    val isVaultViewOpen: StateFlow<Boolean> = _isVaultViewOpen.asStateFlow()

    private val _isVaultAuthenticated = MutableStateFlow(false)
    val isVaultAuthenticated: StateFlow<Boolean> = _isVaultAuthenticated.asStateFlow()

    private val _isSettingsAuthenticated = MutableStateFlow(false)
    val isSettingsAuthenticated: StateFlow<Boolean> = _isSettingsAuthenticated.asStateFlow()

    fun setVaultAuthenticated(auth: Boolean) { _isVaultAuthenticated.value = auth }
    fun setSettingsAuthenticated(auth: Boolean) { _isSettingsAuthenticated.value = auth }

    private val dataStore = SovereignDataStore(application)

    private val _isArabic = MutableStateFlow(true)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    fun setArabic(enabled: Boolean) {
        viewModelScope.launch { dataStore.saveIsArabic(enabled) }
    }

    private val _cyberScore = MutableStateFlow(0)
    val cyberScore: StateFlow<Int> = _cyberScore.asStateFlow()

    private val _operatorName = MutableStateFlow("Sovereign_Operator")
    val operatorName: StateFlow<String> = _operatorName.asStateFlow()

    private val _neuralRole = MutableStateFlow("Sovereign Node v4")
    val neuralRole: StateFlow<String> = _neuralRole.asStateFlow()

    val cyberRank: StateFlow<String> = combine(_cyberScore, _isArabic) { score, isAr ->
        if (isAr) {
            when {
                score <= 50 -> "شيفرة مبتدئة"
                score <= 150 -> "حارس السينتينل"
                else -> "شبح سيادي"
            }
        } else {
            when {
                score <= 50 -> "Novice Cipher"
                score <= 150 -> "Sentinel Guard"
                else -> "Sovereign Ghost"
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Novice Cipher")

    val cyberProgress: StateFlow<Float> = combine(_cyberScore, MutableStateFlow(Unit)) { score, _ ->
        when {
            score <= 50 -> (score.toFloat() / 50f).coerceIn(0f, 1f)
            score <= 150 -> ((score - 50).toFloat() / 100f).coerceIn(0f, 1f)
            else -> 1.0f
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    private val _isAcademyOpen = MutableStateFlow(false)
    val isAcademyOpen: StateFlow<Boolean> = _isAcademyOpen.asStateFlow()

    private val _isResourcesOpen = MutableStateFlow(false)
    val isResourcesOpen: StateFlow<Boolean> = _isResourcesOpen.asStateFlow()

    fun addCyberScore(points: Int) {
        viewModelScope.launch { dataStore.saveCyberScore(_cyberScore.value + points) }
    }

    fun setAcademyOpen(open: Boolean) { _isAcademyOpen.value = open }

    private val _savedAcademyModuleId = MutableStateFlow("")
    private val _savedAcademyScenariosJson = MutableStateFlow("")
    private val _savedAcademyIndex = MutableStateFlow(0)
    private val _savedAcademyUsedIds = MutableStateFlow<Set<String>>(emptySet())

    fun saveAcademyProgress(moduleId: String, scenariosJson: String, index: Int, usedIds: Set<String>) {
        _savedAcademyModuleId.value = moduleId
        _savedAcademyScenariosJson.value = scenariosJson
        _savedAcademyIndex.value = index
        _savedAcademyUsedIds.value = usedIds
    }

    fun getSavedAcademyModuleId(): String = _savedAcademyModuleId.value
    fun getSavedAcademyScenariosJson(): String = _savedAcademyScenariosJson.value
    fun getSavedAcademyIndex(): Int = _savedAcademyIndex.value
    fun getSavedAcademyUsedIds(): Set<String> = _savedAcademyUsedIds.value

    fun setResourcesOpen(open: Boolean) { _isResourcesOpen.value = open }

    private val _isStealthMode = MutableStateFlow(false)
    val isStealthMode: StateFlow<Boolean> = _isStealthMode.asStateFlow()

    private val _isNeuralProxy = MutableStateFlow(false)
    val isNeuralProxy: StateFlow<Boolean> = _isNeuralProxy.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _projectName = MutableStateFlow("")
    val projectName: StateFlow<String> = _projectName.asStateFlow()

    private val _projectId = MutableStateFlow("")
    val projectId: StateFlow<String> = _projectId.asStateFlow()

    private val _projectNumber = MutableStateFlow("")
    val projectNumber: StateFlow<String> = _projectNumber.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    // --- User Cognitive Model State ---
    private val _userLevel = MutableStateFlow("Beginner")
    val userLevel: StateFlow<String> = _userLevel.asStateFlow()

    private val _userInterests = MutableStateFlow<Set<String>>(emptySet())
    val userInterests: StateFlow<Set<String>> = _userInterests.asStateFlow()

    private val _calibrationCompleted = MutableStateFlow(false)
    val calibrationCompleted: StateFlow<Boolean> = _calibrationCompleted.asStateFlow()
    // --- End User Cognitive Model State ---

    private val dohClient = OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS).build()

    private val bypassDns = object : okhttp3.Dns {
        override fun lookup(hostname: String): List<java.net.InetAddress> {
            try { okhttp3.Dns.SYSTEM.lookup(hostname).takeIf { it.isNotEmpty() }?.let { return it } } catch (_: Exception) {}
            listOf("https://1.1.1.1/dns-query?name=$hostname&type=A", "https://8.8.8.8/resolve?name=$hostname&type=A").forEach { dohUrl ->
                try {
                    val req = Request.Builder().url(dohUrl).header("Accept", "application/dns-json").build()
                    val resp = dohClient.newCall(req).execute()
                    val json = org.json.JSONObject(resp.body?.string() ?: "")
                    val answers = json.optJSONArray("Answer") ?: return@forEach
                    val addrs = (0 until answers.length())
                        .mapNotNull { answers.getJSONObject(it) }
                        .filter { it.optInt("type") == 1 }
                        .mapNotNull { try { java.net.InetAddress.getByName(it.getString("data")) } catch (_: Exception) { null } }
                    if (addrs.isNotEmpty()) return addrs
                } catch (_: Exception) {}
            }
            throw java.net.UnknownHostException("Could not resolve $hostname")
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS).readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS).retryOnConnectionFailure(true).dns(bypassDns).build()

    private suspend fun generateContentSafely(prompt: String): String {
        return withContext(Dispatchers.IO) {
            val apiKey = _customApiKey.value.ifBlank { com.asyria.v4.BuildConfig.GEMINI_API_KEY }
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") throw Exception("Error 401: Invalid Key")
            try {
                executeNeuralProxyRequest(prompt, apiKey)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun executeNeuralProxyRequest(prompt: String, apiKey: String): String {
        val json = JSONObject().apply {
            put("contents", org.json.JSONArray().put(JSONObject().apply {
                put("parts", org.json.JSONArray().put(JSONObject().put("text", prompt)))
            }))
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val proxyEnabled = _isNeuralProxy.value
        val endpointUrls = listOf(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent"
        )
        val activeClient = if (proxyEnabled) {
            OkHttpClient.Builder().connectTimeout(45, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).retryOnConnectionFailure(true).build()
        } else {
            okHttpClient
        }
        var lastException: Exception? = null
        for (url in endpointUrls) {
            try {
                val requestBuilder = Request.Builder().url(url).post(body).header("x-goog-api-key", apiKey)
                val request = requestBuilder.build()
                val response = activeClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    val code = response.code
                    val errBody = response.body?.string() ?: ""
                    if (code == 429) throw Exception("Neural Buffer Full (Error 429) - يتم الآن إعادة شحن الطاقة العصبية.. يرجى الانتظار دقيقة")
                    if (code == 503) throw Exception("UPLINK BUSY - السيرفر مشغول حالياً، يرجى المحاولة بعد قليل")
                    lastException = Exception(if (code == 403) "Regional Block - Enable Neural Proxy" else if (code == 401) "Invalid API Key or Project ID" else "Error $code: $errBody")
                    continue
                }
                val resBody = response.body?.string() ?: ""
                val jsonRes = JSONObject(resBody)
                return jsonRes.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            } catch (e: Exception) {
                if (e.message?.contains("429") == true || e.message?.contains("UPLINK BUSY") == true) throw e
                lastException = e
            }
        }
        throw lastException ?: Exception("Neural link failed")
    }

    fun setStealthMode(enabled: Boolean) {
        viewModelScope.launch { dataStore.saveStealthMode(enabled).also { _isStealthMode.value = enabled } }
    }

    fun setNeuralProxy(enabled: Boolean) {
        viewModelScope.launch { dataStore.saveNeuralProxy(enabled).also { _isNeuralProxy.value = enabled } }
    }

    fun updateCustomApiKey(key: String) {
        viewModelScope.launch { dataStore.saveGeminiApiKey(key) }
    }

    fun updateProjectName(name: String) {
        viewModelScope.launch { dataStore.saveProjectName(name) }
    }

    fun updateProjectId(id: String) {
        viewModelScope.launch { dataStore.saveProjectId(id) }
    }

    fun updateProjectNumber(number: String) {
        viewModelScope.launch { dataStore.saveProjectNumber(number) }
    }

    fun updateOperatorName(name: String) {
        viewModelScope.launch { dataStore.saveOperatorName(name) }
    }

    fun updateNeuralRole(role: String) {
        viewModelScope.launch { dataStore.saveNeuralRole(role) }
    }

    fun setSettingsOpen(open: Boolean) { _isSettingsOpen.value = open }

    // --- Function to save calibration data ---
    fun saveCalibrationData(level: String, interests: Set<String>) {
        viewModelScope.launch {
            dataStore.saveUserLevel(level)
            dataStore.saveUserInterests(interests)
            dataStore.saveCalibrationCompleted(true)
        }
    }

    private val _forgeTitle = MutableStateFlow("")
    val forgeTitle: StateFlow<String> = _forgeTitle.asStateFlow()
    private val _forgeCategory = MutableStateFlow("SecOps")
    val forgeCategory: StateFlow<String> = _forgeCategory.asStateFlow()
    private val _forgeIdea = MutableStateFlow("")
    val forgeIdea: StateFlow<String> = _forgeIdea.asStateFlow()
    private val _forgeBlueprint = MutableStateFlow("")
    val forgeBlueprint: StateFlow<String> = _forgeBlueprint.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) { _batteryPercentage.value = ((level.toFloat() / scale.toFloat()) * 100).toInt() }
                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                _chargingStatus.value = if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) "CHARGING" else "DISCHARGING"
            }
        }
    }

    fun testNeuralLink() {
        viewModelScope.launch {
            _isTestingKey.value = true
            val apiKey = _customApiKey.value.ifBlank { com.asyria.v4.BuildConfig.GEMINI_API_KEY }
            if (apiKey.isEmpty() || apiKey.contains("MY_GEMINI_API_KEY")) {
                _isNeuralLinkOffline.value = true
                _terminalResponse.value = "[ TERMINAL ALERT ] Gemini API Key is required. Get yours free at: aistudio.google.com/app/apikey"
                _isTestingKey.value = false
                return@launch
            }
            try {
                withContext(kotlinx.coroutines.time.withTimeout(15000.toDuration(TimeUnit.MILLISECONDS))) {
                    generateContentSafely("ping")
                }
                _isNeuralLinkOffline.value = false
                _terminalResponse.value = "FULL NEURAL LINK ESTABLISHED ✅"
            } catch (e: Exception) {
                _isNeuralLinkOffline.value = true
                _terminalResponse.value = "[ TERMINAL ALERT ] ${e.message}"
            } finally {
                _isTestingKey.value = false
            }
        }
    }

    init {
        application.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        viewModelScope.launch { dataStore.isArabic.collect { _isArabic.value = it } }
        viewModelScope.launch { dataStore.stealthMode.collect { _isStealthMode.value = it } }
        viewModelScope.launch { dataStore.cyberScore.collect { _cyberScore.value = it } }
        viewModelScope.launch { dataStore.geminiApiKey.collect { _customApiKey.value = it } }
        viewModelScope.launch { dataStore.projectName.collect { _projectName.value = it } }
        viewModelScope.launch { dataStore.projectId.collect { _projectId.value = it } }
        viewModelScope.launch { dataStore.projectNumber.collect { _projectNumber.value = it } }
        viewModelScope.launch { dataStore.operatorName.collect { _operatorName.value = it } }
        viewModelScope.launch { dataStore.neuralRole.collect { _neuralRole.value = it } }
        viewModelScope.launch { dataStore.neuralProxy.collect { _isNeuralProxy.value = it } }
        // Collect user profile data
        viewModelScope.launch { dataStore.userLevel.collect { _userLevel.value = it } }
        viewModelScope.launch { dataStore.userInterests.collect { _userInterests.value = it } }
        viewModelScope.launch { dataStore.calibrationCompleted.collect { _calibrationCompleted.value = it } }

        viewModelScope.launch(Dispatchers.IO) {
            try { _ipAddress.value = fetchPublicIp() } catch (_: Exception) { _ipAddress.value = extractLocalIp() }
        }
    }

    fun setTerminalExpanded(expanded: Boolean) {
        _isTerminalExpanded.value = expanded
        if (!expanded) { _terminalInput.value = ""; _terminalResponse.value = "" }
    }

    fun setForgePanelOpen(open: Boolean) {
        _isForgePanelOpen.value = open
        if (!open) { clearForgeFields() }
    }

    fun setVaultViewOpen(open: Boolean) { _isVaultViewOpen.value = open }

    fun updateTerminalInput(input: String) { _terminalInput.value = input }
    fun updateForgeTitle(title: String) { _forgeTitle.value = title }
    fun updateForgeCategory(category: String) { _forgeCategory.value = category }
    fun updateForgeIdea(idea: String) { _forgeIdea.value = idea }

    private fun clearForgeFields() {
        _forgeTitle.value = ""
        _forgeCategory.value = "SecOps"
        _forgeIdea.value = ""
        _forgeBlueprint.value = ""
    }

    fun deleteIdea(ideaId: Int) {
        viewModelScope.launch { repository.deleteIdeaById(ideaId) }
    }

    fun wipeVault() {
        viewModelScope.launch { repository.clearAll() }
    }

    fun forgeAndSaveIdea() {
        val title = _forgeTitle.value.trim()
        val category = _forgeCategory.value.trim()
        val ideaRaw = _forgeIdea.value.trim()
        if (title.isEmpty() || ideaRaw.isEmpty()) return
        viewModelScope.launch {
            _isThinking.value = true
            val generatedBlueprint = "[ ALERT ] Neural Blueprinting is offline. Local Encryption protocol used instead."
            _isThinking.value = false
            val encryptedEntity = InventorIdea.createEncrypted(title, category, ideaRaw, generatedBlueprint)
            repository.insertIdea(encryptedEntity)
            _forgeBlueprint.value = generatedBlueprint
            delay(500)
            _isForgePanelOpen.value = false
            _isVaultViewOpen.value = true
        }
    }

    private fun fetchPublicIp(): String {
        listOf("https://api64.ipify.org", "https://checkip.amazonaws.com", "https://ifconfig.me/ip", "https://icanhazip.com").forEach { endpoint ->
            try {
                val req = Request.Builder().url(endpoint).get().build()
                val resp = okHttpClient.newCall(req).execute()
                if (resp.isSuccessful) { resp.body?.string()?.trim()?.takeIf { it.isNotBlank() && it.length < 50 && (it.contains(".") || it.contains(":")) }?.let { return it } }
            } catch (_: Exception) {}
        }
        return extractLocalIp()
    }

    private fun extractLocalIp(): String {
        try {
            NetworkInterface.getNetworkInterfaces().toList().mapNotNull { intf ->
                intf.inetAddresses.toList().find { !it.isLoopbackAddress && it is Inet4Address }?.hostAddress
            }.firstOrNull()?.let { return it }
        } catch (_: Exception) {}
        return "N/A"
    }

    fun fetchStrategicIntelligence() {
        // This can now be personalized too in the future!
        val list = if (_isArabic.value) {
            listOf("تجنب منافذ الشحن العامة...", "الهندسة الاجتماعية المتقدمة...", "تجنب الاتصال بشبكات الواي فاي...", "امسح فقط الأكواد التفاعلية QR المضمونة...")
        } else {
            listOf("Avoid public charging ports...", "Advanced social engineering vectors...", "Do not connect to secondary public Wi-Fi...", "QR code phishing (Quishing)..." )
        }
        _intelligenceBrief.value = "[!] ALERT: ${list.random()}"
    }

    fun sendTerminalQuery(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isThinking.value = true
            _terminalResponse.value = if (_isArabic.value) "[ جارٍ الاتصال بالنواة العصبية... ]" else "[ CONNECTING TO NEURAL CORE... ]"
            try {
                val sysContext = if (_isArabic.value)
                    "أنت نظام ذكاء اصطناعي للأمن السيبراني باسم A.SYRIA SOVEREIGN OS v4. أجب بأسلوب تقني احترافي وموجز. الاستعلام: $query"
                else
                    "You are A.SYRIA SOVEREIGN OS v4 cybersecurity AI assistant. Respond concisely in professional technical style. Query: $query"
                val result = generateContentSafely(sysContext)
                _terminalResponse.value = result
                _isNeuralLinkOffline.value = false
                addCyberScore(5)
            } catch (e: Exception) {
                _terminalResponse.value = "[ TERMINAL ALERT ] :Error\n${e.message}"
                _isNeuralLinkOffline.value = true
            } finally {
                _isThinking.value = false
            }
        }
    }

    fun generateAcademyScenarios(
        moduleNameEn: String,
        moduleNameAr: String,
        useArabic: Boolean,
        usedTopics: List<String> = emptyList(),
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            _isAcademyGenerating.value = true
            val apiKey = _customApiKey.value.ifBlank { com.asyria.v4.BuildConfig.GEMINI_API_KEY }
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    val promptLocale = if (useArabic) "Arabic (العربية)" else "English"
                    val seed = System.currentTimeMillis()

                    // --- NEW ADAPTIVE INTELLIGENCE --- 
                    val level = _userLevel.value
                    val interests = _userInterests.value
                    val userProfilePrompt = """
                    USER PROFILE:
                    - Skill Level: $level
                    - Core Interests: ${if (interests.isNotEmpty()) interests.joinToString(", ") else "General Cybersecurity"}
                    """.trimIndent()

                    val topicFocus = if (interests.isNotEmpty()) interests.random() else "General Cybersecurity"

                    val avoidClause = if (usedTopics.isNotEmpty()) {
                        val topicList = usedTopics.take(30).joinToString("\n- ", prefix = "\n- ")
                        """⛔ STRICTLY FORBIDDEN — Do NOT create any scenario similar to or repeating these already-asked topics:
$topicList

Every scenario you produce MUST be on a COMPLETELY DIFFERENT situation, attack vector, and context from the above list."""
                    } else ""

                    val prompt = """
                        You are a Senior Cybersecurity Trainer AI generating FRESH, UNIQUE quiz scenarios.
                        Entropy seed (guarantees uniqueness): [$seed]
                        Language: $promptLocale
                        Module: '$moduleNameEn' / '$moduleNameAr'

                        $userProfilePrompt

                        Your scenarios MUST be tailored to this user's profile.
                        The difficulty and topic MUST align with their specified level and interests.
                        Topic focus for this round: [$topicFocus]

                        $avoidClause

                        Rules:
                        - Scenarios must match the user's skill level. For "Beginner", use clear, simple language and common threats. For "Advanced", use complex, technical, multi-step attack scenarios.
                        - Scenarios must relate to the topic focus.
                        - Each scenario MUST describe a DIFFERENT real-world attack, threat, or security decision.
                        - Options must be plausible, educational, and distinct from each other.
                        - Explanation must teach a concrete security lesson.
                        - NEVER reuse the same story, platform, or attack method across scenarios.

                        Generate exactly 3 unique challenging scenarios in this STRICT JSON format:
                        {
                          "scenarios": [
                            { "id": "s...", "scenario": "...", "options": [...], "correctIndex": ..., "explanation": "..." }
                          ]
                        }

                        Output ONLY valid JSON. No markdown. No extra text outside JSON.
                    """.trimIndent()
                    
                    val cleanRes = generateContentSafely(prompt).replace("```json", "").replace("```", "").trim()
                    if (cleanRes.isNotBlank()) {
                        onSuccess(cleanRes)
                        _isAcademyGenerating.value = false
                        return@launch
                    }
                } catch (e: Exception) {
                    _isAcademyGenerating.value = false
                    onFailure(e)
                    return@launch
                }
            }
            _isAcademyGenerating.value = false
            onFailure(Exception("Neural Link offline or unavailable"))
        }
    }

    fun analyzeResourceLink(url: String) {
        viewModelScope.launch {
            _isAnalyzingLink.value = true
            _linkAnalysisResult.value = null
            try {
                val prompt = if (_isArabic.value) {
                    """أنت محلل أمن سيبراني خبير. حلل هذا الرابط: "$url". قدم: 1. تقييم الأمان (آمن/مشبوه/خطير). 2. تحليل النطاق. 3. توصية للمستخدم. أجب بالعربية بأسلوب موجز."""
                } else {
                    """You are an expert cybersecurity analyst. Analyze this link: "$url". Provide: 1. Security Rating (SAFE/SUSPICIOUS/DANGEROUS). 2. Domain analysis. 3. Actionable user recommendation. Be concise."""
                }
                val result = generateContentSafely(prompt)
                _linkAnalysisResult.value = result
                addCyberScore(10)
            } catch (e: Exception) {
                _linkAnalysisResult.value = "[ SCAN ERROR ]: ${e.message}"
            } finally {
                _isAnalyzingLink.value = false
            }
        }
    }

    fun clearLinkAnalysis() {
        _linkAnalysisResult.value = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravity = event.values
                val (ax, ay) = event.values
                currentRoll += alpha * ((-ax * 4f) - currentRoll)
                currentPitch += alpha * ((ay * 4f) - currentPitch)
                _roll.value = currentRoll
                _pitch.value = currentPitch
            }
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values
        }
        if (gravity != null && geomagnetic != null) {
            val rMatrix = FloatArray(9)
            if (SensorManager.getRotationMatrix(rMatrix, null, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rMatrix, orientation)
                _azimuth.value = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        try { getApplication<Application>().unregisterReceiver(batteryReceiver) } catch (_: Exception) {}
    }
}
