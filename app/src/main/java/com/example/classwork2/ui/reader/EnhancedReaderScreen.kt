package com.example.classwork2.ui.reader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classwork2.database.AppDatabase
import com.example.classwork2.database.entities.TranslationStatus
import com.example.classwork2.database.repository.BookRepository
import com.example.classwork2.network.TranslationService
import com.example.classwork2.network.api.SentencePair
import com.example.classwork2.network.api.TranslationState
import com.example.classwork2.settings.DisplayMode
import com.example.classwork2.settings.TranslationSettings
import com.example.classwork2.ui.components.BilingualSentencePair
import com.example.classwork2.ui.settings.TranslationSettingsDialog
import com.example.classwork2.utils.TextProcessor
import com.example.classwork2.network.NarouContentParser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 增强版阅读界面
 * 支持双语显示和流式翻译
 * 
 * @param bookId 书籍ID
 * @param chapterId 章节ID
 * @param onBackClick 返回按钮点击事件
 * @param onNavigateToChapter 章节导航事件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedReaderScreen(
    bookId: String,
    chapterId: String,
    onBackClick: () -> Unit,
    onNavigateToChapter: (String, String, Boolean) -> Unit = { _, _, _ -> }, // 第三个参数表示是否为下一章
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val bookRepository = remember { BookRepository(database.bookDao(), database.chapterDao()) }
    val translationService = remember { TranslationService() }
    val translationSettings = remember { TranslationSettings(context) }
    val textProcessor = remember { TextProcessor() }
    val contentParser = remember { NarouContentParser() }
    val gson = remember { Gson() }
    val scope = rememberCoroutineScope()
    
    // 状态管理
    var chapter by remember { mutableStateOf<com.example.classwork2.database.entities.ChapterEntity?>(null) }
    var book by remember { mutableStateOf<com.example.classwork2.database.entities.BookEntity?>(null) }
    var chapterContent by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 翻译相关状态
    var sentencePairs by remember { mutableStateOf<List<SentencePair>>(emptyList()) }
    var translationState by remember { mutableStateOf(TranslationState()) }
    var displayMode by remember { mutableStateOf(translationSettings.displayMode) }
    var isTranslating by remember { mutableStateOf(false) }
    var showTranslationSettings by remember { mutableStateOf(false) }
    var isTranslationEnabled by remember { mutableStateOf(false) }
    
    // API key配置状态（用于触发重组）
    var isApiKeyConfigured by remember { mutableStateOf(translationSettings.isApiKeyConfigured()) }
    
    // 阅读设置状态
    var fontSize by remember { mutableStateOf(16.sp) }
    var lineHeight by remember { mutableStateOf(1.6f) }
    
    // 列表状态
    val listState = rememberLazyListState()
    
    // 加载章节和内容
    LaunchedEffect(bookId, chapterId) {
        try {
            isLoading = true
            errorMessage = null
            
            // 获取书籍信息
            val bookEntity = bookRepository.getBookById(bookId)
            book = bookEntity
            
            // 获取章节信息
            val chapterEntity = database.chapterDao().getChapterById(chapterId)
            chapter = chapterEntity
            
            if (chapterEntity != null) {
                // 加载原文内容 - 添加动态内容加载逻辑
                if (chapterEntity.content.isNullOrEmpty()) {
                    // 需要从网络下载内容
                    if (!chapterEntity.url.isNullOrEmpty()) {
                        try {
                            // 从网络获取真实的章节内容
                            chapterContent = contentParser.parseChapterContent(chapterEntity.url!!)
                            
                            // 如果获取成功，保存到数据库
                            if (!chapterContent.isNullOrBlank()) {
                                database.chapterDao().updateChapterContent(chapterId, chapterContent!!)
                            } else {
                                chapterContent = "章节内容为空"
                            }
                        } catch (e: Exception) {
                            chapterContent = "网络获取内容失败: ${e.message}\n\n调试信息:\n章节URL: ${chapterEntity.url}"
                        }
                    } else {
                        chapterContent = "无法获取章节内容：章节URL为空\n\n这可能是因为该章节是在添加URL支持之前导入的。请重新导入该书籍以获取章节URL。\n\n调试信息:\n章节ID: $chapterId\n章节标题: ${chapterEntity.title}\nURL字段: ${if (chapterEntity.url == null) "null" else "空字符串"}"
                    }
                } else {
                    chapterContent = chapterEntity.content
                }
                
                // 加载翻译数据 - 确保状态更新在主线程中执行
                if (!chapterContent.isNullOrBlank()) {
                    val translationPairs = loadTranslationData(chapterEntity, chapterContent, textProcessor, gson, translationSettings)
                    withContext(Dispatchers.Main) {
                        sentencePairs = translationPairs
                        // 如果已有翻译内容，默认启用翻译显示
                        if (translationPairs.any { it.translation.isNotEmpty() }) {
                            isTranslationEnabled = true
                        }
                    }
                } else {
                }
            }
            
        } catch (e: Exception) {
            errorMessage = "加载失败: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    // 获取当前章节在列表中的位置
    val allChapters = remember { mutableStateOf<List<com.example.classwork2.database.entities.ChapterEntity>>(emptyList()) }
    LaunchedEffect(bookId) {
        try {
            bookRepository.getChaptersByBookId(bookId).collect { chapters ->
                allChapters.value = chapters.sortedBy { it.chapterOrder }
            }
        } catch (e: Exception) {
            // 忽略错误
        }
    }
    
    val currentChapterIndex = allChapters.value.indexOfFirst { it.id == chapterId }
    val hasNextChapter = currentChapterIndex >= 0 && currentChapterIndex < allChapters.value.size - 1
    val hasPrevChapter = currentChapterIndex > 0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = chapter?.title ?: "加载中...",
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
                },
                actions = {
                    // 翻译切换按钮
                    IconButton(
                        onClick = {
                            if (isTranslating) {
                                // 翻译进行中，允许切换显示状态
                                isTranslationEnabled = !isTranslationEnabled
                            } else if (sentencePairs.any { it.translation.isNotEmpty() }) {
                                // 如果已有翻译，切换显示状态
                                isTranslationEnabled = !isTranslationEnabled
                            } else {
                                // 如果没有翻译，开始翻译并立即启用显示
                                
                                // 立即启用翻译显示
                                isTranslationEnabled = true
                                
                                startTranslation(
                                    chapterEntity = chapter!!,
                                    actualContent = chapterContent,
                                    translationService = translationService,
                                    translationSettings = translationSettings,
                                    textProcessor = textProcessor,
                                    gson = gson,
                                    database = database,
                                    onStateUpdate = { state ->
                                        translationState = state
                                        updateSentencePairs(state, sentencePairs) { pairs ->
                                            sentencePairs = pairs
                                        }
                                    },
                                    onTranslatingChanged = { translating ->
                                        isTranslating = translating
                                    },
                                    scope = scope
                                )
                            }
                        },
                        enabled = !isLoading && chapterContent != null && isApiKeyConfigured
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = when {
                                isTranslating -> "翻译中..."
                                sentencePairs.any { it.translation.isNotEmpty() } -> 
                                    if (isTranslationEnabled) "隐藏翻译" else "显示翻译"
                                else -> "开始翻译"
                            },
                            tint = if (isTranslationEnabled) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                LocalContentColor.current
                        )
                    }
                    
                    // 设置按钮
                    IconButton(
                        onClick = { showTranslationSettings = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "翻译设置"
                        )
                    }
                    
                    // 字体大小控制
                    IconButton(
                        onClick = {
                            fontSize = (fontSize.value - 2f).coerceAtLeast(12f).sp
                        }
                    ) {
                        Text("A-", fontSize = 12.sp)
                    }
                    IconButton(
                        onClick = {
                            fontSize = (fontSize.value + 2f).coerceAtMost(24f).sp
                        }
                    ) {
                        Text("A+", fontSize = 14.sp)
                    }
                }
            )
        },
        bottomBar = {
            // 章节导航栏
            if (hasNextChapter || hasPrevChapter) {
                BottomAppBar(
                    modifier = Modifier.height(56.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 上一章按钮
                        Button(
                            onClick = {
                                if (hasPrevChapter) {
                                    val prevChapter = allChapters.value[currentChapterIndex - 1]
                                    onNavigateToChapter(bookId, prevChapter.id, false) // false表示上一章
                                }
                            },
                            enabled = hasPrevChapter,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            Text("上一章")
                        }
                        
                        // 章节进度指示
                        Text(
                            text = "第${chapter?.chapterOrder ?: "?"} 话",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        // 下一章按钮
                        Button(
                            onClick = {
                                if (hasNextChapter) {
                                    val nextChapter = allChapters.value[currentChapterIndex + 1]
                                    onNavigateToChapter(bookId, nextChapter.id, true) // true表示下一章
                                }
                            },
                            enabled = hasNextChapter,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            Text("下一章")
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("正在加载章节内容...")
                    }
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            chapterContent != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // 章节标题
                            Text(
                                text = chapter?.title ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }
                        
                        // 显示内容逻辑
                        if (sentencePairs.isNotEmpty()) {
                            items(sentencePairs) { pair ->
                                if (isTranslationEnabled) {
                                    // 翻译模式：根据显示模式显示双语或仅译文
                                    if (displayMode == DisplayMode.BILINGUAL) {
                                        // 双语对照模式
                                        BilingualSentencePair(
                                            originalText = pair.original,
                                            translatedText = pair.translation,
                                            isTranslating = pair.isTranslating,
                                            showTranslation = true,
                                            originalStyle = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = fontSize,
                                                lineHeight = fontSize * lineHeight
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        // 仅译文模式
                                        BilingualSentencePair(
                                            originalText = "",
                                            translatedText = pair.translation,
                                            isTranslating = pair.isTranslating,
                                            showTranslation = true,
                                            originalStyle = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = fontSize,
                                                lineHeight = fontSize * lineHeight
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    // 原文模式：仅显示原文
                                    Text(
                                        text = pair.original,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = fontSize,
                                            lineHeight = fontSize * lineHeight
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            // 没有句子对时显示原始内容
                            item {
                                Text(
                                    text = chapterContent!!,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = fontSize,
                                        lineHeight = fontSize * lineHeight
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
        
    // 翻译设置对话框
    if (showTranslationSettings) {
        TranslationSettingsDialog(
            onDismiss = { showTranslationSettings = false },
            onSettingsChanged = {
                // 更新显示模式
                displayMode = translationSettings.displayMode
                // 更新API key配置状态，触发重组
                isApiKeyConfigured = translationSettings.isApiKeyConfigured()
            },
            translationSettings = translationSettings
        )
        }
    }
}

// 辅助函数：加载翻译数据
private suspend fun loadTranslationData(
    chapterEntity: com.example.classwork2.database.entities.ChapterEntity,
    actualContent: String?,
    textProcessor: TextProcessor,
    gson: Gson,
    translationSettings: TranslationSettings
): List<SentencePair> {
    try {
        val content = actualContent ?: chapterEntity.content ?: return emptyList()
        
        // 如果已有翻译数据且未禁用缓存，加载它
        if (!translationSettings.disableCache &&
            chapterEntity.translationStatus == TranslationStatus.COMPLETED &&
            !chapterEntity.originalSentences.isNullOrEmpty() &&
            !chapterEntity.translatedSentences.isNullOrEmpty()) {
            
            
            val originalType = object : TypeToken<List<String>>() {}.type
            val originalSentences: List<String> = gson.fromJson(chapterEntity.originalSentences, originalType)
            val translatedSentences: List<String> = gson.fromJson(chapterEntity.translatedSentences, originalType)
            
            
            originalSentences.forEachIndexed { index, sentence ->
            }
            
            translatedSentences.forEachIndexed { index, sentence ->
            }
            
            val pairs = originalSentences.mapIndexed { index, original ->
                val translation = translatedSentences.getOrElse(index) { "" }
                val pair = SentencePair(
                    original = original,
                    translation = translation,
                    isTranslating = false,
                    isComplete = true
                )
                pair
            }
            
            return pairs
        } else {
            // 创建原文句子对
            val sentences = textProcessor.splitIntoSentences(content)
            
            val pairs = sentences.mapIndexed { index, sentence ->
                val pair = SentencePair(
                    original = sentence,
                    translation = "",
                    isTranslating = false,
                    isComplete = false
                )
                pair
            }
            
            return pairs
        }
    } catch (e: Exception) {
        // 出错时创建基本的句子对
        val content = actualContent ?: chapterEntity.content ?: ""
        val sentences = textProcessor.splitIntoSentences(content)
        val pairs = sentences.map { sentence ->
            SentencePair(
                original = sentence,
                translation = "",
                isTranslating = false,
                isComplete = false
            )
        }
        return pairs
    } finally {
    }
}

// 辅助函数：开始翻译
private fun startTranslation(
    chapterEntity: com.example.classwork2.database.entities.ChapterEntity,
    actualContent: String?,
    translationService: TranslationService,
    translationSettings: TranslationSettings,
    textProcessor: TextProcessor,
    gson: Gson,
    database: AppDatabase,
    onStateUpdate: (TranslationState) -> Unit,
    onTranslatingChanged: (Boolean) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val content = actualContent ?: chapterEntity.content ?: return
    
    scope.launch {
        onTranslatingChanged(true)
        
        try {
            // 如果禁用缓存，清理之前的翻译数据
            if (translationSettings.disableCache) {
                withContext(Dispatchers.IO) {
                    database.chapterDao().clearTranslationData(chapterEntity.id)
                }
            }
            
            // 更新数据库状态为翻译中
            withContext(Dispatchers.IO) {
                database.chapterDao().updateTranslationStatus(chapterEntity.id, TranslationStatus.TRANSLATING)
            }
            
            translationService.translateTextStream(
                originalText = content,
                apiKey = translationSettings.apiKey,
                targetLanguage = translationSettings.targetLanguage,
                enableThinking = translationSettings.enableThinking
            ).collect { state ->
                // 确保UI状态更新在主线程
                withContext(Dispatchers.Main) {
                    onStateUpdate(state)
                }
                
                // 如果翻译完成，保存到数据库
                if (state.isComplete && state.error == null) {
                    withContext(Dispatchers.IO) {
                        val originalJson = gson.toJson(state.originalSentences)
                        val translatedJson = gson.toJson(state.translations)
                        
                        database.chapterDao().updateTranslationData(
                            chapterId = chapterEntity.id,
                            translatedContent = state.translations.joinToString("\n"),
                            originalSentences = originalJson,
                            translatedSentences = translatedJson,
                            status = TranslationStatus.COMPLETED
                        )
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.IO) {
                database.chapterDao().updateTranslationStatus(chapterEntity.id, TranslationStatus.ERROR)
            }
            withContext(Dispatchers.Main) {
                onStateUpdate(TranslationState(error = "翻译失败: ${e.message}"))
            }
        } finally {
            onTranslatingChanged(false)
        }
    }
}

// 辅助函数：更新句子对
private fun updateSentencePairs(
    translationState: TranslationState,
    currentPairs: List<SentencePair>,
    onUpdate: (List<SentencePair>) -> Unit
) {
    if (translationState.originalSentences.isEmpty()) return
    
    val updatedPairs = translationState.originalSentences.mapIndexed { index, original ->
        val translation = translationState.translations.getOrElse(index) { "" }
        val isTranslating = index == translationState.currentTranslatingIndex && translationState.isTranslating
        val partialTranslation = if (isTranslating) translationState.currentPartialTranslation else translation
        
        SentencePair(
            original = original,
            translation = partialTranslation,
            isTranslating = isTranslating,
            isComplete = translation.isNotEmpty() && !isTranslating
        )
    }
    
    onUpdate(updatedPairs)
}