package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "terminal_logs")
data class TerminalLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val level: String, // "INFO", "WARN", "CRITICAL"
    val source: String, // "SYS", "PROXMOX", "DOCKER", "BACKUP"
    val message: String
)
