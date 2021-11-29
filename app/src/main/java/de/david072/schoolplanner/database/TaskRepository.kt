package de.david072.schoolplanner.database

import android.app.Application
import android.content.Context
import de.david072.schoolplanner.database.daos.TaskDao
import de.david072.schoolplanner.database.entities.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository {

    private val taskDao: TaskDao

    constructor(application: Application) {
        taskDao = AppDatabase.instance(application).taskDao()
    }

    constructor(context: Context) {
        taskDao = AppDatabase.instance(context).taskDao()
    }

    fun getAll(): Flow<List<Task>> = taskDao.getAll()

    fun findBySubject(subjectId: Int): Flow<List<Task>> = taskDao.findBySubject(subjectId)

    fun findById(id: Int): Flow<Task> = taskDao.findById(id)

    fun getOrderedByDueDate(): Flow<List<Task>> = taskDao.getOrderedByDueDate()

    suspend fun insertAll(vararg tasks: Task) = taskDao.insertAll(*tasks)

    suspend fun update(task: Task) = taskDao.update(task)

    suspend fun delete(task: Task) = taskDao.delete(task)

}