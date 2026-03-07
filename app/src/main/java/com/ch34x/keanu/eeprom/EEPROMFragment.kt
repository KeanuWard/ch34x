package com.ch34x.keanu.eeprom

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.ch34x.keanu.R
import com.ch34x.keanu.ch34x.EEPROMManager

class EEPROMFragment : Fragment() {

    private lateinit var txtStatus: TextView
    private lateinit var btnRead: Button
    private lateinit var btnWrite: Button
    private lateinit var eepromManager: EEPROMManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_eeprom, container, false)
        txtStatus = v.findViewById(R.id.txt_eeprom_status)
        btnRead = v.findViewById(R.id.btn_eeprom_read)
        btnWrite = v.findViewById(R.id.btn_eeprom_write)

        eepromManager = EEPROMManager.getInstance()

        btnRead.setOnClickListener { readEEPROM() }
        btnWrite.setOnClickListener { writeEEPROM() }

        return v
    }

    private fun readEEPROM() {
        val data = eepromManager.read(0, 256)
        txtStatus.text = "读取数据: ${data.joinToString(" ") { "%02X".format(it) }}"
    }

    private fun writeEEPROM() {
        val data = ByteArray(4) { it.toByte() }
        eepromManager.write(0, data)
        txtStatus.text = "写入完成"
    }
}