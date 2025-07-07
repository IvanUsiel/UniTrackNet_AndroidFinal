package com.irjarqui.unitracknetv3.data.remote.api

import com.irjarqui.unitracknetv3.data.remote.model.ospf.SegmentosOspfResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface OspfApiService {
    //Pruebas
    @GET("ospf/segmentos/todos")

    //Produccion
    //@GET("unitrack_ospf.cgi")
    suspend fun getSegmentos(): Response<SegmentosOspfResponseDto>

    //Pruebas
    @GET("ospf/segmentos/todos")

    //Produccion
    //@GET("unitrack_ospf.cgi")
    suspend fun getSegmentosTodos(): SegmentosOspfResponseDto
}
