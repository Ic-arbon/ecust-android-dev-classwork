/** Classwork2 Android应用程序包声明 该包包含应用程序的主要组件和UI相关代码 */
package com.example.classwork2

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.classwork2.ui.theme.Classwork2Theme
import kotlinx.coroutines.launch

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
                // 使用带抽屉导航栏的主界面
                MainScreenWithDrawer(
                    onLogout = {
                        // 登出功能：跳转到登录界面
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish() // 关闭主界面，防止用户按返回键回到主界面
                    },
                )
            }
        }
    }
}

/**
 * 带抽屉导航栏的主界面组件
 *
 * 这是主界面的核心组件，集成了：
 * - DrawerLayout抽屉布局
 * - TopAppBar顶部应用栏（包含汉堡菜单图标）
 * - 抽屉导航内容（用户头像、用户名、登出选项）
 * - 主要内容区域
 *
 * @param onLogout 登出回调函数，用于处理用户登出操作
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithDrawer(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 抽屉状态管理
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 模拟用户信息（实际应用中应该从SharedPreferences或数据库获取）
    val currentUserName = "魔法使"
    val currentUserAvatar = AvatarType.IconAvatar(Icons.Default.Person)

    // 使用ModalNavigationDrawer实现抽屉导航
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // 抽屉导航内容
            DrawerContent(
                userName = currentUserName,
                userAvatar = currentUserAvatar,
                onLogout = {
                    onLogout()
                    // 登出后关闭抽屉
                    scope.launch { drawerState.close() }
                },
            )
        },
        modifier = modifier,
    ) {
        // 主要内容区域
        Scaffold(
            topBar = {
                // 顶部应用栏，包含汉堡菜单图标
                TopAppBar(
                    title = { Text("魔法图书馆") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                // 点击汉堡菜单图标打开抽屉
                                scope.launch { drawerState.open() }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "打开菜单",
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            // 主要内容区域
            MainContent(modifier = Modifier.fillMaxSize().padding(paddingValues))
        }
    }
}

/**
 * 抽屉导航内容组件
 *
 * 包含用户信息和导航选项：
 * - 用户头像显示
 * - 用户名显示
 * - 登出选项
 *
 * @param userName 当前用户名
 * @param userAvatar 当前用户头像
 * @param onLogout 登出回调函数
 */
@Composable
fun DrawerContent(
    userName: String,
    userAvatar: AvatarType,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 获取屏幕配置信息
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // 根据屏幕方向和屏幕宽度计算抽屉宽度
    val drawerWidth =
        with(density) {
            val screenWidthPx = configuration.screenWidthDp.dp
            when (configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    // 竖屏模式：使用屏幕宽度的75%，最大300dp，最小240dp
                    (screenWidthPx * 0.5f).coerceIn(240.dp, 300.dp)
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    // 横屏模式：使用屏幕宽度的50%，最大320dp，最小280dp
                    (screenWidthPx * 0.5f).coerceIn(280.dp, 320.dp)
                }
                else -> 280.dp // 默认宽度
            }
        }

    ModalDrawerSheet(modifier = modifier.width(drawerWidth)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // 用户信息区域
            UserInfoSection(
                userName = userName,
                userAvatar = userAvatar,
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // 导航选项区域
            NavigationOptionsSection(onLogout = onLogout)
        }
    }
}

/**
 * 用户信息区域组件
 *
 * 显示用户头像和用户名
 *
 * @param userName 用户名
 * @param userAvatar 用户头像
 */
@Composable
fun UserInfoSection(
    userName: String,
    userAvatar: AvatarType,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 用户头像
        Surface(
            modifier = Modifier.size(80.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            when (userAvatar) {
                is AvatarType.IconAvatar -> {
                    Icon(
                        imageVector = userAvatar.icon,
                        contentDescription = "用户头像",
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                is AvatarType.ImageAvatar -> {
                    Image(
                        painter = painterResource(userAvatar.drawableRes),
                        contentDescription = "用户头像",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 用户名
        Text(
            text = userName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = "欢迎回来！",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 导航选项区域组件
 *
 * 包含应用的导航选项，目前主要是登出功能
 *
 * @param onLogout 登出回调函数
 */
@Composable
fun NavigationOptionsSection(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // 登出选项
        NavigationDrawerItem(
            label = { Text(text = "登出", style = MaterialTheme.typography.labelLarge) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                )
            },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        )
    }
}

/**
 * 主要内容区域组件
 *
 * 显示应用的主要内容，目前是简单的欢迎信息
 */
@Composable
fun MainContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "欢迎来到魔法图书馆！",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "点击左上角菜单图标打开抽屉导航",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Greeting - 问候语可组合函数（保留用于兼容性）
 *
 * 这是一个简单的文本显示组件，用于显示个性化的问候消息。
 *
 * @param name 要问候的名字，将显示在"Hello"后面
 * @param modifier 修饰符，用于自定义组件的外观和行为
 */
@Composable
fun greeting(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(text = "Hello $name!", modifier = modifier)
}

/** GreetingPreview - 问候语组件的预览函数 */
@Preview(showBackground = true)
@Composable
fun greetingPreview() {
    Classwork2Theme { greeting("Android") }
}

/** MainScreenPreview - 主界面预览函数 */
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    Classwork2Theme { MainScreenWithDrawer(onLogout = { /* 预览中的空实现 */ }) }
}
