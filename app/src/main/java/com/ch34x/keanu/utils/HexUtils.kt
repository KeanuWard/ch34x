package com.ch34x.keanu.utils

/*
HEX工具

字节数组转HEX字符串
*/

object HexUtils {

fun toHex(data: ByteArray): String {

    val sb = StringBuilder()

    for (b in data) {

        sb.append(String.format("%02X ", b))

    }

    return sb.toString()

}

fun fromHex(text: String): ByteArray {

    val clean = text.replace(" ", "")

    val result = ByteArray(clean.length / 2)

    for (i in result.indices) {

        val index = i * 2

        val byte =
            clean.substring(index, index + 2).toInt(16)

        result[i] = byte.toByte()

    }

    return result

}

}