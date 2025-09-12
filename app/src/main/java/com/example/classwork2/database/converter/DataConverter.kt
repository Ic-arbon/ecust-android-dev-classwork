package com.example.classwork2.database.converter

import com.example.classwork2.*
import com.example.classwork2.database.entities.BookEntity
import com.example.classwork2.database.entities.ChapterEntity
import com.example.classwork2.database.entities.UserEntity

/**
 * 数据转换工具类
 * 
 * 负责在数据库实体和UI模型之间进行转换
 */
object DataConverter {
    
    // ============ 用户数据转换 ============
    
    /**
     * UserInfo转换为UserEntity
     */
    fun userInfoToEntity(userInfo: UserInfo, id: Long = 0): UserEntity {
        return when (val avatar = userInfo.userAvatar) {
            is AvatarType.IconAvatar -> UserEntity(
                id = id,
                username = userInfo.username,
                avatarType = "icon",
                avatarIconName = avatar.iconName,
                avatarDrawableRes = null
            )
            is AvatarType.ImageAvatar -> UserEntity(
                id = id,
                username = userInfo.username,
                avatarType = "image",
                avatarIconName = null,
                avatarDrawableRes = avatar.drawableRes
            )
        }
    }
    
    /**
     * UserEntity转换为UserInfo
     */
    fun entityToUserInfo(entity: UserEntity): UserInfo {
        val avatar = when (entity.avatarType) {
            "icon" -> AvatarType.IconAvatar(entity.avatarIconName ?: "person")
            "image" -> AvatarType.ImageAvatar(entity.avatarDrawableRes ?: R.drawable.av1)
            else -> AvatarType.IconAvatar("person")
        }
        
        return UserInfo(
            username = entity.username,
            userAvatar = avatar
        )
    }
    
    // ============ 书籍数据转换 ============
    
    /**
     * Book转换为BookEntity
     */
    fun bookToEntity(book: Book): BookEntity {
        return BookEntity(
            id = book.id,
            title = book.title,
            author = book.author,
            description = book.description,
            coverImageRes = book.coverImageRes,
            lastUpdateTime = book.lastUpdateTime
        )
    }
    
    /**
     * BookEntity转换为Book（不包含章节）
     */
    fun entityToBook(entity: BookEntity, chapters: List<Chapter> = emptyList()): Book {
        return Book(
            id = entity.id,
            title = entity.title,
            author = entity.author,
            description = entity.description,
            coverImageRes = entity.coverImageRes,
            lastUpdateTime = entity.lastUpdateTime,
            chapters = chapters
        )
    }
    
    /**
     * Chapter转换为ChapterEntity
     */
    fun chapterToEntity(chapter: Chapter, bookId: String): ChapterEntity {
        return ChapterEntity(
            id = chapter.id,
            bookId = bookId,
            title = chapter.title,
            pageCount = chapter.pageCount
        )
    }
    
    /**
     * ChapterEntity转换为Chapter
     */
    fun entityToChapter(entity: ChapterEntity): Chapter {
        return Chapter(
            id = entity.id,
            title = entity.title,
            pageCount = entity.pageCount
        )
    }
    
    // ============ 批量转换 ============
    
    /**
     * 批量转换Book列表到Entity列表
     */
    fun booksToEntities(books: List<Book>): List<BookEntity> {
        return books.map { bookToEntity(it) }
    }
    
    /**
     * 批量转换BookEntity列表到Book列表
     */
    fun entitiesToBooks(entities: List<BookEntity>): List<Book> {
        return entities.map { entityToBook(it) }
    }
    
    /**
     * 批量转换Chapter列表到Entity列表
     */
    fun chaptersToEntities(chapters: List<Chapter>, bookId: String): List<ChapterEntity> {
        return chapters.map { chapterToEntity(it, bookId) }
    }
    
    /**
     * 批量转换ChapterEntity列表到Chapter列表
     */
    fun entitiesToChapters(entities: List<ChapterEntity>): List<Chapter> {
        return entities.map { entityToChapter(it) }
    }
    
    /**
     * 转换Book（包含章节）到数据库实体对
     */
    fun bookWithChaptersToEntities(book: Book): Pair<BookEntity, List<ChapterEntity>> {
        val bookEntity = bookToEntity(book)
        val chapterEntities = chaptersToEntities(book.chapters, book.id)
        return Pair(bookEntity, chapterEntities)
    }
}