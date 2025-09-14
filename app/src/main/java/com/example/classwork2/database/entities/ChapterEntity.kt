package com.example.classwork2.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 章节信息数据库实体
 * 
 * 对应数据库中的章节表，存储书籍章节的详细信息
 * 与BookEntity建立外键关系，支持层级章节结构
 * 
 * @param id 章节主键
 * @param bookId 所属书籍ID（外键）
 * @param title 章节标题
 * @param pageCount 页数
 * @param volumeTitle 卷/大章节标题（可选）
 * @param volumeOrder 卷序号（可选）
 * @param subOrder 卷内序号（可选）
 * @param chapterOrder 章节全局序号
 * @param content 章节正文内容（可选，按需加载）
 * @param url 章节原始URL（可选，用于获取章节内容）
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
    val pageCount: Int,
    val volumeTitle: String? = null,
    val volumeOrder: Int? = null,
    val subOrder: Int? = null,
    val chapterOrder: Int = 0,
    val content: String? = null,
    val url: String? = null
)