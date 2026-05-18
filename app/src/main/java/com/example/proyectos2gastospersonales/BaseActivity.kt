package com.example.proyectos2gastospersonales

import android.content.Intent
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {
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
                    if(javaClass != MainActivity::class.java){
                        val intent = Intent(this, MainActivity::class.java)
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
    }

    fun setSelectedItem(itemId: Int){
        navigationView.setCheckedItem(itemId)
    }
}