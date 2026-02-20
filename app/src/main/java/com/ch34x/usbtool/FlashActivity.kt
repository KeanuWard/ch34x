package com.ch34x.usbtool

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ch34x.usbtool.databinding.ActivityFlashBinding
import com.ch34x.usbtool.driver.CH34XDriver
import com.ch34x.usbtool.flash.FlashDatabase
import com.ch34x.usbtool.spi.SPIFlashProgrammer
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FlashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFlashBinding
    private lateinit var driver: CH34XDriver
    private lateinit var flashDatabase: FlashDatabase
    private var programmer: SPIFlashProgrammer? = null
    private var selectedFlash: FlashDatabase.FlashInfo? = null
    
    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { loadFile(it) }
    }
    
    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let { saveFile(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashBinding.inflate(layoutInflater)
        setContentView(binding)
        
        driver = (application as? CH34XApplication)?.driver ?: CH34XDriver()
        flashDatabase = FlashDatabase(this)
        
        setupToolbar()
        setupSpinners()
        setupListeners()
        loadFlashList()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupSpinners() {
        // Flash列表下拉框
        val flashAdapter = ArrayAdapter<FlashDatabase.FlashInfo>(
            this,
            android.R.layout.simple_spinner_item,
            flashDatabase.getAllFlash()
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerFlashList.adapter = flashAdapter
        
        binding.spinnerFlashList.setOnItemClickListener { _, _, position, _ ->
            selectedFlash = flashDatabase.getAllFlash()[position]
            updateFlashInfo(selectedFlash)
        }
    }
    
    private fun setupListeners() {
        binding.btnIdentify.setOnClickListener {
            identifyFlash()
        }
        
        binding.btnRead.setOnClickListener {
            readFlash()
        }
        
        binding.btnWrite.setOnClickListener {
            writeFlash()
        }
        
        binding.btnErase.setOnClickListener {
            eraseFlash()
        }
        
        binding.btnVerify.setOnClickListener {
            verifyFlash()
        }
        
        binding.btnChipErase.setOnClickListener {
            chipErase()
        }
        
        binding.btnBrowse.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("*/*"))
        }
        
        binding.btnExportDb.setOnClickListener {
            exportDatabase()
        }
        
        binding.btnImportDb.setOnClickListener {
            importDatabase()
        }
    }
    
    private fun loadFlashList() {
        val flashes = flashDatabase.getAllFlash()
        if (flashes.isNotEmpty()) {
            selectedFlash = flashes[0]
            updateFlashInfo(selectedFlash)
        }
    }
    
    private fun updateFlashInfo(flash: FlashDatabase.FlashInfo?) {
        if (flash == null) {
            binding.tvFlashName.text = "-"
            binding.tvFlashSize.text = "-"
            binding.tvFlashPageSize.text = "-"
            binding.tvFlashSectorSize.text = "-"
        } else {
            binding.tvFlashName.text = flash.name
            binding.tvFlashSize.text = formatSize(flash.capacity)
            binding.tvFlashPageSize.text = "${flash.pageSize} bytes"
            binding.tvFlashSectorSize.text = "${flash.sectorSize} bytes"
        }
    }
    
    private fun identifyFlash() {
        lifecycleScope.launch {
            showProgress("正在识别Flash...")
            
            selectedFlash?.let { flash ->
                programmer = SPIFlashProgrammer(driver, flash)
                val id = programmer?.readID()
                
                if (id != null && id.isNotEmpty()) {
                    val manufacturerId = id[0]
                    val deviceId = if (id.size > 1) id[1] else 0
                    
                    binding.tvFlashId.text = String.format(
                        "制造商: %02X, 设备: %02X",
                        manufacturerId, deviceId
                    )
                    
                    Toast.makeText(this@FlashActivity, "识别成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@FlashActivity, "识别失败", Toast.LENGTH_SHORT).show()
                }
            }
            
            hideProgress()
        }
    }
    
    private fun readFlash() {
        val address = binding.etStartAddress.text.toString().toLongOrNull() ?: 0
        val size = binding.etLength.text.toString().toIntOrNull() ?: 0
        
        if (size <= 0) {
            Toast.makeText(this, "请输入有效长度", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            val progressDialog = ProgressDialog(this@FlashActivity).apply {
                setTitle("读取Flash")
                setMessage("正在读取...")
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setMax(100)
                setCancelable(false)
                show()
            }
            
            val fileData = mutableListOf<Byte>()
            
            programmer?.readFlash(
                address = address,
                size = size,
                onDataRead = { data ->
                    fileData.addAll(data.toList())
                },
                onProgress = { progress ->
                    progressDialog.progress = progress
                }
            )?.let { success ->
                progressDialog.dismiss()
                
                if (success) {
                    val data = fileData.toByteArray()
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    val filename = "flash_read_${timestamp}.bin"
                    
                    createDocumentLauncher.launch(filename)
                }
            }
        }
    }
    
    private fun writeFlash() {
        val address = binding.etStartAddress.text.toString().toLongOrNull() ?: 0
        
        lifecycleScope.launch {
            val progressDialog = ProgressDialog(this@FlashActivity).apply {
                setTitle("写入Flash")
                setMessage("正在写入...")
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setMax(100)
                setCancelable(false)
                show()
            }
            
            // 这里需要从文件读取数据
            // 示例代码，实际需要实现文件读取
            val data = byteArrayOf()
            
            programmer?.writeFlash(
                address = address,
                data = data,
                verify = binding.cbVerify.isChecked,
                autoErase = binding.cbAutoErase.isChecked,
                onProgress = { progress ->
                    progressDialog.progress = progress
                }
            )?.let { success ->
                progressDialog.dismiss()
                
                if (success) {
                    Toast.makeText(this@FlashActivity, "写入成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@FlashActivity, "写入失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun eraseFlash() {
        val address = binding.etStartAddress.text.toString().toLongOrNull() ?: 0
        val size = binding.etLength.text.toString().toIntOrNull() ?: 0
        
        if (size <= 0) {
            Toast.makeText(this, "请输入有效长度", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            val progressDialog = ProgressDialog(this@FlashActivity).apply {
                setTitle("擦除Flash")
                setMessage("正在擦除...")
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setMax(100)
                setCancelable(false)
                show()
            }
            
            programmer?.eraseFlash(address, size)?.let { success ->
                progressDialog.dismiss()
                
                if (success) {
                    Toast.makeText(this@FlashActivity, "擦除成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@FlashActivity, "擦除失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun verifyFlash() {
        val address = binding.etStartAddress.text.toString().toLongOrNull() ?: 0
        
        lifecycleScope.launch {
            showProgress("正在校验...")
            
            // 实现校验逻辑
            
            hideProgress()
        }
    }
    
    private fun chipErase() {
        lifecycleScope.launch {
            val progressDialog = ProgressDialog(this@FlashActivity).apply {
                setTitle("整片擦除")
                setMessage("正在擦除...")
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setMax(100)
                setCancelable(false)
                show()
            }
            
            programmer?.chipErase()?.let { success ->
                progressDialog.dismiss()
                
                if (success) {
                    Toast.makeText(this@FlashActivity, "整片擦除成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@FlashActivity, "整片擦除失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun loadFile(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val data = inputStream.readBytes()
                binding.tvFilePath.text = uri.path
                // 保存数据到临时变量
            }
        } catch (e: Exception) {
            Toast.makeText(this, "文件读取失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveFile(uri: Uri) {
        // 实现文件保存逻辑
    }
    
    private fun exportDatabase() {
        val json = flashDatabase.exportDatabase()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "flash_db_export_${timestamp}.json"
        
        createDocumentLauncher.launch(filename)
    }
    
    private fun importDatabase() {
        openDocumentLauncher.launch(arrayOf("application/json"))
    }
    
    private fun formatSize(size: Long): String {
        return when {
            size >= 1024 * 1024 * 1024 -> String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0))
            size >= 1024 * 1024 -> String.format("%.2f MB", size / (1024.0 * 1024.0))
            size >= 1024 -> String.format("%.2f KB", size / 1024.0)
            else -> "$size B"
        }
    }
    
    private fun showProgress(message: String) {
        // 显示进度对话框
    }
    
    private fun hideProgress() {
        // 隐藏进度对话框
    }
}