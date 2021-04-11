package com.covidtracker.vitemadose.master

import com.covidtracker.vitemadose.data.CenterResponse
import com.covidtracker.vitemadose.data.Department
import com.covidtracker.vitemadose.data.StatsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface RetrofitService {
    @GET("CovidTrackerFr/vitemadose/data-auto/data/output/departements.json")
    suspend fun getDepartments(): List<Department>

    @GET("CovidTrackerFr/vitemadose/data-auto/data/output/{code}.json")
    suspend fun getCenters(@Path("code") code: String): CenterResponse

    @GET
    suspend fun getStats(@Url url: String): StatsResponse
}
