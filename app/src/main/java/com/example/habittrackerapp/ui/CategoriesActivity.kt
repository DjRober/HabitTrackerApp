package com.example.habittrackerapp.ui

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habittrackerapp.R
import com.example.habittrackerapp.databinding.ActivityCategoriesBinding

class CategoriesActivity : AppCompatActivity() {

    // Binding
    private lateinit var binding: ActivityCategoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuramos toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Cargamos categorías
        setupCustomCategories()
        setupDefaultCategories()

        // Eventos onClick
        binding.btnNewCategory.setOnClickListener {
            println("Nueva categoría clickeado") // Debugeo temporal
        }
    }

    // Funciones:
    // Categorías personalizadas:
    private fun setupCustomCategories() {
        val customCategories = listOf(
            Category("Espiritual", R.drawable.ic_leaf, 2, Color.parseColor("#C8A84A"))
        )

        binding.tvCustomSubtitle.text = "${customCategories.size} disponibles"

        val adapter = CategoryAdapter(customCategories) { category ->
            println("Categoría clickeada: ${category.name}") // Debugeo temporal
        }

        binding.rvCustomCategories.layoutManager = GridLayoutManager(this, 4)
        binding.rvCustomCategories.adapter = adapter
    }

    // Categorías por defecto:
    private fun setupDefaultCategories() {
        val defaultCategories = listOf(
            Category("Dejar un...", R.drawable.ic_leaf, 0, Color.parseColor("#E74C3C")),
            Category("Arte", R.drawable.ic_leaf, 0, Color.parseColor("#E91E8C")),
            Category("Tarea", R.drawable.ic_leaf, 0, Color.parseColor("#E91E63")),
            Category("Meditación", R.drawable.ic_leaf, 1, Color.parseColor("#9B59B6")),
            Category("Estudio", R.drawable.ic_leaf, 2, Color.parseColor("#673AB7")),
        )

        val adapter = CategoryAdapter(defaultCategories) { category ->
            println("Categoría clickeada: ${category.name}") // Debugeo temporal
        }

        binding.rvDefaultCategories.layoutManager = GridLayoutManager(this, 4)
        binding.rvDefaultCategories.adapter = adapter
    }

    // Regresa a la pantalla anterior
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}