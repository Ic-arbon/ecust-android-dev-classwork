package com.example.classwork2.network.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * 小説家になろう API 服务
 * 
 * 提供官方API的调用功能，用于获取准确的小说元数据
 */
interface NarouApi {
    
    /**
     * 根据ncode获取小说信息
     * 
     * @param ncode 小说编号（如：n1234ab）
     * @param outputFormat 输出格式（默认json）
     * @param gzip 是否启用gzip压缩（1-5，推荐5）
     * @param fields 指定输出字段
     */
    @GET("novelapi/api/")
    suspend fun getNovelInfo(
        @Query("ncode") ncode: String,
        @Query("out") outputFormat: String = "json",
        @Query("of") fields: String = "t-n-u-w-s-bg-g-k-gf-gl-nt-e-ga-l-ti-gp-dp-wp-mp-qp-yp-f-imp-r-a-ah-sa-ka-nu-ua"
    ): List<NarouNovelInfo>
}

/**
 * 小説家になろう API 服务实现
 * 
 * 封装API调用逻辑，提供便捷的访问方法
 */
class NarouApiService {
    
    companion object {
        private const val BASE_URL = "https://api.syosetu.com/"
        private const val NCODE_PATTERN = "[Nn][0-9]{4}[A-Za-z]{2}"
        
        /**
         * URL中提取ncode的正则表达式
         * 支持格式：
         * - https://ncode.syosetu.com/n1234ab/
         * - https://novel18.syosetu.com/n1234ab/
         * - ncode.syosetu.com/n1234ab/
         */
        private val URL_NCODE_PATTERN = Pattern.compile("(?:https?://)?(?:ncode|novel18)\\.syosetu\\.com/($NCODE_PATTERN)/?")
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 提升日志级别用于调试
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val api = retrofit.create(NarouApi::class.java)
    
    /**
     * 从URL中提取ncode
     * 
     * @param url 小说URL
     * @return ncode或null（如果提取失败）
     */
    fun extractNcodeFromUrl(url: String): String? {
        println("=== [API] 提取ncode ===")
        println("原始URL: $url")
        println("正则模式: $URL_NCODE_PATTERN")
        
        val matcher = URL_NCODE_PATTERN.matcher(url)
        return if (matcher.find()) {
            val ncode = matcher.group(1)?.lowercase()
            println("✅ 提取成功: $ncode")
            ncode
        } else {
            println("❌ 提取失败: URL格式不匹配")
            println("   支持的格式:")
            println("   - https://ncode.syosetu.com/n1234ab/")
            println("   - https://novel18.syosetu.com/n1234ab/")
            null
        }
    }
    
    /**
     * 验证ncode格式是否正确
     * 
     * @param ncode 待验证的ncode
     * @return 是否为有效的ncode格式
     */
    fun isValidNcode(ncode: String): Boolean {
        return ncode.matches(Regex(NCODE_PATTERN, RegexOption.IGNORE_CASE))
    }
    
    /**
     * 根据URL获取小说信息
     * 
     * @param url 小说URL
     * @return 小说信息或null（如果获取失败）
     */
    suspend fun getNovelInfoByUrl(url: String): NarouNovelInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val ncode = extractNcodeFromUrl(url)
                if (ncode == null) {
                    println("无法从 URL 提取 ncode: $url")
                    return@withContext null
                }
                
                println("提取到 ncode: $ncode")
                println("调用 API: https://api.syosetu.com/novelapi/api/?ncode=$ncode&out=json")
                
                val response = api.getNovelInfo(ncode = ncode)
                println("API 返回数组大小: ${response.size}")
                
                // API返回数组，第一个元素是统计信息，第二个元素是实际数据
                if (response.size > 1) {
                    val novelInfo = response[1]
                    println("API 调用成功，获取到书籍信息: ${novelInfo.title}")
                    return@withContext novelInfo
                } else {
                    println("未找到书籍数据，可能是无效的 ncode")
                    return@withContext null
                }
                
            } catch (e: Exception) {
                println("API 调用失败: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * 根据ncode获取小说信息
     * 
     * @param ncode 小说编号
     * @return 小说信息或null（如果获取失败）
     */
    suspend fun getNovelInfoByNcode(ncode: String): NarouNovelInfo? {
        return withContext(Dispatchers.IO) {
            try {
                if (!isValidNcode(ncode)) {
                    println("ncode 格式不正确: $ncode")
                    return@withContext null
                }
                
                println("调用 API, ncode: $ncode")
                val response = api.getNovelInfo(ncode = ncode.lowercase())
                println("API 返回数组大小: ${response.size}")
                
                // API返回数组，第一个元素是统计信息，第二个元素是实际数据
                if (response.size > 1) {
                    val novelInfo = response[1]
                    println("API 调用成功，获取到书籍信息: ${novelInfo.title}")
                    return@withContext novelInfo
                } else {
                    println("未找到书籍数据，可能是无效的 ncode")
                    return@withContext null
                }
                
            } catch (e: Exception) {
                println("API 调用失败: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * 检查URL是否为支持的小説家になろう链接
     * 
     * @param url 待检查的URL
     * @return 是否为支持的链接
     */
    fun isSupportedUrl(url: String): Boolean {
        return extractNcodeFromUrl(url) != null
    }
}