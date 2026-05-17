package com.example.proyectos2gastospersonales

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [User::class, Account::class, Category::class, Movement::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun movementDao(): MovementDao

    companion object {  // https://developer.android.com/codelabs/android-room-with-a-view-kotlin#7 por si lo quieren checar
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).allowMainThreadQueries()
                    .addCallback(roomCallback)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        //Todos estos son datos de prueba, luego los borramos 👍
        private val roomCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                db.execSQL(
                    """
                    INSERT INTO users (id, email, username, password, avatar) VALUES
                    (1, 'carlos@example.com', 'carlos_dev', 'password123', 1),
                    (2, 'ana@example.com', 'ana_finanzas', 'password456', 2)
                """
                )

                db.execSQL(
                    """
                    INSERT INTO categories (id, name, icon, user_id) VALUES
                    (1, 'Alimentación', 201, 1),
                    (2, 'Transporte', 202, 1),
                    (3, 'Salario', 203, 1),
                    (4, 'Entretenimiento', 204, 1),
                    (5, 'Transferencias', 205, 1)
                """
                )

                db.execSQL(
                    """
                    INSERT INTO accounts (id, name, description, icon, user_id) VALUES
                    (1, 'Efectivo', 'Billetera física', 101, 1),
                    (2, 'Cuenta Débito', 'Tarjeta principal del banco', 102, 1),
                    (3, 'Ahorros', 'Fondo de emergencia', 103, 1)
                """
                )

                db.execSQL(
                    """
    INSERT INTO movements (id, type, amount, account_id, category_id, description, date, origin_account_id, destiny_account_id, user_id) VALUES
    -- Octubre
    (1, 'Ingreso', 15000.0, 2, 3, 'Quincena', ${java.sql.Date.valueOf("2023-10-15").time}, NULL, NULL, 1),
    (2, 'Gasto', 350.0, 1, 1, 'Cena familiar', ${java.sql.Date.valueOf("2023-10-16").time}, NULL, NULL, 1),
    (3, 'Gasto', 120.5, 2, 2, 'Viaje al trabajo', ${java.sql.Date.valueOf("2023-10-17").time}, NULL, NULL, 1),
    (4, 'Transferencia', 2000.0, 2, 5, 'Ahorro mensual', ${java.sql.Date.valueOf("2023-10-18").time}, 2, 3, 1),
    (5, 'Gasto', 1200.0, 2, 1, 'Despensa en el supermercado', ${java.sql.Date.valueOf("2023-10-20").time}, NULL, NULL, 1),
    (6, 'Gasto', 450.0, 1, 4, 'Salida al cine', ${java.sql.Date.valueOf("2023-10-22").time}, NULL, NULL, 1),
    (7, 'Gasto', 600.0, 2, 2, 'Gasolina', ${java.sql.Date.valueOf("2023-10-25").time}, NULL, NULL, 1),
    (8, 'Ingreso', 15000.0, 2, 3, 'Quincena', ${java.sql.Date.valueOf("2023-10-31").time}, NULL, NULL, 1),

    -- Noviembre
    (9, 'Transferencia', 3000.0, 2, 5, 'Ahorro mensual', ${java.sql.Date.valueOf("2023-11-01").time}, 2, 3, 1),
    (10, 'Gasto', 250.0, 1, 1, 'Desayuno en cafetería', ${java.sql.Date.valueOf("2023-11-03").time}, NULL, NULL, 1),
    (11, 'Gasto', 800.0, 2, 4, 'Suscripciones y videojuegos', ${java.sql.Date.valueOf("2023-11-05").time}, NULL, NULL, 1),
    (12, 'Gasto', 1500.0, 2, 1, 'Despensa mayor', ${java.sql.Date.valueOf("2023-11-10").time}, NULL, NULL, 1),
    (13, 'Ingreso', 15000.0, 2, 3, 'Quincena', ${java.sql.Date.valueOf("2023-11-15").time}, NULL, NULL, 1),
    (14, 'Transferencia', 500.0, 3, 5, 'Retiro de emergencia', ${java.sql.Date.valueOf("2023-11-18").time}, 3, 1, 1),
    (15, 'Gasto', 300.0, 1, 2, 'Taxis fin de semana', ${java.sql.Date.valueOf("2023-11-20").time}, NULL, NULL, 1)
"""
                )
            }
        }
    }
}
