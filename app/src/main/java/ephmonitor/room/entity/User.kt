package com.example.ephmonitor.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")//表的名字
data class User(
    @PrimaryKey(autoGenerate = true)
    val uid: Long = 0,
    val username: String,
    val password: String
)
