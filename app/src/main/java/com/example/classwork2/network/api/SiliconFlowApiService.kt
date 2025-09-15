package com.example.classwork2.network.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * SiliconFlow API 服务接口
 */
interface SiliconFlowApiService {
    
    /**
     * 流式聊天完成
     * 
     * @param authorization Bearer token
     * @param request 聊天完成请求
     * @return 流式响应
     */
    @Streaming
    @POST("v1/chat/completions")
    suspend fun streamChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ChatCompletionRequest
    ): Response<ResponseBody>
    
    /**
     * 非流式聊天完成
     * 
     * @param authorization Bearer token
     * @param request 聊天完成请求
     * @return 聊天完成响应
     */
    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>
    
    companion object {
        const val BASE_URL = "https://api.siliconflow.cn/"
        const val DEFAULT_MODEL = "Qwen/QwQ-32B"
        
        /**
         * 创建授权头
         */
        fun createAuthHeader(apiKey: String): String {
            return "Bearer $apiKey"
        }
    }
}