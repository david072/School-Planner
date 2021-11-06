package de.david072.schoolplanner.database.daos

import androidx.room.Dao
import androidx.room.Query
import de.david072.schoolplanner.database.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE subject_id LIKE :subjectId")
    fun findBySubject(subjectId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE uid LIKE :id")
    fun findById(id: Int): Flow<Task>
}