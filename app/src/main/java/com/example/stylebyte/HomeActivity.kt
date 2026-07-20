package com.example.stylebyte

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

/**
 * Pantalla Home: catálogo de productos con búsqueda y filtro por categoría.
 *
 * FASE 1: los datos ya NO son hardcodeados. ProductRepository y CategoryRepository
 * los traen de StyleStoreAPI (SQL Server real) la primera vez que se necesitan.
 * Como esas llamadas son de red, se hacen dentro de "lifecycleScope.launch { }"
 * (una corrutina atada al ciclo de vida de esta Activity: si el usuario sale de
 * la pantalla antes de que responda la API, se cancela sola y no crashea).
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var rvCategories: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var productAdapter: ProductAdapter
    private var selectedCategoryId: String? = null
    private var currentQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bindViews()
        setupProductsRecycler()
        setupSearch()
        setupBottomNavigation()

        loadCategoriesAndProducts()
    }

    override fun onResume() {
        super.onResume()
        // Por si el catálogo cambió desde el Admin mientras el usuario navegaba.
        loadProductsFromApi()
    }

    private fun bindViews() {
        etSearch = findViewById(R.id.et_search)
        rvCategories = findViewById(R.id.rv_categories)
        rvProducts = findViewById(R.id.rv_products)
        tvEmpty = findViewById(R.id.tv_empty_products)
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }

    private fun setupProductsRecycler() {
        rvProducts.layoutManager = GridLayoutManager(this, 2)
        productAdapter = ProductAdapter(
            products = emptyList(),
            onAddToCart = { product -> addToCart(product) },
            onProductClick = { /* Detalle simple: por ahora el grid ya muestra lo esencial. */ }
        )
        rvProducts.adapter = productAdapter
    }

    private fun addToCart(product: Product) {
        if (!SessionManager(this).isLoggedIn()) {
            Toast.makeText(this, "Inicia sesión para agregar productos al carrito", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        lifecycleScope.launch {
            val result = CartManager.addToCart(this@HomeActivity, product)
            result.onSuccess {
                Toast.makeText(this@HomeActivity, getString(R.string.added_to_cart), Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@HomeActivity, it.message ?: "No se pudo agregar el producto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString() ?: ""
                applyLocalFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /** Primera carga: trae categorías (para los chips) y luego productos. */
    private fun loadCategoriesAndProducts() {
        lifecycleScope.launch {
            val categoriesResult = CategoryRepository.refreshFromApi()
            categoriesResult.onSuccess { categories ->
                rvCategories.layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
                rvCategories.adapter = CategoryChipAdapter(categories, selectedCategoryId) { categoryId ->
                    selectedCategoryId = categoryId
                    applyLocalFilters()
                }
            }.onFailure {
                Toast.makeText(this@HomeActivity, "No se pudieron cargar las categorías (¿la API está corriendo?)", Toast.LENGTH_LONG).show()
            }
        }
        loadProductsFromApi()
    }

    /** Pide el catálogo completo a la API (una sola llamada de red) y luego filtra localmente. */
    private fun loadProductsFromApi() {
        lifecycleScope.launch {
            val result = ProductRepository.refreshFromApi()
            result.onFailure {
                Toast.makeText(
                    this@HomeActivity,
                    "No se pudo conectar con la API. Verifica que el servidor Node esté corriendo y la IP en RetrofitClient.",
                    Toast.LENGTH_LONG
                ).show()
            }
            applyLocalFilters()
        }
    }

    /** Filtra la copia YA cargada en memoria (búsqueda/categoría). No hace una llamada de red nueva. */
    private fun applyLocalFilters() {
        lifecycleScope.launch {
            val results = ProductRepository.search(currentQuery, selectedCategoryId)
            productAdapter.updateData(results)
            tvEmpty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    false
                }

                R.id.nav_orders -> {
                    startActivity(Intent(this, OrderHistoryActivity::class.java))
                    false
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }

                else -> false
            }
        }
    }
}
