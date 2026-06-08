package com.example.proyectos2gastospersonales

import android.content.Intent
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: MaterialToolbar

    fun setupDrawer(title: String, layoutResId: Int){
        // Cargar layout base
        super.setContentView(R.layout.layout_drawer_base)

        // Cargar layout del contenido
        val contentFrame = findViewById<FrameLayout>(R.id.content_frame)
        layoutInflater.inflate(layoutResId, contentFrame, true)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.topAppBar)

        navigationView.menu.findItem(R.id.nav_help).isEnabled = false

        toolbar.title = title

        // Abrir drawer
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Navegación
        navigationView.setNavigationItemSelectedListener { item ->

            when(item.itemId) {
                R.id.nav_home -> {
                    if(javaClass != MainActivity::class.java){
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                R.id.nav_movements -> {
                    if(javaClass != MovementsActivity::class.java){
                        val intent = Intent(this, MovementsActivity::class.java)
                        startActivity(intent)
                        //finish()
                    }
                }

                R.id.nav_accounts -> {
                    if(javaClass != AccountsActivity::class.java){
                        val intent = Intent(this, AccountsActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                R.id.nav_accounts-> {
                    if(javaClass != AccountsActivity::class.java){
                        val intent = Intent(this, AccountsActivity::class.java)
                        startActivity(intent)
                        //finish()
                    }
                }

                R.id.nav_categories -> {
                    if(javaClass != CategoriesActivity::class.java){
                        val intent = Intent(this, CategoriesActivity::class.java)
                        startActivity(intent)
                        //finish()
                    }
                }

                R.id.nav_help -> {
                    if(javaClass != CategoriesActivity::class.java){
                        val intent = Intent(this, CategoriesActivity::class.java)
                        startActivity(intent)
                        //finish()
                    }
                }

                R.id.nav_about -> {
                    if(javaClass != AboutUsActivity::class.java){
                        val intent = Intent(this, AboutUsActivity::class.java)
                        startActivity(intent)
                        //finish()
                    }
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)

            true
        }

        onBackPressedDispatcher.addCallback(this) {
            if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
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

        val sharedPreferences =
            getSharedPreferences("session", MODE_PRIVATE)

        val userId =
            sharedPreferences.getInt("user_id", -1)

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

        val avatarResource = when(user.avatar) {

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

    fun setSelectedItem(itemId: Int){
        navigationView.setCheckedItem(itemId)
    }
}