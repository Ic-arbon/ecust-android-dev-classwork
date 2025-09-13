package com.example.classwork2.network

/**
 * 导入进度数据类
 */
data class ImportProgress(
    val stage: ImportStage,
    val currentStep: Int,
    val totalSteps: Int,
    val message: String = ""
) {
    val progress: Float get() = if (totalSteps > 0) currentStep.toFloat() / totalSteps else 0f
    val percentage: Int get() = (progress * 100).toInt()
}

/**
 * 导入阶段枚举
 */
enum class ImportStage(val displayName: String) {
    PREPARING("准备中"),
    DOWNLOADING("下载页面"),
    PARSING("解析章节"),
    SAVING("保存数据"),
    COMPLETED("完成")
}

/**
 * 导入进度回调接口
 */
interface ImportProgressCallback {
    /**
     * 进度更新回调
     */
    fun onProgressUpdate(progress: ImportProgress)
    
    /**
     * 错误回调
     */
    fun onError(error: String)
    
    /**
     * 完成回调
     */
    fun onComplete()
}

/**
 * 取消令牌接口
 */
interface CancellationToken {
    val isCancelled: Boolean
    fun cancel()
}

/**
 * 简单的取消令牌实现
 */
class SimpleCancellationToken : CancellationToken {
    @Volatile
    private var _cancelled = false
    
    override val isCancelled: Boolean get() = _cancelled
    
    override fun cancel() {
        _cancelled = true
    }
}