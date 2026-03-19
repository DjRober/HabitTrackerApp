package com.example.habittrackerapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittrackerapp.data.DayState
import com.example.habittrackerapp.data.DayStatus
import com.example.habittrackerapp.data.Habit
import com.example.habittrackerapp.databinding.ActivityHabitsBinding

class HabitsActivity : AppCompatActivity() {

    // Binding
    private lateinit var binding: ActivityHabitsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cazamiento
        val rvHabits = binding.rvHabits

        // Cargamos los hábitos de prueba
        val habits = getSampleHabits()

        // Configuramos el adaptador
        val adapter = HabitAdapter(habits) { habit ->
            println("Más opciones: ${habit.name}") // Debugeo temporal
        }

        // Configuramos el RecyclerView
        rvHabits.layoutManager = LinearLayoutManager(this)
        rvHabits.adapter = adapter
    }

    // Funciones:
    // Genera los últimos 7 días dinámicamente:
    private fun getLast7Days(): List<DayStatus> {
        val days = mutableListOf<DayStatus>()
        val dayLabels = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

        for (i in 6 downTo 0) {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val label = dayLabels[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1]
            val dayNumber = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val status = if (i == 0) DayState.TODAY else DayState.MISSED
            days.add(DayStatus(label, dayNumber, status))
        }
        return days
    }
    // Datos de prueba (se reemplazarán con base de datos):
    private fun getSampleHabits(): List<Habit> {
        val week = getLast7Days()
        return listOf(
            Habit(1, "Leer las escrituras", "Todos los días | 3P", 0, week, 1, 18),
            Habit(2, "Meditar", "Todos los días | 3P", 0, week, 0, 5),
            Habit(3, "Entrenar", "Lun - Mar - Mié - Jue - Vie", 0, week, 0, 9)
        )
    }
}