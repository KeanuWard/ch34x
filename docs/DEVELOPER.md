# CH34X Android USB Tool 开发者指南

## 📋 目录
- [开发环境搭建](#开发环境搭建)
- [项目结构](#项目结构)
- [核心模块](#核心模块)
- [构建系统](#构建系统)
- [测试指南](#测试指南)
- [贡献指南](#贡献指南)
- [发布流程](#发布流程)

## 🛠️ 开发环境搭建

### 必需软件
- **Android Studio Hedgehog** (2023.1.1) 或更高版本
- **JDK 17** 或更高版本
- **Android SDK** 34
- **Gradle 8.0** (项目自带wrapper)

### 环境配置

1. **克隆项目**
```bash
git clone https://github.com/yourusername/ch34x-android-tool.git
cd ch34x-android-tool
添加驱动文件

bash
# 将CH34XUartDriver.jar复制到app/libs目录
cp /path/to/CH34XUartDriver.jar app/libs/
配置local.properties

bash
# 创建local.properties文件
echo "sdk.dir=/path/to/android/sdk" > local.properties
同步项目

bash
# 使用gradle wrapper同步
./gradlew build
📁 项目结构
text
ch34x-android-tool/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ch34x/usbtool/
│   │   │   │   ├── driver/          # USB驱动封装
│   │   │   │   │   └── CH34XDriver.kt
│   │   │   │   ├── spi/             # SPI Flash编程引擎
│   │   │   │   │   └── SPIFlashProgrammer.kt
│   │   │   │   ├── uart/            # UART通信管理
│   │   │   │   │   └── UARTManager.kt
│   │   │   │   ├── flash/           # Flash数据库管理
│   │   │   │   │   └── FlashDatabase.kt
│   │   │   │   ├── utils/           # 工具类
│   │   │   │   │   ├── HexUtils.kt
│   │   │   │   │   └── FileUtils.kt
│   │   │   │   ├── model/           # 数据模型
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── UARTActivity.kt
│   │   │   │   ├── SPIActivity.kt
│   │   │   │   ├── FlashActivity.kt
│   │   │   │   └── CH34XApplication.kt
│   │   │   ├── res/                 # 资源文件
│   │   │   │   ├── layout/          # 布局文件
│   │   │   │   ├── values/          # 值资源
│   │   │   │   ├── drawable/        # 图片资源
│   │   │   │   ├── menu/            # 菜单资源
│   │   │   │   └── xml/             # XML配置
│   │   │   ├── assets/              # 静态资源
│   │   │   │   └── flash_database.json
│   │   │   └── AndroidManifest.xml
│   │   └── test/                     # 单元测试
│   │       └── java/...
│   ├── libs/                         # JAR驱动文件
│   └── build.gradle                  # 模块级构建文件
├── .github/workflows/                 # CI/CD配置
│   └── build.yml
├── docs/                              # 文档
│   ├── API.md
│   ├── USER_GUIDE.md
│   └── DEVELOPER.md
├── gradle/                            # Gradle包装器
├── build.gradle                       # 项目级构建文件
├── settings.gradle                    # 项目设置
├── gradle.properties                   # Gradle属性
├── gradlew                            # Gradle包装器脚本
├── gradlew.bat                        # Windows Gradle包装器
├── LICENSE                            # Apache 2.0许可证
└── README.md                          # 项目说明
🔧 核心模块
1. CH34XDriver
USB驱动封装，处理底层通信。

kotlin
// 初始化驱动
val driver = CH34XDriver()

// 设置数据监听
driver.setDataListener { data ->
    // 处理接收到的数据
}

// 连接设备
lifecycleScope.launch {
    val connected = driver.connect(usbManager, usbDevice)
}

// 配置UART
driver.configureUART(
    baudRate = 115200,
    dataBits = 8,
    stopBits = 1
)

// 发送数据
driver.writeUART("Hello".toByteArray())

// SPI传输
val result = driver.transferSPI(command)
2. UARTManager
UART通信管理，提供高级功能。

kotlin
// 创建UART管理器
val uartManager = UARTManager(driver)

// 配置UART
val config = UARTManager.UARTConfig(
    baudRate = 115200,
    dataBits = 8,
    stopBits = 1,
    parity = UARTManager.Parity.NONE,
    flowControl = UARTManager.FlowControl.NONE
)
uartManager.configure(config)

// 发送数据
uartManager.sendText("AT\r\n")

// 接收数据
lifecycleScope.launch {
    uartManager.receivedData.collect { data ->
        data?.let {
            // 处理接收到的数据
        }
    }
}

// 日志记录
uartManager.startLogging(File(context.filesDir, "logs"))
3. SPIFlashProgrammer
SPI Flash编程引擎。

kotlin
// 创建编程器
val programmer = SPIFlashProgrammer(driver, flashInfo)

// 监控进度
lifecycleScope.launch {
    programmer.progress.collect { progress ->
        // 更新进度条
    }
}

// 读取Flash
programmer.readFlash(
    address = 0x00000000,
    size = 1024 * 1024,
    onDataRead = { data ->
        // 处理读取的数据
    }
) { success ->
    if (success) {
        // 读取成功
    }
}

// 写入Flash
programmer.writeFlash(
    address = 0x00000000,
    data = firmware,
    verify = true
)

// 停止操作
programmer.stop()
4. FlashDatabase
Flash数据库管理。

kotlin
// 初始化数据库
val database = FlashDatabase(context)

// 搜索Flash
val results = database.searchFlash("W25Q")

// 识别Flash
val flash = database.identifyFlash(0xEF.toByte(), 0x40.toByte())

// 导出数据库
val json = database.exportDatabase()

// 导入数据库
database.importDatabase(json)
🏗️ 构建系统
本地构建
bash
# 调试版本
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease

# 清理构建
./gradlew clean

# 运行测试
./gradlew test

# 安装到设备
./gradlew installDebug
构建变体
debug: 调试版本，包含日志

release: 发布版本，代码混淆

签名配置
在app/build.gradle中配置签名：

gradle
android {
    signingConfigs {
        release {
            storeFile file("release.keystore")
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias System.getenv("KEY_ALIAS")
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
🧪 测试指南
单元测试
bash
# 运行所有测试
./gradlew test

# 运行特定测试
./gradlew testDebugUnitTest --tests *HexUtilsTest
测试示例
kotlin
@Test
fun testBytesToHex() {
    val bytes = byteArrayOf(0x12, 0x34, 0xAB.toByte())
    val hex = HexUtils.bytesToHex(bytes)
    assertEquals("1234AB", hex)
}

@Test
fun testCrc16() {
    val data = "Test".toByteArray()
    val crc = HexUtils.crc16(data)
    assertEquals(0x1D0F, crc)
}
集成测试
bash
# 安装并运行测试
./gradlew connectedAndroidTest
📦 发布流程
1. 版本更新
更新版本号在app/build.gradle：

gradle
defaultConfig {
    versionCode 2
    versionName "1.0.1"
}
2. 生成发布包
bash
# 清理并构建
./gradlew clean
./gradlew assembleRelease

# 生成发布说明
git log --pretty=format:"- %s" $(git describe --tags --abbrev=0)..HEAD > release_notes.txt
3. GitHub Release
创建新标签

bash
git tag -a v1.0.1 -m "Version 1.0.1"
git push origin v1.0.1
GitHub Actions自动构建并创建Release

🤝 贡献指南
分支策略
main: 稳定版本

develop: 开发分支

feature/xxx: 功能分支

bugfix/xxx: 修复分支

代码规范
Kotlin编码规范

遵循官方Kotlin编码规范

使用4空格缩进

最大行长度120字符

命名规范

类名: PascalCase

函数/变量: camelCase

常量: UPPER_CASE

资源ID: snake_case

注释规范

公共API使用KDoc

复杂逻辑添加注释

更新文档同步

提交规范
text
<type>(<scope>): <subject>

<body>

<footer>
类型(type):

feat: 新功能

fix: 修复

docs: 文档

style: 格式

refactor: 重构

test: 测试

chore: 构建/工具

示例:

text
feat(uart): 添加硬件流控支持

- 实现RTS/CTS流控
- 添加流控配置界面
- 更新相关文档

Closes #123
Pull Request流程
Fork项目

创建功能分支

提交更改

运行测试

创建PR

代码审查

合并

📈 性能优化
内存优化
使用对象池

避免大对象分配

及时释放资源

线程管理
使用协程处理并发

避免主线程阻塞

合理配置线程池

缓冲区优化
动态缓冲区大小

批量数据处理

零拷贝传输

🔒 安全建议
数据安全
敏感数据加密

清理内存中的敏感信息

安全的文件权限

USB安全
验证设备VID/PID

限制设备访问权限

防止缓冲区溢出

📚 扩展开发
添加新的设备支持
在DeviceType枚举中添加新类型

更新identifyDevice方法

添加设备特定配置

更新文档

添加新的Flash芯片
在flash_database.json中添加条目

测试读写兼容性

更新数据库

自定义协议
kotlin
class CustomProtocolHandler(private val driver: CH34XDriver) {
    fun sendCommand(cmd: ByteArray): ByteArray? {
        // 实现自定义协议
    }
}
📝 调试技巧
启用日志
kotlin
// 在Application中
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}

// 使用日志
Timber.d("Data received: ${data.size} bytes")
Timber.e(error, "Operation failed")
调试USB
bash
# 查看USB设备
adb shell lsusb

# 抓取USB日志
adb logcat -s UsbManager
性能分析
bash
# CPU分析
adb shell top -n 1 | grep package.name

# 内存分析
adb shell dumpsys meminfo package.name
🚀 CI/CD配置
GitHub Actions
yaml
name: Build
on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - run: ./gradlew build
环境变量
在GitHub Secrets中配置：

KEYSTORE_PASSWORD

KEY_ALIAS

KEY_PASSWORD

📄 文档生成
KDoc生成
bash
# 生成KDoc文档
./gradlew dokka
API文档
文档位于docs/api/目录，使用Markdown格式。

版本: 1.0.0
最后更新: 2024-01-15
联系方式: GitHub Issues