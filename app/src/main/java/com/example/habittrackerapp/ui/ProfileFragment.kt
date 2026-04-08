package com.example.habittrackerapp.ui

import android.content.Intent
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
        // Configuramos la inicial del avatar con la primera letra del nombre
        // Nota: cuando llegue Firebase esto se reemplaza con el nombre real del usuario
        val nombre = binding.tvUserName.text.toString()
        binding.tvAvatar.text = nombre.firstOrNull()?.uppercase() ?: "U"
        // Opciones de configuración — pendientes de implementar
        binding.optionNotifications.setOnClickListener { /* Nota: pendiente */ }
        binding.optionTheme.setOnClickListener         { /* Nota: pendiente */ }
        binding.optionPrivacy.setOnClickListener       { /* Nota: pendiente */ }
        binding.optionAbout.setOnClickListener         { /* Nota: pendiente */ }
        // Si cerramos sesión regresamos al login y limpiamos el back stack
        binding.btnLogout.setOnClickListener {
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
