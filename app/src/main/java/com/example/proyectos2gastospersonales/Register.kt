package com.example.proyectos2gastospersonales

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

class Register : AppCompatActivity() {

    private lateinit var db: RoomDatabase
    private lateinit var backButton: ImageButton
    private lateinit var nameTextInput: EditText
    private lateinit var emailTextInput: EditText
    private lateinit var passwordTextInput: EditText
    private lateinit var confirmPasswordTextInput: EditText
    private lateinit var registerButton: Button

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
    }
}