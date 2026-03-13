package com.example.habittrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.habittrackerapp.MainActivity
import com.example.habittrackerapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class inicioSesionInterfaz : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_iniciosesioninterfaz)

        // Cazamiento
        val edt_correo : TextInputEditText = findViewById<TextInputEditText>(R.id.edt_correo)
        val edt_contraseña : TextInputEditText = findViewById<TextInputEditText>(R.id.edt_contraseña)
        val btn_login : MaterialButton = findViewById<MaterialButton>(R.id.btn_login)

        // Eventos onClick:
        // Login:
        btn_login.setOnClickListener {
            println("Login clickeado") // Debugeo temporal

            // Asignamos las variables:
            val correo = edt_correo.text.toString()
            val contraseña = edt_contraseña.text.toString()

            // Validamos las credenciales (Por el momento hardcodeadas)
            if(validarCredenciales(correo, contraseña)){
                val intent : Intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

        }
    }
    // Funciones:
    // Comprobar credenciales:
    fun validarCredenciales(correo: String, contraseña: String): Boolean{
        println("Entramos a la validacion") // Debugeo

        if(correo == "admin" && contraseña == "admin"){
            return true // Retorna true si se cumple
        }
        return false // En caso de no cumplir la condicion se niega el acceso
    }

}