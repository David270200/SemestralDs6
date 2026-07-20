package com.example.stylebyte

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.example.stylebyte.R

/**
 * Enum que mapea cada estado de pedido a sus recursos visuales (texto y colores del chip).
 * Es una decisión de presentación, no de lógica de negocio: solo indica "cómo se ve" cada estado.
 *
 * Estados de negocio (definidos por el Panel de Administración):
 * Pending -> Preparing -> Shipped -> Delivered, o Cancelled en cualquier punto.
 */
enum class OrderStatus(
    @StringRes val labelRes: Int,
    @ColorRes val backgroundColorRes: Int,
    @ColorRes val textColorRes: Int
) {
    PENDING(R.string.status_pending, R.color.status_pending_bg, R.color.status_pending_text),
    PREPARING(R.string.status_preparing, R.color.status_preparing_bg, R.color.status_preparing_text),
    SHIPPED(R.string.status_shipped, R.color.status_shipped_bg, R.color.status_shipped_text),
    DELIVERED(R.string.status_delivered, R.color.status_delivered_bg, R.color.status_delivered_text),
    CANCELLED(R.string.status_cancelled, R.color.status_cancelled_bg, R.color.status_cancelled_text);

    /** El valor exacto que espera/devuelve la columna Pedidos.Estado en SQL Server. */
    fun toEstadoApi(): String = when (this) {
        PENDING -> "Pendiente"
        PREPARING -> "Preparando"
        SHIPPED -> "Enviado"
        DELIVERED -> "Entregado"
        CANCELLED -> "Cancelado"
    }

    companion object {
        /** Convierte el valor de Pedidos.Estado (en español) al enum que usa la UI. */
        fun fromEstadoApi(estado: String): OrderStatus = when (estado) {
            "Pendiente" -> PENDING
            "Preparando" -> PREPARING
            "Enviado" -> SHIPPED
            "Entregado" -> DELIVERED
            "Cancelado" -> CANCELLED
            else -> PENDING
        }
    }
}
