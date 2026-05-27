package com.example.data.repository

import com.example.data.local.HomelabDao
import com.example.data.model.HomelabService
import com.example.data.model.SystemMetricLog
import com.example.data.model.TerminalLog
import kotlinx.coroutines.flow.Flow

class HomelabRepository(private val homelabDao: HomelabDao) {

    val allServices: Flow<List<HomelabService>> = homelabDao.getAllServices()
    val recentMetrics: Flow<List<SystemMetricLog>> = homelabDao.getRecentMetricsHistory()
    val terminalLogs: Flow<List<TerminalLog>> = homelabDao.getRecentTerminalLogs()

    suspend fun insertService(service: HomelabService) {
        homelabDao.insertService(service)
    }

    suspend fun updateService(service: HomelabService) {
        homelabDao.updateService(service)
    }

    suspend fun deleteService(service: HomelabService) {
        homelabDao.deleteService(service)
    }

    suspend fun updateServiceStatus(serviceId: Int, isOnline: Boolean) {
        homelabDao.updateServiceStatus(serviceId, isOnline)
    }

    suspend fun getServiceCount(): Int {
        return homelabDao.getServiceCount()
    }

    suspend fun insertMetricLog(log: SystemMetricLog) {
        homelabDao.insertMetricLog(log)
    }

    suspend fun clearOldMetrics(oldTimestamp: Long) {
        homelabDao.clearOldMetrics(oldTimestamp)
    }

    suspend fun insertTerminalLog(log: TerminalLog) {
        homelabDao.insertTerminalLog(log)
    }

    suspend fun clearAllTerminalLogs() {
        homelabDao.clearAllTerminalLogs()
    }
}
