package com.example.proyectos2gastospersonales

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var backButton: ImageButton
    private lateinit var nameTextLayout: TextInputLayout
    private lateinit var emailTextLayout: TextInputLayout
    private lateinit var passwordTextLayout: TextInputLayout
    private lateinit var confirmPasswordTextLayout: TextInputLayout
    private lateinit var nameTextInput: TextInputEditText
    private lateinit var emailTextInput: TextInputEditText
    private lateinit var passwordTextInput: TextInputEditText
    private lateinit var confirmPasswordTextInput: TextInputEditText
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
        nameTextLayout = findViewById(R.id.user_txtb)
        emailTextLayout = findViewById(R.id.email_txtb)
        passwordTextLayout = findViewById(R.id.password_txtb)
        confirmPasswordTextLayout = findViewById(R.id.confirm_password_txtb)
        nameTextInput = findViewById(R.id.user_input)
        emailTextInput = findViewById(R.id.email_input)
        passwordTextInput = findViewById(R.id.password_input)
        confirmPasswordTextInput = findViewById(R.id.confirm_password_input)
        registerButton = findViewById(R.id.register_button)
        avatar1Button = findViewById(R.id.avatar_1)
        avatar2Button = findViewById(R.id.avatar_2)
        avatar3Button = findViewById(R.id.avatar_3)
        avatar4Button = findViewById(R.id.avatar_4)

        var avatarSelected = 0

        backButton.setOnClickListener { _ ->
            val intent = Intent(this, LoginActivity::class.java)
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

            var userValid = true

            if (name.isEmpty()) {
                nameTextLayout.error = "Este campo es obligatorio"
                userValid = false }

            if (email.isEmpty()) {
                emailTextLayout.error = "Este campo es obligatorio"
                userValid = false }

            if (password.isEmpty()) {
                passwordTextLayout.error = "Este campo es obligatorio"
                userValid = false }

            if (confirmPassword.isEmpty()) {
                confirmPasswordTextLayout.error = "Este campo es obligatorio"
                userValid = false }

            if (password != confirmPassword){
                confirmPasswordTextLayout.error = "Las contraseñas no coinciden"
                userValid = false }

            if (avatarSelected == 0) {
                Toast.makeText(this,
                    "Falta seleccionar un avatar", Toast.LENGTH_SHORT).show()
                userValid = false }

            val existingUser = db.userDao().getUserByEmail(email)

            if (existingUser != null){
                emailTextLayout.error = "Este correo ya fue registrado"
                userValid = false
            }

            if (!userValid) {
                Toast.makeText(this,
                    "ERROR: Revisa los datos", Toast.LENGTH_SHORT).show()
            } else {
                val user = User(username = name, email = email, avatar = avatarSelected, password = password)
                db.userDao().InsertUser(user)

                Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}