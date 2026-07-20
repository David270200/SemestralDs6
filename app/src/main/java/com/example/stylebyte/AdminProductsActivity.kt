package com.example.stylebyte

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
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
 * Gestión de Productos del Admin: agregar, editar, eliminar, cambiar precio/stock,
 * buscar y filtrar por categoría — todo conectado a SQL Server real vía la API
 * (ProductRepository/CategoryRepository).
 */
class AdminProductsActivity : AppCompatActivity() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var rvCategoriesFilter: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: AdminProductAdapter

    private var currentQuery = ""
    private var currentCategoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_products)

        findViewById<Toolbar>(R.id.toolbar_admin_products).setNavigationOnClickListener { finish() }

        rvProducts = findViewById(R.id.rv_admin_products)
        rvCategoriesFilter = findViewById(R.id.rv_categories_filter)
        etSearch = findViewById(R.id.et_admin_product_search)

        rvProducts.layoutManager = LinearLayoutManager(this)
        adapter = AdminProductAdapter(
            products = emptyList(),
            onEdit = { showProductDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        rvProducts.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString() ?: ""
                refresh()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<FloatingActionButton>(R.id.fab_add_product).setOnClickListener {
            showProductDialog(null)
        }

        loadEverything()
    }

    override fun onResume() {
        super.onResume()
        loadEverything()
    }

    /** Carga (o recarga) categorías + productos desde la API y refresca la UI. */
    private fun loadEverything() {
        lifecycleScope.launch {
            val categoriesResult = CategoryRepository.refreshFromApi()
            categoriesResult.onSuccess { categories ->
                rvCategoriesFilter.layoutManager = LinearLayoutManager(this@AdminProductsActivity, LinearLayoutManager.HORIZONTAL, false)
                rvCategoriesFilter.adapter = CategoryChipAdapter(categories, currentCategoryId) { categoryId ->
                    currentCategoryId = categoryId
                    refresh()
                }
            }.onFailure {
                Toast.makeText(this@AdminProductsActivity, "No se pudieron cargar las categorías desde la API", Toast.LENGTH_LONG).show()
            }

            val productsResult = ProductRepository.refreshFromApi()
            productsResult.onFailure {
                Toast.makeText(this@AdminProductsActivity, "No se pudo conectar con la API de productos", Toast.LENGTH_LONG).show()
            }

            refresh()
        }
    }

    /** Filtra la copia ya cargada en memoria (no dispara una llamada de red nueva). */
    private fun refresh() {
        lifecycleScope.launch {
            adapter.updateData(ProductRepository.search(currentQuery, currentCategoryId))
        }
    }

    private fun confirmDelete(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Seguro que deseas eliminar \"${product.name}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    val result = ProductRepository.delete(product.id)
                    result.onSuccess {
                        refresh()
                        Toast.makeText(this@AdminProductsActivity, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(this@AdminProductsActivity, it.message ?: "No se pudo eliminar el producto", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** Un mismo diálogo sirve para "Agregar" (product == null) y "Editar". */
    private fun showProductDialog(product: Product?) {
        lifecycleScope.launch {
            val categories = CategoryRepository.getAll()
            if (categories.isEmpty()) {
                Toast.makeText(this@AdminProductsActivity, "No hay categorías cargadas todavía", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val view = LayoutInflater.from(this@AdminProductsActivity).inflate(R.layout.dialog_add_edit_product, null)
            val etName = view.findViewById<EditText>(R.id.et_dialog_product_name)
            val etDescription = view.findViewById<EditText>(R.id.et_dialog_product_description)
            val etPrice = view.findViewById<EditText>(R.id.et_dialog_product_price)
            val etStock = view.findViewById<EditText>(R.id.et_dialog_product_stock)
            val spinnerCategory = view.findViewById<Spinner>(R.id.spinner_dialog_product_category)

            spinnerCategory.adapter = ArrayAdapter(
                this@AdminProductsActivity, android.R.layout.simple_spinner_dropdown_item, categories.map { it.name }
            )

            if (product != null) {
                etName.setText(product.name)
                etDescription.setText(product.description)
                etPrice.setText(product.price.toString())
                etStock.setText(product.stock.toString())
                val idx = categories.indexOfFirst { it.id == product.categoryId }
                if (idx >= 0) spinnerCategory.setSelection(idx)
            }

            AlertDialog.Builder(this@AdminProductsActivity)
                .setTitle(if (product == null) "Agregar producto" else "Editar producto")
                .setView(view)
                .setPositiveButton(if (product == null) "Agregar" else "Guardar") { _, _ ->
                    val name = etName.text.toString().trim()
                    val description = etDescription.text.toString().trim()
                    val price = etPrice.text.toString().toDoubleOrNull()
                    val stock = etStock.text.toString().toIntOrNull()
                    val categoryId = categories.getOrNull(spinnerCategory.selectedItemPosition)?.id

                    if (name.isEmpty() || price == null || stock == null || categoryId == null) {
                        Toast.makeText(this@AdminProductsActivity, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    lifecycleScope.launch {
                        val result = if (product == null) {
                            ProductRepository.add(Product("", name, description, price, categoryId, stock))
                        } else {
                            ProductRepository.update(
                                product.copy(name = name, description = description, price = price, stock = stock, categoryId = categoryId)
                            )
                        }
                        result.onSuccess {
                            refresh()
                            Toast.makeText(
                                this@AdminProductsActivity,
                                if (product == null) "Producto agregado" else "Producto actualizado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.onFailure {
                            Toast.makeText(this@AdminProductsActivity, it.message ?: "No se pudo guardar el producto", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}
