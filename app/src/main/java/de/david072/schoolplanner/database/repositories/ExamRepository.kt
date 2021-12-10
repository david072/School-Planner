package de.david072.schoolplanner.database.repositories

import android.app.Application
import android.content.Context
import de.david072.schoolplanner.database.AppDatabase
import de.david072.schoolplanner.database.daos.ExamDao
import de.david072.schoolplanner.database.entities.Exam

class ExamRepository {

    private val examDao: ExamDao

    constructor(application: Application) {
        examDao = AppDatabase.instance(application).examDao()
    }

    constructor(context: Context) {
        examDao = AppDatabase.instance(context).examDao()
    }


    suspend fun update(exam: Exam) = examDao.update(exam)

    suspend fun insertAll(vararg exams: Exam) = examDao.insertAll(*exams)

}