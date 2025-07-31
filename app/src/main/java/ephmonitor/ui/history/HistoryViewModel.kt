package com.example.ephmonitor.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ephmonitor.model.History
import com.example.ephmonitor.room.dao.PersonDao
import com.example.ephmonitor.room.dao.RecordDao
import com.example.ephmonitor.room.db.AppDatabase
import com.example.ephmonitor.room.entity.Record
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val recordDao: RecordDao = AppDatabase.getInstance(application).recordDao()
    private val personDao: PersonDao = AppDatabase.getInstance(application).personDao()

//    fun getHRandBR(uid:Long):LiveData<List<Record>>{
//        val result = MutableLiveData<List<Record>>()
//        viewModelScope.launch {
//            result.postValue(recordDao.getRecordsByUid(uid))
//        }
//        return result
//    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is history Fragment"
    }
    val text: LiveData<String> = _text
}