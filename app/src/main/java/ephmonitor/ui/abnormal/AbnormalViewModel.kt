package com.example.ephmonitor.ui.abnormal

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ephmonitor.model.History
import com.example.ephmonitor.room.dao.PersonDao
import com.example.ephmonitor.room.dao.RecordDao
import com.example.ephmonitor.room.dao.UserDao
import com.example.ephmonitor.room.db.AppDatabase
import com.example.ephmonitor.room.entity.Person
import com.example.ephmonitor.room.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.ephmonitor.room.entity.Record

class AbnormalViewModel(application: Application) : AndroidViewModel(application) {
    private val recordDao: RecordDao = AppDatabase.getInstance(application).recordDao()

    fun getAbnormalHR(uid:Long):LiveData<List<Record>>{
        val result = MutableLiveData<List<Record>>()
        viewModelScope.launch {
            result.postValue(recordDao.getAbnormalHeartRecordsByUid(uid))
        }
        return result
    }

    fun getAbnormalBR(uid:Long):LiveData<List<Record>>{
        val result = MutableLiveData<List<Record>>()
        viewModelScope.launch {
            result.postValue(recordDao.getAbnormalBreathRecordsByUid(uid))
        }
        return result
    }
}
