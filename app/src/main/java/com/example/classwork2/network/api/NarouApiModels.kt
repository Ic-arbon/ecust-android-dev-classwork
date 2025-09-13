package com.example.classwork2.network.api

import com.google.gson.annotations.SerializedName

/**
 * 小説家になろう API 响应数据模型
 * 
 * 官方API文档：https://dev.syosetu.com/man/api/
 */

/**
 * API响应根对象
 */
data class NarouApiResponse(
    @SerializedName("allcount")
    val totalCount: Int = 0,
    
    @SerializedName("novels")
    val novels: List<NarouNovelInfo> = emptyList()
)

/**
 * 小说信息数据类
 * 
 * 包含从API获取的小说元数据
 */
data class NarouNovelInfo(
    @SerializedName("title")
    val title: String = "",
    
    @SerializedName("ncode")
    val ncode: String = "",
    
    @SerializedName("userid")
    val userId: Int = 0,
    
    @SerializedName("writer")
    val author: String = "",
    
    @SerializedName("story")
    val description: String = "",
    
    @SerializedName("biggenre")
    val bigGenre: Int = 0,
    
    @SerializedName("genre")
    val genre: Int = 0,
    
    @SerializedName("keyword")
    val keywords: String = "",
    
    @SerializedName("general_firstup")
    val firstPublishedAt: String = "",
    
    @SerializedName("general_lastup")
    val lastUpdatedAt: String = "",
    
    @SerializedName("novel_type")
    val novelType: Int = 0, // 1: 短篇, 2: 连载中, 3: 完结
    
    @SerializedName("end")
    val isCompleted: Int = 0, // 0: 连载中, 1: 完结
    
    @SerializedName("general_all_no")
    val totalEpisodes: Int = 0,
    
    @SerializedName("length")
    val totalLength: Int = 0,
    
    @SerializedName("time")
    val readingTime: Int = 0,
    
    @SerializedName("global_point")
    val globalPoint: Int = 0,
    
    @SerializedName("daily_point")
    val dailyPoint: Int = 0,
    
    @SerializedName("weekly_point")
    val weeklyPoint: Int = 0,
    
    @SerializedName("monthly_point")
    val monthlyPoint: Int = 0,
    
    @SerializedName("quarter_point")
    val quarterPoint: Int = 0,
    
    @SerializedName("yearly_point")
    val yearlyPoint: Int = 0,
    
    @SerializedName("fav_novel_cnt")
    val favoriteCount: Int = 0,
    
    @SerializedName("impression_cnt")
    val impressionCount: Int = 0,
    
    @SerializedName("review_cnt")
    val reviewCount: Int = 0,
    
    @SerializedName("all_point")
    val allPoint: Int = 0,
    
    @SerializedName("all_hyoka_cnt")
    val allReviewCount: Int = 0,
    
    @SerializedName("sasie_cnt")
    val illustrationCount: Int = 0,
    
    @SerializedName("kaiwaritu")
    val dialogueRate: Int = 0,
    
    @SerializedName("novelupdated_at")
    val novelUpdatedAt: String = "",
    
    @SerializedName("updated_at")
    val updatedAt: String = ""
) {
    /**
     * 获取小说状态文本
     */
    fun getStatusText(): String {
        return when {
            novelType == 1 -> "短篇"
            isCompleted == 1 -> "已完结"
            else -> "连载中"
        }
    }
    
    /**
     * 获取小说网址
     */
    fun getNovelUrl(): String {
        return "https://ncode.syosetu.com/$ncode/"
    }
    
    /**
     * 获取关键词列表
     */
    fun getKeywordList(): List<String> {
        return if (keywords.isBlank()) {
            emptyList()
        } else {
            keywords.split(" ").filter { it.isNotBlank() }
        }
    }
    
    /**
     * 格式化字数
     */
    fun getFormattedLength(): String {
        return when {
            totalLength >= 10000 -> "${totalLength / 10000}万字"
            totalLength >= 1000 -> "${(totalLength / 1000.0).let { "%.1f".format(it) }}千字"
            else -> "${totalLength}字"
        }
    }
}