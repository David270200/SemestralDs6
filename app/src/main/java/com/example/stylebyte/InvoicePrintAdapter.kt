package com.example.stylebyte

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import java.io.FileOutputStream
import java.io.IOException

/**
 * Dibuja la Factura como PDF usando las APIs nativas de impresión de Android
 * (PrintDocumentAdapter + PrintedPdfDocument). No se necesita ninguna librería
 * externa: el propio diálogo de impresión de Android ya trae la opción
 * "Guardar como PDF" como una "impresora" más, así que este mismo código sirve
 * tanto para imprimir en una impresora real como para exportar el PDF.
 */
class InvoicePrintAdapter(
    private val context: Context,
    private val order: Order
) : PrintDocumentAdapter() {

    private var pdfDocument: PrintedPdfDocument? = null

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        pdfDocument = PrintedPdfDocument(context, newAttributes)

        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder("Factura_${order.id}.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1)
            .build()

        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        val document = pdfDocument ?: return
        val page = document.startPage(0)

        try {
            dibujarFactura(page.canvas)
        } finally {
            document.finishPage(page)
        }

        try {
            document.writeTo(FileOutputStream(destination.fileDescriptor))
        } catch (e: IOException) {
            callback.onWriteFailed(e.message)
            return
        } finally {
            document.close()
            pdfDocument = null
        }

        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
    }

    /** Dibuja el contenido de la factura en el canvas de la página PDF. */
    private fun dibujarFactura(canvas: Canvas) {
        val marginLeft = 40f
        var y = 60f

        val paintTitle = Paint().apply { textSize = 22f; isFakeBoldText = true }
        val paintLabel = Paint().apply { textSize = 12f }
        val paintTotal = Paint().apply { textSize = 16f; isFakeBoldText = true }

        canvas.drawText("StyleStore", marginLeft, y, paintTitle)
        y += 22f
        canvas.drawText("Factura de compra", marginLeft, y, paintLabel)
        y += 34f

        canvas.drawText("Número de orden: ${order.id}", marginLeft, y, paintLabel); y += 20f
        canvas.drawText("Fecha: ${order.date}", marginLeft, y, paintLabel); y += 20f
        canvas.drawText("Cliente: ${order.customerName}", marginLeft, y, paintLabel); y += 20f
        canvas.drawText("Correo: ${order.customerEmail}", marginLeft, y, paintLabel); y += 20f
        canvas.drawText("Método de entrega: ${order.deliveryMethod}", marginLeft, y, paintLabel); y += 20f
        canvas.drawText("Estado del pedido: ${context.getString(order.status.labelRes)}", marginLeft, y, paintLabel); y += 34f

        canvas.drawText("Productos:", marginLeft, y, Paint().apply { textSize = 13f; isFakeBoldText = true })
        y += 22f

        for (item in order.items) {
            canvas.drawText(
                "${item.quantity}x ${item.productName}",
                marginLeft, y, paintLabel
            )
            y += 18f
            canvas.drawText(
                "   ${Order.formatPrice(item.unitPrice)} c/u  =  ${Order.formatPrice(item.subtotal)}",
                marginLeft, y, paintLabel
            )
            y += 24f
        }

        y += 10f
        canvas.drawText("Subtotal: ${Order.formatPrice(order.subtotal)}", marginLeft, y, paintLabel); y += 20f
        canvas.drawText("Costo de envío: ${Order.formatPrice(order.deliveryFee)}", marginLeft, y, paintLabel); y += 28f
        canvas.drawText("Total: ${order.totalPrice}", marginLeft, y, paintTotal)
    }
}
