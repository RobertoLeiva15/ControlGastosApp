package edu.umg.controlgastos.app.datos

// Versión temporal: guarda los usuarios en una lista.
// En el Paso 7 la cambiamos por Room sin tocar las pantallas.
class UsuarioRepositoryEnMemoria : UsuarioRepository {

    private val usuarios = mutableListOf<Usuario>()
    private var siguienteId = 1

    override suspend fun registrar(usuario: Usuario): Usuario? {
        if (usuarios.any { it.usuario == usuario.usuario }) return null
        val guardado = usuario.copy(id = siguienteId++)
        usuarios.add(guardado)
        return guardado
    }

    override suspend fun buscarPorUsuario(usuario: String): Usuario? =
        usuarios.find { it.usuario == usuario }
}