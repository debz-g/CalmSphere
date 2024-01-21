package dev.redfox.calmsphere.offline

import androidx.room.withTransaction
import dev.redfox.calmsphere.networking.ApiInterface
import dev.redfox.calmsphere.utils.networkBoundResource
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ZenOfflineRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    private val zenDatabase: ZenDatabase
) {
    private val calendar: Calendar = Calendar.getInstance()
    private val zenDao = zenDatabase.zenDao()

    fun getZenDataOffline() = networkBoundResource(
        query = {
            zenDao.getZenData()
        },
        fetch = {
            delay(2000)
            apiInterface.getZenData(getCurrentDate(),"2")
        },
        saveFetchResult = { zenDataList ->
            zenDatabase.withTransaction {
                zenDao.deleteAllZenData()
                zenDataList.body()?.let { zenDao.insertData(it) }
            }
        }
    )

    fun getCurrentDate() : String {
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, 0)

        val currentDate = calendar.time
        val returnDateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val returnFormattedDate = returnDateFormat.format(currentDate)
        return returnFormattedDate
    }
}