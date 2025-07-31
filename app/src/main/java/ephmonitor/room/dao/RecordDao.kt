package com.example.ephmonitor.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.ephmonitor.room.entity.Record

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: Record)

    @Query("SELECT * FROM records WHERE uid = :uid")
    suspend fun getRecordsByUid(uid: Long): List<Record>

    @Query("SELECT * FROM records WHERE uid = :uid AND date = :date")
    suspend fun getRecordsByUidAndDate(uid: Long, date: String): List<Record>

    @Query("SELECT * FROM records WHERE uid = :uid AND isHeartAbnormal = 1 ORDER BY startTime DESC")
    suspend fun getAbnormalHeartRecordsByUid(uid: Long): List<Record>

    @Query("SELECT * FROM records WHERE uid = :uid AND date = :date AND isHeartAbnormal = 1 ORDER BY startTime DESC")
    suspend fun getAbnormalHeartRecordsByUidAndDate(uid: Long, date: String): List<Record>

    @Query("SELECT * FROM records WHERE uid = :uid AND isBreathAbnormal = 1 ORDER BY startTime DESC")
    suspend fun getAbnormalBreathRecordsByUid(uid: Long): List<Record>

    @Query("SELECT * FROM records WHERE uid = :uid AND date = :date AND isBreathAbnormal = 1 ORDER BY startTime DESC")
    suspend fun getAbnormalBreathRecordsByUidAndDate(uid: Long, date: String): List<Record>
}