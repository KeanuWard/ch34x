package com.ch34x.usbtool.spi

import com.ch34x.usbtool.driver.CH34XDriver
import com.ch34x.usbtool.flash.FlashDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

/**
 * SPI Flash烧录引擎
 */
class SPIFlashProgrammer(
    private val driver: CH34XDriver,
    private val flashInfo: FlashDatabase.FlashInfo
) {
    
    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()
    
    private val _status = MutableStateFlow(ProgramStatus.IDLE)
    val status: StateFlow<ProgramStatus> = _status.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _speed = MutableStateFlow(0L)
    val speed: StateFlow<Long> = _speed.asStateFlow()
    
    private val isRunning = AtomicBoolean(false)
    private val bufferSize = 4096
    private val retryCount = 3
    
    enum class ProgramStatus {
        IDLE,
        READING,
        WRITING,
        ERASING,
        VERIFYING,
        COMPLETED,
        ERROR
    }
    
    /**
     * 读取Flash
     */
    suspend fun readFlash(
        address: Long, 
        size: Int, 
        onDataRead: (ByteArray) -> Unit,
        onProgress: (Int) -> Unit = {}
    ): Boolean {
        _status.value = ProgramStatus.READING
        _progress.value = 0
        isRunning.set(true)
        
        return try {
            val pages = (size + flashInfo.pageSize - 1) / flashInfo.pageSize
            var totalBytesRead = 0
            val startTime = System.currentTimeMillis()
            
            for (page in 0 until pages) {
                if (!isRunning.get()) {
                    _status.value = ProgramStatus.IDLE
                    return false
                }
                
                val pageAddress = address + page * flashInfo.pageSize
                val readSize = minOf(flashInfo.pageSize, size - page * flashInfo.pageSize)
                
                var success = false
                var data: ByteArray? = null
                
                for (attempt in 0 until retryCount) {
                    data = readPage(pageAddress, readSize)
                    if (data != null && data.size == readSize) {
                        success = true
                        break
                    }
                }
                
                if (!success || data == null) {
                    _error.value = "读取失败 at 0x${pageAddress.toString(16)}"
                    _status.value = ProgramStatus.ERROR
                    return false
                }
                
                onDataRead(data)
                totalBytesRead += data.size
                
                val progress = ((page + 1) * 100 / pages)
                _progress.value = progress
                onProgress(progress)
                
                // 计算速度
                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime > 0) {
                    _speed.value = (totalBytesRead * 1000 / elapsedTime)
                }
            }
            
            _status.value = ProgramStatus.COMPLETED
            true
        } catch (e: Exception) {
            _error.value = e.message
            _status.value = ProgramStatus.ERROR
            false
        } finally {
            isRunning.set(false)
        }
    }
    
    /**
     * 写入Flash
     */
    suspend fun writeFlash(
        address: Long, 
        data: ByteArray, 
        verify: Boolean = true,
        autoErase: Boolean = true,
        onProgress: (Int) -> Unit = {}
    ): Boolean {
        _status.value = ProgramStatus.WRITING
        _progress.value = 0
        isRunning.set(true)
        
        return try {
            // 自动擦除
            if (autoErase) {
                if (!eraseFlash(address, data.size)) {
                    return false
                }
            }
            
            // 自动对齐处理
            val alignedData = alignData(data, address)
            val pages = (alignedData.size + flashInfo.pageSize - 1) / flashInfo.pageSize
            var totalBytesWritten = 0
            val startTime = System.currentTimeMillis()
            
            for (page in 0 until pages) {
                if (!isRunning.get()) {
                    _status.value = ProgramStatus.IDLE
                    return false
                }
                
                val pageAddress = address + page * flashInfo.pageSize
                val start = page * flashInfo.pageSize
                val end = minOf(start + flashInfo.pageSize, alignedData.size)
                val pageData = alignedData.copyOfRange(start, end)
                
                var success = false
                for (attempt in 0 until retryCount) {
                    if (writePage(pageAddress, pageData)) {
                        success = true
                        break
                    }
                }
                
                if (!success) {
                    _error.value = "写入失败 at 0x${pageAddress.toString(16)}"
                    _status.value = ProgramStatus.ERROR
                    return false
                }
                
                totalBytesWritten += pageData.size
                
                val progress = ((page + 1) * 100 / pages)
                _progress.value = progress
                onProgress(progress)
                
                // 计算速度
                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime > 0) {
                    _speed.value = (totalBytesWritten * 1000 / elapsedTime)
                }
            }
            
            // 校验
            if (verify) {
                if (!verifyFlash(address, data)) {
                    _error.value = "校验失败"
                    _status.value = ProgramStatus.ERROR
                    return false
                }
            }
            
            _status.value = ProgramStatus.COMPLETED
            true
        } catch (e: Exception) {
            _error.value = e.message
            _status.value = ProgramStatus.ERROR
            false
        } finally {
            isRunning.set(false)
        }
    }
    
    /**
     * 擦除Flash
     */
    suspend fun eraseFlash(address: Long, size: Int): Boolean {
        _status.value = ProgramStatus.ERASING
        _progress.value = 0
        isRunning.set(true)
        
        return try {
            val sectors = (size + flashInfo.sectorSize - 1) / flashInfo.sectorSize
            
            for (sector in 0 until sectors) {
                if (!isRunning.get()) {
                    _status.value = ProgramStatus.IDLE
                    return false
                }
                
                val sectorAddress = address + sector * flashInfo.sectorSize
                
                var success = false
                for (attempt in 0 until retryCount) {
                    if (eraseSector(sectorAddress)) {
                        success = true
                        break
                    }
                    waitForReady()
                }
                
                if (!success) {
                    _error.value = "擦除失败 at 0x${sectorAddress.toString(16)}"
                    _status.value = ProgramStatus.ERROR
                    return false
                }
                
                _progress.value = ((sector + 1) * 100 / sectors)
            }
            
            _status.value = ProgramStatus.COMPLETED
            true
        } catch (e: Exception) {
            _error.value = e.message
            _status.value = ProgramStatus.ERROR
            false
        } finally {
            isRunning.set(false)
        }
    }
    
    /**
     * 整片擦除
     */
    suspend fun chipErase(): Boolean {
        _status.value = ProgramStatus.ERASING
        _progress.value = 0
        isRunning.set(true)
        
        return try {
            writeEnable()
            driver.transferSPI(byteArrayOf(flashInfo.instructionSet.chipErase))
            waitForReady()
            
            _progress.value = 100
            _status.value = ProgramStatus.COMPLETED
            true
        } catch (e: Exception) {
            _error.value = e.message
            _status.value = ProgramStatus.ERROR
            false
        } finally {
            isRunning.set(false)
        }
    }
    
    /**
     * 校验Flash
     */
    private suspend fun verifyFlash(address: Long, originalData: ByteArray): Boolean {
        _status.value = ProgramStatus.VERIFYING
        _progress.value = 0
        
        val readData = mutableListOf<Byte>()
        val success = readFlash(address, originalData.size, { data ->
            readData.addAll(data.toList())
        }, { progress ->
            _progress.value = progress
        })
        
        if (success) {
            val originalMd5 = calculateMD5(originalData)
            val readMd5 = calculateMD5(readData.toByteArray())
            return originalMd5.contentEquals(readMd5)
        }
        
        return false
    }
    
    /**
     * 读页面
     */
    private fun readPage(address: Long, size: Int): ByteArray? {
        try {
            val cmd = byteArrayOf(
                flashInfo.instructionSet.readData,
                ((address shr 16) and 0xFF).toByte(),
                ((address shr 8) and 0xFF).toByte(),
                (address and 0xFF).toByte()
            )
            // 发送读取命令，后面跟dummy字节
            val result = driver.transferSPI(cmd + ByteArray(size))
            return result?.drop(4)?.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 写页面
     */
    private fun writePage(address: Long, data: ByteArray): Boolean {
        try {
            // 写使能
            writeEnable()
            
            val cmd = byteArrayOf(
                flashInfo.instructionSet.pageProgram,
                ((address shr 16) and 0xFF).toByte(),
                ((address shr 8) and 0xFF).toByte(),
                (address and 0xFF).toByte()
            )
            
            driver.transferSPI(cmd + data)
            waitForReady()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * 擦除扇区
     */
    private fun eraseSector(address: Long): Boolean {
        try {
            writeEnable()
            
            val cmd = byteArrayOf(
                flashInfo.instructionSet.sectorErase,
                ((address shr 16) and 0xFF).toByte(),
                ((address shr 8) and 0xFF).toByte(),
                (address and 0xFF).toByte()
            )
            
            driver.transferSPI(cmd)
            waitForReady()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * 写使能
     */
    private fun writeEnable() {
        driver.transferSPI(byteArrayOf(flashInfo.instructionSet.writeEnable))
    }
    
    /**
     * 等待Flash就绪
     */
    private fun waitForReady() {
        var timeout = 10000 // 10秒超时
        while (timeout > 0) {
            val status = readStatus()
            if (status.isNotEmpty() && (status[0].toInt() and 0x01) == 0) {
                break
            }
            Thread.sleep(10)
            timeout -= 10
        }
    }
    
    /**
     * 读状态寄存器
     */
    private fun readStatus(): ByteArray {
        return driver.transferSPI(byteArrayOf(flashInfo.instructionSet.readStatus, 0))?.drop(1)?.toByteArray() ?: byteArrayOf()
    }
    
    /**
     * 读ID
     */
    fun readID(): ByteArray? {
        return driver.transferSPI(byteArrayOf(flashInfo.instructionSet.readID, 0, 0, 0))?.drop(1)?.toByteArray()
    }
    
    /**
     * 数据对齐
     */
    private fun alignData(data: ByteArray, address: Long): ByteArray {
        val offset = (address % flashInfo.pageSize).toInt()
        if (offset == 0) return data
        
        val aligned = ByteArray(data.size + offset)
        System.arraycopy(data, 0, aligned, offset, data.size)
        return aligned
    }
    
    /**
     * 计算MD5
     */
    private fun calculateMD5(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("MD5").digest(data)
    }
    
    /**
     * 停止操作
     */
    fun stop() {
        isRunning.set(false)
    }
    
    /**
     * 重置状态
     */
    fun reset() {
        isRunning.set(false)
        _progress.value = 0
        _status.value = ProgramStatus.IDLE
        _error.value = null
        _speed.value = 0
    }
}