package com.example.stylebyte

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

/**
 * Gestión de Categorías del Admin — CRUD completo conectado a SQL Server real
 * (GET/POST/PUT/DELETE /categories).
 */
class AdminCategoriesActivity : AppCompatActivity() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var adapter: AdminCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_categories)

        findViewById<Toolbar>(R.id.toolbar_admin_categories).setNavigationOnClickListener { finish() }

        rvCategories = findViewById(R.id.rv_admin_categories)
        rvCategories.layoutManager = LinearLayoutManager(this)
        adapter = AdminCategoryAdapter(
            categories = emptyList(),
            onEdit = { showCategoryDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        rvCategories.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fab_add_category).setOnClickListener {
            showCategoryDialog(null)
        }

        loadFromApi()
    }

    override fun onResume() {
        super.onResume()
        loadFromApi()
    }

    private fun loadFromApi() {
        lifecycleScope.launch {
            val result = CategoryRepository.refreshFromApi()
            result.onFailure {
                Toast.makeText(this@AdminCategoriesActivity, "No se pudo conectar con la API de categorías", Toast.LENGTH_LONG).show()
            }
            adapter.updateData(CategoryRepository.getAll())
        }
    }

    private fun refresh() {
        lifecycleScope.launch {
            adapter.updateData(CategoryRepository.getAll())
        }
    }

    private fun confirmDelete(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar categoría")
            .setMessage("¿Seguro que deseas eliminar \"${category.name}\"? Los productos que la usaban quedarán sin categoría.")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    val result = CategoryRepository.delete(category.id)
                    result.onSuccess {
                        refresh()
                        Toast.makeText(this@AdminCategoriesActivity, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(this@AdminCategoriesActivity, it.message ?: "No se pudo eliminar la categoría", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showCategoryDialog(category: Category?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_category, null)
        val etName = view.findViewById<EditText>(R.id.et_dialog_category_name)
        if (category != null) etName.setText(category.name)

        AlertDialog.Builder(this)
            .setTitle(if (category == null) "Nueva categoría" else "Editar categoría")
            .setView(view)
            .setPositiveButton(if (category == null) "Agregar" else "Guardar") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Ingresa un nombre válido", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val result = if (category == null) CategoryRepository.add(name) else CategoryRepository.update(category.id, name)
                    result.onSuccess {
                        refresh()
                        Toast.makeText(
                            this@AdminCategoriesActivity,
                            if (category == null) "Categoría agregada" else "Categoría actualizada",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.onFailure {
                        Toast.makeText(this@AdminCategoriesActivity, it.message ?: "No se pudo guardar la categoría", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
