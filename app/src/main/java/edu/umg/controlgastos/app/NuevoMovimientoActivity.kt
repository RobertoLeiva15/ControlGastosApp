package edu.umg.controlgastos.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.umg.controlgastos.app.databinding.ActivityNuevoMovimientoBinding
import edu.umg.controlgastos.biblico.Versiculo
import edu.umg.controlgastos.modelo.Categoria
import edu.umg.controlgastos.modelo.Movimiento
import edu.umg.controlgastos.modelo.NivelAlerta
import edu.umg.controlgastos.modelo.TipoMovimiento
import edu.umg.controlgastos.servicio.fechaDeHoy
import edu.umg.controlgastos.servicio.mesDe
import kotlinx.coroutines.launch
import java.util.Locale

// Sirve para CREAR un movimiento nuevo o para EDITAR uno existente
// (si recibe un id en el Intent, entra en modo edición)
class NuevoMovimientoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevoMovimientoBinding
    private var tipo = TipoMovimiento.EGRESO
    private var fechaElegida = fechaDeHoy()
    private var movimientoEditando: Movimiento? = null // null = es nuevo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNuevoMovimientoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.respetarBarrasDelSistema()

        tipo = intent.getStringExtra(EXTRA_TIPO)
            ?.let { TipoMovimiento.valueOf(it) }
            ?: TipoMovimiento.EGRESO

        binding.grupoTipo.check(
            if (tipo == TipoMovimiento.INGRESO) R.id.btnTipoIngreso else R.id.btnTipoGasto
        )
        binding.grupoTipo.addOnButtonCheckedListener { _, idBoton, marcado ->
            if (marcado) {
                cambiarTipo(
                    if (idBoton == R.id.btnTipoIngreso) TipoMovimiento.INGRESO else TipoMovimiento.EGRESO
                )
            }
        }
        cambiarTipo(tipo)
        mostrarFecha()

        binding.tvFecha.setOnClickListener { elegirFecha() }
        binding.tvVolver.setOnClickListener { finish() }
        binding.btnGuardar.setOnClickListener { guardar() }

        // ¿Nos mandaron un id? Entonces cargamos ese movimiento para editarlo
        val idEditar = intent.getIntExtra(EXTRA_ID, -1)
        if (idEditar != -1) {
            lifecycleScope.launch {
                val movimiento = Contenedor.finanzas.buscarPorId(idEditar)
                if (movimiento != null) cargarParaEditar(movimiento)
            }
        }
    }

    // Llena el formulario con los datos del movimiento que se va a editar
    private fun cargarParaEditar(m: Movimiento) {
        movimientoEditando = m
        fechaElegida = m.fecha
        mostrarFecha()

        binding.grupoTipo.check(
            if (m.tipo == TipoMovimiento.INGRESO) R.id.btnTipoIngreso else R.id.btnTipoGasto
        )
        cambiarTipo(m.tipo)

        // Marca el chip de su categoría
        for (i in 0 until binding.grupoCategorias.childCount) {
            val chip = binding.grupoCategorias.getChildAt(i) as Chip
            if (chip.tag == m.categoria) chip.isChecked = true
        }

        binding.etMonto.setText(String.format(Locale.US, "%.2f", m.monto))
        binding.etDescripcion.setText(m.descripcion)
    }

    // Cambia textos, colores y categorías según sea gasto o ingreso
    private fun cambiarTipo(nuevo: TipoMovimiento) {
        tipo = nuevo
        val esIngreso = tipo == TipoMovimiento.INGRESO
        val editando = movimientoEditando != null

        binding.tvTitulo.text =
            (if (editando) "Editar " else "Nuevo ") + (if (esIngreso) "ingreso" else "gasto")
        binding.btnGuardar.text = when {
            editando -> "Guardar cambios"
            esIngreso -> "Guardar ingreso"
            else -> "Guardar gasto"
        }
        binding.btnGuardar.setBackgroundResource(
            if (esIngreso) R.drawable.fondo_boton_ingreso else R.drawable.fondo_boton
        )
        binding.tvQ.setTextColor(
            ContextCompat.getColor(this, if (esIngreso) R.color.verde_ingreso else R.color.naranja)
        )
        llenarCategorias()
    }

    // Crea un chip por cada categoría del tipo elegido (usa Categoria.deTipo de la librería)
    private fun llenarCategorias() {
        binding.grupoCategorias.removeAllViews()
        Categoria.deTipo(tipo).forEachIndexed { posicion, categoria ->
            val chip = layoutInflater.inflate(
                R.layout.chip_categoria, binding.grupoCategorias, false
            ) as Chip
            chip.id = View.generateViewId()
            chip.text = "${categoria.emoji()} ${categoria.nombreVisible}"
            chip.tag = categoria // guardamos la categoría dentro del chip
            binding.grupoCategorias.addView(chip)
            if (posicion == 0) chip.isChecked = true
        }
    }

    private fun categoriaElegida(): Categoria? {
        val id = binding.grupoCategorias.checkedChipId
        return binding.grupoCategorias.findViewById<Chip>(id)?.tag as? Categoria
    }

    private fun mostrarFecha() {
        binding.tvFecha.text = "📅  ${fechaBonita(fechaElegida)}"
    }

    // Calendario para elegir la fecha (no deja elegir fechas futuras)
    private fun elegirFecha() {
        val partes = fechaElegida.split("-").map { it.toInt() }
        val dialogo = DatePickerDialog(
            this,
            { _, anio, mes, dia ->
                fechaElegida = String.format(Locale.US, "%04d-%02d-%02d", anio, mes + 1, dia)
                mostrarFecha()
            },
            partes[0], partes[1] - 1, partes[2] // en DatePicker enero es 0
        )
        dialogo.datePicker.maxDate = System.currentTimeMillis()
        dialogo.show()
    }

    private fun guardar() {
        val categoria = categoriaElegida()
        if (categoria == null) {
            mostrarError("Elige una categoría")
            return
        }

        val datos = Movimiento(
            descripcion = binding.etDescripcion.text.toString().trim(),
            monto = binding.etMonto.text.toString().toDoubleOrNull() ?: 0.0,
            tipo = tipo,
            categoria = categoria,
            fecha = fechaElegida
        )

        binding.btnGuardar.isEnabled = false // evita guardar dos veces por doble toque
        lifecycleScope.launch {
            val editando = movimientoEditando
            if (editando != null) {
                // MODO EDITAR: mismo id, datos nuevos
                val error = Contenedor.finanzas.actualizar(datos.copy(id = editando.id))
                binding.btnGuardar.isEnabled = true
                if (error != null) {
                    mostrarError(error)
                } else {
                    Toast.makeText(this@NuevoMovimientoActivity, "✏️ Cambios guardados", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                // MODO NUEVO: registrar valida, guarda y devuelve un versículo
                val resultado = Contenedor.finanzas.registrar(datos)
                binding.btnGuardar.isEnabled = true
                val error = resultado.error
                if (error != null) {
                    mostrarError(error)
                } else {
                    val resumen = Contenedor.finanzas.resumenDelMes(mesDe(fechaElegida))
                    mostrarExito(resultado.versiculo, resumen.nivel)
                }
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        binding.tvError.text = mensaje
        binding.tvError.visibility = View.VISIBLE
    }

    // Ventana de "¡Guardado!" con el versículo y, si aplica, la alerta del 80 %
    private fun mostrarExito(versiculo: Versiculo?, nivel: NivelAlerta) {
        val mensaje = buildString {
            if (versiculo != null) append("«${versiculo.texto}»\n— ${versiculo.cita}")
            if (nivel == NivelAlerta.CUIDADO || nivel == NivelAlerta.EXCEDIDO) {
                append("\n\n${nivel.mensaje}")
            }
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(if (tipo == TipoMovimiento.INGRESO) "✅ ¡Ingreso guardado!" else "✅ ¡Gasto guardado!")
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("Amén 🙏") { _, _ -> finish() }
            .show()
    }

    companion object {
        const val EXTRA_TIPO = "tipo"
        const val EXTRA_ID = "id" // si viene, es modo edición
    }
}