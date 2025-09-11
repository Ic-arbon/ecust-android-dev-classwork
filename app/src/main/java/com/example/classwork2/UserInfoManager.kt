package com.example.classwork2

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

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
 * 负责用户信息的持久化存储和读取，使用本地存储实现
 * 支持用户名和头像信息的保存，确保应用重启和屏幕旋转后数据不丢失
 */
class UserInfoManager(private val context: Context) {
    
    companion object {
        private const val STORAGE_NAME = "user_info"
        private const val KEY_USERNAME = "username"
        private const val KEY_AVATAR_TYPE = "avatar_type"
        private const val KEY_AVATAR_ICON_NAME = "avatar_icon_name"
        private const val KEY_AVATAR_DRAWABLE_RES = "avatar_drawable_res"
        
        // 头像类型标识
        private const val AVATAR_TYPE_ICON = "icon"
        private const val AVATAR_TYPE_IMAGE = "image"
    }
    
    private val userDataStorage: SharedPreferences =
        context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE)
    
    /**
     * 保存用户信息到本地存储
     * 
     * @param userInfo 要保存的用户信息
     */
    fun saveUserInfo(userInfo: UserInfo) {
        val editor = userDataStorage.edit()
        
        // 保存用户名
        editor.putString(KEY_USERNAME, userInfo.username)
        
        // 根据头像类型保存不同的信息
        when (val avatar = userInfo.userAvatar) {
            is AvatarType.IconAvatar -> {
                editor.putString(KEY_AVATAR_TYPE, AVATAR_TYPE_ICON)
                // 保存图标名称标识符
                editor.putString(KEY_AVATAR_ICON_NAME, avatar.iconName)
            }
            is AvatarType.ImageAvatar -> {
                editor.putString(KEY_AVATAR_TYPE, AVATAR_TYPE_IMAGE)
                editor.putInt(KEY_AVATAR_DRAWABLE_RES, avatar.drawableRes)
            }
        }
        
        editor.apply()
    }
    
    /**
     * 从本地存储读取用户信息
     * 
     * @return 用户信息，如果没有保存的信息则返回默认值
     */
    fun getUserInfo(): UserInfo {
        val username = userDataStorage.getString(KEY_USERNAME, "") ?: ""
        val avatarType = userDataStorage.getString(KEY_AVATAR_TYPE, AVATAR_TYPE_ICON) ?: AVATAR_TYPE_ICON
        
        val avatar = when (avatarType) {
            AVATAR_TYPE_ICON -> {
                // 从保存的图标名称重建IconAvatar
                val iconName = userDataStorage.getString(KEY_AVATAR_ICON_NAME, "person") ?: "person"
                AvatarType.IconAvatar(iconName)
            }
            AVATAR_TYPE_IMAGE -> {
                val drawableRes = userDataStorage.getInt(KEY_AVATAR_DRAWABLE_RES, R.drawable.av1)
                AvatarType.ImageAvatar(drawableRes)
            }
            else -> AvatarType.IconAvatar("person")
        }
        
        return UserInfo(username, avatar)
    }
    
    /**
     * 清除所有用户信息
     */
    fun clearUserInfo() {
        userDataStorage.edit().clear().apply()
    }
    
    /**
     * 检查是否有保存的用户信息
     * 
     * @return 如果有保存的用户名则返回true
     */
    fun hasUserInfo(): Boolean {
        val username = userDataStorage.getString(KEY_USERNAME, "")
        return !username.isNullOrEmpty()
    }
}