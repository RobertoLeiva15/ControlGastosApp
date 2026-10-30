package edu.umg.controlgastos.app

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

// Maneja el modo claro/oscuro y lo recuerda en SharedPreferences
// (un archivito de "clave = valor" que no se borra al cerrar la app)
object Tema {
    private const val ARCHIVO = "preferencias"
    private const val CLAVE_OSCURO = "modo_oscuro"

    fun esOscuro(context: Context): Boolean =
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .getBoolean(CLAVE_OSCURO, false)

    // Al abrir la app usa el modo que el usuario eligió la última vez
    fun aplicarGuardado(context: Context) {
        aplicar(esOscuro(context))
    }

    // Cambia de claro a oscuro (o al revés) y lo guarda
    fun cambiar(context: Context) {
        val nuevo = !esOscuro(context)
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(CLAVE_OSCURO, nuevo)
            .apply()
        aplicar(nuevo)
    }

    // Android vuelve a dibujar las pantallas con los colores de values o values-night
    private fun aplicar(oscuro: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (oscuro) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
