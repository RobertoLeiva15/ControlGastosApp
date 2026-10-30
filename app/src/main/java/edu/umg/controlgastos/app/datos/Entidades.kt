package edu.umg.controlgastos.app.datos

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// @Entity = esta clase es una TABLA de SQLite. Cada propiedad es una COLUMNA.

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["usuario"], unique = true)] // no puede haber dos usuarios iguales
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // igual que AUTO_INCREMENT en MySQL
    val nombre: String,
    val usuario: String,
    @ColumnInfo(name = "clave_cifrada") val claveCifrada: String
)

@Entity(
    tableName = "movimientos",
    // Llave foránea: cada movimiento pertenece a un usuario.
    // CASCADE = si se borra el usuario, se borran sus movimientos.
    foreignKeys = [ForeignKey(
        entity = UsuarioEntity::class,
        parentColumns = ["id"],
        childColumns = ["usuario_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("usuario_id")] // hace más rápida la búsqueda por usuario
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "usuario_id") val usuarioId: Int,
    val descripcion: String,
    val monto: Double,
    val tipo: String,      // se guarda "INGRESO" o "EGRESO"
    val categoria: String, // se guarda "COMIDA", "SALARIO"...
    val fecha: String      // "yyyy-MM-dd"
)

