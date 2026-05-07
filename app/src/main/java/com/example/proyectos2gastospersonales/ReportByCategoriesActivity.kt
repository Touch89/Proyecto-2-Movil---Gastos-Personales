package com.example.proyectos2gastospersonales

import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.Int
import kotlin.String

data class Category(val name: String, val amount: Int, val total: Int, val icon: Int)

class CategoryAdapter(private val category: MutableList<Category>, val activity: AppCompatActivity) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val itemCategory: TextView
        private val itemAmount: TextView
        private val itemTotal: TextView
        private val itemImage: ImageView
        private lateinit var category: Category

        init {
            itemCategory = view.findViewById(R.id.item_category)
            itemAmount = view.findViewById(R.id.item_amount)
            itemTotal = view.findViewById(R.id.item_total)
            itemImage = view.findViewById(R.id.item_image)
        }

        fun bind(category: Category) {
            this.category = category
            itemCategory.text = category.name
            itemAmount.text = "(${category.amount})"
            itemTotal.text = "$${category.total}"
            itemImage.setImageResource(
                when (category.icon) {
                    0 -> R.drawable.ic_android_black_24dp
                    1 -> R.drawable.outline_1k_24
                    else -> R.drawable.ic_launcher_foreground
                }
            )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.categorylist_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(category[position])

    override fun getItemCount(): Int = category.size
}

class ReportByCategoriesActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var testCategories: MutableList<Category>
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_by_categories)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rv = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        testCategories = mutableListOf(
            Category("Comida", 6, 1300, 0),
            Category("Casa", 3, 1114, 1),
            Category("Transporte", 3, 742, 2),
            Category("Entretenimiento", 7, 371, 3),
            Category("Mascotas", 6, 185, 4)
        )

        categoryAdapter = CategoryAdapter(testCategories, this)
        rv.adapter = categoryAdapter
    }

}