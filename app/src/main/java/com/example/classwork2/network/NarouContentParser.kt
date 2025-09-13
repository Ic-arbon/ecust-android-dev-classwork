package com.example.classwork2.network

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

/**
 * 小説家になろう 内容解析器
 * 
 * 专门用于解析小説家になろう网站的章节内容和目录信息
 * 支持分页遍历，确保获取完整的章节列表
 */
class NarouContentParser {
    
    companion object {
        private const val SITE_DOMAIN = "ncode.syosetu.com"
        private const val SITE_NAME = "小説家になろう"
        
        // 请求间隔，避免对服务器造成压力
        private const val REQUEST_DELAY_MS = 1000L
        
        // 最大页面数限制，防止无限循环
        private const val MAX_PAGES = 100
    }
    
    /**
     * 检查URL是否可以被此解析器处理
     */
    fun canParse(url: String): Boolean {
        return url.contains(SITE_DOMAIN) && url.contains("/n")
    }
    
    /**
     * 获取网站名称
     */
    fun getSiteName(): String = SITE_NAME
    
    /**
     * 解析章节列表（支持分页）
     * 
     * @param baseUrl 小说的基础URL
     * @return 完整的章节列表
     */
    suspend fun parseChapterList(baseUrl: String): List<ChapterInfo> {
        return withContext(Dispatchers.IO) {
            val allChapters = mutableListOf<ChapterInfo>()
            
            try {
                // 确保URL格式正确
                val novelUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                
                // 提取小说ID
                val novelId = extractNovelIdFromUrl(novelUrl)
                if (novelId.isEmpty()) {
                    println("无法提取小说ID from $novelUrl")
                    return@withContext allChapters
                }
                
                var currentPage = 1
                var hasNextPage = true
                
                // 跨页面保持的状态变量
                var lastVolumeTitle: String? = null
                var currentVolumeOrder = 1
                var lastSubChapterOrder = 1
                
                while (hasNextPage && currentPage <= MAX_PAGES) {
                    println("正在解析第 $currentPage 页...")
                    
                    // 构建当前页的URL
                    val pageUrl = if (currentPage == 1) {
                        novelUrl
                    } else {
                        "${novelUrl}?p=$currentPage"
                    }
                    
                    // 获取页面内容
                    val document = try {
                        Jsoup.connect(pageUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(15000)
                            .get()
                    } catch (e: Exception) {
                        println("获取页面失败: $pageUrl, 错误: ${e.message}")
                        break
                    }
                    
                    // 解析当前页的章节，传入跨页面状态
                    val parseResult = parsePageChapters(document, novelId, allChapters.size, lastVolumeTitle, currentVolumeOrder, lastSubChapterOrder)
                    val pageChapters = parseResult.chapters
                    
                    if (pageChapters.isEmpty()) {
                        println("第 $currentPage 页没有找到章节，停止解析")
                        break
                    }
                    
                    allChapters.addAll(pageChapters)
                    println("第 $currentPage 页解析到 ${pageChapters.size} 个章节")
                    
                    // 更新跨页面状态
                    lastVolumeTitle = parseResult.lastVolumeTitle
                    currentVolumeOrder = parseResult.lastVolumeOrder
                    lastSubChapterOrder = parseResult.lastSubChapterOrder
                    
                    // 检查是否有下一页
                    hasNextPage = hasNextPageLink(document)
                    
                    if (hasNextPage) {
                        currentPage++
                        // 添加请求间隔
                        delay(REQUEST_DELAY_MS)
                    }
                }
                
                println("总共解析到 ${allChapters.size} 个章节，共 $currentPage 页")
                
            } catch (e: Exception) {
                println("解析章节列表失败: ${e.message}")
                e.printStackTrace()
            }
            
            return@withContext allChapters.sortedBy { it.order }
        }
    }
    
    /**
     * 页面解析结果
     */
    private data class PageParseResult(
        val chapters: List<ChapterInfo>,
        val lastVolumeTitle: String?,
        val lastVolumeOrder: Int,
        val lastSubChapterOrder: Int
    )
    
    /**
     * 解析单个页面的章节
     */
    private fun parsePageChapters(
        document: Document, 
        novelId: String, 
        startOrder: Int,
        inheritedVolumeTitle: String? = null,
        inheritedVolumeOrder: Int = 1,
        inheritedSubChapterOrder: Int = 1
    ): PageParseResult {
        val chapters = mutableListOf<ChapterInfo>()
        
        // 继承上一页的卷状态
        var currentVolumeTitle: String? = inheritedVolumeTitle
        var currentVolumeOrder = inheritedVolumeOrder
        var subChapterOrder = inheritedSubChapterOrder
        var globalOrder = startOrder + 1
        
        try {
            // 获取所有章节容器元素
            val eplistElements = document.select(".p-eplist > *")
            
            for (element in eplistElements) {
                when {
                    // 检查是否是大章节标题
                    element.hasClass("p-eplist__chapter-title") -> {
                        val newVolumeTitle = element.text().trim()
                        // 只有在真正发现新的大章节时才更新
                        if (newVolumeTitle != currentVolumeTitle) {
                            currentVolumeTitle = newVolumeTitle
                            currentVolumeOrder++
                            subChapterOrder = 1 // 重置子章节计数
                            println("发现新大章节: $currentVolumeTitle (卷序号: $currentVolumeOrder)")
                        }
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
                                            order = globalOrder,
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
            }
            
        } catch (e: Exception) {
            println("解析页面章节失败: ${e.message}")
            e.printStackTrace()
        }
        
        return PageParseResult(chapters, currentVolumeTitle, currentVolumeOrder, subChapterOrder)
    }
    
    /**
     * 检查页面是否有下一页链接
     */
    private fun hasNextPageLink(document: Document): Boolean {
        // 查找分页导航中的"次へ"（下一页）链接
        val nextLink = document.select("a:contains(次へ)").first()
            ?: document.select("a:contains(→)").first()
            ?: document.select(".c-pager__item--next a").first()
        
        return nextLink != null
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
                // 这里可以实现更精确的时间解析
                // 为了简化，暂时返回当前时间
                System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}