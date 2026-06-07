package com.example.proyectos2gastospersonales

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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

    private var selectIconId: Int = 101
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
}
