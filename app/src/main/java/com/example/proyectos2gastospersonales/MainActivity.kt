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
import java.util.Calendar
import kotlin.math.roundToInt

class HomeCategoryAdapter(
    private val categories: MutableList<CategoryData>,
    private val onItemClick: (CategoryData) -> Unit
) : RecyclerView.Adapter<HomeCategoryAdapter.ViewHolder>() {

    private var totalSum: Double = 0.0

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val itemCategory: TextView = view.findViewById(R.id.item_category)
        private val itemAmount: TextView = view.findViewById(R.id.item_amount)
        private val itemTotal: TextView = view.findViewById(R.id.item_total)
        private val itemImage: ImageView = view.findViewById(R.id.item_image)
        private val itemProgressBar: ProgressBar = view.findViewById(R.id.item_progressbar)
        private val itemPercentage: TextView = view.findViewById(R.id.item_textpercentage)

        fun bind(category: CategoryData, totalSum: Double) {
            itemCategory.text = category.name
            itemAmount.text = "(${category.amount})"
            itemTotal.text = "$${category.total}"
            if (totalSum > 0) {
                itemProgressBar.progress = ((category.total / totalSum) * 100).toInt()
                itemPercentage.text = "${((category.total / totalSum) * 100).roundToInt()}%"
            } else {
                itemProgressBar.progress = 0
                itemPercentage.text = "0%"
            }
            itemImage.setImageResource(
                when (category.icon) {
                    0 -> R.drawable.ic_android_black_24dp
                    1 -> R.drawable.outline_1k_24
                    else -> R.drawable.ic_launcher_foreground
                }
            )
            view.setOnClickListener { onItemClick(category) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.categorylist_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(categories[position], totalSum)

    override fun getItemCount(): Int = categories.size

    fun updateCategories(updatedCategories: List<CategoryData>, newTotalSum: Double) {
        totalSum = newTotalSum
        categories.clear()
        categories.addAll(updatedCategories)
        notifyDataSetChanged()
    }
}

class MainActivity : AppCompatActivity() {

    val idUser = 1 // TEMPORAL

    val db by lazy { AppDatabase.getDatabase(this) }

    private lateinit var rv: RecyclerView
    private lateinit var categoryAdapter: HomeCategoryAdapter

    private lateinit var spinnerAccount: Spinner
    private lateinit var spinnerYear: Spinner
    private lateinit var spinnerMonth: Spinner

    private lateinit var tvIngreso: TextView
    private lateinit var tvSaldoAnterior: TextView
    private lateinit var tvGasto: TextView
    private lateinit var tvSaldoActual: TextView

    private val spinnerMonthNames = arrayOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    private val spinnerYears = (2020..2026).toList().toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Views
        spinnerAccount = findViewById(R.id.spinner_account)
        spinnerYear = findViewById(R.id.spinner_year)
        spinnerMonth = findViewById(R.id.spinner_month)
        tvIngreso = findViewById(R.id.tv_ingreso)
        tvSaldoAnterior = findViewById(R.id.tv_saldo_anterior)
        tvGasto = findViewById(R.id.tv_gasto)
        tvSaldoActual = findViewById(R.id.tv_saldo_actual)

        // RecyclerView
        rv = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        categoryAdapter = HomeCategoryAdapter(mutableListOf()) { category ->
            val intent = Intent(this, ReportByCategoriesActivity::class.java)
            val selectedYear = spinnerYear.selectedItem?.toString() ?: "2026"
            val selectedMonth = String.format("%02d", spinnerMonth.selectedItemPosition + 1)
            val selectedAccount = spinnerAccount.selectedItem?.toString() ?: "Todas"
            intent.putExtra("user_id", idUser)
            intent.putExtra("category_name", category.name)
            intent.putExtra("selected_year", selectedYear)
            intent.putExtra("selected_month", selectedMonth)
            intent.putExtra("selected_account", selectedAccount)
            startActivity(intent)
        }
        rv.adapter = categoryAdapter

        // Spinner cuentas
        val userAccounts = db.accountDao().getAccountsFromUser(idUser)
        val accountNames = mutableListOf("Todas")
        userAccounts.accounts.forEach { accountNames.add(it.name) }
        val accountAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountNames)
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAccount.adapter = accountAdapter

        // Spinner año
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerYears)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter

        // Spinner mes
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerMonthNames)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter

        // Seleccionar año y mes actuales por defecto
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) // 0-indexed
        val yearIndex = spinnerYears.indexOf(currentYear)
        if (yearIndex >= 0) spinnerYear.setSelection(yearIndex)
        spinnerMonth.setSelection(currentMonth)

        // Listener compartido para los tres spinners
        val spinnerListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateUI()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerAccount.onItemSelectedListener = spinnerListener
        spinnerYear.onItemSelectedListener = spinnerListener
        spinnerMonth.onItemSelectedListener = spinnerListener

        // Carga inicial explícita (garantía extra además de onResume)
        updateUI()

        // ImageButtons → abren Pantalla #4 (AddMovementActivity)
        val btnIngreso = findViewById<ImageButton>(R.id.btn_ingreso)
        val btnRetiro = findViewById<ImageButton>(R.id.btn_retiro)
        val btnTransferencia = findViewById<ImageButton>(R.id.btn_transferencia)

        btnIngreso.setOnClickListener { openAddMovement(MovementType.Ingreso) }
        btnRetiro.setOnClickListener { openAddMovement(MovementType.Gasto) }
        btnTransferencia.setOnClickListener { openAddMovement(MovementType.Transferencia) }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun openAddMovement(type: MovementType) {
        val intent = Intent(this, AddMovementActivity::class.java)
        intent.putExtra("movement_type", type.name)
        startActivity(intent)
    }

    private fun updateUI() {
        val selectedAccount = spinnerAccount.selectedItem?.toString() ?: "Todas"
        val selectedYear = spinnerYear.selectedItem?.toString() ?: "2024"
        val selectedMonth = String.format("%02d", spinnerMonth.selectedItemPosition + 1)

        // Resumen por categoría (Ingresos y Gastos del mes seleccionado)
        val categories: List<CategoryData> = if (selectedAccount == "Todas") {
            db.categoryDao().getCategoriesFromAllAccounts(
                idUser, MovementType.Ingreso, selectedMonth, selectedYear
            ) + db.categoryDao().getCategoriesFromAllAccounts(
                idUser, MovementType.Gasto, selectedMonth, selectedYear
            )
        } else {
            db.categoryDao().getCategoriesFromOneAccount(
                idUser, selectedAccount, MovementType.Ingreso, selectedMonth, selectedYear
            ) + db.categoryDao().getCategoriesFromOneAccount(
                idUser, selectedAccount, MovementType.Gasto, selectedMonth, selectedYear
            )
        }
        categoryAdapter.updateCategories(categories, categories.sumOf { it.total.toDouble() })

        // Balance mensual
        val ingreso = db.movementDao()
            .getTotalByType(idUser, selectedAccount, MovementType.Ingreso, selectedMonth, selectedYear) ?: 0.0
        val gasto = db.movementDao()
            .getTotalByType(idUser, selectedAccount, MovementType.Gasto, selectedMonth, selectedYear) ?: 0.0
        val ingresoAnterior = db.movementDao()
            .getTotalByTypeBeforeMonth(idUser, selectedAccount, MovementType.Ingreso, selectedMonth, selectedYear) ?: 0.0
        val gastoAnterior = db.movementDao()
            .getTotalByTypeBeforeMonth(idUser, selectedAccount, MovementType.Gasto, selectedMonth, selectedYear) ?: 0.0

        val saldoAnterior = ingresoAnterior - gastoAnterior
        val saldoActual = saldoAnterior + ingreso - gasto

        tvIngreso.text = "$${"%.2f".format(ingreso)}"
        tvSaldoAnterior.text = "$${"%.2f".format(saldoAnterior)}"
        tvGasto.text = "$${"%.2f".format(gasto)}"
        tvSaldoActual.text = "$${"%.2f".format(saldoActual)}"
    }


}