package com.ch34x.keanu.uart

import com.ch34x.keanu.ch34x.CH34xDriver
import kotlin.concurrent.thread

/*
UartManager

串口控制层
*/

class UartManager(
private val driver: CH34xDriver
) {

private var running = false

private val buffer = SerialBuffer()

var rxBytes: Long = 0
var txBytes: Long = 0

fun start(onReceive: (ByteArray) -> Unit) {

    running = true

    thread {

        val buf = ByteArray(512)

        while (running) {

            val len = driver.bulkRead(buf)

            if (len > 0) {

                val data = buf.copyOf(len)

                rxBytes += len

                onReceive(data)

            }

        }

    }

}

fun stop() {

    running = false

}

fun send(data: ByteArray) {

    driver.bulkWrite(data)

    txBytes += data.size

}

}