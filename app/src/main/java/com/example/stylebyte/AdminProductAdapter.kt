package com.example.stylebyte

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminProductAdapter(
    private var products: List<Product>,
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.VH>() {

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.iv_admin_product_image)
        val tvName: TextView = view.findViewById(R.id.tv_admin_product_name)
        val tvCategory: TextView = view.findViewById(R.id.tv_admin_product_category)
        val tvPriceStock: TextView = view.findViewById(R.id.tv_admin_product_price_stock)
        val btnEdit: ImageView = view.findViewById(R.id.btn_edit_product)
        val btnDelete: ImageView = view.findViewById(R.id.btn_delete_product)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_product, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val product = products[position]
        holder.ivImage.loadProductImage(product.imageUrl)
        holder.tvName.text = product.name
        holder.tvCategory.text = CategoryRepository.findById(product.categoryId)?.name ?: "Sin categoría"
        holder.tvPriceStock.text = "${Order.formatPrice(product.price)} · Stock: ${product.stock}"
        holder.btnEdit.setOnClickListener { onEdit(product) }
        holder.btnDelete.setOnClickListener { onDelete(product) }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
