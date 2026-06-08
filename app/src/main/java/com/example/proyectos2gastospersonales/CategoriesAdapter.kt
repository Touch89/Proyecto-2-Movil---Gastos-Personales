package com.example.proyectos2gastospersonales

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter para el RecyclerView de la pantalla #11 (Categorías)
// Recibe la lista de categorías y un listener que delega las acciones al Activity
class CategoriesAdapter(
    private val categories: MutableList<Category>,
    private val listener: CategoryActionListener
) : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

    // Interfaz que CategoriesActivity implementa para manejar las acciones del menú de cada ítem
    interface CategoryActionListener {
        fun onModify(category: Category)
        fun onDelete(category: Category)
        fun hasMovements(categoryId: Int): Boolean
    }

    // ViewHolder que representa un ítem de la lista: ícono y nombre de la categoría
    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.item_category_icon)
        private val name: TextView = view.findViewById(R.id.item_category_name)
        private lateinit var category: Category

        // Enlaza los datos de una categoría con las vistas del ítem
        fun bind(category: Category) {
            this.category = category
            name.text = category.name
            // Mapea el código numérico del ícono (201-210) al drawable correspondiente
            icon.setImageResource(
                when (category.icon) {
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
                    else -> R.drawable.ic_launcher_foreground
                }
            )
            view.setOnClickListener { showPopupMenu(view, category) }
        }

        // Muestra el PopupMenu al tocar el ítem; oculta "Eliminar" si la categoría tiene movimientos
        private fun showPopupMenu(anchor: View, category: Category) {
            val popup = PopupMenu(anchor.context, anchor)
            popup.menuInflater.inflate(R.menu.menu_category_item, popup.menu)

            popup.menu.findItem(R.id.action_delete_category).isVisible =
                !listener.hasMovements(category.id)

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_modify_category -> { listener.onModify(category); true }
                    R.id.action_delete_category -> { listener.onDelete(category); true }
                    else -> false
                }
            }
            popup.show()
        }
    }

    // Infla el layout de cada ítem de la lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)
        return ViewHolder(view)
    }

    // Enlaza el ViewHolder con la categoría en la posición indicada
    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(categories[position])

    // Devuelve el total de categorías en la lista
    override fun getItemCount(): Int = categories.size

    // Reemplaza la lista actual con una nueva y notifica al RecyclerView para que se redibuje
    fun updateCategories(updatedCategories: List<Category>) {
        categories.clear()
        categories.addAll(updatedCategories)
        notifyDataSetChanged()
    }
}
