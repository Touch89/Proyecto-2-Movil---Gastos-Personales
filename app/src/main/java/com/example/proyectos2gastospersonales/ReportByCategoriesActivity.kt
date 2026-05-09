package com.example.proyectos2gastospersonales

import android.health.connect.datatypes.units.Percentage
import android.os.Bundle
import android.util.Range
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.sql.Date
import kotlin.Int
import kotlin.String
import kotlin.math.roundToInt

data class CategoryData(val name: String, val amount: Int, val total: Float, val icon: Int)

data class SelectedData(
    val account: Int,
    val type: String,
    val year: Int,
    val month: Int,
    val user: Int
)

class CategoryAdapter(
    private val category: MutableList<CategoryData>,
    val activity: AppCompatActivity
) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var totalSum: Double = 0.0

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val itemCategory: TextView
        private val itemAmount: TextView
        private val itemTotal: TextView
        private val itemImage: ImageView
        private val itemPercentage: TextView
        private val itemProgressBar: ProgressBar
        private lateinit var category: CategoryData

        init {
            itemCategory = view.findViewById(R.id.item_category)
            itemAmount = view.findViewById(R.id.item_amount)
            itemTotal = view.findViewById(R.id.item_total)
            itemImage = view.findViewById(R.id.item_image)
            itemProgressBar = view.findViewById(R.id.item_progressbar)
            itemPercentage = view.findViewById(R.id.item_textpercentage)
        }

        fun bind(category: CategoryData, totalSum: Double) {
            this.category = category
            itemCategory.text = category.name
            itemAmount.text = "(${category.amount})"
            itemTotal.text = "$${category.total}"
            if (totalSum > 0) {
                itemProgressBar.progress = ((category.total / totalSum) * 100).toInt()
                itemPercentage.text = "${((category.total / totalSum) * 100).roundToInt()}%"
            } else {
                itemProgressBar.progress = 0
            }
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
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.categorylist_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(category[position], totalSum)

    override fun getItemCount(): Int = category.size

    fun updateCategories(updatedCategories: List<CategoryData>, totalSum: Double) {
        this.totalSum = totalSum
        category.clear()
        category.addAll(updatedCategories)
        notifyDataSetChanged()
    }
}

class ReportByCategoriesActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val userId = 1 //TEMPORAL
    private val accountId = 1 //TEMPORAL
    val db by lazy { AppDatabase.getDatabase(this) }
    private lateinit var rv: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val spinnerType = arrayOf(
        "Gasto", "Ingreso"
    )
    private val spinnerMonth = arrayOf(
        "Enero",
        "Febrero",
        "Marzo",
        "Abril",
        "Mayo",
        "Junio",
        "Julio",
        "Agosto",
        "Septiembre",
        "Octubre",
        "Noviembre",
        "Diciembre"
    )

    val spinnerYear = (1970..2030).toList().toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_by_categories)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userAccounts = db.accountDao().getAccountsFromUser(userId)

        val accountNameList = mutableListOf<String>()

        accountNameList.add("Todas")

        for (account in userAccounts.accounts) {
            accountNameList.add(account.name)
        }

        rv = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val itemTypeSpinner = findViewById<Spinner>(R.id.spinner_type)

        val itemAccountSpinner = findViewById<Spinner>(R.id.spinner_account)

        val itemYearSpinner = findViewById<Spinner>(R.id.spinner_year)

        val itemMonthSpinner = findViewById<Spinner>(R.id.spinner_month)

        val typeAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item, spinnerType
        )

        typeAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        val monthAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item, spinnerMonth
        )

        monthAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        val accountAdapter: ArrayAdapter<*> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item, accountNameList
        )

        accountAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        val yearAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item, spinnerYear
        )

        yearAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        itemTypeSpinner.adapter = typeAdapter
        itemAccountSpinner.adapter = accountAdapter
        itemYearSpinner.adapter = yearAdapter
        itemMonthSpinner.adapter = monthAdapter
        itemTypeSpinner.onItemSelectedListener = this
        itemAccountSpinner.onItemSelectedListener = this
        itemYearSpinner.onItemSelectedListener = this
        itemMonthSpinner.onItemSelectedListener = this
        //Por si acaso https://www.geeksforgeeks.org/android/how-to-set-the-selected-item-of-spinner-by-value-and-not-by-position-in-android/

        categoryAdapter = CategoryAdapter(mutableListOf(), this)
        rv.adapter = categoryAdapter
    }

    private fun updateRView() {
        val itemAccountSpinner = findViewById<Spinner>(R.id.spinner_account)
        val itemTypeSpinner = findViewById<Spinner>(R.id.spinner_type)
        val itemYearSpinner = findViewById<Spinner>(R.id.spinner_year)
        val itemMonthSpinner = findViewById<Spinner>(R.id.spinner_month)

        val selectedAccount = itemAccountSpinner.selectedItem.toString()
        val selectedType = itemTypeSpinner.selectedItem.toString()
        val sTypeAsEnum = if (selectedType == "Gasto") MovementType.Gasto else MovementType.Ingreso

        val selectedYear = itemYearSpinner.selectedItem.toString()
        val selectedMonth = String.format("%02d", itemMonthSpinner.selectedItemPosition + 1)

        if (selectedAccount == "Todas") {
            categoryAdapter.updateCategories(
                db.categoryDao()
                    .getCategoriesFromAllAccounts(userId, sTypeAsEnum, selectedMonth, selectedYear),
                db.categoryDao()
                    .getCategoriesFromAllAccounts(userId, sTypeAsEnum, selectedMonth, selectedYear)
                    .sumOf { it.total.toDouble() }
            )
        } else {
            categoryAdapter.updateCategories(
                db.categoryDao().getCategoriesFromOneAccount(
                    userId,
                    selectedAccount,
                    sTypeAsEnum,
                    selectedMonth,
                    selectedYear
                ),
                db.categoryDao().getCategoriesFromOneAccount(
                    userId,
                    selectedAccount,
                    sTypeAsEnum,
                    selectedMonth,
                    selectedYear
                ).sumOf { it.total.toDouble() }
            )
        }
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        updateRView()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}
