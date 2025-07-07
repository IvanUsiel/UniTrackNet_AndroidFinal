package com.irjarqui.unitracknetv3.data.remote.api

import com.irjarqui.unitracknetv3.data.remote.model.ping.PingRequest
import com.irjarqui.unitracknetv3.data.remote.model.ping.PingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PingApiService {
    @POST("ping/verificar")
    suspend fun verificarPing(@Body body: PingRequest): Response<PingResponse>
}