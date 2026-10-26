package com.example.ui.terminal

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CyberTheme
import com.example.model.LineType
import com.example.model.TerminalLine
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Custom Jetpack Compose Terminal Emulator Component
 * Features:
 * - Real ANSI color escape rendering (16 color, 256 color, TrueColor RGB)
 * - Command input history buffer navigation (Up/Down + History Search Dialog)
 * - Animated blinking block/bar cursor
 * - Aux keypad (Ctrl, Alt, Tab, Esc, Pipes, Arrow keys)
 * - Auto-scroll with floating jump-to-bottom indicator
 * - Log copy / export
 */
@Composable
fun TerminalEmulatorComponent(
    lines: List<TerminalLine>,
    history: List<String>,
    promptPrefix: String,
    activeTheme: CyberTheme,
    fontSizeSp: Int = 13,
    isExecuting: Boolean = false,
    onExecuteCommand: (String) -> Unit,
    onClearOutput: () -> Unit = {},
    onInterrupt: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    // Text field state with cursor selection tracking
    var inputFieldState by remember { mutableStateOf(TextFieldValue("")) }
    var historyIndex by remember { mutableIntStateOf(-1) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    // Cursor Blink Animation
    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(530)
            cursorVisible = !cursorVisible
        }
    }

    // Auto-scroll when new lines arrive
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    // Detect if user has scrolled away from bottom
    val isScrolledToBottom by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems == 0 || lastVisible >= totalItems - 2
        }
    }

    val autoCompletionCommands = remember {
        listOf("help", "clear", "sysinfo", "ping", "portscan", "dig", "curl", "whois", "hash", "hashid", "encode", "decode", "jwt", "subnets", "matrix", "ai")
    }

    // Function to navigate history
    val handleHistoryNavigate: (Boolean) -> Unit = { navigateUp ->
        if (history.isNotEmpty()) {
            val newIndex = if (navigateUp) {
                if (historyIndex == -1) history.size - 1 else (historyIndex - 1).coerceAtLeast(0)
            } else {
                if (historyIndex == -1 || historyIndex >= history.size - 1) -1 else historyIndex + 1
            }
            historyIndex = newIndex
            val recalled = if (newIndex in history.indices) history[newIndex] else ""
            inputFieldState = TextFieldValue(
                text = recalled,
                selection = TextRange(recalled.length)
            )
        }
    }

    // Tab autocomplete logic
    val handleTabCompletion: () -> Unit = {
        val currentText = inputFieldState.text
        if (currentText.isNotBlank()) {
            val parts = currentText.split(" ")
            val lastToken = parts.last()
            val match = autoCompletionCommands.firstOrNull { it.startsWith(lastToken, ignoreCase = true) }
            if (match != null) {
                val completed = if (parts.size > 1) {
                    parts.dropLast(1).joinToString(" ") + " $match "
                } else {
                    "$match "
                }
                inputFieldState = TextFieldValue(completed, TextRange(completed.length))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Top Toolbar: Active Theme badge & Copy Buffer action
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isExecuting) AlertYellow else AlertGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isExecuting) "STATUS: RUNNING" else "STATUS: IDLE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExecuting) AlertYellow else AlertGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[${lines.size} lines]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // History Search Button
                        IconButton(
                            onClick = { showHistoryDialog = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("open_history_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History Search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Copy Output Button
                        IconButton(
                            onClick = {
                                val fullText = lines.joinToString("\n") { AnsiColorParser.stripAnsi(it.text) }
                                clipboard.setText(AnnotatedString(fullText))
                                Toast.makeText(context, "Terminal output copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("copy_terminal_output")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Log",
                                tint = Color.Gray,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Scrollable Output Viewport
            SelectionContainer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(lines, key = { it.id }) { line ->
                        AnsiTerminalLineRenderer(
                            line = line,
                            fontSizeSp = fontSizeSp,
                            defaultColor = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (isExecuting) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Executing process (Ctrl+C to abort)...",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = (fontSizeSp - 1).sp,
                                    color = AlertYellow
                                )
                            }
                        }
                    }
                }
            }

            // Virtual Auxiliary Keypad Bar
            AuxiliaryTerminalKeypad(
                onAppendToken = { token ->
                    val cur = inputFieldState.text
                    val sel = inputFieldState.selection.start
                    val updated = cur.substring(0, sel) + token + cur.substring(sel)
                    inputFieldState = TextFieldValue(updated, TextRange(sel + token.length))
                },
                onArrowKey = { isLeft ->
                    val curSel = inputFieldState.selection.start
                    val newSel = if (isLeft) (curSel - 1).coerceAtLeast(0) else (curSel + 1).coerceAtMost(inputFieldState.text.length)
                    inputFieldState = inputFieldState.copy(selection = TextRange(newSel))
                },
                onTab = handleTabCompletion,
                onCtrlC = {
                    onInterrupt()
                    inputFieldState = TextFieldValue("")
                    historyIndex = -1
                },
                onCtrlL = onClearOutput,
                onExecute = { cmd ->
                    onExecuteCommand(cmd)
                    inputFieldState = TextFieldValue("")
                    historyIndex = -1
                }
            )

            // Command Prompt & Input Line
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Up/Down History buttons
                    IconButton(
                        onClick = { handleHistoryNavigate(true) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("terminal_history_prev")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Previous Command",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { handleHistoryNavigate(false) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("terminal_history_next")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Next Command",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Prompt text
                    Text(
                        text = promptPrefix,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = fontSizeSp.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Text Input with Custom Cursor
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                    ) {
                        BasicTextField(
                            value = inputFieldState,
                            onValueChange = {
                                inputFieldState = it
                                historyIndex = -1
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .testTag("terminal_command_input_field"),
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = fontSizeSp.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(if (cursorVisible) MaterialTheme.colorScheme.primary else Color.Transparent),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    val cmd = inputFieldState.text.trim()
                                    if (cmd.isNotEmpty()) {
                                        onExecuteCommand(cmd)
                                        inputFieldState = TextFieldValue("")
                                        historyIndex = -1
                                    }
                                }
                            ),
                            decorationBox = { innerTextField ->
                                if (inputFieldState.text.isEmpty()) {
                                    Text(
                                        text = "Type command or 'help'...",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = (fontSizeSp - 1).sp,
                                        color = Color.DarkGray
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    if (inputFieldState.text.isNotEmpty()) {
                        IconButton(
                            onClick = { inputFieldState = TextFieldValue("") },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Input",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Execute Button
                    IconButton(
                        onClick = {
                            val cmd = inputFieldState.text.trim()
                            if (cmd.isNotEmpty()) {
                                onExecuteCommand(cmd)
                                inputFieldState = TextFieldValue("")
                                historyIndex = -1
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("terminal_execute_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Run",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Floating "Scroll to Bottom" badge when user scrolled up
        AnimatedVisibility(
            visible = !isScrolledToBottom,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .clickable {
                        coroutineScope.launch {
                            if (lines.isNotEmpty()) {
                                listState.animateScrollToItem(lines.size - 1)
                            }
                        }
                    }
                    .testTag("scroll_to_bottom_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll to latest",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Scroll to latest",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // History Search Modal
        if (showHistoryDialog) {
            HistorySearchDialog(
                history = history,
                onSelectCommand = { cmd ->
                    inputFieldState = TextFieldValue(cmd, TextRange(cmd.length))
                    showHistoryDialog = false
                },
                onExecuteCommand = { cmd ->
                    onExecuteCommand(cmd)
                    inputFieldState = TextFieldValue("")
                    showHistoryDialog = false
                },
                onDismiss = { showHistoryDialog = false }
            )
        }
    }
}

/**
 * Line renderer supporting ANSI Color codes and Semantic status colors
 */
@Composable
fun AnsiTerminalLineRenderer(
    line: TerminalLine,
    fontSizeSp: Int,
    defaultColor: Color
) {
    val fallbackColor = when (line.type) {
        LineType.INPUT -> MaterialTheme.colorScheme.primary
        LineType.SUCCESS -> AlertGreen
        LineType.ERROR -> AlertRed
        LineType.WARNING -> AlertYellow
        LineType.INFO -> MaterialTheme.colorScheme.secondary
        LineType.SYSTEM -> MaterialTheme.colorScheme.tertiary
        LineType.AI_RESPONSE -> MaterialTheme.colorScheme.primary
        LineType.OUTPUT, LineType.PROMPT_BANNER -> defaultColor
    }

    val annotated = AnsiColorParser.parse(line.text, fallbackColor)

    if (line.type == LineType.AI_RESPONSE) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Text(
                text = annotated,
                fontFamily = FontFamily.Monospace,
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp + 5).sp
            )
        }
    } else {
        Text(
            text = annotated,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp + 4).sp
        )
    }
}

/**
 * Auxiliary Terminal Keypad for developer keyboard controls
 */
@Composable
fun AuxiliaryTerminalKeypad(
    onAppendToken: (String) -> Unit,
    onArrowKey: (Boolean) -> Unit,
    onTab: () -> Unit,
    onCtrlC: () -> Unit,
    onCtrlL: () -> Unit,
    onExecute: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Control keys
            AuxKeyButton(label = "Ctrl+C", color = AlertRed) { onCtrlC() }
            AuxKeyButton(label = "Ctrl+L", color = MaterialTheme.colorScheme.secondary) { onCtrlL() }
            AuxKeyButton(label = "TAB", color = MaterialTheme.colorScheme.primary) { onTab() }
            AuxKeyButton(label = "←") { onArrowKey(true) }
            AuxKeyButton(label = "→") { onArrowKey(false) }

            // Common Bash & pipe operators
            listOf("|", "&", "&&", ">", "<", "$", "~", "/", "-", "_").forEach { token ->
                AuxKeyButton(label = token) { onAppendToken(token) }
            }

            // Fast commands
            listOf("clear", "sysinfo", "help", "matrix").forEach { cmd ->
                AuxKeyButton(label = cmd, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)) {
                    onExecute(cmd)
                }
            }
        }
    }
}

@Composable
fun AuxKeyButton(
    label: String,
    color: Color = Color.LightGray,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Interactive History Search Dialog (similar to Bash Reverse-i-search Ctrl+R)
 */
@Composable
fun HistorySearchDialog(
    history: List<String>,
    onSelectCommand: (String) -> Unit,
    onExecuteCommand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredHistory = remember(searchQuery, history) {
        val uniqueHistory = history.distinct().reversed()
        if (searchQuery.isBlank()) uniqueHistory
        else uniqueHistory.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REVERSE COMMAND SEARCH (CTRL+R)",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    decorationBox = { inner ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            if (searchQuery.isEmpty()) {
                                Text("Filter command history...", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Gray)
                            }
                            inner()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredHistory.isEmpty()) {
                    Text(
                        text = "No matching commands found.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredHistory) { cmd ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cmd,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectCommand(cmd) }
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "USE",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .clickable { onSelectCommand(cmd) }
                                            .padding(4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "RUN",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AlertGreen,
                                        modifier = Modifier
                                            .clickable { onExecuteCommand(cmd) }
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
