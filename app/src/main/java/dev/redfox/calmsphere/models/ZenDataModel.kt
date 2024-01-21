package dev.redfox.calmsphere.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zendata")
data class ZenDataModel(
    val articleUrl: String,
    val author: String,
    val bgImageUrl: String,
    val dzImageUrl: String,
    val dzType: String,
    val language: String,
    val primaryCTAText: String,
    val sharePrefix: String,
    val text: String,
    val theme: String,
    val themeTitle: String,
    val type: String,
    @PrimaryKey val uniqueId: String
)