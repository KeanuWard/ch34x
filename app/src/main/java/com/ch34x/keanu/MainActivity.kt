package com.ch34x.keanu

import android.app.PendingIntent
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ch34x.keanu.ch34x.CH34xDriver
import com.ch34x.keanu.ch34x.CH340Driver
import com.ch34x.keanu.ch34x.CH341Driver
import com.ch34x.keanu.uart.UartFragment
import com.ch34x.keanu.spi.SpiFragment
import com.ch34x.keanu.i2c.I2CFragment
import com.ch34x.keanu.gpio.GPIOFragment
import com.ch34x.keanu.eeprom.EEPROMFragment

class MainActivity : AppCompatActivity() {

    private lateinit var txtLog: TextView
    private lateinit var btnScan: Button
    private lateinit var btnOpen: Button
    private lateinit var btnUart: Button
    private lateinit var btnSpi: Button
    private lateinit var btnI2C: Button
    private lateinit var btnGpio: Button
    private lateinit var btnEEPROM: Button

    private lateinit var usbManager: UsbManager
    private var device: UsbDevice? = null
    private var driver: CH34xDriver? = null

    companion object {
        private const val ACTION_USB_PERMISSION = "com.ch34x.keanu.USB_PERMISSION"
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_USB_PERMISSION) {
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                if (granted) {
                    log("USB权限已授予")
                    initDriver()
                } else {
                    log("USB权限被拒绝")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtLog = findViewById(R.id.txt_log)
        btnScan = findViewById(R.id.btn_scan)
        btnOpen = findViewById(R.id.btn_open)
        btnUart = findViewById(R.id.btn_uart)
        btnSpi = findViewById(R.id.btn_spi)
        btnI2C = findViewById(R.id.btn_i2c)
        btnGpio = findViewById(R.id.btn_gpio)
        btnEEPROM = findViewById(R.id.btn_eeprom)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        // 默认隐藏功能按钮
        setFunctionButtonsVisibility(false)

        btnScan.setOnClickListener { scanUsbDevices() }
        btnOpen.setOnClickListener { requestUsbPermission() }

        // 功能按钮绑定对应Fragment
        btnUart.setOnClickListener { switchFragment(UartFragment()) }
        btnSpi.setOnClickListener { switchFragment(SpiFragment()) }
        btnI2C.setOnClickListener { switchFragment(I2CFragment()) }
        btnGpio.setOnClickListener { switchFragment(GPIOFragment()) }
        btnEEPROM.setOnClickListener { switchFragment(EEPROMFragment()) }

        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    private fun log(msg: String) {
        txtLog.append("$msg\n")
        txtLog.scrollTo(0, txtLog.bottom)
    }

    private fun scanUsbDevices() {
        log("扫描USB设备...")
        val deviceList = usbManager.deviceList
        if (deviceList.isEmpty()) {
            log("未发现USB设备")
            device = null
            return
        }
        device = deviceList.values.first()
        log("发现设备: ${device?.deviceName}, VID=${device?.vendorId}, PID=${device?.productId}")
    }

    private fun requestUsbPermission() {
        if (device == null) {
            log("请先扫描设备")
            return
        }
        if (usbManager.hasPermission(device)) {
            log("USB权限已存在")
            initDriver()
        } else {
            log("请求USB权限...")
            val pi = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE)
            usbManager.requestPermission(device, pi)
        }
    }

    private fun initDriver() {
        if (device == null) return
        driver = when (device?.productId) {
            0x7523 -> CH340Driver(device!!, usbManager)
            0x5511 -> CH341Driver(device!!, usbManager)
            else -> {
                log("未知CH34x设备")
                null
            }
        }

        driver?.let {
            if (it.open()) {
                log("驱动初始化成功: ${it.javaClass.simpleName}")
                val capabilities = it.capabilities()
                // 显示功能按钮并根据能力启用
                setFunctionButtonsVisibility(true)
                btnUart.isEnabled = capabilities.contains(CH34xDriver.Capability.UART)
                btnSpi.isEnabled = capabilities.contains(CH34xDriver.Capability.SPI)
                btnI2C.isEnabled = capabilities.contains(CH34xDriver.Capability.I2C)
                btnGpio.isEnabled = capabilities.contains(CH34xDriver.Capability.GPIO)
                btnEEPROM.isEnabled = capabilities.contains(CH34xDriver.Capability.EEPROM)
            } else {
                log("驱动打开失败")
            }
        }
    }

    private fun setFunctionButtonsVisibility(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        btnUart.visibility = visibility
        btnSpi.visibility = visibility
        btnI2C.visibility = visibility
        btnGpio.visibility = visibility
        btnEEPROM.visibility = visibility
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}