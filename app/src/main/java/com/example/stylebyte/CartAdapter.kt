package com.example.stylebyte

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stylebyte.network.dto.CartItemDto

/** Adaptador del listado de items del carrito (ahora viene directo de la API). */
class CartAdapter(
    private var items: List<CartItemDto>,
    private val onQuantityChange: (idProducto: Int, newQuantity: Int) -> Unit,
    private val onRemove: (idProducto: Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.iv_cart_item_image)
        val tvName: TextView = view.findViewById(R.id.tv_cart_item_name)
        val tvPrice: TextView = view.findViewById(R.id.tv_cart_item_price)
        val tvQty: TextView = view.findViewById(R.id.tv_qty)
        val btnDecrease: ImageView = view.findViewById(R.id.btn_decrease_qty)
        val btnIncrease: ImageView = view.findViewById(R.id.btn_increase_qty)
        val ivRemove: ImageView = view.findViewById(R.id.iv_cart_remove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.ivImage.loadProductImage(item.RutaImagen)
        holder.tvName.text = item.Nombre
        holder.tvPrice.text = Order.formatPrice(item.Precio)
        holder.tvQty.text = item.Cantidad.toString()

        holder.btnDecrease.setOnClickListener { onQuantityChange(item.IdProducto, item.Cantidad - 1) }
        holder.btnIncrease.setOnClickListener {
            // No dejamos superar el stock disponible en la base de datos.
            val nuevaCantidad = item.Cantidad + 1
            if (nuevaCantidad > item.Stock) {
                android.widget.Toast.makeText(holder.itemView.context, "No hay más stock disponible", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                onQuantityChange(item.IdProducto, nuevaCantidad)
            }
        }
        holder.ivRemove.setOnClickListener { onRemove(item.IdProducto) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<CartItemDto>) {
        items = newItems
        notifyDataSetChanged()
    }
}
