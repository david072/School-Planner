package de.david072.schoolplanner.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.david072.schoolplanner.database.daos.ExamDao
import de.david072.schoolplanner.database.daos.SubjectDao
import de.david072.schoolplanner.database.daos.TaskDao
import de.david072.schoolplanner.database.entities.Exam
import de.david072.schoolplanner.database.entities.Subject
import de.david072.schoolplanner.database.entities.Task

@Database(entities = [Task::class, Exam::class, Subject::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun examDao(): ExamDao
    abstract fun subjectDao(): SubjectDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun instance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance =
                Room.databaseBuilder(context, AppDatabase::class.java, "school-planner_database")
                    .addMigrations(object : Migration(1, 2) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("CREATE TABLE IF NOT EXISTS `exams` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `due_date` INTEGER NOT NULL, `reminder` INTEGER NOT NULL, `subject_id` INTEGER NOT NULL, `description` TEXT)")
                        }
                    })
                    .build()
            instance!!
        }
    }
}
