package de.david072.schoolplanner.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import de.david072.schoolplanner.database.entities.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects")
    fun getAll(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE uid LIKE :id LIMIT 1")
    fun findById(id: Int): Flow<Subject>

    @Insert
    fun insert(subject: Subject)

    @Delete
    fun delete(subject: Subject)
}