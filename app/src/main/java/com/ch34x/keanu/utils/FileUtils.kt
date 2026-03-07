package com.ch34x.keanu.utils

import android.content.Context
import java.io.File

/*
文件工具

*/

object FileUtils {

fun saveText(
    context: Context,
    name: String,
    text: String
) {

    val file = File(context.filesDir, name)

    file.writeText(text)

}

}