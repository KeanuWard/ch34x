package com.ch34x.keanu.spi

/*
SPI配置对象

保存SPI控制参数
*/

data class SpiConfig(

var frequency: Int = 400000,

var mode: Int = 0,

var lsbFirst: Boolean = false,

var chipSelect: Boolean = true,

var testMode: Boolean = false

)