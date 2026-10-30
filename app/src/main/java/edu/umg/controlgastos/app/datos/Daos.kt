package edu.umg.controlgastos.app.datos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// DAO = Data Access Object: aquí van las consultas SQL.
// Room revisa el SQL al COMPILAR: si hay un error en una consulta, no compila.
// Las funciones son suspend: Room las corre fuera del hilo de la pantalla.

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long // devuelve el id nuevo

    @Query("SELECT * FROM usuarios WHERE usuario = :usuario LIMIT 1")
    suspend fun buscarPorUsuario(usuario: String): UsuarioEntity?
}

@Dao
interface MovimientoDao {
    @Insert
    suspend fun insertar(movimiento: MovimientoEntity): Long

    // Solo los movimientos del usuario que inició sesión
    @Query("SELECT * FROM movimientos WHERE usuario_id = :usuarioId ORDER BY fecha DESC, id DESC")
    suspend fun listar(usuarioId: Int): List<MovimientoEntity>

    @Update
    suspend fun actualizar(movimiento: MovimientoEntity): Int // filas cambiadas

    @Query("DELETE FROM movimientos WHERE id = :id AND usuario_id = :usuarioId")
    suspend fun eliminar(id: Int, usuarioId: Int): Int // filas borradas
}