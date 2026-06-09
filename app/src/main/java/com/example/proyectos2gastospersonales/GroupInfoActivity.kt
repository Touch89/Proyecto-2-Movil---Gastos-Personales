package com.example.proyectos2gastospersonales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

data class PaymentItemData(
    val movId: String,
    val memberId: String,
)

class PaymentListAdapter(
    private val payment: MutableList<PaymentItemData>,
    val activity: AppCompatActivity
) :
    RecyclerView.Adapter<PaymentListAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val itemDate: TextView
        private val itemMember: TextView
        private val itemDesc: TextView
        private val itemAmount: TextView
        private lateinit var payment: PaymentItemData

        init {
            itemDate = view.findViewById(R.id.item_date)
            itemMember = view.findViewById(R.id.item_member_name)
            itemDesc = view.findViewById(R.id.item_date)
            itemAmount = view.findViewById(R.id.item_amount)
        }

        fun bind(payment: PaymentItemData) {
            val db by lazy { AppDatabase.getDatabase(GroupInfoActivity()) }
            this.payment = payment
            val movement = db.movementDao().getMovement(payment.movId.toInt())
            itemDate.text = movement.date.toString()
            itemMember.text = db.userDao().getUser(payment.memberId.toInt()).username
            itemDesc.text = movement.description.toString()
            itemAmount.text = "$${movement.date}"
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_group_payments, parent, false)

        activity.registerForContextMenu(view)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(payment[position])

    override fun getItemCount(): Int = payment.size

    fun updatePayments(updatedPayments: List<PaymentItemData>) {
        payment.clear()
        payment.addAll(updatedPayments)
        notifyDataSetChanged()
    }
}

class GroupInfoActivity : AppCompatActivity() {
    val db by lazy { AppDatabase.getDatabase(this) }
    private lateinit var rv: RecyclerView
    private lateinit var paymentAdapter: PaymentListAdapter
    private var movementsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_group_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val sharedPreferences =
            getSharedPreferences("session", MODE_PRIVATE)

        val userId =
            sharedPreferences.getInt("user_id", -1)

        val groupId = (intent.getStringExtra("groupId")).toString()


        rv = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        rv.setHasFixedSize(true)
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        paymentAdapter = PaymentListAdapter(mutableListOf(), this)
        rv.adapter = paymentAdapter

        realtimeDatabase(groupId)
    }

    private fun realtimeDatabase(groupId: String) {
        val movementsReference = Firebase.database.reference
            .child("grupos")
            .child(groupId)
            .child("movements")

        movementsListener = movementsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedPayments = mutableListOf<PaymentItemData>()

                if (snapshot.exists()) {
                    for (paymentSnapshot in snapshot.children) {
                        val movId =
                            paymentSnapshot.child("movId").value?.toString() ?: paymentSnapshot.key
                            ?: ""
                        val memberId = paymentSnapshot.child("memberId").value?.toString() ?: ""

                        updatedPayments.add(PaymentItemData(movId, memberId))
                    }
                }

                paymentAdapter.updatePayments(updatedPayments)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        movementsListener?.let {
            val groupId = intent.getStringExtra("groupId").toString()
            Firebase.database.reference.child("grupos").child(groupId).child("movements")
                .removeEventListener(it)
        }
    }
}