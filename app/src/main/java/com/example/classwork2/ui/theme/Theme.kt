/**
 * 应用程序主题定义文件
 * 
 * 该文件定义了应用程序的Material Design 3主题，包括颜色方案的配置
 * 和主题组合函数。支持浅色模式、深色模式和Android 12+的动态颜色主题。
 */
package com.example.classwork2.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * 深色主题颜色方案
 * 
 * 定义了深色模式下应用程序使用的颜色方案，使用较亮的颜色值
 * 以在深色背景上提供良好的对比度和可读性。
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,        // 主色调 - 用于重要操作按钮、链接等
    secondary = PurpleGrey80,  // 次要色调 - 用于次要按钮、辅助信息等
    tertiary = Pink80          // 第三色调 - 用于强调元素、装饰等
)

/**
 * 浅色主题颜色方案
 * 
 * 定义了浅色模式下应用程序使用的颜色方案，使用较深的颜色值
 * 以在浅色背景上提供良好的对比度和可读性。
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,        // 主色调 - 用于重要操作按钮、链接等
    secondary = PurpleGrey40,  // 次要色调 - 用于次要按钮、辅助信息等
    tertiary = Pink40          // 第三色调 - 用于强调元素、装饰等

    /* 其他可以覆盖的默认颜色配置示例：
    background = Color(0xFFFFFBFE),      // 背景色
    surface = Color(0xFFFFFBFE),         // 表面色（如卡片背景）
    onPrimary = Color.White,             // 主色调上的文本颜色
    onSecondary = Color.White,           // 次要色调上的文本颜色
    onTertiary = Color.White,            // 第三色调上的文本颜色
    onBackground = Color(0xFF1C1B1F),    // 背景上的文本颜色
    onSurface = Color(0xFF1C1B1F),       // 表面上的文本颜色
    */
)

/**
 * Classwork2Theme - 应用程序主题组合函数
 * 
 * 该函数是应用程序的主题容器，负责根据系统设置和参数选择合适的颜色方案，
 * 并应用Material Design 3主题到整个应用程序。
 * 
 * @param darkTheme 是否使用深色主题，默认跟随系统设置
 * @param dynamicColor 是否使用动态颜色（Android 12+功能），默认为true
 * @param content 主题包装的内容，接收一个可组合函数作为子内容
 */
@Composable
fun Classwork2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),  // 默认跟随系统深色模式设置
    dynamicColor: Boolean = true,                // Android 12+动态颜色功能默认开启
    content: @Composable () -> Unit
) {
    // 根据条件选择合适的颜色方案
    val colorScheme = when {
        // 如果设备支持动态颜色且系统版本≥Android 12 (API 31)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            // 根据当前主题模式选择动态深色或浅色方案
            if (darkTheme) {
                dynamicDarkColorScheme(context)   // 动态深色方案，从系统壁纸提取颜色
            } else {
                dynamicLightColorScheme(context)  // 动态浅色方案，从系统壁纸提取颜色
            }
        }
        // 如果当前是深色模式但不支持动态颜色
        darkTheme -> DarkColorScheme
        // 默认情况：使用静态浅色主题
        else -> LightColorScheme
    }

    // 应用Material Design 3主题
    MaterialTheme(
        colorScheme = colorScheme,  // 使用选定的颜色方案
        typography = Typography,    // 使用自定义字体样式
        content = content          // 渲染传入的内容
    )
}