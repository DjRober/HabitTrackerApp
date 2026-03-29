package com.example.habittrackerapp.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.habittrackerapp.R
import com.example.habittrackerapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    // Creamos el binding (privado)
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Definimos el evento 'onClick' del botón principal
        binding.btnRegister.setOnClickListener {
            if (validarCampos()) {
                ejecutarRegistro()
            }
        }
        // Regresamos al login usando la misma instancia (no creamos una nueva)
        binding.tvLoginLink.setOnClickListener {
            finish()
        }
        // Limpiamos los errores que el usuario crea al escribir
        binding.edtName.setOnFocusChangeListener { _, _ ->
            binding.tilName.error = null
        }
        binding.edtCorreo.setOnFocusChangeListener { _, _ ->
            binding.tilEmail.error = null
        }
        binding.edtPassword.setOnFocusChangeListener { _, _ ->
            binding.tilPassword.error = null
        }
        binding.edtConfirmPassword.setOnFocusChangeListener { _, _ ->
            binding.tilConfirmPassword.error = null
        }
    }
    // Validamos todos los campos del formulario
    private fun validarCampos(): Boolean {
        val nombre          = binding.edtName.text.toString().trim()
        val correo          = binding.edtCorreo.text.toString().trim()
        val contraseña      = binding.edtPassword.text.toString()
        val confirmar       = binding.edtConfirmPassword.text.toString()
        var esValido        = true
        // Nombre no vacío (validación)
        if (nombre.isEmpty()) {
            binding.tilName.error = getString(R.string.error_empty_field)
            esValido = false
        } else {
            binding.tilName.error = null
        }
        // Correo con formato válido (validación)
        if (correo.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_empty_field)
            esValido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            esValido = false
        } else {
            binding.tilEmail.error = null
        }
        // Contraseña mínimo 6 caracteres (validación)
        if (contraseña.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_empty_field)
            esValido = false
        } else if (contraseña.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_short)
            esValido = false
        } else {
            binding.tilPassword.error = null
        }
        // Las contraseñas coinciden (validación)
        if (confirmar.isEmpty()) {
            binding.tilConfirmPassword.error = getString(R.string.error_empty_field)
            esValido = false
        } else if (contraseña != confirmar) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_dont_match)
            esValido = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return esValido
    }
    // Simulamos el estado de carga del registro (se reemplaza con Firebase Auth)
    private fun ejecutarRegistro() {
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text      = getString(R.string.btn_register_loading)

        // Simulamos creación de cuenta (1.5 segundos)
        Handler(Looper.getMainLooper()).postDelayed({
            // Nota: aquí irá Firebase Auth createUserWithEmailAndPassword()
            // Por ahora regresamos al login con la cuenta "creada"
            finish()
        }, 1300)
    }
}
