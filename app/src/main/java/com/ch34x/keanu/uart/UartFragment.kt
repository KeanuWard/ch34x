package com.ch34x.keanu.uart

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.ch34x.keanu.R
import com.ch34x.keanu.ch34x.UartManager

class UartFragment : Fragment() {

    private lateinit var rxView: TextView
    private lateinit var txEdit: EditText
    private lateinit var btnSend: Button
    private lateinit var btnClear: Button
    private lateinit var hexSwitch: Switch
    private lateinit var uartManager: UartManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_uart, container, false)
        rxView = v.findViewById(R.id.rx_view)
        txEdit = v.findViewById(R.id.tx_edit)
        btnSend = v.findViewById(R.id.btn_send)
        btnClear = v.findViewById(R.id.btn_clear)
        hexSwitch = v.findViewById(R.id.hex_switch)

        uartManager = UartManager.getInstance() // 获取单例 UART 管理器

        btnSend.setOnClickListener { sendData() }
        btnClear.setOnClickListener { rxView.text = "" }

        // 订阅接收数据
        uartManager.setOnDataReceivedListener { data ->
            activity?.runOnUiThread {
                val display = if (hexSwitch.isChecked) data.toHexString() else String(data)
                rxView.append(display + "\n")
            }
        }

        return v
    }

    private fun sendData() {
        val data = txEdit.text.toString()
        if (hexSwitch.isChecked) {
            uartManager.sendHex(data)
        } else {
            uartManager.sendText(data)
        }
    }

    private fun ByteArray.toHexString(): String {
        return joinToString(" ") { String.format("%02X", it) }
    }
}