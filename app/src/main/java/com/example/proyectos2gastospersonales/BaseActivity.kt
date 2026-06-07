package com.example.proyectos2gastospersonales

import android.content.Intent
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.google.firebase.database.ValueEventListener

import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: MaterialToolbar

    fun setupDrawer(title: String, layoutResId: Int) {
        // Cargar layout base
        super.setContentView(R.layout.layout_drawer_base)

        // Cargar layout del contenido
        val contentFrame = findViewById<FrameLayout>(R.id.content_frame)
        layoutInflater.inflate(layoutResId, contentFrame, true)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.topAppBar)

        navigationView.menu.findItem(R.id.nav_accounts).isEnabled = false
        navigationView.menu.findItem(R.id.nav_help).isEnabled = false

        toolbar.title = title

        // Abrir drawer
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }


        val sharedPreferences =
            getSharedPreferences("session", MODE_PRIVATE)

        val userId = sharedPreferences.getInt("user_id", -1)

        val database = Firebase.database.reference

        val dynamicGroupsMap = mutableMapOf<Int, String>()

        val myRef = database.child("grupos")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var index = 0

                navigationView.menu.removeGroup(5)
                dynamicGroupsMap.clear()

                for (groups in snapshot.children) {
                    val groupMembers = mutableListOf<String>()
                    for (member in groups.child("members").children) {
                        val memberId = member.getValue<String>()
                        if (memberId != null) {
                            groupMembers.add(memberId)
                        }
                    }
                    if (groupMembers.contains(userId.toString())) {
                        val groupName = groups.child("name").getValue<String>()
                        val groupId = groups.child("id").getValue<String>()

                        val newItem = navigationView.menu.add(5, index, 4, groupName)

                        newItem.setIcon(R.drawable.baseline_group_24)

                        dynamicGroupsMap[index] = groupId.toString()

                        index++
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@BaseActivity,
                    "Fallo Firebase: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        })

        // Navegación
        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.nav_home -> {
                    if (javaClass != MainActivity::class.java) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                R.id.nav_movements -> {
                    if (javaClass != MovementsActivity::class.java) {
                        val intent = Intent(this, MovementsActivity::class.java)
                        startActivity(intent)
                        //finish()
                    }
                }

                R.id.nav_categories -> {
                    if (javaClass != ReportByCategoriesActivity::class.java) {
                        val intent = Intent(this, ReportByCategoriesActivity::class.java)
                        startActivity(intent)
                        //finish()
                    }
                }

                R.id.nav_about -> {
                    if (javaClass != AboutUsActivity::class.java) {
                        val intent = Intent(this, AboutUsActivity::class.java)
                        startActivity(intent)
                        //finish()
                    }
                }

                R.id.nav_createGroup -> {
                    if (javaClass != CreateGroupActivity::class.java) {
                        val intent = Intent(this, CreateGroupActivity::class.java)
                        startActivity(intent)
                        //finish()
                    }
                }

                R.id.nav_joinGroup -> {
                    if (javaClass != JoinGroupActivity::class.java) {
                        val intent = Intent(this, JoinGroupActivity::class.java)
                        startActivity(intent)
                        //finish()
                    }
                }

                else -> {
                    if (item.groupId == 5) {
                        val groupId = dynamicGroupsMap[item.itemId]

                        if (groupId != null) {
                            val intent = Intent(
                                this,
                                MainActivity::class.java
                            ) //Acá va la actividad del grupo, en vez del main
                            intent.putExtra("groupId", groupId)
                            startActivity(intent)
                        }
                    }
                }
            }


            drawerLayout.closeDrawer(GravityCompat.START)

            true
        }

        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "testapp")
            .allowMainThreadQueries()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {}
            })
            .build()

        val user =
            db.userDao().getUser(userId)


        if (user == null) {

            val sharedPreferences =
                getSharedPreferences("session", MODE_PRIVATE)

            sharedPreferences.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()

            return
        }

        val headerView =
            navigationView.getHeaderView(0)

        val logoutButton =
            headerView.findViewById<ImageButton>(R.id.logout_button)

        val profileImage =
            headerView.findViewById<ImageView>(R.id.user_icon)

        val userName =
            headerView.findViewById<TextView>(R.id.user_name)

        val userEmail =
            headerView.findViewById<TextView>(R.id.user_email)

        val avatarResource = when (user.avatar) {

            1 -> R.drawable.avatar_1
            2 -> R.drawable.avatar_2
            3 -> R.drawable.avatar_3
            4 -> R.drawable.avatar_4

            else -> R.drawable.avatar_1
        }

        profileImage.setImageResource(avatarResource)

        userName.text = user.username
        userEmail.text = user.email

        logoutButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)

            sharedPreferences.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun setSelectedItem(itemId: Int) {
        navigationView.setCheckedItem(itemId)
    }
}