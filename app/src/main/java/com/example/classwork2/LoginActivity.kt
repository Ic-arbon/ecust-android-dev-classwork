package com.example.classwork2

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classwork2.ui.theme.Classwork2Theme

/**
 * 头像类型密封类，用于管理不同类型的头像
 * 
 * 这个密封类提供了类型安全的方式来处理两种不同的头像类型：
 * - IconAvatar: 基于矢量图标的头像
 * - ImageAvatar: 基于图片资源的头像
 */
sealed class AvatarType {
    /**
     * 图标头像类型
     * @param icon 矢量图标，通常来自Material Icons
     */
    data class IconAvatar(
        val icon: ImageVector,
    ) : AvatarType()

    /**
     * 图片头像类型
     * @param drawableRes 图片资源ID，用@DrawableRes注解确保类型安全
     */
    data class ImageAvatar(
        @DrawableRes val drawableRes: Int,
    ) : AvatarType()
}

/**
 * 登录界面Activity
 * 
 * 这个Activity提供了用户登录功能，包括：
 * - 用户名和密码输入
 * - 用户头像选择功能
 * - 响应式布局（支持横屏和竖屏）
 * - Material 3 设计规范
 */
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 启用边到边显示

        setContent {
            Classwork2Theme {
                // 使用Scaffold提供基础的Material Design布局结构
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        onLogin = {
                            // 登录成功后跳转到MainActivity
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish() // 关闭当前Activity，防止用户返回到登录页
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

/**
 * 登录界面主组件
 * 
 * 这是登录界面的核心Compose组件，负责：
 * - 管理登录状态（用户名、密码、选中的头像）
 * - 根据屏幕方向自动切换布局（横屏/竖屏）
 * - 处理用户交互事件
 * 
 * @param onLogin 登录回调函数，当用户点击登录按钮时触发
 * @param modifier Compose修饰符，用于自定义样式和行为
 */
@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 状态管理 - 使用remember确保组件重组时状态不丢失
    var username by remember { mutableStateOf("") } // 用户名输入状态
    var password by remember { mutableStateOf("") } // 密码输入状态
    var selectedAvatar by remember { // 当前选中的头像
        mutableStateOf<AvatarType>(AvatarType.IconAvatar(Icons.Default.Person))
    }
    var showAvatarSelector by remember { mutableStateOf(false) } // 头像选择器显示状态
    
    // 获取当前设备配置，用于判断屏幕方向
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // 根据屏幕方向选择不同的布局组件
    if (isLandscape) {
        // 横屏布局：左右分栏，头像和标题在左侧，输入框在右侧
        LandscapeLoginLayout(
            username = username,
            password = password,
            selectedAvatar = selectedAvatar,
            showAvatarSelector = showAvatarSelector,
            onUsernameChange = { username = it },
            onPasswordChange = { password = it },
            onAvatarSelect = { selectedAvatar = it },
            onAvatarSelectorToggle = { showAvatarSelector = it },
            onLogin = onLogin,
            modifier = modifier,
        )
    } else {
        // 竖屏布局：垂直排列，所有元素从上到下布局
        PortraitLoginLayout(
            username = username,
            password = password,
            selectedAvatar = selectedAvatar,
            showAvatarSelector = showAvatarSelector,
            onUsernameChange = { username = it },
            onPasswordChange = { password = it },
            onAvatarSelect = { selectedAvatar = it },
            onAvatarSelectorToggle = { showAvatarSelector = it },
            onLogin = onLogin,
            modifier = modifier,
        )
    }
}

/**
 * 竖屏模式登录布局组件
 * 
 * 专为竖屏（Portrait）模式优化的登录界面布局：
 * - 采用垂直Column布局，所有元素从上到下排列
 * - 头像位于顶部，下方依次是输入框和登录按钮
 * - 支持垂直滚动，确保在小屏设备上也能正常显示
 * - 居中对齐，提供良好的视觉效果
 * 
 * @param username 当前用户名输入值
 * @param password 当前密码输入值  
 * @param selectedAvatar 当前选中的头像
 * @param showAvatarSelector 头像选择器是否显示
 * @param onUsernameChange 用户名输入变化回调
 * @param onPasswordChange 密码输入变化回调
 * @param onAvatarSelect 头像选择回调
 * @param onAvatarSelectorToggle 头像选择器切换回调
 * @param onLogin 登录按钮点击回调
 * @param modifier Compose修饰符
 */
@Composable
fun PortraitLoginLayout(
    username: String,
    password: String,
    selectedAvatar: AvatarType,
    showAvatarSelector: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAvatarSelect: (AvatarType) -> Unit,
    onAvatarSelectorToggle: (Boolean) -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 支持垂直滚动
            .padding(32.dp), // 添加外边距
        horizontalAlignment = Alignment.CenterHorizontally, // 水平居中
        verticalArrangement = Arrangement.Center, // 垂直居中
    ) {
        // 应用标题
        Text(
            text = "魔法图书馆", 
            fontSize = 24.sp, 
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 头像选择器组件 - 可点击的圆形头像框
        AvatarSelector(
            selectedAvatar = selectedAvatar,
            showSelector = showAvatarSelector,
            onAvatarClick = { onAvatarSelectorToggle(!showAvatarSelector) },
            onAvatarSelect = { avatar ->
                onAvatarSelect(avatar)
                onAvatarSelectorToggle(false) // 选择后自动隐藏选择器
            },
            modifier = Modifier.size(100.dp),
        )

        Spacer(modifier = Modifier.height(32.dp)) // 头像和输入框之间的间距

        // 用户名输入框
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        )

        // 密码输入框 - 使用密码变换隐藏输入内容
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("密码") },
            visualTransformation = PasswordVisualTransformation(), // 密码掩码
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        )

        // 登录按钮 - 占满宽度
        Button(
            onClick = onLogin, 
            modifier = Modifier.fillMaxWidth()
        ) { 
            Text("登入") 
        }
    }
}

