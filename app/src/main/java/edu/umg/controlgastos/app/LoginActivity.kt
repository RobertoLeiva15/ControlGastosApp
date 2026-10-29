package edu.umg.controlgastos.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.umg.controlgastos.app.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // View Binding: conecta el XML con el código sin usar findViewById
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.respetarBarrasDelSistema()

        binding.btnEntrar.setOnClickListener { entrar() }

        // Intent = "quiero ir a otra pantalla"
        binding.tvCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun entrar() {
        val usuario = binding.etUsuario.text.toString()
        val clave = binding.etClave.text.toString()

        // iniciarSesion es suspend, por eso se llama dentro de lifecycleScope.launch
        lifecycleScope.launch {
            val error = Contenedor.autenticacion.iniciarSesion(usuario, clave)
            if (error != null) {
                binding.tvError.text = error
                binding.tvError.visibility = View.VISIBLE
            } else {
                irAInicio()
            }
        }
    }

    private fun irAInicio() {
        val intent = Intent(this, MainActivity::class.java)
        // Borra el login del historial: al dar "atrás" en Inicio, la app se cierra
        // en lugar de regresar al login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}