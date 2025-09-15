package com.example.classwork2.database.entities

/**
 * 翻译状态常量
 */
object TranslationStatus {
    const val NOT_TRANSLATED = 0
    const val TRANSLATING = 1  
    const val COMPLETED = 2
    const val ERROR = 3
    
    /**
     * 获取状态描述
     */
    fun getStatusDescription(status: Int): String {
        return when (status) {
            NOT_TRANSLATED -> "未翻译"
            TRANSLATING -> "翻译中"
            COMPLETED -> "已完成"
            ERROR -> "翻译失败"
            else -> "未知状态"
        }
    }
}