package com.example.classwork2.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户信息数据库实体
 * 
 * 对应数据库中的用户表，存储用户的基本信息
 * 
 * @param id 用户主键，自动生成
 * @param username 用户名
 * @param avatarType 头像类型（"icon"或"image"）
 * @param avatarIconName 图标头像名称（当avatarType为"icon"时使用）
 * @param avatarDrawableRes 图片头像资源ID（当avatarType为"image"时使用）
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val avatarType: String, // "icon" 或 "image"
    val avatarIconName: String? = null,
    val avatarDrawableRes: Int? = null
)