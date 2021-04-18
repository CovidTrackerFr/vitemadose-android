package com.cvtracker.vmd.master

import com.cvtracker.vmd.BuildConfig
import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.CenterResponse
import com.cvtracker.vmd.data.Department
import com.cvtracker.vmd.data.StatsResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

object DataManager {

    /** Theses urls could be override at startup via the Firebase Remote config **/
    var URL_BASE = "https://vitemadose.gitlab.io"
    var PATH_DATA_DEPARTMENT = "/vitemadose/{code}.json"
    var PATH_STATS = "/vitemadose/stats.json"

    private var cacheDepartmentsList: List<Department>? = null

    private val service: RetrofitService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(client)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(
                Json(JsonConfiguration(strictMode = false)).asConverterFactory(
                    MediaType.get("application/json")
                )
            )
            .build()

        retrofit.create(RetrofitService::class.java)
    }

    suspend fun getCenters(departmentCode: String): CenterResponse {
        return service.getCenters(URL_BASE + PATH_DATA_DEPARTMENT.replace("{code}", departmentCode))
    }

    suspend fun getStats(): StatsResponse {
        return service.getStats(URL_BASE + PATH_STATS)
    }

    fun getDepartmentsByCode(code : String): List<Department> {
        return getDepartmentsList()?.filter {
            it.departmentCode.startsWith(code)
        } ?: emptyList()
    }

    fun getDepartmentsByName(name: String): List<Department> {
        return getDepartmentsList()?.filter {
            it.departmentName.toLowerCase(Locale.FRANCE).contains(name.toLowerCase(Locale.FRANCE))
        } ?: emptyList()
    }

    suspend fun getCitiesByPostalCode(code : String): List<Department> {
        return emptyList()
    }

    suspend fun getCitiesByName(name: String): List<Department> {
        return emptyList()
    }

    private fun getDepartmentsList(): List<Department>? {
        return cacheDepartmentsList ?: run {
            val data =
                ViteMaDoseApp.get().resources.openRawResource(R.raw.departments).bufferedReader()
                    .use { it.readText() }
            val myType = object : TypeToken<List<Department>>() {}.type
            try {
                cacheDepartmentsList = Gson().fromJson(data, myType)
                cacheDepartmentsList
            } catch (e: Exception) {
                null
            }
        }
    }
}