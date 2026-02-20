package com.ch34x.usbtool

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ch34x.usbtool.databinding.ActivityMainBinding
import com.ch34x.usbtool.driver.CH34XDriver
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var usbManager: UsbManager
    private lateinit var driver: CH34XDriver
    private lateinit var deviceAdapter: DeviceAdapter
    
    private var connectedDevice: UsbDevice? = null
    private var deviceInfo: CH34XDriver.DeviceInfo? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        driver = CH34XDriver()
        
        setupUI()
        observeDriverState()
        scanDevices()
    }
    
    private fun setupUI() {
        // 设置Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "CH34X USB Tool"
        
        // 设置RecyclerView
        deviceAdapter = DeviceAdapter { device ->
            connectToDevice(device)
        }
        binding.rvDevices.layoutManager = LinearLayoutManager(this)
        binding.rvDevices.adapter = deviceAdapter
        
        // 设置按钮点击事件
        binding.btnRefresh.setOnClickListener {
            scanDevices()
        }
        
        binding.btnUart.setOnClickListener {
            connectedDevice?.let { device ->
                val intent = Intent(this, UARTActivity::class.java)
                intent.putExtra("device_info", deviceInfo)
                startActivity(intent)
            }
        }
        
        binding.btnSpi.setOnClickListener {
            connectedDevice?.let {
                val intent = Intent(this, SPIActivity::class.java)
                startActivity(intent)
            }
        }
        
        binding.btnFlash.setOnClickListener {
            connectedDevice?.let {
                val intent = Intent(this, FlashActivity::class.java)
                startActivity(intent)
            }
        }
    }
    
    private fun observeDriverState() {
        lifecycleScope.launch {
            driver.connectionState.collect { state ->
                when (state) {
                    CH34XDriver.ConnectionState.CONNECTED -> {
                        updateStatus("已连接", R.color.status_connected)
                        binding.btnUart.isEnabled = true
                        binding.btnSpi.isEnabled = true
                        binding.btnFlash.isEnabled = true
                        updateDeviceStatus(connectedDevice, true)
                    }
                    CH34XDriver.ConnectionState.DISCONNECTED -> {
                        updateStatus("未连接", R.color.status_disconnected)
                        binding.btnUart.isEnabled = false
                        binding.btnSpi.isEnabled = false
                        binding.btnFlash.isEnabled = false
                        updateDeviceStatus(connectedDevice, false)
                        connectedDevice = null
                        deviceInfo = null
                        updateDeviceInfo(null)
                    }
                    CH34XDriver.ConnectionState.CONNECTING -> {
                        updateStatus("连接中...", R.color.status_busy)
                    }
                    CH34XDriver.ConnectionState.ERROR -> {
                        updateStatus("连接错误", R.color.status_error)
                        Toast.makeText(this@MainActivity, "连接失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun updateStatus(text: String, colorResId: Int) {
        binding.tvStatus.text = text
        binding.statusLed.backgroundTintList = getColorStateList(colorResId)
    }
    
    private fun scanDevices() {
        val deviceList = usbManager.deviceList
        val ch34xDevices = mutableListOf<UsbDevice>()
        
        for ((_, device) in deviceList) {
            val info = driver.identifyDevice(usbManager, device)
            if (info.deviceType != CH34XDriver.DeviceType.UNKNOWN) {
                ch34xDevices.add(device)
            }
        }
        
        deviceAdapter.submitList(ch34xDevices)
        
        if (ch34xDevices.isEmpty()) {
            Toast.makeText(this, "未检测到CH34X设备", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun connectToDevice(device: UsbDevice) {
        lifecycleScope.launch {
            val info = driver.identifyDevice(usbManager, device)
            if (driver.connect(usbManager, device)) {
                connectedDevice = device
                deviceInfo = info
                updateDeviceInfo(info)
                Toast.makeText(this@MainActivity, "连接成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "连接失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateDeviceInfo(info: CH34XDriver.DeviceInfo?) {
        if (info == null) {
            binding.tvDeviceName.text = "-"
            binding.tvDeviceType.text = "-"
            binding.tvDeviceSerial.text = "-"
        } else {
            binding.tvDeviceName.text = info.product ?: info.deviceType.name
            binding.tvDeviceType.text = "${info.deviceType} (VID: ${info.vid.toString(16)} PID: ${info.pid.toString(16)})"
            binding.tvDeviceSerial.text = info.serialNumber ?: "无序列号"
        }
    }
    
    private fun updateDeviceStatus(device: UsbDevice?, connected: Boolean) {
        deviceAdapter.updateDeviceStatus(device, connected)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        driver.disconnect()
    }
    
    /**
     * 设备列表适配器
     */
    class DeviceAdapter(
        private val onItemClick: (UsbDevice) -> Unit
    ) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
        
        private var devices: List<UsbDevice> = emptyList()
        private val connectedDevices = mutableSetOf<UsbDevice>()
        
        fun submitList(newList: List<UsbDevice>) {
            devices = newList
            notifyDataSetChanged()
        }
        
        fun updateDeviceStatus(device: UsbDevice?, connected: Boolean) {
            if (device == null) {
                connectedDevices.clear()
            } else if (connected) {
                connectedDevices.add(device)
            } else {
                connectedDevices.remove(device)
            }
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_device, parent, false)
            return DeviceViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            holder.bind(devices[position])
        }
        
        override fun getItemCount() = devices.size
        
        inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
            private val tvDeviceInfo: TextView = itemView.findViewById(R.id.tvDeviceInfo)
            private val statusLed: View = itemView.findViewById(R.id.deviceStatusLed)
            
            init {
                itemView.setOnClickListener {
                    onItemClick(devices[adapterPosition])
                }
            }
            
            fun bind(device: UsbDevice) {
                val isConnected = connectedDevices.contains(device)
                
                tvDeviceName.text = device.productName ?: "CH34X Device"
                tvDeviceInfo.text = String.format(
                    "VID: %04X PID: %04X",
                    device.vendorId, device.productId
                )
                
                statusLed.backgroundTintList = itemView.context.getColorStateList(
                    if (isConnected) R.color.status_connected else R.color.status_disconnected
                )
            }
        }
    }
}