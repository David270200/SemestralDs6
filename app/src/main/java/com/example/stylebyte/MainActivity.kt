package com.example.stylebyte

import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION_MS = 3000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Debe llamarse ANTES de super.onCreate() y ANTES de setContentView.
        //    Activa el splash nativo del sistema (Android 12+, tu minSdk=33 siempre lo usa).
        installSplashScreen()

        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        animateSplashContent()
        navigateAfterDelay()
    }

    private fun animateSplashContent() {
        val logo = findViewById<ImageView>(R.id.ivLogo)
        val title = findViewById<TextView>(R.id.tvAppName)

        // Logo: Fade In + Scale sutil
        logo.scaleX = 0.8f
        logo.scaleY = 0.8f
        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(700)
            .setInterpolator(OvershootInterpolator(0.8f))
            .start()

        // Texto: Fade In con retraso, efecto secuencial
        title.animate()
            .alpha(1f)
            .setStartDelay(350)
            .setDuration(600)
            .start()
    }

    private fun navigateAfterDelay() {
        // lifecycleScope cancela la corrutina automáticamente si la Activity
        // se destruye antes de tiempo (evita crashes tipo IllegalStateException).
        lifecycleScope.launch {
            delay(SPLASH_DURATION_MS)
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish() // Evita volver al splash al presionar "atrás" desde Login
        }
    }
}