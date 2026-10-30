package edu.umg.controlgastos.app

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.umg.controlgastos.app.databinding.ActivityHistorialBinding
import edu.umg.controlgastos.app.databinding.ItemMovimientoBinding
import edu.umg.controlgastos.modelo.Movimiento
import edu.umg.controlgastos.modelo.TipoMovimiento
import edu.umg.controlgastos.servicio.formatearQuetzales
import edu.umg.controlgastos.servicio.mesActual
import edu.umg.controlgastos.servicio.moverMes
import edu.umg.controlgastos.servicio.nombreDelMes
import kotlinx.coroutines.launch

class HistorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialBinding
    private var mesVisible = mesActual()
    private var filtro: TipoMovimiento? = null // null = mostrar todos
    private var textoBusqueda = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.respetarBarrasDelSistema()

        // Abre en el mismo mes que se estaba viendo en Inicio
        mesVisible = intent.getStringExtra(MainActivity.EXTRA_MES) ?: mesActual()

        binding.tvVolver.setOnClickListener { finish() }
        binding.btnMesAnterior.setOnClickListener { cambiarMes(-1) }
        binding.btnMesSiguiente.setOnClickListener { cambiarMes(+1) }

        binding.grupoFiltro.setOnCheckedStateChangeListener { _, idsMarcados ->
            filtro = when (idsMarcados.firstOrNull()) {
                R.id.chipIngresos -> TipoMovimiento.INGRESO
                R.id.chipGastos -> TipoMovimiento.EGRESO
                else -> null
            }
            refrescar()
        }

        // Cada vez que escribes una letra, se vuelve a filtrar la lista
        binding.etBuscar.doAfterTextChanged { texto ->
            textoBusqueda = texto?.toString()?.trim() ?: ""
            refrescar()
        }
    }

    // onResume también se ejecuta al regresar de "Editar", así la lista se actualiza
    override fun onResume() {
        super.onResume()
        refrescar()
    }

    private fun cambiarMes(cantidad: Int) {
        mesVisible = moverMes(mesVisible, cantidad)
        refrescar()
    }

    private fun refrescar() {
        lifecycleScope.launch {
            val finanzas = Contenedor.finanzas
            val resumen = finanzas.resumenDelMes(mesVisible)

            // 1) Filtra por tipo  2) Filtra por texto (en descripción o categoría, sin importar mayúsculas)
            val lista = finanzas.movimientosDelMes(mesVisible)
                .filter { filtro == null || it.tipo == filtro }
                .filter {
                    textoBusqueda.isEmpty() ||
                            it.descripcion.contains(textoBusqueda, ignoreCase = true) ||
                            it.categoria.nombreVisible.contains(textoBusqueda, ignoreCase = true)
                }

            binding.tvMes.text = nombreDelMes(mesVisible)
            binding.tvIngresos.text = formatearQuetzales(resumen.ingresos)
            binding.tvGastos.text = formatearQuetzales(resumen.egresos)
            binding.tvSaldo.text = formatearQuetzales(resumen.saldo)
            binding.tvSaldo.setTextColor(colorDeSaldo(resumen.saldo))
            binding.tvCantidad.text =
                if (lista.isEmpty()) "" else "${lista.size} movimiento(s) · Toca uno para editar o eliminar"
            binding.tvSinMovimientos.text =
                if (textoBusqueda.isEmpty()) "No hay movimientos aquí 🙂"
                else "No se encontró «$textoBusqueda» 🔎"

            mostrarMovimientos(lista)
            mostrarMeses(finanzas.mesesConMovimientos())
        }
    }

    private fun mostrarMovimientos(lista: List<Movimiento>) {
        binding.contenedorMovimientos.removeAllViews()
        val vacia = lista.isEmpty()
        binding.tvSinMovimientos.visibility = if (vacia) View.VISIBLE else View.GONE
        binding.contenedorMovimientos.visibility = if (vacia) View.GONE else View.VISIBLE

        for (movimiento in lista) {
            val fila = ItemMovimientoBinding.inflate(layoutInflater, binding.contenedorMovimientos, false)
            fila.mostrar(movimiento)
            fila.root.setOnClickListener { mostrarOpciones(movimiento) }
            binding.contenedorMovimientos.addView(fila.root)
        }
    }

    // Una fila por cada mes con datos; se reutiliza el diseño item_movimiento
    private suspend fun mostrarMeses(meses: List<String>) {
        binding.contenedorMeses.removeAllViews()
        for (mes in meses) {
            val resumen = Contenedor.finanzas.resumenDelMes(mes)
            val fila = ItemMovimientoBinding.inflate(layoutInflater, binding.contenedorMeses, false)

            fila.tvIcono.text = if (mes == mesVisible) "📍" else "📆"
            fila.tvIcono.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.superficie_alta))
            fila.tvDescripcion.text = nombreDelMes(mes)
            fila.tvDetalle.text =
                "↑ ${formatearQuetzales(resumen.ingresos)}  ·  ↓ ${formatearQuetzales(resumen.egresos)}"
            fila.tvMonto.text = formatearQuetzales(resumen.saldo)
            fila.tvMonto.setTextColor(colorDeSaldo(resumen.saldo))

            fila.root.setOnClickListener {
                mesVisible = mes
                refrescar()
                binding.root.smoothScrollTo(0, 0) // sube al inicio de la pantalla
            }
            binding.contenedorMeses.addView(fila.root)
        }
    }

    // Al tocar un movimiento: elegir Editar o Eliminar
    private fun mostrarOpciones(m: Movimiento) {
        MaterialAlertDialogBuilder(this)
            .setTitle("${m.categoria.emoji()} ${m.descripcion}")
            .setItems(arrayOf("✏️  Editar", "🗑  Eliminar")) { _, opcion ->
                when (opcion) {
                    0 -> editar(m)
                    1 -> confirmarEliminar(m)
                }
            }
            .show()
    }

    // Abre la misma pantalla de "Nuevo movimiento", pero mandándole el id para editar
    private fun editar(m: Movimiento) {
        val intent = Intent(this, NuevoMovimientoActivity::class.java)
        intent.putExtra(NuevoMovimientoActivity.EXTRA_ID, m.id)
        startActivity(intent)
    }

    private fun confirmarEliminar(m: Movimiento) {
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Eliminar movimiento?")
            .setMessage(
                "${m.categoria.emoji()} ${m.descripcion}\n" +
                        "${formatearQuetzales(m.monto)} · ${fechaBonita(m.fecha)}"
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    if (Contenedor.finanzas.eliminar(m.id)) {
                        Toast.makeText(this@HistorialActivity, "🗑 Eliminado", Toast.LENGTH_SHORT).show()
                        refrescar()
                    }
                }
            }
            .show()
    }

    private fun colorDeSaldo(saldo: Double): Int =
        ContextCompat.getColor(this, if (saldo >= 0) R.color.verde_ingreso else R.color.rojo_egreso)
}