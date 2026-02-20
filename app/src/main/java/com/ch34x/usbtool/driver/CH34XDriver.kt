package com.ch34x.usbtool.driver

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * CH34X USB驱动封装类
 * 基于CH34XUartDriver.jar实现
 */
class CH34XDriver {
    private var nativeDriver: Any? = null
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val isConnected = AtomicBoolean(false)
    private val dataBuffer = ByteArray(4096)
    private var dataListener: OnDataReceivedListener? = null
    
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }
    
    enum class DeviceType {
        CH340E,
        CH341B,
        UNKNOWN
    }
    
    data class DeviceInfo(
        val deviceType: DeviceType,
        val vid: Int,
        val pid: Int,
        val manufacturer: String?,
        val product: String?,
        val serialNumber: String?
    )
    
    interface OnDataReceivedListener {
        fun onDataReceived(data: ByteArray)
    }
    
    init {
        // 初始化本地驱动
        try {
            System.loadLibrary("CH34XUartDriver")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }
    
    fun setDataListener(listener: OnDataReceivedListener) {
        this.dataListener = listener
    }
    
    /**
     * 识别连接的设备
     */
    fun identifyDevice(usbManager: UsbManager, usbDevice: UsbDevice): DeviceInfo {
        val vid = usbDevice.vendorId
        val pid = usbDevice.productId
        
        val deviceType = when {
            vid == 0x1A86 && pid == 0x5523 -> DeviceType.CH340E
            vid == 0x1A86 && pid == 0x5512 -> DeviceType.CH341B
            else -> DeviceType.UNKNOWN
        }
        
        return DeviceInfo(
            deviceType = deviceType,
            vid = vid,
            pid = pid,
            manufacturer = usbDevice.manufacturerName,
            product = usbDevice.productName,
            serialNumber = usbDevice.serialNumber
        )
    }
    
    /**
     * 打开设备连接
     */
    suspend fun connect(usbManager: UsbManager, usbDevice: UsbDevice): Boolean {
        _connectionState.value = ConnectionState.CONNECTING
        return try {
            // 调用本地驱动连接方法
            val result = nativeConnect(usbManager, usbDevice)
            if (result) {
                isConnected.set(true)
                _connectionState.value = ConnectionState.CONNECTED
                startDataMonitor()
            } else {
                _connectionState.value = ConnectionState.ERROR
            }
            result
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.ERROR
            false
        }
    }
    
    /**
     * 断开设备连接
     */
    fun disconnect() {
        try {
            nativeDisconnect()
        } finally {
            isConnected.set(false)
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
    
    /**
     * UART配置
     */
    fun configureUART(
        baudRate: Int = 115200,
        dataBits: Int = 8,
        stopBits: Int = 1,
        parity: Int = 0,
        flowControl: Int = 0
    ): Boolean {
        return nativeConfigureUART(baudRate, dataBits, stopBits, parity, flowControl)
    }
    
    /**
     * 写入UART数据
     */
    fun writeUART(data: ByteArray): Int {
        return if (isConnected.get()) {
            nativeWriteUART(data)
        } else 0
    }
    
    /**
     * 读取UART数据
     */
    fun readUART(): ByteArray? {
        if (!isConnected.get()) return null
        val size = nativeReadUART(dataBuffer)
        return if (size > 0) {
            dataBuffer.copyOf(size)
        } else null
    }
    
    /**
     * SPI配置
     */
    fun configureSPI(
        mode: Int = 0,  // SPI模式 0-3
        speed: Int = 1000000,  // 1MHz
        lsbFirst: Boolean = false
    ): Boolean {
        return nativeConfigureSPI(mode, speed, lsbFirst)
    }
    
    /**
     * SPI传输
     */
    fun transferSPI(data: ByteArray): ByteArray? {
        if (!isConnected.get()) return null
        return nativeTransferSPI(data)
    }
    
    /**
     * 获取连接状态
     */
    fun isConnected(): Boolean = isConnected.get()
    
    /**
     * 本地方法声明
     */
    private external fun nativeConnect(usbManager: UsbManager, usbDevice: UsbDevice): Boolean
    private external fun nativeDisconnect()
    private external fun nativeConfigureUART(baudRate: Int, dataBits: Int, stopBits: Int, parity: Int, flowControl: Int): Boolean
    private external fun nativeWriteUART(data: ByteArray): Int
    private external fun nativeReadUART(buffer: ByteArray): Int
    private external fun nativeConfigureSPI(mode: Int, speed: Int, lsbFirst: Boolean): Boolean
    private external fun nativeTransferSPI(data: ByteArray): ByteArray?
    
    /**
     * 启动数据监控
     */
    private fun startDataMonitor() {
        // 启动监控线程
        Thread {
            while (isConnected.get()) {
                try {
                    val data = readUART()
                    data?.let {
                        dataListener?.onDataReceived(it)
                    }
                    Thread.sleep(10)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}