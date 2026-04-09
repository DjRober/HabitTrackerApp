package com.example.habittrackerapp.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.habittrackerapp.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {

    // Creamos el binding
    private lateinit var binding: ActivityNotificationsBinding
    // Declaramos las claves de 'SharedPreferences' para notificaciones
    companion object {
        const val PREFS_NAME        = "habitus_prefs"
        const val KEY_NOTIF_ENABLED = "notif_enabled"
        const val KEY_NOTIF_TIME    = "notif_time"
        const val TIME_MORNING      = "morning"
        const val TIME_AFTERNOON    = "afternoon"
        const val TIME_EVENING      = "evening"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Cargamos las preferencias guardadas
        val prefs          = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notifActivadas = prefs.getBoolean(KEY_NOTIF_ENABLED, false)
        val horaGuardada   = prefs.getString(KEY_NOTIF_TIME, TIME_MORNING)
        // Aplicamos el estado inicial al toggle
        binding.switchNotifications.isChecked = notifActivadas
        actualizarSeleccionHora(horaGuardada ?: TIME_MORNING)
        actualizarEstadoOpciones(notifActivadas)
        // El toggle activa o desactiva las notificaciones (depende)
        binding.switchNotifications.setOnCheckedChangeListener { _, activado ->
            prefs.edit().putBoolean(KEY_NOTIF_ENABLED, activado).apply()
            actualizarEstadoOpciones(activado)
        }
        // Declaramos los eventos de 'Selección de hora'
        binding.optionMorning.setOnClickListener {
            guardarHora(TIME_MORNING)
            actualizarSeleccionHora(TIME_MORNING)
        }
        binding.optionAfternoon.setOnClickListener {
            guardarHora(TIME_AFTERNOON)
            actualizarSeleccionHora(TIME_AFTERNOON)
        }
        binding.optionEvening.setOnClickListener {
            guardarHora(TIME_EVENING)
            actualizarSeleccionHora(TIME_EVENING)
        }
        binding.btnBack.setOnClickListener { finish() }
    }
    // Guardamos la hora seleccionada en 'SharedPreferences'
    private fun guardarHora(hora: String) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NOTIF_TIME, hora)
            .apply()
    }
    // Actualizamos el check visual según la hora seleccionada
    private fun actualizarSeleccionHora(horaSeleccionada: String) {
        binding.checkMorning.visibility   =
            if (horaSeleccionada == TIME_MORNING)   View.VISIBLE else View.GONE
        binding.checkAfternoon.visibility =
            if (horaSeleccionada == TIME_AFTERNOON) View.VISIBLE else View.GONE
        binding.checkEvening.visibility   =
            if (horaSeleccionada == TIME_EVENING)   View.VISIBLE else View.GONE
    }
    // Habilitamos o deshabilitamos el selector de hora según el toggle
    private fun actualizarEstadoOpciones(activado: Boolean) {
        val alpha = if (activado) 1.0f else 0.4f
        binding.optionMorning.alpha   = alpha
        binding.optionAfternoon.alpha = alpha
        binding.optionEvening.alpha   = alpha
        binding.optionMorning.isEnabled   = activado
        binding.optionAfternoon.isEnabled = activado
        binding.optionEvening.isEnabled   = activado
    }
}
