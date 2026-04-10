package com.example.habittrackerapp.data

import com.example.habittrackerapp.data.CategoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository {
    // Declaramos instancias de Firebase, son singletons, no se crean múltiples veces
    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    // Retorna el usuario actualmente autenticado (null si no hay sesión)
    fun obtenerUsuarioActual(): FirebaseUser? = auth.currentUser
    // Se inicia sesión, si el usuario no tiene categorías inicializadas las crea en ese momento
    suspend fun iniciarSesion(correo: String, contraseña: String): Result<FirebaseUser> {
        return try {
            val resultado = auth.signInWithEmailAndPassword(correo, contraseña).await()
            val usuario   = resultado.user ?: throw Exception("Usuario no encontrado")
            // Inicializamos las categorías por defecto si el usuario no las tiene aún
            CategoryRepository().inicializarCategoriasPorDefecto()

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Registra un nuevo usuario con correo, contraseña y nombre
    // También crea su documento en Firestore dentro de la colección 'usuarios'
    suspend fun registrarUsuario(
        nombre: String,
        correo: String,
        contraseña: String
    ): Result<FirebaseUser> {
        return try {
            // Creamos la cuenta en Firebase Auth
            val resultado = auth.createUserWithEmailAndPassword(correo, contraseña).await()
            val usuario   = resultado.user ?: throw Exception("Error al crear usuario")
            // Guardamos el perfil en Firestore
            val datosUsuario = mapOf(
                "nombre"         to nombre,
                "correo"         to correo,
                "fechaRegistro"  to com.google.firebase.Timestamp.now()
            )
            firestore.collection("usuarios")
                .document(usuario.uid)
                .set(datosUsuario)
                .await()
            // Inicializamos las categorías por defecto para el usuario nuevo
            CategoryRepository().inicializarCategoriasPorDefecto()

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Envía el correo de restablecimiento de contraseña
    suspend fun enviarCorreoRestablecimiento(correo: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(correo).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Cerramos la sesión del usuario actual
    fun cerrarSesion() = auth.signOut()
}
