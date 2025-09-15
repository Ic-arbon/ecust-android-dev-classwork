package com.example.classwork2.utils

import java.util.regex.Pattern

/**
 * 文本处理工具类
 * 提供句子分割、清理和格式化功能
 */
class TextProcessor {
    
    companion object {
        // 保留类结构，但移除不再需要的标点符号常量
    }
    
    /**
     * 基于换行符的句子分割
     * 因为HTML标签已在解析阶段转换为换行符，直接按换行分割即可
     * 
     * @param text 原始文本（HTML标签已转换为换行符）
     * @return 分割后的句子列表
     */
    fun splitIntoSentences(text: String): List<String> {
        println("=== [TextProcessor] 开始句子分割（基于换行符）===")
        println("原始文本长度: ${text.length}")
        println("原始文本前100字符: ${text.take(100)}")
        
        if (text.isBlank()) {
            println("文本为空，返回空列表")
            return emptyList()
        }
        
        // 基于换行符分割，因为HTML标签已在解析阶段转换为换行符
        val lines = text.split('\n')
        
        // 清理和过滤
        val sentences = lines
            .map { it.trim() }           // 清理前后空白
            .filter { it.isNotEmpty() }  // 过滤空行
        
        println("=== [TextProcessor] 分句完成 ===")
        println("最终句子数量: ${sentences.size}")
        sentences.forEachIndexed { index, sentence ->
            println("句子 ${index + 1}: \"$sentence\"")
        }
        
        return sentences
    }
    
    /**
     * 清理HTML标签和特殊字符
     * 增强版本，基于现有的extractChapterText逻辑
     */
    fun cleanText(rawText: String): String {
        return rawText
            .replace(Regex("<[^>]+>"), " ") // 移除HTML标签
            .replace(Regex("&[a-zA-Z]+;"), " ") // 移除HTML实体
            .replace(Regex("\\s+"), " ") // 合并多余空格
            .replace(Regex("^\\s+|\\s+$"), "") // 移除首尾空格
            .trim()
    }
    
    /**
     * 为翻译准备文本
     * 将句子列表组合成适合API翻译的格式
     */
    fun prepareForTranslation(sentences: List<String>): String {
        return sentences.joinToString("\n") { sentence ->
            "【句子】$sentence"
        }
    }
    
    /**
     * 解析翻译结果
     * 从API响应中提取翻译后的句子
     */
    fun parseTranslationResult(translatedText: String): List<String> {
        val sentences = mutableListOf<String>()
        val lines = translatedText.split("\n")
        
        for (line in lines) {
            val trimmed = line.trim()
            // 匹配翻译格式：【翻译】或【句子】等
            if (trimmed.startsWith("【") && trimmed.contains("】")) {
                val content = trimmed.substringAfter("】").trim()
                if (content.isNotEmpty()) {
                    sentences.add(content)
                }
            }
            // 如果没有标记，直接使用内容
            else if (trimmed.isNotEmpty() && !trimmed.startsWith("【")) {
                sentences.add(trimmed)
            }
        }
        
        return sentences
    }
    
    /**
     * 检测文本主要语言
     * 用于选择合适的分割策略
     */
    fun detectLanguage(text: String): TextLanguage {
        val sampleText = text.take(200) // 取前200字符进行检测
        
        var japaneseCount = 0
        var chineseCount = 0
        var englishCount = 0
        
        for (char in sampleText) {
            when {
                // 日文假名
                char in '\u3040'..'\u309F' || char in '\u30A0'..'\u30FF' -> japaneseCount++
                // 中文汉字（简体为主）
                char in '\u4E00'..'\u9FFF' -> chineseCount++
                // 英文字母
                char in 'A'..'Z' || char in 'a'..'z' -> englishCount++
            }
        }
        
        return when {
            japaneseCount > chineseCount && japaneseCount > englishCount -> TextLanguage.JAPANESE
            chineseCount > englishCount -> TextLanguage.CHINESE
            else -> TextLanguage.ENGLISH
        }
    }
}

/**
 * 文本语言枚举
 */
enum class TextLanguage {
    CHINESE,
    JAPANESE,
    ENGLISH
}