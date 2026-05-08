package com.example.proyectos2gastospersonales

import androidx.room.*

@Database(entities = [User::class, Account::class, Category::class, Movement::class], version = 1)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun movementDao(): MovementDao
}
