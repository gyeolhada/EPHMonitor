package com.example.ephmonitor.ui.mine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ephmonitor.room.db.AppDatabase
import com.example.ephmonitor.model.History
import com.example.ephmonitor.room.dao.PersonDao
import com.example.ephmonitor.room.dao.RecordDao
import com.example.ephmonitor.room.entity.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MineViewModel(application: Application) : AndroidViewModel(application) {
    private val recordDao: RecordDao = AppDatabase.getInstance(application).recordDao()
    private val personDao: PersonDao = AppDatabase.getInstance(application).personDao()

    fun getHistoryItems(uid: Long): LiveData<List<History>> {
        val result = MutableLiveData<List<History>>()
        viewModelScope.launch {
            val totalRecords = recordDao.getRecordsByUid(uid)
            val historyMap = totalRecords.groupBy { it.date }
            val historyItems = historyMap.map { (date, records) ->
                History(uid = uid, date = date, sports = records)
            }
            result.postValue(historyItems)
        }
        return result
    }

    private val personLiveData = MutableLiveData<Person?>()

    fun getPerson(uid: Long): MutableLiveData<Person?> {
        viewModelScope.launch(Dispatchers.IO) {
            val person = personDao.getPersonByUid(uid)
            withContext(Dispatchers.Main) {
                personLiveData.value = person
            }
        }
        return personLiveData
    }

    fun getPersonAge(uid: Long):String?{
        var age: String? = null
        // 使用协程执行数据库查询
        viewModelScope.launch {
            age = withContext(Dispatchers.IO) {
                personDao.getAgeByUid(uid) // 这里执行数据库查询
            }
        }
        return age
    }

    fun getPersonWeight(uid: Long): String? {
        var weight: String? = null
        // 使用协程执行数据库查询
        viewModelScope.launch {
            weight = withContext(Dispatchers.IO) {
                personDao.getWeightByUid(uid) // 这里执行数据库查询
            }
        }
        return weight
    }
}