package com.example.classwork2.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 章节信息数据库实体
 * 
 * 对应数据库中的章节表，存储书籍章节的详细信息
 * 与BookEntity建立外键关系
 * 
 * @param id 章节主键
 * @param bookId 所属书籍ID（外键）
 * @param title 章节标题
 * @param pageCount 页数
 */
@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"])]
)
data class ChapterEntity(
    @PrimaryKey
    val id: String,
    val bookId: String,
    val title: String,
    val pageCount: Int
)