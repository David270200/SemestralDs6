package com.example.stylebyte

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stylebyte.network.RetrofitClient
import com.example.stylebyte.network.dto.RegisterRequestDto
import com.example.stylebyte.network.extractErrorMessage
import kotlinx.coroutines.launch

/**
 * Registro real contra SQL Server (tabla Usuarios), vía POST /auth/register.
 * La API valida que el correo no exista y siempre crea el usuario con Rol
 * = "Cliente" (no se puede registrar un Administrador desde aquí).
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    private lateinit var btnRegister: Button
    private lateinit var tvBackToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.et_full_name)
        etEmail = findViewById(R.id.et_register_email)
        etPassword = findViewById(R.id.et_register_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)

        btnRegister = findViewById(R.id.btn_create_account)
        tvBackToLogin = findViewById(R.id.tv_back_to_login)

        PasswordToggleHelper.attach(etPassword)
        PasswordToggleHelper.attach(etConfirmPassword)

        btnRegister.setOnClickListener {
            registerUser()
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val nombreCompleto = etName.text.toString().trim()
        val correo = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmar = etConfirmPassword.text.toString()

        if (nombreCompleto.isEmpty()) {
            etName.error = "Ingrese su nombre"
            etName.requestFocus()
            return
        }

        if (correo.isEmpty()) {
            etEmail.error = "Ingrese un correo"
            etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etEmail.error = "Correo inválido"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Ingrese una contraseña"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            etPassword.requestFocus()
            return
        }

        if (confirmar.isEmpty()) {
            etConfirmPassword.error = "Confirme la contraseña"
            etConfirmPassword.requestFocus()
            return
        }

        if (password != confirmar) {
            etConfirmPassword.error = "Las contraseñas no coinciden"
            etConfirmPassword.requestFocus()
            return
        }

        // La tabla Usuarios separa Nombre y Apellido; el formulario solo pide
        // "Nombre Completo", así que partimos por la primera palabra.
        val partes = nombreCompleto.split(" ", limit = 2)
        val nombre = partes[0]
        val apellido = if (partes.size > 1) partes[1] else "-"

        btnRegister.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authApi.register(
                    RegisterRequestDto(nombre = nombre, apellido = apellido, correo = correo, password = password)
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    SessionManager(this@RegisterActivity).saveSession(
                        userId = body.usuario.IdUsuario,
                        name = "${body.usuario.Nombre} ${body.usuario.Apellido}".trim(),
                        email = body.usuario.Correo,
                        role = body.usuario.Rol,
                        token = body.token
                    )
                    Toast.makeText(this@RegisterActivity, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                    finish()
                } else if (response.code() == 409) {
                    etEmail.error = "Ya existe una cuenta con ese correo"
                    etEmail.requestFocus()
                } else {
                    Toast.makeText(this@RegisterActivity, response.extractErrorMessage("No se pudo crear la cuenta"), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity,
                    "No se pudo conectar con la API. Verifica que el servidor esté corriendo.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnRegister.isEnabled = true
            }
        }
    }
}
