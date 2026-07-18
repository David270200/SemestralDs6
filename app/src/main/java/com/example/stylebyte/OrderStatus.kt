package com.example.stylebyte

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.example.stylebyte.R

/**
 * Enum que mapea cada estado de pedido a sus recursos visuales (texto y colores del chip).
 * Es una decisión de presentación, no de lógica de negocio: solo indica "cómo se ve" cada estado.
 */
enum class OrderStatus(
    @StringRes val labelRes: Int,
    @ColorRes val backgroundColorRes: Int,
    @ColorRes val textColorRes: Int
) {
    DELIVERED(R.string.status_delivered, R.color.status_delivered_bg, R.color.status_delivered_text),
    PENDING(R.string.status_pending, R.color.status_pending_bg, R.color.status_pending_text),
    SHIPPED(R.string.status_shipped, R.color.status_shipped_bg, R.color.status_shipped_text),
    CANCELLED(R.string.status_cancelled, R.color.status_cancelled_bg, R.color.status_cancelled_text)
}