package com.example.stylebyte

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class AdminUserAdapter(
    private var users: List<AdminUser>,
    private val onToggleActive: (AdminUser) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.VH>() {

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView = view.findViewById(R.id.tv_admin_user_avatar)
        val tvName: TextView = view.findViewById(R.id.tv_admin_user_name)
        val tvEmail: TextView = view.findViewById(R.id.tv_admin_user_email)
        val tvOrders: TextView = view.findViewById(R.id.tv_admin_user_orders)
        val chipStatus: Chip = view.findViewById(R.id.chip_admin_user_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val user = users[position]
        val context = holder.itemView.context

        holder.tvAvatar.text = user.name.trim().firstOrNull()?.uppercase() ?: "?"
        holder.tvName.text = user.name
        holder.tvEmail.text = "${user.email}  ·  ${user.role}"
        holder.tvOrders.text = if (user.ordersCount == 1) "1 pedido" else "${user.ordersCount} pedidos"

        if (user.active) {
            holder.chipStatus.text = "Activo"
            holder.chipStatus.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.status_delivered_bg)
            holder.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_delivered_text))
        } else {
            holder.chipStatus.text = "Inactivo"
            holder.chipStatus.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.status_cancelled_bg)
            holder.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_cancelled_text))
        }

        // Tocar el chip activa/inactiva la cuenta (solo Admin puede llegar a esta pantalla).
        holder.chipStatus.setOnClickListener { onToggleActive(user) }
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<AdminUser>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
