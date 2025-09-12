/** Classwork2 Android应用程序包声明 该包包含应用程序的主要组件和UI相关代码 */
package com.example.classwork2

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.classwork2.ui.theme.Classwork2Theme
import kotlinx.coroutines.launch

/**
 * 章节数据类
 * 
 * @param id 章节唯一标识符
 * @param title 章节标题
 * @param pageCount 页数
 */
data class Chapter(
    val id: String,
    val title: String,
    val pageCount: Int
)

/**
 * 书籍数据类
 * 
 * 表示图书馆中的一本书籍信息
 * 
 * @param id 书籍唯一标识符
 * @param title 书籍标题
 * @param author 作者
 * @param description 书籍描述
 * @param coverImageRes 封面图片资源ID，如果为null则使用默认封面
 * @param chapters 章节列表
 */
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val coverImageRes: Int? = null,
    val chapters: List<Chapter> = emptyList()
)

/**
 * 生成示例书籍数据
 */
fun getSampleBooks(): List<Book> {
    return listOf(
        Book(
            id = "1",
            title = "魔法原理与实践",
            author = "阿尔巴斯·邓布利多",
            description = "这是一本全面介绍魔法基础理论和实践应用的经典教材。从基础魔法原理到高级咒语应用，为魔法学习者提供系统性的知识框架。",
            coverImageRes = R.drawable.maodie,
            chapters = listOf(
                Chapter("1-1", "魔法的起源", 45),
                Chapter("1-2", "基础魔法理论", 38),
                Chapter("1-3", "魔法能量控制", 52),
                Chapter("1-4", "咒语构造原理", 41),
                Chapter("1-5", "实践练习指南", 35)
            )
        ),
        Book(
            id = "2",
            title = "古代咒语大全",
            author = "梅林",
            description = "收录了数千年来最重要的古代咒语，包括失传的禁咒和保护咒语。每个咒语都有详细的施法说明和历史背景。",
            chapters = listOf(
                Chapter("2-1", "远古时代咒语", 65),
                Chapter("2-2", "治疗系咒语", 48),
                Chapter("2-3", "攻击系咒语", 72),
                Chapter("2-4", "防护系咒语", 56),
                Chapter("2-5", "禁咒警示录", 29)
            )
        ),
        Book(
            id = "3",
            title = "炼金术基础",
            author = "尼古拉·勒梅",
            description = "炼金术的入门指南，从基础材料识别到复杂的炼制过程。包含详细的实验步骤和安全注意事项。",
            chapters = listOf(
                Chapter("3-1", "炼金术历史", 32),
                Chapter("3-2", "基础材料学", 44),
                Chapter("3-3", "设备与工具", 28),
                Chapter("3-4", "初级炼制技巧", 58),
                Chapter("3-5", "高级合成方法", 67)
            )
        ),
        Book(
            id = "4",
            title = "魔法药剂学",
            author = "西弗勒斯·斯内普",
            description = "深入研究各种魔法药剂的配制方法，从简单的治疗药剂到复杂的变身药水，涵盖了药剂学的各个方面。",
            chapters = listOf(
                Chapter("4-1", "药剂学基础", 39),
                Chapter("4-2", "常用药材识别", 53),
                Chapter("4-3", "治疗类药剂", 45),
                Chapter("4-4", "增益类药剂", 41),
                Chapter("4-5", "危险药剂警告", 23)
            )
        ),
        Book(
            id = "5",
            title = "占星术入门",
            author = "西比尔·特里劳尼",
            description = "通过观察星象来预知未来的古老艺术。本书详细介绍了星座知识、占卜技巧和预言解读方法。",
            chapters = listOf(
                Chapter("5-1", "星座与命运", 42),
                Chapter("5-2", "占卜工具使用", 36),
                Chapter("5-3", "预言解读技巧", 49),
                Chapter("5-4", "时间魔法理论", 38),
                Chapter("5-5", "实践占卜案例", 55)
            )
        ),
        Book(
            id = "6",
            title = "魔法生物学",
            author = "鲁伯·海格",
            description = "全面介绍魔法世界中的各种神奇生物，包括它们的习性、栖息地和与人类的互动方式。",
            chapters = listOf(
                Chapter("6-1", "友善魔法生物", 47),
                Chapter("6-2", "危险生物防范", 61),
                Chapter("6-3", "生物栖息环境", 39),
                Chapter("6-4", "生物保护法则", 33),
                Chapter("6-5", "驯养技巧指南", 52)
            )
        ),
        Book(
            id = "7",
            title = "时空魔法理论",
            author = "赫敏·格兰杰",
            description = "探索时间和空间魔法的深奥理论，包括时光旅行的原理、空间折叠技术和维度魔法的应用。",
            chapters = listOf(
                Chapter("7-1", "时间魔法原理", 58),
                Chapter("7-2", "空间折叠技术", 63),
                Chapter("7-3", "维度魔法入门", 71),
                Chapter("7-4", "时空悖论研究", 45),
                Chapter("7-5", "实践应用案例", 41)
            )
        ),
        Book(
            id = "8",
            title = "元素魔法指南",
            author = "阿凡达·安昂",
            description = "系统介绍火、水、土、气四大元素魔法的修炼方法，从基础元素操控到高级元素融合技巧。",
            chapters = listOf(
                Chapter("8-1", "火元素魔法", 44),
                Chapter("8-2", "水元素魔法", 46),
                Chapter("8-3", "土元素魔法", 42),
                Chapter("8-4", "气元素魔法", 48),
                Chapter("8-5", "元素融合技巧", 59)
            )
        ),
        Book(
            id = "9",
            title = "魔法防御术",
            author = "詹姆斯·波特",
            description = "专注于防御性魔法的学习，包括护盾咒语、反击技巧和对抗黑魔法的方法。",
            chapters = listOf(
                Chapter("9-1", "基础防护咒语", 37),
                Chapter("9-2", "高级护盾技术", 51),
                Chapter("9-3", "反击策略", 43),
                Chapter("9-4", "黑魔法防御", 66),
                Chapter("9-5", "团队防御战术", 34)
            )
        ),
        Book(
            id = "10",
            title = "高级变形术",
            author = "米勒娃·麦格",
            description = "深入研究变形魔法的高级技巧，从简单的物体变形到复杂的生物变形，挑战魔法师的极限。",
            chapters = listOf(
                Chapter("10-1", "变形术基础理论", 49),
                Chapter("10-2", "物体变形技巧", 55),
                Chapter("10-3", "生物变形术", 68),
                Chapter("10-4", "永久变形咒", 42),
                Chapter("10-5", "变形术安全须知", 26)
            )
        ),
        Book(
            id = "11",
            title = "魔法历史",
            author = "宾斯教授",
            description = "详细记录了魔法世界的发展历程，从古代魔法文明到现代魔法社会的演变过程。",
            chapters = listOf(
                Chapter("11-1", "古代魔法文明", 72),
                Chapter("11-2", "中世纪魔法发展", 58),
                Chapter("11-3", "现代魔法革命", 45),
                Chapter("11-4", "著名魔法师传记", 63),
                Chapter("11-5", "魔法社会制度", 41)
            )
        ),
        Book(
            id = "12",
            title = "幻术与错觉",
            author = "吉德罗·洛哈特",
            description = "专门研究幻术魔法和错觉创造的技巧，包括视觉幻象、心理暗示和现实扭曲等高级幻术。",
            chapters = listOf(
                Chapter("12-1", "幻术基础原理", 38),
                Chapter("12-2", "视觉幻象创造", 52),
                Chapter("12-3", "心理暗示技巧", 44),
                Chapter("12-4", "现实扭曲法术", 67),
                Chapter("12-5", "幻术防护方法", 35)
            )
        )
    )
}

