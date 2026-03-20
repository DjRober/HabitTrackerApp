package com.example.habittrackerapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.habittrackerapp.databinding.ActivityHomeBinding
import java.util.Calendar
import com.example.habittrackerapp.R

class HomeActivity : AppCompatActivity() {

    // Definimos el binding y lo protegemos (declaramos como privado)
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Configuramos el saludo y la fecha (de forma dinámica)
        configurarSaludo()
        configurarFecha()
        configurarNavegacion()    // Configuramos la navegación inferior
    }
    override fun onResume() {
        super.onResume()
        binding.bottomNav.selectedItemId = R.id.nav_home
    }
    // 'configurarSaludo' saluda según la hora del día (dinámico)
    private fun configurarSaludo() {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hora < 12 -> getString(com.example.habittrackerapp.R.string.home_greeting_morning)
            hora < 18 -> getString(com.example.habittrackerapp.R.string.home_greeting_afternoon)
            else      -> getString(com.example.habittrackerapp.R.string.home_greeting_evening)
        }
    }
    // 'configurarFecha' muestra la fecha actual (dinámico)
    private fun configurarFecha() {
        val cal = Calendar.getInstance()
        val diasSemana = listOf("Domingo", "Lunes", "Martes", "Miércoles",
            "Jueves", "Viernes", "Sábado")
        val meses = listOf("enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")

        val diaSemana = diasSemana[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val dia       = cal.get(Calendar.DAY_OF_MONTH)
        val mes       = meses[cal.get(Calendar.MONTH)]

        binding.tvDate.text = "$diaSemana, $dia de $mes"
    }
    // Declaramos la navegación inferior
    private fun configurarNavegacion() {
        binding.bottomNav.selectedItemId = com.example.habittrackerapp.R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.habittrackerapp.R.id.nav_home -> {
                    true    // Ya estamos aquí, no hacemos nada
                }
                com.example.habittrackerapp.R.id.nav_habits -> {
                    // Abre HabitsActivity; si ya existe la traemos al frente
                    val intent = Intent(this, HabitsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    true
                }
                com.example.habittrackerapp.R.id.nav_categories -> {
                    val intent = Intent(this, CategoriesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
