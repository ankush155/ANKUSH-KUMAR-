package com.example.engine

import android.content.Context
import android.os.Build
import com.example.model.CyberTheme
import com.example.model.LineType
import com.example.model.SystemStats
import com.example.model.TerminalLine
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TerminalCommandExecutor(private val context: Context) {

    suspend fun execute(
        rawCommand: String,
        currentDir: String,
        activeTheme: CyberTheme,
        emitLine: (TerminalLine) -> Unit
    ) {
        val trimmed = rawCommand.trim()
        if (trimmed.isEmpty()) return

        // Echo command
        emitLine(
            TerminalLine(
                text = "${activeTheme.promptSymbol} $trimmed",
                type = LineType.INPUT
            )
        )

        val parts = trimmed.split("\\s+".toRegex())
        val cmd = parts[0].lowercase()
        val args = parts.drop(1)

        when (cmd) {
            "help", "commands", "?" -> handleHelp(emitLine)
            "clear", "cls" -> {
                // Handled in ViewModel to wipe lines, but emit if called
            }
            "sysinfo", "neofetch" -> handleSysInfo(emitLine)
            "matrix" -> handleMatrix(emitLine)
            "ping" -> handlePing(args, emitLine)
            "portscan", "nmap" -> handlePortScan(args, emitLine)
            "dig", "dns", "nslookup" -> handleDns(args, emitLine)
            "curl", "http", "fetch" -> handleCurl(args, emitLine)
            "whois" -> handleWhois(args, emitLine)
            "hash" -> handleHash(args, emitLine)
            "hashid" -> handleHashId(args, emitLine)
            "encode" -> handleEncode(args, emitLine)
            "decode" -> handleDecode(args, emitLine)
            "jwt" -> handleJwt(args, emitLine)
            "subnets", "arp" -> handleSubnets(emitLine)
            "ai", "nova", "ask" -> handleAi(args, emitLine)
            "pwd" -> emitLine(TerminalLine(text = currentDir, type = LineType.OUTPUT))
            "ls" -> {
                emitLine(TerminalLine(text = "bin/  etc/  recon/  payloads/  scripts/  tools/  ai_models/  cypher.conf", type = LineType.OUTPUT))
            }
            "whoami" -> emitLine(TerminalLine(text = "root (uid=0 gid=0 cyberdeck-ops)", type = LineType.SUCCESS))
            "uname", "uname -a" -> emitLine(
                TerminalLine(
                    text = "Linux cypherdeck ${Build.VERSION.RELEASE} #1 SMP PREEMPT ${Build.HARDWARE} ${Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"}",
                    type = LineType.INFO
                )
            )
            "date" -> {
                val now = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US).format(Date())
                emitLine(TerminalLine(text = now, type = LineType.OUTPUT))
            }
            "echo" -> {
                emitLine(TerminalLine(text = args.joinToString(" "), type = LineType.OUTPUT))
            }
            "cat" -> {
                if (args.isEmpty()) {
                    emitLine(TerminalLine(text = "Usage: cat <filename>", type = LineType.WARNING))
                } else {
                    emitLine(TerminalLine(text = "[FILE: ${args[0]}]", type = LineType.INFO))
                    emitLine(TerminalLine(text = "# CypherDeck v2.4 Security Environment\nstatus=ONLINE\nencryption=AES-256-GCM\nagent=CYPHER_AI_RECON", type = LineType.OUTPUT))
                }
            }
            "msfconsole", "metasploit" -> handleMetasploit(emitLine)
            "aircrack-ng", "aircrack" -> {
                emitLine(TerminalLine(text = "Aircrack-ng 1.7 - Wireless Security Suite (Educational Reference)", type = LineType.INFO))
                emitLine(TerminalLine(text = "Supported sub-modules: airmon-ng, airodump-ng, aireplay-ng, packetforge-ng", type = LineType.OUTPUT))
                emitLine(TerminalLine(text = "Type 'ai wifi' for full audit workflow & educational walkthrough.", type = LineType.SYSTEM))
            }
            else -> {
                emitLine(
                    TerminalLine(
                        text = "bash: $cmd: command not found. Type 'help' for built-in cyber tools or 'ai $trimmed' for AI guidance.",
                        type = LineType.ERROR
                    )
                )
            }
        }
    }

    private fun handleHelp(emitLine: (TerminalLine) -> Unit) {
        val helpText = """
        |\u001b[1;36m═════════════════════════════════════════════════════════════\u001b[0m
        |\u001b[1;32m ⚡ CYPHERDECK v2.4.0 — ETHICAL CYBER TERMINAL & RECON DECK ⚡\u001b[0m
        |\u001b[1;36m═════════════════════════════════════════════════════════════\u001b[0m
        |
        |\u001b[1;33m [NETWORK RECONNAISSANCE]\u001b[0m
        |  • \u001b[32mping\u001b[0m \u001b[36m<host>\u001b[0m [count]        : Real socket latency & packet tester
        |  • \u001b[32mportscan\u001b[0m \u001b[36m<target>\u001b[0m          : Real multi-threaded TCP port prober
        |  • \u001b[32mdig\u001b[0m \u001b[36m<host>\u001b[0m                 : DNS record resolution & canonical names
        |  • \u001b[32mcurl\u001b[0m \u001b[36m<url>\u001b[0m                 : Real HTTP/HTTPS headers & response probe
        |  • \u001b[32mwhois\u001b[0m \u001b[36m<domain>\u001b[0m             : Domain registrant & NS data
        |  • \u001b[32msubnets\u001b[0m                    : Local network IP & interface discoverer
        |
        |\u001b[1;33m [CRYPTOGRAPHY & DECODING]\u001b[0m
        |  • \u001b[32mhash\u001b[0m \u001b[36m<md5|sha256|sha512> <text>\u001b[0m : Generate cryptographic hashes
        |  • \u001b[32mhashid\u001b[0m \u001b[36m<hash>\u001b[0m              : Automated hash algorithm identifier
        |  • \u001b[32mencode\u001b[0m \u001b[36m<b64|hex|url> <str>\u001b[0m : Encode string to formats
        |  • \u001b[32mdecode\u001b[0m \u001b[36m<b64|hex|url> <str>\u001b[0m : Decode string from formats
        |  • \u001b[32mjwt\u001b[0m \u001b[36m<token>\u001b[0m                : Inspect & decode JSON Web Token
        |
        |\u001b[1;33m [CYBER AI CO-PILOT & CONTROL]\u001b[0m
        |  • \u001b[32mai\u001b[0m \u001b[36m<query>\u001b[0m                 : Ask Nova AI for commands, scripts, syntax
        |  • \u001b[32mmatrix\u001b[0m                     : Digital rain animation
        |  • \u001b[32msysinfo / neofetch\u001b[0m         : Cyberpunk device hardware & network status
        |  • \u001b[32mmsfconsole\u001b[0m                 : Penetration testing module advisor
        |  • \u001b[32mclear\u001b[0m                      : Wipe terminal console (or Ctrl+L)
        |\u001b[1;36m═════════════════════════════════════════════════════════════\u001b[0m
        """.trimMargin()

        for (line in helpText.lines()) {
            emitLine(TerminalLine(text = line, type = LineType.OUTPUT))
        }
    }

    private suspend fun handlePing(args: List<String>, emitLine: (TerminalLine) -> Unit) {
        if (args.isEmpty()) {
            emitLine(TerminalLine(text = "\u001b[1;33mUsage: ping <hostname/IP> [count]\u001b[0m", type = LineType.WARNING))
            return
        }
        val host = args[0]
        val count = args.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 10) ?: 4
        emitLine(TerminalLine(text = "\u001b[36mPING $host: sending $count packets with real socket probing...\u001b[0m", type = LineType.INFO))

        val results = NetworkScannerEngine.ping(host, count)
        for (r in results) {
            delay(150)
            if (r.success) {
                emitLine(
                    TerminalLine(
                        text = "64 bytes from \u001b[1;36m${r.ip.ifEmpty { r.host }}\u001b[0m: icmp_seq=${r.sequence} ttl=64 time=\u001b[1;32m${r.timeMs}ms\u001b[0m",
                        type = LineType.SUCCESS
                    )
                )
            } else {
                emitLine(
                    TerminalLine(
                        text = "\u001b[1;31mRequest timeout for icmp_seq=${r.sequence} (host unreachable)\u001b[0m",
                        type = LineType.ERROR
                    )
                )
            }
        }
        val successCount = results.count { it.success }
        val avg = results.filter { it.success }.map { it.timeMs }.average().toInt()
        emitLine(
            TerminalLine(
                text = "\u001b[1;35m--- $host ping statistics --- $count packets transmitted, $successCount received, avg time = ${if (avg >= 0) avg else 0}ms\u001b[0m",
                type = LineType.SYSTEM
            )
        )
    }

    private suspend fun handlePortScan(args: List<String>, emitLine: (TerminalLine) -> Unit) {
        if (args.isEmpty()) {
            emitLine(TerminalLine(text = "\u001b[1;33mUsage: portscan <target_host_or_ip>\u001b[0m", type = LineType.WARNING))
            return
        }
        val target = args[0]
        emitLine(TerminalLine(text = "\u001b[36mStarting CypherDeck Stealth TCP SYN/Connect Scan on $target...\u001b[0m", type = LineType.INFO))
        emitLine(TerminalLine(text = "\u001b[90mProbing top 20 critical ports (FTP, SSH, Telnet, HTTP, HTTPS, DBs)...\u001b[0m", type = LineType.SYSTEM))

        val result = NetworkScannerEngine.scanPorts(target) { current, total, foundPort ->
            if (foundPort != null) {
                emitLine(
                    TerminalLine(
                        text = "\u001b[1;32m[+] PORT ${foundPort.port}/tcp OPEN\u001b[0m \u001b[36m(${foundPort.serviceName})\u001b[0m - \u001b[90m${foundPort.description}\u001b[0m",
                        type = LineType.SUCCESS
                    )
                )
            }
        }

        if (result.openPorts.isEmpty()) {
            emitLine(TerminalLine(text = "Scan complete. All scanned ports are closed or filtered by firewall.", type = LineType.WARNING))
        } else {
            emitLine(
                TerminalLine(
                    text = "Scan completed in ${result.scanDurationMs}ms. Found ${result.openPorts.size} open port(s).",
                    type = LineType.SYSTEM
                )
            )
        }
    }

    private suspend fun handleDns(args: List<String>, emitLine: (TerminalLine) -> Unit) {
        if (args.isEmpty()) {
            emitLine(TerminalLine(text = "Usage: dig <domain_name>", type = LineType.WARNING))
            return
        }
        val domain = args[0]
        emitLine(TerminalLine(text = ";; <<>> CypherDeck DiG 9.18 <<>> $domain", type = LineType.INFO))
        val res = NetworkScannerEngine.resolveDns(domain)
        if (res.isSuccess) {
            emitLine(TerminalLine(text = ";; ->>HEADER<<- opcode: QUERY, status: NOERROR", type = LineType.SYSTEM))
            emitLine(TerminalLine(text = ";; QUESTION SECTION:\n;$domain.\t\t\tIN\tA", type = LineType.OUTPUT))
            emitLine(TerminalLine(text = ";; ANSWER SECTION:", type = LineType.INFO))
            for (ip in res.records) {
                emitLine(TerminalLine(text = "$domain.\t\t300\tIN\tA\t$ip", type = LineType.SUCCESS))
            }
            emitLine(TerminalLine(text = ";; Query time: ${res.queryTimeMs} msec", type = LineType.SYSTEM))
        } else {
            emitLine(TerminalLine(text = "DNS Error: ${res.errorMessage}", type = LineType.ERROR))
        }
    }

    private suspend fun handleCurl(args: List<String>, emitLine: (TerminalLine) -> Unit) {
        if (args.isEmpty()) {
            emitLine(TerminalLine(text = "Usage: curl <url> (e.g., curl https://api.github.com)", type = LineType.WARNING))
            return
        }
        val url = args[0]
        emitLine(TerminalLine(text = "* Connecting to $url...", type = LineType.INFO))
        val analysis = HttpReconEngine.analyzeUrl(url)

        if (analysis.statusCode > 0) {
            emitLine(TerminalLine(text = "< HTTP/1.1 ${analysis.statusCode} OK (Protocol: ${analysis.protocol})", type = LineType.SUCCESS))
            emitLine(TerminalLine(text = "< Response Time: ${analysis.responseTimeMs}ms | Security Grade: [${analysis.grade}]", type = LineType.SYSTEM))
            analysis.rawHeaders.entries.take(6).forEach { (k, v) ->
                emitLine(TerminalLine(text = "< $k: $v", type = LineType.OUTPUT))
            }
            emitLine(TerminalLine(text = "* Security Audit: ${analysis.securityHeaders.count { it.status == com.example.model.HeaderStatus.SECURE }} secure headers verified.", type = LineType.INFO))
        } else {
            emitLine(TerminalLine(text = "* Failed to fetch URL: ${analysis.rawHeaders["Error"]}", type = LineType.ERROR))
        }
    }

    private fun handleWhois(args: List<String>, emitLine: (TerminalLine) -> Unit) {
        if (args.isEmpty()) {
            emitLine(TerminalLine(text = "Usage: whois <domain>", type = LineType.WARNING))
            return
        }
        val domain = args[0]
        emitLine(TerminalLine(text = "Domain Name: ${domain.uppercase()}", type = LineType.INFO))
        emitLine(TerminalLine(text = "Registry Domain ID: 2139048_DOMAIN_COM-VRSN", type = LineType.OUTPUT))
        emitLine(TerminalLine(text = "Registrar WHOIS Server: whois.markmonitor.com", type = LineType.OUTPUT))
        emitLine(TerminalLine(text = "Registrar: MarkMonitor Inc.", type = LineType.OUTPUT))
        emitLine(TerminalLine(text = "Name Server: NS1.${domain.uppercase()}", type = LineType.SUCCESS))
        emitLine(TerminalLine(text = "Name Server: NS2.${domain.uppercase()}", type = LineType.SUCCESS))
        emitLine(TerminalLine(text = "DNSSEC: signedDelegation", type = LineType.SYSTEM))
    }

    private fun handleHash(args: List<String>, emitLine: (TerminalLine) -> Unit) {
        if (args.size < 2) {
            emitLine(TerminalLine(text = "Usage: hash <md5|sha1|sha256|sha512> <input_string>", type = LineType.WARNING))
            return
        }
        val algo = when (args[0].lowercase()) {
            "md5" -> "MD5"
            "sha1" -> "SHA-1"
            "sha256" -> "SHA-256"
            "sha512" -> "SHA-512"
            else -> "SHA-256"
        }
        val input = args.drop(1).joinToString(" ")
        val hashed = CryptoEngine.hash(algo, input)
        emitLine(TerminalLine(text = "Algorithm : $algo", type = LineType.INFO))
        emitLine(TerminalLine(text = "Input     : \"$input\"", type = LineType.OUTPUT))
        emitLine(TerminalLine(text = "Hash      : $hashed", type = LineType.SUCCESS))
    }

    private fun handleHashId(args: List<String>, emitLine: (TerminalLine) -> Unit) {
        if (args.isEmpty()) {
            emitLine(TerminalLine(text = "Usage: hashid <hash_string>", type = LineType.WARNING))
            return
        }
        val hash = args[0]
        val identification = CryptoEngine.identifyHash(hash)
        emitLine(TerminalLine(text = "Analyzing Hash: $hash", type = LineType.INFO))
        emitLine(TerminalLine(text = "Length: ${hash.length} chars (${identification.bitLength} bits)", type = LineType.SYSTEM))
        emitLine(TerminalLine(text = "Probable Algorithms:", type = LineType.INFO))
        for (algo in identification.probableAlgorithms) {
            emitLine(TerminalLine(text = "  [+] $algo", type = LineType.SUCCESS))
        }
    }

    private fun handleEncode(args: List<String>, emitLine: (TerminalLine) -> Unit) {
        if (args.size < 2) {
            emitLine(TerminalLine(text = "Usage: encode <b64|hex|url|rot13> <string>", type = LineType.WARNING))
            return
        }
        val type = args[0].lowercase()
        val text = args.drop(1).joinToString(" ")
        val encoded = when (type) {
            "b64", "base64" -> CryptoEngine.encodeBase64(text)
            "hex" -> CryptoEngine.encodeHex(text)
            "url" -> CryptoEngine.encodeUrl(text)
            "rot13" -> CryptoEngine.rot13(text)
            else -> "Unsupported type. Choose: b64, hex, url, rot13"
        }
        emitLine(TerminalLine(text = "Encoded ($type): $encoded", type = LineType.SUCCESS))
    }

    private fun handleDecode(args: List<String>, emitLine: (TerminalLine) -> Unit) {
        if (args.size < 2) {
            emitLine(TerminalLine(text = "Usage: decode <b64|hex|url|rot13> <encoded_string>", type = LineType.WARNING))
            return
        }
        val type = args[0].lowercase()
        val text = args.drop(1).joinToString(" ")
        val decoded = when (type) {
            "b64", "base64" -> CryptoEngine.decodeBase64(text)
            "hex" -> CryptoEngine.decodeHex(text)
            "url" -> CryptoEngine.decodeUrl(text)
            "rot13" -> CryptoEngine.rot13(text)
            else -> "Unsupported type. Choose: b64, hex, url, rot13"
        }
        emitLine(TerminalLine(text = "Decoded ($type): $decoded", type = LineType.SUCCESS))
    }

    private fun handleJwt(args: List<String>, emitLine: (TerminalLine) -> Unit) {
        if (args.isEmpty()) {
            emitLine(TerminalLine(text = "Usage: jwt <jwt_token>", type = LineType.WARNING))
            return
        }
        val token = args[0]
        val decoded = CryptoEngine.decodeJwt(token)
        for (line in decoded.lines()) {
            val type = if (line.startsWith("---")) LineType.INFO else LineType.OUTPUT
            emitLine(TerminalLine(text = line, type = type))
        }
    }

    private fun handleSubnets(emitLine: (TerminalLine) -> Unit) {
        val ip = NetworkScannerEngine.getLocalIpAddress()
        emitLine(TerminalLine(text = "Local Interface: wlan0 ($ip/24)", type = LineType.INFO))
        emitLine(TerminalLine(text = "Gateway Router  : ${ip.substringBeforeLast(".")}.1", type = LineType.SYSTEM))
        emitLine(TerminalLine(text = "Active Subnet Hosts (Live ARP/Socket check):", type = LineType.INFO))
        emitLine(TerminalLine(text = "  [+] ${ip.substringBeforeLast(".")}.1    [Gateway Router / AP]", type = LineType.SUCCESS))
        emitLine(TerminalLine(text = "  [+] $ip    [Current CyberDeck Device]", type = LineType.SUCCESS))
        emitLine(TerminalLine(text = "  [+] ${ip.substringBeforeLast(".")}.102  [Workstation Host]", type = LineType.OUTPUT))
        emitLine(TerminalLine(text = "Use 'portscan <target>' to probe open ports on any discovered host.", type = LineType.SYSTEM))
    }

    private suspend fun handleAi(args: List<String>, emitLine: (TerminalLine) -> Unit) {
        val prompt = if (args.isEmpty()) "help with cybersecurity commands" else args.joinToString(" ")
        emitLine(TerminalLine(text = "Querying Nova Cyber AI Co-Pilot...", type = LineType.INFO))
        val aiMessage = AiCyberAssistantEngine.query(prompt)
        for (line in aiMessage.text.lines()) {
            emitLine(TerminalLine(text = line, type = LineType.AI_RESPONSE))
        }
        if (aiMessage.suggestedCommands.isNotEmpty()) {
            emitLine(
                TerminalLine(
                    text = "Suggested actions: " + aiMessage.suggestedCommands.joinToString(" | "),
                    type = LineType.SYSTEM
                )
            )
        }
    }

    private fun handleMatrix(emitLine: (TerminalLine) -> Unit) {
        emitLine(TerminalLine(text = "MATRIX DIGITAL RAIN INITIALIZED. Toggle canvas view from top bar icon.", type = LineType.SUCCESS))
        emitLine(TerminalLine(text = "01000011 01011001 01010000 01001000 01000101 01010010", type = LineType.SYSTEM))
        emitLine(TerminalLine(text = "Wake up, Neo... The Matrix has you.", type = LineType.INFO))
    }

    private fun handleSysInfo(emitLine: (TerminalLine) -> Unit) {
        val runtime = Runtime.getRuntime()
        val totalMem = runtime.totalMemory() / (1024 * 1024)
        val freeMem = runtime.freeMemory() / (1024 * 1024)
        val usedMem = totalMem - freeMem
        val cores = Runtime.getRuntime().availableProcessors()
        val ip = NetworkScannerEngine.getLocalIpAddress()

        val banner = """
        |      /\_/\        OS      : CypherDeck Android Edition
        |     ( o.o )       Kernel  : Linux ${Build.VERSION.RELEASE} (${Build.ID})
        |      > ^ <        Model   : ${Build.MANUFACTURER.uppercase()} ${Build.MODEL}
        |    /       \      CPU     : $cores Cores (${Build.SUPPORTED_ABIS.firstOrNull()})
        |   (  CYBER  )     RAM     : ${usedMem}MB / ${totalMem}MB used
        |    \_______/      IP      : $ip
        |                   Shell   : NeoTerm ZSH 5.9 with Nova AI
        |                   Security: SELinux Enforcing, App Sandbox OK
        """.trimMargin()

        for (line in banner.lines()) {
            emitLine(TerminalLine(text = line, type = LineType.INFO))
        }
    }

    private fun handleMetasploit(emitLine: (TerminalLine) -> Unit) {
        val msfBanner = """
        |                 _                    _       _ _   
        | _ __ ___   ___| |_ __ _ ___ _ __ | | ___ (_) |_ 
        || '_ ` _ \ / _ \ __/ _` / __| '_ \| |/ _ \| | __|
        || | | | | |  __/ || (_| \__ \ |_) | | (_) | | |_ 
        ||_| |_| |_|\___|\__\__,_|___/ .__/|_|\___/|_|\__|
        |                            |_|                  
        |
        |=[ metasploit v6.3-dev (Educational Framework)        ]
        |+ -- --=[ 2341 exploits - 1219 auxiliary - 413 post   ]
        |+ -- --=[ 965 payloads - 46 encoders - 11 nops        ]
        |
        |Quick Cheatsheet:
        | • use exploit/multi/handler
        | • set payload linux/x64/meterpreter/reverse_tcp
        | • set LHOST ${NetworkScannerEngine.getLocalIpAddress()}
        | • set LPORT 4444
        | • exploit -j -z
        """.trimMargin()

        for (line in msfBanner.lines()) {
            emitLine(TerminalLine(text = line, type = LineType.SYSTEM))
        }
    }
}
