package com.example.proyectos2gastospersonales

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AddEditAccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_account)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
    }
}
