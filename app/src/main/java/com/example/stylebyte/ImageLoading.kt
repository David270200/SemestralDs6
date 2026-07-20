package com.example.stylebyte

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stylebyte.network.RetrofitClient

/**
 * Carga la imagen real de un producto (servida por la API) en un ImageView,
 * usando Glide. Mientras carga, y si falla o el producto no tiene imagen
 * asignada, se muestra el mismo placeholder gris que ya se usaba antes en
 * todo el proyecto (bg_product_placeholder) — no cambia el diseño visual.
 */
fun ImageView.loadProductImage(rutaImagen: String?) {
    val url = RetrofitClient.buildImageUrl(rutaImagen)

    if (url == null) {
        setImageResource(R.drawable.bg_product_placeholder)
        return
    }

    Glide.with(this)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.ALL) // cachea en disco: no vuelve a descargar la misma imagen
        .placeholder(R.drawable.bg_product_placeholder)
        .error(R.drawable.bg_product_placeholder)
        .centerCrop()
        .into(this)
}
