package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "homelab_services")
data class HomelabService(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val type: String, // "PROXMOX", "UNRAID", "HOMEAX", "PIHOLE", "PLEX", "PORTAINER"
    val isOnline: Boolean,
    val port: Int,
    val pingMs: Int,
    val uptimePct: Double = 99.9,
    val isCustom: Boolean = false
)
