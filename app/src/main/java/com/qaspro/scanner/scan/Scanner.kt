package com.qaspro.scanner.scan

import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_BASE
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning

/**
 * Central config for the free ML Kit document scanner. BASE mode keeps the
 * natural capture (crop + rotate only) with no automatic enhancement filters.
 */
object Scanner {

    /** Multi-page document scan (Smart Scan, Book, Whiteboard, etc.). */
    fun documentClient(pageLimit: Int = 30) = GmsDocumentScanning.getClient(
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_BASE)
            .setGalleryImportAllowed(true)
            .setPageLimit(pageLimit)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()
    )

    /**
     * Continuous scan: capture many pages in one session, all joined into a
     * single PDF. Same engine as a document scan, just a high page limit.
     */
    fun continuousClient() = documentClient(pageLimit = 100)

    /** ID cards: front + back, exactly two pages. */
    fun idCardClient() = GmsDocumentScanning.getClient(
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_BASE)
            .setGalleryImportAllowed(true)
            .setPageLimit(2)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()
    )

    /** Single page, used before OCR / Extract Text. */
    fun singlePageClient() = GmsDocumentScanning.getClient(
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_BASE)
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()
    )
}
