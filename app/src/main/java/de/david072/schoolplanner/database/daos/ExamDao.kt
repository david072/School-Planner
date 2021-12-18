package de.david072.schoolplanner.database.daos

import androidx.room.*
import de.david072.schoolplanner.database.entities.Exam
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {

    @Query("SELECT * FROM exams ORDER BY due_date")
    fun getOrderedByDueDate(): Flow<List<Exam>>

    @Update
    suspend fun update(exam: Exam)

    @Insert
    suspend fun insertAll(vararg exams: Exam)

    @Delete
    suspend fun delete(exam: Exam)

}