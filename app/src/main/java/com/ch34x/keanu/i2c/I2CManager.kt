package com.ch34x.keanu.i2c

import com.ch34x.keanu.ch34x.CH341Driver

/*
 I2C管理器
 ----------------------------------------
 提供I2C总线操作接口
 */
class I2CManager(private val driver: CH341Driver) {

    fun scan(): List<Int> {
        val found = mutableListOf<Int>()
        for (addr in 0x03..0x77) {
            if (ping(addr)) found.add(addr)
        }
        return found
    }

    fun ping(addr: Int): Boolean {
        val cmd = byteArrayOf(0x02, addr.toByte())
        driver.bulkWrite(cmd)
        val buffer = ByteArray(1)
        val len = driver.bulkRead(buffer)
        return len > 0
    }

    fun read(addr: Int, reg: Int, length: Int): ByteArray {
        val cmd = byteArrayOf(0x03, addr.toByte(), reg.toByte()) + ByteArray(length)
        return driver.bulkTransfer(cmd)
    }

    fun write(addr: Int, reg: Int, data: ByteArray) {
        val cmd = byteArrayOf(0x04, addr.toByte(), reg.toByte()) + data
        driver.bulkTransfer(cmd)
    }

}