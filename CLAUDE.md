# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

这是一个基于Android Jetpack Compose的Android应用程序项目，使用Kotlin开发。项目采用现代Android开发技术栈，使用Material 3设计规范。

## 构建系统

- **构建工具**: Gradle (Kotlin DSL)
- **Android Gradle Plugin**: 8.11.1
- **Kotlin版本**: 2.0.21
- **最小SDK**: 24
- **目标SDK**: 36
- **编译SDK**: 36

## 常用开发命令

### 构建和运行
```bash
# 构建调试版本
./gradlew assembleDebug

# 构建发布版本
./gradlew assembleRelease

# 安装调试版本到设备
./gradlew installDebug

# 清理构建产物
./gradlew clean
```

### 测试命令
```bash
# 运行单元测试
./gradlew test

# 运行单个测试类
./gradlew test --tests "com.example.classwork2.ExampleUnitTest"

# 运行Android仪器化测试
./gradlew connectedAndroidTest

# 运行特定的仪器化测试
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.classwork2.ExampleInstrumentedTest
```

### 代码质量
```bash
# 运行Lint检查
./gradlew lint

# 生成测试覆盖率报告
./gradlew createDebugCoverageReport
```

### 提交规范
形如 feat(xxx): content，不要写ai合作者。

## 项目架构

### 目录结构
- `app/src/main/java/com/example/classwork2/` - 主要Kotlin源代码
  - `MainActivity.kt` - 应用程序主Activity，使用Compose设置UI
  - `ui/theme/` - Compose主题定义（颜色、字体、样式）
- `app/src/androidTest/` - Android仪器化测试
- `app/src/test/` - JVM单元测试

### 技术栈
- **UI框架**: Jetpack Compose (BOM 2024.09.00)
- **架构组件**: 
  - Activity Compose (1.10.1)
  - Lifecycle Runtime KTX (2.9.3)
  - Core KTX (1.17.0)
- **测试框架**:
  - JUnit 4 (4.13.2) - 单元测试
  - AndroidX JUnit (1.3.0) - 仪器化测试
  - Espresso (3.7.0) - UI测试
  - Compose UI Test - Compose组件测试

### 关键配置
- Java兼容性: Java 11
- Kotlin JVM目标: 11
- 启用Compose构建功能
- 使用版本目录管理依赖 (`gradle/libs.versions.toml`)

### 开发注意事项
- 项目使用Kotlin DSL构建脚本
- 依赖项通过版本目录集中管理
- 启用了边到边显示支持 (`enableEdgeToEdge()`)
- 使用Material 3设计系统和主题
