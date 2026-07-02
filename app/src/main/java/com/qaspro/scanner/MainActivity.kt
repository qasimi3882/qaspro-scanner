package com.qaspro.scanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.qaspro.scanner.data.DocumentStore
import com.qaspro.scanner.ocr.TextExtractor
import com.qaspro.scanner.scan.Scanner
import com.qaspro.scanner.ui.AppScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** What to do once the scanner returns. */
enum class ScanIntentKind { DOCUMENT, ID_CARD, EXTRACT_TEXT }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val brand = darkColorScheme(
            primary = Color(0xFF12B886),
            background = Color(0xFF111315),
            surface = Color(0xFF1B1E20),
        )
        setContent {
            MaterialTheme(colorScheme = brand) {
                Surface { App() }
            }
        }
    }
}

@Composable
private fun App() {
    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()

    var docs by remember { mutableStateOf(DocumentStore.list(context)) }
    // rememberSaveable: survives rotation and Android killing the app while the camera is open,
    // so an "Extract Text" scan is never misrouted into a plain document save.
    var pendingKind by rememberSaveable { mutableStateOf(ScanIntentKind.DOCUMENT) }
    var busy by remember { mutableStateOf(false) }
    var extractedText by rememberSaveable { mutableStateOf<String?>(null) }

    fun refresh() { docs = DocumentStore.list(context) }

    val scannerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val scan = GmsDocumentScanningResult.fromActivityResultIntent(result.data) ?: return@rememberLauncherForActivityResult

        when (pendingKind) {
            ScanIntentKind.EXTRACT_TEXT -> {
                val page = scan.pages?.firstOrNull()?.imageUri
                if (page != null) {
                    busy = true
                    scope.launch {
                        val text = try {
                            withContext(Dispatchers.Default) { TextExtractor.extract(context, page) }
                        } catch (e: Exception) {
                            "Could not read text: ${e.message}"
                        }
                        busy = false
                        extractedText = text.ifBlank { "No text found in the image." }
                    }
                }
            }
            else -> {
                scan.pdf?.uri?.let { pdfUri ->
                    val prefix = if (pendingKind == ScanIntentKind.ID_CARD) "ID_Card" else "Scan"
                    val saved = DocumentStore.savePdf(context, pdfUri, DocumentStore.newFileName(prefix))
                    if (saved == null) {
                        Toast.makeText(context, "Could not save the scan. Please try again.", Toast.LENGTH_LONG).show()
                    }
                    refresh()
                }
            }
        }
    }

    fun launchScan(kind: ScanIntentKind) {
        pendingKind = kind
        val client = when (kind) {
            ScanIntentKind.ID_CARD -> Scanner.idCardClient()
            ScanIntentKind.EXTRACT_TEXT -> Scanner.singlePageClient()
            ScanIntentKind.DOCUMENT -> Scanner.documentClient()
        }
        client.getStartScanIntent(activity)
            .addOnSuccessListener { sender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(sender).build())
            }
            .addOnFailureListener {
                // Scanner module may still be downloading on a fresh device — tell the user
                // instead of silently doing nothing.
                Toast.makeText(
                    context,
                    "Scanner is not ready yet. Connect to the internet, wait a minute, and try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    AppScaffold(
        docs = docs,
        busy = busy,
        extractedText = extractedText,
        onDismissText = { extractedText = null },
        onScanDocument = { launchScan(ScanIntentKind.DOCUMENT) },
        onScanIdCard = { launchScan(ScanIntentKind.ID_CARD) },
        onExtractText = { launchScan(ScanIntentKind.EXTRACT_TEXT) },
        onOpen = { doc ->
            val uri = DocumentStore.uriFor(context, doc.file)
            val view = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            runCatching { context.startActivity(view) }
        },
        onShare = { doc ->
            val uri = DocumentStore.uriFor(context, doc.file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, "Share PDF"))
        },
        onDelete = { doc -> DocumentStore.delete(doc); refresh() },
    )
}
