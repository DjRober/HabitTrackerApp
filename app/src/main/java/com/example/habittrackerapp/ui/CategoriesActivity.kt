package com.example.habittrackerapp.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habittrackerapp.R
import com.example.habittrackerapp.databinding.ActivityCategoriesBinding

class CategoriesActivity : AppCompatActivity() {

    // Definimos el binding y lo protegemos (declaramos como privado)
    private lateinit var binding: ActivityCategoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Configuramos 'toolbar'
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Cargamos las categorías
        setupCustomCategories()
        setupDefaultCategories()
        // Declaramos el evento onClick
        binding.btnNewCategory.setOnClickListener {
            // Nota Importante: Nueva categoría — pendiente de implementar
        }
        // Configuramos la navegación inferior
        configurarNavegacion()
    }
    override fun onResume() {
        super.onResume()
        binding.bottomNav.selectedItemId = R.id.nav_categories
    }
    // Declaramos la navegación inferior
    private fun configurarNavegacion() {
        binding.bottomNav.selectedItemId = R.id.nav_categories

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    true
                }
                R.id.nav_habits -> {
                    val intent = Intent(this, HabitsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    true
                }
                R.id.nav_categories -> {
                    true    // Ya estamos aqui
                }
                else -> false
            }
        }
    }
    // Declaramos 'setupCustomCategories' para las categorías personalizadas
    private fun setupCustomCategories() {
        val customCategories = listOf(
            Category("Espiritual", R.drawable.ic_leaf, 2, Color.parseColor("#C8614A"))
        )
        binding.tvCustomSubtitle.text = "${customCategories.size} disponibles"
        val adapter = CategoryAdapter(customCategories) { /* Nota: pendiente */ }
        binding.rvCustomCategories.layoutManager = GridLayoutManager(this, 4)
        binding.rvCustomCategories.adapter = adapter
    }
    // Declaramos 'setupDefaultCategories' para las categorías por defecto
    private fun setupDefaultCategories() {
        val defaultCategories = listOf(
            Category("Dejar un...", R.drawable.ic_leaf, 0, Color.parseColor("#E74C3C")),
            Category("Arte",        R.drawable.ic_leaf, 0, Color.parseColor("#E91E8C")),
            Category("Tarea",       R.drawable.ic_leaf, 0, Color.parseColor("#E91E63")),
            Category("Meditación",  R.drawable.ic_leaf, 1, Color.parseColor("#9B59B6")),
            Category("Estudio",     R.drawable.ic_leaf, 2, Color.parseColor("#673AB7")),
        )
        val adapter = CategoryAdapter(defaultCategories) { /* Nota: pendiente */ }
        binding.rvDefaultCategories.layoutManager = GridLayoutManager(this, 4)
        binding.rvDefaultCategories.adapter = adapter
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
