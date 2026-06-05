package com.example.proyectos2gastospersonales

import androidx.room.*

@Entity
data class UserWithMovements(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "user_id"
    )
    val movements: List<Movement>
)

@Dao
interface MovementDao {
    @Transaction
    @Query("SELECT * FROM users")
    fun getUsersWithMovements(): List<UserWithMovements>

    @Query("SELECT count(*) FROM movements WHERE category_id = :categoryId")
    fun getMovementCountByCategory(categoryId: Int): Int

    @Query("SELECT sum(amount) FROM movements WHERE account_id = :accountId AND category_id = :categoryId AND type = :type AND strftime('%m', date / 1000, 'unixepoch') = :month \n AND strftime('%Y', date / 1000, 'unixepoch') = :year ORDER BY sum(amount) DESC")
    fun getTotalAmountByCategory(accountId: Int, categoryId: Int, type: MovementType, month: Int, year: Int): Float?

    @Query("SELECT sum(amount) FROM movements WHERE category_id = :categoryId AND type = :type AND strftime('%m', date / 1000, 'unixepoch') = :month \n AND strftime('%Y', date / 1000, 'unixepoch') = :year ORDER BY sum(amount) DESC")
    fun getTotalAmountFromAllAccounts(categoryId: Int, type: MovementType, month: Int, year: Int): Float?

    @Query("DELETE FROM movements WHERE id = :id")
    fun deleteMovementById(id: Int)

    @Query("""
        SELECT m.id as movId, ac.icon as accIcon, ac.name as accName, m.date as movDate, m.description movDesc, m.amount as movAmount, type, category_id as categoryId FROM movements as m
        INNER JOIN accounts as ac ON account_id == ac.id
        WHERE m.user_id = :userId
        AND m.category_id = (SELECT c.id FROM categories as c WHERE c.name == :categoryName)
        AND m.type = :type
        AND strftime('%m', m.date / 1000, 'unixepoch') = :month
        AND strftime('%Y', m.date / 1000, 'unixepoch') = :year
        ORDER BY movDate DESC
    """)
    fun getMovementDataByCategoryByDateOrder(userId: Int, categoryName: String, type: MovementType, year: String, month: String): List<MovementItemData>

    @Query("""
        SELECT m.id as movId, ac.icon as accIcon, ac.name as accName, m.date as movDate, m.description movDesc, m.amount as movAmount, type, category_id as categoryId FROM movements as m
        INNER JOIN accounts as ac ON account_id == ac.id
        WHERE m.user_id = :userId
        AND m.category_id = (SELECT c.id FROM categories as c WHERE c.name == :categoryName)
        AND m.type = :type
        AND strftime('%m', m.date / 1000, 'unixepoch') = :month
        AND strftime('%Y', m.date / 1000, 'unixepoch') = :year
        ORDER BY accName DESC
    """)
    fun getMovementDataByCategoryByAccountOrder(userId: Int, categoryName: String, type: MovementType, year: String, month: String): List<MovementItemData>

    @Query("""
        SELECT m.id as movId, ac.icon as accIcon, ac.name as accName, m.date as movDate, m.description movDesc, m.amount as movAmount, type, category_id as categoryId FROM movements as m
        INNER JOIN accounts as ac ON account_id == ac.id
        WHERE m.user_id = :userId
        AND m.category_id = (SELECT c.id FROM categories as c WHERE c.name == :categoryName)
        AND m.type = :type
        AND strftime('%m', m.date / 1000, 'unixepoch') = :month
        AND strftime('%Y', m.date / 1000, 'unixepoch') = :year
        ORDER BY movAmount DESC
    """)
    fun getMovementDataByCategoryByAmountOrder(userId: Int, categoryName: String, type: MovementType, year: String, month: String): List<MovementItemData>

