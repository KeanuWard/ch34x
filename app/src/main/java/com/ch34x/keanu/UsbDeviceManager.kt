package com.ch34x.keanu

import android.hardware.usb.*

/*
UsbDeviceManager

USB设备统一管理层

功能：

设备扫描
CH34x识别
返回设备列表
*/

class UsbDeviceManager(
private val usbManager: UsbManager
) {

fun findCH34x(): List<UsbDevice> {

    val result = mutableListOf<UsbDevice>()

    val list = usbManager.deviceList

    for (device in list.values) {

        if (device.vendorId == 0x1A86) {

            result.add(device)

        }

    }

    return result
}

}