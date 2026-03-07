package com.ch34x.keanu.i2c

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.ch34x.keanu.R
import com.ch34x.keanu.ch34x.I2CManager

class I2CFragment : Fragment() {

    private lateinit var txtStatus: TextView
    private lateinit var btnScan: Button
    private lateinit var btnRead: Button
    private lateinit var btnWrite: Button
    private lateinit var i2cManager: I2CManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_i2c, container, false)
        txtStatus = v.findViewById(R.id.txt_i2c_status)
        btnScan = v.findViewById(R.id.btn_i2c_scan)
        btnRead = v.findViewById(R.id.btn_i2c_read)
        btnWrite = v.findViewById(R.id.btn_i2c_write)

        i2cManager = I2CManager.getInstance()

        btnScan.setOnClickListener { scanDevices() }
        btnRead.setOnClickListener { readData() }
        btnWrite.setOnClickListener { writeData() }

        return v
    }

    private fun scanDevices() {
        val devices = i2cManager.scan()
        txtStatus.text = if (devices.isEmpty()) "未发现I2C设备" else "发现设备: ${devices.joinToString(",") { "0x%02X".format(it) }}"
    }

    private fun readData() {
        val data = i2cManager.read(0x50, 0, 16)
        txtStatus.text = "读取数据: ${data.joinToString(" ") { "%02X".format(it) }}"
    }

    private fun writeData() {
        val buffer = byteArrayOf(0x01, 0x02)
        i2cManager.write(0x50, 0, buffer)
        txtStatus.text = "写入完成"
    }
}