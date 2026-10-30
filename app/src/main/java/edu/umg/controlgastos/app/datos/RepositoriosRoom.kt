package edu.umg.controlgastos.app.datos

import edu.umg.controlgastos.datos.MovimientoRepository
import edu.umg.controlgastos.modelo.Categoria
import edu.umg.controlgastos.modelo.Movimiento
import edu.umg.controlgastos.modelo.TipoMovimiento

// Implementa la MISMA interface que UsuarioRepositoryEnMemoria, pero guardando en Room.
class UsuarioRepositoryRoom(private val dao: UsuarioDao) : UsuarioRepository {

    override suspend fun registrar(usuario: Usuario): Usuario? {
        if (dao.buscarPorUsuario(usuario.usuario) != null) return null // ya existe
        val nuevoId = dao.insertar(
            UsuarioEntity(
                nombre = usuario.nombre,
                usuario = usuario.usuario,
                claveCifrada = usuario.claveCifrada
            )
        )
        return usuario.copy(id = nuevoId.toInt())
    }

    override suspend fun buscarPorUsuario(usuario: String): Usuario? =
        dao.buscarPorUsuario(usuario)?.let {
            Usuario(it.id, it.nombre, it.usuario, it.claveCifrada)
        }
}

// Implementa la interface MovimientoRepository DE LA LIBRERÍA.
// Por eso AdministradorFinanzas funciona igual, sin cambiarle nada (inversión de dependencias).
class MovimientoRepositoryRoom(
    private val dao: MovimientoDao,
    private val usuarioId: Int // el usuario que inició sesión
) : MovimientoRepository {

    override suspend fun agregar(movimiento: Movimiento): Movimiento {
        val nuevoId = dao.insertar(movimiento.aEntidad(usuarioId).copy(id = 0))
        return movimiento.copy(id = nuevoId.toInt())
    }

    override suspend fun listar(): List<Movimiento> =
        dao.listar(usuarioId).map { it.aModelo() }

    override suspend fun eliminar(id: Int): Boolean =
        dao.eliminar(id, usuarioId) > 0

    override suspend fun actualizar(movimiento: Movimiento): Boolean =
        dao.actualizar(movimiento.aEntidad(usuarioId)) > 0
}

// Convertidores: el Movimiento de la librería <-> la fila de la tabla.
// Los enums se guardan como texto con .name y se recuperan con valueOf().
private fun Movimiento.aEntidad(usuarioId: Int) = MovimientoEntity(
    id = id,
    usuarioId = usuarioId,
    descripcion = descripcion,
    monto = monto,
    tipo = tipo.name,
    categoria = categoria.name,
    fecha = fecha
)

private fun MovimientoEntity.aModelo() = Movimiento(
    id = id,
    descripcion = descripcion,
    monto = monto,
    tipo = TipoMovimiento.valueOf(tipo),
    categoria = Categoria.valueOf(categoria),
    fecha = fecha
)