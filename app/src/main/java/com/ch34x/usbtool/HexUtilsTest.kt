package com.ch34x.usbtool

import com.ch34x.usbtool.utils.HexUtils
import org.junit.Assert.*
import org.junit.Test

/**
 * HexUtils 单元测试类
 */
class HexUtilsTest {
    
    @Test
    fun testBytesToHex() {
        val bytes = byteArrayOf(0x12, 0x34, 0xAB.toByte(), 0xCD.toByte())
        val hex = HexUtils.bytesToHex(bytes)
        assertEquals("1234ABCD", hex)
    }
    
    @Test
    fun testBytesToHexWithSpace() {
        val bytes = byteArrayOf(0x12, 0x34, 0xAB.toByte(), 0xCD.toByte())
        val hex = HexUtils.bytesToHexWithSpace(bytes)
        assertEquals("12 34 AB CD", hex)
    }
    
    @Test
    fun testHexToBytes() {
        val hex = "1234ABCD"
        val bytes = HexUtils.hexToBytes(hex)
        assertArrayEquals(byteArrayOf(0x12, 0x34, 0xAB.toByte(), 0xCD.toByte()), bytes)
    }
    
    @Test
    fun testHexToBytesWithSpaces() {
        val hex = "12 34 AB CD"
        val bytes = HexUtils.hexToBytes(hex)
        assertArrayEquals(byteArrayOf(0x12, 0x34, 0xAB.toByte(), 0xCD.toByte()), bytes)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testHexToBytesInvalidLength() {
        val hex = "123"
        HexUtils.hexToBytes(hex)
    }
    
    @Test
    fun testFormatHexDump() {
        val data = ByteArray(32) { it.toByte() }
        val dump = HexUtils.formatHexDump(data)
        
        assertTrue(dump.contains("00000000:"))
        assertTrue(dump.contains("00000010:"))
        assertTrue(dump.contains("00 01 02 03"))
    }
    
    @Test
    fun testIsValidHex() {
        assertTrue(HexUtils.isValidHex("1234ABCD"))
        assertTrue(HexUtils.isValidHex("12 34 AB CD"))
        assertFalse(HexUtils.isValidHex("123G"))
        assertFalse(HexUtils.isValidHex("123"))
    }
    
    @Test
    fun testIntToHex() {
        assertEquals("000000FF", HexUtils.intToHex(255))
        assertEquals("FFFF", HexUtils.intToHex(65535, 4))
    }
    
    @Test
    fun testLongToHex() {
        assertEquals("00000000FFFFFFFF", HexUtils.longToHex(4294967295))
    }
    
    @Test
    fun testParseAddress() {
        assertEquals(0x1234, HexUtils.parseAddress("0x1234"))
        assertEquals(0x5678, HexUtils.parseAddress("5678"))
        assertEquals(0x9ABC, HexUtils.parseAddress("0x9ABC"))
        assertEquals(0, HexUtils.parseAddress("invalid"))
    }
    
    @Test
    fun testCrc16() {
        val data = "Hello".toByteArray()
        val crc = HexUtils.crc16(data)
        assertEquals(0x92B7, crc)
    }
    
    @Test
    fun testCrc32() {
        val data = "Hello".toByteArray()
        val crc = HexUtils.crc32(data)
        assertEquals(0x3B4B8F8A, crc)
    }
    
    @Test
    fun testBytesToIntLe() {
        val bytes = byteArrayOf(0x78, 0x56, 0x34, 0x12)
        assertEquals(0x12345678, HexUtils.bytesToIntLe(bytes))
    }
    
    @Test
    fun testIntToBytesLe() {
        val bytes = HexUtils.intToBytesLe(0x12345678)
        assertArrayEquals(byteArrayOf(0x78, 0x56, 0x34, 0x12), bytes)
    }
    
    @Test
    fun testBytesToIntBe() {
        val bytes = byteArrayOf(0x12, 0x34, 0x56, 0x78)
        assertEquals(0x12345678, HexUtils.bytesToIntBe(bytes))
    }
    
    @Test
    fun testIntToBytesBe() {
        val bytes = HexUtils.intToBytesBe(0x12345678)
        assertArrayEquals(byteArrayOf(0x12, 0x34, 0x56, 0x78), bytes)
    }
    
    @Test
    fun testBytesToIntLeShort() {
        val bytes = byteArrayOf(0x34, 0x12)
        assertEquals(0x1234, HexUtils.bytesToIntLe(bytes))
    }
    
    @Test
    fun testBytesToIntBeShort() {
        val bytes = byteArrayOf(0x12, 0x34)
        assertEquals(0x1234, HexUtils.bytesToIntBe(bytes))
    }
    
    @Test
    fun testEmptyArray() {
        val empty = byteArrayOf()
        assertEquals("", HexUtils.bytesToHex(empty))
        assertEquals("", HexUtils.bytesToHexWithSpace(empty))
        assertTrue(HexUtils.formatHexDump(empty).isEmpty())
    }
    
    @Test
    fun testAllPossibleValues() {
        val bytes = ByteArray(256) { it.toByte() }
        val hex = HexUtils.bytesToHex(bytes)
        assertEquals(512, hex.length)
        
        for (i in 0 until 256) {
            assertTrue(hex.contains(String.format("%02X", i)))
        }
    }
    
    @Test
    fun testBoundaryValues() {
        // 测试边界值
        assertEquals("00", HexUtils.bytesToHex(byteArrayOf(0)))
        assertEquals("FF", HexUtils.bytesToHex(byteArrayOf(0xFF.toByte())))
        assertEquals("7F", HexUtils.bytesToHex(byteArrayOf(0x7F)))
        assertEquals("80", HexUtils.bytesToHex(byteArrayOf(0x80.toByte())))
    }
    
    @Test
    fun testHexCaseInsensitivity() {
        val hexLower = "abcd1234"
        val hexUpper = "ABCD1234"
        
        val bytesLower = HexUtils.hexToBytes(hexLower)
        val bytesUpper = HexUtils.hexToBytes(hexUpper)
        
        assertArrayEquals(bytesLower, bytesUpper)
    }
}