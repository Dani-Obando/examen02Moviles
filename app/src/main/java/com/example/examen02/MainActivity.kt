package com.example.examen02

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.widget.ListView
import android.widget.Toast
import com.example.examen02.ui.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.example.examen02.entities.cls_Category
import com.example.examen02.ui.categories.CategoryAdapter

const val valorIntentLogin = 1

class MainActivity : AppCompatActivity() {
    private var auth = FirebaseAuth.getInstance()
    private var email: String? = null
    private var contra: String? = null
    private var db = FirebaseFirestore.getInstance()
    private var TAG = "DanTestingApp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Obtener credenciales guardadas en el almacenamiento local
        val prefe = getSharedPreferences("appData", Context.MODE_PRIVATE)
        email = prefe.getString("email", "")
        contra = prefe.getString("contra", "")

        // Verificar si las credenciales están vacías o no
        if (email.isNullOrEmpty() || contra.isNullOrEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, valorIntentLogin)
        } else {
            // Si las credenciales existen, intenta autenticar al usuario
            auth.signInWithEmailAndPassword(email.toString(), contra.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Autenticación correcta", Toast.LENGTH_SHORT).show()
                        obtenerDatos()  // Cargar los datos de Firestore
                    } else {
                        // Si la autenticación falla, redirigir al login
                        Toast.makeText(this, "Autenticación fallida. Por favor, intente nuevamente.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivityForResult(intent, valorIntentLogin)
                    }
                }
        }
    }

    private fun obtenerDatos() {
        // Obtener categorías desde Firestore
        val coleccion: ArrayList<cls_Category?> = ArrayList()
        val listaView: ListView = findViewById(R.id.lstCategories)

        db.collection("Categories").orderBy("CategoryID")
            .get()
            .addOnCompleteListener { docc ->
                if (docc.isSuccessful) {
                    for (document in docc.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        val datos = cls_Category(
                            document.data["CategoryID"].toString().toInt(),
                            document.data["CategoryName"].toString(),
                            document.data["Description"].toString(),
                            document.data["urlImage"].toString()
                        )
                        coleccion.add(datos)
                    }
                    // Usar un adapter para mostrar las categorías en el ListView
                    val adapter = CategoryAdapter(this, coleccion)
                    listaView.adapter = adapter
                } else {
                    Log.w(TAG, "Error getting documents.", docc.exception)
                }
            }
    }
}
