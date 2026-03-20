package com.example.habittrackerapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.habittrackerapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class InicioSesionInterfaz : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_iniciosesioninterfaz)
        // Referenciamos a los elementos de la interfaz (cazamiento de variables)
        val edtCorreo     : TextInputEditText = findViewById(R.id.edt_correo)
        val edtContraseña : TextInputEditText = findViewById(R.id.edt_contraseña)
        val btnLogin      : MaterialButton    = findViewById(R.id.btn_login)
        // Declaramos el evento onclick de 'Login'
        btnLogin.setOnClickListener {
            val correo     = edtCorreo.text.toString()
            val contraseña = edtContraseña.text.toString()

            if (validarCredenciales(correo, contraseña)) {
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
    // Comprobamos que las credenciales sean correctas
    private fun validarCredenciales(correo: String, contraseña: String): Boolean {
        return correo == "admin" && contraseña == "admin"
    }
}
