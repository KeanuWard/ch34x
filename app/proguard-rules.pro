CH34x 工具应用 ProGuard 配置

保留 USB 类

-keep class android.hardware.usb.** { *; }

保留 Kotlin 反射

-keep class kotlin.Metadata { *; }

保留所有 Driver

-keep class com.ch34x.keanu.** { *; }