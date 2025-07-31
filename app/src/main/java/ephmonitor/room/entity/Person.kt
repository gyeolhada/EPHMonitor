package com.example.ephmonitor.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val pid: Int = 0,
    var pname: String? = "--",
    var pbirth: String? = "--",
    var psex: String? = "男",
    var pheight: String? = "--",
    var pweight: String? = "--",
    var pBMI: String? = "--",
    var page: String? = "--",
    var avatar: String? = "--",
    var uid: Long // 用户id,外键
)