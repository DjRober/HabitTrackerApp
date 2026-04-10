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

class ProfileFragment : Fragment() {    // Declaramos el fragmento de perfil
    private var _binding: FragmentProfileBinding? = null    // Declaramos el binding
    private val binding get() = _binding!!    // Declaramos el binding get()
    // Declaramos los repositorios de autenticación y hábitos
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
    // Configuramos la UI con los datos del usuario
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usuario = authRepository.obtenerUsuarioActual()

        if (usuario != null) {
            // Mostramos el correo inmediatamente desde Firebase Auth (disponible sin red)
            binding.tvUserEmail.text = usuario.email ?: ""
            // Obtenemos el nombre desde Firestore a través del repositorio
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
        // Cargamos estadísticas reales
        lifecycleScope.launch {
            val resultado = habitRepository.obtenerEstadisticas()
            resultado.fold(
                onSuccess = { (totalHabitos, completadosHoy, rachaMaxima) ->
                    binding.tvStatHabits.text    = totalHabitos.toString()
                    binding.tvStatStreak.text    = rachaMaxima.toString()
                    binding.tvStatCompleted.text = completadosHoy.toString()
                },
                onFailure = {
                    binding.tvStatHabits.text    = "0"
                    binding.tvStatStreak.text    = "0"
                    binding.tvStatCompleted.text = "0"
                }
            )
        }
        // Configuramos los listeners de los botones de configuración
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
        // Cerramos sesión
        binding.btnLogout.setOnClickListener {
            authRepository.cerrarSesion()
            val intent = Intent(requireContext(), InicioSesionInterfaz::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    // Limpiamos el binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
