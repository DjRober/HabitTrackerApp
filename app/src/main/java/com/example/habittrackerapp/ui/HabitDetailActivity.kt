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

class HabitDetailActivity : AppCompatActivity() {    // Declaramos el fragmento de detalle de hábitos
    // Declaramos los repositorios de hábitos
    private lateinit var binding: ActivityHabitDetailBinding
    private val habitRepository = HabitRepository()
    // Declaramos las variables de estado
    private var habitoId      = ""
    private var completadoHoy = false
    private var rachaActual   = 0
    private var porcentajeActual = 0
    // Declaramos las constantes de Intent
    companion object {
        const val EXTRA_HABIT_ID          = "habit_id"
        const val EXTRA_HABIT_NAME        = "habit_name"
        const val EXTRA_HABIT_FREQUENCY   = "habit_frequency"
        const val EXTRA_HABIT_STREAK      = "habit_streak"
        const val EXTRA_HABIT_PERCENT     = "habit_percent"
        const val EXTRA_HABIT_CATEGORY_ID = "habit_category_id"
    }
    // Configuramos la UI con los datos del hábito
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtenemos los datos del Intent
        habitoId = intent.getStringExtra(EXTRA_HABIT_ID) ?: ""
        // Mostramos datos del Intent de forma inmediata para que la pantalla no quede en blanco
        val nombreInicial     = intent.getStringExtra(EXTRA_HABIT_NAME)    ?: "Hábito"
        val frecuenciaInicial = intent.getStringExtra(EXTRA_HABIT_FREQUENCY) ?: ""
        rachaActual      = intent.getIntExtra(EXTRA_HABIT_STREAK, 0)
        porcentajeActual = intent.getIntExtra(EXTRA_HABIT_PERCENT, 0)
        // Actualizamos la UI con los datos del Intent
        binding.tvHabitName.text = nombreInicial
        binding.tvFrequency.text = frecuenciaInicial
        actualizarUIEstadisticas()
        // Ajustamos el tamaño de la barra de progreso
        binding.progressFill.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.progressFill.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    ajustarBarraProgreso(porcentajeActual)
                }
            }
        )
        // Declaramos el evento que regresa a la pantalla anterior
        binding.btnBack.setOnClickListener { finish() }
        // Declaramos el evento que edita el hábito
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
        // Cargamos el estado real desde Firestore -> si ya fue completado hoy e historial de 7 días
        cargarEstadoReal()
    }
    // Consulta Firestore para obtener el estado actual del hábito y el historial de la semana
    private fun cargarEstadoReal() {
        if (habitoId.isEmpty()) return

        lifecycleScope.launch {
            // Verificamos si ya está completado hoy
            val resultadoCompletado = habitRepository.estaCompletadoHoy(habitoId)
            resultadoCompletado.fold(
                onSuccess = { estaCompletado ->
                    completadoHoy = estaCompletado
                    actualizarBotonCompletado()
                },
                onFailure = { /* Mantenemos completadoHoy = false como estado por defecto */ }
            )
            // Obtenemos datos frescos del hábito para racha y porcentaje actualizados
            val resultadoHabito = habitRepository.obtenerHabito(habitoId)
            resultadoHabito.fold(
                onSuccess = { habito ->
                    rachaActual      = habito.racha
                    porcentajeActual = habito.porcentaje
                    actualizarUIEstadisticas()
                    ajustarBarraProgreso(porcentajeActual)
                },
                onFailure = { /* Mantenemos los valores del Intent */ }
            )
            // Cargamos el historial real de 7 días
            cargarHistorial()
        }
    }
    // Obtenemos el historial real de la subcolección y actualiza el RecyclerView
    private suspend fun cargarHistorial() {
        val resultado = habitRepository.obtenerHistorial7Dias(habitoId)
        resultado.fold(
            onSuccess = { diasReales ->
                val adapter = com.example.habittrackerapp.ui.DayAdapter(diasReales)
                binding.rvWeekDays.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.rvWeekDays.adapter = adapter
            },
            onFailure = { /* El RecyclerView queda vacío si falla la carga */ }
        )
    }
    // Ajustamos el tamaño de la barra de progreso según el porcentaje
    private fun ajustarBarraProgreso(porcentaje: Int) {
        val contenedor  = binding.progressFill.parent as android.view.View
        val anchoPixels = (contenedor.width * porcentaje / 100f).toInt()
        val params      = binding.progressFill.layoutParams
        params.width    = anchoPixels
        binding.progressFill.layoutParams = params
    }
    // Usamos el resultado de completarHabito para saber si fue un cambio real o ya estaba completado
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
                            rachaActual = rachaActual + 1
                            porcentajeActual = minOf(porcentajeActual + 5, 100)
                            actualizarUIEstadisticas()
                            ajustarBarraProgreso(porcentajeActual)
                        }
                        // Si fueNuevo == false, ya estaba completado; no modificamos estado local
                    },
                    onFailure = { /* El botón recupera su estado sin cambios */ }
                )
            } else {
                val resultado = habitRepository.descompletarHabito(habitoId)
                resultado.fold(
                    onSuccess = {
                        completadoHoy = false
                        rachaActual = maxOf(rachaActual - 1, 0)
                        porcentajeActual = maxOf(porcentajeActual - 5, 0)
                        actualizarUIEstadisticas()
                        ajustarBarraProgreso(porcentajeActual)
                    },
                    onFailure = { /* El botón recupera su estado sin cambios */ }
                )
            }
            // Actualizamos el botón
            actualizarBotonCompletado()
            binding.btnComplete.isEnabled = true
            // Refrescamos el historial visual después de cada cambio
            cargarHistorial()
        }
    }
    // Actualizamos los TextView de racha y porcentaje con los valores actuales en memoria
    private fun actualizarUIEstadisticas() {
        binding.tvStreakValue.text       = rachaActual.toString()
        binding.tvCompletionPercent.text = "$porcentajeActual%"
    }
    // Actualizamos el texto y color del botón según el estado de completación
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
