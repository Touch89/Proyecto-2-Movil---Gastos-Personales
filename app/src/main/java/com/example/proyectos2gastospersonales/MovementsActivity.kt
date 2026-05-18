package com.example.proyectos2gastospersonales

import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlin.Int
import kotlin.String
import kotlin.toString


class MovementsActivity : BaseActivity(), AdapterView.OnItemSelectedListener, PopupMenu.OnMenuItemClickListener {

    var idUser =
        1 //TEMPORAL, si la llaman userId y la dejan pública la app se muere, ni idea de por qué
    var accountId = 1 //TEMPORAL
    var selectedMonth = "0"
    var selectedYear = "1970"
    var selectedType = "Gasto"
    var selectedAccount = "Todas"
    private lateinit var filterButton: ImageButton
    private var movementsList: MutableList<MovementItemData> = mutableListOf()
    private var itemPosition = -1


    val db by lazy { AppDatabase.getDatabase(this) }
    private lateinit var rv: RecyclerView
    private lateinit var movementAdapter: MovementAdapter
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

    val spinnerYear = (2020..2030).toList().toTypedArray()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_movements)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupDrawer("Movimientos", R.layout.activity_movements)

        val userAccounts = db.accountDao().getAccountsFromUser(idUser)?.accounts ?: emptyList()

        val accountNameList = mutableListOf<String>()

        accountNameList.add("Todas")

        userAccounts.forEach { accountNameList.add(it.name) }

        rv = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
        rv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        val itemAccountSpinner = findViewById<Spinner>(R.id.spinner_account)

        val itemYearSpinner = findViewById<Spinner>(R.id.spinner_year)

        val itemMonthSpinner = findViewById<Spinner>(R.id.spinner_month)

        val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)
        idUser = sharedPreferences.getInt("user_id", -1)

        val passedYear = intent.getStringExtra("selected_year")
        val passedMonth = intent.getStringExtra("selected_month")
        val passedAccount = intent.getStringExtra("selected_account")

        backButton = findViewById(R.id.rbc_backButton)
        filterButton = findViewById(R.id.movFilterButton)

        backButton.setOnClickListener {
            finish()
        }

        filterButton.setOnClickListener { view ->
            showMenu(view)
        }

        movementsList = db.movementDao().getMovementDataByCategoryByDateOrder(
            idUser,
            "*",
            MovementType.valueOf(selectedType),
            selectedYear,
            selectedMonth
        ).toMutableList()

        movementAdapter = MovementAdapter(movementsList, this)
        rv.adapter = movementAdapter

        updateAmount()

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

        itemAccountSpinner.adapter = accountAdapter
        itemYearSpinner.adapter = yearAdapter
        itemMonthSpinner.adapter = monthAdapter
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

        movementAdapter = MovementAdapter(mutableListOf(), this)
        rv.adapter = movementAdapter
    }

    fun showMenu(v: View) {
        PopupMenu(this, v).apply {
            setOnMenuItemClickListener(this@MovementsActivity)
            inflate(R.menu.movement_filter_menu)
            show()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filterByDate -> {
                if (selectedAccount == "Todas") {
                    movementAdapter.updateMovements(db.movementDao().getMovementsByDate(idUser, this.selectedYear, this.selectedMonth))
                } else {
                    movementAdapter.updateMovements(db.movementDao().getMovementsByDate(idUser, this.selectedYear, this.selectedMonth))
                }
                true
            }

            R.id.action_filterByAccount -> {
                if (selectedAccount == "Todas") {
                    movementsList = db.movementDao().getMovementsByDateByAccount(idUser, "Todas", this.selectedYear, this.selectedMonth) as MutableList<MovementItemData>
                    movementAdapter.updateMovements(movementsList)
                } else {
                    movementAdapter.updateMovements(db.movementDao().getMovementsByDateByAccount(idUser, selectedType.toString(), this.selectedYear, this.selectedMonth))
                }
                true
            }

            R.id.action_filterByAmount -> {
                if (selectedAccount == "Todas") {
                    movementsList =
                        db.movementDao().getMovementsByAmount(idUser, this.selectedYear,  this.selectedMonth) as MutableList<MovementItemData>
                    movementAdapter.updateMovements(movementsList)
                } else {
                    movementsList =
                        db.movementDao().getMovementsByAmount(idUser, this.selectedYear, this.selectedMonth) as MutableList<MovementItemData>
                    movementAdapter.updateMovements(movementsList)
                }
                true
            }

            else -> false
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.movement_context_menu, menu)

        val holder = rv.getChildViewHolder(v!!) as MovementAdapter.ViewHolder
        itemPosition = holder.adapterPosition
    }

    override fun onResume() {
        super.onResume()
        updateRView()
    }

    fun updateRView() {
        val itemAccountSpinner = findViewById<Spinner>(R.id.spinner_account)
        val itemYearSpinner = findViewById<Spinner>(R.id.spinner_year)
        val itemMonthSpinner = findViewById<Spinner>(R.id.spinner_month)

        val selectedAccount = itemAccountSpinner.selectedItem.toString()
        val sTypeAsEnum = if (selectedType == "Gasto") MovementType.Gasto else MovementType.Ingreso

        val selectedYear = itemYearSpinner.selectedItem.toString()
        val selectedMonth = String.format("%02d", itemMonthSpinner.selectedItemPosition + 1)

        this.selectedType = selectedType
        this.selectedMonth = selectedMonth
        this.selectedYear = selectedYear

        if (selectedAccount == "Todas") {
            movementsList = db.movementDao().getMovementsByDate(idUser, this.selectedYear, this.selectedMonth) as MutableList<MovementItemData>
            movementAdapter.updateMovements(movementsList)
        } else {
            movementsList = db.movementDao().getMovementsByDateByAccount(idUser, selectedType.toString(), this.selectedYear, this.selectedMonth) as MutableList<MovementItemData>
            movementAdapter.updateMovements(movementsList)
        }
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        updateRView()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onContextItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_modify -> {
            // TODO
            true
        }

        R.id.action_delete -> {
            val tempMovement = movementsList[itemPosition]
            val tempPosition = itemPosition
            val movCopy: Movement = db.movementDao().getMovement(tempMovement.movId)
            db.movementDao().deleteMovementById(tempMovement.movId)
            movementsList.removeAt(itemPosition)
            movementAdapter.notifyDataSetChanged()
            updateAmount()

            val snackbar = Snackbar
                .make(rv, "Movimiento eliminado", Snackbar.LENGTH_LONG)
                .setAction("Deshacer") {
                    movementsList.add(tempPosition, tempMovement)
                    db.movementDao().insertMovement(movCopy)
                    movementAdapter.notifyDataSetChanged()
                    updateAmount()
                    Toast.makeText(
                        this,
                        "Movimiento restaurado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            snackbar.show()
            true
        }

        else -> {
            super.onContextItemSelected(item)
        }
    }

    private fun updateAmount() {
    }
}
