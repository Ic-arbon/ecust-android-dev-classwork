package com.example.classwork2.database.dao

import androidx.room.*
import com.example.classwork2.database.entities.BookEntity
import kotlinx.coroutines.flow.Flow

/**
 * 书籍数据访问对象
 * 
 * 定义与书籍表相关的数据库操作
 */
@Dao
interface BookDao {
    
    /**
     * 获取所有书籍
     * 使用Flow自动监听数据变化，按更新时间降序排列
     */
    @Query("SELECT * FROM books ORDER BY lastUpdateTime DESC")
    fun getAllBooks(): Flow<List<BookEntity>>
    
    /**
     * 根据ID获取单本书籍
     */
    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: String): BookEntity?
    
    /**
     * 根据标题搜索书籍
     */
    @Query("SELECT * FROM books WHERE title LIKE '%' || :title || '%' ORDER BY lastUpdateTime DESC")
    fun searchBooksByTitle(title: String): Flow<List<BookEntity>>
    
    /**
     * 根据作者搜索书籍
     */
    @Query("SELECT * FROM books WHERE author LIKE '%' || :author || '%' ORDER BY lastUpdateTime DESC")
    fun searchBooksByAuthor(author: String): Flow<List<BookEntity>>
    
    /**
     * 插入新书籍
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)
    
    /**
     * 批量插入书籍
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)
    
    /**
     * 更新书籍信息
     */
    @Update
    suspend fun updateBook(book: BookEntity)
    
    /**
     * 删除书籍
     */
    @Delete
    suspend fun deleteBook(book: BookEntity)
    
    /**
     * 根据ID删除书籍
     */
    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: String)
    
    /**
     * 清除所有书籍
     */
    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()
}