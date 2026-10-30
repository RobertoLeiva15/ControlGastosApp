package edu.umg.controlgastos.app

import android.app.Application

// Application se crea ANTES que cualquier pantalla.
class AplicacionGastos : Application() {
    override fun onCreate() {
        super.onCreate()
        Tema.aplicarGuardado(this)  // modo claro u oscuro guardado
        Contenedor.iniciar(this)    // abre la base de datos Room
    }
}