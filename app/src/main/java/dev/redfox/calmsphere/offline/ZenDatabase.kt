package dev.redfox.calmsphere.offline

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.redfox.calmsphere.models.ZenDataModel

@Database(entities = [ZenDataModel::class], version = 1)
abstract class ZenDatabase : RoomDatabase() {
    abstract fun zenDao(): ZenDataDao
}