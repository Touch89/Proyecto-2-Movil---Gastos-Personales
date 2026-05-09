package com.example.proyectos2gastospersonales

import androidx.room.*

@Entity
data class UserWithAccounts(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "user_id" //Se puede cambiar luego, depende de como se implemente
    )
    val accounts: List<Account>
)

@Dao
interface AccountDao {
    @Transaction
    @Query("SELECT * FROM accounts")
    fun getUsersWithAccounts(): List<UserWithAccounts>
}