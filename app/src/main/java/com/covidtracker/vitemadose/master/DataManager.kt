package com.covidtracker.vitemadose.master

import com.covidtracker.vitemadose.data.CenterResponse
import com.covidtracker.vitemadose.data.Department
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object DataManager {

    private const val URL_BASE = "https://raw.githubusercontent.com"

    private val service: RetrofitService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(client)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(
                Json(JsonConfiguration(strictMode = false)).asConverterFactory(
                    MediaType.get("application/json")
                )
            )
            .build()

        service = retrofit.create(RetrofitService::class.java)
    }

    suspend fun getDepartments(): List<Department> {
        return service.getDepartments()
    }

    suspend fun getCenters(departmentCode: String): CenterResponse {
        return service.getCenters(departmentCode)
    }
}