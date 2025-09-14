package com.example.classwork2.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 图片文件管理工具类
 * 
 * 负责处理图片的保存、压缩和文件管理
 */
class ImageFileManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ImageFileManager"
        private const val COVERS_DIR = "book_covers"
        private const val MAX_WIDTH = 800
        private const val MAX_HEIGHT = 1200
        private const val QUALITY = 85
    }
    
    private val coversDir: File by lazy {
        File(context.filesDir, COVERS_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    /**
     * 从Uri保存图片到应用私有存储
     * 
     * @param uri 图片Uri
     * @return 保存后的文件路径，失败时返回null
     */
    suspend fun saveImageFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e(TAG, "无法打开输入流: $uri")
                return@withContext null
            }
            
            // 读取原始图片
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (originalBitmap == null) {
                Log.e(TAG, "无法解码图片: $uri")
                return@withContext null
            }
            
            // 压缩图片
            val compressedBitmap = compressBitmap(originalBitmap)
            
            // 生成文件名
            val fileName = generateFileName()
            val targetFile = File(coversDir, fileName)
            
            // 保存图片
            val saved = saveBitmapToFile(compressedBitmap, targetFile)
            
            // 清理资源
            if (originalBitmap != compressedBitmap) {
                originalBitmap.recycle()
            }
            compressedBitmap.recycle()
            
            if (saved) {
                Log.d(TAG, "图片保存成功: ${targetFile.absolutePath}")
                targetFile.absolutePath
            } else {
                Log.e(TAG, "图片保存失败: ${targetFile.absolutePath}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存图片时出错", e)
            null
        }
    }
    
    /**
     * 从文件路径复制图片到应用私有存储
     * 
     * @param sourcePath 源文件路径
     * @return 保存后的文件路径，失败时返回null
     */
    suspend fun copyImageFromPath(sourcePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) {
                Log.e(TAG, "源文件不存在: $sourcePath")
                return@withContext null
            }
            
            // 读取原始图片
            val originalBitmap = BitmapFactory.decodeFile(sourcePath)
            if (originalBitmap == null) {
                Log.e(TAG, "无法解码图片: $sourcePath")
                return@withContext null
            }
            
            // 压缩图片
            val compressedBitmap = compressBitmap(originalBitmap)
            
            // 生成文件名
            val fileName = generateFileName()
            val targetFile = File(coversDir, fileName)
            
            // 保存图片
            val saved = saveBitmapToFile(compressedBitmap, targetFile)
            
            // 清理资源
            if (originalBitmap != compressedBitmap) {
                originalBitmap.recycle()
            }
            compressedBitmap.recycle()
            
            if (saved) {
                Log.d(TAG, "图片复制成功: ${targetFile.absolutePath}")
                targetFile.absolutePath
            } else {
                Log.e(TAG, "图片复制失败: ${targetFile.absolutePath}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "复制图片时出错", e)
            null
        }
    }
    
    /**
     * 删除图片文件
     * 
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    fun deleteImage(filePath: String): Boolean {
        try {
            val file = File(filePath)
            if (file.exists() && file.parent == coversDir.absolutePath) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "图片删除成功: $filePath")
                } else {
                    Log.w(TAG, "图片删除失败: $filePath")
                }
                return deleted
            } else {
                Log.w(TAG, "文件不存在或不在封面目录中: $filePath")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "删除图片时出错", e)
            return false
        }
    }
    
    /**
     * 清理未使用的封面文件
     * 
     * @param usedPaths 正在使用的文件路径列表
     */
    fun cleanupUnusedImages(usedPaths: List<String>) {
        try {
            val usedFiles = usedPaths.map { File(it).name }.toSet()
            
            coversDir.listFiles()?.forEach { file ->
                if (file.isFile && !usedFiles.contains(file.name)) {
                    val deleted = file.delete()
                    if (deleted) {
                        Log.d(TAG, "清理未使用的封面文件: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "清理文件时出错", e)
        }
    }
    
    /**
     * 获取封面目录的大小（字节）
     */
    fun getCoversDirSize(): Long {
        return try {
            coversDir.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
        } catch (e: Exception) {
            Log.e(TAG, "计算目录大小时出错", e)
            0L
        }
    }
    
    /**
     * 压缩Bitmap
     */
    private fun compressBitmap(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height
        
        // 如果图片已经足够小，直接返回
        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
            return original
        }
        
        // 计算缩放比例
        val scaleX = MAX_WIDTH.toFloat() / width
        val scaleY = MAX_HEIGHT.toFloat() / height
        val scale = minOf(scaleX, scaleY)
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
    }
    
    /**
     * 将Bitmap保存到文件
     */
    private fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream)
                outputStream.flush()
                true
            }
        } catch (e: IOException) {
            Log.e(TAG, "保存Bitmap到文件失败", e)
            false
        }
    }
    
    /**
     * 生成唯一的文件名
     */
    private fun generateFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val random = (1000..9999).random()
        return "cover_${timestamp}_$random.jpg"
    }
    
    /**
     * 检查文件是否为有效的图片文件
     */
    fun isValidImageFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) return false
            
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, options)
            
            // 检查是否能解码出尺寸信息
            options.outWidth > 0 && options.outHeight > 0
        } catch (e: Exception) {
            false
        }
    }
}