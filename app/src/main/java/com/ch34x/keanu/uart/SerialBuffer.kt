package com.ch34x.keanu.uart

import java.util.concurrent.LinkedBlockingQueue

/*
SerialBuffer

串口接收缓冲区
*/

class SerialBuffer {

private val queue = LinkedBlockingQueue<Byte>()

fun push(data: ByteArray) {

    for (b in data) {

        queue.add(b)

    }

}

fun read(max: Int): ByteArray {

    val result = ByteArray(max)

    var i = 0

    while (i < max && queue.isNotEmpty()) {

        result[i++] = queue.poll()

    }

    return result.copyOf(i)

}

}