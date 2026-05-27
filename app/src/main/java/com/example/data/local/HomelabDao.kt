package com.example.data.local

import androidx.room.*
import com.example.data.model.HomelabService
import com.example.data.model.SystemMetricLog
import com.example.data.model.TerminalLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HomelabDao {

    // --- Services Queries ---
    @Query("SELECT * FROM homelab_services ORDER BY name ASC")
    fun getAllServices(): Flow<List<HomelabService>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: HomelabService)

    @Update
    suspend fun updateService(service: HomelabService)

    @Delete
    suspend fun deleteService(service: HomelabService)

    @Query("UPDATE homelab_services SET isOnline = :isOnline WHERE id = :serviceId")
    suspend fun updateServiceStatus(serviceId: Int, isOnline: Boolean)

    @Query("SELECT COUNT(*) FROM homelab_services")
    suspend fun getServiceCount(): Int

    // --- Metrics Queries ---
    @Query("SELECT * FROM system_metric_logs ORDER BY timestamp DESC LIMIT 30")
    fun getRecentMetricsHistory(): Flow<List<SystemMetricLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetricLog(log: SystemMetricLog)

    @Query("DELETE FROM system_metric_logs WHERE timestamp < :oldTimestamp")
    suspend fun clearOldMetrics(oldTimestamp: Long)

    // --- Terminal Logs Queries ---
    @Query("SELECT * FROM terminal_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentTerminalLogs(): Flow<List<TerminalLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerminalLog(log: TerminalLog)

    @Query("DELETE FROM terminal_logs")
    suspend fun clearAllTerminalLogs()
}
