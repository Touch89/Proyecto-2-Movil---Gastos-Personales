package com.example.proyectos2gastospersonales

import androidx.room.*

@Entity(tableName = "users", indices = [Index(value = ["email", "username"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "username") val username: String,
    val password: String,
    val avatar: Int
)
