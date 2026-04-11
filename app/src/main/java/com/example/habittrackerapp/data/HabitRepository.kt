package com.example.habittrackerapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitRepository {    // Declaramos el repositorio para los hábitos
    private val auth      = FirebaseAuth.getInstance()         // Declaramos el repositorio para los hábitos
    private val firestore = FirebaseFirestore.getInstance()    // Declaramos el repositorio para los hábitos
    private fun coleccionHabitos() = firestore.collection("habitos")

    private fun obtenerUid(): String =
        auth.currentUser?.uid ?: throw Exception("No hay sesión activa")
    // Obtenemos la fecha actual en formato yyyy-MM-dd
    private fun obtenerFechaHoy(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    // Comprueba si la fecha de completación es hoy
    private fun esHoy(fechaTimestamp: com.google.firebase.Timestamp?): Boolean {
        if (fechaTimestamp == null) return false
        val hoy      = Calendar.getInstance()
        val fechaDoc = Calendar.getInstance().apply { time = fechaTimestamp.toDate() }
        return hoy.get(Calendar.DAY_OF_YEAR) == fechaDoc.get(Calendar.DAY_OF_YEAR) &&
                hoy.get(Calendar.YEAR)        == fechaDoc.get(Calendar.YEAR)
    }
    // Declaramos el mapa de constante Calendar -> etiqueta de día usada en diasSemana
    private val mapaCalendario = mapOf(
        Calendar.MONDAY    to "Lun",
        Calendar.TUESDAY   to "Mar",
        Calendar.WEDNESDAY to "Mié",
        Calendar.THURSDAY  to "Jue",
        Calendar.FRIDAY    to "Vie",
        Calendar.SATURDAY  to "Sáb",
        Calendar.SUNDAY    to "Dom"
    )
    /* Determinamos si la racha debe resetearse a cero, busca hacia atrás hasta 7 días para
    encontrar el último día programado antes de hoy; si ese día no tiene completación
    (la fecha de ultimaCompletacion es anterior), la racha se rompió */
    private fun debeResetearRacha(
        diasSemana: List<String>,
        ultimaCompletacion: com.google.firebase.Timestamp?
    ): Boolean {
        if (diasSemana.isEmpty()) return false

        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 1..7) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val etiqueta = mapaCalendario[cal.get(Calendar.DAY_OF_WEEK)] ?: continue

            if (diasSemana.contains(etiqueta)) {
                // Encontramos el último día en que debía cumplirse el hábito
                val fechaEsperadaStr = formato.format(cal.time)

                if (ultimaCompletacion == null) return true

                val fechaUltimaStr = formato.format(ultimaCompletacion.toDate())
                // Si la última completación es anterior al último día esperado, la racha se rompió
                return fechaUltimaStr < fechaEsperadaStr
            }
        }
        return false
    }
    // Recalculamos el porcentaje de cumplimiento real sobre los últimos 30 días
    private suspend fun calcularPorcentaje30Dias(
        habitoId: String,
        diasSemana: List<String>
    ): Int {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val fechasEsperadas = mutableListOf<String>()
        for (i in 0..29) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val etiqueta = mapaCalendario[cal.get(Calendar.DAY_OF_WEEK)]
            if (diasSemana.contains(etiqueta)) {
                fechasEsperadas.add(formato.format(cal.time))
            }
        }

        if (fechasEsperadas.isEmpty()) return 0

        val completaciones = coleccionHabitos()
            .document(habitoId)
            .collection("completaciones")
            .get()
            .await()

        val fechasCompletadas = completaciones.documents.map { it.id }.toSet()
        val cumplidas = fechasEsperadas.count { fechasCompletadas.contains(it) }

        return ((cumplidas.toFloat() / fechasEsperadas.size.toFloat()) * 100).toInt()
    }
    // Guardamos un hábito en Firestore
    suspend fun guardarHabito(
        nombre: String,
        frecuencia: String,
        diasSemana: List<String>,
        categoriaId: String = ""
    ): Result<String> {
        return try {
            val uid  = obtenerUid()
            val dato = mapOf(
                "uid"                to uid,
                "nombre"             to nombre,
                "frecuencia"         to frecuencia,
                "diasSemana"         to diasSemana,
                "categoriaId"        to categoriaId,
                "racha"              to 0,
                "porcentaje"         to 0,
                "totalCompletaciones" to 0,
                "fechaCreacion"      to com.google.firebase.Timestamp.now()
            )
            val referencia = coleccionHabitos().add(dato).await()
            Result.success(referencia.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /* Cargamos los hábitos del usuario y detecta automáticamente rachas rotas,
    si un hábito tiene racha > 0 y el usuario faltó al último día programado,
    la racha se resetea a 0 en Firestore antes de devolver los datos a la UI */
    suspend fun obtenerHabitos(): Result<List<Habit>> {
        return try {
            val uid      = obtenerUid()
            val snapshot = coleccionHabitos()
                .whereEqualTo("uid", uid)
                .get()
                .await()

            val snapshotCategorias = firestore.collection("categorias")
                .whereEqualTo("uid", uid)
                .get()
                .await()

            val mapaCategorias = snapshotCategorias.documents.associate { doc ->
                doc.id to Pair(
                    doc.getString("nombre") ?: "",
                    doc.getString("color")  ?: "#C8614A"
                )
            }
            // Detectamos hábitos con racha rota y los reseteamos en lote
            val batch = firestore.batch()
            var hayResets = false

            snapshot.documents.forEach { doc ->
                val rachaActual = (doc.getLong("racha") ?: 0L).toInt()
                if (rachaActual > 0) {
                    val diasSemana = (doc.get("diasSemana") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList()
                    val ultimaCompletacion = doc.getTimestamp("ultimaCompletacion")

                    if (debeResetearRacha(diasSemana, ultimaCompletacion)) {
                        batch.update(
                            coleccionHabitos().document(doc.id),
                            mapOf("racha" to 0)
                        )
                        hayResets = true
                    }
                }
            }
            // Solo escribimos a Firestore si hay alguna racha que resetear
            if (hayResets) batch.commit().await()
            // Construimos los modelos con los datos definitivos (ya corregidos en Firestore)
            val habitosActualizados = if (hayResets) {
                coleccionHabitos().whereEqualTo("uid", uid).get().await().documents
            } else {
                snapshot.documents
            }

            val habitos = habitosActualizados.mapNotNull { doc ->
                val categoriaId = doc.getString("categoriaId") ?: ""
                val (categoriaNombre, categoriaColor) = mapaCategorias[categoriaId]
                    ?: Pair("", "#C8614A")

                Habit(
                    id                  = doc.id,
                    nombre              = doc.getString("nombre")      ?: "",
                    frecuencia          = doc.getString("frecuencia")  ?: "",
                    diasSemana          = (doc.get("diasSemana") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList(),
                    racha               = (doc.getLong("racha")       ?: 0L).toInt(),
                    porcentaje          = (doc.getLong("porcentaje")  ?: 0L).toInt(),
                    totalCompletaciones = (doc.getLong("totalCompletaciones") ?: 0L).toInt(),
                    uid                 = doc.getString("uid")         ?: "",
                    categoriaId         = categoriaId,
                    categoriaNombre     = categoriaNombre,
                    categoriaColor      = categoriaColor,
                    estaCompletadoHoy   = esHoy(doc.getTimestamp("ultimaCompletacion"))
                )
            }

            val habitosOrdenados = habitos.sortedByDescending { habit ->
                habitosActualizados
                    .find { it.id == habit.id }
                    ?.getTimestamp("fechaCreacion")
                    ?.seconds ?: 0L
            }

            Result.success(habitosOrdenados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Eliminamos el hábito y su subcolección de completaciones en lote
    suspend fun eliminarHabito(habitoId: String): Result<Unit> {
        return try {
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
    // Registramos la completación de hoy, incrementa racha, totalCompletaciones y recalcula el porcentaje real
    suspend fun completarHabito(habitoId: String): Result<Boolean> {
        return try {
            val hoy             = obtenerFechaHoy()
            val refCompletacion = coleccionHabitos()
                .document(habitoId)
                .collection("completaciones")
                .document(hoy)

            val yaExiste = refCompletacion.get().await()
            if (yaExiste.exists()) return Result.success(false)

            refCompletacion.set(
                mapOf(
                    "fecha"     to hoy,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
            ).await()

            val refHabito = coleccionHabitos().document(habitoId)
            val snapshot  = refHabito.get().await()

            val rachaActual      = (snapshot.getLong("racha")               ?: 0L).toInt()
            val totalActual      = (snapshot.getLong("totalCompletaciones") ?: 0L).toInt()
            val diasSemana       = (snapshot.get("diasSemana") as? List<*>)
                ?.filterIsInstance<String>() ?: emptyList()

            val nuevoPorcentaje = calcularPorcentaje30Dias(habitoId, diasSemana)

            refHabito.update(
                mapOf(
                    "racha"               to rachaActual + 1,
                    "porcentaje"          to nuevoPorcentaje,
                    "totalCompletaciones" to totalActual + 1,
                    "ultimaCompletacion"  to com.google.firebase.Timestamp.now()
                )
            ).await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Eliminamos la completación de hoy, decrementa racha, totalCompletaciones y recalcula el porcentaje real
    suspend fun descompletarHabito(habitoId: String): Result<Unit> {
        return try {
            val hoy             = obtenerFechaHoy()
            val refCompletacion = coleccionHabitos()
                .document(habitoId)
                .collection("completaciones")
                .document(hoy)

            val existeHoy = refCompletacion.get().await()
            if (!existeHoy.exists()) return Result.success(Unit)

            refCompletacion.delete().await()

            val refHabito = coleccionHabitos().document(habitoId)
            val snapshot  = refHabito.get().await()

            val rachaActual = (snapshot.getLong("racha")               ?: 0L).toInt()
            val totalActual = (snapshot.getLong("totalCompletaciones") ?: 0L).toInt()
            val diasSemana  = (snapshot.get("diasSemana") as? List<*>)
                ?.filterIsInstance<String>() ?: emptyList()

            val nuevoPorcentaje = calcularPorcentaje30Dias(habitoId, diasSemana)

            refHabito.update(
                mapOf(
                    "racha"               to maxOf(rachaActual - 1, 0),
                    "porcentaje"          to nuevoPorcentaje,
                    "totalCompletaciones" to maxOf(totalActual - 1, 0)
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Comprobamos si el hábito se completó hoy
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
    // Obtenemos el historial de los últimos 7 días
    suspend fun obtenerHistorial7Dias(habitoId: String): Result<List<DayStatus>> {
        return try {
            val formato   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val etiquetas = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
            val hoyStr    = obtenerFechaHoy()

            val completaciones = coleccionHabitos()
                .document(habitoId)
                .collection("completaciones")
                .get()
                .await()

            val fechasCompletadas = completaciones.documents.map { it.id }.toSet()

            val dias = mutableListOf<DayStatus>()
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
    // Obtenemos el hábito por su ID
    suspend fun obtenerHabito(habitoId: String): Result<Habit> {
        return try {
            val doc = coleccionHabitos().document(habitoId).get().await()
            val habito = Habit(
                id                  = doc.id,
                nombre              = doc.getString("nombre")      ?: "",
                frecuencia          = doc.getString("frecuencia")  ?: "",
                diasSemana          = (doc.get("diasSemana") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                racha               = (doc.getLong("racha")               ?: 0L).toInt(),
                porcentaje          = (doc.getLong("porcentaje")          ?: 0L).toInt(),
                totalCompletaciones = (doc.getLong("totalCompletaciones") ?: 0L).toInt(),
                uid                 = doc.getString("uid")         ?: "",
                categoriaId         = doc.getString("categoriaId") ?: ""
            )
            Result.success(habito)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Actualizamos el hábito
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
    /* Calculamos las estadísticas globales del usuario. 'totalCompletaciones' suma el
    campo almacenado en cada documento de hábito, sin necesidad de leer subcolecciones */
    suspend fun obtenerEstadisticas(): Result<EstadisticasUsuario> {
        return try {
            val uid      = obtenerUid()
            val snapshot = coleccionHabitos()
                .whereEqualTo("uid", uid)
                .get()
                .await()

            val totalHabitos = snapshot.size()

            val rachaMaxima = snapshot.documents
                .maxOfOrNull { (it.getLong("racha") ?: 0L).toInt() } ?: 0

            val completadosHoy = snapshot.documents.count { doc ->
                esHoy(doc.getTimestamp("ultimaCompletacion"))
            }

            val totalCompletaciones = snapshot.documents.sumOf { doc ->
                (doc.getLong("totalCompletaciones") ?: 0L).toInt()
            }

            Result.success(
                EstadisticasUsuario(
                    totalHabitos        = totalHabitos,
                    completadosHoy      = completadosHoy,
                    rachaMaxima         = rachaMaxima,
                    totalCompletaciones = totalCompletaciones
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
