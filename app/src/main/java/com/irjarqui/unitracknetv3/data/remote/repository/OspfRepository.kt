package com.irjarqui.unitracknetv3.data.remote.repository

import com.irjarqui.unitracknetv3.data.remote.api.OspfApiService
import com.irjarqui.unitracknetv3.data.remote.model.ospf.SegmentoOspfDto
import com.irjarqui.unitracknetv3.data.remote.model.ospf.SegmentosOspfResponseDto

class OspfRepository(private val api: OspfApiService) {

    suspend fun getSegmentos(): List<SegmentoOspfDto>? {
        val response = api.getSegmentos()
        return if (response.isSuccessful) response.body()?.segmentos else null
    }

    suspend fun getRawOspfResponse(): SegmentosOspfResponseDto? {
        return api.getSegmentosTodos()
    }
}
