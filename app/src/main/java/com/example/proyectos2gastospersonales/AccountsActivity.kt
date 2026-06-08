package com.example.proyectos2gastospersonales

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AccountsAdapter (var accounts: MutableList<Account>, val activity: AppCompatActivity) :

    RecyclerView.Adapter<AccountsAdapter.AccountsViewHolder>() {
    val db = AppDatabase.getDatabase(activity)

    class AccountsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val accountName: TextView = view.findViewById(R.id.account_name)
        val accountDesc: TextView = view.findViewById(R.id.account_description)
        var accountImage: ImageView = view.findViewById(R.id.image_account)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.accounts_item, parent, false)
        val vh = AccountsViewHolder(view)

        return vh
    }

    override fun onBindViewHolder(holder: AccountsViewHolder, position: Int) {
        val account = accounts[position]

        holder.accountName.text = account.name
        holder.accountDesc.text = account.description

        val accountResource = when(account.icon) {

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

        holder.accountImage.setImageResource(accountResource)
        holder.itemView.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.accounts_context_menu, popup.menu)

            activity.lifecycleScope.launch {
                val count = db.movementDao().countMovementsByAccount(account.id)
                val hasMovements = count > 0

                val deleteItem = popup.menu.findItem(R.id.eliminate_account)
                deleteItem.isVisible = !hasMovements

                popup.setOnMenuItemClickListener { menuItem ->
                    when(menuItem.itemId) {

                        R.id.modify_account -> {
                            val intent = Intent(view.context, AddEditAccountActivity::class.java)
                            intent.putExtra("ACCOUNT_ID", account.id)
                            view.context.startActivity(intent)
                            true
                        }

                        R.id.eliminate_account -> {
                            eliminarCuenta(position, view)
                            true
                        }

                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    override fun getItemCount(): Int = accounts.size

    private fun eliminarCuenta(position: Int, view: View){
        val accountToDelete = accounts[position]

        accounts.removeAt(position)
        notifyItemRemoved(position)

        Snackbar.make(
            view,
            "Cuenta eliminada",
            Snackbar.LENGTH_LONG
        ).setAction("Deshacer") {
            accounts.add(position, accountToDelete)
            notifyItemInserted(position)
        }.addCallback(object : com.google.android.material.snackbar.BaseTransientBottomBar.
        BaseCallback<Snackbar>(){
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event != DISMISS_EVENT_ACTION) {
                    activity.lifecycleScope.launch {
                        db.accountDao().deleteAccount(accountToDelete)
                    }
                }
            }
        }).show()
    }
}


class AccountsActivity : BaseActivity(), PopupMenu.OnMenuItemClickListener {

    private lateinit var appBarMenu: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var rv: RecyclerView
    private lateinit var emptyViewAlert: TextView
    private lateinit var accountAdapter: AccountsAdapter
    private var accountsList: MutableList<Account> = mutableListOf()
    private val db by lazy { AppDatabase.getDatabase(this) }

    var idUser = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_accounts)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupDrawer("Cuentas", R.layout.activity_accounts)

        backButton = findViewById(R.id.backButton)

        appBarMenu = findViewById(R.id.appbar_menu_accounts)

        backButton.setOnClickListener { finish() }

        appBarMenu.setOnClickListener { view -> showMenu(view) }

        rv = findViewById(R.id.accounts_rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        accountAdapter = AccountsAdapter(accountsList, this)
        rv.adapter = accountAdapter

        emptyViewAlert = findViewById(R.id.empty_view_alert)
    }

    override fun onResume() {
        super.onResume()
        cargarCuentas()
    }

    fun showMenu(v: View) {
        PopupMenu(this, v).apply {
            setOnMenuItemClickListener(this@AccountsActivity)
            inflate(R.menu.accounts_appbar_menu)
            show()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_acount -> {
                val intent = Intent(this, AddEditAccountActivity::class.java)
                startActivity(intent)
                true
            }
            else -> false
        }
    }

    private fun cargarCuentas(){
        val sharedPreferences = getSharedPreferences("session", MODE_PRIVATE)
        idUser = sharedPreferences.getInt("user_id", -1)

        val result = db.accountDao().getAccountsFromUser(userId = idUser)
        val cuentas = result?.accounts ?: emptyList()

        accountsList.clear()
        accountsList.addAll(cuentas)
        accountAdapter.notifyDataSetChanged()

        if (accountsList.isEmpty()) {
            rv.visibility = View.GONE
            emptyViewAlert.visibility = View.VISIBLE

        } else {
            rv.visibility = View.VISIBLE
            emptyViewAlert.visibility = View.GONE
        }
    }
}
