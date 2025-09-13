package com.example.classwork2.network

import com.example.classwork2.network.api.NarouApiService
import com.example.classwork2.network.api.NarouNovelInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 小説家になろう 书籍导入器
 * 
 * 整合官方API和HTML解析，提供完整的书籍导入功能
 * - 使用官方API获取准确的元数据
 * - 使用HTML解析获取完整的章节列表（支持分页）
 */
class NarouBookImporter : BookSiteParser {
    
    private val apiService = NarouApiService()
    private val contentParser = NarouContentParser()
    
    // 日期格式解析器
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    override fun canParse(url: String): Boolean {
        return apiService.isSupportedUrl(url)
    }
    
    override fun getSiteName(): String = "小説家になろう (API + 解析)"
    
    override suspend fun parseBookInfo(url: String): NetworkBookInfo? {
        return parseBookInfo(url, null, null)
    }
    
    /**
     * 带进度回调的解析方法
     */
    suspend fun parseBookInfo(
        url: String,
        progressCallback: ImportProgressCallback?,
        cancellationToken: CancellationToken?
    ): NetworkBookInfo? {
        return withContext(Dispatchers.IO) {
            try {
                println("=== [NarouBookImporter] 开始导入 ===")
                println("输入URL: $url")
                println("检查URL是否支持: ${canParse(url)}")
                
                progressCallback?.onProgressUpdate(
                    ImportProgress(ImportStage.PREPARING, 1, 3, "准备获取API数据...")
                )
                
                // 检查取消状态
                if (cancellationToken?.isCancelled == true) {
                    progressCallback?.onError("用户取消操作")
                    return@withContext null
                }
                
                // 第一步：使用API获取准确的元数据
                println("=== [步骤1] 调用API获取元数据 ===")
                progressCallback?.onProgressUpdate(
                    ImportProgress(ImportStage.PREPARING, 2, 3, "正在调用API获取元数据...")
                )
                
                val apiInfo = apiService.getNovelInfoByUrl(url)
                if (apiInfo == null) {
                    val errorMsg = "API获取信息失败，可能原因：URL格式不正确、网络连接问题、API服务异常或该小说不存在"
                    println("❌ $errorMsg")
                    progressCallback?.onError(errorMsg)
                    return@withContext null
                }
                
                println("✅ API获取成功!")
                println("   标题: ${apiInfo.title}")
                println("   作者: ${apiInfo.author}")
                println("   类型: ${apiInfo.getStatusText()}")
                println("   总集数: ${apiInfo.totalEpisodes}")
                println("   ncode: ${apiInfo.ncode}")
                println("   最后更新: ${apiInfo.lastUpdatedAt}")
                
                // 第二步：解析完整的章节列表（支持分页和进度回调）
                val chapters = if (apiInfo.totalEpisodes > 0) {
                    println("开始解析章节列表...")
                    progressCallback?.onProgressUpdate(
                        ImportProgress(ImportStage.PREPARING, 3, 3, "开始解析章节列表...")
                    )
                    
                    // 使用带进度回调的新解析器
                    contentParser.parseChapterList(url, progressCallback, cancellationToken)
                } else {
                    println("短篇小说，无章节列表")
                    progressCallback?.onProgressUpdate(
                        ImportProgress(ImportStage.COMPLETED, 1, 1, "短篇小说，无章节列表")
                    )
                    emptyList()
                }
                
                // 检查是否被取消
                if (cancellationToken?.isCancelled == true) {
                    progressCallback?.onError("用户取消操作")
                    return@withContext null
                }
                
                println("章节解析完成，共 ${chapters.size} 章")
                
                // 第三步：整合数据
                progressCallback?.onProgressUpdate(
                    ImportProgress(ImportStage.SAVING, 1, 1, "整合书籍数据...")
                )
                
                val networkBookInfo = NetworkBookInfo(
                    title = apiInfo.title,
                    author = apiInfo.author,
                    description = apiInfo.description.ifBlank { "暂无简介" },
                    sourceUrl = url,
                    chapters = chapters,
                    status = apiInfo.getStatusText(),
                    tags = apiInfo.getKeywordList(),
                    lastUpdateTime = parseApiDate(apiInfo.lastUpdatedAt)
                )
                
                println("导入完成: ${networkBookInfo.title}")
                println("- 作者: ${networkBookInfo.author}")
                println("- 状态: ${networkBookInfo.status}")
                println("- 章节数: ${networkBookInfo.chapters.size}")
                println("- 标签: ${networkBookInfo.tags.joinToString(", ")}")
                
                return@withContext networkBookInfo
                
            } catch (e: Exception) {
                val errorMsg = "导入失败: ${e.message}"
                println("❌ [错误] $errorMsg")
                println("   异常类型: ${e::class.java.simpleName}")
                e.printStackTrace()
                progressCallback?.onError(errorMsg)
                return@withContext null
            }
        }
    }
    
