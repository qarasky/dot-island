/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.qarasky.dotisland.data.LiveUpdateSourceStore
import dev.qarasky.dotisland.data.isLiveUpdatePackageName
import kotlinx.coroutines.launch

/** Explicit notification-source opt-in; not a generic notification mirror. */
@Composable
internal fun LiveUpdateSourceAccessSection(store: LiveUpdateSourceStore, notificationAccess: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allowed by store.allowedPackages.collectAsStateWithLifecycle(emptySet())
    var open by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("LIVE UPDATES", color = Color(0xFF8E8E93), fontFamily = FontFamily.Monospace,
            fontSize = 10.sp, letterSpacing = 1.sp)
        Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF111111),
            border = BorderStroke(1.dp, Color(0xFF262626)),
            modifier = Modifier.fillMaxWidth().bounceClick { open = true }) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Allowed notification sources", fontSize = 16.sp)
                Text(if (!notificationAccess) "Notification access required"
                    else if (allowed.isEmpty()) "None" else "${allowed.size} allowed · Ongoing only",
                    color = Color(0xFF8E8E93), fontSize = 12.sp)
            }
        }
    }
    if (open) {
        var entry by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { open = false }, containerColor = Color(0xFF111111),
            titleContentColor = Color.White, textContentColor = Color(0xFFB8B8BD),
            title = { Text("Live Updates", fontSize = 19.sp) },
            text = {
                Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Only ongoing Android progress-style or promoted Live Updates from these apps. " +
                        "Dodo order notifications also have a compatibility parser. Ordinary alerts stay in the shade.",
                        fontSize = 12.sp, lineHeight = 17.sp)
                    allowed.sorted().forEach { pkg ->
                        val label = remember(pkg) {
                            runCatching { context.packageManager.getApplicationLabel(
                                context.packageManager.getApplicationInfo(pkg, 0)).toString() }.getOrDefault(pkg)
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(label, fontSize = 14.sp)
                                Text(pkg, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }
                            TextButton(onClick = { scope.launch { store.setAllowed(pkg, false) } }) { Text("Remove") }
                        }
                    }
                    OutlinedTextField(value = entry, onValueChange = { entry = it },
                        label = { Text("App package name") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth())
                    TextButton(enabled = isLiveUpdatePackageName(entry.trim()), onClick = {
                        val pkg = entry.trim()
                        scope.launch { store.setAllowed(pkg, true); entry = "" }
                    }) { Text("Allow source") }
                    Text("This does not grant access to the publishing broadcast API.", fontSize = 11.sp)
                }
            },
            confirmButton = { TextButton(onClick = { open = false }) { Text("Done") } })
    }
}
