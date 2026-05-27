package com.example.ui.screens.hadith

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class HadithApiResponse(
    val metadata: HadithMetadataDto?,
    val hadiths: List<HadithDto>?
)

@JsonClass(generateAdapter = true)
data class HadithMetadataDto(
    val name: String?,
    val sections: Map<String, String>?,
    val section_details: Map<String, SectionDetailDto>?
)

@JsonClass(generateAdapter = true)
data class SectionDetailDto(
    val hadithnumber_first: Int?,
    val hadithnumber_last: Int?,
    val arabicnumber_first: Int?,
    val arabicnumber_last: Int?
)

@JsonClass(generateAdapter = true)
data class HadithDto(
    val hadithnumber: Int,
    val arabicnumber: Int?,
    val text: String,
    val grades: List<HadithGradeDto>?
)

@JsonClass(generateAdapter = true)
data class HadithGradeDto(
    val name: String?,
    val grade: String?
)

interface HadithApiService {
    // Fetch an entire section
    @GET("editions/{edition}/sections/{section}.json")
    suspend fun getHadithSection(
        @Path("edition") edition: String,
        @Path("section") section: Int
    ): HadithApiResponse

    // Fetch a single hadith by number
    @GET("editions/{edition}/{number}.json")
    suspend fun getSingleHadith(
        @Path("edition") edition: String,
        @Path("number") number: Int
    ): HadithApiResponse
}

object HadithApiClient {
    private const val BASE_URL = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: HadithApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(HadithApiService::class.java)
    }
}
