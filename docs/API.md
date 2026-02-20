# CH34X Android USB Tool API 文档

## 目录
- [CH34XDriver](#ch34xdriver)
- [UARTManager](#uartmanager)
- [SPIFlashProgrammer](#spiflashprogrammer)
- [FlashDatabase](#flashdatabase)
- [工具类](#工具类)

## CH34XDriver

USB驱动封装类，处理设备连接和基础通信。

### 枚举类型

#### ConnectionState
```kotlin
enum class ConnectionState {
    DISCONNECTED,  // 未连接
    CONNECTING,    // 连接中
    CONNECTED,     // 已连接
    ERROR         // 错误状态
}

DeviceType
enum class DeviceType {
    CH340E,  // CH340E设备
    CH341B,  // CH341B设备
    UNKNOWN  // 未知设备
}
数据类
DeviceInfo
data class DeviceInfo(
    val deviceType: DeviceType,  // 设备类型
    val vid: Int,                // 供应商ID
    val pid: Int,                // 产品ID
    val manufacturer: String?,   // 制造商
    val product: String?,        // 产品名称
    val serialNumber: String?    // 序列号
)
方法
identifyDevice
识别连接的USB设备。
fun identifyDevice(usbManager: UsbManager, usbDevice: UsbDevice): DeviceInfo
参数:

usbManager: USB管理器实例

usbDevice: USB设备实例

返回: 设备信息

connect
连接到USB设备。
suspend fun connect(usbManager: UsbManager, usbDevice: UsbDevice): Boolean
参数:

usbManager: USB管理器实例

usbDevice: 要连接的设备

返回: 连接成功返回true，失败返回false

disconnect
断开设备连接。fun disconnect()
fun disconnect()
configureUART
配置UART参数。
fun configureUART(
    baudRate: Int = 115200,  // 波特率
    dataBits: Int = 8,       // 数据位
    stopBits: Int = 1,       // 停止位
    parity: Int = 0,         // 校验位 (0:无,1:奇,2:偶,3:标记,4:空格)
    flowControl: Int = 0     // 流控制 (0:无,1:硬件,2:软件)
): Boolean
返回: 配置成功返回true

writeUART
写入UART数据。
fun writeUART(data: ByteArray): Int
参数:

data: 要发送的数据

返回: 实际发送的字节数

readUART
读取UART数据。
fun readUART(): ByteArray?
返回: 接收到的数据，无数据时返回null

configureSPI
配置SPI参数。
fun configureSPI(
    mode: Int = 0,          // SPI模式 0-3
    speed: Int = 1000000,   // 时钟速度 (Hz)
    lsbFirst: Boolean = false // LSB优先
): Boolean
返回: 配置成功返回true

transferSPI
SPI数据传输。
fun transferSPI(data: ByteArray): ByteArray?
参数:

data: 要发送的数据

返回: 接收到的数据

UARTManager
UART通信管理器，提供高级UART功能。

枚举类型
Parity
enum class Parity {
    NONE,  // 无校验
    ODD,   // 奇校验
    EVEN,  // 偶校验
    MARK,  // 标记校验
    SPACE  // 空格校验
}
FlowControl
enum class FlowControl {
    NONE,      // 无流控
    HARDWARE,  // 硬件流控
    SOFTWARE   // 软件流控
}
数据类
UARTConfig
data class UARTConfig(
    val baudRate: Int,        // 波特率
    val dataBits: Int,        // 数据位
    val stopBits: Int,        // 停止位
    val parity: Parity,       // 校验位
    val flowControl: FlowControl // 流控制
)
方法
configure
配置UART参数。
fun configure(config: UARTConfig): Boolean
返回: 配置成功返回true

sendData
发送数据。

kotlin
fun sendData(data: ByteArray): Int
返回: 实际发送的字节数

sendText
发送文本。

kotlin
fun sendText(text: String): Int
返回: 实际发送的字节数

startLogging
开始日志记录。

kotlin
fun startLogging(directory: File)
参数:

directory: 日志保存目录

stopLogging
停止日志记录。

kotlin
fun stopLogging()
SPIFlashProgrammer
SPI Flash烧录引擎。

枚举类型
ProgramStatus
kotlin
enum class ProgramStatus {
    IDLE,       // 空闲
    READING,    // 读取中
    WRITING,    // 写入中
    ERASING,    // 擦除中
    VERIFYING,  // 校验中
    COMPLETED,  // 完成
    ERROR       // 错误
}
方法
readFlash
读取Flash数据。

kotlin
suspend fun readFlash(
    address: Long,                      // 起始地址
    size: Int,                          // 读取长度
    onDataRead: (ByteArray) -> Unit,    // 数据回调
    onProgress: (Int) -> Unit = {}      // 进度回调
): Boolean
返回: 操作成功返回true

writeFlash
写入Flash数据。

kotlin
suspend fun writeFlash(
    address: Long,                       // 起始地址
    data: ByteArray,                     // 要写入的数据
    verify: Boolean = true,               // 是否校验
    autoErase: Boolean = true,            // 是否自动擦除
    onProgress: (Int) -> Unit = {}        // 进度回调
): Boolean
返回: 操作成功返回true

eraseFlash
擦除Flash区域。

kotlin
suspend fun eraseFlash(address: Long, size: Int): Boolean
返回: 操作成功返回true

chipErase
整片擦除。

kotlin
suspend fun chipErase(): Boolean
返回: 操作成功返回true

readID
读取Flash ID。

kotlin
fun readID(): ByteArray?
返回: Flash ID数据

stop
停止当前操作。

kotlin
fun stop()
FlashDatabase
Flash芯片信息数据库管理。

数据类
FlashInfo
kotlin
data class FlashInfo(
    val name: String,                    // 芯片名称
    val manufacturer: String,             // 制造商
    val capacity: Long,                   // 容量(字节)
    val pageSize: Int,                    // 页大小
    val sectorSize: Int,                  // 扇区大小
    val blockSize: Int,                   // 块大小
    val instructionSet: InstructionSet,   // 指令集
    val voltageMin: Double,                // 最小电压
    val voltageMax: Double,                // 最大电压
    val maxSpeed: Int,                     // 最大速度
    val supportedModes: List<SpiMode>      // 支持的SPI模式
)
InstructionSet
kotlin
data class InstructionSet(
    val readID: Byte,      // 读ID指令
    val readData: Byte,    // 读数据指令
    val fastRead: Byte,    // 快速读指令
    val pageProgram: Byte, // 页编程指令
    val sectorErase: Byte, // 扇区擦除指令
    val blockErase: Byte,  // 块擦除指令
    val chipErase: Byte,   // 整片擦除指令
    val writeEnable: Byte, // 写使能指令
    val writeDisable: Byte, // 写禁止指令
    val readStatus: Byte,  // 读状态寄存器指令
    val writeStatus: Byte  // 写状态寄存器指令
)
方法
identifyFlash
识别Flash芯片。

kotlin
fun identifyFlash(manufacturerId: Byte, deviceId: Byte): FlashInfo?
searchFlash
搜索Flash芯片。

kotlin
fun searchFlash(keyword: String): List<FlashInfo>
exportDatabase
导出数据库。

kotlin
fun exportDatabase(): String
importDatabase
导入数据库。

kotlin
fun importDatabase(json: String): Boolean
工具类
HexUtils
十六进制工具类。

kotlin
// 字节数组转十六进制字符串
fun bytesToHex(bytes: ByteArray): String

// 十六进制字符串转字节数组
fun hexToBytes(hex: String): ByteArray

// 格式化十六进制转储
fun formatHexDump(data: ByteArray, address: Long = 0): String

// 计算CRC16
fun crc16(data: ByteArray): Int

// 计算CRC32
fun crc32(data: ByteArray): Long
FileUtils
文件工具类。

kotlin
// 从URI读取字节数组
fun readBytesFromUri(context: Context, uri: Uri): ByteArray?

// 写入字节数组到URI
fun writeBytesToUri(context: Context, uri: Uri, data: ByteArray): Boolean

// 获取文件大小
fun getFileSize(context: Context, uri: Uri): Long

// 格式化文件大小
fun formatFileSize(size: Long): String
Flow 状态
CH34XDriver
connectionState: StateFlow<ConnectionState> - 连接状态

UARTManager
receivedData: StateFlow<ByteArray?> - 接收到的数据

isLogging: StateFlow<Boolean> - 日志记录状态

isConnected: StateFlow<Boolean> - 连接状态

SPIFlashProgrammer
progress: StateFlow<Int> - 进度 (0-100)

status: StateFlow<ProgramStatus> - 状态

error: StateFlow<String?> - 错误信息

speed: StateFlow<Long> - 速度 (字节/秒)