package com.example.habittrackerapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider

class FirebaseAuthRepository {    // Declaramos el repositorio de autenticación de Firebase
    private val auth      = FirebaseAuth.getInstance()         // Instanciamos la autentificación de Firebase
    private val firestore = FirebaseFirestore.getInstance()    // Instanciamos Firestore
    fun obtenerUsuarioActual(): FirebaseUser? = auth.currentUser    // Obtenemos el usuario actual
    // Obtenemos el nombre del usuario desde Firestore usando el repositorio, sin acceso directo desde la UI
    suspend fun obtenerNombreUsuario(): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("No hay sesión activa")
            val doc = firestore.collection("usuarios").document(uid).get().await()
            val nombre = doc.getString("nombre") ?: "Usuario"
            Result.success(nombre)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Iniciamos sesión con correo y contraseña (Usuario)
    suspend fun iniciarSesion(correo: String, contraseña: String): Result<FirebaseUser> {
        return try {
            val resultado = auth.signInWithEmailAndPassword(correo, contraseña).await()
            val usuario   = resultado.user ?: throw Exception("Usuario no encontrado")
            CategoryRepository().inicializarCategoriasPorDefecto()
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Iniciamos sesión con correo y contraseña (Usuario)
    suspend fun registrarUsuario(
        nombre: String,
        correo: String,
        contraseña: String
    ): Result<FirebaseUser> {
        return try {
            val resultado = auth.createUserWithEmailAndPassword(correo, contraseña).await()
            val usuario   = resultado.user ?: throw Exception("Error al crear usuario")
            val datosUsuario = mapOf(
                "nombre"        to nombre,
                "correo"        to correo,
                "fechaRegistro" to com.google.firebase.Timestamp.now()
            )
            firestore.collection("usuarios")
                .document(usuario.uid)
                .set(datosUsuario)
                .await()
            CategoryRepository().inicializarCategoriasPorDefecto()
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Recuperamos contraseña (Usuario)
    suspend fun enviarCorreoRestablecimiento(correo: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(correo).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun cerrarSesion() = auth.signOut()    // Cerramos sesión
    // Iniciamos sesión con Google (Usuario)
    suspend fun iniciarSesionConGoogle(
        context: Context,
        webClientId: String
    ): Result<FirebaseUser> {
        return try {
            val opcion = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val solicitud = GetCredentialRequest.Builder()
                .addCredentialOption(opcion)
                .build()

            val credentialManager = CredentialManager.create(context)
            val resultado = credentialManager.getCredential(context, solicitud)
            val credencial = resultado.credential

            val tokenGoogle = GoogleIdTokenCredential
                .createFrom(credencial.data)
                .idToken

            val credencialFirebase = GoogleAuthProvider.getCredential(tokenGoogle, null)
            val resultadoFirebase  = auth.signInWithCredential(credencialFirebase).await()
            val usuario = resultadoFirebase.user
                ?: throw Exception("No se pudo autenticar con Google")
            // Inicializamos categorías por defecto igual que en email
            CategoryRepository().inicializarCategoriasPorDefecto()
            // Guardamos el nombre del usuario en Firestore
            Result.success(usuario)
        } catch (e: GetCredentialException) {
            Result.failure(Exception("Inicio con Google cancelado o no disponible"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
