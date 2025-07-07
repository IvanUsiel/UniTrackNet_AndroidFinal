package com.irjarqui.unitracknetv3.data.remote.api

import com.irjarqui.unitracknetv3.data.remote.model.bgp.SegmentosBgpResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface BgpApiService {

    //Pruebas
    @GET("bgp/segmentos/todos")

    //Producción
    //@GET("unitrack_bgp.cgi")
    suspend fun getSegmentos(): Response<SegmentosBgpResponseDto>

    //Pruebas
    @GET("bgp/segmentos/todos")

    //Produccion
    //@GET("unitrack_bgp.cgi")
    suspend fun getSegmentosTodos(): SegmentosBgpResponseDto
}
