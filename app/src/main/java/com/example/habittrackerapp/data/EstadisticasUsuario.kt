package com.example.habittrackerapp.data

// Declaramos el modelo de datos para las estadísticas del usuario
data class EstadisticasUsuario(
    val totalHabitos: Int,
    val completadosHoy: Int,
    val rachaMaxima: Int,
    val totalCompletaciones: Int
)
