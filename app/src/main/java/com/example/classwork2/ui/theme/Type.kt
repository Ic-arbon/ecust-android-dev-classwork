/**
 * 应用程序字体样式定义文件
 * 
 * 该文件定义了应用程序的Typography（字体排版）样式，遵循Material Design 3
 * 的字体系统规范。可以自定义字体族、字重、字号、行高和字间距等属性。
 */
package com.example.classwork2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography - Material Design 3字体样式集合
 * 
 * 定义了应用程序使用的字体样式规范，包括不同类型文本的字体、字重、大小等属性。
 * 目前只定义了bodyLarge样式，其他样式使用Material Design 3的默认值。
 */
val Typography = Typography(
    /**
     * bodyLarge - 大号正文文本样式
     * 
     * 用于应用程序中的主要正文内容，如文章正文、详细描述等。
     * 该样式提供了良好的可读性和视觉层次。
     */
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,  // 使用系统默认字体族
        fontWeight = FontWeight.Normal,   // 正常字重（400）
        fontSize = 16.sp,                // 字体大小：16sp，适合正文阅读
        lineHeight = 24.sp,              // 行高：24sp，提供舒适的行间距
        letterSpacing = 0.5.sp           // 字间距：0.5sp，轻微增加字符间距提升可读性
    )
    
    /* 其他可以覆盖的默认文本样式示例：
    
    // 大标题样式 - 用于页面标题、重要标题等
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,     // 字体族
        fontWeight = FontWeight.Normal,      // 字重
        fontSize = 22.sp,                   // 字体大小
        lineHeight = 28.sp,                 // 行高
        letterSpacing = 0.sp                // 字间距
    ),
    
    // 小标签样式 - 用于按钮文字、小标签等
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,     // 字体族
        fontWeight = FontWeight.Medium,      // 中等字重（500）
        fontSize = 11.sp,                   // 小字体大小
        lineHeight = 16.sp,                 // 紧凑的行高
        letterSpacing = 0.5.sp              // 字间距
    )
    */
)