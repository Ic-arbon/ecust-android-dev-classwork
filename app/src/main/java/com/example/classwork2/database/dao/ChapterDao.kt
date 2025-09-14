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
    @Query("UPDATE chapters SET content = :content WHERE id = :chapterId")
    suspend fun updateChapterContent(chapterId: String, content: String)
    
    /**
     * 检查章节是否已有内容
     */
    @Query("SELECT CASE WHEN content IS NOT NULL AND content != '' THEN 1 ELSE 0 END FROM chapters WHERE id = :chapterId")
    suspend fun hasChapterContent(chapterId: String): Boolean
}