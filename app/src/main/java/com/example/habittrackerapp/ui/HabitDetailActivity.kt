package com.example.habittrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittrackerapp.R
import com.example.habittrackerapp.data.DayState
import com.example.habittrackerapp.data.DayStatus
import com.example.habittrackerapp.data.HabitRepository
import com.example.habittrackerapp.databinding.ActivityHabitDetailBinding
import kotlinx.coroutines.launch

// Creamos la clase para mostrar el detalle del hábito
class HabitDetailActivity : AppCompatActivity() {
    // Creamos el binding
    private lateinit var binding: ActivityHabitDetailBinding
    // Declaramos el repositorio de hábitos
    private val habitRepository = HabitRepository()
    //  Declaramos el estado local del hábito en esta sesión
    private var completadoHoy = false
    private var habitoId      = ""
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
        // Recibimos los datos del hábito desde el Intent
        habitoId           = intent.getStringExtra(EXTRA_HABIT_ID)        ?: ""
        val nombre         = intent.getStringExtra(EXTRA_HABIT_NAME)      ?: "Hábito"
        val frecuencia     = intent.getStringExtra(EXTRA_HABIT_FREQUENCY) ?: ""
        val racha          = intent.getIntExtra(EXTRA_HABIT_STREAK, 0)
        val porcentaje     = intent.getIntExtra(EXTRA_HABIT_PERCENT, 0)
        // Poblamos la UI con los datos recibidos
        binding.tvHabitName.text         = nombre
        binding.tvFrequency.text         = frecuencia
        binding.tvStreakValue.text        = racha.toString()
        binding.tvCompletionPercent.text = "$porcentaje%"
        // Ajustamos la barra de progreso una vez que el layout esté listo
        binding.progressFill.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.progressFill.viewTreeObserver
                        .removeOnGlobalLayoutListener(this)
                    ajustarBarraProgreso(porcentaje)
                }
            }
        )
        configurarSemana()    // Configuramos el historial de la semana
        binding.btnComplete.setOnClickListener {    // Botón de completar hábito, esta conectado a Firestore
            toggleCompletado()
        }
        binding.btnBack.setOnClickListener {    // Declaramos el evento del botón de regreso
            finish()
        }
        // Abre la pantalla de edición con los datos actuales
        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditHabitActivity::class.java).apply {
                putExtra(EditHabitActivity.EXTRA_HABIT_ID,          habitoId)
                putExtra(EditHabitActivity.EXTRA_HABIT_NAME,        intent.getStringExtra(EXTRA_HABIT_NAME) ?: "")
                putExtra(EditHabitActivity.EXTRA_HABIT_FREQUENCY,   intent.getStringExtra(EXTRA_HABIT_FREQUENCY) ?: "")
                putExtra(EditHabitActivity.EXTRA_HABIT_CATEGORY_ID, intent.getStringExtra(EXTRA_HABIT_CATEGORY_ID) ?: "")
            }
            startActivity(intent)
        }
    }
    // Ajustamos el ancho del relleno de la barra según el porcentaje
    private fun ajustarBarraProgreso(porcentaje: Int) {
        val contenedor  = binding.progressFill.parent as android.view.View
        val anchoPixels = (contenedor.width * porcentaje / 100f).toInt()
        val params      = binding.progressFill.layoutParams
        params.width    = anchoPixels
        binding.progressFill.layoutParams = params
    }
    // Configuramos el 'RecyclerView' con los últimos 7 días
    private fun configurarSemana() {
        val adapter = DayAdapter(getLast7Days())
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
    // Alternamos el estado del hábito y actualizamos Firestore
    private fun toggleCompletado() {
        if (habitoId.isEmpty()) return
        completadoHoy = !completadoHoy
        // Actualizamos la UI inmediatamente para dar feedback instantáneo
        actualizarBotonCompletado()
        // Actualizamos Firestore en segundo plano
        lifecycleScope.launch {
            if (completadoHoy) {
                habitRepository.completarHabito(habitoId)
            } else {
                habitRepository.descompletarHabito(habitoId)
            }
        }
    }
    // Actualizamos el aspecto visual del botón según el estado
    private fun actualizarBotonCompletado() {
        if (completadoHoy) {
            binding.btnComplete.text = getString(R.string.detail_btn_completed)
            binding.btnComplete.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    getColor(R.color.verde_salvia)
                )
        } else {
            binding.btnComplete.text = getString(R.string.detail_btn_complete)
            binding.btnComplete.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    getColor(R.color.accent)
                )
        }
    }
}
