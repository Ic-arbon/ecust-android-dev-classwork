package com.example.classwork2.database.dao

import androidx.room.*
import com.example.classwork2.database.entities.ChapterEntity
import kotlinx.coroutines.flow.Flow

/**
 * 章节数据访问对象
 * 
 * 定义与章节表相关的数据库操作
 */
@Dao
interface ChapterDao {
    
    /**
     * 获取指定书籍的所有章节
     * 按章节序号排序
     */
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterOrder")
    fun getChaptersByBookId(bookId: String): Flow<List<ChapterEntity>>
    
    /**
     * 根据章节ID获取章节
     */
    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: String): ChapterEntity?
    
    /**
     * 获取所有章节
     */
    @Query("SELECT * FROM chapters ORDER BY bookId, chapterOrder")
    fun getAllChapters(): Flow<List<ChapterEntity>>
    
    /**
     * 插入新章节
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)
    
    /**
     * 批量插入章节
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)
    
    /**
     * 更新章节信息
     */
    @Update
    suspend fun updateChapter(chapter: ChapterEntity)
    
    /**
     * 删除章节
     */
    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)
    
    /**
     * 根据章节ID删除章节
     */
    @Query("DELETE FROM chapters WHERE id = :chapterId")
    suspend fun deleteChapterById(chapterId: String)
    
    /**
     * 删除指定书籍的所有章节
     */
    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersByBookId(bookId: String)
    
    /**
     * 清除所有章节
     */
    @Query("DELETE FROM chapters")
    suspend fun deleteAllChapters()
    
    /**
     * 更新章节内容
     */
    @Query("UPDATE chapters SET content = :content, updateTime = :updateTime WHERE id = :chapterId")
    suspend fun updateChapterContent(chapterId: String, content: String, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 检查章节是否已有内容
     */
    @Query("SELECT CASE WHEN content IS NOT NULL AND content != '' THEN 1 ELSE 0 END FROM chapters WHERE id = :chapterId")
    suspend fun hasChapterContent(chapterId: String): Boolean
    
    // ========== 翻译相关方法 ==========
    
    /**
     * 更新章节翻译内容
     */
    @Query("UPDATE chapters SET translatedContent = :translatedContent, updateTime = :updateTime WHERE id = :chapterId")
    suspend fun updateChapterTranslation(chapterId: String, translatedContent: String, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 更新章节翻译状态
     */
    @Query("UPDATE chapters SET translationStatus = :status WHERE id = :chapterId")
    suspend fun updateTranslationStatus(chapterId: String, status: Int)
    
    /**
     * 更新章节原文句子列表
     */
    @Query("UPDATE chapters SET originalSentences = :originalSentences WHERE id = :chapterId")
    suspend fun updateOriginalSentences(chapterId: String, originalSentences: String)
    
    /**
     * 更新章节译文句子列表
     */
    @Query("UPDATE chapters SET translatedSentences = :translatedSentences WHERE id = :chapterId")
    suspend fun updateTranslatedSentences(chapterId: String, translatedSentences: String)
    
    /**
     * 批量更新翻译数据
     */
    @Query("""
        UPDATE chapters 
        SET translatedContent = :translatedContent,
            originalSentences = :originalSentences,
            translatedSentences = :translatedSentences,
            translationStatus = :status,
            updateTime = :updateTime
        WHERE id = :chapterId
    """)
    suspend fun updateTranslationData(
        chapterId: String,
        translatedContent: String?,
        originalSentences: String?,
        translatedSentences: String?,
        status: Int,
        updateTime: Long = System.currentTimeMillis()
    )
    
    /**
     * 检查章节是否已有翻译
     */
    @Query("SELECT CASE WHEN translationStatus = 2 AND translatedContent IS NOT NULL THEN 1 ELSE 0 END FROM chapters WHERE id = :chapterId")
    suspend fun hasChapterTranslation(chapterId: String): Boolean
    
    /**
     * 获取章节翻译状态
     */
    @Query("SELECT translationStatus FROM chapters WHERE id = :chapterId")
    suspend fun getTranslationStatus(chapterId: String): Int?
    
    /**
     * 清除章节翻译数据
     */
    @Query("""
        UPDATE chapters 
        SET translatedContent = NULL,
            originalSentences = NULL,
            translatedSentences = NULL,
            translationStatus = 0
        WHERE id = :chapterId
    """)
    suspend fun clearTranslationData(chapterId: String)
    
    /**
     * 获取指定书籍中已翻译的章节数量
     */
    @Query("SELECT COUNT(*) FROM chapters WHERE bookId = :bookId AND translationStatus = 2")
    suspend fun getTranslatedChapterCount(bookId: String): Int
    
    /**
     * 获取指定书籍的章节总数
     */
    @Query("SELECT COUNT(*) FROM chapters WHERE bookId = :bookId")
    suspend fun getTotalChapterCount(bookId: String): Int
    
    // ========== 更新时间相关方法 ==========
    
    /**
     * 更新章节的更新时间
     */
    @Query("UPDATE chapters SET updateTime = :updateTime WHERE id = :chapterId")
    suspend fun updateChapterTime(chapterId: String, updateTime: Long = System.currentTimeMillis())
    
    /**
     * 获取指定书籍的章节，按更新时间倒序排列
     */
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY updateTime DESC")
    fun getChaptersByBookIdOrderByUpdateTime(bookId: String): Flow<List<ChapterEntity>>
    
    /**
     * 获取最近更新的章节
     */
    @Query("SELECT * FROM chapters ORDER BY updateTime DESC LIMIT :limit")
    fun getRecentlyUpdatedChapters(limit: Int = 10): Flow<List<ChapterEntity>>
    
    /**
     * 获取指定时间之后更新的章节
     */
    @Query("SELECT * FROM chapters WHERE updateTime > :timestamp ORDER BY updateTime DESC")
    fun getChaptersUpdatedAfter(timestamp: Long): Flow<List<ChapterEntity>>
}