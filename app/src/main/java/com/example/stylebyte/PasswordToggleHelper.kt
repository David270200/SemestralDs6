package com.example.stylebyte

import android.text.InputType
import android.view.MotionEvent
import android.widget.EditText
import androidx.core.content.ContextCompat

/**
 * Agrega un ícono de "ojo" (Show/Hide Password) al final de un EditText de contraseña,
 * siguiendo el patrón recomendado por Material Design (endIcon dentro del propio campo,
 * sin agregar una vista extra al layout).
 *
 * Se implementa con un drawableEnd + OnTouchListener porque los EditText de este
 * proyecto son simples (no usan TextInputLayout), manteniendo el mismo estilo que
 * ya usa el resto del proyecto.
 */
object PasswordToggleHelper {

    fun attach(editText: EditText) {
        var isPasswordVisible = false

        fun applyState() {
            editText.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            val icon = if (isPasswordVisible) R.drawable.ic_eye_off else R.drawable.ic_eye
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
            editText.setSelection(editText.text.length)
        }

        applyState()

        editText.setOnTouchListener { view, event ->
            val et = view as EditText
            val drawableEnd = et.compoundDrawables[2]
            if (drawableEnd != null && event.action == MotionEvent.ACTION_UP) {
                val touchAreaStart = et.width - et.paddingEnd - drawableEnd.intrinsicWidth
                if (event.x >= touchAreaStart) {
                    isPasswordVisible = !isPasswordVisible
                    applyState()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}
