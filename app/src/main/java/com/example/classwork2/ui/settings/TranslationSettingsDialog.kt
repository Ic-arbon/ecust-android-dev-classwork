package com.example.classwork2.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.classwork2.settings.DisplayMode
import com.example.classwork2.settings.TranslationSettings
import com.example.classwork2.network.TranslationService
import kotlinx.coroutines.launch

/**
 * 翻译设置对话框
 * 
 * @param onDismiss 关闭对话框的回调
 * @param onSettingsChanged 设置变更的回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationSettingsDialog(
    onDismiss: () -> Unit,
    onSettingsChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings = remember { TranslationSettings(context) }
    val translationService = remember { TranslationService() }
    val scope = rememberCoroutineScope()
    
    // 状态管理
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var targetLanguage by remember { mutableStateOf(settings.targetLanguage) }
    var model by remember { mutableStateOf(settings.model) }
    var temperature by remember { mutableStateOf(settings.temperature) }
    var autoTranslate by remember { mutableStateOf(settings.autoTranslate) }
    var displayMode by remember { mutableStateOf(settings.displayMode) }
    var animationEnabled by remember { mutableStateOf(settings.animationEnabled) }
    var enableThinking by remember { mutableStateOf(settings.enableThinking) }
    
    var showApiKey by remember { mutableStateOf(false) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var connectionTestResult by remember { mutableStateOf<String?>(null) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 标题
                Text(
                    text = "翻译设置",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 滚动内容
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // API密钥设置
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "API配置",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // API密钥输入
                            OutlinedTextField(
                                value = apiKey,
                                onValueChange = { apiKey = it },
                                label = { Text("SiliconFlow API Key") },
                                placeholder = { Text("请输入您的API密钥") },
                                visualTransformation = if (showApiKey) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showApiKey = !showApiKey }) {
                                        Icon(
                                            imageVector = if (showApiKey) {
                                                Icons.Default.Visibility
                                            } else {
                                                Icons.Default.VisibilityOff
                                            },
                                            contentDescription = if (showApiKey) "隐藏" else "显示"
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // 测试连接按钮
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (apiKey.isNotBlank()) {
                                            scope.launch {
                                                isTestingConnection = true
                                                connectionTestResult = try {
                                                    val success = translationService.testConnection(apiKey.trim())
                                                    if (success) "✅ 连接成功" else "❌ 连接失败"
                                                } catch (e: Exception) {
                                                    "❌ 连接失败: ${e.message}"
                                                }
                                                isTestingConnection = false
                                            }
                                        }
                                    },
                                    enabled = !isTestingConnection && apiKey.isNotBlank(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isTestingConnection) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("测试连接")
                                    }
                                }
                                
                                connectionTestResult?.let { result ->
                                    Text(
                                        text = result,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (result.startsWith("✅")) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // 翻译配置
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "翻译配置",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // 目标语言选择
                            var languageExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = languageExpanded,
                                onExpandedChange = { languageExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = targetLanguage,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("目标语言") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = languageExpanded,
                                    onDismissRequest = { languageExpanded = false }
                                ) {
                                    settings.getAvailableLanguages().forEach { language ->
                                        DropdownMenuItem(
                                            text = { Text(language) },
                                            onClick = {
                                                targetLanguage = language
                                                languageExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // 模型选择
                            var modelExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = modelExpanded,
                                onExpandedChange = { modelExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = model,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("翻译模型") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = modelExpanded,
                                    onDismissRequest = { modelExpanded = false }
                                ) {
                                    settings.getAvailableModels().forEach { modelOption ->
                                        DropdownMenuItem(
                                            text = { Text(modelOption) },
                                            onClick = {
                                                model = modelOption
                                                modelExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // 温度参数调节
                            Column {
                                Text(
                                    text = "创意度: ${String.format("%.1f", temperature)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Slider(
                                    value = temperature,
                                    onValueChange = { temperature = it },
                                    valueRange = 0.0f..1.0f,
                                    steps = 10,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "较低值保持准确性，较高值增加创意性",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // 显示设置
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "显示设置",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // 显示模式选择
                            var displayModeExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = displayModeExpanded,
                                onExpandedChange = { displayModeExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = displayMode.displayName,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("显示模式") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = displayModeExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = displayModeExpanded,
                                    onDismissRequest = { displayModeExpanded = false }
                                ) {
                                    DisplayMode.values().forEach { mode ->
                                        DropdownMenuItem(
                                            text = { Text(mode.displayName) },
                                            onClick = {
                                                displayMode = mode
                                                displayModeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // 自动翻译开关
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "自动翻译",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "打开章节时自动开始翻译",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = autoTranslate,
                                    onCheckedChange = { autoTranslate = it }
                                )
                            }
                            
                            // 动画效果开关
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "打字机动画",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "启用流式翻译的打字机效果",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = animationEnabled,
                                    onCheckedChange = { animationEnabled = it }
                                )
                            }
                            
                            // AI思维过程开关
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "AI思维过程",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "启用AI的内部思维过程（禁用可加快翻译速度）",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = enableThinking,
                                    onCheckedChange = { enableThinking = it }
                                )
                            }
                        }
                    }
                }
                
                // 底部按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = {
                            // 保存设置
                            settings.apiKey = apiKey.trim()
                            settings.targetLanguage = targetLanguage
                            settings.model = model
                            settings.temperature = temperature
                            settings.autoTranslate = autoTranslate
                            settings.displayMode = displayMode
                            settings.animationEnabled = animationEnabled
                            settings.enableThinking = enableThinking
                            
                            onSettingsChanged()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}