package com.example.stylebyte

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var rvOrderHistory: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        toolbar = findViewById(R.id.toolbar_order_history)
        rvOrderHistory = findViewById(R.id.rv_order_history)

        toolbar.setNavigationOnClickListener { finish() }

        rvOrderHistory.layoutManager = LinearLayoutManager(this)
        rvOrderHistory.adapter = OrderHistoryAdapter(OrderSampleData.sampleOrders)
    }
}