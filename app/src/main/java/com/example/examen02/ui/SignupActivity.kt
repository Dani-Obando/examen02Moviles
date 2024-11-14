package com.example.examen02.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.examen02.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class SignupActivity : AppCompatActivity() {
    var auth = FirebaseAuth.getInstance()
    var db = FirebaseFirestore.getInstance()

    private lateinit var txtRNombre: EditText
    private lateinit var txtREmail: EditText
    private lateinit var txtRContra: EditText
    private lateinit var txtRreContra: EditText
    private lateinit var btnRegistrarU: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        super.onCreate(savedInstanceState)
        txtRNombre = findViewById(R.id.txtRNombre)
        txtREmail = findViewById(R.id.txtREmail)
        txtRContra = findViewById(R.id.txtRContra)
        txtRreContra = findViewById(R.id.txtRreContra)
        btnRegistrarU = findViewById(R.id.btnRegistrarU)

        btnRegistrarU.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        val nombre = txtRNombre.text.toString()
        val email = txtREmail.text.toString()
        val contra = txtRContra.text.toString()
        val reContra = txtRreContra.text.toString()

        if (nombre.isEmpty() || email.isEmpty() || contra.isEmpty() || reContra.isEmpty()) {
            Toast.makeText(this, "Favor de llenar todos los campos", Toast.LENGTH_SHORT).show()
        } else {
            if (contra == reContra) {
                auth.createUserWithEmailAndPassword(email, contra)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val dt: Date = Date()
                            val user = hashMapOf(
                                "idemp" to task.result?.user?.uid,
                                "usuario" to nombre,
                                "email" to email,
                                "ultAcceso" to dt.toString(),
                            )

                            db.collection("Customers").limit(1).get()
                                .addOnSuccessListener { customerSnapshot ->
                                    val customerDocument = customerSnapshot.documents.firstOrNull()
                                    if (customerDocument != null) {
                                        val customerID = customerDocument.id
                                        val contactName = customerDocument.getString("ContactName") ?: ""
                                        val contactTitle = customerDocument.getString("ContactTitle") ?: ""

                                        val updatedCustomer: MutableMap<String, Any> = hashMapOf(
                                            "ContactName" to nombre,
                                            "ContactTitle" to nombre
                                        )

                                        db.collection("Customers").document(customerID).update(updatedCustomer)
                                            .addOnSuccessListener {
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "Error al actualizar el cliente", Toast.LENGTH_SHORT).show()
                                            }

                                        user["CustomerID"] = customerID

                                        db.collection("datosUsuarios")
                                            .add(user)
                                            .addOnSuccessListener { documentReference ->
                                                val shipData = hashMapOf(
                                                    "ShipVia" to customerDocument.getString("ShipVia"),
                                                    "ShipName" to customerDocument.getString("ShipName"),
                                                    "ShipAddress" to customerDocument.getString("ShipAddress"),
                                                    "ShipCity" to customerDocument.getString("ShipCity"),
                                                    "ShipRegion" to customerDocument.getString("ShipRegion"),
                                                    "ShipPostalCode" to customerDocument.getString("ShipPostalCode"),
                                                    "ShipCountry" to customerDocument.getString("ShipCountry")
                                                )

                                                val prefe = this.getSharedPreferences("appData", Context.MODE_PRIVATE)
                                                val editor = prefe.edit()

                                                editor.putString("email", email)
                                                editor.putString("contra", contra)
                                                editor.putString("CustomerID", customerID)

                                                shipData.forEach { (key, value) ->
                                                    editor.putString(key, value)
                                                }

                                                editor.commit()

                                                Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()

                                                Intent().let {
                                                    setResult(Activity.RESULT_OK)
                                                    finish()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(this, "No se encontró un cliente para vincular.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al obtener cliente para vincular", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
