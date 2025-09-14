package com.example.classwork2.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.classwork2.utils.ImageFileManager
import kotlinx.coroutines.launch
import java.io.File

/**
 * 全屏图片查看器
 * 
 * 支持缩放、拖拽手势，并提供编辑和关闭按钮
 * 
 * @param imagePath 图片文件路径，null时显示默认图标
 * @param title 图片标题
 * @param onClose 关闭回调
 * @param onEdit 编辑回调
 */
@Composable
fun FullscreenImageViewer(
    imagePath: String?,
    title: String,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
        ) {
            // 可缩放的图片区域
            ZoomableImage(
                imagePath = imagePath,
                title = title,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
            
            // 顶部操作栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                
                // 操作按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 编辑按钮
                    FilledIconButton(
                        onClick = onEdit,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑封面",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    // 关闭按钮
                    FilledIconButton(
                        onClick = onClose,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        }
    }
}

/**
 * 可缩放的图片组件
 * 
 * 支持双指缩放和拖拽手势
 * 
 * @param imagePath 图片文件路径，null时显示默认图标
 * @param title 图片描述
 */
@Composable
private fun ZoomableImage(
    imagePath: String?,
    title: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        
                        if (scale > 1f) {
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(
                                    -size.width * (scale - 1f) / 2,
                                    size.width * (scale - 1f) / 2
                                ),
                                y = (offset.y + pan.y).coerceIn(
                                    -size.height * (scale - 1f) / 2,
                                    size.height * (scale - 1f) / 2
                                )
                            )
                        } else {
                            offset = Offset.Zero
                        }
                    }
                )
            }
            .clickable {
                // 双击重置缩放
                if (scale > 1f) {
                    scale = 1f
                    offset = Offset.Zero
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (imagePath != null && File(imagePath).exists()) {
            // 显示自定义图片
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )
        } else {
            // 显示默认图标
            Surface(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * 智能图片组件
 * 
 * 根据图片类型自动选择加载方式：文件路径或drawable资源
 * 
 * @param imagePath 图片文件路径，null时显示默认图标
 * @param contentDescription 图片描述
 * @param modifier 修饰符
 * @param contentScale 内容缩放方式
 */
@Composable
fun SmartImage(
    imagePath: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (imagePath != null && File(imagePath).exists()) {
        // 加载本地文件
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imagePath)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        // 显示默认图标
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(48.dp)
                    .padding(12.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * 封面编辑对话框
 * 
 * 提供从相册选择、拍照、删除当前封面等功能
 * 
 * @param currentImagePath 当前图片路径
 * @param onImageSelected 图片选择回调，返回新的图片文件路径
 * @param onImageDeleted 图片删除回调
 * @param onDismiss 对话框关闭回调
 */
@Composable
fun CoverEditDialog(
    currentImagePath: String?,
    onImageSelected: (String) -> Unit,
    onImageDeleted: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 相册选择器
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                // 将选中的图片复制到应用私有存储
                val imageManager = ImageFileManager(context)
                val savedPath = imageManager.saveImageFromUri(uri)
                if (savedPath != null) {
                    onImageSelected(savedPath)
                    onDismiss()
                }
            }
        }
    }
    
    // 相机拍照
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // 拍照成功，图片已保存到临时文件
            // 这里需要实际的文件路径处理
            onDismiss()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "编辑封面",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 当前封面预览
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    SmartImage(
                        imagePath = currentImagePath,
                        contentDescription = "当前封面",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // 操作选项
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 从相册选择
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("从相册选择")
                    }
                    
                    // 拍照（暂时注释，需要额外的权限处理）
                    /*
                    OutlinedButton(
                        onClick = {
                            // TODO: 实现拍照功能
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("拍照")
                    }
                    */
                    
                    // 删除当前封面
                    if (currentImagePath != null) {
                        OutlinedButton(
                            onClick = {
                                onImageDeleted()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("删除封面")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}