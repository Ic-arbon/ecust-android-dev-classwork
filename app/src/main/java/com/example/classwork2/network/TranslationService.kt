package com.example.classwork2.network

import android.util.Log
import com.example.classwork2.BuildConfig
import com.example.classwork2.network.api.*
import com.example.classwork2.utils.TextProcessor
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * 翻译服务类
 * 支持流式翻译，实现实时翻译反馈
 */
class TranslationService {
    
    companion object {
        private const val TAG = "TranslationService"
        
        // 只在Debug模式下启用详细日志
        private val isDebugMode = BuildConfig.DEBUG
        
        private fun logDebug(message: String) {
            if (isDebugMode) {
                Log.d(TAG, message)
            }
        }
        
        private fun logInfo(message: String) {
            Log.i(TAG, message)
        }
    }
    
    private val textProcessor = TextProcessor()
    private val gson = Gson()
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(SiliconFlowApiService.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val apiService = retrofit.create(SiliconFlowApiService::class.java)
    
    /**
     * 流式翻译文本
     * 
     * @param originalText 原始文本
     * @param apiKey API密钥
     * @param targetLanguage 目标语言（默认中文）
     * @param enableThinking 是否启用AI思维过程（默认false，禁用以加快翻译速度）
     * @return 翻译状态流
     */
    fun translateTextStream(
        originalText: String,
        apiKey: String,
        targetLanguage: String = "中文",
        enableThinking: Boolean = false
    ): Flow<TranslationState> = flow {
        try {
            // 1. 分割原文为句子
            val originalSentences = textProcessor.splitIntoSentences(originalText)
            emit(TranslationState(
                originalSentences = originalSentences,
                isTranslating = true
            ))
            
            // 2. 构建翻译提示词
            val prompt = buildTranslationPrompt(originalSentences, targetLanguage)
            
            // 3. 创建API请求
            val request = ChatCompletionRequest(
                model = SiliconFlowApiService.DEFAULT_MODEL,
                messages = listOf(
                    ChatMessage("system", "你是一个专业的翻译专家。"),
                    ChatMessage("user", prompt)
                ),
                stream = true,
                temperature = 0.3,
                maxTokens = originalText.length * 3, // 增加预估译文长度，确保有足够空间完成翻译
                enableThinking = enableThinking // 使用传入的设置参数
            )
            
            // 4. 发送流式请求
            val response = apiService.streamChatCompletion(
                authorization = SiliconFlowApiService.createAuthHeader(apiKey),
                request = request
            )
            
            if (!response.isSuccessful) {
                emit(TranslationState(
                    originalSentences = originalSentences,
                    error = "API请求失败: ${response.code()} ${response.message()}"
                ))
                return@flow
            }
            
            // 5. 处理流式响应
            val responseBody = response.body()
            if (responseBody != null) {
                processStreamResponse(responseBody, originalSentences) { state ->
                    emit(state)
                }
            } else {
                emit(TranslationState(
                    originalSentences = originalSentences,
                    error = "响应体为空"
                ))
            }
            
        } catch (e: Exception) {
            emit(TranslationState(
                error = "翻译失败: ${e.message}"
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 处理流式响应
     */
    private suspend fun processStreamResponse(
        responseBody: okhttp3.ResponseBody,
        originalSentences: List<String>,
        onStateUpdate: suspend (TranslationState) -> Unit
    ) {
        val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))
        var currentContent = StringBuilder()
        val translatedSentences = mutableListOf<String>()
        var currentTranslatingIndex = 0
        var lastExtractedCount = 0 // 缓存上次提取的句子数量，避免重复解析
        var extractionAttempts = 0 // 提取尝试计数器
        var lastContentLength = 0 // 上次内容长度，用于检测显著变化
        
        logInfo("开始处理流式翻译响应，原文句子数: ${originalSentences.size}")
        
        try {
            reader.useLines { lines ->
                for (line in lines) {
                    // 检查连接是否异常断开
                    if (line.trim().isEmpty() && currentContent.isNotEmpty()) {
                        logDebug("检测到空行，可能是流结束")
                        continue
                    }
                    
                    if (line.startsWith("data: ")) {
                        val jsonData = line.removePrefix("data: ").trim()
                        
                        // 检查是否为结束标志
                        if (jsonData == "[DONE]") {
                            // 处理最后剩余的内容
                            val finalContent = currentContent.toString()
                            if (finalContent.isNotEmpty()) {
                                val finalSentences = extractTranslatedSentences(finalContent, translatedSentences.size)
                                val newFinalSentences = finalSentences.drop(translatedSentences.size)
                                if (newFinalSentences.isNotEmpty()) {
                                    translatedSentences.addAll(newFinalSentences)
                                    logInfo("翻译完成，最终添加 ${newFinalSentences.size} 个句子")
                                }
                            }
                            
                            logInfo("翻译完成，共翻译 ${translatedSentences.size} 个句子")
                            onStateUpdate(TranslationState(
                                originalSentences = originalSentences,
                                translations = translatedSentences,
                                currentTranslatingIndex = -1,
                                isTranslating = false,
                                isComplete = true
                            ))
                            break
                        }
                        
                        try {
                            val chunk = gson.fromJson(jsonData, StreamChunk::class.java)
                            val delta = chunk.choices.firstOrNull()?.delta
                            val deltaContent = delta?.content
                            
                            if (deltaContent != null) {
                                logDebug("收到数据块: \"$deltaContent\"")
                                currentContent.append(deltaContent)
                                
                                // 智能提取逻辑：大幅减少无效解析
                                val currentContentStr = currentContent.toString()
                                val shouldExtract = shouldAttemptExtraction(deltaContent, currentContentStr) &&
                                    (currentContentStr.length - lastContentLength > 100 || // 内容显著增长
                                     extractionAttempts == 0 || // 首次尝试
                                     extractionAttempts % 20 == 0) // 每20次数据块尝试一次
                                
                                if (shouldExtract) {
                                    extractionAttempts++
                                    val extractedSentences = extractTranslatedSentences(currentContentStr, translatedSentences.size)
                                    
                                    if (extractedSentences.size > translatedSentences.size) {
                                        // 有新完整的句子
                                        val newSentences = extractedSentences.drop(translatedSentences.size)
                                        logInfo("检测到 ${newSentences.size} 个新完整句子")
                                        
                                        translatedSentences.addAll(newSentences)
                                        currentTranslatingIndex = translatedSentences.size
                                        lastExtractedCount = extractedSentences.size
                                        lastContentLength = currentContentStr.length
                                        extractionAttempts = 0 // 重置计数器
                                    }
                                }
                                
                                // 获取当前正在翻译的部分内容（减少频繁调用）
                                val partialTranslation = if (translatedSentences.size < originalSentences.size && 
                                                            currentContentStr.length > lastContentLength + 20) {
                                    getPartialTranslation(currentContentStr, translatedSentences.size)
                                } else {
                                    "" // 跳过部分翻译更新以提高性能
                                }
                                val adjustedIndex = minOf(currentTranslatingIndex, originalSentences.size - 1)
                                
                                onStateUpdate(TranslationState(
                                    originalSentences = originalSentences,
                                    translations = translatedSentences,
                                    currentTranslatingIndex = adjustedIndex,
                                    currentPartialTranslation = partialTranslation,
                                    isTranslating = true
                                ))
                            }
                        } catch (e: Exception) {
                            // 忽略JSON解析错误，继续处理
                            logDebug("解析流数据失败: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理响应流失败", e)
            
            // 即使出错也尝试处理已积累的内容
            if (currentContent.isNotEmpty()) {
                try {
                    val finalSentences = extractTranslatedSentences(currentContent.toString(), translatedSentences.size)
                    val newFinalSentences = finalSentences.drop(translatedSentences.size)
                    if (newFinalSentences.isNotEmpty()) {
                        translatedSentences.addAll(newFinalSentences)
                        logInfo("异常处理中添加 ${newFinalSentences.size} 个句子")
                    }
                } catch (extractError: Exception) {
                    logDebug("最终提取失败: ${extractError.message}")
                }
            }
            
            onStateUpdate(TranslationState(
                originalSentences = originalSentences,
                translations = translatedSentences,
                error = "处理响应流失败: ${e.message}，已保存 ${translatedSentences.size} 个翻译结果"
            ))
        } finally {
            reader.close()
            
            // 确保流正常结束时也处理剩余内容
            if (translatedSentences.size < originalSentences.size && currentContent.isNotEmpty()) {
                logInfo("流结束时检测到未完成的翻译，尝试最后提取")
                try {
                    val finalSentences = extractTranslatedSentences(currentContent.toString(), translatedSentences.size)
                    val newFinalSentences = finalSentences.drop(translatedSentences.size)
                    if (newFinalSentences.isNotEmpty()) {
                        translatedSentences.addAll(newFinalSentences)
                        logInfo("流结束时添加 ${newFinalSentences.size} 个句子")
                        
                        // 发送最终状态更新
                        onStateUpdate(TranslationState(
                            originalSentences = originalSentences,
                            translations = translatedSentences,
                            currentTranslatingIndex = -1,
                            isTranslating = false,
                            isComplete = true
                        ))
                    }
                } catch (finalError: Exception) {
                    logDebug("最终提取失败: ${finalError.message}")
                }
            }
        }
    }
    
    /**
     * 构建翻译提示词
     */
    private fun buildTranslationPrompt(sentences: List<String>, targetLanguage: String): String {
        logInfo("构建翻译提示词，原文句子数: ${sentences.size}，目标语言: $targetLanguage")
        
        val numberedSentences = sentences.mapIndexed { index, sentence ->
            "${index + 1}. $sentence"
        }.joinToString("\n")
        
        val prompt = """
请将以下文本逐句翻译成$targetLanguage。要求：

1. 必须翻译所有${sentences.size}句，保持原文的句子顺序和数量
2. 每个译文句子前加上序号，格式为"1. 译文内容"
3. 保持原文的氛围感和文学性
4. 确保专业术语的准确性
5. 请完整翻译所有句子，不要遗漏

原文如下：
$numberedSentences

请开始完整翻译所有${sentences.size}句：
        """.trimIndent()
        
        logDebug("提示词长度: ${prompt.length} 字符")
        
        return prompt
    }
    
    /**
     * 从当前内容中提取已完成的翻译句子（真正的增量解析版本）
     */
    private fun extractTranslatedSentences(content: String, alreadyExtractedCount: Int = 0): List<String> {
        val sentences = mutableListOf<String>()
        val lines = content.split("\n")
        val sentenceRegex = Regex("^(\\d+)\\. (.+)$")
        
        var newSentencesFound = 0
        
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            
            val match = sentenceRegex.find(trimmed)
            if (match != null) {
                val sentenceNumber = match.groupValues[1].toIntOrNull()
                val sentenceContent = match.groupValues[2].trim()
                
                if (sentenceNumber != null && sentenceContent.isNotEmpty()) {
                    // 确保句子按顺序添加，且内容足够完整（避免保存过短的不完整句子）
                    if (sentenceNumber == sentences.size + 1 && sentenceContent.length >= 2) {
                        sentences.add(sentenceContent)
                        // 只记录超过已提取数量的新句子
                        if (sentences.size > alreadyExtractedCount) {
                            newSentencesFound++
                            logDebug("新发现句子 $sentenceNumber: $sentenceContent")
                        }
                    }
                }
            }
        }
        
        if (newSentencesFound > 0) {
            logInfo("增量提取完成，新增 $newSentencesFound 个句子，总计 ${sentences.size} 个")
        }
        
        return sentences
    }
    
    /**
     * 获取当前正在翻译的部分内容（高性能版本）
     */
    private fun getPartialTranslation(content: String, completedCount: Int): String {
        val lines = content.split("\n")
        val nextIndex = completedCount + 1
        
        // 查找下一个序号开始的不完整句子
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("$nextIndex.")) {
                val partialContent = trimmed.removePrefix("$nextIndex.").trim()
                // 只在调试模式下输出部分翻译日志
                if (isDebugMode && partialContent.length > 10) {
                    logDebug("部分翻译 $nextIndex: ${partialContent.take(20)}...")
                }
                return partialContent
            }
        }
        
        // 如果没找到带序号的，返回最后一行非空内容
        return lines.lastOrNull { it.trim().isNotEmpty() }?.trim() ?: ""
    }
    
    /**
     * 判断是否需要尝试提取句子（避免无意义的重复解析）
     */
    private fun shouldAttemptExtraction(deltaContent: String, fullContent: String): Boolean {
        // 只有当新内容包含句子结束标志或数字序号时才进行解析
        return deltaContent.contains("。") || 
               deltaContent.contains("\n") || 
               deltaContent.contains("\"") ||
               Regex("\\d+\\.").containsMatchIn(deltaContent)
    }
    
    /**
     * 测试API连接
     */
    suspend fun testConnection(apiKey: String): Boolean {
        return try {
            logInfo("开始测试API连接")
            val request = ChatCompletionRequest(
                model = SiliconFlowApiService.DEFAULT_MODEL,
                messages = listOf(
                    ChatMessage("user", "测试")
                ),
                stream = false,
                maxTokens = 10,
                enableThinking = false // 测试连接时也禁用思维过程
            )
            
            val response = apiService.chatCompletion(
                authorization = SiliconFlowApiService.createAuthHeader(apiKey),
                request = request
            )
            
            val isSuccessful = response.isSuccessful
            logInfo("API连接测试结果: $isSuccessful")
            isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "API连接测试失败", e)
            false
        }
    }
}