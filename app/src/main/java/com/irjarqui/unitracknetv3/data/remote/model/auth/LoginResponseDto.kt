package com.irjarqui.unitracknetv3.data.remote.model.auth

data class LoginResponseDto(
    val status: String,
    val code: Int,
    val message: String,
    val error_type: String? = null,
    val server: String? = null
)