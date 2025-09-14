package com.example.classwork2.network

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * 小説家になろう 内容解析器
 * 
 * 专门用于解析小説家になろう网站的章节内容和目录信息
 * 支持分页遍历，确保获取完整的章节列表
 * 新版本支持页面缓存和进度回调
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
     * 解析章节列表（支持分页和进度回调）
     * 
     * @param baseUrl 小说的基础URL
     * @param progressCallback 进度回调（可选）
     * @param cancellationToken 取消令牌（可选）
     * @return 完整的章节列表
     */
    suspend fun parseChapterList(
        baseUrl: String,
        progressCallback: ImportProgressCallback? = null,
        cancellationToken: CancellationToken? = null
    ): List<ChapterInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // 确保URL格式正确
                val novelUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                
                // 提取小说ID
                val novelId = extractNovelIdFromUrl(novelUrl)
                if (novelId.isEmpty()) {
                    progressCallback?.onError("无法提取小说ID from $novelUrl")
                    return@withContext emptyList()
                }
                
                progressCallback?.onProgressUpdate(
                    ImportProgress(ImportStage.PREPARING, 1, 2, "准备下载章节页面...")
                )
                
                // 第一阶段：下载所有页面到缓存
                val pageCache = downloadAllPages(novelUrl, progressCallback, cancellationToken)
                    ?: return@withContext emptyList()
                
                // 第二阶段：从缓存解析所有章节
                val chapters = parseChaptersFromCache(pageCache, novelId, progressCallback, cancellationToken)
                
                progressCallback?.onProgressUpdate(
                    ImportProgress(ImportStage.COMPLETED, 1, 1, "解析完成，共 ${chapters.size} 个章节")
                )
                progressCallback?.onComplete()
                
                return@withContext chapters.sortedBy { it.order }
                
            } catch (e: Exception) {
                val errorMsg = "解析章节列表失败: ${e.message}"
                println(errorMsg)
                e.printStackTrace()
                progressCallback?.onError(errorMsg)
                return@withContext emptyList()
            }
        }
    }
    
    /**
     * 向后兼容的解析方法（无进度回调）
     * 
     * @param baseUrl 小说的基础URL
     * @return 完整的章节列表
     */
    suspend fun parseChapterList(baseUrl: String): List<ChapterInfo> {
        return parseChapterList(baseUrl, null, null)
    }
    
    /**
     * 下载所有页面到缓存
     */
    private suspend fun downloadAllPages(
        novelUrl: String,
        progressCallback: ImportProgressCallback?,
        cancellationToken: CancellationToken?
    ): PageCache? {
        val pageCache = PageCache()
        var currentPage = 1
        var hasNextPage = true
        val totalPagesEstimate = mutableListOf<String>() // 用于估算总页数
        
        while (hasNextPage && currentPage <= MAX_PAGES) {
            // 检查取消状态
            if (cancellationToken?.isCancelled == true) {
                progressCallback?.onError("用户取消操作")
                return null
            }
            
            currentCoroutineContext().ensureActive() // 检查协程取消状态
            
            val pageUrl = if (currentPage == 1) {
                novelUrl
            } else {
                "${novelUrl}?p=$currentPage"
            }
            
            progressCallback?.onProgressUpdate(
                ImportProgress(
                    ImportStage.DOWNLOADING,
                    currentPage,
                    maxOf(currentPage + 1, totalPagesEstimate.size),
                    "正在下载第 $currentPage 页..."
                )
            )
            
            try {
                val document = Jsoup.connect(pageUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get()
                
                // 缓存页面
                pageCache.put(pageUrl, document)
                println("✅ 缓存页面: $pageUrl")
                
                // 检查是否有下一页
                hasNextPage = hasNextPageLink(document)
                
                if (hasNextPage) {
                    currentPage++
                    delay(REQUEST_DELAY_MS) // 请求间隔
                } else {
                    println("✅ 下载完成，共 $currentPage 页")
                }
                
            } catch (e: Exception) {
                val errorMsg = "下载页面失败: $pageUrl, 错误: ${e.message}"
                println("❌ $errorMsg")
                progressCallback?.onError(errorMsg)
                return null
            }
        }
        
        return pageCache
    }
    
    /**
     * 从缓存解析所有章节
     */
    private suspend fun parseChaptersFromCache(
        pageCache: PageCache,
        novelId: String,
        progressCallback: ImportProgressCallback?,
        cancellationToken: CancellationToken?
    ): List<ChapterInfo> {
        val allChapters = mutableListOf<ChapterInfo>()
        val cachedUrls = pageCache.getCachedUrls().sorted()
        
        // 跨页面保持的状态变量
        var lastVolumeTitle: String? = null
        var currentVolumeOrder = 1
        var lastSubChapterOrder = 1
        
        cachedUrls.forEachIndexed { index, pageUrl ->
            // 检查取消状态
            if (cancellationToken?.isCancelled == true) {
                progressCallback?.onError("用户取消操作")
                return emptyList()
            }
            
            currentCoroutineContext().ensureActive()
            
            progressCallback?.onProgressUpdate(
                ImportProgress(
                    ImportStage.PARSING,
                    index + 1,
                    cachedUrls.size,
                    "正在解析第 ${index + 1} 页章节..."
                )
            )
            
            val document = pageCache.get(pageUrl)
            if (document != null) {
                try {
                    val parseResult = parsePageChapters(
                        document, novelId, allChapters.size,
                        lastVolumeTitle, currentVolumeOrder, lastSubChapterOrder
                    )
                    val pageChapters = parseResult.chapters
                    
                    allChapters.addAll(pageChapters)
                    println("✅ 页面 ${index + 1} 解析到 ${pageChapters.size} 个章节")
                    
                    // 更新跨页面状态
                    lastVolumeTitle = parseResult.lastVolumeTitle
                    currentVolumeOrder = parseResult.lastVolumeOrder
                    lastSubChapterOrder = parseResult.lastSubChapterOrder
                    
                } catch (e: Exception) {
                    println("❌ 解析页面失败: $pageUrl, ${e.message}")
                }
            }
        }
        
        return allChapters
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
                                            order = chapterNumber ?: globalOrder, // 优先使用URL中的真实序号
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
    
    /**
     * 解析单个章节的内容
     * 
     * @param chapterUrl 章节页面URL
     * @return 章节正文内容
     */
    suspend fun parseChapterContent(chapterUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val document = Jsoup.connect(chapterUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get()
                
                // 提取章节内容
                extractChapterText(document)
                
            } catch (e: Exception) {
                println("❌ 解析章节内容失败: $chapterUrl, ${e.message}")
                e.printStackTrace()
                ""
            }
        }
    }
    
    /**
     * 从文档中提取章节正文
     */
    private fun extractChapterText(document: Document): String {
        try {
            // 小説家になろう的章节内容在 .p-novel__body 或 #novel_honbun 中
            val contentElement = document.select(".p-novel__body").first()
                ?: document.select("#novel_honbun").first()
                ?: document.select(".novel_view").first()
            
            if (contentElement != null) {
                // 移除不需要的元素（广告、注释等）
                contentElement.select(".ads, .ad, script, style").remove()
                
                // 改进的文本提取：更好地保留换行结构
                val text = contentElement.html()
                    .replace("<br\\s*/?>"," \n", ignoreCase = true) // 处理<br>和<br/>标签
                    .replace("</p>", "\n\n") // 段落结束添加双换行
                    .replace("<p[^>]*>", "") // 移除段落开始标签（包括带属性的）
                    .replace("</div>", "\n") // div结束添加换行
                    .replace("<div[^>]*>", "") // 移除div开始标签
                
                // 使用Jsoup清理剩余HTML标签，但保留我们添加的换行符
                val cleanText = Jsoup.parse(text).wholeText() // 使用wholeText()而不是text()来保留换行
                
                // 规范化空白字符，但保留段落结构
                return cleanText
                    .replace(Regex("[ \t]+"), " ") // 合并多余的空格和制表符
                    .replace(Regex("\n[ \t]*\n[ \t]*\n+"), "\n\n") // 最多保留双换行
                    .replace(Regex("^\n+"), "") // 移除开头的换行
                    .replace(Regex("\n+$"), "") // 移除结尾的换行
                    .trim()
            }
            
            return "未找到章节内容"
            
        } catch (e: Exception) {
            println("❌ 提取章节文本失败: ${e.message}")
            return "内容提取失败: ${e.message}"
        }
    }
}