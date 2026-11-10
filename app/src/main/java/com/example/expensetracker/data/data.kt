package com.example.expensetracker.data


import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["refNo", "accountLast4", "type"], unique = true)]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountPaise: Long,
    val type: String,          // "DEBIT" / "CREDIT"
    val bank: String,
    val accountLast4: String,
    val refNo: String?,
    val counterparty: String?,
    val timestamp: Long,
    val rawSms: String
)

data class AccountTotal(
    val bank: String,
    val accountLast4: String,
    val debitPaise: Long,
    val creditPaise: Long
)

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(t: Transaction): Long   // -1 if duplicate

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :n")
    fun recent(n: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun all(): Flow<List<Transaction>>

    @Query("""
        SELECT bank, accountLast4,
               SUM(CASE WHEN type = 'DEBIT'  THEN amountPaise ELSE 0 END) AS debitPaise,
               SUM(CASE WHEN type = 'CREDIT' THEN amountPaise ELSE 0 END) AS creditPaise
        FROM transactions
        WHERE timestamp >= :from
        GROUP BY bank, accountLast4
    """)
    fun totalsSince(from: Long): Flow<List<AccountTotal>>
}

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): TransactionDao

    companion object {
        @Volatile private var inst: AppDatabase? = null
        fun get(ctx: android.content.Context): AppDatabase =
            inst ?: synchronized(this) {
                inst ?: Room.databaseBuilder(
                    ctx.applicationContext, AppDatabase::class.java, "expenses.db"
                ).build().also { inst = it }
            }
    }
}