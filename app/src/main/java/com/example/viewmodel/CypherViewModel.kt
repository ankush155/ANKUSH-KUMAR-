package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AiCyberAssistantEngine
import com.example.engine.AiMessage
import com.example.engine.AiSender
import com.example.engine.CryptoEngine
import com.example.engine.HttpReconEngine
import com.example.engine.NetworkScannerEngine
import com.example.engine.TerminalCommandExecutor
import com.example.model.CheatSheetItem
import com.example.model.CyberTheme
import com.example.model.HashIdentification
import com.example.model.HashResult
import com.example.model.HttpAnalysisResult
import com.example.model.LineType
import com.example.model.PortScanResult
import com.example.model.SavedScript
import com.example.model.TerminalLine
import com.example.model.TerminalSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CypherUiState(
    val activeTab: Int = 0, // 0: Terminal, 1: AI Co-Pilot, 2: Cyber Arsenal, 3: Cheatsheets, 4: Script Studio
    val sessions: List<TerminalSession> = listOf(
        TerminalSession(
            id = "sess-1",
            title = "Deck #1",
            lines = listOf(
                TerminalLine(
                    text = "═══════════════════════════════════════════════════════\n⚡ CYPHERDECK ULTIMATE TERMINAL & ETHICAL CYBER OPS v2.4 ⚡\n═══════════════════════════════════════════════════════",
                    type = LineType.SYSTEM
                ),
                TerminalLine(
                    text = "[+] CyberDeck Kernel v5.15-zen initialized with Nova AI Co-Pilot",
                    type = LineType.SUCCESS
                ),
                TerminalLine(
                    text = "[+] Architecture: ARM64 / x86_64 Dual Simulation Engine Active",
                    type = LineType.INFO
                ),
                TerminalLine(
                    text = "Type 'help' for available tools or 'ai <prompt>' to ask Nova AI.",
                    type = LineType.OUTPUT
                )
            )
        )
    ),
    val activeSessionId: String = "sess-1",
    val commandInput: String = "",
    val historyIndex: Int = -1,
    val isExecutingCommand: Boolean = false,
    val activeTheme: CyberTheme = CyberTheme.CYBERPUNK_NEON,
    val matrixRainActive: Boolean = false,
    val terminalFontSizeSp: Int = 13,
    val scanlinesActive: Boolean = true,
    // AI Chat
    val aiMessages: List<AiMessage> = listOf(
        AiMessage(
            sender = AiSender.CYBER_AI,
            text = "⚡ Greetings, Operator. I am Nova AI, your cyber security co-pilot. I can generate attack/defense scripts, explain Nmap/Wireshark flags, audit vulnerabilities, or craft custom recon commands. What are we analyzing today?"
        )
    ),
    val aiInputText: String = "",
    val isAiThinking: Boolean = false,
    // Arsenal: Port Scanner
    val portScanTarget: String = "google.com",
    val isScanningPorts: Boolean = false,
    val portScanProgress: String = "",
    val portScanResult: PortScanResult? = null,
    // Arsenal: HTTP Recon
    val httpTargetUrl: String = "https://github.com",
    val isAnalyzingHttp: Boolean = false,
    val httpAnalysisResult: HttpAnalysisResult? = null,
    // Arsenal: Crypto
    val cryptoInputText: String = "CypherDeckSecure2026",
    val currentHashResult: HashResult? = null,
    val identifiedHash: HashIdentification? = null,
    val jwtInput: String = "",
    val jwtDecodedOutput: String = "",
    // Cheatsheets
    val cheatsheetQuery: String = "",
    val selectedCheatCategory: String = "All",
    // Script Studio
    val savedScripts: List<SavedScript> = emptyList(),
    val currentEditingScript: SavedScript? = null
)

class CypherViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CypherUiState())
    val uiState: StateFlow<CypherUiState> = _uiState.asStateFlow()

    private val commandExecutor = TerminalCommandExecutor(application)

    init {
        initDefaultScripts()
        calculateDefaultHash()
    }

    private fun calculateDefaultHash() {
        val defaultHash = CryptoEngine.hashAll("CypherDeckSecure2026")
        _uiState.update { it.copy(currentHashResult = defaultHash) }
    }

    fun setActiveTab(index: Int) {
        _uiState.update { it.copy(activeTab = index) }
    }

    fun setTheme(theme: CyberTheme) {
        _uiState.update { it.copy(activeTheme = theme) }
    }

    fun toggleMatrixRain() {
        _uiState.update { it.copy(matrixRainActive = !it.matrixRainActive) }
    }

    fun toggleScanlines() {
        _uiState.update { it.copy(scanlinesActive = !it.scanlinesActive) }
    }

    fun setTerminalFontSize(sp: Int) {
        _uiState.update { it.copy(terminalFontSizeSp = sp) }
    }

    fun onCommandInputChange(text: String) {
        _uiState.update { it.copy(commandInput = text, historyIndex = -1) }
    }

    fun appendToCommand(token: String) {
        _uiState.update {
            val current = it.commandInput
            val updated = if (current.isEmpty()) token else "$current$token"
            it.copy(commandInput = updated)
        }
    }

    fun addTerminalSession() {
        val count = _uiState.value.sessions.size + 1
        val newId = "sess-$count"
        val newSession = TerminalSession(
            id = newId,
            title = "Deck #$count",
            lines = listOf(
                TerminalLine(
                    text = "[+] Session #$count opened. Ready for commands.",
                    type = LineType.SUCCESS
                )
            )
        )
        _uiState.update {
            it.copy(
                sessions = it.sessions + newSession,
                activeSessionId = newId
            )
        }
    }

    fun switchSession(sessionId: String) {
        _uiState.update { it.copy(activeSessionId = sessionId) }
    }

    fun closeSession(sessionId: String) {
        val current = _uiState.value.sessions
        if (current.size <= 1) return
        val filtered = current.filter { it.id != sessionId }
        val newActive = if (_uiState.value.activeSessionId == sessionId) filtered.first().id else _uiState.value.activeSessionId
        _uiState.update { it.copy(sessions = filtered, activeSessionId = newActive) }
    }

    fun navigateHistory(up: Boolean) {
        val session = getCurrentSession() ?: return
        val history = session.history
        if (history.isEmpty()) return

        val currentIndex = _uiState.value.historyIndex
        val nextIndex = if (up) {
            if (currentIndex == -1) history.size - 1 else (currentIndex - 1).coerceAtLeast(0)
        } else {
            if (currentIndex == -1 || currentIndex >= history.size - 1) -1 else currentIndex + 1
        }

        val text = if (nextIndex in history.indices) history[nextIndex] else ""
        _uiState.update { it.copy(commandInput = text, historyIndex = nextIndex) }
    }

    fun executeTerminalCommand(customCmd: String? = null) {
        val cmd = (customCmd ?: _uiState.value.commandInput).trim()
        if (cmd.isEmpty()) return

        val activeId = _uiState.value.activeSessionId
        val activeTheme = _uiState.value.activeTheme
        val currentSession = getCurrentSession() ?: return

        _uiState.update {
            it.copy(
                commandInput = "",
                historyIndex = -1,
                isExecutingCommand = true
            )
        }

        // Add to history
        updateSession(activeId) { sess ->
            val updatedHistory = if (sess.history.lastOrNull() != cmd) sess.history + cmd else sess.history
            sess.copy(history = updatedHistory)
        }

        if (cmd.equals("clear", ignoreCase = true) || cmd.equals("cls", ignoreCase = true)) {
            updateSession(activeId) { sess ->
                sess.copy(lines = emptyList())
            }
            _uiState.update { it.copy(isExecutingCommand = false) }
            return
        }

        viewModelScope.launch {
            commandExecutor.execute(
                rawCommand = cmd,
                currentDir = currentSession.currentWorkingDirectory,
                activeTheme = activeTheme,
                emitLine = { newLine ->
                    updateSession(activeId) { sess ->
                        sess.copy(lines = sess.lines + newLine)
                    }
                }
            )
            _uiState.update { it.copy(isExecutingCommand = false) }
        }
    }

    private fun getCurrentSession(): TerminalSession? {
        val id = _uiState.value.activeSessionId
        return _uiState.value.sessions.firstOrNull { it.id == id }
    }

    private fun updateSession(sessionId: String, update: (TerminalSession) -> TerminalSession) {
        _uiState.update { state ->
            val updated = state.sessions.map {
                if (it.id == sessionId) update(it) else it
            }
            state.copy(sessions = updated)
        }
    }

    // AI Assistant actions
    fun onAiInputChange(text: String) {
        _uiState.update { it.copy(aiInputText = text) }
    }

    fun sendAiPrompt(customPrompt: String? = null) {
        val prompt = (customPrompt ?: _uiState.value.aiInputText).trim()
        if (prompt.isEmpty()) return

        val userMsg = AiMessage(sender = AiSender.USER, text = prompt)
        _uiState.update {
            it.copy(
                aiMessages = it.aiMessages + userMsg,
                aiInputText = "",
                isAiThinking = true
            )
        }

        viewModelScope.launch {
            val response = AiCyberAssistantEngine.query(prompt)
            _uiState.update {
                it.copy(
                    aiMessages = it.aiMessages + response,
                    isAiThinking = false
                )
            }
        }
    }

    // Cyber Arsenal: Port Scanner
    fun onPortScanTargetChange(target: String) {
        _uiState.update { it.copy(portScanTarget = target) }
    }

    fun runPortScan() {
        val target = _uiState.value.portScanTarget.trim()
        if (target.isEmpty()) return

        _uiState.update {
            it.copy(
                isScanningPorts = true,
                portScanProgress = "Initializing scanner...",
                portScanResult = null
            )
        }

        viewModelScope.launch {
            val result = NetworkScannerEngine.scanPorts(
                target = target,
                onProgress = { current, total, found ->
                    _uiState.update {
                        it.copy(portScanProgress = "Scanned $current/$total ports... (${resultDescription(found)})")
                    }
                }
            )
            _uiState.update {
                it.copy(
                    isScanningPorts = false,
                    portScanResult = result,
                    portScanProgress = "Completed in ${result.scanDurationMs}ms"
                )
            }
        }
    }

    private fun resultDescription(port: com.example.model.OpenPort?): String {
        return if (port != null) "Found ${port.serviceName}:${port.port}" else "Scanning"
    }

    // Cyber Arsenal: HTTP Recon
    fun onHttpTargetUrlChange(url: String) {
        _uiState.update { it.copy(httpTargetUrl = url) }
    }

    fun runHttpRecon() {
        val url = _uiState.value.httpTargetUrl.trim()
        if (url.isEmpty()) return

        _uiState.update { it.copy(isAnalyzingHttp = true, httpAnalysisResult = null) }
        viewModelScope.launch {
            val result = HttpReconEngine.analyzeUrl(url)
            _uiState.update { it.copy(isAnalyzingHttp = false, httpAnalysisResult = result) }
        }
    }

    // Cyber Arsenal: Crypto
    fun onCryptoInputChange(text: String) {
        val hashes = CryptoEngine.hashAll(text)
        _uiState.update { it.copy(cryptoInputText = text, currentHashResult = hashes) }
    }

    fun identifyHash(hash: String) {
        val id = CryptoEngine.identifyHash(hash)
        _uiState.update { it.copy(identifiedHash = id) }
    }

    fun onJwtInputChange(token: String) {
        val decoded = CryptoEngine.decodeJwt(token)
        _uiState.update { it.copy(jwtInput = token, jwtDecodedOutput = decoded) }
    }

    // Cheatsheets
    fun onCheatsheetQueryChange(query: String) {
        _uiState.update { it.copy(cheatsheetQuery = query) }
    }

    fun onCheatCategorySelect(category: String) {
        _uiState.update { it.copy(selectedCheatCategory = category) }
    }

    // Script Studio
    fun selectScriptForEdit(script: SavedScript) {
        _uiState.update { it.copy(currentEditingScript = script) }
    }

    fun updateCurrentScriptContent(newContent: String) {
        _uiState.update {
            val updated = it.currentEditingScript?.copy(content = newContent, updatedAt = System.currentTimeMillis())
            it.copy(currentEditingScript = updated)
        }
    }

    fun saveCurrentScript() {
        val script = _uiState.value.currentEditingScript ?: return
        _uiState.update { state ->
            val updatedList = state.savedScripts.map {
                if (it.id == script.id) script else it
            }
            state.copy(savedScripts = updatedList)
        }
    }

    fun runScriptInTerminal(script: SavedScript) {
        _uiState.update { it.copy(activeTab = 0) } // Switch to terminal
        executeTerminalCommand("echo \"[RUNNING SCRIPT: ${script.name}]\"")
        val lines = script.content.lines().filter { it.isNotBlank() && !it.startsWith("#") }
        lines.take(3).forEach {
            executeTerminalCommand(it)
        }
    }

    private fun initDefaultScripts() {
        val scripts = listOf(
            SavedScript(
                name = "Port Sweep & Recon",
                description = "Probes top ports and retrieves HTTP response headers",
                language = "bash",
                content = """
                # CypherDeck Fast Recon Automation
                target="scanme.nmap.org"
                echo "Scanning target: ${'$'}target"
                portscan ${'$'}target
                curl https://${'$'}target
                dig ${'$'}target
                """.trimIndent()
            ),
            SavedScript(
                name = "Subnet Discovery Sweep",
                description = "Identifies active devices and router on local Wi-Fi",
                language = "bash",
                content = """
                # Subnet Discovery Script
                echo "Scanning local subnet topology..."
                subnets
                sysinfo
                """.trimIndent()
            ),
            SavedScript(
                name = "Crypto Hasher Pipeline",
                description = "Computes MD5, SHA-256, and Base64 signatures",
                language = "bash",
                content = """
                # Multi-algorithm integrity pipeline
                hash sha256 CypherDeck_Admin_Payload
                hash md5 CypherDeck_Admin_Payload
                encode b64 CypherDeck_Admin_Payload
                """.trimIndent()
            ),
            SavedScript(
                name = "Socket Port Scanner (Python)",
                description = "Multi-threaded socket script template",
                language = "python",
                content = """
                # Python Socket Port Scanner
                import socket
                target = "127.0.0.1"
                ports = [21, 22, 80, 443, 8080]
                for p in ports:
                    s = socket.socket()
                    s.settimeout(0.3)
                    if s.connect_ex((target, p)) == 0:
                        print(f"Port {p} is OPEN")
                    s.close()
                """.trimIndent()
            )
        )

        _uiState.update {
            it.copy(
                savedScripts = scripts,
                currentEditingScript = scripts.first()
            )
        }
    }
}
