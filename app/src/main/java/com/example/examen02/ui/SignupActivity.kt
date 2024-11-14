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
    // Declarar los EditTexts para obtener los datos de los campos
    private lateinit var txtRNombre: EditText
    private lateinit var txtREmail: EditText
    private lateinit var txtRContra: EditText
    private lateinit var txtRreContra: EditText
    private lateinit var btnRegistrarU: Button

    // Declarar los EditTexts para los datos de envío
    private lateinit var txtRShipName: EditText
    private lateinit var txtRShipAddress: EditText
    private lateinit var txtRShipCity: EditText
    private lateinit var txtRShipRegion: EditText
    private lateinit var txtRShipPostalCode: EditText
    private lateinit var txtRShipCountry: EditText
    val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        // Inicializar los EditTexts
        txtRNombre = findViewById(R.id.txtRNombre)
        txtREmail = findViewById(R.id.txtREmail)
        txtRContra = findViewById(R.id.txtRContra)
        txtRreContra = findViewById(R.id.txtRreContra)
        btnRegistrarU = findViewById(R.id.btnRegistrarU)

        // Inicializar los EditTexts de los campos de dirección
        txtRShipName = findViewById(R.id.txtRShipName)
        txtRShipAddress = findViewById(R.id.txtRShipAddress)
        txtRShipCity = findViewById(R.id.txtRShipCity)
        txtRShipRegion = findViewById(R.id.txtRShipRegion)
        txtRShipPostalCode = findViewById(R.id.txtRShipPostalCode)
        txtRShipCountry = findViewById(R.id.txtRShipCountry)

        // Configurar el botón para registrar al usuario
        btnRegistrarU.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        // Obtener los valores de los campos de entrada
        val nombre = txtRNombre.text.toString()
        val email = txtREmail.text.toString()
        val contra = txtRContra.text.toString()
        val reContra = txtRreContra.text.toString()

        // Obtener los valores de los campos de dirección
        val shipName = txtRShipName.text.toString()
        val shipAddress = txtRShipAddress.text.toString()
        val shipCity = txtRShipCity.text.toString()
        val shipRegion = txtRShipRegion.text.toString()
        val shipPostalCode = txtRShipPostalCode.text.toString()
        val shipCountry = txtRShipCountry.text.toString()

        // Validar que todos los campos no estén vacíos
        if (nombre.isEmpty() || email.isEmpty() || contra.isEmpty() || reContra.isEmpty() ||
            shipName.isEmpty() || shipAddress.isEmpty() || shipCity.isEmpty() || shipRegion.isEmpty() ||
            shipPostalCode.isEmpty() || shipCountry.isEmpty()) {
            Toast.makeText(this, "Favor de llenar todos los campos", Toast.LENGTH_SHORT).show()
        } else {
            // Validar que las contraseñas coincidan
            if (contra == reContra) {
                // Crear el usuario en Firebase Authentication
                auth.createUserWithEmailAndPassword(email, contra)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Si el registro es exitoso, guardar los datos en Firestore
                            val dt: Date = Date()
                            val user = hashMapOf(
                                "idemp" to task.result?.user?.uid,
                                "usuario" to nombre,
                                "email" to email,
                                "ultAcceso" to dt.toString(),
                                "shipName" to shipName,
                                "shipAddress" to shipAddress,
                                "shipCity" to shipCity,
                                "shipRegion" to shipRegion,
                                "shipPostalCode" to shipPostalCode,
                                "shipCountry" to shipCountry
                            )

                            // Guardar en Firestore
                            val db = FirebaseFirestore.getInstance()
                            db.collection("usuarios").add(user)
                                .addOnSuccessListener {
                                    // Almacenar la información en el almacenamiento local (SharedPreferences)
                                    val sharedPref = getSharedPreferences("MiApp", Context.MODE_PRIVATE)
                                    val editor = sharedPref.edit()
                                    editor.putString("customerID", task.result?.user?.uid)
                                    editor.putString("nombre", nombre)
                                    editor.putString("email", email)
                                    editor.putString("shipName", shipName)
                                    editor.putString("shipAddress", shipAddress)
                                    editor.putString("shipCity", shipCity)
                                    editor.putString("shipRegion", shipRegion)
                                    editor.putString("shipPostalCode", shipPostalCode)
                                    editor.putString("shipCountry", shipCountry)
                                    editor.apply()

                                    Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                                    // Volver a la actividad de login o a la actividad principal
                                    finish()
                                }
                        } else {
                            // Si hay un error al registrar el usuario
                            Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Si las contraseñas no coinciden
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

