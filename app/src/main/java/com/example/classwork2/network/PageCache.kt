package com.example.classwork2.network

import org.jsoup.nodes.Document
import java.util.concurrent.ConcurrentHashMap
import java.util.Collections

/**
 * HTML页面缓存管理器
 * 
 * 使用LRU策略管理HTML页面缓存，避免内存溢出
 */
class PageCache(
    private val maxSize: Int = 50 // 最大缓存页面数
) {
    // 使用LRU LinkedHashMap实现缓存
    private val cache = Collections.synchronizedMap(
        object : LinkedHashMap<String, CachedPage>(maxSize, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CachedPage>?): Boolean {
                return size > maxSize
            }
        }
    )
    
    /**
     * 缓存的页面数据
     */
    data class CachedPage(
        val url: String,
        val document: Document,
        val cachedTime: Long = System.currentTimeMillis()
    )
    
    /**
     * 添加页面到缓存
     */
    fun put(url: String, document: Document) {
        cache[url] = CachedPage(url, document)
    }
    
    /**
     * 从缓存获取页面
     */
    fun get(url: String): Document? {
        return cache[url]?.document
    }
    
    /**
     * 检查缓存中是否包含指定页面
     */
    fun contains(url: String): Boolean {
        return cache.containsKey(url)
    }
    
    /**
     * 获取缓存大小
     */
    fun size(): Int {
        return cache.size
    }
    
    /**
     * 清空缓存
     */
    fun clear() {
        cache.clear()
    }
    
    /**
     * 获取所有缓存的URL
     */
    fun getCachedUrls(): Set<String> {
        return cache.keys.toSet()
    }
    
    /**
     * 移除指定URL的缓存
     */
    fun remove(url: String): Document? {
        return cache.remove(url)?.document
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getStats(): CacheStats {
        return CacheStats(
            size = cache.size,
            maxSize = maxSize,
            urls = cache.keys.toList()
        )
    }
    
    /**
     * 缓存统计信息
     */
    data class CacheStats(
        val size: Int,
        val maxSize: Int,
        val urls: List<String>
    )
}