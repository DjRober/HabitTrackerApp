package com.example.habittrackerapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.habittrackerapp.R
import com.example.habittrackerapp.data.HabitRepository
import com.example.habittrackerapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeFragment : Fragment() {    // Declaramos el fragmento HomeFragment
    // Declaramos las variables de enlace de vista
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    // Declaramos el repositorio de hábitos
    private val habitRepository = HabitRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    // Configuramos el comportamiento de la interfaz de usuario
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarSaludo()
        configurarFecha()
        cargarEstadisticas()
    }
    // Cargamos las estadísticas al volver a la pantalla
    override fun onResume() {
        super.onResume()
        cargarEstadisticas()
    }
    // Configuramos el saludo según la hora del día
    private fun configurarSaludo() {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hora < 12 -> getString(R.string.home_greeting_morning)
            hora < 18 -> getString(R.string.home_greeting_afternoon)
            else      -> getString(R.string.home_greeting_evening)
        }
    }
    // Configuramos la fecha actual
    private fun configurarFecha() {
        val cal       = Calendar.getInstance()
        val diasSemana = listOf(
            "Domingo", "Lunes", "Martes", "Miércoles",
            "Jueves", "Viernes", "Sábado"
        )
        val meses = listOf(
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
        )
        val diaSemana = diasSemana[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val dia       = cal.get(Calendar.DAY_OF_MONTH)
        val mes       = meses[cal.get(Calendar.MONTH)]
        binding.tvDate.text = "$diaSemana, $dia de $mes"
    }
    // Cargamos las estadísticas del usuario
    private fun cargarEstadisticas() {
        lifecycleScope.launch {
            val resultado = habitRepository.obtenerEstadisticas()
            resultado.fold(
                onSuccess = { stats ->
                    binding.tvStatActivos.text     = stats.totalHabitos.toString()
                    binding.tvStatCompletados.text = stats.completadosHoy.toString()
                    binding.tvStatRacha.text       = stats.rachaMaxima.toString()
                },
                onFailure = {
                    binding.tvStatActivos.text     = "0"
                    binding.tvStatCompletados.text = "0"
                    binding.tvStatRacha.text       = "0"
                }
            )
        }
    }
    // Limpiamos el enlace de vista
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
