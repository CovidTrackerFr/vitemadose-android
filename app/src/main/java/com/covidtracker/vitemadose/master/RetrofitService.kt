package com.covidtracker.vitemadose.master

import com.covidtracker.vitemadose.data.CenterResponse
import com.covidtracker.vitemadose.data.Department
import com.covidtracker.vitemadose.data.StatsResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface RetrofitService {
    @GET
    suspend fun getDepartments(@Url url: String): List<Department>

    @GET
    suspend fun getCenters(@Url url: String): CenterResponse

    @GET
    suspend fun getStats(@Url url: String): StatsResponse
}
