package com.example.ui.screens.chat

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenRouterMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenRouterRequest(
    val model: String = "google/gemma-3-27b-it:free",
    val messages: List<OpenRouterMessage>,
    val stream: Boolean = true,
    val response_format: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class OpenRouterChoiceDto(
    val message: OpenRouterMessage?
)

@JsonClass(generateAdapter = true)
data class OpenRouterResponseDto(
    val choices: List<OpenRouterChoiceDto>?
)

interface OpenRouterService {
    @Streaming
    @POST("v1/chat/completions")
    suspend fun streamChatCompletions(
        @Header("Authorization") auth: String,
        @Header("HTTP-Referer") referer: String = "https://aistudio.google.com",
        @Header("X-Title") title: String = "Ammu App",
        @Body request: OpenRouterRequest
    ): ResponseBody

    @POST("v1/chat/completions")
    suspend fun getChatCompletions(
        @Header("Authorization") auth: String,
        @Header("HTTP-Referer") referer: String = "https://aistudio.google.com",
        @Header("X-Title") title: String = "Ammu App",
        @Body request: OpenRouterRequest
    ): OpenRouterResponseDto
}

object OpenRouterClient {
    private const val BASE_URL = "https://openrouter.ai/api/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: OpenRouterService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenRouterService::class.java)
    }
}

