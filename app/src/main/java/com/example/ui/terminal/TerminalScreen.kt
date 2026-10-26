package com.example.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.CyberTheme
import com.example.model.TerminalSession
import com.example.ui.components.MatrixRainCanvas

@Composable
fun TerminalScreen(
    session: TerminalSession?,
    commandInput: String,
    isExecuting: Boolean,
    activeTheme: CyberTheme,
    matrixRainActive: Boolean,
    fontSizeSp: Int,
    onInputChange: (String) -> Unit,
    onExecuteCommand: (String?) -> Unit,
    onAppendToken: (String) -> Unit,
    onNavigateHistory: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Optional Matrix Rain Digital Overlay
        if (matrixRainActive) {
            MatrixRainCanvas(
                color = MaterialTheme.colorScheme.primary,
                alpha = 0.20f
            )
        }

        // Custom Terminal Emulator Component
        TerminalEmulatorComponent(
            lines = session?.lines ?: emptyList(),
            history = session?.history ?: emptyList(),
            promptPrefix = "$ ",
            activeTheme = activeTheme,
            fontSizeSp = fontSizeSp,
            isExecuting = isExecuting,
            onExecuteCommand = { cmd -> onExecuteCommand(cmd) },
            onClearOutput = { onExecuteCommand("clear") },
            onInterrupt = { onExecuteCommand("echo ^C") }
        )
    }
}
