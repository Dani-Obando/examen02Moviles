package com.example.examen02.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.example.examen02.R
import com.example.examen02.ui.categories.Product
import com.example.examen02.ui.categories.ProductDatabaseHelper

class PurchaseActivity : AppCompatActivity() {

    private lateinit var etProductName: EditText
    private lateinit var etProductPrice: EditText
    private lateinit var etProductQuantity: EditText
    private lateinit var etProductDiscount: EditText
    private lateinit var btnAddProduct: Button
    private lateinit var btnApplyPurchase: Button
    private lateinit var btnCancelPurchase: Button
    private lateinit var listViewProducts: ListView
    private val productList = mutableListOf<Product>()
    private lateinit var productAdapter: ArrayAdapter<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)

        // Inicialización de vistas
        etProductName = findViewById(R.id.etProductName)
        etProductPrice = findViewById(R.id.etProductPrice)
        etProductQuantity = findViewById(R.id.etProductQuantity)
        etProductDiscount = findViewById(R.id.etProductDiscount)
        btnAddProduct = findViewById(R.id.btnAddProduct)
        btnApplyPurchase = findViewById(R.id.btnApplyPurchase)
        btnCancelPurchase = findViewById(R.id.btnCancelPurchase)
        listViewProducts = findViewById(R.id.listViewProducts)

        // Adaptador para el ListView
        productAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, productList)
        listViewProducts.adapter = productAdapter

        // Acción al agregar producto
        btnAddProduct.setOnClickListener {
            val name = etProductName.text.toString()
            val price = etProductPrice.text.toString().toDoubleOrNull() ?: 0.0
            val quantity = etProductQuantity.text.toString().toIntOrNull() ?: 0
            val discount = etProductDiscount.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isNotEmpty() && price > 0 && quantity > 0) {
                val product = Product(name, price, quantity, discount)
                productList.add(product)
                productAdapter.notifyDataSetChanged()

                // Limpiar los campos de entrada
                etProductName.text.clear()
                etProductPrice.text.clear()
                etProductQuantity.text.clear()
                etProductDiscount.text.clear()
            } else {
                Toast.makeText(this, "Por favor ingresa datos válidos", Toast.LENGTH_SHORT).show()
            }
        }

        // Acción al aplicar la compra
        btnApplyPurchase.setOnClickListener {
            applyPurchase()
        }

        // Acción al cancelar la compra
        btnCancelPurchase.setOnClickListener {
            cancelPurchase()
        }
    }

    private fun applyPurchase() {
        val db = FirebaseFirestore.getInstance()

        // Guardar la compra en Firestore
        val purchaseData = hashMapOf(
            "products" to productList,
            "userId" to FirebaseAuth.getInstance().currentUser?.uid
        )

        db.collection("Purchases").add(purchaseData)
            .addOnSuccessListener {
                // Eliminar productos de SQLite
                val dbHelper = ProductDatabaseHelper(this)
                dbHelper.deleteAllProducts()

                Toast.makeText(this, "Compra aplicada correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al aplicar la compra", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cancelPurchase() {
        // Eliminar productos de SQLite
        val dbHelper = ProductDatabaseHelper(this)
        dbHelper.deleteAllProducts()

        Toast.makeText(this, "Compra cancelada", Toast.LENGTH_SHORT).show()
    }
}
