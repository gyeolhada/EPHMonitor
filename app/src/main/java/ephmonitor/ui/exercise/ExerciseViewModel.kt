package com.example.ephmonitor.ui.exercise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ephmonitor.room.dao.RecordDao
import com.example.ephmonitor.room.db.AppDatabase
import com.example.ephmonitor.room.entity.Record
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseViewModel(application: Application) : AndroidViewModel(application) {
    private val recordDao: RecordDao = AppDatabase.getInstance(application).recordDao()

    fun addRecord(
        sportType: Int, date: String, duration: String, startTime: String, endTime: String,
        heartRate: List<Float>,
        breathRate: List<Float>,
        isHeartAbnormal: Boolean,
        isBreathAbnormal:Boolean,
        uid: Long) {
        val record = Record(sportType = sportType, date = date, duration = duration, startTime = startTime, endTime = endTime,
            heartRate=heartRate,breathRate=breathRate,isHeartAbnormal=isHeartAbnormal,isBreathAbnormal=isBreathAbnormal,uid = uid)
        viewModelScope.launch(Dispatchers.IO) {
            recordDao.insert(record)
        }
    }
}
