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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import com.example.classwork2.database.AppDatabase
import com.example.classwork2.database.repository.BookRepository
import com.example.classwork2.database.converter.DataConverter
import com.example.classwork2.network.NetworkBookService
import com.example.classwork2.network.ImportResult
import com.example.classwork2.network.ImportProgress
import com.example.classwork2.network.ImportProgressCallback
import com.example.classwork2.network.ImportStage
import com.example.classwork2.network.SimpleCancellationToken
import kotlinx.coroutines.launch

/**
 * 章节数据类
 * 
 * @param id 章节唯一标识符
 * @param title 章节标题
 * @param pageCount 页数
 * @param volumeTitle 卷/大章节标题（可选）
 * @param volumeOrder 卷序号（可选）
 * @param subOrder 卷内序号（可选）
 * @param chapterOrder 章节全局序号
 */
data class Chapter(
    val id: String,
    val title: String,
    val pageCount: Int,
    val volumeTitle: String? = null,
    val volumeOrder: Int? = null,
    val subOrder: Int? = null,
    val chapterOrder: Int = 0
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
 * @param lastUpdateTime 最后更新时间（毫秒时间戳）
 * @param chapters 章节列表
 */
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val coverImageRes: Int? = null,
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val chapters: List<Chapter> = emptyList()
)


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
                // 创建导航控制器状态
                val navController = rememberNavController()
                
                // 主界面导航宿主
                NavHost(
                    navController = navController,
                    startDestination = "main",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("main") {
                        MainScreenWithDrawer(
                            userInfoManager = userInfoManager,
                            onLogout = {
                                // 登出功能：清除用户信息并跳转到登录界面
                                userInfoManager.clearUserInfo()
                                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish() // 关闭主界面，防止用户按返回键回到主界面
                            },
                            onImportClick = {
                                navController.navigate("import_book")
                            }
                        )
                    }
                    composable("import_book") {
                        ImportBookScreen(
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onBookImported = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
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
    onImportClick: () -> Unit,
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
                // 顶部应用栏，包含汉堡菜单图标和导入按钮
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
                    actions = {
                        // 网络导入按钮
                        IconButton(
                            onClick = onImportClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "导入书籍"
                            )
                        }
                    }
                )
            },
        ) { paddingValues ->
            // 主要内容区域
            AppNavigation(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            )
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
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val bookRepository = remember { BookRepository(database.bookDao(), database.chapterDao()) }
    
    var book by remember { mutableStateOf<Book?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // 折叠状态管理：使用卷标题作为键，存储每个卷的展开/折叠状态
    var expandedVolumes by remember { mutableStateOf(setOf<String>()) }
    
    // 从数据库加载书籍和章节信息
    LaunchedEffect(bookId) {
        try {
            val bookEntity = bookRepository.getBookById(bookId)
            if (bookEntity != null) {
                bookRepository.getChaptersByBookId(bookId).collect { chapterEntities ->
                    val chapters = DataConverter.entitiesToChapters(chapterEntities)
                    book = DataConverter.entityToBook(bookEntity, chapters)
                    isLoading = false
                }
            } else {
                isLoading = false
            }
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    if (isLoading) {
        // 加载状态
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("加载中...") },
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
                CircularProgressIndicator()
            }
        }
        return
    }
    
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
    
    book?.let { currentBook ->
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentBook.title) },
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
                .padding(paddingValues)
                .animateContentSize(
                    animationSpec = tween(300)
                ),
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
                        val coverRes = currentBook.coverImageRes
                        if (coverRes != null) {
                            Image(
                                painter = painterResource(coverRes),
                                contentDescription = currentBook.title,
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
                    text = currentBook.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                // 作者信息
                Text(
                    text = "作者: ${currentBook.author}",
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
                            text = currentBook.description,
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
            
            // 按层级结构显示章节
            val sortedChapters = currentBook.chapters.sortedWith(
                compareBy<Chapter> { it.volumeOrder ?: 0 }.thenBy { it.chapterOrder }
            )
            val groupedChapters = sortedChapters.groupBy { it.volumeTitle }
            
            // 如果有层级结构，按卷显示
            if (groupedChapters.keys.any { it != null }) {
                // 按卷序号排序组
                val sortedGroups = groupedChapters.toList().sortedBy { (volumeTitle, chapters) ->
                    chapters.firstOrNull()?.volumeOrder ?: 0
                }
                sortedGroups.forEach { (volumeTitle, chaptersInVolume) ->
                    // 显示卷标题（如果存在）
                    if (volumeTitle != null) {
                        item {
                            val isExpanded = expandedVolumes.contains(volumeTitle)
                            val rotationAngle by animateFloatAsState(
                                targetValue = if (isExpanded) 180f else 0f,
                                animationSpec = tween(300),
                                label = "arrow_rotation"
                            )
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clickable {
                                        expandedVolumes = if (isExpanded) {
                                            expandedVolumes - volumeTitle
                                        } else {
                                            expandedVolumes + volumeTitle
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = "卷",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = volumeTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${chaptersInVolume.size} 话",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) "收起" else "展开",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .rotate(rotationAngle)
                                    )
                                }
                            }
                        }
                    }
                    
                    // 只在展开时显示该卷下的章节
                    if (volumeTitle == null || expandedVolumes.contains(volumeTitle)) {
                        items(chaptersInVolume.sortedBy { it.chapterOrder }) { chapter ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = if (volumeTitle != null) 16.dp else 0.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 如果有子序号，显示序号
                                if (chapter.subOrder != null && volumeTitle != null) {
                                    Text(
                                        text = "${chapter.subOrder}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                Text(
                                    text = chapter.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    }
                }
            } else {
                // 如果没有层级结构，按原方式显示
                items(currentBook.chapters.sortedWith(compareBy<Chapter> { it.volumeOrder ?: 0 }.thenBy { it.chapterOrder })) { chapter ->
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = chapter.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
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
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val bookRepository = remember { BookRepository(database.bookDao(), database.chapterDao()) }
    
    // 从数据库获取书籍数据
    val bookEntities by bookRepository.getAllBooks().collectAsState(initial = null)
    val books = remember(bookEntities) {
        if (bookEntities != null) {
            DataConverter.entitiesToBooks(bookEntities!!)
        } else {
            null
        }
    }
    
    when {
        // 数据加载中
        books == null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        // 无书籍，显示引导文字
        books.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "还没有任何书籍",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击右上角的 + 按钮开始导入书籍",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        // 有书籍，显示列表
        else -> {
            BooksList(
                books = books,
                onBookClick = onBookClick,
                modifier = modifier
            )
        }
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

/**
 * 网络书籍导入界面
 * 
 * 提供从网络URL导入书籍信息的功能
 * 
 * @param onBackClick 返回按钮点击回调
 * @param onBookImported 书籍导入成功回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportBookScreen(
    onBackClick: () -> Unit,
    onBookImported: () -> Unit,
    modifier: Modifier = Modifier
) {
    var url by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewBookInfo by remember { mutableStateOf<com.example.classwork2.network.NetworkBookInfo?>(null) }
    
    // 进度相关状态
    var importProgress by remember { mutableStateOf<ImportProgress?>(null) }
    var cancellationToken by remember { mutableStateOf<SimpleCancellationToken?>(null) }
    
    val context = LocalContext.current
    val networkBookService = remember { NetworkBookService() }
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val bookRepository = remember { BookRepository(database.bookDao(), database.chapterDao()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入网络书籍") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 支持的网站提示
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "支持的网站",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val supportedSites = networkBookService.getSupportedSites()
                    supportedSites.forEach { site ->
                        Text(
                            text = "• $site",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // URL输入框
            OutlinedTextField(
                value = url,
                onValueChange = { 
                    url = it
                    errorMessage = null
                    previewBookInfo = null
                },
                label = { Text("书籍页面URL") },
                placeholder = { Text("例如: https://ncode.syosetu.com/n3297eu/") },
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 预览按钮
                Button(
                    onClick = {
                        if (url.isBlank()) {
                            errorMessage = "请输入URL"
                            return@Button
                        }
                        
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            importProgress = null
                            
                            // 创建进度回调
                            val progressCallback = object : ImportProgressCallback {
                                override fun onProgressUpdate(progress: ImportProgress) {
                                    importProgress = progress
                                }
                                
                                override fun onError(error: String) {
                                    errorMessage = error
                                    importProgress = null
                                }
                                
                                override fun onComplete() {
                                    importProgress = null
                                }
                            }
                            
                            when (val result = networkBookService.previewBookFromUrl(url.trim(), progressCallback)) {
                                is ImportResult.Success -> {
                                    previewBookInfo = result.bookInfo
                                }
                                is ImportResult.Error -> {
                                    errorMessage = result.message
                                }
                            }
                            isLoading = false
                        }
                    },
                    enabled = !isLoading && url.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("预览")
                    }
                }
                
                // 导入按钮
                Button(
                    onClick = {
                        if (isLoading && cancellationToken != null) {
                            // 取消导入
                            cancellationToken?.cancel()
                        } else if (url.isNotBlank()) {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                importProgress = null
                                
                                // 创建取消令牌
                                val token = SimpleCancellationToken()
                                cancellationToken = token
                                
                                // 创建进度回调
                                val progressCallback = object : ImportProgressCallback {
                                    override fun onProgressUpdate(progress: ImportProgress) {
                                        importProgress = progress
                                    }
                                    
                                    override fun onError(error: String) {
                                        errorMessage = error
                                        importProgress = null
                                    }
                                    
                                    override fun onComplete() {
                                        importProgress = null
                                    }
                                }
                                
                                try {
                                    // 使用完整导入流程，包含章节解析和进度回调
                                    when (val result = networkBookService.importBookFromUrl(url.trim(), progressCallback, token)) {
                                        is ImportResult.Success -> {
                                            val bookInfo = result.bookInfo
                                            
                                            // 转换为数据库实体并保存
                                            val bookEntity = com.example.classwork2.database.entities.BookEntity(
                                                id = bookInfo.sourceUrl.hashCode().toString(),
                                                title = bookInfo.title,
                                                author = bookInfo.author,
                                                description = bookInfo.description,
                                                lastUpdateTime = bookInfo.lastUpdateTime
                                            )
                                            
                                            bookRepository.insertBook(bookEntity)
                                            
                                            // 保存章节信息，重新分配连续的章节序号
                                            val sortedChapters = bookInfo.chapters.sortedBy { it.order }
                                            sortedChapters.forEachIndexed { index, chapterInfo ->
                                                val chapterEntity = com.example.classwork2.database.entities.ChapterEntity(
                                                    id = chapterInfo.url.hashCode().toString(),
                                                    bookId = bookEntity.id,
                                                    title = chapterInfo.title,
                                                    pageCount = 1, // 暂时设为1页，后续可实现章节内容获取后计算实际页数
                                                    volumeTitle = chapterInfo.volumeTitle,
                                                    volumeOrder = chapterInfo.volumeOrder,
                                                    subOrder = chapterInfo.subOrder,
                                                    chapterOrder = index + 1  // 使用从1开始的连续序号
                                                )
                                                database.chapterDao().insertChapter(chapterEntity)
                                            }
                                            
                                            onBookImported()
                                        }
                                        is ImportResult.Error -> {
                                            errorMessage = result.message
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "导入失败: ${e.message}"
                                } finally {
                                    isLoading = false
                                    importProgress = null
                                    cancellationToken = null
                                }
                            }
                        }
                    },
                    enabled = url.isNotBlank(), // 移除 && !isLoading 条件，允许在导入时点击取消
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading && cancellationToken != null) {
                        Text("取消")
                    } else {
                        Text("导入")
                    }
                }
            }
            
            // 进度显示
            importProgress?.let { progress ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "导入进度",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // 阶段显示
                        Text(
                            text = "当前阶段: ${progress.stage.displayName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        // 进度条
                        LinearProgressIndicator(
                            progress = { progress.progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        
                        // 进度信息
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${progress.currentStep} / ${progress.totalSteps}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${progress.percentage}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        // 详细信息
                        if (progress.message.isNotEmpty()) {
                            Text(
                                text = progress.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // 书籍预览信息
            previewBookInfo?.let { bookInfo ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "书籍预览",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "标题: ${bookInfo.title}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "作者: ${bookInfo.author}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "状态: ${bookInfo.status}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        if (bookInfo.chapters.isNotEmpty()) {
                            Text(
                                text = "章节数: ${bookInfo.chapters.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            // 显示卷信息
                            val volumeInfo = bookInfo.chapters.groupBy { it.volumeTitle }
                            if (volumeInfo.keys.filterNotNull().isNotEmpty()) {
                                Text(
                                    text = "分卷: ${volumeInfo.keys.filterNotNull().size} 卷",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                // 显示每卷的章节数
                                volumeInfo.forEach { (volumeTitle, chapters) ->
                                    if (volumeTitle != null) {
                                        Text(
                                            text = "├─ $volumeTitle: ${chapters.size} 话",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (bookInfo.tags.isNotEmpty()) {
                            Text(
                                text = "标签: ${bookInfo.tags.joinToString(", ")}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Text(
                            text = "简介:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = bookInfo.description,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
