package com.qaspro.scanner.scan

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.File

/**
 * Composes the front and back of an ID onto a single PDF page.
 * ML Kit returns one image per capture; we stack up to two of them
 * vertically on one A4 page so both sides land on the same sheet.
 */
object IdCardComposer {

    private const val PAGE_W = 595   // A4 @ 72 dpi
    private const val PAGE_H = 842
    private const val MARGIN = 32

    fun compose(context: Context, pageUris: List<Uri>, target: File): File? {
        val bitmaps = pageUris.take(2).mapNotNull { loadBitmap(context, it) }
        if (bitmaps.isEmpty()) return null

        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create())
        page.canvas.drawColor(Color.WHITE)

        val slots = bitmaps.size
        val usableH = PAGE_H - MARGIN * (slots + 1)
        val slotH = usableH / slots
        val slotW = PAGE_W - MARGIN * 2

        bitmaps.forEachIndexed { i, bmp ->
            val top = MARGIN + i * (slotH + MARGIN)
            page.canvas.drawBitmap(bmp, null, fitRect(bmp.width, bmp.height, MARGIN, top, slotW, slotH), null)
            bmp.recycle()
        }
        doc.finishPage(page)

        return try {
            target.outputStream().use { doc.writeTo(it) }
            target
        } catch (e: Exception) {
            target.delete()
            null
        } finally {
            doc.close()
        }
    }

    /** Scale to fit within (maxW × maxH) while centred inside the slot. */
    private fun fitRect(w: Int, h: Int, left: Int, top: Int, maxW: Int, maxH: Int): Rect {
        val scale = minOf(maxW.toFloat() / w, maxH.toFloat() / h)
        val dw = (w * scale).toInt()
        val dh = (h * scale).toInt()
        val l = left + (maxW - dw) / 2
        val t = top + (maxH - dh) / 2
        return Rect(l, t, l + dw, t + dh)
    }

    private fun loadBitmap(context: Context, uri: Uri) = try {
        val opts = BitmapFactory.Options().apply { inSampleSize = 2 } // downsample to stay memory-safe
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
    } catch (e: Exception) {
        null
    }
}
