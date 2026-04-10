package com.example.habittrackerapp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class HabitusApp : Application() {

    override fun onCreate() {
        super.onCreate()
        restaurarTema()    // Restauramos el tema guardado antes de que se infle cualquier vista
    }
    private fun restaurarTema() {
        val prefs        = getSharedPreferences("habitus_prefs", Context.MODE_PRIVATE)
        val temaGuardado = prefs.getString("app_theme", "light") ?: "light"

        val modo = when (temaGuardado) {
            "dark"   -> AppCompatDelegate.MODE_NIGHT_YES
            "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else     -> AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(modo)
    }
}
