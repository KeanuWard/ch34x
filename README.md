# CH34X Android USB Tool

[![Build Status](https://github.com/yourusername/ch34x-android-tool/actions/workflows/build.yml/badge.svg)](https://github.com/yourusername/ch34x-android-tool/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![API](https://img.shields.io/badge/API-29%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=29)

## 📱 项目简介

CH34X Android USB Tool 是一个专业的安卓USB主机模式工具，支持CH340E和CH341B芯片的UART通信和SPI Flash编程。该项目提供完整的工业级功能，包括自动设备识别、错误恢复、数据校验和进度显示。

### ✨ 主要特性

#### 🚀 UART功能
- ✅ 完整的USB主机通讯封装
- ✅ 基于CH34XUartDriver.jar的驱动实现
- ✅ 波特率可配置（1200-3000000）
- ✅ 数据位/停止位/校验位完整支持
- ✅ 硬件流控支持
- ✅ 异步读取线程
- ✅ 写入队列管理
- ✅ 自动断线检测与重连
- ✅ 实时终端流式输出
- ✅ 十六进制/ASCII模式切换
- ✅ 文件发送功能
- ✅ 串口抓包日志记录
- ✅ 工业级缓冲区管理
- ✅ 高速模式支持

#### 💾 SPI Flash编程
- ✅ 完整的SPI指令实现
- ✅ 支持所有常用Flash芯片
- ✅ Flash芯片信息数据库
- ✅ 数据库导入/导出功能
- ✅ 自动设备识别
- ✅ 根据设备选择可用功能
- ✅ 自动Flash识别
- ✅ 指定地址读取
- ✅ 指定长度读取
- ✅ 指定偏移写入
- ✅ CRC校验
- ✅ 自动页对齐处理
- ✅ 自动擦除
- ✅ 自动Busy等待
- ✅ 工业错误恢复机制
- ✅ 自动重试流程
- ✅ 块/页对齐自动处理

#### 🎨 用户界面
- ✅ 真正的专业级GUI
- ✅ 实时进度条显示
- ✅ 状态指示灯
- ✅ 详细的设备信息面板
- ✅ EventBus日志系统
- ✅ 文件操作栈管理

### 📋 系统要求

- Android 10 - 13 (API 29+)
- 支持USB Host模式的设备
- CH340E或CH341B USB设备

### 🛠️ 技术栈

- **语言**: Kotlin
- **最低API**: 29 (Android 10)
- **目标API**: 34 (Android 14)
- **构建工具**: Gradle 8.0
- **CI/CD**: GitHub Actions
- **主要依赖**:
  - AndroidX Core KTX
  - Material Design Components
  - Coroutines
  - GSON
  - SpeedViewLib

## 📦 项目结构
ch34x-android-tool/
├── app/
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/com/ch34x/usbtool/
│ │ │ │ ├── driver/ # USB驱动封装
│ │ │ │ ├── spi/ # SPI Flash编程引擎
│ │ │ │ ├── uart/ # UART通信管理
│ │ │ │ ├── flash/ # Flash数据库管理
│ │ │ │ ├── model/ # 数据模型
│ │ │ │ ├── utils/ # 工具类
│ │ │ │ ├── MainActivity.kt # 主界面
│ │ │ │ ├── UARTActivity.kt # UART界面
│ │ │ │ ├── SPIActivity.kt # SPI界面
│ │ │ │ └── FlashActivity.kt # Flash编程界面
│ │ │ ├── res/ # 资源文件
│ │ │ ├── assets/ # 静态资源
│ │ │ └── AndroidManifest.xml
│ │ └── test/ # 单元测试
│ ├── libs/ # JAR驱动文件
│ └── build.gradle # 模块级构建文件
├── .github/workflows/ # CI/CD配置
├── docs/ # 文档
├── gradle/ # Gradle包装器
├── build.gradle # 项目级构建文件
├── settings.gradle # 项目设置
└── README.md # 项目说明

## 🚀 快速开始

### 前置条件

1. Android Studio Hedgehog (2023.1.1) 或更高版本
2. JDK 17
3. Android SDK 34
4. CH34XUartDriver.jar 驱动文件

### 构建步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/ch34x-android-tool.git
   cd ch34x-android-tool
添加驱动文件

将 CH34XUartDriver.jar 复制到 app/libs/ 目录

本地构建

bash
# 调试版本
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease
GitHub CI构建

推送到GitHub仓库

GitHub Actions自动构建

构建产物在Actions页面下载

📖 使用指南
1. 设备连接
通过OTG线连接CH34X设备

应用自动识别设备类型（CH340E/CH341B）

状态指示灯显示连接状态

2. UART通信
选择UART模式

配置波特率、数据位、停止位、校验位

可选择硬件流控

实时数据显示（HEX/ASCII）

支持日志记录功能

3. SPI Flash编程
选择SPI模式

自动识别Flash芯片

支持读取、写入、擦除操作

自动校验和数据对齐

实时显示进度和速度

4. Flash数据库管理
内置常用Flash芯片数据库

支持数据库导入/导出

可手动添加新芯片

🔧 配置说明
UART配置参数
波特率: 1200 ~ 3000000

数据位: 5, 6, 7, 8

停止位: 1, 1.5, 2

校验位: None, Odd, Even, Mark, Space

流控制: None, Hardware, Software

SPI配置参数
SPI模式: 0, 1, 2, 3

时钟速度: 可配置

数据顺序: MSB/LSB

🤝 贡献指南
欢迎贡献代码！请遵循以下步骤：

Fork 项目

创建特性分支 (git checkout -b feature/AmazingFeature)

提交更改 (git commit -m 'Add some AmazingFeature')

推送到分支 (git push origin feature/AmazingFeature)

开启 Pull Request

📄 许可证
本项目采用 Apache License 2.0 许可证 - 详见 LICENSE 文件

🙏 致谢
CH34XUartDriver.jar 驱动提供者

所有贡献者和测试者

📞 联系方式
项目主页: GitHub

问题反馈: Issues

📊 版本历史
v1.0.0 (2024-01-15)
初始版本发布

支持CH340E/CH341B设备

完整的UART通信功能

基础的SPI Flash编程功能

Flash数据库管理

GitHub CI集成

计划功能
多语言支持

波形分析工具

脚本自动化

远程调试支持