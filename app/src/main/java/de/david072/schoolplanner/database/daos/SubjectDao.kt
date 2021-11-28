package de.david072.schoolplanner.database.daos

import androidx.room.*
import de.david072.schoolplanner.database.entities.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects")
    fun getAll(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE uid LIKE :id LIMIT 1")
    fun findById(id: Int): Flow<Subject>

    @Insert
    suspend fun insert(subject: Subject)

    @Update
    suspend fun update(subject: Subject)

    @Delete
    suspend fun delete(subject: Subject)

    @Query("DELETE FROM subjects WHERE uid LIKE :id")
    suspend fun delete(id: Int)
}