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
}