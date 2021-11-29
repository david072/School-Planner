package de.david072.schoolplanner.database

import android.app.Application
import android.content.Context
import de.david072.schoolplanner.database.daos.SubjectDao
import de.david072.schoolplanner.database.entities.Subject
import kotlinx.coroutines.flow.Flow

class SubjectRepository {

    private val subjectDao: SubjectDao

    constructor(application: Application) {
        subjectDao = AppDatabase.instance(application).subjectDao()
    }

    constructor(context: Context) {
        subjectDao = AppDatabase.instance(context).subjectDao()
    }

    fun getAll(): Flow<List<Subject>> = subjectDao.getAll()

    fun findById(id: Int): Flow<Subject> = subjectDao.findById(id)

    suspend fun insert(subject: Subject) = subjectDao.insert(subject)

    suspend fun update(subject: Subject) = subjectDao.update(subject)

    suspend fun delete(subject: Subject) = subjectDao.delete(subject)

    suspend fun delete(id: Int) = subjectDao.delete(id)

}