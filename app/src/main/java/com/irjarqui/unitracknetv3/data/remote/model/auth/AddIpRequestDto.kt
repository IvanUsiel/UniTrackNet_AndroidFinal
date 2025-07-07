package com.irjarqui.unitracknetv3.data.remote.model.auth

data class AddIpRequestDto(
    val username: String,
    val password: String,
    val reason: String
)