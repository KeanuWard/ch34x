package com.ch34x.keanu.ch34x

import android.hardware.usb.*

/*
CH341Driver

CH341工业驱动

支持：

UART
SPI
I2C
EEPROM
GPIO
*/

class CH341Driver(
device: UsbDevice,
connection: UsbDeviceConnection
) : CH34xDriver(device, connection) {

override fun open() {

    super.open()

    initChip()

}

/*
 初始化CH341
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