package com.qaspro.scanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qaspro.scanner.data.ScanDoc
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    docs: List<ScanDoc>,
    busy: Boolean,
    extractedText: String?,
    onDismissText: () -> Unit,
    onScanDocument: () -> Unit,
    onScanIdCard: () -> Unit,
    onExtractText: () -> Unit,
    onOpen: (ScanDoc) -> Unit,
    onShare: (ScanDoc) -> Unit,
    onDelete: (ScanDoc) -> Unit,
) {
    var tab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = Color(0xFF111315),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF17191B)) {
                bottomItem("Home", Icons.Filled.Home, tab == 0) { tab = 0 }
                bottomItem("Files", Icons.Filled.Description, tab == 1) { tab = 1 }
                bottomItem("Tools", Icons.Filled.GridView, tab == 2) { tab = 2 }
                bottomItem("Me", Icons.Filled.MoreHoriz, tab == 3) { tab = 3 }
            }
        },
        floatingActionButton = {
            if (tab == 0) {
                FloatingActionButton(onClick = onScanDocument, containerColor = Color(0xFF12B886), shape = CircleShape) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Scan", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                1 -> FilesTab(docs, onOpen, onShare, onDelete)
                2 -> ToolsTab(onScanDocument, onScanIdCard, onExtractText)
                3 -> MeTab()
                else -> HomeTab(docs, onScanDocument, onScanIdCard, onExtractText, onOpen, onShare)
            }

            if (busy) {
                Box(
                    Modifier.fillMaxSize().background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color(0xFF12B886)) }
            }
        }
    }

    if (extractedText != null) {
        AlertDialog(
            onDismissRequest = onDismissText,
            confirmButton = { TextButton(onClick = onDismissText) { Text("Close") } },
            title = { Text("Extracted Text") },
            text = { Text(extractedText) },
            containerColor = Color(0xFF1B1E20),
        )
    }
}

@Composable
private fun RowScope.bottomItem(
    label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontSize = 11.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF12B886),
            selectedTextColor = Color(0xFF12B886),
            unselectedIconColor = Color(0xFF9AA0A6),
            unselectedTextColor = Color(0xFF9AA0A6),
            indicatorColor = Color(0x2212B886),
        )
    )
}

private data class Tool(val label: String, val icon: ImageVector, val tint: Color, val onClick: () -> Unit)

@Composable
private fun HomeTab(
    docs: List<ScanDoc>,
    onScanDocument: () -> Unit,
    onScanIdCard: () -> Unit,
    onExtractText: () -> Unit,
    onOpen: (ScanDoc) -> Unit,
    onShare: (ScanDoc) -> Unit,
) {
    val green = Color(0xFF12B886)
    val blue = Color(0xFF4C8DFF)
    val tools = listOf(
        Tool("Smart Scan", Icons.Filled.DocumentScanner, green, onScanDocument),
        Tool("PDF Tools", Icons.Filled.PictureAsPdf, blue, onScanDocument),
        Tool("Import Images", Icons.Filled.Image, blue, onScanDocument),
        Tool("Import Files", Icons.Filled.FolderOpen, blue, onScanDocument),
        Tool("ID Cards", Icons.Filled.Badge, green, onScanIdCard),
        Tool("Extract Text", Icons.Filled.TextFields, green, onExtractText),
        Tool("All", Icons.Filled.GridView, blue, onScanDocument),
    )

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 96.dp),
    ) {
        item { SearchBar() }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            Column {
                tools.chunked(4).forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { t -> Box(Modifier.weight(1f)) { ToolButton(t) } }
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Text("Recents", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp))
        }
        if (docs.isEmpty()) {
            item {
                Text("No scans yet. Tap the camera button to start.",
                    color = Color(0xFF9AA0A6), fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
            }
        } else {
            items(docs) { doc -> DocRow(doc, onOpen, onShare, onDelete = {}) }
        }
    }
}

@Composable
private fun ToolsTab(onScanDocument: () -> Unit, onScanIdCard: () -> Unit, onExtractText: () -> Unit) {
    val green = Color(0xFF12B886)
    val blue = Color(0xFF4C8DFF)
    val tools = listOf(
        Tool("ID Cards", Icons.Filled.Badge, green, onScanIdCard),
        Tool("Extract Text", Icons.Filled.TextFields, green, onExtractText),
        Tool("Smart Scan", Icons.Filled.DocumentScanner, green, onScanDocument),
        Tool("PDF Tools", Icons.Filled.PictureAsPdf, blue, onScanDocument),
        Tool("Import Images", Icons.Filled.Image, blue, onScanDocument),
        Tool("Import Files", Icons.Filled.FolderOpen, blue, onScanDocument),
    )
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)) {
        item { Text("Tools", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold) }
        item { Spacer(Modifier.height(16.dp)) }
        item { Text("Scan", color = Color(0xFF9AA0A6), fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
        item {
            Column {
                tools.chunked(4).forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { t -> Box(Modifier.weight(1f)) { ToolButton(t) } }
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeTab() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Qaspro Scanner", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("100% free • unlimited • offline. Powered by on-device Google ML Kit.",
            color = Color(0xFF9AA0A6), fontSize = 14.sp)
    }
}

@Composable
private fun FilesTab(docs: List<ScanDoc>, onOpen: (ScanDoc) -> Unit, onShare: (ScanDoc) -> Unit, onDelete: (ScanDoc) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(vertical = 24.dp)) {
        item { Text("Files", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold) }
        item { Spacer(Modifier.height(8.dp)) }
        if (docs.isEmpty()) {
            item { Text("No files yet.", color = Color(0xFF9AA0A6), fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp)) }
        } else {
            items(docs) { doc -> DocRow(doc, onOpen, onShare, onDelete) }
        }
    }
}

@Composable
private fun SearchBar() {
    Row(
        Modifier.fillMaxWidth().height(48.dp)
            .background(Color(0xFF1B1E20), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF9AA0A6), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text("Search", color = Color(0xFF9AA0A6), fontSize = 15.sp)
    }
}

@Composable
private fun ToolButton(tool: Tool) {
    Column(
        Modifier.fillMaxWidth().clickable { tool.onClick() }.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(56.dp).background(tool.tint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(tool.icon, contentDescription = tool.label, tint = tool.tint, modifier = Modifier.size(26.dp)) }
        Spacer(Modifier.height(8.dp))
        Text(tool.label, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun DocRow(doc: ScanDoc, onOpen: (ScanDoc) -> Unit, onShare: (ScanDoc) -> Unit, onDelete: (ScanDoc) -> Unit) {
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    Row(
        Modifier.fillMaxWidth().clickable { onOpen(doc) }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp).background(Color(0x224C8DFF), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color(0xFF4C8DFF)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(doc.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text("${fmt.format(doc.date)}  •  ${doc.sizeKb} KB", color = Color(0xFF9AA0A6), fontSize = 12.sp)
        }
        TextButton(onClick = { onShare(doc) }) { Text("Share", color = Color(0xFF12B886)) }
    }
}
