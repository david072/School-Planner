package de.david072.schoolplanner.database.repositories

import android.app.Application
import android.content.Context
import de.david072.schoolplanner.database.AppDatabase
import de.david072.schoolplanner.database.daos.ExamDao
import de.david072.schoolplanner.database.entities.Exam
import kotlinx.coroutines.flow.Flow

class ExamRepository {

    private val examDao: ExamDao

    constructor(application: Application) {
        examDao = AppDatabase.instance(application).examDao()
    }

    constructor(context: Context) {
        examDao = AppDatabase.instance(context).examDao()
    }

    fun findById(id: Int): Flow<Exam> = examDao.findById(id)

    fun getOrderedByDueDate(): Flow<List<Exam>> = examDao.getOrderedByDueDate()

    suspend fun update(exam: Exam) = examDao.update(exam)

    suspend fun insertAll(vararg exams: Exam) = examDao.insertAll(*exams)

    suspend fun delete(exam: Exam) = examDao.delete(exam)

}