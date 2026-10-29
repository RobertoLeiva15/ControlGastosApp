package edu.umg.controlgastos.app

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.umg.controlgastos.app.databinding.ActivityMainBinding
import edu.umg.controlgastos.app.databinding.ItemMovimientoBinding
import edu.umg.controlgastos.modelo.Movimiento
import edu.umg.controlgastos.modelo.NivelAlerta
import edu.umg.controlgastos.modelo.TipoMovimiento
import edu.umg.controlgastos.servicio.formatearQuetzales
import edu.umg.controlgastos.servicio.mesActual
import edu.umg.controlgastos.servicio.moverMes
import edu.umg.controlgastos.servicio.nombreDelMes
import kotlinx.coroutines.launch

// Pantalla de Inicio (dashboard)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mesVisible = mesActual() // el mes que se está viendo, ej. "2026-10"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si nadie inició sesión (por ejemplo, Android cerró la app), regresa al login
        val usuario = Contenedor.autenticacion.usuarioActual
        if (usuario == null) {
            irALogin()
            return
        }

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.respetarBarrasDelSistema()

        binding.tvSaludo.text = "Hola, ${usuario.nombre.substringBefore(" ")} 👋"
        binding.tvAvatar.text = usuario.nombre.first().uppercase()

        binding.tvAvatar.setOnClickListener { mostrarMenu(it) }
        binding.btnMesAnterior.setOnClickListener { cambiarMes(-1) }
        binding.btnMesSiguiente.setOnClickListener { cambiarMes(+1) }
        binding.btnNuevoIngreso.setOnClickListener { abrirNuevo(TipoMovimiento.INGRESO) }
        binding.btnNuevoGasto.setOnClickListener { abrirNuevo(TipoMovimiento.EGRESO) }
        binding.cardVersiculo.setOnClickListener { mostrarVersiculo() }
        binding.btnCerrarSesion.setOnClickListener { confirmarCerrarSesion() }
        binding.btnSalir.setOnClickListener { finishAffinity() } // cierra la app

        // NUEVO: abren las pantallas de Historial y Resumen en el mes que se está viendo
        binding.btnHistorial.setOnClickListener { abrirConMes(HistorialActivity::class.java) }
        binding.tvVerTodo.setOnClickListener { abrirConMes(HistorialActivity::class.java) }
        binding.btnResumen.setOnClickListener { abrirConMes(ResumenActivity::class.java) }

        mostrarVersiculo()
    }

    // onResume se ejecuta cada vez que esta pantalla vuelve a verse,
    // así siempre está actualizada (por ejemplo, después de eliminar en Historial)
    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) refrescar()
    }

    private fun cambiarMes(cantidad: Int) {
        mesVisible = moverMes(mesVisible, cantidad)
        refrescar()
    }

    private fun refrescar() {
        lifecycleScope.launch {
            val resumen = Contenedor.finanzas.resumenDelMes(mesVisible)
            val movimientos = Contenedor.finanzas.movimientosDelMes(mesVisible)

            binding.tvMes.text = nombreDelMes(mesVisible)
            binding.tvSaldo.text = formatearQuetzales(resumen.saldo)
            binding.tvIngresos.text = formatearQuetzales(resumen.ingresos)
            binding.tvGastos.text = formatearQuetzales(resumen.egresos)
            binding.progresoGasto.setProgressCompat(resumen.porcentajeGastado.coerceAtMost(100), true)
            binding.tvPorcentaje.text = "Has usado el ${resumen.porcentajeGastado} % de tus ingresos"

            pintarAlerta(resumen.nivel)
            mostrarMovimientos(movimientos.take(5)) // solo los 5 más recientes
        }
    }

    // Verde = bien, amarillo = cuidado (80 %), rojo = gastaste de más
    private fun pintarAlerta(nivel: NivelAlerta) {
        val (colorFondo, colorTexto) = when (nivel) {
            NivelAlerta.SIN_DATOS -> R.color.superficie_alta to R.color.texto_secundario
            NivelAlerta.TRANQUILO -> R.color.verde_suave to R.color.verde_ingreso
            NivelAlerta.CUIDADO -> R.color.ambar_suave to R.color.naranja_oscuro
            NivelAlerta.EXCEDIDO -> R.color.rojo_suave to R.color.rojo_egreso
        }
        binding.tvAlerta.text = nivel.mensaje
        binding.tvAlerta.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, colorFondo))
        binding.tvAlerta.setTextColor(ContextCompat.getColor(this, colorTexto))
    }

    private fun mostrarMovimientos(lista: List<Movimiento>) {
        binding.contenedorMovimientos.removeAllViews()
        val vacia = lista.isEmpty()
        binding.tvSinMovimientos.visibility = if (vacia) View.VISIBLE else View.GONE
        binding.contenedorMovimientos.visibility = if (vacia) View.GONE else View.VISIBLE

        for (movimiento in lista) {
            val fila = ItemMovimientoBinding.inflate(layoutInflater, binding.contenedorMovimientos, false)
            fila.mostrar(movimiento) // función de Formatos.kt
            binding.contenedorMovimientos.addView(fila.root)
        }
    }

    private fun mostrarVersiculo() {
        val versiculo = Contenedor.versiculos.siguiente() // nunca repite el anterior
        binding.tvVersiculo.text = "«${versiculo.texto}»"
        binding.tvCita.text = "— ${versiculo.cita}"
    }

    private fun abrirNuevo(tipo: TipoMovimiento) {
        val intent = Intent(this, NuevoMovimientoActivity::class.java)
        intent.putExtra(NuevoMovimientoActivity.EXTRA_TIPO, tipo.name)
        startActivity(intent)
    }

    // Abre otra pantalla y le manda el mes que se está viendo
    private fun abrirConMes(pantalla: Class<*>) {
        val intent = Intent(this, pantalla)
        intent.putExtra(EXTRA_MES, mesVisible)
        startActivity(intent)
    }

    // Menú al tocar tu inicial
    private fun mostrarMenu(ancla: View) {
        val menu = PopupMenu(this, ancla)
        menu.menu.add(0, 1, 0, "🚪  Cerrar sesión")
        menu.menu.add(0, 2, 1, "✖  Salir de la app")
        menu.setOnMenuItemClickListener { opcion ->
            when (opcion.itemId) {
                1 -> confirmarCerrarSesion()
                2 -> finishAffinity()
            }
            true
        }
        menu.show()
    }

    private fun confirmarCerrarSesion() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que quieres salir de tu cuenta?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Sí, salir") { _, _ ->
                Contenedor.autenticacion.cerrarSesion()
                irALogin()
            }
            .show()
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    companion object {
        const val EXTRA_MES = "mes" // la "llave" para mandar el mes entre pantallas
    }
}