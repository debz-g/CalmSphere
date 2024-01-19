package dev.redfox.calmsphere.networking

import dev.redfox.calmsphere.models.ZenDataModel
import retrofit2.Response
import javax.inject.Inject

class Repository @Inject constructor (private val apiInterface: ApiInterface) {

     suspend fun getZenData(date: String, version: String): Response<List<ZenDataModel>>{
        return apiInterface.getZenData(date, version)
    }

}