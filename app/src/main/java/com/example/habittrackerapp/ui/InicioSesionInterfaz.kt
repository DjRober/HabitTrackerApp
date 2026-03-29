package com.example.habittrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.habittrackerapp.MainActivity
import com.example.habittrackerapp.R
import com.example.habittrackerapp.databinding.ActivityIniciosesioninterfazBinding

class InicioSesionInterfaz : AppCompatActivity() {

    // Iniciamos y usamos 'ViewBinding' para consistencia con el resto
    private lateinit var binding: ActivityIniciosesioninterfazBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIniciosesioninterfazBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Definimos el evento 'onClick' del botón principal
        binding.btnLogin.setOnClickListener {
            if (validarCampos()) {
                ejecutarLogin()
            }
        }
        // Definimos el evento 'onClick' del botón de a la pantalla de registro
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        // Definimos el evento de la pantalla de recuperación de contraseña
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        // Limpiamos el error del campo 'correo' mientras el usuario escribe
        binding.edtCorreo.setOnFocusChangeListener { _, _ ->
            binding.tilEmail.error = null
        }
        // Limpiamos el error del campo 'contraseña' mientras el usuario escribe
        binding.edtContrasena.setOnFocusChangeListener { _, _ ->
            binding.tilPassword.error = null
        }
    }
    // Validamos los campos antes de intentar el login
    private fun validarCampos(): Boolean {
        val correo     = binding.edtCorreo.text.toString().trim()
        val contraseña = binding.edtContrasena.text.toString()
        var esValido   = true
        // Verificamos que el correo no esté vacío y contenga un formato válido
        if (correo.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_empty_field)
            esValido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            esValido = false
        } else {
            binding.tilEmail.error = null
        }
        // Verificamos que la contraseña no esté vacía y tenga mínimo 6 caracteres
        if (contraseña.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_empty_field)
            esValido = false
        } else if (contraseña.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_short)
            esValido = false
        } else {
            binding.tilPassword.error = null
        }
        return esValido    // Devolvemos el resultado de la validación
    }
    // Simulamos el estado de carga mientras se verifican las credenciales
    private fun ejecutarLogin() {
        val correo     = binding.edtCorreo.text.toString().trim()
        val contraseña = binding.edtContrasena.text.toString()
        // Deshabilitamos botón y cambiamos texto (mostramos el estado de carga)
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text      = getString(R.string.btn_loading)
        // Simulamos una verificación de 1.2 segundos (se reemplaza con Firebase Auth)
        Handler(Looper.getMainLooper()).postDelayed({
            if (validarCredenciales(correo, contraseña)) {
                // Navegamos al Home y limpiamos el back stack (credenciales correctas)
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                // Mostramos error y restauramos el botón (credenciales incorrectas)
                binding.tilEmail.error    = getString(R.string.error_invalid_credentials)
                binding.tilPassword.error = getString(R.string.error_invalid_credentials)
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text      = getString(R.string.btn_login)
            }
        }, 1000)
    }
    // Comprobamos credenciales hardcodeadas (reemplazar con Firebase Auth)
    private fun validarCredenciales(correo: String, contraseña: String): Boolean {
        return correo == "admin@habitus.com" && contraseña == "admin1"
    }
}
