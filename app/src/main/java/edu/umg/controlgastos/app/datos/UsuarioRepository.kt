package edu.umg.controlgastos.app.datos

import java.security.MessageDigest

// Un usuario de la app. La contraseña NUNCA se guarda tal cual, solo cifrada.
data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val usuario: String,
    val claveCifrada: String
)

// El contrato: QUÉ se puede hacer con los usuarios (no CÓMO).
interface UsuarioRepository {
    // Devuelve el usuario con su id, o null si ese nombre de usuario ya existe
    suspend fun registrar(usuario: Usuario): Usuario?

    // Busca por nombre de usuario; null si no existe
    suspend fun buscarPorUsuario(usuario: String): Usuario?
}

// SHA-256 convierte la contraseña en un código que no se puede revertir.
fun cifrarClave(clave: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(clave.toByteArray())
        .joinToString("") { "%02x".format(it) }