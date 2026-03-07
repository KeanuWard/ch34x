package com.ch34x.keanu.ch34x

import android.hardware.usb.*

/*
CH34xDriver

CH34x驱动抽象类

所有CH34x设备驱动必须继承该类

支持功能：

UART
SPI
I2C
EEPROM
GPIO
*/

abstract class CH34xDriver(

protected val device: UsbDevice,

protected val connection: UsbDeviceConnection

) {

protected var usbInterface: UsbInterface? = null

protected var endpointIn: UsbEndpoint? = null

protected var endpointOut: UsbEndpoint? = null

/*
 打开设备
 */
open fun open() {

    usbInterface = device.getInterface(0)

    connection.claimInterface(usbInterface, true)

    for (i in 0 until usbInterface!!.endpointCount) {

        val ep = usbInterface!!.getEndpoint(i)

        if (ep.direction == UsbConstants.USB_DIR_IN) {

            endpointIn = ep

        } else {

            endpointOut = ep

        }

    }

}

/*
 关闭设备
 */
open fun close() {

    usbInterface?.let {

        connection.releaseInterface(it)

    }

    connection.close()

}

/*
 Bulk发送
 */
protected fun bulkWrite(data: ByteArray): Int {

    return connection.bulkTransfer(
        endpointOut,
        data,
        data.size,
        2000
    )

}

/*
 Bulk读取
 */
protected fun bulkRead(buffer: ByteArray): Int {

    return connection.bulkTransfer(
        endpointIn,
        buffer,
        buffer.size,
        2000
    )

}

}