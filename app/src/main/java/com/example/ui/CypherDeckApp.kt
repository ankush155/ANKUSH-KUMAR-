package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ai.AiAssistantScreen
import com.example.ui.components.CyberTopBar
import com.example.ui.scripts.ScriptStudioScreen
import com.example.ui.terminal.TerminalScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.tools.CyberArsenalScreen
import com.example.ui.vault.CheatsheetScreen
import com.example.viewmodel.CypherViewModel

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun CypherDeckApp(
    viewModel: CypherViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val navItems = listOf(
        NavTabItem("Terminal", Icons.Default.Terminal, "nav_terminal"),
        NavTabItem("AI Co-Pilot", Icons.Default.Psychology, "nav_ai"),
        NavTabItem("Arsenal", Icons.Default.Security, "nav_arsenal"),
        NavTabItem("Vault", Icons.Default.LibraryBooks, "nav_vault"),
        NavTabItem("Scripts", Icons.Default.Code, "nav_scripts")
    )

    MyApplicationTheme(activeTheme = state.activeTheme) {
        Scaffold(
            topBar = {
                CyberTopBar(
                    sessions = state.sessions,
                    activeSessionId = state.activeSessionId,
                    activeTheme = state.activeTheme,
                    matrixRainActive = state.matrixRainActive,
                    onSelectSession = { viewModel.switchSession(it) },
                    onAddSession = { viewModel.addTerminalSession() },
                    onCloseSession = { viewModel.closeSession(it) },
                    onSelectTheme = { viewModel.setTheme(it) },
                    onToggleMatrixRain = { viewModel.toggleMatrixRain() }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("cyber_bottom_nav")
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = state.activeTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setActiveTab(index) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (state.activeTab) {
                    0 -> {
                        val activeSession = state.sessions.firstOrNull { it.id == state.activeSessionId }
                        TerminalScreen(
                            session = activeSession,
                            commandInput = state.commandInput,
                            isExecuting = state.isExecutingCommand,
                            activeTheme = state.activeTheme,
                            matrixRainActive = state.matrixRainActive,
                            fontSizeSp = state.terminalFontSizeSp,
                            onInputChange = { viewModel.onCommandInputChange(it) },
                            onExecuteCommand = { viewModel.executeTerminalCommand(it) },
                            onAppendToken = { viewModel.appendToCommand(it) },
                            onNavigateHistory = { viewModel.navigateHistory(it) }
                        )
                    }
                    1 -> {
                        AiAssistantScreen(
                            messages = state.aiMessages,
                            inputText = state.aiInputText,
                            isThinking = state.isAiThinking,
                            onInputChange = { viewModel.onAiInputChange(it) },
                            onSendPrompt = { viewModel.sendAiPrompt(it) },
                            onExecuteInTerminal = { cmd ->
                                viewModel.setActiveTab(0)
                                viewModel.executeTerminalCommand(cmd)
                            }
                        )
                    }
                    2 -> {
                        CyberArsenalScreen(
                            portScanTarget = state.portScanTarget,
                            isScanningPorts = state.isScanningPorts,
                            portScanProgress = state.portScanProgress,
                            portScanResult = state.portScanResult,
                            onPortScanTargetChange = { viewModel.onPortScanTargetChange(it) },
                            onRunPortScan = { viewModel.runPortScan() },
                            httpTargetUrl = state.httpTargetUrl,
                            isAnalyzingHttp = state.isAnalyzingHttp,
                            httpAnalysisResult = state.httpAnalysisResult,
                            onHttpTargetUrlChange = { viewModel.onHttpTargetUrlChange(it) },
                            onRunHttpRecon = { viewModel.runHttpRecon() },
                            cryptoInputText = state.cryptoInputText,
                            hashResult = state.currentHashResult,
                            identifiedHash = state.identifiedHash,
                            jwtInput = state.jwtInput,
                            jwtDecodedOutput = state.jwtDecodedOutput,
                            onCryptoInputChange = { viewModel.onCryptoInputChange(it) },
                            onIdentifyHash = { viewModel.identifyHash(it) },
                            onJwtInputChange = { viewModel.onJwtInputChange(it) },
                            onExecuteInTerminal = { cmd ->
                                viewModel.setActiveTab(0)
                                viewModel.executeTerminalCommand(cmd)
                            }
                        )
                    }
                    3 -> {
                        CheatsheetScreen(
                            searchQuery = state.cheatsheetQuery,
                            selectedCategory = state.selectedCheatCategory,
                            onSearchChange = { viewModel.onCheatsheetQueryChange(it) },
                            onCategoryChange = { viewModel.onCheatCategorySelect(it) },
                            onExecuteInTerminal = { cmd ->
                                viewModel.setActiveTab(0)
                                viewModel.executeTerminalCommand(cmd)
                            }
                        )
                    }
                    4 -> {
                        ScriptStudioScreen(
                            scripts = state.savedScripts,
                            activeScript = state.currentEditingScript,
                            onSelectScript = { viewModel.selectScriptForEdit(it) },
                            onContentChange = { viewModel.updateCurrentScriptContent(it) },
                            onSaveScript = { viewModel.saveCurrentScript() },
                            onRunScript = { script -> viewModel.runScriptInTerminal(script) }
                        )
                    }
                }
            }
        }
    }
}
