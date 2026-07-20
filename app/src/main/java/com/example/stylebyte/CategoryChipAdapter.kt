package com.example.stylebyte

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptador de los "chips" de categoría horizontales en Home.
 * selectedCategoryId == null representa "Todas".
 */
class CategoryChipAdapter(
    private val categories: List<Category>,
    private var selectedCategoryId: String?,
    private val onCategorySelected: (String?) -> Unit
) : RecyclerView.Adapter<CategoryChipAdapter.ChipViewHolder>() {

    // Item 0 es siempre "Todas"; el resto son categorías reales.
    private val displayNames: List<Pair<String?, String>> =
        listOf(null to "Todas") + categories.map { it.id to it.name }

    class ChipViewHolder(view: TextView) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_chip, parent, false) as TextView
        return ChipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val (id, name) = displayNames[position]
        holder.textView.text = name

        val isSelected = id == selectedCategoryId
        holder.textView.setBackgroundResource(
            if (isSelected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
        )
        holder.textView.setTextColor(
            holder.textView.context.getColor(
                if (isSelected) R.color.white else R.color.chip_unselected_text
            )
        )

        holder.textView.setOnClickListener {
            if (selectedCategoryId != id) {
                selectedCategoryId = id
                onCategorySelected(id)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = displayNames.size
}
