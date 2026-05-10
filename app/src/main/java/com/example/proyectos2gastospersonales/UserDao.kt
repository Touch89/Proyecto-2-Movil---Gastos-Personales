package com.example.proyectos2gastospersonales

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUser(id: Int): User

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): User?
}