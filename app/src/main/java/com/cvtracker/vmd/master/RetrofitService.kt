package com.cvtracker.vmd.master

import com.cvtracker.vmd.data.CenterResponse
import com.cvtracker.vmd.data.StatsResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface RetrofitService {
    @GET
    suspend fun getCenters(@Url url: String): CenterResponse

    @GET
    suspend fun getStats(@Url url: String): StatsResponse
}
