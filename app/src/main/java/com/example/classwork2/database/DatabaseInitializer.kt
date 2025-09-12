package com.example.classwork2.database

import android.content.Context
import com.example.classwork2.Book
import com.example.classwork2.Chapter
import com.example.classwork2.R
import com.example.classwork2.database.converter.DataConverter
import com.example.classwork2.database.repository.BookRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 数据库初始化器
 * 
 * 负责初始化数据库并导入示例数据
 */
class DatabaseInitializer(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val bookRepository = BookRepository(database.bookDao(), database.chapterDao())
    
    /**
     * 初始化数据库，导入示例书籍数据
     */
    fun initializeDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            // 检查是否已有数据
            val existingBooks = bookRepository.getAllBooks()
            
            // 如果数据库为空，则导入示例数据
            existingBooks.collect { books ->
                if (books.isEmpty()) {
                    importSampleBooks()
                }
                return@collect // 只执行一次检查
            }
        }
    }
    
    /**
     * 导入示例书籍数据
     */
    private suspend fun importSampleBooks() {
        val sampleBooks = getSampleBooks()
        
        val booksWithChapters = sampleBooks.map { book ->
            DataConverter.bookWithChaptersToEntities(book)
        }
        
        bookRepository.insertBooksWithChapters(booksWithChapters)
    }
    
    /**
     * 强制重新导入示例数据（清除现有数据）
     */
    suspend fun forceReinitialize() {
        bookRepository.clearAllBooks()
        importSampleBooks()
    }
    
    /**
     * 获取示例书籍数据
     * 
     * 只保留3本示例书籍，并添加更新时间
     */
    private fun getSampleBooks(): List<Book> {
        val currentTime = System.currentTimeMillis()
        return listOf(
            Book(
                id = "1",
                title = "魔法原理与实践",
                author = "阿尔巴斯·邓布利多",
                description = "这是一本全面介绍魔法基础理论和实践应用的经典教材。从基础魔法原理到高级咒语应用，为魔法学习者提供系统性的知识框架。",
                coverImageRes = R.drawable.maodie,
                lastUpdateTime = currentTime - 86400000, // 1天前
                chapters = listOf(
                    Chapter("1-1", "魔法的起源", 45),
                    Chapter("1-2", "基础魔法理论", 38),
                    Chapter("1-3", "魔法能量控制", 52),
                    Chapter("1-4", "咒语构造原理", 41),
                    Chapter("1-5", "实践练习指南", 35)
                )
            ),
            Book(
                id = "2",
                title = "古代咒语大全",
                author = "梅林",
                description = "收录了数千年来最重要的古代咒语，包括失传的禁咒和保护咒语。每个咒语都有详细的施法说明和历史背景。",
                lastUpdateTime = currentTime - 43200000, // 12小时前
                chapters = listOf(
                    Chapter("2-1", "远古时代咒语", 65),
                    Chapter("2-2", "治疗系咒语", 48),
                    Chapter("2-3", "攻击系咒语", 72),
                    Chapter("2-4", "防护系咒语", 56),
                    Chapter("2-5", "禁咒警示录", 29)
                )
            ),
            Book(
                id = "3",
                title = "炼金术基础",
                author = "尼古拉·勒梅",
                description = "炼金术的入门指南，从基础材料识别到复杂的炼制过程。包含详细的实验步骤和安全注意事项。",
                lastUpdateTime = currentTime, // 刚刚更新
                chapters = listOf(
                    Chapter("3-1", "炼金术历史", 32),
                    Chapter("3-2", "基础材料学", 44),
                    Chapter("3-3", "设备与工具", 28),
                    Chapter("3-4", "初级炼制技巧", 58),
                    Chapter("3-5", "高级合成方法", 67)
                )
            )
        )
    }
}