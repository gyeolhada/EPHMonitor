package com.example.ephmonitor.room.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ephmonitor.Converters
import com.example.ephmonitor.room.dao.RecordDao
import com.example.ephmonitor.room.dao.UserDao
import com.example.ephmonitor.room.entity.User
import com.example.ephmonitor.room.entity.Record
import com.example.ephmonitor.model.History
import com.example.ephmonitor.room.dao.PersonDao
import com.example.ephmonitor.room.entity.Person

@Database(entities = [User::class,Record::class, Person::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao() : UserDao
    abstract fun recordDao(): RecordDao
    abstract fun personDao(): PersonDao

    companion object {
    private var instance: AppDatabase ?= null
        fun getInstance(context: Context) : AppDatabase {
            return instance ?: synchronized(this){
                Room.databaseBuilder(context, AppDatabase::class.java, "db_feng")
                    .build().also { instance = it }
            }
        }
    }
}
