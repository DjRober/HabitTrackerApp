package com.example.habittrackerapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.habittrackerapp.data.FirebaseAuthRepository
import com.example.habittrackerapp.data.HabitRepository
import com.example.habittrackerapp.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {    // Declaramos el fragmento ProfileFragment
    // Declaramos las variables de enlace de vista
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    // Declaramos los repositorios de autenticación y de hábitos
    private val authRepository  = FirebaseAuthRepository()
    private val habitRepository = HabitRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    // Configuramos el comportamiento de la interfaz de usuario
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usuario = authRepository.obtenerUsuarioActual()

        if (usuario != null) {
            binding.tvUserEmail.text = usuario.email ?: ""

            lifecycleScope.launch {
                val resultado = authRepository.obtenerNombreUsuario()
                resultado.fold(
                    onSuccess = { nombre ->
                        binding.tvUserName.text = nombre
                        binding.tvAvatar.text   = nombre.firstOrNull()?.uppercase() ?: "U"
                    },
                    onFailure = {
                        binding.tvUserName.text = "Usuario"
                        binding.tvAvatar.text   = "U"
                    }
                )
            }
        }
        // Cargamos las estadísticas del usuario
        lifecycleScope.launch {
            val resultado = habitRepository.obtenerEstadisticas()
            resultado.fold(
                onSuccess = { stats ->
                    binding.tvStatHabits.text    = stats.totalHabitos.toString()
                    binding.tvStatStreak.text    = stats.rachaMaxima.toString()
                    // Ahora sí muestra el total histórico de completaciones
                    binding.tvStatCompleted.text = stats.totalCompletaciones.toString()
                },
                onFailure = {
                    binding.tvStatHabits.text    = "0"
                    binding.tvStatStreak.text    = "0"
                    binding.tvStatCompleted.text = "0"
                }
            )
        }
        // Configuramos los listeners de los botones
        binding.optionNotifications.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }
        binding.optionTheme.setOnClickListener {
            startActivity(Intent(requireContext(), AppearanceActivity::class.java))
        }
        binding.optionPrivacy.setOnClickListener {
            startActivity(Intent(requireContext(), PrivacyActivity::class.java))
        }
        binding.optionAbout.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
        // Cerramos la sesión (Usuario)
        binding.btnLogout.setOnClickListener {
            authRepository.cerrarSesion()
            val intent = Intent(requireContext(), InicioSesionInterfaz::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    // Limpiamos el enlace de vista
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
