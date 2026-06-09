package com.greenrou.rouxen.feature.history.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scan: ScanResultEntity): Long

    @Query("SELECT * FROM scan_results ORDER BY scannedAtMs DESC")
    fun getAll(): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE id = :id")
    suspend fun getById(id: Long): ScanResultEntity?

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun deleteById(id: Long)
}
