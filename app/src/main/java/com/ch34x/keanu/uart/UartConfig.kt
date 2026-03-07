package com.ch34x.keanu.uart

/*
UART配置对象

保存串口参数
*/

data class UartConfig(

var baudRate: Int = 115200,

var dataBits: Int = 8,

var stopBits: Int = 1,

var parity: Int = 0,

var flowControl: Int = 0

)