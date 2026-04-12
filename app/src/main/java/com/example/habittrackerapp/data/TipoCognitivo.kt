package com.example.habittrackerapp.data

object TipoCognitivo {
    const val FISICO    = "Físico"
    const val MENTAL    = "Mental"
    const val SOCIAL    = "Social"
    const val CREATIVO  = "Creativo"
    const val DESCANSO  = "Descanso"

    val todos = listOf(FISICO, MENTAL, SOCIAL, CREATIVO, DESCANSO)

    // Colores representativos para cada tipo (hex strings)
    fun color(tipo: String): String = when (tipo) {
        FISICO   -> "#E07B5A"   // terracota cálido
        MENTAL   -> "#7B9FD4"   // azul serenidad
        SOCIAL   -> "#F0B86E"   // ámbar social
        CREATIVO -> "#A78BCA"   // violeta creativo
        DESCANSO -> "#78B89A"   // verde salvia
        else     -> "#C8614A"
    }

    // Emoji para cada tipo
    fun emoji(tipo: String): String = when (tipo) {
        FISICO   -> " "
        MENTAL   -> " "
        SOCIAL   -> " "
        CREATIVO -> " "
        DESCANSO -> " "
        else     -> " "
    }
}
