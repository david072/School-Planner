package de.david072.schoolplanner.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import de.david072.schoolplanner.database.entities.Exam

@Dao
interface ExamDao {

    @Update
    suspend fun update(exam: Exam)

    @Insert
    suspend fun insertAll(vararg exams: Exam)

}