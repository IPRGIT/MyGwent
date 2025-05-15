package com.example.mygwent.dependencies

import com.example.mygwent.api.APIConfig

class AppContainer {

    //Creación del servicio, usando la api.
    private val GwentApiService = APIConfig.provideRetrofit().create(APIConfig.ApiService::class.java)
}