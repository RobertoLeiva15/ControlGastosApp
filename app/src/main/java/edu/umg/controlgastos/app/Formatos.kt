package edu.umg.controlgastos.app

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import edu.umg.controlgastos.app.databinding.ItemMovimientoBinding
import edu.umg.controlgastos.modelo.Categoria
import edu.umg.controlgastos.modelo.Movimiento
import edu.umg.controlgastos.modelo.TipoMovimiento
import edu.umg.controlgastos.servicio.formatearQuetzales

// Función de extensión: le agrega a Categoria un emoji para mostrar en pantalla.
// Va aquí (en Android) y no en la librería, porque es algo visual.
fun Categoria.emoji(): String = when (this) {
    Categoria.SALARIO -> "💼"
    Categoria.NEGOCIO -> "🏪"
    Categoria.REGALO -> "🎁"
    Categoria.OTRO_INGRESO -> "💰"
    Categoria.COMIDA -> "🍔"
    Categoria.TRANSPORTE -> "🚌"
    Categoria.ESTUDIOS -> "📚"
    Categoria.SALUD -> "💊"
    Categoria.DIVERSION -> "🎮"
    Categoria.OFRENDA -> "⛪"
    Categoria.OTRO_EGRESO -> "🧾"
}

private val MESES_CORTOS = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic"
)

// "2026-10-29" -> "29 oct 2026"
fun fechaBonita(fecha: String): String {
    val partes = fecha.split("-")
    if (partes.size != 3) return fecha
    val mes = partes[1].toIntOrNull() ?: return fecha
    val dia = partes[2].toIntOrNull() ?: return fecha
    return "$dia ${MESES_CORTOS[mes - 1]} ${partes[0]}"
}

// Llena una fila (item_movimiento.xml) con los datos de un movimiento.
// La usan Inicio y, después, Historial: así no repetimos código.
fun ItemMovimientoBinding.mostrar(m: Movimiento) {
    val contexto = root.context
    val esIngreso = m.tipo == TipoMovimiento.INGRESO

    tvIcono.text = m.categoria.emoji()
    tvIcono.backgroundTintList = ColorStateList.valueOf(
        ContextCompat.getColor(contexto, if (esIngreso) R.color.verde_suave else R.color.rojo_suave)
    )
    tvDescripcion.text = m.descripcion
    tvDetalle.text = "${m.categoria.nombreVisible} · ${fechaBonita(m.fecha)}"
    tvMonto.text = (if (esIngreso) "+ " else "− ") + formatearQuetzales(m.monto)
    tvMonto.setTextColor(
        ContextCompat.getColor(contexto, if (esIngreso) R.color.verde_ingreso else R.color.rojo_egreso)
    )
}