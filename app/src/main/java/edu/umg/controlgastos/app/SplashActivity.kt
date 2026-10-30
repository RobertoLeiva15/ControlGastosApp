package edu.umg.controlgastos.app

import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.umg.controlgastos.app.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Pantalla de bienvenida: logo animado por un momento y luego el Login
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animar()

        // delay espera sin congelar la pantalla (es una corrutina)
        lifecycleScope.launch {
            delay(1800)
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish() // cierra el splash para que "atrás" no regrese aquí
        }
    }

    private fun animar() {
        // El logo empieza pequeño e invisible, y "rebota" a su tamaño normal
        binding.ivLogo.scaleX = 0.3f
        binding.ivLogo.scaleY = 0.3f
        binding.ivLogo.alpha = 0f
        binding.ivLogo.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setDuration(700)
            .setInterpolator(OvershootInterpolator())
            .start()

        // Los textos suben suavemente mientras aparecen
        for ((vista, retraso) in listOf(binding.tvNombre to 300L, binding.tvLema to 500L)) {
            vista.alpha = 0f
            vista.translationY = 40f
            vista.animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(retraso)
                .setDuration(600)
                .start()
        }
    }
}