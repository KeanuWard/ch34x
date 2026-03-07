package com.ch34x.keanu.gpio

import com.ch34x.keanu.ch34x.CH341Driver

/*
 GPIO管理器
 ------------------------------------------------
 支持读/写/切换
 */
class GPIOManager(private val driver: CH341Driver) {

    private var state: Byte = 0x00

    fun read(): Byte {
        val cmd = byteArrayOf(0x05)
        val resp = driver.bulkTransfer(cmd)
        state = resp.getOrNull(0) ?: state
        return state
    }

    fun write(value: Byte) {
        val cmd = byteArrayOf(0x06, value)
        driver.bulkTransfer(cmd)
        state = value
    }

    fun toggle() {
        state = state.inv()
        write(state)
    }
}