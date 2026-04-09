package com.example.habittrackerapp.ui

import androidx.lifecycle.lifecycleScope
import com.example.habittrackerapp.data.HabitRepository
import kotlinx.coroutines.launch
import android.content.Intent
import com.example.habittrackerapp.data.FirebaseAuthRepository
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.habittrackerapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    // Creamos el binding, se anula en 'onDestroyView' para evitar memory leaks
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Cargamos los datos reales del usuario desde Firebase Auth
        val authRepository = FirebaseAuthRepository()
        val usuario        = authRepository.obtenerUsuarioActual()

        if (usuario != null) {
            binding.tvUserEmail.text = usuario.email ?: ""    // Mostramos el email real del usuario
            // Buscamos el nombre en Firestore
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("usuarios")
                .document(usuario.uid)
                .get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: "Usuario"
                    binding.tvUserName.text = nombre
                    binding.tvAvatar.text   = nombre.firstOrNull()?.uppercase() ?: "U"
                }
        }
        // Cargamos las estadísticas reales del usuario
        val habitRepo = HabitRepository()
        lifecycleScope.launch {
            val resultado = habitRepo.obtenerEstadisticas()
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
        // Opciones de configuración, pendientes de implementar
        binding.optionNotifications.setOnClickListener { }
        binding.optionTheme.setOnClickListener         { }
        binding.optionPrivacy.setOnClickListener       { }
        binding.optionAbout.setOnClickListener         { }
        // Cerrar sesión real con Firebase Auth
        binding.btnLogout.setOnClickListener {
            val authRepo = FirebaseAuthRepository()
            authRepo.cerrarSesion()

            val intent = Intent(requireContext(), InicioSesionInterfaz::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    // Liberamos el binding al destruir la vista del Fragment
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
