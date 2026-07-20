package com.example.stylebyte.network

import com.example.stylebyte.network.dto.AuthResponseDto
import com.example.stylebyte.network.dto.LoginRequestDto
import com.example.stylebyte.network.dto.RegisterRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<AuthResponseDto>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequestDto): Response<AuthResponseDto>
}
