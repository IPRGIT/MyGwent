package com.example.mygwent

import com.example.mygwent.api.APIConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetroFitClient {


        private const val BASE_URL = "https://api.gwent.one/"

        val apiService: APIConfig.ApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(APIConfig.ApiService::class.java)
        }


}