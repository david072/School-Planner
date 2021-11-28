package de.david072.schoolplanner.database.daos

import androidx.room.*
import de.david072.schoolplanner.database.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE subject_id LIKE :subjectId")
    fun findBySubject(subjectId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE uid LIKE :id")
    fun findById(id: Int): Flow<Task>

    @Query("SELECT * FROM tasks ORDER BY due_date, completed")
    fun getOrderedByDueDate(): Flow<List<Task>>

    @Insert
    suspend fun insertAll(vararg tasks: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}