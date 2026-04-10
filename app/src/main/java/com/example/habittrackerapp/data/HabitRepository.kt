package com.example.habittrackerapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitRepository {    // Declaramos el repositorio de hábitos

    private val auth      = FirebaseAuth.getInstance()         // Instanciamos la autentificación de Firebase
    private val firestore = FirebaseFirestore.getInstance()    // Instanciamos Firestore
    // Referenciamos a la colección de hábitos del usuario
    private fun coleccionHabitos() = firestore.collection("habitos")
    private fun obtenerUid(): String =    // Obtenemos el UID del usuario actual
        auth.currentUser?.uid ?: throw Exception("No hay sesión activa")
    // Asignamos un formato de fecha estándar para IDs de completaciones -> "2026-04-10"
    private fun obtenerFechaHoy(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    // Verificamos si una fecha (timestamp) corresponde al día de hoy
    private fun esHoy(fechaTimestamp: com.google.firebase.Timestamp?): Boolean {
        if (fechaTimestamp == null) return false
        val hoy     = Calendar.getInstance()
        val fechaDoc = Calendar.getInstance().apply { time = fechaTimestamp.toDate() }
        return hoy.get(Calendar.DAY_OF_YEAR) == fechaDoc.get(Calendar.DAY_OF_YEAR) &&
                hoy.get(Calendar.YEAR)        == fechaDoc.get(Calendar.YEAR)
    }
    // Guardamos un nuevo hábito en Firestore
    suspend fun guardarHabito(
        nombre: String,
        frecuencia: String,
        diasSemana: List<String>,
        categoriaId: String = ""
    ): Result<String> {
        return try {
            val uid  = obtenerUid()
            val dato = mapOf(
                "uid"            to uid,
                "nombre"         to nombre,
                "frecuencia"     to frecuencia,
                "diasSemana"     to diasSemana,
                "categoriaId"    to categoriaId,
                "racha"          to 0,
                "porcentaje"     to 0,
                "fechaCreacion"  to com.google.firebase.Timestamp.now()
            )
            val referencia = coleccionHabitos().add(dato).await()
            Result.success(referencia.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Obtenemos todos los hábitos del usuario con categorías cruzadas y estado de completación de hoy
    suspend fun obtenerHabitos(): Result<List<Habit>> {
        return try {
            val uid      = obtenerUid()
            val snapshot = coleccionHabitos()
                .whereEqualTo("uid", uid)
                .get()
                .await()
            // Obtenemos los nombres de las categorías para cada hábito
            val snapshotCategorias = firestore.collection("categorias")
                .whereEqualTo("uid", uid)
                .get()
                .await()
            // Construimos un mapa de categorías por ID
            val mapaCategorias = snapshotCategorias.documents.associate { doc ->
                doc.id to Pair(
                    doc.getString("nombre") ?: "",
                    doc.getString("color")  ?: "#C8614A"
                )
            }
            // Construimos los hábitos con categorías cruzadas
            val habitos = snapshot.documents.mapNotNull { doc ->
                val categoriaId = doc.getString("categoriaId") ?: ""
                val (categoriaNombre, categoriaColor) = mapaCategorias[categoriaId]
                    ?: Pair("", "#C8614A")
                // Determinamos si el hábito fue completado hoy usando ultimaCompletacion
                val completadoHoy = esHoy(doc.getTimestamp("ultimaCompletacion"))

                Habit(
                    id               = doc.id,
                    nombre           = doc.getString("nombre")     ?: "",
                    frecuencia       = doc.getString("frecuencia") ?: "",
                    diasSemana       = (doc.get("diasSemana") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList(),
                    racha            = (doc.getLong("racha")      ?: 0L).toInt(),
                    porcentaje       = (doc.getLong("porcentaje") ?: 0L).toInt(),
                    uid              = doc.getString("uid")        ?: "",
                    categoriaId      = categoriaId,
                    categoriaNombre  = categoriaNombre,
                    categoriaColor   = categoriaColor,
                    estaCompletadoHoy = completadoHoy
                )
            }
            // Ordenamos los hábitos por fecha de creación
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
    // Eliminamos un hábito y su subcolección de completaciones
    suspend fun eliminarHabito(habitoId: String): Result<Unit> {
        return try {
            // Eliminamos las completaciones primero para no dejar documentos huérfanos
            val completaciones = coleccionHabitos()
                .document(habitoId)
                .collection("completaciones")
                .get()
                .await()

            val batch = firestore.batch()
            completaciones.documents.forEach { batch.delete(it.reference) }
            batch.delete(coleccionHabitos().document(habitoId))
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /* Marca el hábito como completado hoy,
    retorna 'true' si se registró correctamente, 'false' si ya estaba completado hoy */
    suspend fun completarHabito(habitoId: String): Result<Boolean> {
        return try {
            val hoy               = obtenerFechaHoy()
            val refCompletacion   = coleccionHabitos()
                .document(habitoId)
                .collection("completaciones")
                .document(hoy)
            // Si ya existe el documento de hoy, no hacemos nada (Guard)
            val yaExiste = refCompletacion.get().await()
            if (yaExiste.exists()) return Result.success(false)
            // Registramos la completación de hoy
            refCompletacion.set(
                mapOf(
                    "fecha"     to hoy,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
            ).await()
            // Actualizamos racha, porcentaje y ultimaCompletacion en el documento del hábito
            val refHabito  = coleccionHabitos().document(habitoId)
            val snapshot   = refHabito.get().await()
            val rachaActual      = (snapshot.getLong("racha")      ?: 0L).toInt()
            val porcentajeActual = (snapshot.getLong("porcentaje") ?: 0L).toInt()

            refHabito.update(
                mapOf(
                    "racha"              to rachaActual + 1,
                    "porcentaje"         to minOf(porcentajeActual + 5, 100),
                    "ultimaCompletacion" to com.google.firebase.Timestamp.now()
                )
            ).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Desmarcamos la completación de hoy si existe
    suspend fun descompletarHabito(habitoId: String): Result<Unit> {
        return try {
            val hoy             = obtenerFechaHoy()
            val refCompletacion = coleccionHabitos()
                .document(habitoId)
                .collection("completaciones")
                .document(hoy)
            // Solo revertimos si realmente existía la completación de hoy
            val existeHoy = refCompletacion.get().await()
            if (!existeHoy.exists()) return Result.success(Unit)
            // Eliminamos la completación de hoy
            refCompletacion.delete().await()
            // Actualizamos racha y porcentaje en el documento del hábito
            val refHabito  = coleccionHabitos().document(habitoId)
            val snapshot   = refHabito.get().await()
            val rachaActual      = (snapshot.getLong("racha")      ?: 0L).toInt()
            val porcentajeActual = (snapshot.getLong("porcentaje") ?: 0L).toInt()

            refHabito.update(
                mapOf(
                    "racha"      to maxOf(rachaActual - 1, 0),
                    "porcentaje" to maxOf(porcentajeActual - 5, 0)
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Verificamos si el hábito está completado hoy consultando la subcolección
    suspend fun estaCompletadoHoy(habitoId: String): Result<Boolean> {
        return try {
            val hoy = obtenerFechaHoy()
            val doc = coleccionHabitos()
                .document(habitoId)
                .collection("completaciones")
                .document(hoy)
                .get()
                .await()
            Result.success(doc.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Obtenemos el historial real de los últimos 7 días consultando la subcolección
    suspend fun obtenerHistorial7Dias(habitoId: String): Result<List<DayStatus>> {
        return try {
            val formato    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val etiquetas  = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
            val hoyStr     = obtenerFechaHoy()
            // Traemos todos los documentos de la subcolección (máx ~365 por año de uso)
            val completaciones = coleccionHabitos()
                .document(habitoId)
                .collection("completaciones")
                .get()
                .await()
            // Declaramos el conjunto de fechas completadas para búsqueda O(1)
            val fechasCompletadas = completaciones.documents.map { it.id }.toSet()
            // Construimos los días
            val dias = mutableListOf<DayStatus>()
            // Generamos los últimos 7 días de más antiguo a más reciente
            for (i in 6 downTo 0) {
                val cal       = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val fechaStr  = formato.format(cal.time)
                val etiqueta  = etiquetas[cal.get(Calendar.DAY_OF_WEEK) - 1]
                val numeroDia = cal.get(Calendar.DAY_OF_MONTH)

                val estado = when {
                    fechasCompletadas.contains(fechaStr) -> DayState.COMPLETED
                    fechaStr == hoyStr                   -> DayState.TODAY
                    else                                 -> DayState.MISSED
                }

                dias.add(DayStatus(etiqueta, numeroDia, estado))
            }

            Result.success(dias)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Obtenemos los datos frescos de un hábito específico por su ID
    suspend fun obtenerHabito(habitoId: String): Result<Habit> {
        return try {
            val doc = coleccionHabitos().document(habitoId).get().await()
            val habito = Habit(
                id          = doc.id,
                nombre      = doc.getString("nombre")      ?: "",
                frecuencia  = doc.getString("frecuencia")  ?: "",
                diasSemana  = (doc.get("diasSemana") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                racha       = (doc.getLong("racha")      ?: 0L).toInt(),
                porcentaje  = (doc.getLong("porcentaje") ?: 0L).toInt(),
                uid         = doc.getString("uid")         ?: "",
                categoriaId = doc.getString("categoriaId") ?: ""
            )
            Result.success(habito)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Actualizamos los campos editables de un hábito existente
    suspend fun actualizarHabito(
        habitoId: String,
        nombre: String,
        frecuencia: String,
        diasSemana: List<String>,
        categoriaId: String
    ): Result<Unit> {
        return try {
            coleccionHabitos()
                .document(habitoId)
                .update(
                    mapOf(
                        "nombre"      to nombre,
                        "frecuencia"  to frecuencia,
                        "diasSemana"  to diasSemana,
                        "categoriaId" to categoriaId
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Obtenemos las estadísticas globales del usuario para Home y Perfil
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
                esHoy(doc.getTimestamp("ultimaCompletacion"))
            }

            Result.success(Triple(totalHabitos, completadosHoy, rachaMaxima))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
