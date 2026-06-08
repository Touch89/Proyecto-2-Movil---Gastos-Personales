package com.example.proyectos2gastospersonales

import androidx.room.*

@Entity
data class UserWithAccounts(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "user_id"
    )
    val accounts: List<Account>
)

@Dao
interface AccountDao {
    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithAccounts(): List<UserWithAccounts>
    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getAccountsFromUser(userId: Int): UserWithAccounts?

    @Transaction
    @Delete
    suspend fun deleteAccount(account: Account)
}