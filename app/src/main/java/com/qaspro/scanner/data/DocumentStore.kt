package com.qaspro.scanner.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** A saved scan, stored as a PDF in the app's Documents folder. */
data class ScanDoc(
    val file: File,
    val name: String,
    val date: Date,
    val sizeKb: Long,
)

/**
 * Simple file-based store. No database needed — scans live as PDFs on disk,
 * which keeps the app free, offline, and unlimited.
 */
object DocumentStore {

    private const val FOLDER = "Documents"

    private fun dir(context: Context): File =
        File(context.getExternalFilesDir(null), FOLDER).apply { if (!exists()) mkdirs() }

    fun newFileName(prefix: String = "Scan"): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd_HH.mm.ss", Locale.US).format(Date())
        return "${prefix}_$stamp.pdf"
    }

    /**
     * Copy a freshly scanned PDF (from ML Kit) into permanent storage.
     * Returns null if the copy failed so callers can tell the user.
     */
    fun savePdf(context: Context, source: Uri, fileName: String): File? {
        val target = uniqueFile(dir(context), fileName)
        val result = runCatching {
            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
        if (result.isFailure || result.getOrNull() == null || target.length() == 0L) {
            target.delete()
            return null
        }
        return target
    }

    /** Never overwrite an existing scan — append _2, _3, … on name collision. */
    private fun uniqueFile(dir: File, fileName: String): File {
        var candidate = File(dir, fileName)
        val base = fileName.removeSuffix(".pdf")
        var counter = 2
        while (candidate.exists()) {
            candidate = File(dir, "${base}_$counter.pdf")
            counter++
        }
        return candidate
    }

    fun list(context: Context): List<ScanDoc> =
        dir(context).listFiles { f -> f.extension.equals("pdf", true) }
            ?.sortedByDescending { it.lastModified() }
            ?.map {
                ScanDoc(
                    file = it,
                    name = it.nameWithoutExtension,
                    date = Date(it.lastModified()),
                    sizeKb = it.length() / 1024,
                )
            } ?: emptyList()

    fun delete(doc: ScanDoc) {
        doc.file.delete()
    }

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