/**
 * 横屏模式登录布局组件
 * 
 * 专为横屏（Landscape）模式优化的登录界面布局：
 * - 采用水平Row布局，充分利用横屏的宽度空间
 * - 左侧显示应用标题和头像选择器
 * - 右侧显示用户名、密码输入框和登录按钮
 * - 两侧均等分配空间，提供平衡的视觉效果
 * - 右侧支持垂直滚动，适应不同屏幕高度
 * 
 * @param username 当前用户名输入值
 * @param password 当前密码输入值
 * @param selectedAvatar 当前选中的头像
 * @param showAvatarSelector 头像选择器是否显示
 * @param onUsernameChange 用户名输入变化回调
 * @param onPasswordChange 密码输入变化回调
 * @param onAvatarSelect 头像选择回调
 * @param onAvatarSelectorToggle 头像选择器切换回调
 * @param onLogin 登录按钮点击回调
 * @param modifier Compose修饰符
 */
@Composable
fun LandscapeLoginLayout(
    username: String,
    password: String,
    selectedAvatar: AvatarType,
    showAvatarSelector: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAvatarSelect: (AvatarType) -> Unit,
    onAvatarSelectorToggle: (Boolean) -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalAlignment = Alignment.CenterVertically, // 垂直居中对齐
    ) {
        // 左侧区域：应用标题和头像选择器
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(), // 占据左半部分空间
            horizontalAlignment = Alignment.CenterHorizontally, // 水平居中
            verticalArrangement = Arrangement.Center, // 垂直居中
        ) {
            // 应用标题 - 横屏模式下使用更大的字体
            Text(
                text = "魔法图书馆", 
                fontSize = 28.sp, 
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 头像选择器 - 横屏模式下使用更大的头像尺寸
            AvatarSelector(
                selectedAvatar = selectedAvatar,
                showSelector = showAvatarSelector,
                onAvatarClick = { onAvatarSelectorToggle(!showAvatarSelector) },
                onAvatarSelect = { avatar ->
                    onAvatarSelect(avatar)
                    onAvatarSelectorToggle(false) // 选择后自动隐藏选择器
                },
                modifier = Modifier.size(120.dp),
            )
        }

        // 左右两侧之间的间距
        Spacer(modifier = Modifier.width(32.dp))

        // 右侧区域：用户输入区域
        Column(
            modifier = Modifier
                .weight(1f) // 占据右半部分空间
                .verticalScroll(rememberScrollState()), // 支持垂直滚动
            horizontalAlignment = Alignment.CenterHorizontally, // 水平居中
            verticalArrangement = Arrangement.Center, // 垂直居中
        ) {
            // 用户名输入框
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )

            // 密码输入框 - 使用密码变换隐藏输入内容
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(), // 密码掩码
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            )

            // 登录按钮 - 占满当前区域宽度
            Button(
                onClick = onLogin, 
                modifier = Modifier.fillMaxWidth()
            ) { 
                Text("登入") 
            }
        }
    }
}

