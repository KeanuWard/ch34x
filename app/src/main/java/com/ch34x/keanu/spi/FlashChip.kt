package com.ch34x.keanu.spi

/*
FlashChip

Flash数据库中的型号结构
*/

data class FlashChip(

val manufacturer: String,

val model: String,

val jedec: String,

val pageSize: Int,

val sectorSize: Int,

val commands: Map<String, String>

)