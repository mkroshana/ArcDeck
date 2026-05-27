package com.example.data.network

import com.example.data.model.ProxmoxPowerActionResponse
import com.example.data.model.ProxmoxResourceResponse
import com.example.data.model.ProxmoxNodeStatusResponse
import retrofit2.http.*

interface ProxmoxService {

    @GET("api2/json/nodes/{node}/status")
    suspend fun getNodeStatus(
        @Header("Authorization") token: String,
        @Path("node") node: String
    ): ProxmoxNodeStatusResponse

    @GET("api2/json/nodes/{node}/qemu")
    suspend fun getVirtualMachines(
        @Header("Authorization") token: String,
        @Path("node") node: String
    ): ProxmoxResourceResponse

    @GET("api2/json/nodes/{node}/lxc")
    suspend fun getLxcContainers(
        @Header("Authorization") token: String,
        @Path("node") node: String
    ): ProxmoxResourceResponse

    @POST("api2/json/nodes/{node}/qemu/{vmid}/status/start")
    suspend fun startVirtualMachine(
        @Header("Authorization") token: String,
        @Path("node") node: String,
        @Path("vmid") vmid: Int
    ): ProxmoxPowerActionResponse

    @POST("api2/json/nodes/{node}/qemu/{vmid}/status/stop")
    suspend fun stopVirtualMachine(
        @Header("Authorization") token: String,
        @Path("node") node: String,
        @Path("vmid") vmid: Int
    ): ProxmoxPowerActionResponse

    @POST("api2/json/nodes/{node}/qemu/{vmid}/status/shutdown")
    suspend fun shutdownVirtualMachine(
        @Header("Authorization") token: String,
        @Path("node") node: String,
        @Path("vmid") vmid: Int
    ): ProxmoxPowerActionResponse

    @POST("api2/json/nodes/{node}/lxc/{vmid}/status/start")
    suspend fun startLxcContainer(
        @Header("Authorization") token: String,
        @Path("node") node: String,
        @Path("vmid") vmid: Int
    ): ProxmoxPowerActionResponse

    @POST("api2/json/nodes/{node}/lxc/{vmid}/status/stop")
    suspend fun stopLxcContainer(
        @Header("Authorization") token: String,
        @Path("node") node: String,
        @Path("vmid") vmid: Int
    ): ProxmoxPowerActionResponse

    @POST("api2/json/nodes/{node}/lxc/{vmid}/status/shutdown")
    suspend fun shutdownLxcContainer(
        @Header("Authorization") token: String,
        @Path("node") node: String,
        @Path("vmid") vmid: Int
    ): ProxmoxPowerActionResponse

    @GET("api2/json/nodes/{node}/storage")
    suspend fun getNodeStorage(
        @Header("Authorization") token: String,
        @Path("node") node: String
    ): com.example.data.model.ProxmoxStorageResponse

    @POST("api2/json/nodes/{node}/qemu/{vmid}/status/reboot")
    suspend fun rebootVirtualMachine(
        @Header("Authorization") token: String,
        @Path("node") node: String,
        @Path("vmid") vmid: Int
    ): ProxmoxPowerActionResponse

    @POST("api2/json/nodes/{node}/qemu/{vmid}/status/suspend")
    suspend fun suspendVirtualMachine(
        @Header("Authorization") token: String,
        @Path("node") node: String,
        @Path("vmid") vmid: Int
    ): ProxmoxPowerActionResponse

    @POST("api2/json/nodes/{node}/qemu/{vmid}/status/resume")
    suspend fun resumeVirtualMachine(
        @Header("Authorization") token: String,
        @Path("node") node: String,
        @Path("vmid") vmid: Int
    ): ProxmoxPowerActionResponse

    @POST("api2/json/nodes/{node}/lxc/{vmid}/status/reboot")
    suspend fun rebootLxcContainer(
        @Header("Authorization") token: String,
        @Path("node") node: String,
        @Path("vmid") vmid: Int
    ): ProxmoxPowerActionResponse
}
