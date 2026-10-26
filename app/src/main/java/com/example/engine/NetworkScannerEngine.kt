package com.example.engine

import com.example.model.OpenPort
import com.example.model.PortScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Collections

object NetworkScannerEngine {

    val COMMON_PORTS = listOf(
        OpenPort(21, "FTP", isCommonVulnerable = true, "File Transfer Protocol (Cleartext)"),
        OpenPort(22, "SSH", isCommonVulnerable = false, "Secure Shell Remote Access"),
        OpenPort(23, "Telnet", isCommonVulnerable = true, "Unencrypted Remote Terminal"),
        OpenPort(25, "SMTP", isCommonVulnerable = false, "Simple Mail Transfer Protocol"),
        OpenPort(53, "DNS", isCommonVulnerable = false, "Domain Name System"),
        OpenPort(80, "HTTP", isCommonVulnerable = true, "HyperText Transfer Protocol (Cleartext)"),
        OpenPort(110, "POP3", isCommonVulnerable = true, "Post Office Protocol"),
        OpenPort(139, "NetBIOS", isCommonVulnerable = true, "NetBIOS Session Service"),
        OpenPort(143, "IMAP", isCommonVulnerable = false, "Internet Message Access Protocol"),
        OpenPort(443, "HTTPS", isCommonVulnerable = false, "HTTP over TLS/SSL"),
        OpenPort(445, "SMB", isCommonVulnerable = true, "Microsoft Server Message Block"),
        OpenPort(993, "IMAPS", isCommonVulnerable = false, "IMAP over TLS/SSL"),
        OpenPort(1433, "MSSQL", isCommonVulnerable = true, "Microsoft SQL Server"),
        OpenPort(3306, "MySQL", isCommonVulnerable = true, "MySQL Database Server"),
        OpenPort(3389, "RDP", isCommonVulnerable = true, "Remote Desktop Protocol"),
        OpenPort(5432, "PostgreSQL", isCommonVulnerable = false, "PostgreSQL Database Server"),
        OpenPort(6379, "Redis", isCommonVulnerable = true, "Redis Key-Value Cache (Often unauth)"),
        OpenPort(8080, "HTTP-Alt", isCommonVulnerable = false, "Common Alternate HTTP / Proxy / Tomcat"),
        OpenPort(8443, "HTTPS-Alt", isCommonVulnerable = false, "Alternate HTTPS Web Interface"),
        OpenPort(27017, "MongoDB", isCommonVulnerable = true, "MongoDB NoSQL Database")
    )

    suspend fun scanPorts(
        target: String,
        portsToScan: List<OpenPort> = COMMON_PORTS,
        timeoutMs: Int = 400,
        onProgress: ((current: Int, total: Int, foundPort: OpenPort?) -> Unit)? = null
    ): PortScanResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val openPorts = mutableListOf<OpenPort>()
        val total = portsToScan.size

        coroutineScope {
            val deferreds = portsToScan.mapIndexed { index, portInfo ->
                async {
                    val isOpen = isPortOpen(target, portInfo.port, timeoutMs)
                    val found = if (isOpen) portInfo else null
                    onProgress?.invoke(index + 1, total, found)
                    if (isOpen) {
                        synchronized(openPorts) {
                            openPorts.add(portInfo)
                        }
                    }
                }
            }
            deferreds.awaitAll()
        }

        PortScanResult(
            target = target,
            scannedPortsCount = total,
            openPorts = openPorts.sortedBy { it.port },
            scanDurationMs = System.currentTimeMillis() - startTime,
            isComplete = true
        )
    }

    private fun isPortOpen(host: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                val socketAddress = InetSocketAddress(host, port)
                socket.connect(socketAddress, timeoutMs)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun ping(host: String, count: Int = 4, timeoutMs: Int = 2000): List<PingResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<PingResult>()
        for (seq in 1..count) {
            val start = System.currentTimeMillis()
            var reachable = false
            var rtt = -1L
            var resolvedIp = ""
            try {
                val address = InetAddress.getByName(host)
                resolvedIp = address.hostAddress ?: ""
                // Socket check or isReachable
                val socket = Socket()
                val socketAddress = InetSocketAddress(address, 80)
                try {
                    socket.connect(socketAddress, timeoutMs)
                    reachable = true
                    rtt = System.currentTimeMillis() - start
                } catch (_: Exception) {
                    // Try port 443 or fallback to isReachable
                    if (address.isReachable(timeoutMs)) {
                        reachable = true
                        rtt = System.currentTimeMillis() - start
                    }
                } finally {
                    try { socket.close() } catch (_: Exception) {}
                }
                if (!reachable && rtt == -1L) {
                    // If host resolved, measure resolve time + connect attempt
                    rtt = System.currentTimeMillis() - start
                    reachable = rtt < timeoutMs
                }
            } catch (e: Exception) {
                reachable = false
            }

            results.add(
                PingResult(
                    sequence = seq,
                    host = host,
                    ip = resolvedIp,
                    timeMs = if (reachable) rtt.coerceAtLeast(1) else -1,
                    success = reachable
                )
            )
        }
        results
    }

    suspend fun resolveDns(domain: String): DnsResolveResult = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            val addresses = InetAddress.getAllByName(domain)
            val duration = System.currentTimeMillis() - start
            val ipList = addresses.map { it.hostAddress ?: "" }
            val canonical = addresses.firstOrNull()?.canonicalHostName ?: domain
            DnsResolveResult(
                domain = domain,
                canonicalName = canonical,
                records = ipList,
                queryTimeMs = duration,
                isSuccess = true
            )
        } catch (e: Exception) {
            DnsResolveResult(
                domain = domain,
                canonicalName = "",
                records = emptyList(),
                queryTimeMs = 0,
                isSuccess = false,
                errorMessage = e.message ?: "Failed to resolve DNS"
            )
        }
    }

    fun getLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {}
        return "127.0.0.1"
    }
}

data class PingResult(
    val sequence: Int,
    val host: String,
    val ip: String,
    val timeMs: Long,
    val success: Boolean
)

data class DnsResolveResult(
    val domain: String,
    val canonicalName: String,
    val records: List<String>,
    val queryTimeMs: Long,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)
