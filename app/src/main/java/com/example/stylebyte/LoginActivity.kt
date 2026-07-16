package com.example.stylebyte

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LoginActivity : AppCompatActivity() {

    private lateinit var tabSignIn: TextView
    private lateinit var tabCreateAccount: TextView
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var btnGuest: Button
    private lateinit var tvForgot: TextView
    private lateinit var tvTerms: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tabSignIn = findViewById(R.id.tab_sign_in)
        tabCreateAccount = findViewById(R.id.tab_create_account)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnSignIn = findViewById(R.id.btn_sign_in)
        btnGuest = findViewById(R.id.btn_guest)
        tvForgot = findViewById(R.id.tv_forgot)
        tvTerms = findViewById(R.id.tv_terms)

        setupTabs()
        setupTermsLink()

        btnSignIn.setOnClickListener { attemptSignIn() }

        btnGuest.setOnClickListener {
            // TODO: navegar a la pantalla principal como invitado
            Toast.makeText(this, "Continuando como invitado", Toast.LENGTH_SHORT).show()
        }

        tvForgot.setOnClickListener {
            // TODO: navegar a pantalla de recuperar contraseña
            Toast.makeText(this, "Recuperar contraseña", Toast.LENGTH_SHORT).show()
        }
    }

    /** Alterna el estilo visual entre "Sign In" y "Create Account" */
    private fun setupTabs() {
        tabSignIn.setOnClickListener {
            tabSignIn.setBackgroundResource(R.drawable.bg_tab_active)
            tabSignIn.setTextColor(ContextCompat.getColor(this, R.color.white))
            tabCreateAccount.background = null
            tabCreateAccount.setTextColor(ContextCompat.getColor(this, R.color.tab_inactive_text))
        }

        tabCreateAccount.setOnClickListener {

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    /** Hace clickeable solo la palabra "Terms of Service" dentro del texto legal */
    private fun setupTermsLink() {
        val fullText = "By continuing, you agree to our Terms of Service"
        val linkText = "Terms of Service"
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf(linkText)
        val end = start + linkText.length

        if (start >= 0) {
            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // TODO: abrir pantalla o navegador con los Terms of Service
                    Toast.makeText(this@LoginActivity, "Abrir Terms of Service", Toast.LENGTH_SHORT).show()
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        tvTerms.text = spannable
        tvTerms.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun attemptSignIn() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Ingresa un correo válido"
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Ingresa tu contraseña"
            return
        }

        // TODO: reemplazar con tu lógica real de autenticación (API, Firebase, etc.)
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()
    }
}