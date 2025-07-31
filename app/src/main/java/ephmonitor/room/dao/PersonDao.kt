package com.example.ephmonitor.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ephmonitor.room.entity.Person

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(persons: Person)

    @Delete
    fun delete(vararg persons: Person)

    @Update
    fun updatePerson(persons: Person)

    @Query("SELECT * FROM person WHERE uid = :uid")
    fun getPersonByUid(uid: Long?): Person?

    @Query("DELETE FROM person WHERE uid=:uid")
    fun deleteByUid(uid: Long?)

    @Query("SELECT pname FROM person WHERE uid = :uid")
    fun getNameByUid(uid: Long?): String?

    @Query("SELECT psex FROM person WHERE uid = :uid")
    fun getSexByUid(uid: Long?): String?

    @Query("SELECT pheight FROM person WHERE uid = :uid")
    fun getHeightByUid(uid: Long?): String?

    @Query("SELECT pweight FROM person WHERE uid = :uid")
    fun getWeightByUid(uid: Long?): String?

    @Query("SELECT page FROM person WHERE uid = :uid")
    fun getAgeByUid(uid: Long?): String?

    @Query("SELECT pBMI FROM person WHERE uid = :uid")
    fun getBMIByUid(uid: Long?): String?
}