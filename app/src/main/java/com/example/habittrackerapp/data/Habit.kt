package com.example.habittrackerapp.data

data class Habit(      // Declaramos el modelo de datos de hábito
    val id: String = "",
    val nombre: String = "",
    val frecuencia: String = "",
    val diasSemana: List<String> = emptyList(),
    val racha: Int = 0,
    val porcentaje: Int = 0,
    val uid: String = "",
    val categoriaId: String = "",
    val categoriaNombre: String = "",
    val categoriaColor: String = "",
    // Campos calculados en cliente, no persisten en Firestore
    val weekDays: List<DayStatus> = emptyList(),
    val iconRes: Int = 0,
    val estaCompletadoHoy: Boolean = false
)

data class DayStatus(    // Declaramos el modelo de datos de día
    val label: String,
    val dayNumber: Int,
    val status: DayState
)

enum class DayState {    // Declaramos el estado de un día
    COMPLETED,
    TODAY,
    MISSED,
    NOT_APPLICABLE
}
