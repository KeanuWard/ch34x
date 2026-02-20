package com.ch34x.usbtool.utils

import java.util.Locale

/**
 * 十六进制工具类
 */
object HexUtils {
    
    /**
     * 将字节数组转换为十六进制字符串
     */
    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }
    
    /**
     * 将字节数组转换为带空格的十六进制字符串
     */
    fun bytesToHexWithSpace(bytes: ByteArray): String {
        return bytes.joinToString(" ") { "%02X".format(it) }
    }
    
    /**
     * 将十六进制字符串转换为字节数组
     */
    fun hexToBytes(hex: String): ByteArray {
        val cleanHex = hex.replace("\\s".toRegex(), "").uppercase(Locale.US)
        require(cleanHex.length % 2 == 0) { "Hex string must have even length" }
        
        return ByteArray(cleanHex.length / 2).apply {
            for (i in indices) {
                val index = i * 2
                val byte = cleanHex.substring(index, index + 2).toInt(16)
                this[i] = byte.toByte()
            }
        }
    }
    
    /**
     * 格式化十六进制显示（每行16字节）
     */
    fun formatHexDump(data: ByteArray, address: Long = 0): String {
        val sb = StringBuilder()
        val lineLength = 16
        
        for (i in data.indices step lineLength) {
            // 地址
            sb.append(String.format("%08X: ", address + i))
            
            // 十六进制
            val end = minOf(i + lineLength, data.size)
            for (j in i until end) {
                sb.append(String.format("%02X ", data[j]))
            }
            
            // 对齐
            for (j in end until i + lineLength) {
                sb.append("   ")
            }
            
            sb.append(" ")
            
            // ASCII
            for (j in i until end) {
                val c = data[j].toInt()
                sb.append(if (c in 32..126) data[j].toChar() else '.')
            }
            
            sb.append("\n")
        }
        
        return sb.toString()
    }
    
    /**
     * 检查字符串是否为有效的十六进制
     */
    fun isValidHex(hex: String): Boolean {
        val cleanHex = hex.replace("\\s".toRegex(), "")
        return cleanHex.matches(Regex("^[0-9A-Fa-f]+$")) && cleanHex.length % 2 == 0
    }
    
    /**
     * 将整数转换为十六进制字符串
     */
    fun intToHex(value: Int, length: Int = 8): String {
        return String.format("%0${length}X", value)
    }
    
    /**
     * 将长整数转换为十六进制字符串
     */
    fun longToHex(value: Long, length: Int = 16): String {
        return String.format("%0${length}X", value)
    }
    
    /**
     * 解析十六进制地址字符串
     */
    fun parseAddress(hexStr: String): Long {
        val cleanHex = hexStr.replace("0x", "").replace("0X", "").replace("\\s".toRegex(), "")
        return cleanHex.toLongOrNull(16) ?: 0
    }
    
    /**
     * 计算CRC16
     */
    fun crc16(data: ByteArray): Int {
        var crc = 0xFFFF
        
        for (byte in data) {
            crc = crc xor (byte.toInt() and 0xFF)
            for (i in 0 until 8) {
                if ((crc and 0x0001) != 0) {
                    crc = (crc ushr 1) xor 0x8408
                } else {
                    crc = crc ushr 1
                }
            }
        }
        
        return crc.inv() and 0xFFFF
    }
    
    /**
     * 计算CRC32
     */
    fun crc32(data: ByteArray): Long {
        var crc = 0xFFFFFFFFL
        
        for (byte in data) {
            crc = crc xor (byte.toLong() and 0xFF)
            for (i in 0 until 8) {
                crc = if ((crc and 1L) != 0L) {
                    (crc ushr 1) xor 0xEDB88320L
                } else {
                    crc ushr 1
                }
            }
        }
        
        return crc.inv() and 0xFFFFFFFFL
    }
    
    /**
     * 字节数组转整数（小端序）
     */
    fun bytesToIntLe(bytes: ByteArray): Int {
        require(bytes.size <= 4) { "Byte array too large" }
        var result = 0
        for (i in bytes.indices) {
            result = result or ((bytes[i].toInt() and 0xFF) shl (i * 8))
        }
        return result
    }
    
    /**
     * 整数转字节数组（小端序）
     */
    fun intToBytesLe(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value ushr 8) and 0xFF).toByte(),
            ((value ushr 16) and 0xFF).toByte(),
            ((value ushr 24) and 0xFF).toByte()
        )
    }
    
    /**
     * 字节数组转整数（大端序）
     */
    fun bytesToIntBe(bytes: ByteArray): Int {
        require(bytes.size <= 4) { "Byte array too large" }
        var result = 0
        for (i in bytes.indices) {
            result = (result shl 8) or (bytes[i].toInt() and 0xFF)
        }
        return result
    }
    
    /**
     * 整数转字节数组（大端序）
     */
    fun intToBytesBe(value: Int): ByteArray {
        return byteArrayOf(
            ((value ushr 24) and 0xFF).toByte(),
            ((value ushr 16) and 0xFF).toByte(),
            ((value ushr 8) and 0xFF).toByte(),
            (value and 0xFF).toByte()
        )
    }
}