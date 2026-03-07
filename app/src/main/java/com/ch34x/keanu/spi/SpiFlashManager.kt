package com.ch34x.keanu.spi

import com.ch34x.keanu.utils.HexUtils

/*
SpiFlashManager

Flash操作逻辑
*/

class SpiFlashManager(

private val spi: SpiProtocol,

private val database: FlashDatabase

) {

var chip: FlashChip? = null

/*
 读取JEDEC ID
 */
fun readJedec(): String {

    val cmd = byteArrayOf(0x9F.toByte())

    val resp = spi.transfer(cmd + ByteArray(3))

    return HexUtils.toHex(resp)

}

/*
 自动识别Flash
 */
fun detect(): FlashChip? {

    val id = readJedec()

    chip = database.findByJedec(id)

    return chip

}

/*
 读取Flash
 */
fun read(addr: Int, len: Int): ByteArray {

    val cmd = byteArrayOf(
        0x03,
        (addr shr 16).toByte(),
        (addr shr 8).toByte(),
        addr.toByte()
    )

    val tx = cmd + ByteArray(len)

    return spi.transfer(tx).copyOfRange(4, 4 + len)

}

/*
 写入Flash
 */
fun write(addr: Int, data: ByteArray) {

    val writeEnable = byteArrayOf(0x06)

    spi.transfer(writeEnable)

    val cmd = byteArrayOf(
        0x02,
        (addr shr 16).toByte(),
        (addr shr 8).toByte(),
        addr.toByte()
    )

    spi.transfer(cmd + data)

}

/*
 扇区擦除
 */
fun eraseSector(addr: Int) {

    val writeEnable = byteArrayOf(0x06)

    spi.transfer(writeEnable)

    val cmd = byteArrayOf(
        0x20,
        (addr shr 16).toByte(),
        (addr shr 8).toByte(),
        addr.toByte()
    )

    spi.transfer(cmd)

}

}