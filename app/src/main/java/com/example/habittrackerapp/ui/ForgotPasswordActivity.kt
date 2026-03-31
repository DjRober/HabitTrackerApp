package com.example.habittrackerapp.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.habittrackerapp.R
import com.example.habittrackerapp.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    // Creamos el 'binding' (privado)
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Creamos el evento 'onClick' del botón principal
        binding.btnSend.setOnClickListener {
            if (validarCampo()) {
                ejecutarEnvio()
            }
        }
        // Creamos el evento para  regresar al login
        binding.tvBackLogin.setOnClickListener {
            finish()
        }
        // Limpiamos los errores conforme el usuario escribe
        binding.edtCorreo.setOnFocusChangeListener { _, _ ->
            binding.tilEmail.error = null
        }
    }
    // Validamos que el correo contenga un formato válido
    private fun validarCampo(): Boolean {
        val correo = binding.edtCorreo.text.toString().trim()

        return if (correo.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_empty_field)
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            false
        } else {
            binding.tilEmail.error = null
            true
        }
    }
    // Simulamos el envío del correo de recuperación
    private fun ejecutarEnvio() {
        // Mostramos estado de carga
        binding.btnSend.isEnabled = false
        binding.btnSend.text      = getString(R.string.btn_send_loading)

        // Simulamos el proceso de 1.5 segundos (se reemplaza con Firebase Auth)
        Handler(Looper.getMainLooper()).postDelayed({
            // Nota: aquí irá Firebase Auth sendPasswordResetEmail()
            mostrarConfirmacion()
        }, 1300)
    }
    // Ocultamos el formulario y mostramos el mensaje de confirmación
    private fun mostrarConfirmacion() {
        // Ocultamos los elementos del formulario
        binding.tilEmail.visibility  = View.GONE
        binding.btnSend.visibility   = View.GONE
        // Actualizamos el título y subtítulo con el mensaje de éxito
        binding.tvTitle.text    = getString(R.string.forgot_success_title)
        binding.tvSubtitle.text = getString(R.string.forgot_success_message)
        // Mostramos el link de regreso al login
        binding.tvBackLogin.visibility = View.VISIBLE
    }
}
