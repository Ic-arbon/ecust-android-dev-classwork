/**
 * 单元测试示例文件
 * 
 * 该文件包含JVM单元测试，这些测试在开发机器上执行，不需要Android设备或模拟器。
 * 单元测试主要用于测试业务逻辑、工具类、数据处理等不依赖Android框架的代码。
 */
package com.example.classwork2

import org.junit.Test
import org.junit.Assert.*

/**
 * ExampleUnitTest - 单元测试示例类
 * 
 * 这是一个本地单元测试类，将在开发机器（主机）上执行，而不是Android设备上。
 * 单元测试运行速度快，适合测试纯逻辑代码，如计算、字符串处理、业务规则等。
 * 
 * 参考文档: [Android测试文档](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    
    /**
     * addition_isCorrect - 加法运算正确性测试
     * 
     * 这是一个简单的单元测试方法，用于验证基本的加法运算是否正确。
     * @Test注解标记这是一个测试方法，JUnit框架会自动发现并执行它。
     * 
     * 测试步骤：
     * 1. 执行加法运算：2 + 2
     * 2. 断言结果应该等于4
     * 3. 如果断言失败，测试将报告错误
     */
    @Test
    fun addition_isCorrect() {
        // 使用assertEquals断言验证加法运算结果
        // 参数1：期望值（4）
        // 参数2：实际值（2 + 2的计算结果）
        assertEquals(4, 2 + 2)
    }
}