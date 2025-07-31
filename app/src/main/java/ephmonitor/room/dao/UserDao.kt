package com.example.ephmonitor.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.ephmonitor.room.entity.User

@Dao
interface UserDao {
    @Insert//插入数据操作
    suspend fun insert(user: User):Long
    //查询有无一样的密码，有则返回false.无则返回true
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

}