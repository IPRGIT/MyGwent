package com.example.mygwent.api

import com.example.mygwent.data.CardResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object APIConfig {
    const val BASE_URL = "https://api.gwent.one/"

    fun provideRetrofit(): Retrofit {
        // Add logging interceptor
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // Configure Gson to be lenient (optional)
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    interface GwentApiService {
        @GET("cards")
        suspend fun getAllCards(): Response<CardResponse>

        // O si necesitas paginación en la API:
        @GET("cards")
        suspend fun getCardsByPage(
            @Query("page") page: Int,
            @Query("size") size: Int = 50
        ): Response<CardResponse>
    }



    object GwentApi {
        val retrofitService: ApiService by lazy {
            provideRetrofit().create(ApiService::class.java)
        }
    }



    interface ApiService {
        @GET(".")
        suspend fun getAllCards(
            @Query("key") key: String = "data",
            @Query("response") response: String = "json",
            @Query("version") version: String = "latest"
        ): Response<CardResponse>

        @GET(".")
        suspend fun getCardsByPage(
            @Query("key") key: String = "data",
            @Query("response") response: String = "json",
            @Query("version") version: String = "latest",
            @Query("page") page: Int,
            @Query("size") size: Int = 50
        ): Response<CardResponse>
    }
}