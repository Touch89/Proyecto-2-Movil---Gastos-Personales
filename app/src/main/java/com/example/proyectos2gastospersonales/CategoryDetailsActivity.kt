package com.example.proyectos2gastospersonales

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
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
import java.sql.Date
import kotlin.math.roundToInt

data class MovementItemData(
    val movId: Int,
    val accIcon: Int,
    val accName: String,
    val movDate: Date,
    val movDesc: String,
    val movAmount: Double
)

class MovementAdapter(
    private val movement: MutableList<MovementItemData>,
    val activity: CategoryDetailsActivity
) :
    RecyclerView.Adapter<MovementAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val itemImage: ImageView
        private val itemAccount: TextView
        private val itemDate: TextView
        private val itemDescription: TextView
        private val itemAmount: TextView
        private lateinit var movement: MovementItemData

        init {
            itemAccount = view.findViewById(R.id.item_account_name)
            itemAmount = view.findViewById(R.id.item_amount)
            itemImage = view.findViewById(R.id.item_account_icon)
            itemDate = view.findViewById(R.id.item_date)
            itemDescription = view.findViewById(R.id.item_description)
        }

        fun bind(movement: MovementItemData) {
            this.movement = movement
            itemAccount.text = movement.accName
            itemAmount.text = "$${movement.movAmount}"
            itemDate.text = movement.movDate.toString()
            itemDescription.text = movement.movDesc
            itemImage.setImageResource(
                when (movement.accIcon) {
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
            LayoutInflater.from(parent.context)
                .inflate(R.layout.categorymovementslist_item, parent, false)

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

class CategoryDetailsActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener{
    private lateinit var typeText: TextView
    private lateinit var totalSumText: TextView
    private lateinit var categoryText: TextView
    private lateinit var backButton: ImageButton
    private lateinit var filterButton: ImageButton

    val db by lazy { AppDatabase.getDatabase(this) }
    private lateinit var rv: RecyclerView
    private lateinit var movementAdapter: MovementAdapter
    private var movementsList: MutableList<MovementItemData> = mutableListOf()
    private var itemPosition = -1

    private var idUser: Int = -999
    private var categoryName: String? = ""
    private var selectedMonth: String? = ""
    private var selectedYear: String? = ""
    private var selectedType: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        idUser = intent.getIntExtra("user_id", -999)
        categoryName = intent.getStringExtra("category_name")
        selectedMonth = intent.getStringExtra("selected_month")
        selectedYear = intent.getStringExtra("selected_year")
        selectedType = intent.getStringExtra("selected_type")

        rv = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        backButton = findViewById(R.id.backButton)
        filterButton = findViewById(R.id.movFilterButton)

        totalSumText = findViewById(R.id.total_categoria)
        typeText = findViewById(R.id.movTipo)
        categoryText = findViewById(R.id.nombre_categoria)

        typeText.text = selectedType
        categoryText.text = categoryName

        backButton.setOnClickListener {
            finish()
        }

        filterButton.setOnClickListener { view ->
            showMenu(view)
        }

        movementsList = db.movementDao().getMovementDataByCategoryByDateOrder(
            idUser,
            categoryName.toString(),
            MovementType.valueOf(selectedType.toString()),
            selectedYear.toString(),
            selectedMonth.toString()
        ).toMutableList()

        movementAdapter = MovementAdapter(movementsList, this)
        rv.adapter = movementAdapter

        updateAmount()
    }

    fun showMenu(v: View) {
        PopupMenu(this, v).apply {
            setOnMenuItemClickListener(this@CategoryDetailsActivity)
            inflate(R.menu.movement_filter_menu)
            show()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filterByDate -> {
                val updatedData = db.movementDao().getMovementDataByCategoryByDateOrder(
                    idUser,
                    categoryName.toString(),
                    MovementType.valueOf(selectedType.toString()),
                    selectedYear.toString(),
                    selectedMonth.toString()
                ).toMutableList()
                movementAdapter.updateMovements(updatedData)
                updateAmount()
                true

            }

            R.id.action_filterByAccount -> {
                val updatedData = db.movementDao().getMovementDataByCategoryByAccountOrder(
                    idUser,
                    categoryName.toString(),
                    MovementType.valueOf(selectedType.toString()),
                    selectedYear.toString(),
                    selectedMonth.toString()
                ).toMutableList()
                movementAdapter.updateMovements(updatedData)
                updateAmount()
                true
            }

            R.id.action_filterByAmount -> {
                val updatedData = db.movementDao().getMovementDataByCategoryByAmountOrder(
                    idUser,
                    categoryName.toString(),
                    MovementType.valueOf(selectedType.toString()),
                    selectedYear.toString(),
                    selectedMonth.toString()
                ).toMutableList()
                movementAdapter.updateMovements(updatedData)
                updateAmount()
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

    // FLOATING CONTEXT MENU
    override fun onContextItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_modify -> {
            Toast.makeText(this, movementsList[itemPosition].accName, Toast.LENGTH_SHORT).show()
            true
        }

        R.id.action_delete -> {
            val tempMovement = movementsList[itemPosition]
            val tempPosition = itemPosition
            val movCopy: Movement = db.movementDao().getMovement(tempMovement.movId)
            db.movementDao().deleteMovementById(tempMovement.movId)
            // Agarrar la id del mov, guardar una copia del mov y luego hacer el insert a la bdd***
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

        else -> super.onContextItemSelected(item)
    }

    private fun updateAmount() {
        val updatedAmount = movementsList.sumOf { it.movAmount }
        totalSumText.text = "$${updatedAmount}"
    }
}
