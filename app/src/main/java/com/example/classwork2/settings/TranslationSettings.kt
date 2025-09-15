package com.example.classwork2.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Stable

/**
 * 翻译设置管理类
 * 使用SharedPreferences存储用户的翻译偏好设置
 */
@Stable
class TranslationSettings(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "translation_settings", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_TARGET_LANGUAGE = "target_language"
        private const val KEY_MODEL = "model"
        private const val KEY_TEMPERATURE = "temperature"
        private const val KEY_AUTO_TRANSLATE = "auto_translate"
        private const val KEY_DISPLAY_MODE = "display_mode"
        private const val KEY_ANIMATION_ENABLED = "animation_enabled"
        
        // 默认值
        private const val DEFAULT_TARGET_LANGUAGE = "中文"
        private const val DEFAULT_MODEL = "Qwen/Qwen3-8B"
        private const val DEFAULT_TEMPERATURE = 0.3f
        private val DEFAULT_DISPLAY_MODE = DisplayMode.BILINGUAL
        private const val DEFAULT_AUTO_TRANSLATE = false
        private const val DEFAULT_ANIMATION_ENABLED = true
    }
    
    /**
     * API密钥
     */
    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()
    
    /**
     * 目标语言
     */
    var targetLanguage: String
        get() = prefs.getString(KEY_TARGET_LANGUAGE, DEFAULT_TARGET_LANGUAGE) ?: DEFAULT_TARGET_LANGUAGE
        set(value) = prefs.edit().putString(KEY_TARGET_LANGUAGE, value).apply()
    
    /**
     * 使用的模型
     */
    var model: String
        get() = prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()
    
    /**
     * 翻译温度参数
     */
    var temperature: Float
        get() = prefs.getFloat(KEY_TEMPERATURE, DEFAULT_TEMPERATURE)
        set(value) = prefs.edit().putFloat(KEY_TEMPERATURE, value).apply()
    
    /**
     * 是否自动翻译
     */
    var autoTranslate: Boolean
        get() = prefs.getBoolean(KEY_AUTO_TRANSLATE, DEFAULT_AUTO_TRANSLATE)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_TRANSLATE, value).apply()
    
    /**
     * 显示模式
     */
    var displayMode: DisplayMode
        get() {
            val modeOrdinal = prefs.getInt(KEY_DISPLAY_MODE, DEFAULT_DISPLAY_MODE.ordinal)
            return DisplayMode.values().getOrElse(modeOrdinal) { DEFAULT_DISPLAY_MODE }
        }
        set(value) = prefs.edit().putInt(KEY_DISPLAY_MODE, value.ordinal).apply()
    
    /**
     * 是否启用动画效果
     */
    var animationEnabled: Boolean
        get() = prefs.getBoolean(KEY_ANIMATION_ENABLED, DEFAULT_ANIMATION_ENABLED)
        set(value) = prefs.edit().putBoolean(KEY_ANIMATION_ENABLED, value).apply()
    
    /**
     * 检查是否已配置API密钥
     */
    fun isApiKeyConfigured(): Boolean {
        return apiKey.isNotBlank()
    }
    
    /**
     * 重置所有设置为默认值
     */
    fun resetToDefault() {
        prefs.edit().clear().apply()
    }
    
    /**
     * 获取可用的目标语言列表
     */
    fun getAvailableLanguages(): List<String> {
        return listOf(
            "中文",
            "English",
            "日本語",
            "한국어",
            "Français",
            "Deutsch",
            "Español",
            "Italiano",
            "Русский"
        )
    }
    
    /**
     * 获取可用的模型列表
     */
    fun getAvailableModels(): List<String> {
        return listOf(
            "Qwen/Qwen3-8B",
        )
    }
}

/**
 * 显示模式枚举
 */
enum class DisplayMode(val displayName: String) {
    ORIGINAL_ONLY("仅原文"),
    TRANSLATION_ONLY("仅译文"),
    BILINGUAL("双语对照");
    
    companion object {
        fun fromDisplayName(name: String): DisplayMode? {
            return values().find { it.displayName == name }
        }
    }
}