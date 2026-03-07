package com.ch34x.keanu.eeprom

import com.ch34x.keanu.ch34x.CH341Driver

/*
 EEPROM管理器
 ------------------------------------------------
 支持读取/写入BIN
 */
class EEPROMManager(private val driver: CH341Driver) {

    fun read(addr: Int, length: Int): ByteArray {
        val cmd = byteArrayOf(0x07, (addr shr 8).toByte(), addr.toByte()) + ByteArray(length)
        return driver.bulkTransfer(cmd).copyOfRange(3, 3 + length)
    }

    fun write(addr: Int, data: ByteArray) {
        val cmd = byteArrayOf(0x08, (addr shr 8).toByte(), addr.toByte()) + data
        driver.bulkTransfer(cmd)
    }
}