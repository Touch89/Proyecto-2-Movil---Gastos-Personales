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

class AddMovementActivity : BaseActivity(), AdapterView.OnItemSelectedListener {

    var idUser = 1 // TEMPORAL

    val db by lazy { AppDatabase.getDatabase(this) }

    private lateinit var spinnerType: Spinner
    private lateinit var spinnerCuenta: Spinner
    private lateinit var spinnerCategoria: Spinner
    private lateinit var spinnerCuentaOrigen: Spinner
    private lateinit var spinnerCuentaDestino: Spinner

    private lateinit var iconCuenta: ImageView
    private lateinit var iconCategoria: ImageView
    private lateinit var iconCuentaOrigen: ImageView
    private lateinit var iconCuentaDestino: ImageView

    private lateinit var sectionIngresoGasto: LinearLayout
    private lateinit var sectionTransferencia: LinearLayout

    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etFecha: TextInputEditText

    private val selectedDate: Calendar = Calendar.getInstance()

    private val spinnerTypes = arrayOf("Ingreso", "Gasto", "Transferencia")

    private var accounts: List<Account> = emptyList()
    private var categories: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_movement)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupDrawer("Movimientos", R.layout.activity_add_movement)

        val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)

        idUser = sharedPreferences.getInt("user_id", -1)


        // Tipo recibido desde Pantalla #3
        val movementTypeName = intent.getStringExtra("movement_type") ?: MovementType.Gasto.name
        val initialType = MovementType.valueOf(movementTypeName)

        // Referencias a vistas
        spinnerType = findViewById(R.id.spinner_type)
        spinnerCuenta = findViewById(R.id.spinner_cuenta)
        spinnerCategoria = findViewById(R.id.spinner_categoria)
        spinnerCuentaOrigen = findViewById(R.id.spinner_cuenta_origen)
        spinnerCuentaDestino = findViewById(R.id.spinner_cuenta_destino)

        iconCuenta = findViewById(R.id.icon_cuenta)
        iconCategoria = findViewById(R.id.icon_categoria)
        iconCuentaOrigen = findViewById(R.id.icon_cuenta_origen)
        iconCuentaDestino = findViewById(R.id.icon_cuenta_destino)

        sectionIngresoGasto = findViewById(R.id.section_ingreso_gasto)
        sectionTransferencia = findViewById(R.id.section_transferencia)

        etAmount = findViewById(R.id.et_amount)
        etDescription = findViewById(R.id.et_description)
        etFecha = findViewById(R.id.et_fecha)

        // Botón regresar
        findViewById<ImageButton>(R.id.back_button).setOnClickListener { finish() }

        // Cargar datos desde la BD
        accounts = db.accountDao().getAccountsFromUser(idUser)?.accounts ?: emptyList()
        categories = db.categoryDao().getAllCategoriesFromUser(idUser).categories

        setupSpinnerTipo(initialType)
        setupSpinnersCuenta()
        setupSpinnerCategoria()
        setupFecha()

        // Botón Guardar
        findViewById<Button>(R.id.btn_guardar).setOnClickListener { guardarMovimiento() }

        // Visibilidad inicial de secciones
        updateSectionVisibility(initialType)
    }

    // ─── Setup Spinners ───────────────────────────────────────────────────────

    private fun setupSpinnerTipo(initialType: MovementType) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter
        spinnerType.setSelection(spinnerTypes.indexOf(initialType.name))
        spinnerType.onItemSelectedListener = this
    }

    private fun setupSpinnersCuenta() {
        val accountNames = accounts.map { it.name }

        val adapterCuenta = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountNames)
        adapterCuenta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCuenta.adapter = adapterCuenta
        spinnerCuenta.onItemSelectedListener = this

        val adapterOrigen = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountNames)
        adapterOrigen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCuentaOrigen.adapter = adapterOrigen
        spinnerCuentaOrigen.onItemSelectedListener = this

        val adapterDestino = ArrayAdapter(this, android.R.layout.simple_spinner_item, accountNames)
        adapterDestino.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCuentaDestino.adapter = adapterDestino
        spinnerCuentaDestino.onItemSelectedListener = this
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

    // ─── Lógica de visibilidad ────────────────────────────────────────────────

    private fun updateSectionVisibility(type: MovementType) {
        if (type == MovementType.Transferencia) {
            sectionIngresoGasto.visibility = View.GONE
            sectionTransferencia.visibility = View.VISIBLE
        } else {
            sectionIngresoGasto.visibility = View.VISIBLE
            sectionTransferencia.visibility = View.GONE
        }
    }

    // ─── Actualizar íconos ────────────────────────────────────────────────────

    private fun updateIcons() {
        if (accounts.isNotEmpty()) {
            val cuentaPos = spinnerCuenta.selectedItemPosition.coerceIn(0, accounts.lastIndex)
            iconCuenta.setImageResource(getIconResource(accounts[cuentaPos].icon))

            val origenPos = spinnerCuentaOrigen.selectedItemPosition.coerceIn(0, accounts.lastIndex)
            iconCuentaOrigen.setImageResource(getIconResource(accounts[origenPos].icon))

            val destinoPos = spinnerCuentaDestino.selectedItemPosition.coerceIn(0, accounts.lastIndex)
            iconCuentaDestino.setImageResource(getIconResource(accounts[destinoPos].icon))
        }
        if (categories.isNotEmpty()) {
            val catPos = spinnerCategoria.selectedItemPosition.coerceIn(0, categories.lastIndex)
            iconCategoria.setImageResource(getIconResource(categories[catPos].icon))
        }
    }

    private fun getIconResource(icon: Int): Int = when (icon) {
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

        val selectedTypeName = spinnerType.selectedItem?.toString() ?: return
        val type = MovementType.valueOf(selectedTypeName)
        val description = etDescription.text.toString().trim().ifEmpty { "Sin descripción" }
        val date = Date(selectedDate.timeInMillis)
        val nextId = db.movementDao().getNextId()

        val movement: Movement = when (type) {
            MovementType.Transferencia -> {
                if (accounts.size < 2) {
                    Toast.makeText(this, "Se necesitan al menos 2 cuentas para transferir", Toast.LENGTH_SHORT).show()
                    return
                }
                val origen = accounts[spinnerCuentaOrigen.selectedItemPosition]
                val destino = accounts[spinnerCuentaDestino.selectedItemPosition]
                if (origen.id == destino.id) {
                    Toast.makeText(this, "La cuenta origen y destino deben ser diferentes", Toast.LENGTH_SHORT).show()
                    return
                }
                Movement(
                    id = nextId, type = type, amount = amount,
                    accountId = origen.id, categoryId = 0,
                    description = description, date = date,
                    originAccountId = origen.id, destinyAccountId = destino.id,
                    userId = idUser
                )
            }
            else -> {
                if (accounts.isEmpty()) {
                    Toast.makeText(this, "No hay cuentas disponibles", Toast.LENGTH_SHORT).show()
                    return
                }
                if (categories.isEmpty()) {
                    Toast.makeText(this, "No hay categorías disponibles", Toast.LENGTH_SHORT).show()
                    return
                }
                val account = accounts[spinnerCuenta.selectedItemPosition]
                val category = categories[spinnerCategoria.selectedItemPosition]
                Movement(
                    id = nextId, type = type, amount = amount,
                    accountId = account.id, categoryId = category.id,
                    description = description, date = date,
                    originAccountId = null, destinyAccountId = null,
                    userId = idUser
                )
            }
        }

        db.movementDao().insertMovement(movement)
        Toast.makeText(this, "Movimiento guardado", Toast.LENGTH_SHORT).show()
        finish()
    }

    // ─── Spinner listeners ────────────────────────────────────────────────────

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent?.id == R.id.spinner_type) {
            val selectedTypeName = spinnerType.selectedItem?.toString() ?: return
            updateSectionVisibility(MovementType.valueOf(selectedTypeName))
        }
        updateIcons()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}
