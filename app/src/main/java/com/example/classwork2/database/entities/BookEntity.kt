package com.example.classwork2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 书籍信息数据库实体
 * 
 * 对应数据库中的书籍表，存储书籍的详细信息
 * 
 * @param id 书籍主键
 * @param title 书籍标题
 * @param author 作者
 * @param description 书籍描述
 * @param coverImageRes 封面图片资源ID，可为null使用默认封面
 * @param lastUpdateTime 最后更新时间（毫秒时间戳）
 */
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val coverImageRes: Int? = null,
    val lastUpdateTime: Long = System.currentTimeMillis()
)