/**
 * 头像选择器组件
 * 
 * 这是一个复合组件，包含主头像显示和可展开的头像选择列表：
 * 
 * 功能特性：
 * - 主头像显示：显示当前选中的头像，可点击展开选择器
 * - 头像选择列表：水平滚动的头像选项列表，支持图标和图片两种类型
 * - 视觉反馈：选中状态有不同的颜色显示，提供良好的用户体验
 * - 类型支持：同时支持Material Icons和自定义图片资源
 * - 响应式设计：根据头像类型自动调整显示方式
 * 
 * @param selectedAvatar 当前选中的头像
 * @param showSelector 是否显示头像选择器列表
 * @param onAvatarClick 主头像点击回调，用于展开/收起选择器
 * @param onAvatarSelect 头像选择回调，当用户选择新头像时触发
 * @param modifier Compose修饰符，用于调整组件大小和样式
 */
@Composable
fun AvatarSelector(
    selectedAvatar: AvatarType,
    showSelector: Boolean,
    onAvatarClick: () -> Unit,
    onAvatarSelect: (AvatarType) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 定义可选的头像选项列表
    // 包含一个默认的人物图标和三个自定义图片头像
    val avatarOptions = listOf(
        AvatarType.IconAvatar(Icons.Default.Person), // 默认Material Icons人物图标
        AvatarType.ImageAvatar(R.drawable.av1),      // 自定义头像1
        AvatarType.ImageAvatar(R.drawable.av2),      // 自定义头像2
        AvatarType.ImageAvatar(R.drawable.av3),      // 自定义头像3
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 主头像显示区域 - 圆形可点击头像
        Surface(
            modifier = modifier
                .clip(CircleShape) // 圆形裁剪
                .clickable { onAvatarClick() }, // 点击展开选择器
            color = MaterialTheme.colorScheme.primaryContainer, // 使用主题容器颜色
        ) {
            // 根据头像类型选择不同的显示方式
            when (selectedAvatar) {
                is AvatarType.IconAvatar -> {
                    // 显示矢量图标头像
                    Icon(
                        selectedAvatar.icon,
                        contentDescription = "用户头像",
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer, // 图标颜色
                    )
                }
                is AvatarType.ImageAvatar -> {
                    // 显示图片头像
                    Image(
                        painter = painterResource(selectedAvatar.drawableRes),
                        contentDescription = "用户头像",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop, // 裁剪模式，保持比例填满
                    )
                }
            }
        }

        // 头像选择器列表 - 仅在showSelector为true时显示
        if (showSelector) {
            Spacer(modifier = Modifier.height(16.dp)) // 主头像和选择器之间的间距

            // 使用Card提供阴影效果和材质感
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // 阴影高度
            ) {
                // 水平滚动的头像选项列表
                LazyRow(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), // 选项之间的间距
                ) {
                    items(avatarOptions) { avatar ->
                        // 单个头像选项
                        Surface(
                            modifier = Modifier
                                .size(48.dp) // 选项头像尺寸
                                .clip(CircleShape)
                                .clickable { onAvatarSelect(avatar) }, // 点击选择头像
                            color = if (avatar == selectedAvatar) {
                                // 选中状态使用主色
                                MaterialTheme.colorScheme.primary
                            } else {
                                // 未选中状态使用表面变体色
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ) {
                            // 根据头像类型显示相应内容
                            when (avatar) {
                                is AvatarType.IconAvatar -> {
                                    // 显示图标选项
                                    Icon(
                                        avatar.icon,
                                        contentDescription = "头像选项",
                                        modifier = Modifier.fillMaxSize().padding(8.dp),
                                        tint = if (avatar == selectedAvatar) {
                                            // 选中状态的图标颜色
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            // 未选中状态的图标颜色
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                                is AvatarType.ImageAvatar -> {
                                    // 显示图片选项
                                    Image(
                                        painter = painterResource(avatar.drawableRes),
                                        contentDescription = "头像选项",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop, // 裁剪模式
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
