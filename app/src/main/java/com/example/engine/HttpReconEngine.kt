package com.example.engine

import com.example.model.HeaderStatus
import com.example.model.HttpAnalysisResult
import com.example.model.SecurityHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object HttpReconEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun analyzeUrl(targetUrl: String): HttpAnalysisResult = withContext(Dispatchers.IO) {
        var cleanUrl = targetUrl.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }

        val request = Request.Builder()
            .url(cleanUrl)
            .header("User-Agent", "CypherDeck-CyberTerminal/2.0 (Android; Ethical-Recon-Engine)")
            .build()

        val start = System.currentTimeMillis()
        try {
            client.newCall(request).execute().use { response ->
                val elapsed = System.currentTimeMillis() - start
                val rawHeaders = mutableMapOf<String, String>()
                for (i in 0 until response.headers.size) {
                    rawHeaders[response.headers.name(i)] = response.headers.value(i)
                }

                val isHttps = response.request.isHttps
                val secHeaders = auditSecurityHeaders(rawHeaders, isHttps)
                val grade = calculateSecurityGrade(secHeaders, isHttps)

                HttpAnalysisResult(
                    url = cleanUrl,
                    statusCode = response.code,
                    protocol = response.protocol.toString(),
                    responseTimeMs = elapsed,
                    serverHeader = response.header("Server"),
                    isHttps = isHttps,
                    securityHeaders = secHeaders,
                    rawHeaders = rawHeaders,
                    grade = grade
                )
            }
        } catch (e: Exception) {
            HttpAnalysisResult(
                url = cleanUrl,
                statusCode = 0,
                protocol = "N/A",
                responseTimeMs = System.currentTimeMillis() - start,
                serverHeader = null,
                isHttps = cleanUrl.startsWith("https://"),
                securityHeaders = emptyList(),
                rawHeaders = mapOf("Error" to (e.message ?: "Connection failed")),
                grade = "F"
            )
        }
    }

    private fun auditSecurityHeaders(headers: Map<String, String>, isHttps: Boolean): List<SecurityHeader> {
        val list = mutableListOf<SecurityHeader>()

        fun findHeader(name: String): String? {
            return headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
        }

        // 1. HSTS
        val hsts = findHeader("Strict-Transport-Security")
        list.add(
            SecurityHeader(
                name = "Strict-Transport-Security (HSTS)",
                value = hsts,
                isPresent = hsts != null,
                status = when {
                    hsts != null && hsts.contains("max-age") -> HeaderStatus.SECURE
                    !isHttps -> HeaderStatus.INFORMATIONAL
                    else -> HeaderStatus.MISSING_CRITICAL
                },
                recommendation = "Enforces HTTPS connections and prevents SSL-stripping attacks (max-age>=31536000)."
            )
        )

        // 2. CSP
        val csp = findHeader("Content-Security-Policy")
        list.add(
            SecurityHeader(
                name = "Content-Security-Policy (CSP)",
                value = csp,
                isPresent = csp != null,
                status = if (csp != null) HeaderStatus.SECURE else HeaderStatus.MISSING_CRITICAL,
                recommendation = "Mitigates Cross-Site Scripting (XSS) and data injection attacks by restricting content sources."
            )
        )

        // 3. X-Frame-Options
        val xfo = findHeader("X-Frame-Options")
        list.add(
            SecurityHeader(
                name = "X-Frame-Options",
                value = xfo,
                isPresent = xfo != null,
                status = if (xfo != null && (xfo.equals("DENY", true) || xfo.equals("SAMEORIGIN", true))) {
                    HeaderStatus.SECURE
                } else if (csp?.contains("frame-ancestors") == true) {
                    HeaderStatus.SECURE
                } else {
                    HeaderStatus.WARNING
                },
                recommendation = "Protects against Clickjacking by preventing page from being embedded in iframes."
            )
        )

        // 4. X-Content-Type-Options
        val xcto = findHeader("X-Content-Type-Options")
        list.add(
            SecurityHeader(
                name = "X-Content-Type-Options",
                value = xcto,
                isPresent = xcto != null,
                status = if (xcto?.equals("nosniff", true) == true) HeaderStatus.SECURE else HeaderStatus.WARNING,
                recommendation = "Blocks MIME-type sniffing which could allow malicious script execution."
            )
        )

        // 5. Referrer-Policy
        val ref = findHeader("Referrer-Policy")
        list.add(
            SecurityHeader(
                name = "Referrer-Policy",
                value = ref,
                isPresent = ref != null,
                status = if (ref != null) HeaderStatus.SECURE else HeaderStatus.INFORMATIONAL,
                recommendation = "Controls how much referrer information is included with requests."
            )
        )

        // 6. Information Disclosure (Server / X-Powered-By)
        val server = findHeader("Server")
        val poweredBy = findHeader("X-Powered-By")
        if (server != null || poweredBy != null) {
            list.add(
                SecurityHeader(
                    name = "Technology Disclosure (Server / X-Powered-By)",
                    value = "Server: $server, Powered-By: $poweredBy",
                    isPresent = true,
                    status = HeaderStatus.WARNING,
                    recommendation = "Banner reveals software versions to potential attackers. Should be stripped or obscured."
                )
            )
        }

        return list
    }

    private fun calculateSecurityGrade(headers: List<SecurityHeader>, isHttps: Boolean): String {
        if (!isHttps) return "D"
        val criticalMissing = headers.count { it.status == HeaderStatus.MISSING_CRITICAL }
        val secureCount = headers.count { it.status == HeaderStatus.SECURE }
        return when {
            criticalMissing == 0 && secureCount >= 4 -> "A+"
            criticalMissing == 0 -> "A"
            criticalMissing == 1 -> "B"
            criticalMissing == 2 -> "C"
            else -> "F"
        }
    }
}
