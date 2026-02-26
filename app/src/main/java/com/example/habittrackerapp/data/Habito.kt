package com.example.habittrackerapp.data

class Habito (
    val idHabito: Long,
    val nombreHabito: String,
    val descripcionHabito: String?,
    val frecuenciaHabito: String,
    val fechaCreacionHabito: Long,
    val idUsuario: Long,
    val idCategoria: Long
)