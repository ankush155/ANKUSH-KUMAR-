package com.example.model

data class OpenPort(
    val port: Int,
    val serviceName: String,
    val isCommonVulnerable: Boolean = false,
    val description: String = ""
)

data class PortScanResult(
    val target: String,
    val scannedPortsCount: Int,
    val openPorts: List<OpenPort>,
    val scanDurationMs: Long,
    val isComplete: Boolean = true
)

data class SecurityHeader(
    val name: String,
    val value: String?,
    val isPresent: Boolean,
    val status: HeaderStatus,
    val recommendation: String
)

enum class HeaderStatus {
    SECURE,
    WARNING,
    MISSING_CRITICAL,
    INFORMATIONAL
}

data class HttpAnalysisResult(
    val url: String,
    val statusCode: Int,
    val protocol: String,
    val responseTimeMs: Long,
    val serverHeader: String?,
    val isHttps: Boolean,
    val securityHeaders: List<SecurityHeader>,
    val rawHeaders: Map<String, String>,
    val grade: String
)

data class HashResult(
    val input: String,
    val md5: String,
    val sha1: String,
    val sha256: String,
    val sha512: String
)

data class HashIdentification(
    val hash: String,
    val probableAlgorithms: List<String>,
    val bitLength: Int
)

data class CheatSheetItem(
    val id: String,
    val title: String,
    val category: String,
    val command: String,
    val description: String,
    val tags: List<String> = emptyList()
)

data class SavedScript(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val language: String = "bash", // bash, python
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)
