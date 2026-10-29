package edu.umg.controlgastos.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.umg.controlgastos.app.databinding.ActivityRegistroBinding
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.respetarBarrasDelSistema()

        // finish() cierra esta pantalla y regresa a la anterior (el login)
        binding.tvVolver.setOnClickListener { finish() }
        binding.btnCrear.setOnClickListener { crearCuenta() }
    }

    private fun crearCuenta() {
        val nombre = binding.etNombre.text.toString()
        val usuario = binding.etUsuario.text.toString()
        val clave = binding.etClave.text.toString()
        val confirmacion = binding.etConfirmar.text.toString()

        lifecycleScope.launch {
            val error = Contenedor.autenticacion.crearCuenta(nombre, usuario, clave, confirmacion)
            if (error != null) {
                binding.tvError.text = error
                binding.tvError.visibility = View.VISIBLE
            } else {
                Toast.makeText(
                    this@RegistroActivity,
                    "¡Bienvenido, ${nombre.trim()}!",
                    Toast.LENGTH_SHORT
                ).show()
                // Al crear la cuenta entra directo, sin volver a escribir la contraseña
                val intent = Intent(this@RegistroActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}