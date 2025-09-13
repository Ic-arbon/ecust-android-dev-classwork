package com.example.classwork2.network

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 小説家になろう (ncode.syosetu.com) 网站解析器
 * 
 * 专门用于解析小説家になろう网站的书籍信息
 * URL格式: https://ncode.syosetu.com/[作品ID]/
 */
class SyosetuParser : BookSiteParser {
    
    companion object {
        private const val SITE_DOMAIN = "ncode.syosetu.com"
        private const val SITE_NAME = "小説家になろう"
        
        // 日期格式解析器（针对网站的日期格式）
        private val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
    }
    
    override fun canParse(url: String): Boolean {
        return url.contains(SITE_DOMAIN) && url.contains("/n")
    }
    
    override fun getSiteName(): String = SITE_NAME
    
    override suspend fun parseBookInfo(url: String): NetworkBookInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get()
                
                parseDocumentToBookInfo(document, url)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * 从Jsoup文档解析书籍信息
     */
    private fun parseDocumentToBookInfo(document: Document, sourceUrl: String): NetworkBookInfo? {
        try {
            // 解析书名 - 通常在页面标题中
            val title = document.select("title").text()
                .replace("【完結済み】", "")  // 移除完结标识
                .replace("【連載中】", "")    // 移除连载标识
                .replace(" - ハーメルン", "") // 移除网站名称
                .trim()
            
            if (title.isEmpty()) return null
            
            // 解析作者 - 查找作者链接
            val author = document.select("a[href*=\"mypage.syosetu.com\"]").first()?.text()
                ?: document.select(".novel_writername").text()
                ?: "未知作者"
            
            // 解析简介 - 查找作品简介
            val description = document.select("#novel_ex").text()
                ?: document.select(".novel_ex").text() 
                ?: "暂无简介"
            
            // 解析状态 - 检查是否完结
            val status = if (document.select("title").text().contains("完結")) {
                "已完结"
            } else {
                "连载中"
            }
            
            // 解析章节列表
            val chapters = parseChapterList(document)
            
            // 解析标签
            val tags = document.select(".novel_genre").map { it.text() }
            
            // 计算最后更新时间 - 从最新章节获取
            val lastUpdateTime = chapters.maxOfOrNull { it.publishTime } 
                ?: System.currentTimeMillis()
            
            return NetworkBookInfo(
                title = title,
                author = author,
                description = description,
                sourceUrl = sourceUrl,
                chapters = chapters,
                status = status,
                tags = tags,
                lastUpdateTime = lastUpdateTime
            )
            
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 解析章节列表
     */
    private fun parseChapterList(document: Document): List<ChapterInfo> {
        val chapters = mutableListOf<ChapterInfo>()
        
        try {
            // 获取当前小说的ID，用于匹配章节链接
            val currentUrl = document.baseUri()
            val novelId = extractNovelIdFromUrl(currentUrl)
            
            if (novelId.isEmpty()) {
                println("无法提取小说ID")
                return chapters
            }
            
            // 获取所有章节容器元素，按顺序遍历
            val eplistElements = document.select(".p-eplist > *")
            
            var currentVolumeTitle: String? = null
            var currentVolumeOrder = 1
            var subChapterOrder = 1
            var globalOrder = 1
            
            println("找到 ${eplistElements.size} 个章节相关元素")
            
            for (element in eplistElements) {
                when {
                    // 检查是否是大章节标题
                    element.hasClass("p-eplist__chapter-title") -> {
                        currentVolumeTitle = element.text().trim()
                        subChapterOrder = 1 // 重置子章节计数
                        println("发现大章节: $currentVolumeTitle")
                    }
                    
                    // 检查是否是子话
                    element.hasClass("p-eplist__sublist") -> {
                        try {
                            // 查找子话中的链接
                            val chapterLink = element.select("a.p-eplist__subtitle").first()
                            if (chapterLink != null) {
                                val chapterTitle = chapterLink.text().trim()
                                val chapterUrl = chapterLink.attr("href")
                                
                                // 验证是否是有效的章节链接
                                if (chapterUrl.matches(Regex("/$novelId/\\d+/?$"))) {
                                    // 构建完整URL
                                    val fullUrl = if (chapterUrl.startsWith("http")) {
                                        chapterUrl
                                    } else {
                                        "https://ncode.syosetu.com$chapterUrl"
                                    }
                                    
                                    // 提取章节编号
                                    val chapterNumber = extractChapterNumber(chapterUrl)
                                    
                                    // 解析更新时间
                                    val updateElement = element.select(".p-eplist__update").first()
                                    val updateTime = parseUpdateTime(updateElement?.text() ?: "")
                                    
                                    chapters.add(
                                        ChapterInfo(
                                            title = chapterTitle,
                                            url = fullUrl,
                                            order = chapterNumber ?: globalOrder,
                                            publishTime = updateTime,
                                            volumeTitle = currentVolumeTitle,
                                            volumeOrder = currentVolumeOrder,
                                            subOrder = subChapterOrder
                                        )
                                    )
                                    
                                    println("解析子话: [$currentVolumeTitle] $chapterTitle -> $fullUrl")
                                    
                                    subChapterOrder++
                                    globalOrder++
                                }
                            }
                        } catch (e: Exception) {
                            println("解析子话失败: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }
                
                // 如果遇到新的大章节，增加卷序号
                if (element.hasClass("p-eplist__chapter-title")) {
                    if (currentVolumeTitle != null && chapters.isNotEmpty()) {
                        currentVolumeOrder++
                    }
                }
            }
            
            println("总共解析到 ${chapters.size} 个章节")
        } catch (e: Exception) {
            println("解析章节列表失败: ${e.message}")
            e.printStackTrace()
        }
        
        // 按章节顺序排序
        return chapters.sortedBy { it.order }
    }
    
    /**
     * 从URL中提取小说ID
     */
    private fun extractNovelIdFromUrl(url: String): String {
        return try {
            val regex = Regex("/([^/]+?)/?$")
            regex.find(url)?.groupValues?.get(1) ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * 从章节URL中提取章节编号
     */
    private fun extractChapterNumber(chapterUrl: String): Int? {
        return try {
            val regex = Regex("/(\\d+)/?$")
            regex.find(chapterUrl)?.groupValues?.get(1)?.toInt()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 解析更新时间字符串
     */
    private fun parseUpdateTime(timeText: String): Long {
        return try {
            // 提取时间部分，格式类似 "2018/06/01 17:51"
            val timeRegex = Regex("(\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2})")
            val matchResult = timeRegex.find(timeText)
            
            if (matchResult != null) {
                val timeString = matchResult.groupValues[1]
                DATE_FORMAT.parse(timeString)?.time ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    /**
     * 解析时间字符串为时间戳
     */
    private fun parseTimeString(timeText: String): Long {
        return try {
            // 尝试解析常见的时间格式
            when {
                timeText.matches(Regex("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}")) -> {
                    DATE_FORMAT.parse(timeText)?.time ?: System.currentTimeMillis()
                }
                timeText.contains("今日") -> {
                    System.currentTimeMillis()
                }
                timeText.contains("昨日") -> {
                    System.currentTimeMillis() - 24 * 60 * 60 * 1000
                }
                else -> {
                    System.currentTimeMillis()
                }
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}