package com.example.proyectos2gastospersonales

import androidx.room.*
import java.sql.Date

//Falta lo de las transferencias ***
@Entity(tableName = "movements")
data class Movement(
    @PrimaryKey val id: Int,
    val type: MovementType,
    val amount: Float,
    @ColumnInfo(name = "account_id") val accountId: Int,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    val description: String?,
    val date: Date,
    val originAccount: Account?,
    val destinyAccount: Account?,
    @ColumnInfo(name = "user_id") val userId: Int,
)
