package com.example.classwork2.network.api

import com.google.gson.annotations.SerializedName

/**
 * SiliconFlow Chat Completions API 数据模型
 */

/**
 * 聊天完成请求
 */
data class ChatCompletionRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<ChatMessage>,
    @SerializedName("stream")
    val stream: Boolean = true,
    @SerializedName("max_tokens")
    val maxTokens: Int? = null,
    @SerializedName("temperature")
    val temperature: Double? = null,
    @SerializedName("enable_thinking")
    val enableThinking: Boolean? = null
)

/**
 * 聊天消息
 */
data class ChatMessage(
    @SerializedName("role")
    val role: String, // "system", "user", "assistant"
    @SerializedName("content")
    val content: String
)

/**
 * 聊天完成响应（非流式）
 */
data class ChatCompletionResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("object")
    val objectType: String,
    @SerializedName("created")
    val created: Long,
    @SerializedName("model")
    val model: String,
    @SerializedName("choices")
    val choices: List<ChatChoice>,
    @SerializedName("usage")
    val usage: Usage?
)

/**
 * 聊天选择项
 */
data class ChatChoice(
    @SerializedName("index")
    val index: Int,
    @SerializedName("message")
    val message: ChatMessage?,
    @SerializedName("delta")
    val delta: ChatMessage?,
    @SerializedName("finish_reason")
    val finishReason: String?
)

/**
 * API使用统计
 */
data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

/**
 * 流式响应数据包
 */
data class StreamChunk(
    @SerializedName("id")
    val id: String,
    @SerializedName("object")
    val objectType: String,
    @SerializedName("created")
    val created: Long,
    @SerializedName("model")
    val model: String,
    @SerializedName("choices")
    val choices: List<ChatChoice>
)

/**
 * API错误响应
 */
data class ApiErrorResponse(
    @SerializedName("error")
    val error: ApiError
)

/**
 * API错误详情
 */
data class ApiError(
    @SerializedName("message")
    val message: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("code")
    val code: String?
)

/**
 * 翻译请求参数
 */
data class TranslationRequest(
    val originalText: String,
    val targetLanguage: String = "Chinese",
    val model: String = "Qwen/QwQ-32B",
    val temperature: Double = 0.3
)

/**
 * 翻译响应结果
 */
data class TranslationResponse(
    val originalSentences: List<String>,
    val translatedSentences: List<String>,
    val isComplete: Boolean,
    val error: String? = null
)

/**
 * 翻译状态
 */
data class TranslationState(
    val originalSentences: List<String> = emptyList(),
    val translations: List<String> = emptyList(),
    val currentTranslatingIndex: Int = -1,
    val currentPartialTranslation: String = "",
    val isTranslating: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)

/**
 * 句子对
 */
data class SentencePair(
    val original: String,
    val translation: String = "",
    val isTranslating: Boolean = false,
    val isComplete: Boolean = false
)