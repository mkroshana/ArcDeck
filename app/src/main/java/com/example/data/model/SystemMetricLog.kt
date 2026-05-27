package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_metric_logs")
data class SystemMetricLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val cpuUsage: Int, // Percentage
    val ramUsage: Int, // Percentage
    val storageUsage: Int, // Percentage
    val netDownloadKbps: Double,
    val netUploadKbps: Double
)
