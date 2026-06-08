package com.example.proyectos2gastospersonales

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class CategoriesActivity : AppCompatActivity(), CategoriesAdapter.CategoryActionListener {

    val db by lazy { AppDatabase.getDatabase(this) }
    private lateinit var rv: RecyclerView
    private lateinit var categoriesAdapter: CategoriesAdapter
    private var categoriesList: MutableList<Category> = mutableListOf()
    private var idUser: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_categories)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        idUser = getSharedPreferences("session", MODE_PRIVATE).getInt("user_id", -1)

        rv = findViewById(R.id.rv_categories)
        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        categoriesAdapter = CategoriesAdapter(categoriesList, this)
        rv.adapter = categoriesAdapter

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.addButton).setOnClickListener {
            AddModifyCategoryDialog(null, idUser) { loadCategories() }
                .show(supportFragmentManager, "AddModifyCategory")
        }
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun loadCategories() {
        val updated = db.categoryDao().getCategoriesByUser(idUser)
        categoriesAdapter.updateCategories(updated)
    }

    override fun onModify(category: Category) {
        AddModifyCategoryDialog(category, idUser) { loadCategories() }
            .show(supportFragmentManager, "AddModifyCategory")
    }

    override fun onDelete(category: Category) {
        db.categoryDao().delete(category)
        loadCategories()
        Snackbar.make(rv, "Categoría eliminada", Snackbar.LENGTH_LONG)
            .setAction("Deshacer") {
                db.categoryDao().insert(category)
                loadCategories()
            }
            .show()
    }

    override fun hasMovements(categoryId: Int): Boolean {
        return db.categoryDao().getMovementCount(categoryId) > 0
    }
}