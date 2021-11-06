package de.david072.schoolplanner.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.david072.schoolplanner.database.daos.SubjectDao
import de.david072.schoolplanner.database.daos.TaskDao
import de.david072.schoolplanner.database.entities.Subject
import de.david072.schoolplanner.database.entities.Task

@Database(entities = [Task::class, Subject::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun subjectDao(): SubjectDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun instance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance =
                Room.databaseBuilder(context, AppDatabase::class.java, "school-planner_database")
                    .build()
            instance!!
        }
    }
}