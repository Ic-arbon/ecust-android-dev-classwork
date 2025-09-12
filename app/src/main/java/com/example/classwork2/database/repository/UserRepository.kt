package com.example.classwork2.database.repository

import com.example.classwork2.database.dao.UserDao
import com.example.classwork2.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * 用户数据仓库
 * 
 * 封装用户数据访问逻辑，提供统一的数据操作接口
 * 作为DAO层和UI层之间的中介
 */
class UserRepository(private val userDao: UserDao) {
    
    /**
     * 获取所有用户（Flow）
     */
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    
    /**
     * 根据用户名获取用户
     */
    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }
    
    /**
     * 获取当前用户
     */
    suspend fun getCurrentUser(): UserEntity? {
        return userDao.getCurrentUser()
    }
    
    /**
     * 保存用户信息
     * 如果用户已存在则更新，否则插入新用户
     */
    suspend fun saveUser(user: UserEntity): Long {
        return userDao.insertUser(user)
    }
    
    /**
     * 更新用户信息
     */
    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }
    
    /**
     * 删除用户
     */
    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }
    
    /**
     * 清除所有用户信息
     */
    suspend fun clearAllUsers() {
        userDao.deleteAllUsers()
    }
}