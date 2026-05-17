package com.example.proyectos2gastospersonales

import androidx.room.*
import java.sql.Date

data class UserWithCategories(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "user_id"
    )
    val categories: List<Category>
)

@Dao
interface CategoryDao {
    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithCategories(): List<UserWithCategories>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getAllCategoriesFromUser(userId: Int): UserWithCategories

    @Transaction
    @Query(
        """
        SELECT c.name, COUNT(m.id) as amount, SUM(m.amount) as total, c.icon 
        FROM categories c
        INNER JOIN movements m ON c.id = m.category_id
        WHERE m.user_id = :userId
          AND m.type = :type
          AND strftime('%m', m.date / 1000, 'unixepoch') = :month
          AND strftime('%Y', m.date / 1000, 'unixepoch') = :year
        GROUP BY c.id
        ORDER BY total DESC
    """
    )
    fun getCategoriesFromAllAccounts(
        userId: Int,
        type: MovementType,
        month: String,
        year: String
    ): List<CategoryData>

    //Nada más porque luego me sirve para repasar
    //El strftime (string format time) sirve para poder extraer ciertas partes de una fecha, el %m le indica que extrae el mes
    //y lo extrae como un número de dos dígitos (01, 02, por eso se hace la conversión en el selectedMonth del updateRView, si no se muere)
    //El date / 1000 es porque Android guarda la fecha en milisegundos, entonces hacemos la división para que ahora se conviertan a segundos
    // Y el unixepoch es para decirle que le estamos pasando el número de segundos que han transcurrido desde el origen (en este caso
    // es el 1 de enero de 1970) entonces hace la suma del tiempo que le pasamos y eso nos da el mes que elegimos originalmente. 👍

    @Transaction
    @Query(
        """
        SELECT c.name, COUNT(m.id) as amount, SUM(m.amount) as total, c.icon 
        FROM categories c
        INNER JOIN movements m ON c.id = m.category_id
        INNER JOIN accounts a ON a.id = m.account_id
        WHERE m.user_id = :userId
          AND a.name = :accountName
          AND m.type = :type
          AND strftime('%m', m.date / 1000, 'unixepoch') = :month 
          AND strftime('%Y', m.date / 1000, 'unixepoch') = :year
        GROUP BY c.id
        ORDER BY total DESC
    """
    )
    fun getCategoriesFromOneAccount(
        userId: Int,
        accountName: String,
        type: MovementType,
        month: String,
        year: String
    ): List<CategoryData>
}