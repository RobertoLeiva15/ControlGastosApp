package edu.umg.controlgastos.app.datos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// La base de datos: une las tablas (entities) con sus consultas (DAOs).
// Se guarda en el celular como el archivo "mis_finanzas.db".
@Database(
    entities = [UsuarioEntity::class, MovimientoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun movimientoDao(): MovimientoDao

    companion object {
        // Patrón Singleton: toda la app usa UNA sola conexión a la base de datos
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mis_finanzas.db"
                ).build().also { instancia = it }
            }
    }
}