/**
 * MainActivity - 应用程序的主Activity
 *
 * 这是应用程序的入口点，继承自ComponentActivity以支持Jetpack Compose。 该Activity负责设置应用程序的UI内容和配置边到边显示模式。
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var userInfoManager: UserInfoManager
    
    /**
     * Activity生命周期方法 - 在Activity创建时调用
     *
     * @param savedInstanceState 保存的实例状态，用于恢复Activity状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用边到边显示模式，让应用内容可以延伸到状态栏和导航栏区域
        enableEdgeToEdge()
        
        // 初始化用户信息管理器
        userInfoManager = UserInfoManager(this)

        // 设置Compose UI内容
        setContent {
            // 应用自定义主题
            Classwork2Theme {
                // 使用带抽屉导航栏的主界面
                MainScreenWithDrawer(
                    userInfoManager = userInfoManager,
                    onLogout = {
                        // 登出功能：清除用户信息并跳转到登录界面
                        userInfoManager.clearUserInfo()
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
 * @param userInfoManager 用户信息管理器，用于获取当前用户信息
 * @param onLogout 登出回调函数，用于处理用户登出操作
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithDrawer(
    userInfoManager: UserInfoManager,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 抽屉状态管理
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 从UserInfoManager获取真实的用户信息
    val currentUserInfo = remember { userInfoManager.getUserInfo() }
    val currentUserName = if (currentUserInfo.username.isNotEmpty()) {
        currentUserInfo.username
    } else {
        "魔法使" // 默认用户名
    }
    val currentUserAvatar = currentUserInfo.userAvatar

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
            AppNavigation(modifier = Modifier.fillMaxSize().padding(paddingValues))
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
 * 书籍详情页面
 *
 * @param bookId 书籍ID
 * @param onBackClick 返回按钮点击事件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val books = remember { getSampleBooks() }
    val book = books.find { it.id == bookId }
    
    if (book == null) {
        // 错误状态：书籍不存在
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("错误") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "未找到指定的书籍",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // 书籍封面
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (book.coverImageRes != null) {
                            Image(
                                painter = painterResource(book.coverImageRes),
                                contentDescription = book.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .padding(32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                // 书籍标题
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                // 作者信息
                Text(
                    text = "作者: ${book.author}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                // 书籍描述
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "内容简介",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = book.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        )
                    }
                }
            }
            
            item {
                // 章节目录标题
                Text(
                    text = "章节目录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(book.chapters) { chapter ->
                // 章节项
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${chapter.pageCount} 页",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 书籍项组件
 *
 * 显示单个书籍，包含封面图片和标题
 * 
 * @param book 书籍信息
 * @param onClick 点击回调
 */
