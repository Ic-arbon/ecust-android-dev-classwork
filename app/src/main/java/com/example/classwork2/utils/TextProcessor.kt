package com.example.classwork2.utils

import java.util.regex.Pattern

/**
 * 文本处理工具类
 * 提供句子分割、清理和格式化功能
 */
class TextProcessor {
    
    companion object {
        // 日文句号和标点
        private val JAPANESE_SENTENCE_ENDINGS = arrayOf(
            "。", "！", "？", "…", "‥"
        )
        
        // 中文句号和标点
        private val CHINESE_SENTENCE_ENDINGS = arrayOf(
            "。", "！", "？", "…", "．"
        )
        
        // 英文句号和标点
        private val ENGLISH_SENTENCE_ENDINGS = arrayOf(
            ".", "!", "?", "..."
        )
        
        // 引号和括号
        private val QUOTE_ENDINGS = arrayOf(
            "」", "』", "\"", "'", ")", "）", "】", "〉", "》"
        )
        
        // 段落分隔符
        private val PARAGRAPH_SEPARATORS = arrayOf(
            "\n\n", "\r\n\r\n"
        )
    }
    
    /**
     * 智能句子分割
     * 基于标点符号和语言特征分割句子，保持语义完整性
     * 
     * @param text 原始文本
     * @return 分割后的句子列表
     */
    fun splitIntoSentences(text: String): List<String> {
        println("=== [TextProcessor] 开始句子分割 ===")
        println("原始文本长度: ${text.length}")
        println("原始文本前100字符: ${text.take(100)}")
        
        if (text.isBlank()) {
            println("文本为空，返回空列表")
            return emptyList()
        }
        
        val sentences = mutableListOf<String>()
        var currentSentence = StringBuilder()
        var i = 0
        
        // 预处理：标准化换行符
        val normalizedText = text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .trim()
        
        while (i < normalizedText.length) {
            val currentChar = normalizedText[i]
            currentSentence.append(currentChar)
            
            // 检查是否为句子结束标点
            if (isSentenceEnding(currentChar)) {
                // 检查后续字符
                val nextIndex = i + 1
                var shouldEnd = true
                
                // 处理引号结尾的情况
                if (nextIndex < normalizedText.length) {
                    val nextChar = normalizedText[nextIndex]
                    
                    // 如果句号后面跟着引号，继续包含
                    if (isQuoteEnding(nextChar)) {
                        currentSentence.append(nextChar)
                        i++
                    }
                    // 如果后面紧跟小写字母或数字，可能不是句子结尾
                    else if (nextChar.isLowerCase() || nextChar.isDigit()) {
                        shouldEnd = false
                    }
                }
                
                if (shouldEnd) {
                    val sentence = currentSentence.toString().trim()
                    if (sentence.isNotEmpty()) {
                        sentences.add(sentence)
                        println("分割出句子 ${sentences.size}: $sentence")
                    }
                    currentSentence.clear()
                }
            }
            // 检查段落分隔
            else if (currentChar == '\n') {
                // 检查是否为段落分隔（双换行）
                if (i + 1 < normalizedText.length && normalizedText[i + 1] == '\n') {
                    val sentence = currentSentence.toString().trim()
                    if (sentence.isNotEmpty()) {
                        sentences.add(sentence)
                        println("段落分隔出句子 ${sentences.size}: $sentence")
                    }
                    currentSentence.clear()
                    // 跳过第二个换行符
                    i++
                }
                // 单换行符转为空格，保持句子连续性
                else {
                    currentSentence.setLength(currentSentence.length - 1)
                    currentSentence.append(" ")
                }
            }
            
            i++
        }
        
        // 处理最后剩余的文本
        val lastSentence = currentSentence.toString().trim()
        if (lastSentence.isNotEmpty()) {
            sentences.add(lastSentence)
        }
        
        // 后处理：合并过短的句子和清理
        return postProcessSentences(sentences)
    }
    
    /**
     * 检查字符是否为句子结束标点
     */
    private fun isSentenceEnding(char: Char): Boolean {
        val charStr = char.toString()
        return JAPANESE_SENTENCE_ENDINGS.contains(charStr) ||
                CHINESE_SENTENCE_ENDINGS.contains(charStr) ||
                ENGLISH_SENTENCE_ENDINGS.contains(charStr)
    }
    
    /**
     * 检查字符是否为引号结尾
     */
    private fun isQuoteEnding(char: Char): Boolean {
        val charStr = char.toString()
        return QUOTE_ENDINGS.contains(charStr)
    }
    
    /**
     * 后处理句子列表
     * 合并过短的句子，清理空白字符
     */
    private fun postProcessSentences(sentences: List<String>): List<String> {
        println("=== [TextProcessor] 开始后处理 ===")
        println("待处理句子数量: ${sentences.size}")
        sentences.forEachIndexed { index, sentence ->
            println("句子 ${index + 1} (${sentence.length}字符): $sentence")
        }
        
        val processed = mutableListOf<String>()
        var i = 0
        
        while (i < sentences.size) {
            var currentSentence = sentences[i].trim()
            println("处理句子 ${i + 1}: \"$currentSentence\" (长度: ${currentSentence.length})")
            
            // 如果当前句子太短（少于3个字符），尝试与下一句合并
            if (currentSentence.length < 3 && i + 1 < sentences.size) {
                val nextSentence = sentences[i + 1].trim()
                println("句子过短，与下一句合并: \"$nextSentence\"")
                currentSentence = "$currentSentence $nextSentence"
                i++ // 跳过下一句
                println("合并后: \"$currentSentence\" (长度: ${currentSentence.length})")
            }
            
            // 清理多余的空白字符
            val cleanedSentence = currentSentence
                .replace(Regex("\\s+"), " ")
                .trim()
            
            if (cleanedSentence != currentSentence) {
                println("清理空白字符: \"$currentSentence\" -> \"$cleanedSentence\"")
            }
            currentSentence = cleanedSentence
            
            if (currentSentence.isNotEmpty()) {
                processed.add(currentSentence)
                println("添加到结果: 第${processed.size}句 - \"$currentSentence\"")
            } else {
                println("句子为空，跳过")
            }
            
            i++
        }
        
        println("=== [TextProcessor] 后处理完成 ===")
        println("最终句子数量: ${processed.size}")
        processed.forEachIndexed { index, sentence ->
            println("最终句子 ${index + 1}: \"$sentence\"")
        }
        
        return processed
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