package com.example.classwork2.database.repository

import com.example.classwork2.database.dao.BookDao
import com.example.classwork2.database.dao.ChapterDao
import com.example.classwork2.database.entities.BookEntity
import com.example.classwork2.database.entities.ChapterEntity
import kotlinx.coroutines.flow.Flow

/**
 * 书籍数据仓库
 * 
 * 封装书籍和章节数据访问逻辑，提供统一的数据操作接口
 * 同时管理书籍和章节数据，因为它们关系密切
 */
class BookRepository(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao
) {
    
    // ============ 书籍相关操作 ============
    
    /**
     * 获取所有书籍（Flow）
     */
    fun getAllBooks(): Flow<List<BookEntity>> = bookDao.getAllBooks()
    
    /**
     * 根据ID获取书籍
     */
    suspend fun getBookById(bookId: String): BookEntity? {
        return bookDao.getBookById(bookId)
    }
    
    /**
     * 根据标题搜索书籍
     */
    fun searchBooksByTitle(title: String): Flow<List<BookEntity>> {
        return bookDao.searchBooksByTitle(title)
    }
    
    /**
     * 根据作者搜索书籍
     */
    fun searchBooksByAuthor(author: String): Flow<List<BookEntity>> {
        return bookDao.searchBooksByAuthor(author)
    }
    
    /**
     * 插入单本书籍
     */
    suspend fun insertBook(book: BookEntity) {
        bookDao.insertBook(book)
    }
    
    /**
     * 批量插入书籍
     */
    suspend fun insertBooks(books: List<BookEntity>) {
        bookDao.insertBooks(books)
    }
    
    /**
     * 更新书籍信息
     */
    suspend fun updateBook(book: BookEntity) {
        bookDao.updateBook(book)
    }
    
    /**
     * 删除书籍（同时删除相关章节）
     */
    suspend fun deleteBook(book: BookEntity) {
        // 先删除章节，再删除书籍（由于外键约束，这是自动的，但明确执行更安全）
        chapterDao.deleteChaptersByBookId(book.id)
        bookDao.deleteBook(book)
    }
    
    /**
     * 清除所有书籍数据
     */
    suspend fun clearAllBooks() {
        chapterDao.deleteAllChapters()
        bookDao.deleteAllBooks()
    }
    
    // ============ 章节相关操作 ============
    
    /**
     * 获取指定书籍的章节列表
     */
    fun getChaptersByBookId(bookId: String): Flow<List<ChapterEntity>> {
        return chapterDao.getChaptersByBookId(bookId)
    }
    
    /**
     * 根据ID获取章节
     */
    suspend fun getChapterById(chapterId: String): ChapterEntity? {
        return chapterDao.getChapterById(chapterId)
    }
    
    /**
     * 插入单个章节
     */
    suspend fun insertChapter(chapter: ChapterEntity) {
        chapterDao.insertChapter(chapter)
    }
    
    /**
     * 批量插入章节
     */
    suspend fun insertChapters(chapters: List<ChapterEntity>) {
        chapterDao.insertChapters(chapters)
    }
    
    /**
     * 更新章节信息
     */
    suspend fun updateChapter(chapter: ChapterEntity) {
        chapterDao.updateChapter(chapter)
    }
    
    /**
     * 删除章节
     */
    suspend fun deleteChapter(chapter: ChapterEntity) {
        chapterDao.deleteChapter(chapter)
    }
    
    // ============ 复合操作 ============
    
    /**
     * 插入书籍及其章节（事务操作）
     */
    suspend fun insertBookWithChapters(book: BookEntity, chapters: List<ChapterEntity>) {
        bookDao.insertBook(book)
        if (chapters.isNotEmpty()) {
            chapterDao.insertChapters(chapters)
        }
    }
    
    /**
     * 批量插入书籍及其章节
     */
    suspend fun insertBooksWithChapters(booksWithChapters: List<Pair<BookEntity, List<ChapterEntity>>>) {
        booksWithChapters.forEach { (book, chapters) ->
            insertBookWithChapters(book, chapters)
        }
    }
}