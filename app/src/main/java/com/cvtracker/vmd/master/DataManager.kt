package com.cvtracker.vmd.master

import com.cvtracker.vmd.BuildConfig
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.ValidatorAdapterFactory
import com.cvtracker.vmd.data.CenterResponse
import com.cvtracker.vmd.data.SearchEntry
import com.cvtracker.vmd.data.StatsResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
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

    var URL_CITIES_BY_NAME = "https://geo.api.gouv.fr/communes?nom={NAME}&fields=codesPostaux,centre,departement&limit=15"
    var URL_CITIES_BY_POSTAL_CODE = "https://geo.api.gouv.fr/communes?codePostal={POSTAL_CODE}&fields=codesPostaux,centre,departement&limit=15"

    private var cacheDepartmentsList: List<SearchEntry.Department>? = null
    private var cacheMapNearDepartments: Map<String, List<String>>? = null

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

    private val gson = GsonBuilder()
        .registerTypeAdapterFactory(ValidatorAdapterFactory())
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create()

    suspend fun getCenters(departmentCode: String, useNearDepartment: Boolean): CenterResponse {
        val response = service.getCenters(URL_BASE + PATH_DATA_DEPARTMENT.replace("{code}", departmentCode))
        if(useNearDepartment) {
            /** We load centers from departments near the current department
             * as some centers from an other department could be close to the city currently focused */
            getNearDepartmentsMap()?.get(departmentCode)?.forEach { nearDepartmentCode ->
                response.aggregate(
                    service.getCenters(
                        URL_BASE + PATH_DATA_DEPARTMENT.replace(
                            "{code}",
                            nearDepartmentCode
                        )
                    )
                )
            }
        }
        return response
    }

    suspend fun getStats(): StatsResponse {
        return service.getStats(URL_BASE + PATH_STATS)
    }

    fun getDepartmentsByCode(code: String): List<SearchEntry.Department> {
        return getDepartmentsList()?.filter {
            it.departmentCode.startsWith(code)
        } ?: emptyList()
    }

    fun getDepartmentsByName(name: String): List<SearchEntry.Department> {
        return getDepartmentsList()?.filter {
            it.departmentName.toLowerCase(Locale.FRANCE).contains(name.toLowerCase(Locale.FRANCE))
        } ?: emptyList()
    }

    suspend fun getCitiesByPostalCode(code: String): List<SearchEntry.City> {
        return if(code.length != 5){
            /** The Geo API has not autocomplete for postalCode search **/
            emptyList()
        }else {
            service.getCities(URL_CITIES_BY_POSTAL_CODE.replace("{POSTAL_CODE}", code))
                .onEach { it.searchedPostalCode = code }
        }
    }

    suspend fun getCitiesByName(name: String): List<SearchEntry.City> {
        return service.getCities(URL_CITIES_BY_NAME.replace("{NAME}", name))
    }

    /**
     * Load departments list and keep it
     */
    private fun getDepartmentsList(): List<SearchEntry.Department>? {
        return cacheDepartmentsList ?: run {
            val data =
                ViteMaDoseApp.get().resources.openRawResource(R.raw.departments).bufferedReader()
                    .use { it.readText() }
            val myType = object : TypeToken<List<SearchEntry.Department>>() {}.type
            try {
                cacheDepartmentsList = gson.fromJson(data, myType)
                cacheDepartmentsList
            } catch (e: JsonParseException) {
                null
            }
        }
    }

    /**
     * Here we load (department -> List of near departments) map
     */
    private fun getNearDepartmentsMap(): Map<String, List<String>>? {
        return cacheMapNearDepartments ?: run {
            val data =
                ViteMaDoseApp.get().resources.openRawResource(R.raw.near_departments_map)
                    .bufferedReader()
                    .use { it.readText() }
            val myType = object : TypeToken<Map<String, List<String>>>() {}.type
            try {
                cacheMapNearDepartments = gson.fromJson(data, myType)
                cacheMapNearDepartments
            } catch (e: JsonParseException) {
                null
            }
        }
    }
}