package com.example.stylebyte

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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

        btnRegister.setOnClickListener {
            registerUser()
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {

        val nombre = etName.text.toString().trim()
        val correo = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmar = etConfirmPassword.text.toString()

        if (nombre.isEmpty()) {
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

        guardarUsuario(nombre, correo, password)

        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()

        finish()
    }
    private fun guardarUsuario(nombre: String, correo: String, password: String) {

        val datos = "$nombre,$correo,$password\n"

        openFileOutput("usuarios.txt", MODE_APPEND).use {
            it.write(datos.toByteArray())
        }

    }
}