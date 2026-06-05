package com.example.proyectos2gastospersonales

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.sql.Date
import java.util.Calendar

class ModifyMovementActivity : BaseActivity(), AdapterView.OnItemSelectedListener {

    var idUser = 1 // TEMPORAL

    val db by lazy { AppDatabase.getDatabase(this) }

    private lateinit var spinnerCuenta: Spinner
    private lateinit var spinnerCategoria: Spinner

    private lateinit var iconCuenta: ImageView
    private lateinit var iconCategoria: ImageView

    private lateinit var sectionIngresoGasto: LinearLayout

    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etFecha: TextInputEditText

    private val selectedDate: Calendar = Calendar.getInstance()

    private var accounts: List<Account> = emptyList()
    private var categories: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_modify_movement)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //setupDrawer("Movimientos", R.layout.activity_add_movement)

        val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)

        idUser = sharedPreferences.getInt("user_id", -1)


        // Recibido desde Pantalla #7
        val movement =
            db.movementDao().getMovement(intent.getStringExtra("movement_id")?.toInt() ?: 1)

        // Referencias a vistas
        spinnerCuenta = findViewById(R.id.spinner_cuenta)
        spinnerCategoria = findViewById(R.id.spinner_categoria)

        iconCuenta = findViewById(R.id.icon_cuenta)
        iconCategoria = findViewById(R.id.icon_categoria)

        sectionIngresoGasto = findViewById(R.id.section_ingreso_gasto)

        etAmount = findViewById(R.id.et_amount)
        etDescription = findViewById(R.id.et_description)
        etFecha = findViewById(R.id.et_fecha)

        // Botón regresar
        findViewById<ImageButton>(R.id.back_button).setOnClickListener { finish() }

        // Cargar datos desde la BD
        accounts = db.accountDao().getAccountsFromUser(idUser)?.accounts ?: emptyList()
        categories = db.categoryDao().getAllCategoriesFromUser(idUser)?.categories ?: emptyList()

        setupSpinnersCuenta()
        spinnerCuenta.setSelection(movement.accountId)
        setupSpinnerCategoria()
        spinnerCategoria.setSelection(movement.categoryId)
        setupFecha()

        // Botón Guardar
        findViewById<Button>(R.id.btn_guardar).setOnClickListener { guardarMovimiento() }
    }

    // ─── Setup Spinners ───────────────────────────────────────────────────────
    private fun setupSpinnersCuenta() {
        val accountNames = accounts.map { it.name }

        val adapterCuenta = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountNames)
        adapterCuenta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCuenta.adapter = adapterCuenta
        spinnerCuenta.onItemSelectedListener = this
    }

    private fun setupSpinnerCategoria() {
        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter
        spinnerCategoria.onItemSelectedListener = this
    }

    private fun setupFecha() {
        updateDateField()
        etFecha.setOnClickListener { showDatePicker() }
        findViewById<TextInputLayout>(R.id.til_fecha).setEndIconOnClickListener { showDatePicker() }
    }

    // ─── Actualizar íconos ────────────────────────────────────────────────────

    private fun updateIcons() {
        if (accounts.isNotEmpty()) {
            val cuentaPos = spinnerCuenta.selectedItemPosition.coerceIn(0, accounts.lastIndex)
            iconCuenta.setImageResource(getIconResource(accounts[cuentaPos].icon))
        }
        if (categories.isNotEmpty()) {
            val catPos = spinnerCategoria.selectedItemPosition.coerceIn(0, categories.lastIndex)
            iconCategoria.setImageResource(getIconResource(categories[catPos].icon))
        }
    }

    private fun getIconResource(icon: Int): Int = when (icon) {
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
        201 -> R.drawable.baseline_shopping_cart_24
        202 -> R.drawable.baseline_directions_car_24
        203 -> R.drawable.baseline_restaurant_24
        204 -> R.drawable.baseline_home_24
        205 -> R.drawable.baseline_computer_24
        206 -> R.drawable.baseline_pets_24
        207 -> R.drawable.baseline_school_24
        208 -> R.drawable.baseline_medical_services_24
        209 -> R.drawable.baseline_shopping_bag_24
        210 -> R.drawable.baseline_work_24
        0 -> R.drawable.ic_android_black_24dp
        1 -> R.drawable.outline_1k_24
        else -> R.drawable.ic_launcher_foreground
    }

    // ─── DatePicker ───────────────────────────────────────────────────────────

    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)
        val dialog = DatePickerDialog(this, { _, y, m, d ->
            selectedDate.set(y, m, d)
            updateDateField()
        }, year, month, day)
        val minCal = Calendar.getInstance().apply { set(2020, Calendar.JANUARY, 1) }
        val maxCal = Calendar.getInstance().apply { set(2026, Calendar.DECEMBER, 31) }
        dialog.datePicker.minDate = minCal.timeInMillis
        dialog.datePicker.maxDate = maxCal.timeInMillis
        dialog.show()
    }

    private fun updateDateField() {
        val y = selectedDate.get(Calendar.YEAR)
        val m = selectedDate.get(Calendar.MONTH) + 1
        val d = selectedDate.get(Calendar.DAY_OF_MONTH)
        etFecha.setText(String.format("%04d-%02d-%02d", y, m, d))
    }

    // ─── Guardar movimiento ───────────────────────────────────────────────────

    private fun guardarMovimiento() {
        val amountText = etAmount.text.toString().trim()
        if (amountText.isEmpty()) {
            etAmount.error = "Ingresa una cantidad"
            return
        }
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            etAmount.error = "Cantidad inválida"
            return
        }

        val description = etDescription.text.toString().trim().ifEmpty { "Sin descripción" }
        val date = Date(selectedDate.timeInMillis)
        val account = accounts[spinnerCuenta.selectedItemPosition]
        val category = categories[spinnerCategoria.selectedItemPosition]
        val movement =
            db.movementDao().getMovement(intent.getStringExtra("movement_id")?.toInt() ?: 1)
        val newMovement =
            Movement(
                movement.id, movement.type, amount, account.id, category.id,
                description, date, movement.originAccountId, movement.destinyAccountId, movement.userId
            )
        db.movementDao().updateMovement(newMovement)

        Toast.makeText(this, "Movimiento actualizado", Toast.LENGTH_SHORT).show()
        finish()

    }


    override fun onItemSelected(
        p0: AdapterView<*>?,
        p1: View?,
        p2: Int,
        p3: Long
    ) {
        updateIcons()
    }

// ─── Spinner listeners ────────────────────────────────────────────────────

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}
