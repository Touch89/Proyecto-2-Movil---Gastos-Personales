package com.example.proyectos2gastospersonales

import androidx.room.*

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: Int,
    val name: String,
    val icon: Int,
    @ColumnInfo(name = "user_id") val userId: Int
)
