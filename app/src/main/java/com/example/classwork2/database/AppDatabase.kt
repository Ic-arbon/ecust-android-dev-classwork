package com.example.classwork2.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
 * @version 7 数据库版本号（为章节表添加翻译相关字段）
 * @exportSchema false 不导出schema文件
 */
@Database(
    entities = [
        UserEntity::class,
        BookEntity::class,
        ChapterEntity::class
    ],
    version = 7,
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
         * 数据库迁移：版本3到4
         * 将coverImageRes (INTEGER) 改为 coverImagePath (TEXT)
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建新的表结构
                database.execSQL("""
                    CREATE TABLE books_new (
                        id TEXT NOT NULL,
                        title TEXT NOT NULL,
                        author TEXT NOT NULL,
                        description TEXT NOT NULL,
                        coverImagePath TEXT,
                        lastUpdateTime INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                """.trimIndent())
                
                // 复制数据（coverImageRes设为null，因为无法转换为文件路径）
                database.execSQL("""
                    INSERT INTO books_new (id, title, author, description, coverImagePath, lastUpdateTime)
                    SELECT id, title, author, description, NULL, lastUpdateTime
                    FROM books
                """.trimIndent())
                
                // 删除旧表
                database.execSQL("DROP TABLE books")
                
                // 重命名新表
                database.execSQL("ALTER TABLE books_new RENAME TO books")
            }
        }
        
        /**
         * 数据库迁移：版本4到5
         * 为chapters表添加content字段
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 为chapters表添加content字段
                database.execSQL("ALTER TABLE chapters ADD COLUMN content TEXT")
            }
        }
        
        /**
         * 数据库迁移：版本5到6
         * 为chapters表添加url字段
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 为chapters表添加url字段
                database.execSQL("ALTER TABLE chapters ADD COLUMN url TEXT")
            }
        }
        
        /**
         * 数据库迁移：版本6到7
         * 为chapters表添加翻译相关字段
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 为chapters表添加翻译相关字段
                database.execSQL("ALTER TABLE chapters ADD COLUMN translatedContent TEXT")
                database.execSQL("ALTER TABLE chapters ADD COLUMN originalSentences TEXT")
                database.execSQL("ALTER TABLE chapters ADD COLUMN translatedSentences TEXT")
                database.execSQL("ALTER TABLE chapters ADD COLUMN translationStatus INTEGER NOT NULL DEFAULT 0")
            }
        }
        
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
                ).addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7) // 添加数据库迁移
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