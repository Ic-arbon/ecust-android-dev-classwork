/**
 * 应用程序颜色定义文件
 * 
 * 该文件定义了应用程序在浅色和深色主题下使用的颜色值。
 * 颜色值遵循Material Design 3设计规范，支持动态主题切换。
 */
package com.example.classwork2.ui.theme

import androidx.compose.ui.graphics.Color

// 深色主题颜色定义 (用于深色模式)
// 这些颜色值的亮度较高（80%），在深色背景上提供良好的对比度和可读性

/** 深色主题的主色调 - 淡紫色，用于重要按钮、强调元素等 */
val Purple80 = Color(0xFFD0BCFF)

/** 深色主题的次要色调 - 紫灰色，用于次要按钮、辅助元素等 */
val PurpleGrey80 = Color(0xFFCCC2DC)

/** 深色主题的第三色调 - 淡粉色，用于强调和装饰元素 */
val Pink80 = Color(0xFFEFB8C8)

// 浅色主题颜色定义 (用于浅色模式)
// 这些颜色值的亮度较低（40%），在浅色背景上提供良好的对比度和可读性

/** 浅色主题的主色调 - 深紫色，用于重要按钮、强调元素等 */
val Purple40 = Color(0xFF6650a4)

/** 浅色主题的次要色调 - 深紫灰色，用于次要按钮、辅助元素等 */
val PurpleGrey40 = Color(0xFF625b71)

/** 浅色主题的第三色调 - 深粉色，用于强调和装饰元素 */
val Pink40 = Color(0xFF7D5260)