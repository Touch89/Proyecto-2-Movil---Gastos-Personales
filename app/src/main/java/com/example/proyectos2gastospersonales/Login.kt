package com.example.proyectos2gastospersonales

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

class Login : AppCompatActivity() {

    private lateinit var db : AppDatabase
    private lateinit var emailTextInput : EditText
    private lateinit var passwordTextInput : EditText
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

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "testapp")
            .allowMainThreadQueries()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {}
            })
            .build()

        emailTextInput = findViewById(R.id.email_txtb)
        passwordTextInput = findViewById(R.id.password_txtb)
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
                    emailTextInput.error = "Email incorrecto"
                } else if (user.password != password) {
                    passwordTextInput.error = "Contraseña incorrecta"
                }
                else {
                    // val intent = Intent(this, GameActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        registerButton.setOnClickListener { _ ->
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }
}