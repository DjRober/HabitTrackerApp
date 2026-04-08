package com.example.habittrackerapp.ui

import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittrackerapp.R
import com.example.habittrackerapp.data.DayState
import com.example.habittrackerapp.data.DayStatus
import com.example.habittrackerapp.databinding.ActivityHabitDetailBinding

class HabitDetailActivity : AppCompatActivity() {

    // Creamos la variable de enlace binding
    private lateinit var binding: ActivityHabitDetailBinding

    // Controlamos si el hábito ya fue completado hoy
    private var completadoHoy = false

    companion object {
        // Declaramos las claves para los extras del Intent
        const val EXTRA_HABIT_NAME       = "habit_name"
        const val EXTRA_HABIT_FREQUENCY  = "habit_frequency"
        const val EXTRA_HABIT_STREAK     = "habit_streak"
        const val EXTRA_HABIT_PERCENT    = "habit_percent"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Recibimos los datos del hábito desde el Intent
        val nombre     = intent.getStringExtra(EXTRA_HABIT_NAME)      ?: "Hábito"
        val frecuencia = intent.getStringExtra(EXTRA_HABIT_FREQUENCY) ?: ""
        val racha      = intent.getIntExtra(EXTRA_HABIT_STREAK, 0)
        val porcentaje = intent.getIntExtra(EXTRA_HABIT_PERCENT, 0)
        // Poblamos la UI con los datos recibidos
        binding.tvHabitName.text        = nombre
        binding.tvFrequency.text        = frecuencia
        binding.tvStreakValue.text      = racha.toString()
        binding.tvCompletionPercent.text = "$porcentaje%"
        // Configuramos la barra de progreso una vez que el layout esté listo
        binding.progressFill.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.progressFill.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    ajustarBarraProgreso(porcentaje)
                }
            }
        )
        // Configuramos el historial de la semana
        configurarSemana()
        // Creamos el evento del botón de completar hábito
        binding.btnComplete.setOnClickListener {
            toggleCompletado()
        }
        // Creamos el evento del botón de regreso
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
    // Ajustamos el ancho del relleno de la barra según el porcentaje
    private fun ajustarBarraProgreso(porcentaje: Int) {
        val anchoTotal  = binding.progressFill.parent as android.view.View
        val anchoPixels = (anchoTotal.width * porcentaje / 100f).toInt()
        val params      = binding.progressFill.layoutParams
        params.width    = anchoPixels
        binding.progressFill.layoutParams = params
    }
    // Configuramos el RecyclerView con los últimos 7 días
    private fun configurarSemana() {
        val days      = getLast7Days()
        val adapter   = DayAdapter(days)
        binding.rvWeekDays.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvWeekDays.adapter = adapter
    }
    // Generamos los últimos 7 días dinámicamente
    private fun getLast7Days(): List<DayStatus> {
        val days      = mutableListOf<DayStatus>()
        val dayLabels = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

        for (i in 6 downTo 0) {
            val cal       = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val label     = dayLabels[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1]
            val dayNumber = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val status    = if (i == 0) DayState.TODAY else DayState.MISSED
            days.add(DayStatus(label, dayNumber, status))
        }
        return days
    }
    // Alternamos el estado visual del botón al completar el hábito
    private fun toggleCompletado() {
        completadoHoy = !completadoHoy

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
