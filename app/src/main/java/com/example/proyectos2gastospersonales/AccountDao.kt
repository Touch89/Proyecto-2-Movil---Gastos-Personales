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
    @Query("SELECT * FROM accounts WHERE id = :accountId AND user_id = :userId")
    suspend fun getAccount(accountId: Int, userId: Int): Account

    @Transaction
    @Delete
    suspend fun deleteAccount(account: Account)

    @Transaction
    @Insert
    suspend fun insertAccount(account: Account)

    @Transaction
    @Update
    suspend fun updateAccount(account: Account)
}