package com.example.classwork2.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 打字机效果文本组件
 * 逐字显示文本，模拟实时翻译的效果
 * 
 * @param text 要显示的完整文本
 * @param isAnimating 是否正在执行动画
 * @param animationSpeed 动画速度（毫秒/字符）
 * @param style 文本样式
 * @param color 文本颜色
 * @param showCursor 是否显示光标
 * @param modifier 修饰符
 */
@Composable
fun TypewriterText(
    text: String,
    isAnimating: Boolean = true,
    animationSpeed: Long = 50L,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    showCursor: Boolean = true,
    modifier: Modifier = Modifier
) {
    var displayedText by remember(text) { mutableStateOf("") }
    var currentIndex by remember(text) { mutableStateOf(0) }
    
    // 光标闪烁动画
    val cursorAlpha by animateFloatAsState(
        targetValue = if (showCursor && isAnimating) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )
    
    // 重置状态当文本改变时
    LaunchedEffect(text) {
        currentIndex = 0
        displayedText = ""
    }
    
    // 打字机动画逻辑
    LaunchedEffect(text, isAnimating) {
        if (isAnimating && currentIndex < text.length) {
            while (currentIndex < text.length) {
                delay(animationSpeed)
                currentIndex++
                displayedText = text.substring(0, currentIndex)
            }
        } else if (!isAnimating) {
            // 如果不需要动画，直接显示全部文本
            displayedText = text
            currentIndex = text.length
        }
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = displayedText,
            style = style,
            color = color
        )
        
        // 光标
        if (showCursor && isAnimating && currentIndex < text.length) {
            Text(
                text = "|",
                style = style.copy(
                    fontWeight = FontWeight.Bold,
                    color = color.copy(alpha = cursorAlpha)
                ),
                modifier = Modifier.padding(start = 1.dp)
            )
        }
    }
}

/**
 * 渐进式显示文本组件
 * 适用于长文本的逐句显示
 * 
 * @param sentences 句子列表
 * @param currentSentenceIndex 当前正在显示的句子索引
 * @param currentPartialText 当前句子的部分文本
 * @param isAnimating 是否正在动画
 * @param style 文本样式
 * @param color 文本颜色
 * @param spacing 句子间距
 * @param modifier 修饰符
 */
@Composable
fun ProgressiveText(
    sentences: List<String>,
    currentSentenceIndex: Int,
    currentPartialText: String = "",
    isAnimating: Boolean = true,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    spacing: Int = 8,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.dp)
    ) {
        // 显示已完成的句子
        sentences.take(currentSentenceIndex).forEach { sentence ->
            Text(
                text = sentence,
                style = style,
                color = color
            )
        }
        
        // 显示当前正在输入的句子
        if (currentSentenceIndex < sentences.size) {
            val currentSentence = sentences[currentSentenceIndex]
            val textToShow = currentPartialText.ifEmpty { currentSentence }
            
            TypewriterText(
                text = textToShow,
                isAnimating = isAnimating && currentPartialText.isNotEmpty(),
                style = style,
                color = color,
                showCursor = isAnimating
            )
        }
    }
}

/**
 * 双语句子对组件
 * 显示原文和译文的句子对
 * 
 * @param originalText 原文
 * @param translatedText 译文
 * @param isTranslating 是否正在翻译
 * @param showTranslation 是否显示译文
 * @param originalStyle 原文样式
 * @param translatedStyle 译文样式
 * @param spacing 原文和译文间距
 * @param modifier 修饰符
 */
@Composable
fun BilingualSentencePair(
    originalText: String,
    translatedText: String = "",
    isTranslating: Boolean = false,
    showTranslation: Boolean = true,
    originalStyle: TextStyle = LocalTextStyle.current,
    translatedStyle: TextStyle = LocalTextStyle.current.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = originalStyle.fontSize * 0.9f
    ),
    spacing: Int = 4,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.dp)
    ) {
        // 原文
        Text(
            text = originalText,
            style = originalStyle
        )
        
        // 译文
        if (showTranslation) {
            if (translatedText.isNotEmpty()) {
                TypewriterText(
                    text = translatedText,
                    isAnimating = isTranslating,
                    style = translatedStyle,
                    showCursor = isTranslating
                )
            } else if (isTranslating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "翻译中...",
                        style = translatedStyle.copy(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

/**
 * 翻译进度指示器
 * 显示整体翻译进度
 * 
 * @param currentSentence 当前句子索引
 * @param totalSentences 总句子数
 * @param isTranslating 是否正在翻译
 * @param modifier 修饰符
 */
@Composable
fun TranslationProgressIndicator(
    currentSentence: Int,
    totalSentences: Int,
    isTranslating: Boolean,
    modifier: Modifier = Modifier
) {
    if (totalSentences > 0) {
        val progress = currentSentence.toFloat() / totalSentences.toFloat()
        
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTranslating) "翻译中..." else "翻译完成",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currentSentence / $totalSentences",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}