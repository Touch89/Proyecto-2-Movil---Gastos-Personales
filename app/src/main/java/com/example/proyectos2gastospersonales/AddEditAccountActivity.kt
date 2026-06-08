package com.example.proyectos2gastospersonales

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddEditAccountActivity : AppCompatActivity() {

    private lateinit var nameLayout: TextInputLayout
    private lateinit var nameInput: TextInputEditText

    private lateinit var descLayout: TextInputLayout
    private lateinit var descInput: TextInputEditText

    private lateinit var iconButtons: List<ImageButton>
    private lateinit var saveButton: Button

    val db by lazy { AppDatabase.getDatabase(this) }

    var idUser = 1
    var accountId = -1

    private var selectIconId: Int = 100
    private var isEditMode = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_account)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }

        val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)
        idUser = sharedPreferences.getInt("user_id", -1)

        nameLayout = findViewById(R.id.account_name_layout)
        nameInput = findViewById(R.id.account_name_input)

        descLayout = findViewById(R.id.account_desc_layout)
        descInput = findViewById(R.id.account_desc_input)

        iconButtons = listOf(
            findViewById(R.id.account_img_1),
            findViewById(R.id.account_img_2),
            findViewById(R.id.account_img_3),
            findViewById(R.id.account_img_4),
            findViewById(R.id.account_img_5),
            findViewById(R.id.account_img_6),
            findViewById(R.id.account_img_7),
            findViewById(R.id.account_img_8),
            findViewById(R.id.account_img_9),
            findViewById(R.id.account_img_10)
        )

        iconButtons.forEachIndexed{ index, button ->
            button.setOnClickListener {
                selectIcon(index)
            }
        }

        saveButton = findViewById(R.id.save_changes)

        accountId = intent.getIntExtra("ACCOUNT_ID", -1)
        isEditMode = accountId != -1

        if(isEditMode){
            getAccountValues()
        } else {
            selectIcon(0)
        }

        saveButton.setOnClickListener { createUpdateAccount() }
    }

    private fun selectIcon(index: Int){
        iconButtons.forEach { it.isSelected = false }
        iconButtons[index].isSelected = true

        selectIconId = 101 + index
    }

    private fun getAccountValues(){
        lifecycleScope.launch {
            val account = withContext(Dispatchers.IO){
                db.accountDao().getAccount(accountId, idUser)
            }

            nameInput.setText(account.name)
            descInput.setText(account.description)
            val iconindex = account.icon - 101
            if (iconindex in iconButtons.indices) {
                selectIcon(iconindex)
            }
        }
    }

    private fun createUpdateAccount(){
        lifecycleScope.launch {
            val name = nameInput.text.toString()
            val desc = descInput.text.toString()

            var accountValid = true

            if (name.isEmpty() || name.isBlank()){
                nameLayout.error = "Este campo es obligatorio"
                accountValid = false
            }

            if (desc.isEmpty() || desc.isBlank()){
                descLayout.error = "Este campo es obligatorio"
                accountValid = false
            }

            if (selectIconId == 100) {
                Toast.makeText(this@AddEditAccountActivity, "Falta seleccionar un icono para la cuenta", Toast.LENGTH_SHORT).show()
                accountValid = false
            }

            if(accountValid){
                withContext(Dispatchers.IO){
                    if (isEditMode){
                        val accountToUpdate = Account(
                            id = accountId,
                            name = name,
                            description = desc,
                            icon = selectIconId,
                            userId = idUser
                        )
                        db.accountDao().updateAccount(accountToUpdate)
                    } else {
                        val newAccount = Account(
                            name = name,
                            description = desc,
                            icon = selectIconId,
                            userId = idUser
                        )
                        db.accountDao().insertAccount(newAccount)
                    }
                }

                withContext(Dispatchers.Main){
                    Toast.makeText(this@AddEditAccountActivity,
                        "Cuenta guardada con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
