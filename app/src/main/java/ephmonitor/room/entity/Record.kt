package com.example.ephmonitor.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import androidx.room.Index
import androidx.room.TypeConverters
import com.example.ephmonitor.Converters
import kotlinx.parcelize.Parcelize

@Entity(tableName = "records", indices = [Index(value = ["uid", "date", "startTime"])])
@TypeConverters(Converters::class)
@Parcelize
data class Record(
    @PrimaryKey(autoGenerate = true)
    val rid: Long = 0,
    val sportType: Int,
    val date: String,
    val duration: String,
    val startTime: String,
    val endTime: String,
    val heartRate: List<Float>,
    val breathRate: List<Float>,
    val isHeartAbnormal: Boolean,
    val isBreathAbnormal: Boolean,
    var uid: Long //外键
) : Parcelable
