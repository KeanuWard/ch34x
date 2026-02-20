package com.ch34x.usbtool

import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ch34x.usbtool.databinding.ActivityUartBinding
import com.ch34x.usbtool.driver.CH34XDriver
import com.ch34x.usbtool.uart.UARTManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UARTActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUartBinding
    private lateinit var driver: CH34XDriver
    private lateinit var uartManager: UARTManager
    
    private val parityOptions = arrayOf("NONE", "ODD", "EVEN", "MARK", "SPACE")
    private val flowControlOptions = arrayOf("NONE", "HARDWARE", "SOFTWARE")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUartBinding.inflate(layoutInflater)
        setContentView(binding)
        
        driver = (application as? CH34XApplication)?.driver ?: CH34XDriver()
        uartManager = UARTManager(driver)
        
        setupToolbar()
        setupSpinners()
        setupListeners()
        observeData()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupSpinners() {
        // 设置校验位下拉框
        ArrayAdapter(this, android.R.layout.simple_spinner_item, parityOptions).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerParity.adapter = adapter
        }
        
        // 设置流控制下拉框
        ArrayAdapter(this, android.R.layout.simple_spinner_item, flowControlOptions).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerFlowControl.adapter = adapter
        }
    }
    
    private fun setupListeners() {
        // 波特率滑块
        binding.sliderBaudRate.addOnChangeListener { _, value, _ ->
            binding.tvBaudRateValue.text = value.toInt().toString()
        }
        
        // 连接按钮
        binding.btnConnect.setOnClickListener {
            connectUART()
        }
        
        // 断开按钮
        binding.btnDisconnect.setOnClickListener {
            disconnectUART()
        }
        
        // 发送按钮
        binding.btnSend.setOnClickListener {
            sendData()
        }
        
        // 清除按钮
        binding.btnClear.setOnClickListener {
            binding.tvReceivedData.text = ""
            uartManager.clearBuffer()
        }
        
        // 日志控制
        binding.btnStartLog.setOnClickListener {
            startLogging()
        }
        
        binding.btnStopLog.setOnClickListener {
            stopLogging()
        }
    }
    
    private fun observeData() {
        lifecycleScope.launch {
            uartManager.receivedData.collect { data ->
                data?.let {
                    val currentText = binding.tvReceivedData.text.toString()
                    val newData = uartManager.formatDataForDisplay(it)
                    binding.tvReceivedData.text = currentText + newData
                    
                    // 自动滚动到底部
                    binding.scrollView.post {
                        binding.scrollView.fullScroll(android.view.View.FOCUS_DOWN)
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            uartManager.isLogging.collect { isLogging ->
                binding.btnStartLog.isEnabled = !isLogging
                binding.btnStopLog.isEnabled = isLogging
            }
        }
    }
    
    private fun connectUART() {
        val config = UARTManager.UARTConfig(
            baudRate = binding.sliderBaudRate.value.toInt(),
            dataBits = when (binding.rgDataBits.checkedRadioButtonId) {
                R.id.rbDataBits5 -> 5
                R.id.rbDataBits6 -> 6
                R.id.rbDataBits7 -> 7
                else -> 8
            },
            stopBits = when (binding.rgStopBits.checkedRadioButtonId) {
                R.id.rbStopBits15 -> 2
                R.id.rbStopBits2 -> 3
                else -> 1
            },
            parity = UARTManager.Parity.valueOf(parityOptions[binding.spinnerParity.selectedItemPosition]),
            flowControl = UARTManager.FlowControl.valueOf(flowControlOptions[binding.spinnerFlowControl.selectedItemPosition])
        )
        
        if (uartManager.configure(config)) {
            binding.btnConnect.isEnabled = false
            binding.btnDisconnect.isEnabled = true
            binding.btnSend.isEnabled = true
            Toast.makeText(this, "UART连接成功", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "UART配置失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun disconnectUART() {
        uartManager.stop()
        binding.btnConnect.isEnabled = true
        binding.btnDisconnect.isEnabled = false
        binding.btnSend.isEnabled = false
        Toast.makeText(this, "UART已断开", Toast.LENGTH_SHORT).show()
    }
    
    private fun sendData() {
        val text = binding.etSendData.text.toString()
        if (text.isNotEmpty()) {
            uartManager.sendText(text)
            binding.etSendData.text?.clear()
        }
    }
    
    private fun startLogging() {
        val logDir = File(getExternalFilesDir(null), "uart_logs")
        uartManager.startLogging(logDir)
        Toast.makeText(this, "日志记录已开始", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopLogging() {
        uartManager.stopLogging()
        Toast.makeText(this, "日志记录已停止", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        uartManager.stop()
    }
}