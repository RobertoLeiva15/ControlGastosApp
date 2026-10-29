package edu.umg.controlgastos.app.datos

// Las reglas del login y del registro.
// Cada función devuelve null si todo salió bien, o el mensaje de error.
class Autenticacion(private val repositorio: UsuarioRepository) {

    var usuarioActual: Usuario? = null
        private set

    suspend fun crearCuenta(
        nombre: String,
        usuario: String,
        clave: String,
        confirmacion: String
    ): String? {
        val usuarioLimpio = usuario.trim().lowercase()

        if (nombre.isBlank()) return "Escribe tu nombre"
        if (usuarioLimpio.length < 4) return "El usuario debe tener al menos 4 letras"
        if (usuarioLimpio.contains(" ")) return "El usuario no puede tener espacios"
        if (clave.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        if (clave != confirmacion) return "Las contraseñas no coinciden"

        val nuevo = repositorio.registrar(
            Usuario(
                nombre = nombre.trim(),
                usuario = usuarioLimpio,
                claveCifrada = cifrarClave(clave)
            )
        ) ?: return "Ese usuario ya existe, elige otro"

        usuarioActual = nuevo
        return null
    }

    suspend fun iniciarSesion(usuario: String, clave: String): String? {
        if (usuario.isBlank() || clave.isEmpty()) return "Escribe tu usuario y tu contraseña"

        val encontrado = repositorio.buscarPorUsuario(usuario.trim().lowercase())
            ?: return "Ese usuario no existe. Crea una cuenta primero"

        if (encontrado.claveCifrada != cifrarClave(clave)) return "Contraseña incorrecta"

        usuarioActual = encontrado
        return null
    }

    fun cerrarSesion() {
        usuarioActual = null
    }
}