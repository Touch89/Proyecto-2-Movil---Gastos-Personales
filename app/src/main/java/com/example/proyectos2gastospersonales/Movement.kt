package com.example.proyectos2gastospersonales

import androidx.room.*
import java.sql.Date

@Entity(tableName = "movements")
data class Movement(
    @PrimaryKey val id: Int,
    val type: MovementType,
    val amount: Float,
    @ColumnInfo(name = "account_id") val accountId: Int,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    val description: String?,
    val date: Date,
    @ColumnInfo(name = "origin_account_id")val originAccountId: Int?,
    @ColumnInfo(name = "destiny_account_id")val destinyAccountId: Int?,
    @ColumnInfo(name = "user_id") val userId: Int,
)
