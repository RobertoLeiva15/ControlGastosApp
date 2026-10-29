package edu.umg.controlgastos.app

import edu.umg.controlgastos.app.datos.Autenticacion
import edu.umg.controlgastos.app.datos.UsuarioRepositoryEnMemoria
import edu.umg.controlgastos.biblico.GeneradorVersiculos
import edu.umg.controlgastos.datos.MovimientoRepositoryEnMemoria
import edu.umg.controlgastos.servicio.AdministradorFinanzas

// "object" = solo existe UNA instancia en toda la app (patrón Singleton).
// Todas las pantallas usan el mismo Contenedor, así comparten los mismos datos.
// En el Paso 7 aquí cambiamos "EnMemoria" por Room y listo.
object Contenedor {
    val autenticacion = Autenticacion(UsuarioRepositoryEnMemoria())
    val finanzas = AdministradorFinanzas(MovimientoRepositoryEnMemoria())
    val versiculos = GeneradorVersiculos() // para el versículo del día en Inicio
}