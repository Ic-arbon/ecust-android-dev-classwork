package com.example.classwork2.ui.reader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.classwork2.ui.components.TranslationProgressIndicator
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
    onNavigateToChapter: (String, String) -> Unit = { _, _ -> },
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
                    val translationPairs = loadTranslationData(chapterEntity, chapterContent, textProcessor, gson)
                    withContext(Dispatchers.Main) {
                        sentencePairs = translationPairs
                        println("=== [EnhancedReaderScreen] 翻译数据加载完成，句子对数量: ${translationPairs.size} ===")
                    }
                } else {
                    println("=== [EnhancedReaderScreen] 章节内容为空，跳过翻译数据加载 ===")
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
                    // 显示模式切换
                    IconButton(
                        onClick = {
                            displayMode = when (displayMode) {
                                DisplayMode.ORIGINAL_ONLY -> DisplayMode.BILINGUAL
                                DisplayMode.BILINGUAL -> DisplayMode.TRANSLATION_ONLY
                                DisplayMode.TRANSLATION_ONLY -> DisplayMode.ORIGINAL_ONLY
                            }
                            translationSettings.displayMode = displayMode
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "切换显示模式: ${displayMode.displayName}"
                        )
                    }
                    
                    // 翻译按钮
                    IconButton(
                        onClick = {
                            if (isTranslating) {
                                // 停止翻译（暂时不实现取消功能）
                            } else {
                                println("=== [EnhancedReaderScreen] 开始翻译 ===")
                                println("章节内容长度: ${chapterContent?.length ?: 0}")
                                println("句子对数量: ${sentencePairs.size}")
                                
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
                        enabled = !isLoading && chapterContent != null && translationSettings.isApiKeyConfigured().also { configured ->
                            println("=== [EnhancedReaderScreen] 翻译按钮状态 ===")
                            println("isLoading: $isLoading")
                            println("chapterContent != null: ${chapterContent != null}")
                            println("isApiKeyConfigured: $configured")
                            println("button enabled: ${!isLoading && chapterContent != null && configured}")
                            println("sentencePairs.size: ${sentencePairs.size}")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = if (isTranslating) "停止翻译" else "开始翻译"
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
                                    onNavigateToChapter(bookId, prevChapter.id)
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
                                    onNavigateToChapter(bookId, nextChapter.id)
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
                    // 翻译进度指示器
                    if (isTranslating || sentencePairs.any { it.translation.isNotEmpty() }) {
                        TranslationProgressIndicator(
                            currentSentence = sentencePairs.count { it.isComplete },
                            totalSentences = sentencePairs.size,
                            isTranslating = isTranslating,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                    
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
                        
                        // 显示句子对
                        if (sentencePairs.isNotEmpty()) {
                            items(sentencePairs) { pair ->
                                BilingualSentencePair(
                                    originalText = pair.original,
                                    translatedText = pair.translation,
                                    isTranslating = pair.isTranslating,
                                    showTranslation = displayMode != DisplayMode.ORIGINAL_ONLY,
                                    originalStyle = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = fontSize,
                                        lineHeight = fontSize * lineHeight
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            // 如果没有句子对，显示原始内容
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
                    displayMode = translationSettings.displayMode
                }
            )
        }
    }
}

// 辅助函数：加载翻译数据
private suspend fun loadTranslationData(
    chapterEntity: com.example.classwork2.database.entities.ChapterEntity,
    actualContent: String?,
    textProcessor: TextProcessor,
    gson: Gson
): List<SentencePair> {
    try {
        println("=== [EnhancedReaderScreen] 加载翻译数据 ===")
        val content = actualContent ?: chapterEntity.content ?: return emptyList()
        println("章节内容长度: ${content.length} 字符")
        println("翻译状态: ${chapterEntity.translationStatus}")
        println("原文句子JSON是否为空: ${chapterEntity.originalSentences.isNullOrEmpty()}")
        println("译文句子JSON是否为空: ${chapterEntity.translatedSentences.isNullOrEmpty()}")
        
        // 如果已有翻译数据，加载它
        if (chapterEntity.translationStatus == TranslationStatus.COMPLETED &&
            !chapterEntity.originalSentences.isNullOrEmpty() &&
            !chapterEntity.translatedSentences.isNullOrEmpty()) {
            
            println("检测到完整的翻译数据，加载中...")
            
            val originalType = object : TypeToken<List<String>>() {}.type
            val originalSentences: List<String> = gson.fromJson(chapterEntity.originalSentences, originalType)
            val translatedSentences: List<String> = gson.fromJson(chapterEntity.translatedSentences, originalType)
            
            println("原文句子数量: ${originalSentences.size}")
            println("译文句子数量: ${translatedSentences.size}")
            
            originalSentences.forEachIndexed { index, sentence ->
                println("原文句子 ${index + 1}: \"$sentence\"")
            }
            
            translatedSentences.forEachIndexed { index, sentence ->
                println("译文句子 ${index + 1}: \"$sentence\"")
            }
            
            val pairs = originalSentences.mapIndexed { index, original ->
                val translation = translatedSentences.getOrElse(index) { "" }
                val pair = SentencePair(
                    original = original,
                    translation = translation,
                    isTranslating = false,
                    isComplete = true
                )
                println("创建句子对 ${index + 1}: 原文=\"$original\", 译文=\"$translation\"")
                pair
            }
            
            println("创建的句子对数量: ${pairs.size}")
            return pairs
        } else {
            // 创建原文句子对
            println("未检测到完整的翻译数据，创建基本句子对")
            val sentences = textProcessor.splitIntoSentences(content)
            println("分割得到 ${sentences.size} 个原文句子")
            
            val pairs = sentences.mapIndexed { index, sentence ->
                val pair = SentencePair(
                    original = sentence,
                    translation = "",
                    isTranslating = false,
                    isComplete = false
                )
                println("创建基本句子对 ${index + 1}: \"$sentence\"")
                pair
            }
            
            println("创建的基本句子对数量: ${pairs.size}")
            return pairs
        }
    } catch (e: Exception) {
        // 出错时创建基本的句子对
        println("[EnhancedReaderScreen] 加载翻译数据失败: ${e.message}")
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
        println("在异常处理中创建 ${pairs.size} 个基本句子对")
        return pairs
    } finally {
        println("=== [EnhancedReaderScreen] 翻译数据加载完成 ===")
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
    println("=== [startTranslation] 使用内容长度: ${content.length} 字符 ===")
    
    scope.launch {
        onTranslatingChanged(true)
        
        try {
            // 更新数据库状态为翻译中
            withContext(Dispatchers.IO) {
                database.chapterDao().updateTranslationStatus(chapterEntity.id, TranslationStatus.TRANSLATING)
            }
            
            translationService.translateTextStream(
                originalText = content,
                apiKey = translationSettings.apiKey,
                targetLanguage = translationSettings.targetLanguage
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