    @Query("""
        SELECT SUM(m.amount) FROM movements as m
        INNER JOIN accounts as ac ON account_id == ac.id
        WHERE m.user_id = :userId
        AND m.category_id = (SELECT c.id FROM categories as c WHERE c.name == :categoryName)
        AND m.type = :type
        AND strftime('%m', m.date / 1000, 'unixepoch') = :month
        AND strftime('%Y', m.date / 1000, 'unixepoch') = :year
        GROUP BY category_id
    """)
    fun getMovementsTotalSum(userId: Int, categoryName: String, type: MovementType, year: String, month: String): Double

    @Query("""
        SELECT m.id as movId, ac.icon as accIcon, ac.name as accName, m.date as movDate, m.description movDesc, m.amount as movAmount, type, category_id as categoryId FROM movements as m
        INNER JOIN accounts as ac ON account_id == ac.id
        WHERE m.user_id = :userId
        AND strftime('%m', m.date / 1000, 'unixepoch') = :month
        AND strftime('%Y', m.date / 1000, 'unixepoch') = :year
        ORDER BY movDate DESC
    """)
    fun getMovementsByDate(userId: Int, year: String, month: String): List<MovementItemData>
    @Query("""
        SELECT m.id as movId, ac.icon as accIcon, ac.name as accName, m.date as movDate, m.description movDesc, m.amount as movAmount, type, category_id as categoryId FROM movements as m
        INNER JOIN accounts as ac ON account_id == ac.id
        WHERE ac.user_id = :userId AND ac.name = :accountName
        AND strftime('%m', m.date / 1000, 'unixepoch') = :month
        AND strftime('%Y', m.date / 1000, 'unixepoch') = :year
        ORDER BY movDate DESC
    """)
    fun getMovementsByDateByAccount(userId: Int, accountName: String, year: String, month: String): List<MovementItemData>
    @Query("""
        SELECT m.id as movId, ac.icon as accIcon, ac.name as accName, m.date as movDate, m.description movDesc, m.amount as movAmount, type, category_id as categoryId FROM movements as m
        INNER JOIN accounts as ac ON account_id == ac.id
        WHERE m.user_id = :userId
        AND strftime('%m', m.date / 1000, 'unixepoch') = :month
        AND strftime('%Y', m.date / 1000, 'unixepoch') = :year
        ORDER BY movAmount DESC
    """)
    fun getMovementsByAmount(userId: Int, year: String, month: String): List<MovementItemData>
    @Query("SELECT * FROM movements WHERE id = :id")
    fun getMovement(id: Int): Movement

    @Query("SELECT COALESCE(MAX(id), 0) + 1 FROM movements")
    fun getNextId(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMovement(vararg movement: Movement)

    // Suma total de un tipo de movimiento en un mes/año, filtrado opcionalmente por cuenta
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM movements
        WHERE user_id = :userId
          AND type = :type
          AND strftime('%m', date / 1000, 'unixepoch') = :month
          AND strftime('%Y', date / 1000, 'unixepoch') = :year
          AND (:accountName = 'Todas' OR account_id = (SELECT id FROM accounts WHERE name = :accountName AND user_id = :userId))
    """)
    fun getTotalByType(userId: Int, accountName: String, type: MovementType, month: String, year: String): Double?

    // Suma total de un tipo de movimiento ANTES del mes/año indicado (para calcular saldo anterior)
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM movements
        WHERE user_id = :userId
          AND type = :type
          AND (
              strftime('%Y', date / 1000, 'unixepoch') < :year
              OR (strftime('%Y', date / 1000, 'unixepoch') = :year AND strftime('%m', date / 1000, 'unixepoch') < :month)
          )
          AND (:accountName = 'Todas' OR account_id = (SELECT id FROM accounts WHERE name = :accountName AND user_id = :userId))
    """)
    fun getTotalByTypeBeforeMonth(userId: Int, accountName: String, type: MovementType, month: String, year: String): Double?

    @Update
    fun updateMovement(movement: Movement)
}