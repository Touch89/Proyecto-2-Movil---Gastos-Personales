package com.example.proyectos2gastospersonales

import androidx.room.*

@Entity
data class UserWithMovements(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "user_id"
    )
    val movements: List<Movement>
)

@Dao
interface MovementDao {
    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithMovements(): List<UserWithMovements>

    @Query("SELECT count(*) FROM movements WHERE category_id = :categoryId")
    fun getMovementCountByCategory(categoryId: Int): Int

    @Query("SELECT sum(amount) FROM movements WHERE account_id = :accountId AND category_id = :categoryId AND type = :type AND strftime('%m', date / 1000, 'unixepoch') = :month \n AND strftime('%Y', date / 1000, 'unixepoch') = :year ORDER BY sum(amount) DESC")
    fun getTotalAmountByCategory(accountId: Int, categoryId: Int, type: MovementType, month: Int, year: Int): Float?

    @Query("SELECT sum(amount) FROM movements WHERE category_id = :categoryId AND type = :type AND strftime('%m', date / 1000, 'unixepoch') = :month \n AND strftime('%Y', date / 1000, 'unixepoch') = :year ORDER BY sum(amount) DESC")
    fun getTotalAmountFromAllAccounts(categoryId: Int, type: MovementType, month: Int, year: Int): Float?
}