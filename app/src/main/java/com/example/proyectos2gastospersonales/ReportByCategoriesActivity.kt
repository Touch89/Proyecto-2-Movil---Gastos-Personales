package com.example.proyectos2gastospersonales

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.Int
import kotlin.String
import kotlin.math.roundToInt

data class CategoryData(val name: String, val amount: Int, val total: Float, val icon: Int)

class CategoryAdapter(
    private val category: MutableList<CategoryData>,
    val activity: ReportByCategoriesActivity
) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var totalSum: Double = 0.0

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
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

            view.setOnClickListener {
                val intent = Intent(activity, CategoryDetailsActivity::class.java).apply {
                }
                intent.putExtra("category_name", category.name)
                intent.putExtra("user_id", activity.idUser)
                intent.putExtra("total_sum", totalSum)
                intent.putExtra("selected_month", activity.selectedMonth)
                intent.putExtra("selected_year", activity.selectedYear)
                intent.putExtra("selected_type", activity.selectedType)
                activity.startActivity(intent)
            }
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

class ReportByCategoriesActivity : BaseActivity(), AdapterView.OnItemSelectedListener {

    var idUser = 1 //TEMPORAL, si la llaman userId y la dejan pública la app se muere, ni idea de por qué
    var accountId = 1 //TEMPORAL
    var selectedMonth = "0"
    var selectedYear = "1970"
    var selectedType = "Gasto"
    var selectedAccount = "Todas"


    val db by lazy { AppDatabase.getDatabase(this) }
    private lateinit var rv: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var backButton: ImageButton
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

    val spinnerYear = (2023..2030).toList().toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_by_categories)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupDrawer("Categorías", R.layout.activity_report_by_categories)

        val userAccounts = db.accountDao().getAccountsFromUser(idUser)

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

        this.idUser = intent.getIntExtra("user_id", 1)
        val passedYear = intent.getStringExtra("selected_year")
        val passedMonth = intent.getStringExtra("selected_month")
        val passedAccount = intent.getStringExtra("selected_account")

        backButton = findViewById(R.id.rbc_backButton)

        backButton.setOnClickListener {
            finish()
        }

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

        if (passedYear != null) {
            val year = passedYear.toIntOrNull()
            val position = spinnerYear.indexOf(year)
            if (position >= 0) itemYearSpinner.setSelection(position)
        }

        if (passedMonth != null) {
            val month = (passedMonth.toIntOrNull() ?: 1) - 1
            if (month in spinnerMonth.indices) {
                itemMonthSpinner.setSelection(month)
            }
        }

        if (passedAccount != null) {
            val accountPosition = accountNameList.indexOf(passedAccount)
            if (accountPosition >= 0) {
                itemAccountSpinner.setSelection(accountPosition)
            }
        }

        categoryAdapter = CategoryAdapter(mutableListOf(), this)
        rv.adapter = categoryAdapter
    }

    override fun onResume() {
        super.onResume()
        updateRView()
    }

    fun updateRView() {
        val itemAccountSpinner = findViewById<Spinner>(R.id.spinner_account)
        val itemTypeSpinner = findViewById<Spinner>(R.id.spinner_type)
        val itemYearSpinner = findViewById<Spinner>(R.id.spinner_year)
        val itemMonthSpinner = findViewById<Spinner>(R.id.spinner_month)

        val selectedAccount = itemAccountSpinner.selectedItem.toString()
        val selectedType = itemTypeSpinner.selectedItem.toString()
        val sTypeAsEnum = if (selectedType == "Gasto") MovementType.Gasto else MovementType.Ingreso

        val selectedYear = itemYearSpinner.selectedItem.toString()
        val selectedMonth = String.format("%02d", itemMonthSpinner.selectedItemPosition + 1)

        this.selectedType = selectedType
        this.selectedMonth = selectedMonth
        this.selectedYear = selectedYear

        if (selectedAccount == "Todas") {
            categoryAdapter.updateCategories(
                db.categoryDao()
                    .getCategoriesFromAllAccounts(idUser, sTypeAsEnum, selectedMonth, selectedYear),
                db.categoryDao()
                    .getCategoriesFromAllAccounts(idUser, sTypeAsEnum, selectedMonth, selectedYear)
                    .sumOf { it.total.toDouble() }
            )
        } else {
            categoryAdapter.updateCategories(
                db.categoryDao().getCategoriesFromOneAccount(
                    idUser,
                    selectedAccount,
                    sTypeAsEnum,
                    selectedMonth,
                    selectedYear
                ),
                db.categoryDao().getCategoriesFromOneAccount(
                    idUser,
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
