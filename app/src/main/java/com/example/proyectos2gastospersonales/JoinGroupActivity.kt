package com.example.proyectos2gastospersonales

import android.os.Bundle
import android.util.Log
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
import com.google.firebase.database.database
import kotlin.math.log

class JoinGroupActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var groupRefTextLayout: TextInputLayout
    private lateinit var groupRefInput: TextInputEditText
    private lateinit var joinGroupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_join_group)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val database = Firebase.database.reference

        val sharedPreferences =
            getSharedPreferences("session", MODE_PRIVATE)

        val userId =
            sharedPreferences.getInt("user_id", -1)

        fun joinGroup(groupRef: String) {
            database.child("grupos").child(groupRef).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()){
                    val groupMembers= mutableListOf<String>()
                    for (member in snapshot.child("members").children) {
                        groupMembers.add(member.value.toString())
                    }
                    if (groupMembers.contains(userId.toString())){
                        Toast.makeText(this, "Ya eres miembro del grupo", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        groupMembers.add(userId.toString())
                        database.child("grupos").child(groupRef).child("members").setValue(groupMembers)
                        Toast.makeText(this, "Grupo agregado correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        backButton = findViewById(R.id.JGback_button)
        groupRefTextLayout = findViewById(R.id.groupRef_txtb)
        groupRefInput = findViewById(R.id.groupRef_input)
        joinGroupButton = findViewById(R.id.joinGroup_button)

        backButton.setOnClickListener { _ ->
            //val intent = Intent(this, LoginActivity::class.java)
            //startActivity(intent)
            finish()
        }

        joinGroupButton.setOnClickListener { _ ->
            val ref = groupRefInput.text.toString()

            var groupValid = true

            if (ref.isEmpty()) {
                groupRefTextLayout.error = "Ingrese un código de grupo"
                groupValid = false
            }

            if (!groupValid) {
                Toast.makeText(
                    this,
                    "ERROR: Revisa los datos", Toast.LENGTH_SHORT
                ).show()
            } else {
                joinGroup(ref)
                groupRefInput.text?.clear()
            }
        }
    }
}