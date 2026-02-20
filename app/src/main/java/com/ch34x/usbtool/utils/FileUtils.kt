package com.ch34x.usbtool.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.*

/**
 * 文件工具类
 */
object FileUtils {
    
    /**
     * 从URI读取字节数组
     */
    fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 将字节数组写入URI
     */
    fun writeBytesToUri(context: Context, uri: Uri, data: ByteArray): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(data)
                outputStream.flush()
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 读取文本文件
     */
    fun readTextFile(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 写入文本文件
     */
    fun writeTextFile(context: Context, uri: Uri, text: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.bufferedWriter().use { it.write(text) }
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 复制文件
     */
    fun copyFile(context: Context, sourceUri: Uri, destUri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                context.contentResolver.openOutputStream(destUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                    true
                }
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取文件大小
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use {
                it.statSize
            } ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    
    /**
     * 获取文件名
     */
    fun getFileName(context: Context, uri: Uri): String {
        var fileName = uri.path?.substringAfterLast('/') ?: "unknown"
        
        // 尝试从content resolver获取文件名
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex("_display_name")
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex) ?: fileName
                }
            }
        }
        
        return fileName
    }
    
    /**
     * 获取文件扩展名
     */
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }
    
    /**
     * 检查文件是否为二进制文件
     */
    fun isBinaryFile(data: ByteArray): Boolean {
        val sampleSize = minOf(512, data.size)
        var textCharCount = 0
        
        for (i in 0 until sampleSize) {
            val b = data[i].toInt() and 0xFF
            if (b in 32..126 || b == 9 || b == 10 || b == 13) {
                textCharCount++
            }
        }
        
        return textCharCount < sampleSize * 0.7
    }
    
    /**
     * 获取文件MIME类型
     */
    fun getMimeType(fileName: String): String {
        return when (getFileExtension(fileName)) {
            "bin" -> "application/octet-stream"
            "hex" -> "text/plain"
            "txt" -> "text/plain"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "html", "htm" -> "text/html"
            "csv" -> "text/csv"
            else -> "application/octet-stream"
        }
    }
    
    /**
     * 分割文件
     */
    fun splitFile(data: ByteArray, chunkSize: Int): List<ByteArray> {
        val chunks = mutableListOf<ByteArray>()
        var offset = 0
        
        while (offset < data.size) {
            val size = minOf(chunkSize, data.size - offset)
            chunks.add(data.copyOfRange(offset, offset + size))
            offset += size
        }
        
        return chunks
    }
    
    /**
     * 合并文件
     */
    fun mergeFiles(chunks: List<ByteArray>): ByteArray {
        val totalSize = chunks.sumOf { it.size }
        val result = ByteArray(totalSize)
        var offset = 0
        
        for (chunk in chunks) {
            System.arraycopy(chunk, 0, result, offset, chunk.size)
            offset += chunk.size
        }
        
        return result
    }
    
    /**
     * 创建临时文件
     */
    fun createTempFile(context: Context, prefix: String, suffix: String): File {
        val tempDir = File(context.cacheDir, "temp")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        return File.createTempFile(prefix, suffix, tempDir)
    }
    
    /**
     * 清理临时文件
     */
    fun cleanTempFiles(context: Context) {
        try {
            val tempDir = File(context.cacheDir, "temp")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 获取可读的文件大小
     */
    fun formatFileSize(size: Long): String {
        return when {
            size >= 1024 * 1024 * 1024 -> String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0))
            size >= 1024 * 1024 -> String.format("%.2f MB", size / (1024.0 * 1024.0))
            size >= 1024 -> String.format("%.2f KB", size / 1024.0)
            else -> "$size B"
        }
    }
    
    /**
     * 安全的文件名
     */
    fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9.-]"), "_")
    }
}