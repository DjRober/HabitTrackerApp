package com.example.habittrackerapp.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habittrackerapp.R
import com.example.habittrackerapp.databinding.FragmentCategoriesBinding

class CategoriesFragment : Fragment() {

    // Declaramos el binding (privado), se anula en 'onDestroyView' para evitar memory leaks
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Mandamos a llamar para crear las nuevas categorías
        setupCustomCategories()
        setupDefaultCategories()
        // Dejamos preparado el botón para su implementación futura
        binding.btnNewCategory.setOnClickListener {
            // Nota: nueva categoría — pendiente de implementar
        }
    }
    // Declaramos las categorías personalizadas (privada)
    private fun setupCustomCategories() {
        val customCategories = listOf(
            Category("Espiritual", R.drawable.ic_leaf, 2, Color.parseColor("#C8614A"))
        )
        binding.tvCustomSubtitle.text = "${customCategories.size} disponibles"
        val adapter = CategoryAdapter(customCategories) { /* Nota: pendiente */ }
        binding.rvCustomCategories.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvCustomCategories.adapter = adapter
    }
    // Declaramos las categorías por defecto
    private fun setupDefaultCategories() {
        val defaultCategories = listOf(
            Category("Dejar un...", R.drawable.ic_leaf, 0, Color.parseColor("#E74C3C")),
            Category("Arte",        R.drawable.ic_leaf, 0, Color.parseColor("#E91E8C")),
            Category("Tarea",       R.drawable.ic_leaf, 0, Color.parseColor("#E91E63")),
            Category("Meditación",  R.drawable.ic_leaf, 1, Color.parseColor("#9B59B6")),
            Category("Estudio",     R.drawable.ic_leaf, 2, Color.parseColor("#673AB7")),
        )
        val adapter = CategoryAdapter(defaultCategories) { /* Nota: pendiente */ }
        binding.rvDefaultCategories.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvDefaultCategories.adapter = adapter
    }
    // Liberamos el binding al destruir la vista del Fragment
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
