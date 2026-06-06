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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var groupNameTextLayout: TextInputLayout
    private lateinit var groupDescLayout: TextInputLayout
    private lateinit var groupNameInput: TextInputEditText
    private lateinit var groupDescInput: TextInputEditText
    private lateinit var createGroupButton: Button

    fun generateString(): String {
        val uuid = UUID.randomUUID().toString()
        return uuid.replace("-", "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_group)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences =
            getSharedPreferences("session", MODE_PRIVATE)

        val userId =
            sharedPreferences.getInt("user_id", -1)

        val database = Firebase.database.reference

        fun writeNewGroup(groupName: String, desc: String) {
            var groupDesc = desc
            var membersList = listOf(userId.toString())

            if (groupDesc.isEmpty())
                groupDesc = "Sin descripción"

            val groupId = generateString()
            val group = Group(groupId, groupName, groupDesc, 0.0, membersList, emptyList())

            database.child("grupos").child(groupId).setValue(group)
        }

        backButton = findViewById(R.id.CGback_button)
        groupNameTextLayout = findViewById(R.id.groupName_txtb)
        groupDescLayout = findViewById(R.id.groupDesc_txtb)
        groupNameInput = findViewById(R.id.groupName_input)
        groupDescInput = findViewById(R.id.groupDesc_input)
        createGroupButton = findViewById(R.id.createGroup_button)

        backButton.setOnClickListener { _ ->
            //val intent = Intent(this, LoginActivity::class.java)
            //startActivity(intent)
            finish()
        }

        createGroupButton.setOnClickListener { _ ->
            val name = groupNameInput.text.toString()
            val desc = groupDescInput.text.toString()

            var groupValid = true

            if (name.isEmpty()) {
                groupNameTextLayout.error = "Este campo es obligatorio"
                groupValid = false
            }

            if (!groupValid) {
                Toast.makeText(
                    this,
                    "ERROR: Revisa los datos", Toast.LENGTH_SHORT
                ).show()
            } else {
                writeNewGroup(name, desc)
                groupNameInput.text?.clear()
                groupDescInput.text?.clear()

                Toast.makeText(this, "Grupo creado exitosamente", Toast.LENGTH_SHORT).show()
            }
        }
    }
}