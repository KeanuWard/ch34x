package com.ch34x.keanu.spi

import android.content.Context

/*
FlashDatabase

加载Flash数据库
*/

class FlashDatabase(private val context: Context) {

private val chips = mutableListOf<FlashChip>()

fun load() {

    val json = context.assets
        .open("flash_database.json")
        .bufferedReader()
        .use { it.readText() }

    chips.clear()

    chips.addAll(
        FlashCommandParser.parse(json)
    )

}

fun findByJedec(id: String): FlashChip? {

    return chips.find {

        it.jedec.equals(id, true)

    }

}

}