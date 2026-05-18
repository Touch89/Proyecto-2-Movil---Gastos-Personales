package com.example.proyectos2gastospersonales

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var db : AppDatabase
    private lateinit var emailTextLayout: TextInputLayout
    private lateinit var passwordTextLayout: TextInputLayout
    private lateinit var emailTextInput : TextInputEditText
    private lateinit var passwordTextInput : TextInputEditText
    private lateinit var loginButton : Button
    private lateinit var registerButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        // SI LA SESIÓN EXISTE
        if (userId != -1){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

            return
        }

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "testapp")
            .allowMainThreadQueries()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {}
            })
            .build()

        emailTextLayout = findViewById(R.id.email_txtb)
        passwordTextLayout = findViewById(R.id.password_txtb)
        emailTextInput = findViewById(R.id.email_input)
        passwordTextInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)

        loginButton.setOnClickListener { _ ->

            var email = emailTextInput.text.toString().trim()
            var password = passwordTextInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                    "Datos de sesión faltantes",
                    Toast.LENGTH_SHORT).show()
            } else {

                val user = db.userDao().getUserByEmail(email)
                if (user == null) {
                    emailTextLayout.error = "Email incorrecto"
                } else if (user.password != password) {
                    passwordTextLayout.error = "Contraseña incorrecta"
                }
                else {
                    val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)

                    sharedPreferences.edit().putInt("user_id", user.id).apply()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        registerButton.setOnClickListener { _ ->
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}