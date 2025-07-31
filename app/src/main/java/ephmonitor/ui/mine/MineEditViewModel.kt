package com.example.ephmonitor.ui.mine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ephmonitor.room.dao.PersonDao
import com.example.ephmonitor.room.db.AppDatabase
import com.example.ephmonitor.room.entity.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MineEditViewModel(application: Application) : AndroidViewModel(application) {
    private val personDao: PersonDao = AppDatabase.getInstance(application).personDao()
    private var person: Person? = null

    fun getPerson(uid: Long, callback: (Person?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            person = personDao.getPersonByUid(uid)
            withContext(Dispatchers.Main) {
                callback(person)
            }
        }
    }

    fun changeName(name: String) {
        person?.pname = name
    }

    fun changeBirth(birth: String) {
        person?.pbirth = birth
    }

    fun changeSex() {
        person?.psex = if (person?.psex == "男") "女" else "男"
    }

    fun changeHeight(height: String) {
        person?.pheight = height
    }

    fun changeWeight(weight: String) {
        person?.pweight = weight
    }

    fun changeBMI(bmi: String) {
        person?.pBMI = bmi
    }

    fun changeAge(age: String) {
        person?.page = age
    }

    fun changeAvatar(avatar: String) {
        person?.avatar = avatar
    }

    fun saveChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            person?.let { personDao.updatePerson(it) }
        }
    }
}
