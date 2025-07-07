package com.irjarqui.unitracknetv3.data.remote.repository

import com.irjarqui.unitracknetv3.data.remote.api.BgpApiService
import com.irjarqui.unitracknetv3.data.remote.model.bgp.SegmentoBgpDto
import com.irjarqui.unitracknetv3.data.remote.model.bgp.SegmentosBgpResponseDto

class BgpRepository(private val api: BgpApiService) {

    suspend fun getSegmentos(): List<SegmentoBgpDto>? {
        val response = api.getSegmentos()
        return if (response.isSuccessful) response.body()?.segmentos else null
    }

    suspend fun getRawBgpResponse(): SegmentosBgpResponseDto? {
        return api.getSegmentosTodos()
    }
}
