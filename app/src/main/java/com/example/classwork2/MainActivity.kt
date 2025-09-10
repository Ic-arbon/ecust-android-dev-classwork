/** Classwork2 Android应用程序包声明 该包包含应用程序的主要组件和UI相关代码 */
package com.example.classwork2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.classwork2.ui.theme.Classwork2Theme

/**
 * MainActivity - 应用程序的主Activity
 *
 * 这是应用程序的入口点，继承自ComponentActivity以支持Jetpack Compose。 该Activity负责设置应用程序的UI内容和配置边到边显示模式。
 */
class MainActivity : ComponentActivity() {

    /**
     * Activity生命周期方法 - 在Activity创建时调用
     *
     * @param savedInstanceState 保存的实例状态，用于恢复Activity状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用边到边显示模式，让应用内容可以延伸到状态栏和导航栏区域
        enableEdgeToEdge()

        // 设置Compose UI内容
        setContent {
            // 应用自定义主题
            Classwork2Theme {
                // 使用Scaffold作为应用的基本布局结构，提供Material Design的布局框架
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 显示问候语组件，传入内边距以避免被系统UI遮挡
                    greeting(name = "Android", modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * Greeting - 问候语可组合函数
 *
 * 这是一个简单的文本显示组件，用于显示个性化的问候消息。 该函数是可组合的，可以在Compose UI中重复使用。
 *
 * @param name 要问候的名字，将显示在"Hello"后面
 * @param modifier 修饰符，用于自定义组件的外观和行为，默认为空修饰符
 */
@Composable
fun greeting(name: String, modifier: Modifier = Modifier) {
    // 显示问候文本，使用字符串模板插入名字
    Text(text = "Hello $name!", modifier = modifier)
}

/**
 * GreetingPreview - 问候语组件的预览函数
 *
 * 该函数用于在Android Studio的预览窗口中显示Greeting组件的效果。
 * @Preview注解使得该函数可以在设计时预览，无需运行应用程序。 showBackground = true 参数使预览显示背景，便于查看效果。
 */
@Preview(showBackground = true)
@Composable
fun greetingPreview() {
    // 在预览中应用相同的主题
    Classwork2Theme {
        // 预览Greeting组件，使用"Android"作为示例名字
        greeting("Android")
    }
}

