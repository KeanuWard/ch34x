package com.ch34x.keanu.spi

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.ch34x.keanu.R
import com.ch34x.keanu.ch34x.SpiFlashManager

class SpiFragment : Fragment() {

    private lateinit var txtStatus: TextView
    private lateinit var btnDetect: Button
    private lateinit var btnRead: Button
    private lateinit var btnErase: Button
    private lateinit var spiManager: SpiFlashManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_spi, container, false)
        txtStatus = v.findViewById(R.id.txt_status)
        btnDetect = v.findViewById(R.id.btn_detect)
        btnRead = v.findViewById(R.id.btn_read)
        btnErase = v.findViewById(R.id.btn_erase)

        spiManager = SpiFlashManager.getInstance()

        btnDetect.setOnClickListener { detectFlash() }
        btnRead.setOnClickListener { readFlash() }
        btnErase.setOnClickListener { eraseFlash() }

        return v
    }

    private fun detectFlash() {
        val chip = spiManager.detect()
        txtStatus.text = "检测到Flash: ${chip?.model ?: "未知"}"
    }

    private fun readFlash() {
        val result = spiManager.readAll()
        txtStatus.text = "读取完成, 长度: ${result.size} bytes"
    }

    private fun eraseFlash() {
        val success = spiManager.erase()
        txtStatus.text = if (success) "擦除完成" else "擦除失败"
    }
}