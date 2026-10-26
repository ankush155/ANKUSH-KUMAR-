package com.example.model

enum class LineType {
    INPUT,
    OUTPUT,
    SUCCESS,
    WARNING,
    ERROR,
    INFO,
    SYSTEM,
    AI_RESPONSE,
    PROMPT_BANNER
}

data class TerminalLine(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val type: LineType = LineType.OUTPUT,
    val timestamp: Long = System.currentTimeMillis()
)

data class TerminalSession(
    val id: String,
    val title: String,
    val lines: List<TerminalLine> = emptyList(),
    val currentWorkingDirectory: String = "~/cypherdeck",
    val history: List<String> = emptyList()
)

enum class CyberTheme(val title: String, val promptSymbol: String) {
    MATRIX_GREEN("Matrix 0x00", "λ matrix@root:~$"),
    CYBERPUNK_NEON("Cyberpunk Neon", "⚡ nova@deck:~$"),
    BLOOD_RED("Blood Red Ops", "☠ red_team@c2:~$"),
    AMBER_CRT("Amber Retro CRT", "▶ mainframe@amber:~$"),
    DRACULA_VIOLET("Dracula Violet", "✦ shadow@hex:~$")
}

data class SystemStats(
    val osVersion: String,
    val deviceModel: String,
    val cpuCores: Int,
    val totalRamMb: Long,
    val freeRamMb: Long,
    val ipAddress: String,
    val networkType: String,
    val batteryPct: Int
)
