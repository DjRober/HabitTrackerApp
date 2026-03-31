package com.example.habittrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittrackerapp.data.DayState
import com.example.habittrackerapp.data.DayStatus
import com.example.habittrackerapp.data.Habit
import com.example.habittrackerapp.databinding.FragmentHabitsBinding

class HabitsFragment : Fragment() {

    // Declaramos el binding (privado), se anula en 'onDestroyView' para evitar memory leaks
    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Cargamos los hábitos de prueba y configuramos el 'RecyclerView'
        val habits = getSampleHabits()
        val adapter = HabitAdapter(habits) { habit ->
            // Nota: detalle del hábito — pendientes de implementar
        }
        binding.rvHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHabits.adapter = adapter
        // El FAB abre la pantalla de crear nuevo hábito
        binding.fab.setOnClickListener {
            startActivity(Intent(requireContext(), CreateHabitActivity::class.java))
        }
    }
    // Generamos los últimos siete días de forma dinámica (posible implementacion con Firebase)
    private fun getLast7Days(): List<DayStatus> {
        val days = mutableListOf<DayStatus>()
        val dayLabels = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

        for (i in 6 downTo 0) {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val label     = dayLabels[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1]
            val dayNumber = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val status    = if (i == 0) DayState.TODAY else DayState.MISSED
            days.add(DayStatus(label, dayNumber, status))
        }
        return days
    }
    // Datos de prueba (se reemplazarán con Firebase)
    private fun getSampleHabits(): List<Habit> {
        val week = getLast7Days()
        return listOf(
            Habit(1, "Leer las escrituras", "Todos los días | 3P", 0, week, 1, 18),
            Habit(2, "Meditar",             "Todos los días | 3P", 0, week, 0, 5),
            Habit(3, "Entrenar",            "Lun - Mar - Mié - Jue - Vie", 0, week, 0, 9)
        )
    }
    // Liberamos el binding al destruir la vista del fragment
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
