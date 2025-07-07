package com.irjarqui.unitracknetv3.data.remote.repository

import com.irjarqui.unitracknetv3.data.remote.api.PingApiService
import com.irjarqui.unitracknetv3.data.remote.model.ping.PingRequest

class PingRepository(private val api: PingApiService) {
    suspend fun doPing(req: PingRequest) = api.verificarPing(req)
}
