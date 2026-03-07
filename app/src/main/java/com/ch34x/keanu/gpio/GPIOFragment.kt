package com.ch34x.keanu.gpio

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.ch34x.keanu.R
import com.ch34x.keanu.ch34x.GPIOManager

class GPIOFragment : Fragment() {

    private lateinit var txtStatus: TextView
    private lateinit var btnRead: Button
    private lateinit var btnToggle: Button
    private lateinit var gpioManager: GPIOManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_gpio, container, false)
        txtStatus = v.findViewById(R.id.txt_gpio_status)
        btnRead = v.findViewById(R.id.btn_gpio_read)
        btnToggle = v.findViewById(R.id.btn_gpio_toggle)

        gpioManager = GPIOManager.getInstance()

        btnRead.setOnClickListener { readStatus() }
        btnToggle.setOnClickListener { toggleOutput() }

        return v
    }

    private fun readStatus() {
        val status = gpioManager.read()
        txtStatus.text = "GPIO状态: 0x%02X".format(status)
    }

    private fun toggleOutput() {
        gpioManager.toggle()
        txtStatus.text = "GPIO切换完成"
    }
}