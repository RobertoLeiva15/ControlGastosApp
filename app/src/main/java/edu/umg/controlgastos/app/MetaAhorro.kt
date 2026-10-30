package edu.umg.controlgastos.app

import android.content.Context

// Guarda la meta de ahorro mensual de cada usuario en SharedPreferences.
// La llave incluye el id del usuario, así cada cuenta tiene su propia meta.
object MetaAhorro {
    private const val ARCHIVO = "metas_ahorro"

    fun obtener(context: Context, usuarioId: Int): Double =
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .getString("meta_$usuarioId", null)
            ?.toDoubleOrNull() ?: 0.0

    fun guardar(context: Context, usuarioId: Int, monto: Double) {
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .edit()
            .putString("meta_$usuarioId", monto.toString())
            .apply()
    }
}