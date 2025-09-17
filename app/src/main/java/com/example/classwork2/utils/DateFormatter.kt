package com.example.classwork2.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 日期格式化工具类
 * 
 * 提供统一的日期格式化功能，用于在UI中显示日期时间
 */
object DateFormatter {
    
    private val fullDateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * 格式化为完整日期时间
     * 
     * @param timestamp 时间戳（毫秒）
     * @return 格式化后的日期字符串，如 "2024年03月15日 14:30"
     */
    fun formatFullDateTime(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }
    
    /**
     * 格式化为简短日期时间
     * 
     * @param timestamp 时间戳（毫秒）
     * @return 格式化后的日期字符串，如 "03-15 14:30"
     */
    fun formatShortDateTime(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }
    
    /**
     * 格式化为仅日期
     * 
     * @param timestamp 时间戳（毫秒）
     * @return 格式化后的日期字符串，如 "2024-03-15"
     */
    fun formatDateOnly(timestamp: Long): String {
        return dateOnlyFormat.format(Date(timestamp))
    }
    
    /**
     * 智能格式化日期
     * 根据时间距离当前时间的长短，选择合适的格式
     * 
     * @param timestamp 时间戳（毫秒）
     * @return 格式化后的日期字符串
     */
    fun formatSmartDateTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diffDays = (now - timestamp) / (24 * 60 * 60 * 1000)
        
        return when {
            diffDays < 7 -> formatShortDateTime(timestamp) // 一周内显示简短格式
            else -> formatDateOnly(timestamp) // 一周外只显示日期
        }
    }
}