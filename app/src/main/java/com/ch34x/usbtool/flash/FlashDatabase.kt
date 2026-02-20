package com.ch34x.usbtool.flash

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

/**
 * Flash芯片信息数据库
 */
class FlashDatabase(private val context: Context) {
    
    private val gson = Gson()
    private var flashList: MutableList<FlashInfo> = mutableListOf()
    
    data class FlashInfo(
        val name: String,
        val manufacturer: String,
        val capacity: Long,  // 字节
        val pageSize: Int,
        val sectorSize: Int,
        val blockSize: Int,
        val instructionSet: InstructionSet,
        val voltageMin: Double,
        val voltageMax: Double,
        val maxSpeed: Int,
        val supportedModes: List<SpiMode>
    )
    
    enum class SpiMode {
        MODE_0, MODE_1, MODE_2, MODE_3
    }
    
    data class InstructionSet(
        val readID: Byte,
        val readData: Byte,
        val fastRead: Byte,
        val pageProgram: Byte,
        val sectorErase: Byte,
        val blockErase: Byte,
        val chipErase: Byte,
        val writeEnable: Byte,
        val writeDisable: Byte,
        val readStatus: Byte,
        val writeStatus: Byte
    )
    
    init {
        loadDefaultDatabase()
    }
    
    /**
     * 加载默认Flash数据库
     */
    private fun loadDefaultDatabase() {
        try {
            val inputStream = context.assets.open("flash_database.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<FlashInfo>>() {}.type
            val list: List<FlashInfo> = gson.fromJson(json, type)
            flashList.clear()
            flashList.addAll(list)
        } catch (e: Exception) {
            e.printStackTrace()
            // 加载默认数据
            loadDefaultData()
        }
    }
    
    /**
     * 加载默认数据（当JSON文件不存在时）
     */
    private fun loadDefaultData() {
        flashList.add(
            FlashInfo(
                name = "W25Q64JV",
                manufacturer = "Winbond",
                capacity = 8388608,
                pageSize = 256,
                sectorSize = 4096,
                blockSize = 65536,
                instructionSet = InstructionSet(
                    readID = 0x9F,
                    readData = 0x03,
                    fastRead = 0x0B,
                    pageProgram = 0x02,
                    sectorErase = 0x20,
                    blockErase = 0xD8,
                    chipErase = 0xC7,
                    writeEnable = 0x06,
                    writeDisable = 0x04,
                    readStatus = 0x05,
                    writeStatus = 0x01
                ),
                voltageMin = 2.7,
                voltageMax = 3.6,
                maxSpeed = 104000000,
                supportedModes = listOf(SpiMode.MODE_0, SpiMode.MODE_3)
            )
        )
        
        flashList.add(
            FlashInfo(
                name = "MX25L3206E",
                manufacturer = "Macronix",
                capacity = 4194304,
                pageSize = 256,
                sectorSize = 4096,
                blockSize = 65536,
                instructionSet = InstructionSet(
                    readID = 0x9F,
                    readData = 0x03,
                    fastRead = 0x0B,
                    pageProgram = 0x02,
                    sectorErase = 0x20,
                    blockErase = 0xD8,
                    chipErase = 0xC7,
                    writeEnable = 0x06,
                    writeDisable = 0x04,
                    readStatus = 0x05,
                    writeStatus = 0x01
                ),
                voltageMin = 2.7,
                voltageMax = 3.6,
                maxSpeed = 86000000,
                supportedModes = listOf(SpiMode.MODE_0, SpiMode.MODE_3)
            )
        )
    }
    
    /**
     * 根据设备ID识别Flash
     */
    fun identifyFlash(manufacturerId: Byte, deviceId: Byte): FlashInfo? {
        // 这里实现实际的识别逻辑
        // 可以根据制造商ID和设备ID匹配数据库
        return flashList.firstOrNull { flash ->
            // 简化的匹配逻辑，实际需要根据具体的ID映射
            flash.manufacturer.contains(manufacturerId.toString(16))
        }
    }
    
    /**
     * 根据名称搜索Flash
     */
    fun searchFlash(keyword: String): List<FlashInfo> {
        return flashList.filter { 
            it.name.contains(keyword, ignoreCase = true) ||
            it.manufacturer.contains(keyword, ignoreCase = true)
        }
    }
    
    /**
     * 导出数据库
     */
    fun exportDatabase(): String {
        return gson.toJson(flashList)
    }
    
    /**
     * 导入数据库
     */
    fun importDatabase(json: String): Boolean {
        return try {
            val type = object : TypeToken<List<FlashInfo>>() {}.type
            val list: List<FlashInfo> = gson.fromJson(json, type)
            flashList.clear()
            flashList.addAll(list)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取所有Flash列表
     */
    fun getAllFlash(): List<FlashInfo> = flashList.toList()
    
    /**
     * 添加Flash信息
     */
    fun addFlash(flashInfo: FlashInfo) {
        flashList.add(flashInfo)
    }
    
    /**
     * 删除Flash信息
     */
    fun removeFlash(name: String): Boolean {
        return flashList.removeIf { it.name == name }
    }
    
    /**
     * 更新Flash信息
     */
    fun updateFlash(oldName: String, newInfo: FlashInfo): Boolean {
        val index = flashList.indexOfFirst { it.name == oldName }
        return if (index >= 0) {
            flashList[index] = newInfo
            true
        } else {
            false
        }
    }
}