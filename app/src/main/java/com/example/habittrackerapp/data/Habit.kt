package com.example.habittrackerapp.data
// Con 'Habit.kt' agregamos el campo de IDs de Firestore
data class Habit(
    val id: String = "",           // ID del documento en Firestore
    val nombre: String = "",
    val frecuencia: String = "",
    val diasSemana: List<String> = emptyList(),
    val racha: Int = 0,
    val porcentaje: Int = 0,
    val uid: String = "",
    // Campos para la UI local, no se guardan en Firestore
    val weekDays: List<DayStatus> = emptyList(),
    val iconRes: Int = 0
)

data class DayStatus(
    val label: String,
    val dayNumber: Int,
    val status: DayState
)

enum class DayState {
    COMPLETED,
    TODAY,
    MISSED,
    NOT_APPLICABLE
}
