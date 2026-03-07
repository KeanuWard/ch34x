package com.ch34x.keanu.ch34x

import android.hardware.usb.*

/*
CH340Driver

CH340 芯片驱动

功能：

UART
GPIO
EEPROM
*/

class CH340Driver(
device: UsbDevice,
connection: UsbDeviceConnection
) : CH34xDriver(device, connection) {

override fun open() {

    super.open()

    initChip()

}

/*
 初始化芯片
 */
private fun initChip() {

    val cmd = byteArrayOf(
        0xA1.toByte(),
        0x00,
        0x00
    )

    bulkWrite(cmd)

}

}