    /**
     * 解析API返回的日期字符串
     */
    private fun parseApiDate(dateString: String): Long {
        return try {
            if (dateString.isBlank()) {
                System.currentTimeMillis()
            } else {
                // API返回格式: "2023-12-25 10:30:45"
                apiDateFormat.parse(dateString)?.time ?: System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    /**
     * 预览书籍信息（仅使用API，不解析章节）
     * 用于快速预览，避免长时间的章节解析
     */
    suspend fun previewBookInfo(
        url: String,
        progressCallback: ImportProgressCallback? = null
    ): NetworkBookInfo? {
        return withContext(Dispatchers.IO) {
            try {
                println("=== [NarouBookImporter] 快速预览模式 ===")
                println("输入URL: $url")
                
                progressCallback?.onProgressUpdate(
                    ImportProgress(ImportStage.PREPARING, 1, 2, "正在获取书籍基本信息...")
                )
                
                val apiInfo = apiService.getNovelInfoByUrl(url)
                if (apiInfo == null) {
                    val errorMsg = "预览失败: API返回空数据"
                    println("❌ $errorMsg")
                    progressCallback?.onError(errorMsg)
                    return@withContext null
                }
                
                println("✅ 预览成功: ${apiInfo.title}")
                
                progressCallback?.onProgressUpdate(
                    ImportProgress(ImportStage.COMPLETED, 2, 2, "预览完成")
                )
                progressCallback?.onComplete()
                
                NetworkBookInfo(
                    title = apiInfo.title,
                    author = apiInfo.author,
                    description = apiInfo.description.ifBlank { "暂无简介" },
                    sourceUrl = url,
                    chapters = emptyList(), // 预览模式不解析章节
                    status = apiInfo.getStatusText(),
                    tags = apiInfo.getKeywordList(),
                    lastUpdateTime = parseApiDate(apiInfo.lastUpdatedAt)
                )
            } catch (e: Exception) {
                val errorMsg = "预览失败: ${e.message}"
                println("❌ [错误] $errorMsg")
                e.printStackTrace()
                progressCallback?.onError(errorMsg)
                null
            }
        }
    }
    
    /**
     * 获取详细的导入统计信息
     */
    data class ImportStats(
        val title: String,
        val author: String,
        val status: String,
        val totalEpisodes: Int,
        val actualChapters: Int,
        val totalLength: String,
        val lastUpdate: String,
        val tags: List<String>
    )
    
    /**
     * 获取导入统计（仅API数据，快速获取）
     */
    suspend fun getImportStats(url: String): ImportStats? {
        return withContext(Dispatchers.IO) {
            try {
                val apiInfo = apiService.getNovelInfoByUrl(url)
                    ?: return@withContext null
                
                ImportStats(
                    title = apiInfo.title,
                    author = apiInfo.author,
                    status = apiInfo.getStatusText(),
                    totalEpisodes = apiInfo.totalEpisodes,
                    actualChapters = 0, // 需要解析才能知道
                    totalLength = apiInfo.getFormattedLength(),
                    lastUpdate = apiInfo.lastUpdatedAt,
                    tags = apiInfo.getKeywordList()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}