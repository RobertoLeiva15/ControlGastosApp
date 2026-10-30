package edu.umg.controlgastos.app

import android.content.Context
import edu.umg.controlgastos.app.datos.AppDatabase
import edu.umg.controlgastos.app.datos.Autenticacion
import edu.umg.controlgastos.app.datos.MovimientoRepositoryRoom
import edu.umg.controlgastos.app.datos.UsuarioRepositoryRoom
import edu.umg.controlgastos.biblico.GeneradorVersiculos
import edu.umg.controlgastos.servicio.AdministradorFinanzas

// "object" = una sola instancia en toda la app (Singleton).
// Ahora todo se guarda en Room (SQLite): ya no se pierde al cerrar la app.
object Contenedor {

    private lateinit var baseDatos: AppDatabase

    lateinit var autenticacion: Autenticacion
        private set

    val versiculos = GeneradorVersiculos()

    // Se llama UNA vez al abrir la app (desde AplicacionGastos)
    fun iniciar(context: Context) {
        baseDatos = AppDatabase.obtener(context)
        autenticacion = Autenticacion(UsuarioRepositoryRoom(baseDatos.usuarioDao()))
    }

    // Cada usuario ve SOLO sus movimientos: si cambia el usuario,
    // se crea un AdministradorFinanzas nuevo con el repositorio de ese usuario.
    private var finanzasActuales: AdministradorFinanzas? = null
    private var usuarioDeFinanzas = -1

    val finanzas: AdministradorFinanzas
        get() {
            val usuarioId = autenticacion.usuarioActual?.id ?: -1
            val actuales = finanzasActuales
            if (actuales != null && usuarioDeFinanzas == usuarioId) return actuales

            val nuevas = AdministradorFinanzas(
                MovimientoRepositoryRoom(baseDatos.movimientoDao(), usuarioId)
            )
            finanzasActuales = nuevas
            usuarioDeFinanzas = usuarioId
            return nuevas
        }
}