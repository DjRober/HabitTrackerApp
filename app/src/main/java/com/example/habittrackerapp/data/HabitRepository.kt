package com.example.habittrackerapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class HabitRepository {
    // Declaramos instancias de Firebase, son singletons, no se crean múltiples veces
    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    // Referenciamos a la colección de hábitos del usuario actual
    private fun coleccionHabitos() = firestore.collection("habitos")
    // Retornamos el 'UID' del usuario actual o lanza excepción si no hay sesión
    private fun obtenerUid(): String =
        auth.currentUser?.uid ?: throw Exception("No hay sesión activa")
    // Guarda un nuevo hábito en Firestore
    suspend fun guardarHabito(
        nombre: String,
        frecuencia: String,
        diasSemana: List<String>
    ): Result<String> {
        return try {
            val uid  = obtenerUid()
            val dato = mapOf(
                "uid"           to uid,
                "nombre"        to nombre,
                "frecuencia"    to frecuencia,
                "diasSemana"    to diasSemana,
                "racha"         to 0,
                "porcentaje"    to 0,
                "fechaCreacion" to com.google.firebase.Timestamp.now()
            )
            // Hacemos que Firestore genere el ID automáticamente
            val referencia = coleccionHabitos().add(dato).await()
            Result.success(referencia.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Obtenemos todos los hábitos del usuario actual
    suspend fun obtenerHabitos(): Result<List<Habit>> {
        return try {
            val uid      = obtenerUid()
            val snapshot = coleccionHabitos()
                .whereEqualTo("uid", uid)
                .get()
                .await()

            val habitos = snapshot.documents.mapNotNull { doc ->
                Habit(
                    id         = doc.id,
                    nombre     = doc.getString("nombre")     ?: "",
                    frecuencia = doc.getString("frecuencia") ?: "",
                    diasSemana = (doc.get("diasSemana") as? List<*>)
                        ?.filterIsInstance<String>()
                        ?: emptyList(),
                    racha      = (doc.getLong("racha")      ?: 0L).toInt(),
                    porcentaje = (doc.getLong("porcentaje") ?: 0L).toInt(),
                    uid        = doc.getString("uid")        ?: ""
                )
            }
            // Ordenamos por fecha de creación de forma descendente en el cliente
            val habitosOrdenados = habitos.sortedByDescending { habit ->
                snapshot.documents
                    .find { it.id == habit.id }
                    ?.getTimestamp("fechaCreacion")
                    ?.seconds ?: 0L
            }
            Result.success(habitosOrdenados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Eliminamos un hábito mediante su 'ID'
    suspend fun eliminarHabito(habitoId: String): Result<Unit> {
        return try {
            coleccionHabitos().document(habitoId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Marca un hábito como completado hoy e incrementa su racha
    suspend fun completarHabito(habitoId: String): Result<Unit> {
        return try {
            val referencia = coleccionHabitos().document(habitoId)
            val snapshot   = referencia.get().await()

            val rachaActual    = (snapshot.getLong("racha") ?: 0L).toInt()
            val porcentajeActual = (snapshot.getLong("porcentaje") ?: 0L).toInt()
            val nuevaRacha      = rachaActual + 1    // // Incrementamos la racha y recalculamos el porcentaje
            // El porcentaje sube 5 puntos por cada vez que se completa con tope 100
            val nuevoPorcentaje = minOf(porcentajeActual + 5, 100)

            referencia.update(
                mapOf(
                    "racha"          to nuevaRacha,
                    "porcentaje"     to nuevoPorcentaje,
                    "ultimaCompletacion" to com.google.firebase.Timestamp.now()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Desmarca un hábito completado y restaura la racha anterior
    suspend fun descompletarHabito(habitoId: String): Result<Unit> {
        return try {
            val referencia = coleccionHabitos().document(habitoId)
            val snapshot   = referencia.get().await()

            val rachaActual      = (snapshot.getLong("racha")      ?: 0L).toInt()
            val porcentajeActual = (snapshot.getLong("porcentaje") ?: 0L).toInt()

            val nuevaRacha      = maxOf(rachaActual - 1, 0)
            val nuevoPorcentaje = maxOf(porcentajeActual - 5, 0)

            referencia.update(
                mapOf(
                    "racha"      to nuevaRacha,
                    "porcentaje" to nuevoPorcentaje
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Obtiene las estadísticas del usuario para el 'Home' y el 'Perfil'
    suspend fun obtenerEstadisticas(): Result<Triple<Int, Int, Int>> {
        return try {
            val uid      = obtenerUid()
            val snapshot = coleccionHabitos()
                .whereEqualTo("uid", uid)
                .get()
                .await()

            val totalHabitos   = snapshot.size()
            val rachaMaxima    = snapshot.documents
                .maxOfOrNull { (it.getLong("racha") ?: 0L).toInt() } ?: 0
            val completadosHoy = snapshot.documents.count { doc ->
                val ultimaCompletacion = doc.getTimestamp("ultimaCompletacion")
                if (ultimaCompletacion == null) return@count false
                // Verificamos si fue completado hoy comparando la fecha
                val hoy      = java.util.Calendar.getInstance()
                val fechaDoc = java.util.Calendar.getInstance().apply {
                    time = ultimaCompletacion.toDate()
                }
                hoy.get(java.util.Calendar.DAY_OF_YEAR) == fechaDoc.get(java.util.Calendar.DAY_OF_YEAR) &&
                        hoy.get(java.util.Calendar.YEAR)         == fechaDoc.get(java.util.Calendar.YEAR)
            }
            // Retornamos el total de hábitos, completados hoy y racha máxima
            Result.success(Triple(totalHabitos, completadosHoy, rachaMaxima))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
