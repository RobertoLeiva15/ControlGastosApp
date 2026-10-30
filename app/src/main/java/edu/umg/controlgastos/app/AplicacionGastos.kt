package edu.umg.controlgastos.app

import android.app.Application

// Application se crea ANTES que cualquier pantalla.
// Aquí aplicamos el tema guardado para que la app abra directo en claro u oscuro.
class AplicacionGastos : Application() {
    override fun onCreate() {
        super.onCreate()
        Tema.aplicarGuardado(this)
    }
}