@Composable
fun BookItem(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable { onClick() }, // 添加点击事件
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // 封面图片
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverImageRes != null) {
                    Image(
                        painter = painterResource(book.coverImageRes),
                        contentDescription = book.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 默认书籍图标
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // 书籍标题
            Text(
                text = book.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 应用导航组件
 *
 * 管理应用的整体导航流程
 */
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "book_list",
        modifier = modifier
    ) {
        composable("book_list") {
            MainContent(
                onBookClick = { bookId ->
                    navController.navigate("book_detail/$bookId")
                }
            )
        }
        composable("book_detail/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailScreen(
                bookId = bookId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * 书籍列表组件
 *
 * 显示响应式网格布局的书籍列表，根据屏幕尺寸自动调整列数
 * 
 * @param books 书籍列表
 * @param onBookClick 书籍点击回调
 */
@Composable
fun BooksList(
    books: List<Book>,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 获取屏幕配置信息
    val configuration = LocalConfiguration.current
    
    // 根据屏幕宽度计算列数
    val columns = when {
        configuration.screenWidthDp < 600 -> 2  // 手机竖屏：2列
        configuration.screenWidthDp < 840 -> 3  // 平板竖屏或手机横屏：3列
        else -> 4  // 大屏设备：4列
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(books) { book ->
            BookItem(
                book = book,
                onClick = { onBookClick(book.id) }
            )
        }
    }
}

/**
 * 主要内容区域组件
 *
 * 显示应用的主要内容，现在显示书籍列表
 * 
 * @param onBookClick 书籍点击回调
 */
@Composable
fun MainContent(
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 获取示例书籍数据
    val books = remember { getSampleBooks() }
    
    // 显示书籍列表
    BooksList(
        books = books,
        onBookClick = onBookClick,
        modifier = modifier
    )
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
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(text = "Hello $name!", modifier = modifier)
}

/** GreetingPreview - 问候语组件的预览函数 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Classwork2Theme { Greeting("Android") }
}

/** MainScreenPreview - 主界面预览函数 */
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    Classwork2Theme { 
        // 预览中创建一个模拟的UserInfoManager
        val mockUserInfo = UserInfo("预览用户", AvatarType.IconAvatar("person"))
        
        // 创建一个简化的预览版本
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "主界面预览",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "用户: ${mockUserInfo.username}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
