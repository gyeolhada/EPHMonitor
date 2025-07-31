package com.example.ephmonitor.model
import com.example.ephmonitor.room.entity.Record

data class History(
    val uid: Long,
    val date: String,
    val sports: List<Record>
)
