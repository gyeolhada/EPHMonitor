package com.example.ephmonitor.ui.login

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

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao: UserDao = AppDatabase.getInstance(application).userDao()
    private val personDao: PersonDao = AppDatabase.getInstance(application).personDao()
    private val recordDao: RecordDao = AppDatabase.getInstance(application).recordDao()
    private val sharedPreferences = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private var uid:Long=0

    fun login(username: String, password: String): LiveData<User?> {
        val result = MutableLiveData<User?>()
        viewModelScope.launch(Dispatchers.IO) {
            val user = userDao.login(username, password)
            if (user != null) {
                uid = user.uid
                Log.d("LoginViewModel", "User logged in: ${user.uid}")
                Log.d("LoginViewModel", "User logged in: ${user.username}")
                with(sharedPreferences.edit()) {
                    putBoolean("is_logged_in", true)
                    putLong("current_user_id", user.uid)
                    putString("current_user_username", user.username)
                    apply()
                }
            }
            result.postValue(user)
        }
        return result
    }

    fun register(user: User): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            val existingUser = userDao.getUserByUsername(user.username)
            if (existingUser == null) {
                val userId = userDao.insert(user)
                uid = userId
                Log.d("LoginViewModel", "User registered: $userId")
                personDao.insert(Person(uid=userId))
                result.postValue(true)
            } else {
                result.postValue(false)
            }
        }
        return result
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            with(sharedPreferences.edit()) {
                putBoolean("is_logged_in", false)
                remove("current_user_id")
                remove("current_user_username")
                apply()
            }
        }
    }


    fun checkLogin(callback: (Boolean) -> Unit) {
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        callback(isLoggedIn)
    }

    fun getCurrentUserId(): Long {
        return sharedPreferences.getLong("current_user_id", -1L)
    }

    fun getCurrentUserAccount(): String {
        return sharedPreferences.getString("current_user_username", "") ?: ""
    }
}
