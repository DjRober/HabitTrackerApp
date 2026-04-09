package com.example.habittrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittrackerapp.data.DayState
import com.example.habittrackerapp.data.DayStatus
import com.example.habittrackerapp.data.Habit
import com.example.habittrackerapp.data.HabitRepository
import com.example.habittrackerapp.databinding.FragmentHabitsBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class HabitsFragment : Fragment() {
    // Creamos el binding, se anula en 'onDestroyView' para evitar memory leaks
    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    // Declaramos el repositorio de hábitos
    private val habitRepository = HabitRepository()
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
        // Configuramos el 'RecyclerView' vacío inicialmente
        binding.rvHabits.layoutManager = LinearLayoutManager(requireContext())
        // El 'FAB' abre la pantalla de crear hábito
        binding.fab.setOnClickListener {
            startActivity(Intent(requireContext(), CreateHabitActivity::class.java))
        }
        cargarHabitos()    // Cargamos los hábitos desde Firestore
    }
    override fun onResume() {   // Actualizamos, por si Usuario crea nuevo hábito
        super.onResume()
        cargarHabitos()
    }
    // Cargamos los hábitos del usuario desde Firestore
    private fun cargarHabitos() {
        mostrarCarga(true)

        lifecycleScope.launch {
            val resultado = habitRepository.obtenerHabitos()

            resultado.fold(
                onSuccess = { habitos ->
                    mostrarCarga(false)

                    if (habitos.isEmpty()) {
                        mostrarEstadoVacio(true)
                    } else {
                        mostrarEstadoVacio(false)
                        // Convertimos los hábitos de Firestore al modelo de UI
                        val habitsConDias = habitos.map { habit ->
                            habit.copy(weekDays = getLast7Days())
                        }
                        val adapter = HabitAdapter(habitsConDias) { habit ->
                            // Al tocar un hábito abrimos su detalle
                            val intent = Intent(
                                requireContext(),
                                HabitDetailActivity::class.java
                            ).apply {
                                putExtra(HabitDetailActivity.EXTRA_HABIT_ID,        habit.id)
                                putExtra(HabitDetailActivity.EXTRA_HABIT_NAME,      habit.nombre)
                                putExtra(HabitDetailActivity.EXTRA_HABIT_FREQUENCY, habit.frecuencia)
                                putExtra(HabitDetailActivity.EXTRA_HABIT_STREAK,    habit.racha)
                                putExtra(HabitDetailActivity.EXTRA_HABIT_PERCENT,   habit.porcentaje)
                            }
                            startActivity(intent)
                        }
                        binding.rvHabits.adapter = adapter
                    }
                },
                onFailure = {
                    mostrarCarga(false)
                    mostrarEstadoVacio(true)
                }
            )
        }
    }
    // Mostramos u ocultamos el indicador de carga
    private fun mostrarCarga(mostrar: Boolean) {
        binding.rvHabits.visibility =
            if (mostrar) View.GONE else View.VISIBLE
    }
    // Mostramos u ocultamos el estado vacío
    private fun mostrarEstadoVacio(mostrar: Boolean) {
        binding.tvEmptyState.visibility =
            if (mostrar) View.VISIBLE else View.GONE
        binding.rvHabits.visibility =
            if (mostrar) View.GONE else View.VISIBLE
    }
    // Generamos los últimos 7 días dinámicamente para la UI
    private fun getLast7Days(): List<DayStatus> {
        val days      = mutableListOf<DayStatus>()
        val dayLabels = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

        for (i in 6 downTo 0) {
            val cal       = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val label     = dayLabels[cal.get(Calendar.DAY_OF_WEEK) - 1]
            val dayNumber = cal.get(Calendar.DAY_OF_MONTH)
            val status    = if (i == 0) DayState.TODAY else DayState.MISSED
            days.add(DayStatus(label, dayNumber, status))
        }
        return days
    }
    // Liberamos el binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
