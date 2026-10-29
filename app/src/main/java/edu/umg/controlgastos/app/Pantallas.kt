package edu.umg.controlgastos.app

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Función de extensión: le "agrega" un método nuevo a cualquier View.
// En Android nuevo la app se dibuja debajo de la barra de estado y de navegación;
// esto agrega el espacio justo para que nada quede tapado (ni por el teclado).
fun View.respetarBarrasDelSistema() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { vista, insets ->
        val barras = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
        )
        vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
        insets
    }
}