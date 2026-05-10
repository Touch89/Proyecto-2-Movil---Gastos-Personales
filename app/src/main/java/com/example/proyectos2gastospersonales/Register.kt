package com.example.proyectos2gastospersonales

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

class Register : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var backButton: ImageButton
    private lateinit var nameTextInput: EditText
    private lateinit var emailTextInput: EditText
    private lateinit var passwordTextInput: EditText
    private lateinit var confirmPasswordTextInput: EditText
    private lateinit var registerButton: Button
    private lateinit var avatar1Button: ImageButton
    private lateinit var avatar2Button: ImageButton
    private lateinit var avatar3Button: ImageButton
    private lateinit var avatar4Button: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "testapp")
            .allowMainThreadQueries()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {}
            })
            .build()

        backButton = findViewById(R.id.back_button)
        nameTextInput = findViewById(R.id.user_txtb)
        emailTextInput = findViewById(R.id.email_txtb)
        passwordTextInput = findViewById(R.id.password_txtb)
        confirmPasswordTextInput = findViewById(R.id.confirm_password_txtb)
        registerButton = findViewById(R.id.register_button)
        avatar1Button = findViewById(R.id.avatar_1)
        avatar2Button = findViewById(R.id.avatar_2)
        avatar3Button = findViewById(R.id.avatar_3)
        avatar4Button = findViewById(R.id.avatar_4)

        var avatarSelected : Int? = null

        backButton.setOnClickListener { _ ->
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        avatar1Button.setOnClickListener { _ ->
            avatarSelected = 1
        }

        avatar2Button.setOnClickListener { _ ->
            avatarSelected = 2
        }

        avatar3Button.setOnClickListener { _ ->
            avatarSelected = 3
        }

        avatar4Button.setOnClickListener { _ ->
            avatarSelected = 4
        }

        registerButton.setOnClickListener { _ ->
            val name = nameTextInput.text.toString()
            val email = emailTextInput.text.toString()
            val password = passwordTextInput.text.toString()
            val confirmPassword = confirmPasswordTextInput.text.toString()

            var userValid = false

            if (name.isEmpty()) {
                nameTextInput.error = "Este campo es obligatorio"
                userValid = false
            } else {
                userValid = true
            }

            if (email.isEmpty()) {
                emailTextInput.error = "Este campo es obligatorio"
                userValid = false
            } else {
                userValid = true
            }

            if (password.isEmpty()) {
                passwordTextInput.error = "Este campo es obligatorio"
                userValid = false
            } else {
                userValid = true
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordTextInput.error = "Este campo es obligatorio"
                userValid = false
            } else {
                userValid = true
            }

            if (avatarSelected == null) {
                Toast.makeText(this,
                    "Falta seleccionar un avatar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}