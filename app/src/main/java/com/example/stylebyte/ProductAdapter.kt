package com.example.stylebyte

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

/** Adaptador del grid de productos en Home. */
class ProductAdapter(
    private var products: List<Product>,
    private val onAddToCart: (Product) -> Unit,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.iv_product_image)
        val tvName: TextView = view.findViewById(R.id.tv_product_name)
        val tvStock: TextView = view.findViewById(R.id.tv_product_stock)
        val tvPrice: TextView = view.findViewById(R.id.tv_product_price)
        val btnAdd: MaterialButton = view.findViewById(R.id.btn_add_to_cart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.tvName.text = product.name
        holder.tvPrice.text = Order.formatPrice(product.price)
        holder.tvStock.text = if (product.stock > 0) "${product.stock} en stock" else "Sin stock"
        holder.ivImage.loadProductImage(product.imageUrl)

        holder.btnAdd.isEnabled = product.stock > 0
        holder.btnAdd.alpha = if (product.stock > 0) 1f else 0.4f
        holder.btnAdd.setOnClickListener { onAddToCart(product) }
        holder.itemView.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
