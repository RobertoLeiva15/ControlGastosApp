package edu.umg.controlgastos.app

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import edu.umg.controlgastos.app.databinding.ActivityResumenBinding
import edu.umg.controlgastos.app.databinding.ItemCategoriaBinding
import edu.umg.controlgastos.modelo.Categoria
import edu.umg.controlgastos.modelo.TipoMovimiento
import edu.umg.controlgastos.servicio.formatearQuetzales
import edu.umg.controlgastos.servicio.mesActual
import edu.umg.controlgastos.servicio.moverMes
import edu.umg.controlgastos.servicio.nombreDelMes
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ResumenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumenBinding
    private var mesVisible = mesActual()
    private var tipo = TipoMovimiento.EGRESO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityResumenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.respetarBarrasDelSistema()

        mesVisible = intent.getStringExtra(MainActivity.EXTRA_MES) ?: mesActual()

        binding.tvVolver.setOnClickListener { finish() }
        binding.btnMesAnterior.setOnClickListener { cambiarMes(-1) }
        binding.btnMesSiguiente.setOnClickListener { cambiarMes(+1) }

        binding.grupoTipo.check(R.id.btnGastos)
        binding.grupoTipo.addOnButtonCheckedListener { _, idBoton, marcado ->
            if (marcado) {
                tipo = if (idBoton == R.id.btnIngresos) TipoMovimiento.INGRESO else TipoMovimiento.EGRESO
                refrescar()
            }
        }

        refrescar()
    }

    private fun cambiarMes(cantidad: Int) {
        mesVisible = moverMes(mesVisible, cantidad)
        refrescar()
    }

    private fun refrescar() {
        lifecycleScope.launch {
            // Función nueva de la librería: agrupa por categoría y suma (de mayor a menor)
            val totales = Contenedor.finanzas.totalesPorCategoria(mesVisible, tipo)
            val total = totales.values.sum()
            val esGasto = tipo == TipoMovimiento.EGRESO

            binding.tvMes.text = nombreDelMes(mesVisible)
            binding.tvTituloTotal.text = if (esGasto) "Total gastado" else "Total ingresado"
            binding.tvTotal.text = formatearQuetzales(total)
            binding.tvTotal.setTextColor(
                ContextCompat.getColor(this@ResumenActivity, if (esGasto) R.color.rojo_egreso else R.color.verde_ingreso)
            )

            binding.contenedorCategorias.removeAllViews()
            val vacio = totales.isEmpty()
            binding.tvVacio.visibility = if (vacio) View.VISIBLE else View.GONE
            binding.contenedorCategorias.visibility = if (vacio) View.GONE else View.VISIBLE
            binding.tvConsejo.visibility = if (vacio) View.GONE else View.VISIBLE
            if (vacio) return@launch

            val colores = coloresDeBarras()
            totales.entries.forEachIndexed { posicion, entrada ->
                val categoria = entrada.key
                val monto = entrada.value
                val porcentaje = ((monto / total) * 100).roundToInt()

                val fila = ItemCategoriaBinding.inflate(layoutInflater, binding.contenedorCategorias, false)
                fila.tvNombre.text = "${categoria.emoji()}  ${categoria.nombreVisible}"
                fila.tvMonto.text = formatearQuetzales(monto)
                fila.tvPorcentaje.text = "$porcentaje %"
                fila.barra.setIndicatorColor(colores[posicion % colores.size])
                fila.barra.setProgressCompat(porcentaje, true)
                binding.contenedorCategorias.addView(fila.root)
            }

            // La primera es la más grande, porque la lista viene ordenada de mayor a menor
            val mayor = totales.entries.first()
            val porcentajeMayor = ((mayor.value / total) * 100).roundToInt()
            binding.tvConsejo.text = if (esGasto) {
                "💡 Donde más gastas es en ${mayor.key.emoji()} ${mayor.key.nombreVisible} " +
                        "($porcentajeMayor % de tus gastos).\n${consejoPara(mayor.key)}"
            } else {
                "💡 Tu mayor fuente de ingresos es ${mayor.key.emoji()} ${mayor.key.nombreVisible} " +
                        "($porcentajeMayor %). ¡Dios bendiga tu trabajo! 🙌"
            }
        }
    }

    // Un consejo distinto según la categoría donde más se gasta
    private fun consejoPara(categoria: Categoria): String = when (categoria) {
        Categoria.COMIDA -> "Llevar almuerzo de casa algunos días te puede ayudar a ahorrar."
        Categoria.TRANSPORTE -> "Planificar tus rutas o compartir viaje baja este gasto."
        Categoria.DIVERSION -> "Ponle un límite semanal a la diversión y respétalo."
        Categoria.ESTUDIOS -> "¡Invertir en tus estudios siempre vale la pena! 📚"
        Categoria.SALUD -> "Tu salud es lo primero. Cuídate. 💪"
        Categoria.OFRENDA -> "«Dios ama al dador alegre» — 2 Corintios 9:7"
        else -> "Revisa si puedes reducir un poco este gasto el próximo mes."
    }

    private fun coloresDeBarras(): List<Int> = listOf(
        ContextCompat.getColor(this, R.color.naranja),
        ContextCompat.getColor(this, R.color.rojo_fuego),
        ContextCompat.getColor(this, R.color.ambar),
        Color.parseColor("#FF3B82F6"), // azul
        Color.parseColor("#FF8B5CF6"), // morado
        ContextCompat.getColor(this, R.color.verde_ingreso),
        Color.parseColor("#FFEC4899")  // rosado
    )
}