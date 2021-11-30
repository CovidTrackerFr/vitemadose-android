package com.cvtracker.vmd.master

import com.cvtracker.vmd.data.*
import retrofit2.http.GET
import retrofit2.http.Url

interface RetrofitService {
    @GET
    suspend fun getCenters(@Url url: String): CenterResponse

    @GET
    suspend fun getDailyCentersAvailability(@Url url: String): DailyCenterResponse

    @GET
    suspend fun getStats(@Url url: String): StatsResponse

    @GET
    suspend fun getCities(@Url url: String): List<SearchEntry.City>

    @GET
    suspend fun getContributors(@Url url: String): ContributorResponse
}
