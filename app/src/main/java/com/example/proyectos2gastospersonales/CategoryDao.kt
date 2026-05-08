package com.example.proyectos2gastospersonales

import androidx.room.*

data class UserWithCategories (
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
}