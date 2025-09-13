package com.example.classwork2.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.classwork2.database.dao.BookDao
import com.example.classwork2.database.dao.ChapterDao
import com.example.classwork2.database.dao.UserDao
import com.example.classwork2.database.entities.BookEntity
import com.example.classwork2.database.entities.ChapterEntity
import com.example.classwork2.database.entities.UserEntity

/**
 * 应用主数据库类
 * 
 * 使用Room框架管理SQLite数据库
 * 包含用户、书籍和章节三个表
 * 
 * @version 3 数据库版本号（新增章节层级结构支持）
 * @exportSchema false 不导出schema文件
 */
@Database(
    entities = [
        UserEntity::class,
        BookEntity::class,
        ChapterEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * 获取用户数据访问对象
     */
    abstract fun userDao(): UserDao
    
    /**
     * 获取书籍数据访问对象
     */
    abstract fun bookDao(): BookDao
    
    /**
     * 获取章节数据访问对象
     */
    abstract fun chapterDao(): ChapterDao
    
    companion object {
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * 获取数据库实例（单例模式）
         * 
         * @param context 应用上下文
         * @return 数据库实例
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "magic_library_database"
                ).fallbackToDestructiveMigration() // 简单处理：重建数据库
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * 关闭数据库连接（用于测试）
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}