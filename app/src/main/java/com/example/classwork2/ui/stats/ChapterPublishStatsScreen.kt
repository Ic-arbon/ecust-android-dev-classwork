package com.example.classwork2.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classwork2.Chapter
import com.example.classwork2.database.AppDatabase
import com.example.classwork2.database.converter.DataConverter
import com.example.classwork2.database.repository.BookRepository
import com.example.classwork2.utils.DateFormatter
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 章节投稿时间统计界面
 * 
 * 显示书籍章节的投稿时间分析，包括：
 * - 投稿时间线
 * - 按月统计
 * - 投稿频率分析
 * - 最近更新章节
 * 
 * @param bookId 书籍ID
 * @param onBackClick 返回按钮点击事件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterPublishStatsScreen(
    bookId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val bookRepository = remember { BookRepository(database.bookDao(), database.chapterDao()) }
    
    var bookTitle by remember { mutableStateOf("") }
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // 加载数据
    LaunchedEffect(bookId) {
        try {
            // 获取书籍标题
            val bookEntity = bookRepository.getBookById(bookId)
            bookTitle = bookEntity?.title ?: "未知书籍"
            
            // 获取章节数据
            val chapterEntities = bookRepository.getChaptersByBookId(bookId).first()
            chapters = DataConverter.entitiesToChapters(chapterEntities)
                .sortedBy { it.chapterOrder }
        } catch (e: Exception) {
            // 处理错误
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "投稿时间统计",
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 书籍标题
                item {
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
                                text = bookTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "共 ${chapters.size} 章",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // 基本统计信息
                item {
                    PublishStatsOverview(chapters = chapters)
                }
                
                // 投稿时间线图表
                item {
                    PublishTimelineChart(chapters = chapters)
                }
                
                // 按月统计
                item {
                    MonthlyStatsCard(chapters = chapters)
                }
                
                // 最近更新章节
                item {
                    RecentChaptersCard(chapters = chapters)
                }
            }
        }
    }
}

/**
 * 投稿统计概览卡片
 */
@Composable
fun PublishStatsOverview(
    chapters: List<Chapter>,
    modifier: Modifier = Modifier
) {
    if (chapters.isEmpty()) {
        return
    }
    
    val firstChapter = chapters.minByOrNull { it.updateTime }
    val lastChapter = chapters.maxByOrNull { it.updateTime }
    val daysBetween = if (firstChapter != null && lastChapter != null) {
        ((lastChapter.updateTime - firstChapter.updateTime) / (24 * 60 * 60 * 1000)).toInt()
    } else {
        0
    }
    val avgDaysPerChapter = if (chapters.size > 1 && daysBetween > 0) {
        daysBetween.toDouble() / (chapters.size - 1)
    } else {
        0.0
    }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "投稿概览",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.CalendarMonth,
                    label = "投稿期间",
                    value = if (daysBetween > 0) "${daysBetween} 天" else "单日投稿"
                )
                StatItem(
                    icon = Icons.Default.Schedule,
                    label = "平均间隔",
                    value = if (avgDaysPerChapter > 0) "${String.format("%.1f", avgDaysPerChapter)} 天" else "-"
                )
                StatItem(
                    icon = Icons.Default.AccessTime,
                    label = "最新投稿",
                    value = if (lastChapter != null) {
                        DateFormatter.formatSmartDateTime(lastChapter.updateTime)
                    } else "-"
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 投稿时间线图表
 */
@Composable
fun PublishTimelineChart(
    chapters: List<Chapter>,
    modifier: Modifier = Modifier
) {
    if (chapters.isEmpty()) {
        return
    }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "投稿时间线",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            val primaryColor = MaterialTheme.colorScheme.primary
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 8.dp)
            ) {
                drawTimelineChart(
                    chapters = chapters,
                    primaryColor = primaryColor,
                    textColor = onSurfaceColor
                )
            }
        }
    }
}

/**
 * 绘制时间线图表
 */
fun DrawScope.drawTimelineChart(
    chapters: List<Chapter>,
    primaryColor: Color,
    textColor: Color
) {
    if (chapters.isEmpty()) return
    
    val sortedChapters = chapters.sortedBy { it.updateTime }
    val minTime = sortedChapters.first().updateTime
    val maxTime = sortedChapters.last().updateTime
    val timeRange = maxTime - minTime
    
    val chartWidth = size.width - 32.dp.toPx()
    val chartHeight = size.height - 60.dp.toPx()
    val chartLeft = 16.dp.toPx()
    val chartTop = 30.dp.toPx()
    
    // 绘制坐标轴
    drawLine(
        color = textColor.copy(alpha = 0.3f),
        start = Offset(chartLeft, chartTop + chartHeight),
        end = Offset(chartLeft + chartWidth, chartTop + chartHeight),
        strokeWidth = 1.dp.toPx()
    )
    
    // 准备绘制数据
    val points = sortedChapters.mapIndexed { index, chapter ->
        val x = if (timeRange > 0) {
            chartLeft + (chapter.updateTime - minTime).toFloat() / timeRange * chartWidth
        } else {
            chartLeft + chartWidth / 2
        }
        val y = chartTop + chartHeight - (index + 1).toFloat() / chapters.size * chartHeight
        val isVolumeStart = chapter.subOrder == 1
        
        Triple(Offset(x, y), isVolumeStart, chapter)
    }
    
    // 先绘制普通章节点
    points.forEach { (position, isVolumeStart, _) ->
        if (!isVolumeStart) {
            drawCircle(
                color = primaryColor,
                radius = 2.dp.toPx(),
                center = position
            )
        }
    }
    
    // 后绘制重要节点，确保在最上层
    points.forEach { (position, isVolumeStart, _) ->
        if (isVolumeStart) {
            drawCircle(
                color = Color(0xFFFF6B35), // 橙色表示新卷开始
                radius = 3.dp.toPx(),
                center = position
            )
        }
    }
}

/**
 * 按月统计卡片
 */
@Composable
fun MonthlyStatsCard(
    chapters: List<Chapter>,
    modifier: Modifier = Modifier
) {
    if (chapters.isEmpty()) {
        return
    }
    
    val monthlyStats = remember(chapters) {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
        
        chapters.groupBy { chapter ->
            calendar.timeInMillis = chapter.updateTime
            monthFormat.format(calendar.time)
        }.mapValues { it.value.size }
            .toList()
            .sortedBy { (month, _) ->
                try {
                    monthFormat.parse(month)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
    }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "按月投稿统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            monthlyStats.forEach { (month, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = month,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 简单的柱状图
                        val maxCount = monthlyStats.maxOfOrNull { it.second } ?: 1
                        val barWidth = (count.toFloat() / maxCount * 100).coerceAtMost(100f)
                        
                        Box(
                            modifier = Modifier
                                .width((barWidth * 0.8f).dp)
                                .height(16.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    RoundedCornerShape(8.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "$count 章",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 最近更新章节卡片
 */
@Composable
fun RecentChaptersCard(
    chapters: List<Chapter>,
    modifier: Modifier = Modifier
) {
    if (chapters.isEmpty()) {
        return
    }
    
    val recentChapters = remember(chapters) {
        chapters.sortedByDescending { it.updateTime }.take(5)
    }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "最近投稿章节",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            recentChapters.forEach { chapter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "第${chapter.chapterOrder}话 - ${chapter.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (chapter.volumeTitle != null) {
                            Text(
                                text = chapter.volumeTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    Text(
                        text = DateFormatter.formatSmartDateTime(chapter.updateTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (chapter != recentChapters.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}