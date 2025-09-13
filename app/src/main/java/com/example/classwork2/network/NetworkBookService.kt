package com.example.classwork2.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 网络书籍导入服务
 *
 * 统一管理不同网站的书籍信息获取功能 支持多个网站解析器的注册和调用
 */
class NetworkBookService {

    private val parsers = mutableListOf<BookSiteParser>()

    init {
        // 注册默认支持的网站解析器
        registerParser(NarouBookImporter()) // 新的API+解析器
        // 可以在这里添加更多网站的解析器
        // registerParser(KakuyomuParser())
        // registerParser(AlphapolisParser())
    }

    /**
     * 注册新的网站解析器
     *
     * @param parser 网站解析器实例
     */
    fun registerParser(parser: BookSiteParser) {
        parsers.add(parser)
    }

    /**
     * 获取支持的网站列表
     *
     * @return 支持的网站名称列表
     */
    fun getSupportedSites(): List<String> {
        return parsers.map { it.getSiteName() }
    }

    /**
     * 检查URL是否被支持
     *
     * @param url 网址
     * @return 是否有对应的解析器支持此URL
     */
    fun isSupportedUrl(url: String): Boolean {
        return parsers.any { it.canParse(url) }
    }

    /**
     * 从网络URL导入书籍信息
     *
     * @param url 书籍页面URL
     * @return 导入结果，包含书籍信息和导入状态
     */
    suspend fun importBookFromUrl(url: String): ImportResult {
        return withContext(Dispatchers.IO) {
            try {
                println("=== [NetworkBookService] 开始导入 ===")
                println("输入URL: $url")
                println("可用解析器: ${parsers.map { it.getSiteName() }}")

                // 查找能处理此URL的解析器
                val parser =
                        parsers.firstOrNull {
                            val canParse = it.canParse(url)
                            println("解析器 ${it.getSiteName()}: ${if (canParse) "✅ 支持" else "❌ 不支持"}")
                            canParse
                        }

                if (parser == null) {
                    println("❌ 未找到支持的解析器")
                    return@withContext ImportResult.Error("不支持的网站: $url")
                }

                println("✅ 使用解析器: ${parser.getSiteName()}")

                // 解析书籍信息
                println("开始调用解析器...")
                val bookInfo = parser.parseBookInfo(url)

                if (bookInfo == null) {
                    println("❌ 解析器返回空结果")
                    return@withContext ImportResult.Error("解析失败: 无法从页面获取书籍信息")
                }

                println("✅ 解析器返回结果: ${bookInfo.title}")

                // 验证必要信息
                if (bookInfo.title.isBlank()) {
                    return@withContext ImportResult.Error("解析失败: 未找到书籍标题")
                }

                ImportResult.Success(bookInfo, parser.getSiteName())
            } catch (e: Exception) {
                ImportResult.Error("网络错误: ${e.message}")
            }
        }
    }

    /**
     * 从网络URL预览书籍信息（不保存到数据库）
     *
     * @param url 书籍页面URL
     * @return 预览结果
     */
    suspend fun previewBookFromUrl(url: String): ImportResult {
        return withContext(Dispatchers.IO) {
            try {
                // 查找能处理此URL的解析器
                val parser =
                        parsers.firstOrNull { it.canParse(url) }
                                ?: return@withContext ImportResult.Error("不支持的网站: $url")

                // 如果是Narou导入器，使用快速预览模式
                val bookInfo =
                        if (parser is NarouBookImporter) {
                            parser.previewBookInfo(url)
                        } else {
                            parser.parseBookInfo(url)
                        }

                if (bookInfo == null) {
                    return@withContext ImportResult.Error("解析失败: 无法从页面获取书籍信息")
                }

                // 验证必要信息
                if (bookInfo.title.isBlank()) {
                    return@withContext ImportResult.Error("解析失败: 未找到书籍标题")
                }

                ImportResult.Success(bookInfo, parser.getSiteName())
            } catch (e: Exception) {
                ImportResult.Error("网络错误: ${e.message}")
            }
        }
    }
}

/** 书籍导入结果密封类 */
sealed class ImportResult {
    /**
     * 导入成功
     *
     * @param bookInfo 解析得到的书籍信息
     * @param siteName 来源网站名称
     */
    data class Success(val bookInfo: NetworkBookInfo, val siteName: String) : ImportResult()

    /**
     * 导入失败
     *
     * @param message 错误信息
     */
    data class Error(val message: String) : ImportResult()
}

