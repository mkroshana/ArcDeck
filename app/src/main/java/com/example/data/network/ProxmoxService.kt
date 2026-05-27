package com.example.data.network

import com.example.data.model.ProxmoxPowerActionResponse
import com.example.data.model.ProxmoxResourceResponse
import retrofit2.http.*

interface ProxmoxService {

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
}
