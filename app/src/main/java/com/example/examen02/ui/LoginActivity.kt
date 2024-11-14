package com.example.examen02.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import com.example.examen02.R
import com.example.examen02.ui.SignupActivity

const val valorIntentSignup = 1

class LoginActivity : AppCompatActivity() {
    private var auth = FirebaseAuth.getInstance()
    private lateinit var btnAutenticar: Button
    private lateinit var txtEmail: EditText
    private lateinit var txtContra: EditText
    private lateinit var txtRegister: TextView
    private lateinit var progressBar: ProgressBar
    private var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnAutenticar = findViewById(R.id.btnAutenticar)
        txtEmail = findViewById(R.id.txtEmail)
        txtContra = findViewById(R.id.txtContra)
        txtRegister = findViewById(R.id.txtRegister)
        progressBar = findViewById(R.id.progressBar)  // Asegúrate de que el ProgressBar esté en el layout

        txtRegister.setOnClickListener {
            goToSignup()
        }

        btnAutenticar.setOnClickListener {
            if (txtEmail.text.isNotEmpty() && txtContra.text.isNotEmpty()) {
                showLoading(true)  // Mostrar carga
                auth.signInWithEmailAndPassword(txtEmail.text.toString(), txtContra.text.toString())
                    .addOnCompleteListener {
                        showLoading(false)  // Ocultar carga
                        if (it.isSuccessful) {
                            val dt: Date = Date()
                            val user = hashMapOf("ultAcceso" to dt.toString())

                            db.collection("datosUsuarios")
                                .whereEqualTo("idemp", it.result?.user?.uid.toString())
                                .get()
                                .addOnSuccessListener { documentReference ->
                                    documentReference.forEach { document ->
                                        db.collection("datosUsuarios").document(document.id).update(user as Map<String, Any>)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al actualizar los datos del usuario", Toast.LENGTH_SHORT).show()
                                }

                            // Guardar en almacenamiento local
                            val prefe = this.getSharedPreferences("appData", Context.MODE_PRIVATE)
                            val editor = prefe.edit()
                            editor.putString("email", txtEmail.text.toString())
                            editor.putString("contra", txtContra.text.toString())
                            editor.apply()  // Preferir apply() en lugar de commit()

                            // Volver a la actividad principal
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            showAlert("Error", "Al autenticar el usuario: ${it.exception?.message}")
                        }
                    }
            } else {
                showAlert("Error", "El correo electrónico y la contraseña no pueden estar vacíos")
            }
        }
    }

    private fun goToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivityForResult(intent, valorIntentSignup)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun showAlert(titu: String, mssg: String) {
        AlertDialog.Builder(this)
            .setTitle(titu)
            .setMessage(mssg)
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
