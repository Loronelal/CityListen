package com.citylisten.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(story: StoryEntity)

    @Query("SELECT * FROM stories WHERE placeId = :placeId AND language = :language LIMIT 1")
    suspend fun getCached(placeId: String, language: String): StoryEntity?

    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<StoryEntity>>
}