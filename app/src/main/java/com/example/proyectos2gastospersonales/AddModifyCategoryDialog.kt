package com.example.proyectos2gastospersonales

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment

// Diálogo #12: permite agregar una nueva categoría (category = null) o modificar una existente
class AddModifyCategoryDialog(
    private val category: Category?,   // null = modo Agregar, objeto = modo Modificar
    private val idUser: Int,           // ID del usuario para asignar la categoría nueva
    private val onSaved: () -> Unit    // Callback que recarga la lista al guardar
) : DialogFragment() {

    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    private var selectedIcon: Int = 0  // Código del ícono seleccionado (201-210); 0 = sin selección

    // Relaciona cada ID de ImageButton con su código numérico de ícono
    private val iconMap = mapOf(
        R.id.icon_btn_201 to 201,
        R.id.icon_btn_202 to 202,
        R.id.icon_btn_203 to 203,
        R.id.icon_btn_204 to 204,
        R.id.icon_btn_205 to 205,
        R.id.icon_btn_206 to 206,
        R.id.icon_btn_207 to 207,
        R.id.icon_btn_208 to 208,
        R.id.icon_btn_209 to 209,
        R.id.icon_btn_210 to 210
    )

    // Construye el diálogo: infla la vista, configura la selección de íconos y los botones
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_category_form, null)

        val etName = view.findViewById<EditText>(R.id.et_category_name)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        val iconButtons = iconMap.keys.map { id -> view.findViewById<ImageButton>(id) }

        // Asigna listener a cada botón de ícono para actualizar la selección visual
        iconMap.forEach { (viewId, iconCode) ->
            view.findViewById<ImageButton>(viewId).setOnClickListener {
                selectedIcon = iconCode
                updateIconSelection(iconButtons, view.findViewById(viewId))
            }
        }

        // Si es modo Modificar, precarga el nombre y resalta el ícono actual
        category?.let {
            etName.setText(it.name)
            selectedIcon = it.icon
            val selectedBtn = view.findViewById<ImageButton>(
                iconMap.entries.first { e -> e.value == it.icon }.key
            )
            updateIconSelection(iconButtons, selectedBtn)
        }

        // Cancelar cierra el diálogo sin guardar cambios
        btnCancel.setOnClickListener { dismiss() }

        // Guardar valida los campos y ejecuta insert o update según el modo
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedIcon == 0) {
                Toast.makeText(requireContext(), "Selecciona un ícono", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (category == null) {
                // Modo Agregar: crea la categoría con un ID basado en timestamp
                db.categoryDao().insert(
                    Category(
                        id = System.currentTimeMillis().toInt(),
                        name = name,
                        icon = selectedIcon,
                        userId = idUser
                    )
                )
            } else {
                // Modo Modificar: actualiza solo el nombre e ícono manteniendo el id original
                db.categoryDao().update(category.copy(name = name, icon = selectedIcon))
            }
            onSaved()
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(if (category == null) "Agregar categoría" else "Modificar categoría")
            .setView(view)
            .create()
    }

    // Pinta de naranja el ícono seleccionado y de blanco los demás para dar feedback visual
    private fun updateIconSelection(allButtons: List<ImageButton>, selected: ImageButton) {
        allButtons.forEach {
            it.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
        selected.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange))
    }
}
