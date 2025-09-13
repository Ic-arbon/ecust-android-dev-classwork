package com.example.classwork2.network

/**
 * 网络书籍信息数据类
 * 
 * 从网站解析获得的书籍基本信息
 * 
 * @param title 书籍标题
 * @param author 作者名称
 * @param description 书籍简介描述
 * @param sourceUrl 原始网站链接
 * @param chapters 章节列表
 * @param totalWords 总字数（可选）
 * @param status 连载状态（连载中/已完结）
 * @param tags 标签列表
 * @param lastUpdateTime 最后更新时间（毫秒时间戳）
 */
data class NetworkBookInfo(
    val title: String,
    val author: String,
    val description: String,
    val sourceUrl: String,
    val chapters: List<ChapterInfo> = emptyList(),
    val totalWords: Int? = null,
    val status: String = "连载中",
    val tags: List<String> = emptyList(),
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * 章节信息数据类
 * 
 * @param title 章节标题
 * @param url 章节链接
 * @param order 章节序号
 * @param publishTime 发布时间（毫秒时间戳）
 * @param volumeTitle 卷/大章节标题（可选）
 * @param volumeOrder 卷序号（可选）
 * @param subOrder 卷内序号（可选）
 */
data class ChapterInfo(
    val title: String,
    val url: String,
    val order: Int,
    val publishTime: Long = System.currentTimeMillis(),
    val volumeTitle: String? = null,
    val volumeOrder: Int? = null,
    val subOrder: Int? = null
)

/**
 * 书籍网站解析器接口
 * 
 * 定义了从不同网站解析书籍信息的统一接口
 */
interface BookSiteParser {
    
    /**
     * 检查是否支持解析给定URL
     * 
     * @param url 网站URL
     * @return 是否支持解析此URL
     */
    fun canParse(url: String): Boolean
    
    /**
     * 从网页URL解析书籍信息
     * 
     * @param url 书籍页面URL
     * @return 解析得到的书籍信息，如果解析失败返回null
     */
    suspend fun parseBookInfo(url: String): NetworkBookInfo?
    
    /**
     * 获取网站名称
     * 
     * @return 网站显示名称
     */
    fun getSiteName(): String
}