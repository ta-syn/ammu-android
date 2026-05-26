package com.example.ui.screens.quran

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class AlQuranResponse<T>(
    val code: Int,
    val status: String,
    val data: T
)

@JsonClass(generateAdapter = true)
data class SurahDto(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String
)

@JsonClass(generateAdapter = true)
data class SurahDetailsDto(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val numberOfAyahs: Int,
    val ayahs: List<AyahDto>
)

@JsonClass(generateAdapter = true)
data class AyahDto(
    val number: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val manzil: Int,
    val page: Int,
    val ruku: Int,
    val hizbQuarter: Int
)

interface AlQuranService {
    @GET("surah")
    suspend fun getSurahs(): AlQuranResponse<List<SurahDto>>

    @GET("surah/{number}")
    suspend fun getSurahDetails(@Path("number") number: Int): AlQuranResponse<SurahDetailsDto>
    
    @GET("surah/{number}/editions/quran-uthmani,bn.bengali")
    suspend fun getSurahEditions(@Path("number") number: Int): AlQuranResponse<List<SurahDetailsDto>>
}

object AlQuranClient {
    private const val BASE_URL = "https://api.alquran.cloud/v1/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: AlQuranService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AlQuranService::class.java)
    }
}
