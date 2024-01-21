package dev.redfox.calmsphere.offline

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.redfox.calmsphere.models.ZenDataModel
import kotlinx.coroutines.flow.Flow
import retrofit2.Response


@Dao
interface ZenDataDao {

    @Query("SELECT * FROM zendata")
    fun getZenData(): Flow<List<ZenDataModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(zenDataOfflineList: List<ZenDataModel>)

    @Query("DELETE FROM zendata")
    fun deleteAllZenData()

}