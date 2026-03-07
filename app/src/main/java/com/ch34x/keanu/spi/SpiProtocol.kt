package com.ch34x.keanu.spi

import com.ch34x.keanu.ch34x.CH341Driver

/*
SpiProtocol

CH341 SPI底层协议
*/

class SpiProtocol(
private val driver: CH341Driver
) {

/*
 SPI传输
 */
fun transfer(tx: ByteArray): ByteArray {

    driver.bulkWrite(tx)

    val buffer = ByteArray(tx.size)

    val len = driver.bulkRead(buffer)

    return buffer.copyOf(len)

}

}