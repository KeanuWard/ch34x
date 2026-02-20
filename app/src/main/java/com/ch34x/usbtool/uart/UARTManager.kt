package com.ch34x.usbtool.uart

import com.ch34x.usbtool.driver.CH34XDriver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * UART通信管理器
 */
class UARTManager(private val driver: CH34XDriver) {
    
    private val _receivedData = MutableStateFlow<ByteArray?>(null)
    val receivedData: StateFlow<ByteArray?> = _receivedData.asStateFlow()
    
    private val _isLogging = MutableStateFlow(false)
    val isLogging: StateFlow<Boolean> = _isLogging.asStateFlow()
    
    private val _baudRate = MutableStateFlow(115200)
    val baudRate: StateFlow<Int> = _baudRate.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private var logFile: File? = null
    private var logStream: FileOutputStream? = null
    private val writeQueue = LinkedBlockingQueue<ByteArray>()
    private val isRunning = AtomicBoolean(false)
    private val hexMode = AtomicBoolean(false)
    
    data class UARTConfig(
        val baudRate: Int,
        val dataBits: Int,
        val stopBits: Int,
        val parity: Parity,
        val flowControl: FlowControl
    )
    
    enum class Parity {
        NONE, ODD, EVEN, MARK, SPACE
    }
    
    enum class FlowControl {
        NONE, HARDWARE, SOFTWARE
    }
    
    init {
        driver.setDataListener(object : CH34XDriver.OnDataReceivedListener {
            override fun onDataReceived(data: ByteArray) {
                handleReceivedData(data)
            }
        })
        startWriteThread()
    }
    
    /**
     * 配置UART
     */
    fun configure(config: UARTConfig): Boolean {
        return driver.configureUART(
            baudRate = config.baudRate,
            dataBits = config.dataBits,
            stopBits = config.stopBits,
            parity = when (config.parity) {
                Parity.NONE -> 0
                Parity.ODD -> 1
                Parity.EVEN -> 2
                Parity.MARK -> 3
                Parity.SPACE -> 4
            },
            flowControl = when (config.flowControl) {
                FlowControl.NONE -> 0
                FlowControl.HARDWARE -> 1
                FlowControl.SOFTWARE -> 2
            }
        ).also {
            if (it) {
                _baudRate.value = config.baudRate
                _isConnected.value = true
            }
        }
    }
    
    /**
     * 发送数据
     */
    fun sendData(data: ByteArray): Int {
        return if (_isConnected.value) {
            writeQueue.offer(data)
            data.size
        } else 0
    }
    
    /**
     * 发送文本
     */
    fun sendText(text: String): Int {
        return sendData(text.toByteArray())
    }
    
    /**
     * 设置显示模式
     */
    fun setHexMode(enable: Boolean) {
        hexMode.set(enable)
    }
    
    /**
     * 开始日志记录
     */
    fun startLogging(directory: File) {
        try {
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            logFile = File(directory, "uart_log_$timestamp.txt")
            logStream = FileOutputStream(logFile)
            
            // 写入文件头
            val header = "CH34X UART Log - Started at ${Date()}\n" +
                        "========================================\n\n"
            logStream?.write(header.toByteArray())
            
            _isLogging.value = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 停止日志记录
     */
    fun stopLogging() {
        try {
            logStream?.flush()
            logStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            logStream = null
            _isLogging.value = false
        }
    }
    
    /**
     * 记录数据
     */
    private fun logData(data: ByteArray, isTx: Boolean) {
        try {
            logStream?.let { stream ->
                val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
                val direction = if (isTx) "TX ->" else "RX <-"
                
                // HEX格式
                val hexString = data.joinToString(" ") { "%02X".format(it) }
                
                // ASCII格式（只显示可打印字符）
                val asciiString = data.map { 
                    if (it.toInt() in 32..126) it.toChar() else '.' 
                }.joinToString("")
                
                val logLine = String.format(
                    Locale.US,
                    "[%s] %s [%d bytes]\nHEX: %s\nASC: %s\n\n",
                    timestamp, direction, data.size, hexString, asciiString
                )
                
                stream.write(logLine.toByteArray())
                stream.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 处理接收到的数据
     */
    private fun handleReceivedData(data: ByteArray) {
        _receivedData.value = data
        if (_isLogging.value) {
            logData(data, isTx = false)
        }
    }
    
    /**
     * 启动写入线程
     */
    private fun startWriteThread() {
        isRunning.set(true)
        Thread {
            while (isRunning.get()) {
                try {
                    val data = writeQueue.take()
                    if (_isConnected.value) {
                        driver.writeUART(data)
                        if (_isLogging.value) {
                            logData(data, isTx = true)
                        }
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
    
    /**
     * 停止写入线程
     */
    fun stop() {
        isRunning.set(false)
        writeQueue.clear()
        stopLogging()
        _isConnected.value = false
    }
    
    /**
     * 清空接收缓冲区
     */
    fun clearBuffer() {
        _receivedData.value = null
    }
    
    /**
     * 获取日志文件
     */
    fun getLogFile(): File? = logFile
    
    /**
     * 格式化数据用于显示
     */
    fun formatDataForDisplay(data: ByteArray): String {
        return if (hexMode.get()) {
            data.joinToString(" ") { "%02X".format(it) }
        } else {
            String(data)
        }
    }
}