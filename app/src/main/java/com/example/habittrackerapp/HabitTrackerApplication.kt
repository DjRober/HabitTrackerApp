package com.example.habittrackerapp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.habittrackerapp.ui.AppearanceActivity

class HabitTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences(AppearanceActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val temaGuardado = prefs.getString(AppearanceActivity.KEY_THEME, AppearanceActivity.THEME_SYSTEM)

        val modo = when (temaGuardado) {
            AppearanceActivity.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            AppearanceActivity.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(modo)
    }
}