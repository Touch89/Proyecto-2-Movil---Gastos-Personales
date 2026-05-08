package com.example.proyectos2gastospersonales

import androidx.room.*

@Entity(tableName = "users", indices = [Index(value = ["email", "username"], unique = true)])
data class User(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "username") val username: String,
    val password: String,
    val avatar: Int
)
