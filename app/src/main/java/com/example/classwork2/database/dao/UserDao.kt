package com.example.classwork2.database.dao

import androidx.room.*
import com.example.classwork2.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * 用户数据访问对象
 * 
 * 定义与用户表相关的数据库操作
 */
@Dao
interface UserDao {
    
    /**
     * 获取所有用户
     * 使用Flow自动监听数据变化
     */
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    /**
     * 根据用户名获取用户
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?
    
    /**
     * 获取当前活跃用户（假设只有一个用户）
     */
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?
    
    /**
     * 插入新用户
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long
    
    /**
     * 更新用户信息
     */
    @Update
    suspend fun updateUser(user: UserEntity)
    
    /**
     * 删除用户
     */
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    /**
     * 清除所有用户
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}