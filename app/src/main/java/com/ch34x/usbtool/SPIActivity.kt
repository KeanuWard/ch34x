package com.ch34x.usbtool

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ch34x.usbtool.databinding.ActivitySpiBinding
import com.ch34x.usbtool.driver.CH34XDriver
import kotlinx.coroutines.launch

class SPIActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySpiBinding
    private lateinit var driver: CH34XDriver
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpiBinding.inflate(layoutInflater)
        setContentView(binding)
        
        driver = (application as? CH34XApplication)?.driver ?: CH34XDriver()
        
        setupToolbar()
        setupListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupListeners() {
        binding.btnConfigure.setOnClickListener {
            configureSPI()
        }
        
        binding.btnTransfer.setOnClickListener {
            transferData()
        }
        
        binding.btnRead.setOnClickListener {
            readData()
        }
        
        binding.btnWrite.setOnClickListener {
            writeData()
        }
        
        binding.btnClear.setOnClickListener {
            binding.tvReceivedData.text = ""
        }
    }
    
    private fun configureSPI() {
        val mode = when (binding.rgSpiMode.checkedRadioButtonId) {
            R.id.rbMode0 -> 0
            R.id.rbMode1 -> 1
            R.id.rbMode2 -> 2
            R.id.rbMode3 -> 3
            else -> 0
        }
        
        val speed = binding.etSpeed.text.toString().toIntOrNull() ?: 1000000
        val lsbFirst = binding.cbLsbFirst.isChecked
        
        if (driver.configureSPI(mode, speed, lsbFirst)) {
            Toast.makeText(this, "SPI配置成功", Toast.LENGTH_SHORT).show()
            binding.btnTransfer.isEnabled = true
            binding.btnRead.isEnabled = true
            binding.btnWrite.isEnabled = true
        } else {
            Toast.makeText(this, "SPI配置失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun transferData() {
        val dataStr = binding.etSendData.text.toString()
        if (dataStr.isEmpty()) {
            Toast.makeText(this, "请输入数据", Toast.LENGTH_SHORT).show()
            return
        }
        
        val data = if (binding.cbHexMode.isChecked) {
            hexStringToByteArray(dataStr)
        } else {
            dataStr.toByteArray()
        }
        
        lifecycleScope.launch {
            val result = driver.transferSPI(data)
            result?.let {
                displayResult(it)
            } ?: run {
                Toast.makeText(this@SPIActivity, "传输失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun readData() {
        val length = binding.etReadLength.text.toString().toIntOrNull() ?: 1
        val dummyData = ByteArray(length) { 0x00 }
        
        lifecycleScope.launch {
            val result = driver.transferSPI(dummyData)
            result?.let {
                displayResult(it)
            } ?: run {
                Toast.makeText(this@SPIActivity, "读取失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun writeData() {
        val dataStr = binding.etWriteData.text.toString()
        if (dataStr.isEmpty()) {
            Toast.makeText(this, "请输入数据", Toast.LENGTH_SHORT).show()
            return
        }
        
        val data = if (binding.cbHexMode.isChecked) {
            hexStringToByteArray(dataStr)
        } else {
            dataStr.toByteArray()
        }
        
        lifecycleScope.launch {
            val result = driver.transferSPI(data)
            if (result != null) {
                Toast.makeText(this@SPIActivity, "写入成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@SPIActivity, "写入失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun displayResult(data: ByteArray) {
        val currentText = binding.tvReceivedData.text.toString()
        val displayText = if (binding.cbHexMode.isChecked) {
            data.joinToString(" ") { "%02X".format(it) }
        } else {
            String(data)
        }
        binding.tvReceivedData.text = currentText + displayText + "\n"
    }
    
    private fun hexStringToByteArray(s: String): ByteArray {
        val cleanHex = s.replace("\\s".toRegex(), "")
        val len = cleanHex.length
        require(len % 2 == 0) { "Hex string must have even length" }
        
        return ByteArray(len / 2).apply {
            for (i in indices) {
                val index = i * 2
                val byte = cleanHex.substring(index, index + 2).toInt(16)
                this[i] = byte.toByte()
            }
        }
    }
}