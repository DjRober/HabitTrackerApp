package com.example.habittrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittrackerapp.R
import com.example.habittrackerapp.data.HabitRepository
import com.example.habittrackerapp.databinding.ActivityHabitDetailBinding
import kotlinx.coroutines.launch

class HabitDetailActivity : AppCompatActivity() {    // Declaramos la actividad de detalle de hábito
    // Declaramos las variables
    private lateinit var binding: ActivityHabitDetailBinding
    private val habitRepository = HabitRepository()
    // Declaramos las constantes
    private var habitoId         = ""
    private var completadoHoy    = false
    private var rachaActual      = 0
    private var porcentajeActual = 0
    // Declaramos los métodos
    companion object {
        const val EXTRA_HABIT_ID          = "habit_id"
        const val EXTRA_HABIT_NAME        = "habit_name"
        const val EXTRA_HABIT_FREQUENCY   = "habit_frequency"
        const val EXTRA_HABIT_STREAK      = "habit_streak"
        const val EXTRA_HABIT_PERCENT     = "habit_percent"
        const val EXTRA_HABIT_CATEGORY_ID = "habit_category_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Recuperamos el ID del hábito desde el intent
        habitoId = intent.getStringExtra(EXTRA_HABIT_ID) ?: ""
        // Recuperamos los datos del hábito desde el intent
        val nombreInicial     = intent.getStringExtra(EXTRA_HABIT_NAME)      ?: "Hábito"
        val frecuenciaInicial = intent.getStringExtra(EXTRA_HABIT_FREQUENCY) ?: ""
        rachaActual      = intent.getIntExtra(EXTRA_HABIT_STREAK, 0)
        porcentajeActual = intent.getIntExtra(EXTRA_HABIT_PERCENT, 0)
        // Actualizamos la interfaz de usuario
        binding.tvHabitName.text = nombreInicial
        binding.tvFrequency.text = frecuenciaInicial
        actualizarUIEstadisticas()
        // Ajustamos la barra de progreso
        binding.progressFill.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.progressFill.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    ajustarBarraProgreso(porcentajeActual)
                }
            }
        )

        binding.btnBack.setOnClickListener { finish() }

        binding.btnEdit.setOnClickListener {
            val editIntent = Intent(this, EditHabitActivity::class.java).apply {
                putExtra(EditHabitActivity.EXTRA_HABIT_ID,          habitoId)
                putExtra(EditHabitActivity.EXTRA_HABIT_NAME,        nombreInicial)
                putExtra(EditHabitActivity.EXTRA_HABIT_FREQUENCY,   frecuenciaInicial)
                putExtra(EditHabitActivity.EXTRA_HABIT_CATEGORY_ID,
                    intent.getStringExtra(EXTRA_HABIT_CATEGORY_ID) ?: "")
            }
            startActivity(editIntent)
        }

        binding.btnComplete.setOnClickListener { toggleCompletado() }

        cargarEstadoReal()
    }
    // Cargamos el estado real del hábito
    private fun cargarEstadoReal() {
        if (habitoId.isEmpty()) return

        lifecycleScope.launch {
            val resultadoCompletado = habitRepository.estaCompletadoHoy(habitoId)
            resultadoCompletado.fold(
                onSuccess = { estaCompletado ->
                    completadoHoy = estaCompletado
                    actualizarBotonCompletado()
                },
                onFailure = { }
            )
            refrescarDatosHabito()    // Actualizamos los datos del hábito desde Firestore
            cargarHistorial()         // Cargamos el historial de los últimos 7 días
        }
    }
    // Consultamos Firestore y actualizamos racha, porcentaje y barra de progreso en pantalla
    private suspend fun refrescarDatosHabito() {
        val resultado = habitRepository.obtenerHabito(habitoId)
        resultado.fold(
            onSuccess = { habito ->
                rachaActual      = habito.racha
                porcentajeActual = habito.porcentaje
                actualizarUIEstadisticas()
                ajustarBarraProgreso(porcentajeActual)
            },
            onFailure = { }
        )
    }
    // Cargamos el historial de los últimos 7 días
    private suspend fun cargarHistorial() {
        val resultado = habitRepository.obtenerHistorial7Dias(habitoId)
        resultado.fold(
            onSuccess = { diasReales ->
                val adapter = DayAdapter(diasReales)
                binding.rvWeekDays.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.rvWeekDays.adapter = adapter
            },
            onFailure = { }
        )
    }
    // Ajustamos la barra de progreso
    private fun ajustarBarraProgreso(porcentaje: Int) {
        val contenedor  = binding.progressFill.parent as android.view.View
        val anchoPixels = (contenedor.width * porcentaje / 100f).toInt()
        val params      = binding.progressFill.layoutParams
        params.width    = anchoPixels
        binding.progressFill.layoutParams = params
    }
    // Cambiamos el estado del hábito
    private fun toggleCompletado() {
        if (habitoId.isEmpty()) return

        binding.btnComplete.isEnabled = false

        lifecycleScope.launch {
            if (!completadoHoy) {
                val resultado = habitRepository.completarHabito(habitoId)
                resultado.fold(
                    onSuccess = { fueNuevo ->
                        if (fueNuevo) {
                            completadoHoy = true
                            // Refrescamos desde Firestore para obtener el porcentaje
                            // recalculado en el servidor, no un valor local estimado
                            refrescarDatosHabito()
                        }
                    },
                    onFailure = { }
                )
            } else {
                val resultado = habitRepository.descompletarHabito(habitoId)
                resultado.fold(
                    onSuccess = {
                        completadoHoy = false
                        refrescarDatosHabito()
                    },
                    onFailure = { }
                )
            }

            actualizarBotonCompletado()
            binding.btnComplete.isEnabled = true
            cargarHistorial()
        }
    }
    // Actualizamos la interfaz de usuario
    private fun actualizarUIEstadisticas() {
        binding.tvStreakValue.text       = rachaActual.toString()
        binding.tvCompletionPercent.text = "$porcentajeActual%"
    }
    // Actualizamos el botón de completado
    private fun actualizarBotonCompletado() {
        if (completadoHoy) {
            binding.btnComplete.text = getString(R.string.detail_btn_completed)
            binding.btnComplete.backgroundTintList =
                android.content.res.ColorStateList.valueOf(getColor(R.color.verde_salvia))
        } else {
            binding.btnComplete.text = getString(R.string.detail_btn_complete)
            binding.btnComplete.backgroundTintList =
                android.content.res.ColorStateList.valueOf(getColor(R.color.accent))
        }
    }
}
