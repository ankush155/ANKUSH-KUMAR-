package com.example.ui.tools

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HashIdentification
import com.example.model.HashResult
import com.example.model.HeaderStatus
import com.example.model.HttpAnalysisResult
import com.example.model.PortScanResult
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertYellow

@Composable
fun CyberArsenalScreen(
    portScanTarget: String,
    isScanningPorts: Boolean,
    portScanProgress: String,
    portScanResult: PortScanResult?,
    onPortScanTargetChange: (String) -> Unit,
    onRunPortScan: () -> Unit,
    httpTargetUrl: String,
    isAnalyzingHttp: Boolean,
    httpAnalysisResult: HttpAnalysisResult?,
    onHttpTargetUrlChange: (String) -> Unit,
    onRunHttpRecon: () -> Unit,
    cryptoInputText: String,
    hashResult: HashResult?,
    identifiedHash: HashIdentification?,
    jwtInput: String,
    jwtDecodedOutput: String,
    onCryptoInputChange: (String) -> Unit,
    onIdentifyHash: (String) -> Unit,
    onJwtInputChange: (String) -> Unit,
    onExecuteInTerminal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Port Scanner", "HTTP Recon", "Crypto & JWT")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = subTab == index,
                    onClick = { subTab = index },
                    text = {
                        Text(
                            text = title,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (subTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        when (subTab) {
            0 -> PortScannerTab(
                target = portScanTarget,
                isScanning = isScanningPorts,
                progress = portScanProgress,
                result = portScanResult,
                onTargetChange = onPortScanTargetChange,
                onStartScan = onRunPortScan,
                onExecuteInTerminal = onExecuteInTerminal
            )
            1 -> HttpReconTab(
                url = httpTargetUrl,
                isAnalyzing = isAnalyzingHttp,
                result = httpAnalysisResult,
                onUrlChange = onHttpTargetUrlChange,
                onStartAnalysis = onRunHttpRecon
            )
            2 -> CryptoToolTab(
                inputText = cryptoInputText,
                hashResult = hashResult,
                identifiedHash = identifiedHash,
                jwtInput = jwtInput,
                jwtDecodedOutput = jwtDecodedOutput,
                onInputChange = onCryptoInputChange,
                onIdentify = onIdentifyHash,
                onJwtChange = onJwtInputChange
            )
        }
    }
}

@Composable
fun PortScannerTab(
    target: String,
    isScanning: Boolean,
    progress: String,
    result: PortScanResult?,
    onTargetChange: (String) -> Unit,
    onStartScan: () -> Unit,
    onExecuteInTerminal: (String) -> Unit
) {
    val quickTargets = listOf("google.com", "scanme.nmap.org", "cloudflare.com", "127.0.0.1")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STEALTH TCP PORT SCANNER",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = target,
                        onValueChange = onTargetChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("port_scan_target_input"),
                        label = { Text("Target Host / IP Address", fontFamily = FontFamily.Monospace) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        quickTargets.forEach { q ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                    .clickable { onTargetChange(q) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = q,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onStartScan,
                        enabled = !isScanning && target.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_port_scan_button"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PROBING TARGET...", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RUN PORT SCAN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (progress.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = progress,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        if (result != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OPEN PORTS FOUND (${result.openPorts.size}):",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Time: ${result.scanDurationMs}ms",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            if (result.openPorts.isEmpty()) {
                item {
                    Text(
                        text = "No open ports found on top 20 list. Host is filtered or offline.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = AlertYellow
                    )
                }
            } else {
                items(result.openPorts) { port ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertGreen.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${port.port}/TCP",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AlertGreen
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = port.serviceName,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = port.description,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }

                            if (port.isCommonVulnerable) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AlertRed.copy(alpha = 0.2f))
                                        .border(1.dp, AlertRed, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AUDIT",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = AlertRed,
                                        fontWeight = FontWeight.Bold
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

@Composable
fun HttpReconTab(
    url: String,
    isAnalyzing: Boolean,
    result: HttpAnalysisResult?,
    onUrlChange: (String) -> Unit,
    onStartAnalysis: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Http,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HTTP HEADERS & RECON AUDITOR",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = url,
                        onValueChange = onUrlChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("http_recon_url_input"),
                        label = { Text("Target URL (e.g., https://example.com)", fontFamily = FontFamily.Monospace) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onStartAnalysis,
                        enabled = !isAnalyzing && url.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_http_recon_button"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CONNECTING & AUDITING...", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AUDIT SECURITY HEADERS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (result != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OVERALL GRADE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = result.grade,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = when (result.grade) {
                                    "A+", "A" -> AlertGreen
                                    "B", "C" -> AlertYellow
                                    else -> AlertRed
                                }
                            )
                        }
                        Text(
                            text = "Status: ${result.statusCode} | Latency: ${result.responseTimeMs}ms | HTTPS: ${result.isHttps}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            items(result.securityHeaders) { header ->
                val badgeColor = when (header.status) {
                    HeaderStatus.SECURE -> AlertGreen
                    HeaderStatus.WARNING -> AlertYellow
                    HeaderStatus.MISSING_CRITICAL -> AlertRed
                    HeaderStatus.INFORMATIONAL -> Color.Gray
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = header.name,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = header.status.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                        if (header.value != null) {
                            Text(
                                text = "Value: ${header.value}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                        Text(
                            text = header.recommendation,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoToolTab(
    inputText: String,
    hashResult: HashResult?,
    identifiedHash: HashIdentification?,
    jwtInput: String,
    jwtDecodedOutput: String,
    onInputChange: (String) -> Unit,
    onIdentify: (String) -> Unit,
    onJwtChange: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var hashToIdentify by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE HASH ENGINE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("crypto_text_input"),
                        label = { Text("Input String to Hash", fontFamily = FontFamily.Monospace) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }

        if (hashResult != null) {
            val hashes = listOf(
                "SHA-256" to hashResult.sha256,
                "MD5" to hashResult.md5,
                "SHA-1" to hashResult.sha1,
                "SHA-512" to hashResult.sha512
            )

            items(hashes) { (algo, hashVal) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = algo,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = hashVal,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(hashVal))
                                Toast.makeText(context, "Copied $algo hash", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Hash Identifier Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "HASH IDENTIFIER",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = hashToIdentify,
                        onValueChange = { hashToIdentify = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Paste mystery hash here...", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { onIdentify(hashToIdentify) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("IDENTIFY HASH ALGORITHM", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                    if (identifiedHash != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Probable Formats: " + identifiedHash.probableAlgorithms.joinToString(", "),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = AlertGreen
                        )
                    }
                }
            }
        }

        // JWT Decoder Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "JWT TOKEN INSPECTOR & DECODER",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = jwtInput,
                        onValueChange = onJwtChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Paste eyJhbGciOi... JWT token", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                        maxLines = 3
                    )
                    if (jwtDecodedOutput.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = jwtDecodedOutput,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}
