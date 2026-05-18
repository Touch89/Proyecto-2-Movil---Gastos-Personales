package com.example.proyectos2gastospersonales

import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
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
import com.google.android.material.snackbar.Snackbar
import kotlin.Int
import kotlin.String

class MovementListAdapter(
    private val movement: MutableList<MovementItemData>,
    val activity: AppCompatActivity
) :
    RecyclerView.Adapter<MovementListAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val itemDate: TextView
        private val itemAccountIcon: ImageView
        private val itemAccount: TextView
        private val itemCategoryImage: ImageView
        private val itemCategory: TextView
        private val itemAmount: TextView
        private lateinit var movement: MovementItemData

        init {
            itemDate = view.findViewById(R.id.item_date)
            itemAccountIcon = view.findViewById(R.id.item_account_icon)
            itemAccount = view.findViewById(R.id.item_account_name)
            itemCategoryImage = view.findViewById(R.id.item_category_icon)
            itemCategory = view.findViewById(R.id.item_category)
            itemAmount = view.findViewById(R.id.item_amount)
        }

        fun bind(movement: MovementItemData) {
            this.movement = movement
            itemDate.text = movement.movDate.toString()
            itemAccount.text = movement.accName
            itemAccountIcon.setImageResource(
                when (movement.accIcon) {
                    101 -> R.drawable.baseline_account_balance_wallet_24
                    102 -> R.drawable.baseline_credit_card_24
                    103 -> R.drawable.baseline_payment_24
                    104 -> R.drawable.baseline_account_balance_24
                    105 -> R.drawable.baseline_savings_24
                    106 -> R.drawable.baseline_local_activity_24
                    107 -> R.drawable.baseline_phone_android_24
                    108 -> R.drawable.baseline_trending_up_24
                    109 -> R.drawable.baseline_lock_24
                    110 -> R.drawable.baseline_monetization_on_24

                    else -> R.drawable.ic_launcher_foreground
                }
            )
            itemCategoryImage.setImageResource(
                when (101) {
                    101 -> R.drawable.baseline_account_balance_wallet_24
                    102 -> R.drawable.baseline_credit_card_24
                    103 -> R.drawable.baseline_payment_24
                    104 -> R.drawable.baseline_account_balance_24
                    105 -> R.drawable.baseline_savings_24
                    106 -> R.drawable.baseline_local_activity_24
                    107 -> R.drawable.baseline_phone_android_24
                    108 -> R.drawable.baseline_trending_up_24
                    109 -> R.drawable.baseline_lock_24
                    110 -> R.drawable.baseline_monetization_on_24

                    else -> R.drawable.ic_launcher_foreground
                }
            )
            itemCategory.text = movement.movDesc
            itemAmount.text = "$${movement.movAmount}"
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.movements_list_item, parent, false)

        activity.registerForContextMenu(view)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(movement[position])

    override fun getItemCount(): Int = movement.size

    fun updateMovements(updatedMovements: List<MovementItemData>) {
        movement.clear()
        movement.addAll(updatedMovements)
        notifyDataSetChanged()
    }
}
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
    private lateinit var movementAdapter: MovementListAdapter
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

        movementAdapter = MovementListAdapter(movementsList, this)
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

        movementAdapter = MovementListAdapter(mutableListOf(), this)
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
