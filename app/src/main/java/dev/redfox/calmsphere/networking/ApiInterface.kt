package dev.redfox.calmsphere.networking

import dev.redfox.calmsphere.models.ZenDataModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("prod/dailyzen/")
    suspend fun getZenData(@Query("date")date: String, @Query("version")version: String): Response<List<ZenDataModel>>
}