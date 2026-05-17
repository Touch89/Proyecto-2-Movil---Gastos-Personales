package com.example.proyectos2gastospersonales

import androidx.room.*

@Entity(tableName="accounts")
data class Account(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val icon: Int,
    @ColumnInfo(name = "user_id") val userId: Int
)
