/**
 * Android仪器化测试示例文件
 * 
 * 该文件包含仪器化测试，这些测试将在真实的Android设备或模拟器上执行。
 * 仪器化测试可以访问Android框架的API和组件，适合测试UI交互、数据库操作、
 * 网络请求等依赖Android环境的功能。
 */
package com.example.classwork2

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * ExampleInstrumentedTest - Android仪器化测试示例类
 * 
 * 这是一个仪器化测试类，将在Android设备或模拟器上执行。
 * 仪器化测试可以访问Android Context、Activities、Services等Android组件，
 * 适合测试需要Android运行环境的功能。
 * 
 * @RunWith(AndroidJUnit4::class) 注解指定使用AndroidJUnit4测试运行器，
 * 该运行器专为Android平台优化，支持Android特定的测试功能。
 * 
 * 参考文档: [Android测试文档](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    
    /**
     * useAppContext - 应用程序上下文使用测试
     * 
     * 这个测试方法验证应用程序上下文是否正确可用，并检查应用程序的包名
     * 是否与预期一致。这是一个典型的仪器化测试，因为它需要访问Android Context。
     * 
     * 测试步骤：
     * 1. 获取被测试应用的Context对象
     * 2. 验证Context的包名是否正确
     * 3. 确保应用程序在正确的环境中运行
     */
    @Test
    fun useAppContext() {
        // 获取被测试应用程序的Context对象
        // InstrumentationRegistry提供了访问测试环境信息的API
        // targetContext返回被测试应用程序的Context
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        
        // 断言验证应用程序的包名是否正确
        // 这确保了测试运行在正确的应用程序上下文中
        assertEquals("com.example.classwork2", appContext.packageName)
    }
}