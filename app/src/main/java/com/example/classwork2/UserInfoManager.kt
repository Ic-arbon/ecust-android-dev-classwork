package com.example.classwork2

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import com.example.classwork2.database.AppDatabase
import com.example.classwork2.database.converter.DataConverter
import com.example.classwork2.database.repository.UserRepository
import kotlinx.coroutines.runBlocking

/**
 * 用户信息数据类
 * 
 * 用于在应用内传递和存储用户信息
 * 
 * @param username 用户名
 * @param userAvatar 用户头像
 */
data class UserInfo(
    val username: String,
    val userAvatar: AvatarType
)

/**
 * 用户信息管理器
 * 
 * 负责用户信息的持久化存储和读取，现在使用Room数据库替代SharedPreferences
 * 支持用户名和头像信息的保存，确保应用重启和屏幕旋转后数据不丢失
 */
class UserInfoManager(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val userRepository = UserRepository(database.userDao())
    
    /**
     * 保存用户信息到数据库
     * 
     * @param userInfo 要保存的用户信息
     */
    fun saveUserInfo(userInfo: UserInfo) {
        runBlocking {
            // 先清除现有用户（保持单用户模式）
            userRepository.clearAllUsers()
            
            // 保存新用户信息
            val userEntity = DataConverter.userInfoToEntity(userInfo)
            userRepository.saveUser(userEntity)
        }
    }
    
    /**
     * 从数据库读取用户信息
     * 
     * @return 用户信息，如果没有保存的信息则返回默认值
     */
    fun getUserInfo(): UserInfo {
        return runBlocking {
            val userEntity = userRepository.getCurrentUser()
            if (userEntity != null) {
                DataConverter.entityToUserInfo(userEntity)
            } else {
                // 返回默认用户信息
                UserInfo("", AvatarType.IconAvatar("person"))
            }
        }
    }
    
    /**
     * 清除所有用户信息
     */
    fun clearUserInfo() {
        runBlocking {
            userRepository.clearAllUsers()
        }
    }
    
    /**
     * 检查是否有保存的用户信息
     * 
     * @return 如果有保存的用户名则返回true
     */
    fun hasUserInfo(): Boolean {
        return runBlocking {
            val userInfo = getUserInfo()
            userInfo.username.isNotEmpty()
        }
    }
}