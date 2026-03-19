package com.example.habittrackerapp.data

data class Habit(
    val id: Int,
    val name: String,
    val frequency: String,
    val iconRes: Int,
    val weekDays: List<DayStatus>,
    val streak: Int,
    val completionPercent: Int
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