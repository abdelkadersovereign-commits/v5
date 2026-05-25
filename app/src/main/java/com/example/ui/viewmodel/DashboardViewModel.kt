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
import com.example.data.database.SovereigntyCipher
import com.example.data.ContextualVerseEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import com.example.data.SovereignDataStore
import kotlinx.coroutines.flow.collectLatest
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
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class DashboardViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    // Accelerometer / Parallax State
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
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    // Sovereign Context State: Network Interface
    private val _connectionType = MutableStateFlow("Determining...")
    val connectionType: StateFlow<String> = _connectionType.asStateFlow()

    private val _ipAddress = MutableStateFlow("127.0.0.1")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    // Sovereign Context State: Power Info
    private val _batteryPercentage = MutableStateFlow(100)
    val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()

    private val _chargingStatus = MutableStateFlow("UNKNOWN")
    val chargingStatus: StateFlow<String> = _chargingStatus.asStateFlow()

    // Gemini AI and Strategic Core State
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

    // Internet connection status (regardless of Gemini API)
    private val _hasInternetConnection = MutableStateFlow(true)
    val hasInternetConnection: StateFlow<Boolean> = _hasInternetConnection.asStateFlow()

    private val _isTestingKey = MutableStateFlow(false)
    val isTestingKey: StateFlow<Boolean> = _isTestingKey.asStateFlow()

    private val _linkAnalysisResult = MutableStateFlow<String?>(null)
    val linkAnalysisResult: StateFlow<String?> = _linkAnalysisResult.asStateFlow()

    private val _isAnalyzingLink = MutableStateFlow(false)
    val isAnalyzingLink: StateFlow<Boolean> = _isAnalyzingLink.asStateFlow()

    // Local Persistence Infrastructure: Room Setup
    private val database = AppDatabase.getDatabase(application)
    private val repository = InventorIdeaRepository(database.inventorIdeaDao())

    // UI state flows for Room list view
    val savedIdeas: StateFlow<List<InventorIdea>> = repository.allIdeas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Ambient Contextual Wisdom flow combining system telemetry
    val ambientInsight: StateFlow<ContextualVerseEngine.SpiritualInsight> = combine(
        _batteryPercentage,
        _chargingStatus,
        _connectionType
    ) { battery, charging, connection ->
        val isCharging = charging == "CHARGING"
        ContextualVerseEngine.getInsight(battery, isCharging, connection)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ContextualVerseEngine.getInsight(100, false, "Determining...")
    )

    // Layout panels overlays state
    private val _isForgePanelOpen = MutableStateFlow(false)
    val isForgePanelOpen: StateFlow<Boolean> = _isForgePanelOpen.asStateFlow()

    private val _isVaultViewOpen = MutableStateFlow(false)
    val isVaultViewOpen: StateFlow<Boolean> = _isVaultViewOpen.asStateFlow()

    private val _isVaultAuthenticated = MutableStateFlow(false)
    val isVaultAuthenticated: StateFlow<Boolean> = _isVaultAuthenticated.asStateFlow()

    private val _isSettingsAuthenticated = MutableStateFlow(false)
    val isSettingsAuthenticated: StateFlow<Boolean> = _isSettingsAuthenticated.asStateFlow()

    fun setVaultAuthenticated(auth: Boolean) {
        _isVaultAuthenticated.value = auth
    }

    fun setSettingsAuthenticated(auth: Boolean) {
        _isSettingsAuthenticated.value = auth
    }

    // Phase 7 Settings states and overlays
    private val dataStore = SovereignDataStore(application)

    // Phase 11: Cyber-Rank System & Academy Visibility states
    private val _isArabic = MutableStateFlow(true)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    fun setArabic(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.saveIsArabic(enabled)
            // Phase 26: fetchStrategicIntelligence() removed to protect API quota
        }
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
        viewModelScope.launch {
            dataStore.saveCyberScore(_cyberScore.value + points)
        }
    }

    fun setAcademyOpen(open: Boolean) {
        _isAcademyOpen.value = open
    }

    // Academy progress persistence (survives tab navigation)
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

    fun setResourcesOpen(open: Boolean) {
        _isResourcesOpen.value = open
    }

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

    // DoH (DNS-over-HTTPS) resolver: bypasses ISP DNS filtering in restricted regions
    // Uses Cloudflare and Google DoH as fallbacks - no VPN needed
    private val dohClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val bypassDns = object : okhttp3.Dns {
        override fun lookup(hostname: String): List<java.net.InetAddress> {
            // Try system DNS first (fast path)
            try {
                val result = okhttp3.Dns.SYSTEM.lookup(hostname)
                if (result.isNotEmpty()) return result
            } catch (_: Exception) {}
            // Fallback: Cloudflare DoH
            for (dohUrl in listOf(
                "https://1.1.1.1/dns-query?name=$hostname&type=A",
                "https://8.8.8.8/resolve?name=$hostname&type=A"
            )) {
                try {
                    val req = okhttp3.Request.Builder().url(dohUrl)
                        .header("Accept", "application/dns-json").build()
                    val resp = dohClient.newCall(req).execute()
                    val body = resp.body?.string() ?: continue
                    val json = org.json.JSONObject(body)
                    val answers = json.optJSONArray("Answer") ?: continue
                    val addrs = mutableListOf<java.net.InetAddress>()
                    for (i in 0 until answers.length()) {
                        val a = answers.getJSONObject(i)
                        if (a.optInt("type") == 1) {
                            try { addrs.add(java.net.InetAddress.getByName(a.getString("data"))) } catch (_: Exception) {}
                        }
                    }
                    if (addrs.isNotEmpty()) return addrs
                } catch (_: Exception) {}
            }
            throw java.net.UnknownHostException("Could not resolve $hostname")
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .dns(bypassDns)
        .build()

    private suspend fun generateContentSafely(prompt: String): String {
        return withContext(Dispatchers.IO) {
            val apiKey = if (_customApiKey.value.isNotBlank()) _customApiKey.value else com.asyria.v4.BuildConfig.GEMINI_API_KEY
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
            put("contents", org.json.JSONArray().put(
                JSONObject().apply {
                    put("parts", org.json.JSONArray().put(
                        JSONObject().put("text", prompt)
                    ))
                }
            ))
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())

        // Neural Proxy mode: use alternative endpoint routing with extended timeouts and bypass headers
        val proxyEnabled = _isNeuralProxy.value
        // gemini-2.5-flash-lite: confirmed working, free tier, lowest quota usage
        val endpointUrls = listOf(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent"
        )

        val activeClient = if (proxyEnabled) {
            OkHttpClient.Builder()
                .connectTimeout(45, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
        } else {
            okHttpClient
        }

        var lastException: Exception? = null
        for (url in endpointUrls) {
            try {
                val requestBuilder = Request.Builder()
                    .url(url)
                    .post(body)
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)

                if (proxyEnabled) {
                    requestBuilder
                        .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 14; SM-G998B Build/UP1A.231005.007)")
                        .header("Accept-Language", "en-US,en;q=0.9")
                }
                // Note: AI Studio API keys do NOT require project headers
                // Only x-goog-api-key is needed for generativelanguage.googleapis.com

                val request = requestBuilder.build()
                val response = activeClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    val code = response.code
                    val errBody = response.body?.string() ?: ""
                    if (code == 429) throw Exception("Neural Buffer Full (Error 429) - يتم الآن إعادة شحن الطاقة العصبية.. يرجى الانتظار 30 ثانية")
                    if (code == 503) throw Exception("UPLINK BUSY - السيرفر مشغول حالياً، يرجى المحاولة بعد قليل")
                    lastException = Exception(if (code == 403) "Regional Block - Enable Neural Proxy" else if (code == 401) "Invalid API Key or Project ID" else "Error $code: $errBody")
                    continue
                }

                val resBody = response.body?.string() ?: ""
                val jsonRes = JSONObject(resBody)

                val usage = jsonRes.optJSONObject("usageMetadata")
                if (usage != null) {
                    val totalTokens = usage.optInt("totalTokenCount")
                    val promptTokens = usage.optInt("promptTokenCount")
                    val candidateTokens = usage.optInt("candidatesTokenCount")
                    _terminalResponse.value += "\n\n[ NEURAL LOAD: $totalTokens Tokens (In: $promptTokens, Out: $candidateTokens) ]"
                }

                return jsonRes.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            } catch (e: Exception) {
                if (e.message?.contains("429") == true || e.message?.contains("Neural Buffer") == true) throw e
                if (e.message?.contains("UPLINK BUSY") == true) throw e
                lastException = e
            }
        }
        throw lastException ?: Exception("Neural link failed")
    }


    fun setStealthMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.saveStealthMode(enabled)
            _isStealthMode.value = enabled
        }
    }

    fun setNeuralProxy(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.saveNeuralProxy(enabled)
            _isNeuralProxy.value = enabled
        }
    }

    fun updateCustomApiKey(key: String) {
        viewModelScope.launch {
            dataStore.saveGeminiApiKey(key)
        }
    }

    fun updateProjectName(name: String) {
        viewModelScope.launch {
            dataStore.saveProjectName(name)
        }
    }

    fun updateProjectId(id: String) {
        viewModelScope.launch {
            dataStore.saveProjectId(id)
        }
    }

    fun updateProjectNumber(number: String) {
        viewModelScope.launch {
            dataStore.saveProjectNumber(number)
        }
    }

    fun updateOperatorName(name: String) {
        viewModelScope.launch {
            dataStore.saveOperatorName(name)
        }
    }

    fun updateNeuralRole(role: String) {
        viewModelScope.launch {
            dataStore.saveNeuralRole(role)
        }
    }

    fun setSettingsOpen(open: Boolean) {
        _isSettingsOpen.value = open
    }

    // Forge interface inputs state
    private val _forgeTitle = MutableStateFlow("")
    val forgeTitle: StateFlow<String> = _forgeTitle.asStateFlow()

    private val _forgeCategory = MutableStateFlow("SecOps")
    val forgeCategory: StateFlow<String> = _forgeCategory.asStateFlow()

    private val _forgeIdea = MutableStateFlow("")
    val forgeIdea: StateFlow<String> = _forgeIdea.asStateFlow()

    private val _forgeBlueprint = MutableStateFlow("")
    val forgeBlueprint: StateFlow<String> = _forgeBlueprint.asStateFlow()

    private var monitoringJob: Job? = null

    // Fallback security and news items if offline/no key
    private val offlineBriefs = listOf(
        "OVERWATCH PROTOCOL ACTIVE: Enforce zero-trust architecture across local visual segments.",
        "CYBER SHIELD UPDATE: Core firewall active. Security levels in southern sectors stabilized.",
        "COGNITIVE INTEL: Encrypt terminal tunnels using local neural asymmetric signatures.",
        "SATELLITE WARNING: Quantum vector injection attempts detected in perimeter. No breach.",
        "SECURE TELEMETRY: Decentralize visual frame buffers to resist spatial memory leakage.",
        "COGNITIVE INNOVATION: Isolated sandbox environments deployed for tactical analytical runs."
    )

    // Fallback query responses for typewriter strategic terminal
    private val offlineTerminalResponses = listOf(
        "COGNITIVE ANALYSIS: Target structure verified with optimal efficiency. Standard microservices alignment meets expectations. Recommendation: Deploy redundant localized storage clusters to satisfy regional failover requirements.",
        "STRATEGIC BRIEF: Unified sovereign mainframe online. System dependencies compiled successfully. High threat prevention active. Direct actions suggest reinforcing container isolation and limiting remote API handshakes.",
        "TACTICAL SYSTEM: Core network interfaces are nominal. Rebalancing resources to visual core processors. Recommended path: Proceed with Phase 5 synchronization while keeping high-density firewalls online."
    )

    private val fallbackBlueprints = listOf(
        "SOVEREIGN ENGINEERING BLUEPRINT v1.0\n\nCATEGORY: %CATEGORY%\nPROJECT TITLE: %TITLE%\n\n[1] MISSION CORE LOGIC\nDecentralized sandbox nodes with local encrypted persistence keys. Real-time data validation via SHA-256 verification rings.\n\n[2] SYSTEM INVENTORY\n- Asymmetric key store\n- Local Room encrypted SQLite memory buffers\n- Gemini edge analytical co-processors\n\n[3] RISK ASSESSMENT\n- Remote vector interception: Mitigation via localized network offline locks.\n- Physical side-channel leaks: Mitigation via automatic memory purge routines.",
        "COGNITIVE DEPLOYMENT DOCUMENTATION\n\nSPEC: %TITLE% (%CATEGORY%)\n\n[1] ARCHITECTURAL SPECIFICATION\nHigh efficiency parallel analytical loops running inside clean-room containers. Strict state matching ensures atomic operations.\n\n[2] IMPLEMENTATION PIPELINE\n- Core MVVM State Engine\n- Parallax rendering thread\n- Local hardware security modules\n\n[3] PERIMETER ANALYSIS\n- Dependency collision: Low risk; isolated workspace checks active.\n- Local storage leakage: Secured via Sovereignty Symmetric AES ciphers."
    )

    // Register battery state broadcast receiver safely
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) {
                    _batteryPercentage.value = ((level.toFloat() / scale.toFloat()) * 100).toInt()
                }

                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
                _chargingStatus.value = if (isCharging) "CHARGING" else "DISCHARGING"
            }
        }
    }

    fun testNeuralLink() {
        viewModelScope.launch {
            _isTestingKey.value = true
            val apiKey = if (_customApiKey.value.isNotBlank()) _customApiKey.value else com.asyria.v4.BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey.contains("MY_GEMINI_API_KEY")) {
                _isNeuralLinkOffline.value = true
                _terminalResponse.value = "[ TERMINAL ALERT ] Gemini API Key is required. Get yours free at: aistudio.google.com/app/apikey"
                _isTestingKey.value = false
                return@launch
            }

            try {
                kotlinx.coroutines.withTimeout(15000) {
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
        // Register Battery Level
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        application.registerReceiver(batteryReceiver, filter)

        // Spawn central ticker loop for active counters and state poller
        viewModelScope.launch {
            dataStore.isArabic.collect { _isArabic.value = it }
        }
        viewModelScope.launch {
            dataStore.stealthMode.collect { _isStealthMode.value = it }
        }
        viewModelScope.launch {
            dataStore.cyberScore.collect { _cyberScore.value = it }
        }
        viewModelScope.launch {
            dataStore.geminiApiKey.collect { 
                _customApiKey.value = it 
            }
        }
        viewModelScope.launch {
            dataStore.projectName.collect { _projectName.value = it }
        }
        viewModelScope.launch {
            dataStore.projectId.collect { _projectId.value = it }
        }
        viewModelScope.launch {
            dataStore.projectNumber.collect { _projectNumber.value = it }
        }
        viewModelScope.launch {
            dataStore.operatorName.collect { _operatorName.value = it }
        }
        viewModelScope.launch {
            dataStore.neuralRole.collect { _neuralRole.value = it }
        }
        viewModelScope.launch {
            dataStore.neuralProxy.collect { _isNeuralProxy.value = it }
        }

        // Fetch public IP immediately on start
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _ipAddress.value = fetchPublicIp()
            } catch (_: Exception) {
                _ipAddress.value = extractLocalIp()
            }
        }
    }

    // Phase 26: Removed background polling to ensure API key remains dormant
    // private fun startNeuralServices() { ... }

    // Navigation and Layout Actions
    fun setTerminalExpanded(expanded: Boolean) {
        _isTerminalExpanded.value = expanded
        if (!expanded) {
            _terminalInput.value = ""
            _terminalResponse.value = ""
        }
    }

    fun setForgePanelOpen(open: Boolean) {
        _isForgePanelOpen.value = open
        if (!open) {
            clearForgeFields()
        }
    }

    fun setVaultViewOpen(open: Boolean) {
        _isVaultViewOpen.value = open
    }

    // Input state mutations
    fun updateTerminalInput(input: String) {
        _terminalInput.value = input
    }

    fun updateForgeTitle(title: String) {
        _forgeTitle.value = title
    }

    fun updateForgeCategory(category: String) {
        _forgeCategory.value = category
    }

    fun updateForgeIdea(idea: String) {
        _forgeIdea.value = idea
    }

    private fun clearForgeFields() {
        _forgeTitle.value = ""
        _forgeCategory.value = "SecOps"
        _forgeIdea.value = ""
        _forgeBlueprint.value = ""
    }

    // Local Database Actions
    fun deleteIdea(ideaId: Int) {
        viewModelScope.launch {
            repository.deleteIdeaById(ideaId)
        }
    }

    fun wipeVault() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // Task 2: AI Forging System - formats design blueprints and stores in secure Room database
    fun forgeAndSaveIdea() {
        val title = _forgeTitle.value.trim()
        val category = _forgeCategory.value.trim()
        val ideaRaw = _forgeIdea.value.trim()

        if (title.isEmpty() || ideaRaw.isEmpty()) return

        viewModelScope.launch {
            _isThinking.value = true
            _forgeBlueprint.value = ""

            // Phase 26: AI Blueprinting removed to save quota
            val generatedBlueprint = "[ ALERT ] Neural Blueprinting is offline. Local Encryption protocol used instead."

            // Let character stream trigger live on screen UI updates
            _isThinking.value = false
            
            // Build and persist actual encrypted Room database entity
            val encryptedEntity = InventorIdea.createEncrypted(
                title = title,
                category = category,
                originalIdea = ideaRaw,
                geminiBlueprint = generatedBlueprint
            )
            repository.insertIdea(encryptedEntity)

            _forgeBlueprint.value = generatedBlueprint
        }
    }

    private fun pollNetworkIntel() {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        // Check for basic internet capability - being less strict to avoid "false offline" states
        val hasInternet = capabilities != null &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        // Update internet connection status
        _hasInternetConnection.value = hasInternet

        if (hasInternet) {
            when {
                capabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_VPN) &&
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    _connectionType.value = "VPN+WIFI (ENCRYPTED)"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) &&
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    _connectionType.value = "VPN+CELLULAR (ENCRYPTED)"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                    _connectionType.value = "VPN (TUNNELED)"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    _connectionType.value = "WIFI (SECURE)"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    _connectionType.value = "CELLULAR (LTE/5G)"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    _connectionType.value = "ETHERNET (WIRED)"
                }
                else -> {
                    _connectionType.value = "CONNECTED"
                }
            }
            // Fetch real public IP in background
            viewModelScope.launch(Dispatchers.IO) {
                _ipAddress.value = fetchPublicIp()
            }
        } else if (capabilities != null) {
            // Network exists but no validated internet (e.g. captive portal)
            _connectionType.value = "NO_INTERNET (SHIELDED)"
            _ipAddress.value = extractLocalIp()
        } else {
            _connectionType.value = "DISCONNECTED"
            _ipAddress.value = "N/A"
        }
    }

    private fun fetchPublicIp(): String {
        val endpoints = listOf(
            "https://api64.ipify.org",
            "https://checkip.amazonaws.com",
            "https://ifconfig.me/ip",
            "https://icanhazip.com"
        )
        for (endpoint in endpoints) {
            try {
                val req = Request.Builder()
                    .url(endpoint)
                    .get()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept", "text/plain")
                    .build()
                
                val resp = okHttpClient.newCall(req).execute()
                if (resp.isSuccessful) {
                    val ip = resp.body?.string()?.trim() ?: continue
                    // Basic validation to ensure it looks like an IP (v4 or v6)
                    if (ip.isNotBlank() && ip.length < 50 && (ip.contains(".") || ip.contains(":"))) {
                        return ip
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return extractLocalIp()
    }

    private fun extractLocalIp(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "N/A"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "N/A"
    }

    // Phase 26: fetchStrategicIntelligence() removed to protect API quota
    fun fetchStrategicIntelligence() {
        val list = if (_isArabic.value) {
            listOf(
                "تجنب منافذ الشحن العامة بالمطارات لحماية جهازك الرقمي من الاختراقات السلكية الحيوية.",
                "الهندسة الاجتماعية المتقدمة تستهدف مستخدمي كود واتساب للتلفيق واستلام حساباتهم.",
                "تجنب الاتصال بشبكات الواي فاي العامة دون تفعيل نفق اتصال مشفر مسبقاً.",
                "امسح فقط الأكواد التفاعلية QR المضمونة لتفادي برمجيات الفيشينغ الخبيثة التلقائية."
            )
        } else {
            listOf(
                "Avoid public charging ports (Juice Jacking) to secure local device data hardware.",
                "Advanced social engineering vectors target active WhatsApp authentication pins.",
                "Do not connect to secondary public Wi-Fi zones without VPN tunnels verified.",
                "QR code phishing (Quishing) executes automatic remote malicious software payloads."
            )
        }
        _intelligenceBrief.value = "[!] ALERT: ${list[Random.nextInt(list.size)]}"
    }

    // Task 3: Interactive query execution on Gemini. Uses real simulated satellite streaming typewriter visual
    fun sendTerminalQuery(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isThinking.value = true
            _terminalResponse.value = if (_isArabic.value) "[ جارٍ الاتصال بالنواة العصبية... ]" else "[ CONNECTING TO NEURAL CORE... ]"
            try {
                val isAr = _isArabic.value
                val sysContext = if (isAr)
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
        usedIds: List<String> = emptyList(),
        usedTopics: List<String> = emptyList(),
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            _isAcademyGenerating.value = true
            val apiKey = if (_customApiKey.value.isNotBlank()) _customApiKey.value else com.asyria.v4.BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    val promptLocale = if (useArabic) "Arabic (العربية)" else "English"
                    val seed = System.currentTimeMillis()

                    val allCategories = listOf(
                        "الهندسة الاجتماعية وانتحال الهوية",
                        "أمن الشبكات وهجمات MITM",
                        "التشفير والمفاتيح الرقمية",
                        "اختراق الهواتف والتطبيقات",
                        "التصيد الاحتيالي عبر البريد والرسائل",
                        "برمجيات الفدية والفيروسات",
                        "الجريمة الإلكترونية والقانون",
                        "أمن كلمات المرور والمصادقة الثنائية",
                        "الخصوصية الرقمية وتتبع البيانات",
                        "ثغرات التطبيقات وCVE",
                        "الـ Dark Web والشبكات المجهولة",
                        "هجمات التشفير والبلوكشين"
                    )
                    // Pick a category that hasn't been picked before this session
                    val randomCategory = allCategories[Random.nextInt(allCategories.size)]

                    // Build a real avoid clause using actual past scenario text snippets
                    val avoidClause = if (usedTopics.isNotEmpty()) {
                        val topicList = usedTopics.take(30).joinToString("\n- ", prefix = "\n- ")
                        """
                        ⛔ STRICTLY FORBIDDEN — Do NOT create any scenario similar to or repeating these already-asked topics:
                        $topicList
                        
                        Every scenario you produce MUST be on a COMPLETELY DIFFERENT situation, attack vector, and context from the above list. 
                        Do not use the same characters, companies, or specific phishing lures mentioned above.
                        """.trimIndent()
                    } else ""

                    val prompt = """
                        You are a Senior Cybersecurity Trainer AI generating FRESH, UNIQUE quiz scenarios.
                        Entropy seed (guarantees uniqueness): [$seed]
                        Topic focus this round: [$randomCategory]
                        Language: $promptLocale
                        Module: '$moduleNameEn' / '$moduleNameAr'
                        
                        $avoidClause
                        
                        Rules:
                        - Each scenario MUST describe a DIFFERENT real-world attack, threat, or security decision.
                        - Cover varied attack vectors: phishing, MITM, malware, social engineering, password, privacy, etc.
                        - Options must be plausible, educational, and distinct from each other.
                        - Explanation must teach a concrete security lesson.
                        - NEVER reuse the same story, platform, or attack method across scenarios.
                        - Focus on extremely specific and diverse technical details to ensure variety.
                        
                        Generate exactly 3 unique challenging scenarios in this STRICT JSON:
                        {
                          "scenarios": [
                            {
                              "id": "s${seed}_1",
                              "scenario": "Detailed realistic attack scenario...",
                              "options": ["Option A", "Option B", "Option C", "Option D"],
                              "correctIndex": 0,
                              "explanation": "Security lesson explaining why..."
                            }
                          ]
                        }
                        
                        Output ONLY valid JSON. No markdown. No extra text outside JSON.
                    """.trimIndent()
                    val res = generateContentSafely(prompt)
                    // Remove markdown wrapper if any
                    val cleanRes = res.replace("```json", "").replace("```", "").trim()
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

    // Phase 26: generateStrategicDebrief() removed; explanation is now part of AcademyScenario
    fun generateStrategicDebrief(scenario: String, choiceText: String, useArabic: Boolean, onResponse: (String) -> Unit) {
        onResponse("")
    }

    fun analyzeResourceLink(url: String, resourceTitle: String = "Unknown Reference") {
        viewModelScope.launch {
            _isAnalyzingLink.value = true
            _linkAnalysisResult.value = null
            try {
                val isAr = _isArabic.value
                val prompt = if (isAr) {
                    """أنت محلل أمن سيبراني خبير. حلل هذا الرابط أو المورد: "$url"
قدم:
1. ⚠️ تقييم الأمان: آمن / مشبوه / خطير
2. 🔍 تحليل النطاق والمؤشرات التقنية
3. 🛡️ توصية للمستخدم
أجب بالعربية بأسلوب تقني موجز."""
                } else {
                    """You are an expert cybersecurity analyst. Analyze this link/resource: "$url"
Provide:
1. ⚠️ Security Rating: SAFE / SUSPICIOUS / DANGEROUS
2. 🔍 Domain & technical indicator analysis
3. 🛡️ Actionable user recommendation
Be concise and direct."""
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
                val ax = event.values[0]
                val ay = event.values[1]

                val targetRoll = -ax * 4f
                val targetPitch = ay * 4f

                currentRoll = currentRoll + alpha * (targetRoll - currentRoll)
                currentPitch = currentPitch + alpha * (targetPitch - currentPitch)

                _roll.value = currentRoll
                _pitch.value = currentPitch
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic = event.values
            }
        }

        if (gravity != null && geomagnetic != null) {
            val rMatrix = FloatArray(9)
            val iMatrix = FloatArray(9)
            if (SensorManager.getRotationMatrix(rMatrix, iMatrix, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rMatrix, orientation)
                var az = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (az < 0) az += 360f
                _azimuth.value = az
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        monitoringJob?.cancel()
    }
}
