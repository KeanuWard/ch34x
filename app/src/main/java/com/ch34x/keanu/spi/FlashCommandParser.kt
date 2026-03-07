package com.ch34x.keanu.spi

import org.json.JSONObject

/*
FlashCommandParser

解析Flash数据库JSON
*/

object FlashCommandParser {

fun parse(json: String): List<FlashChip> {

    val result = mutableListOf<FlashChip>()

    val root = JSONObject(json)

    val array = root.getJSONArray("chips")

    for (i in 0 until array.length()) {

        val item = array.getJSONObject(i)

        val commandsJson = item.getJSONObject("commands")

        val commands = mutableMapOf<String, String>()

        val keys = commandsJson.keys()

        while (keys.hasNext()) {

            val key = keys.next()

            commands[key] = commandsJson.getString(key)

        }

        result.add(
            FlashChip(
                item.getString("manufacturer"),
                item.getString("model"),
                item.getString("jedec"),
                item.getInt("page_size"),
                item.getInt("sector_size"),
                commands
            )
        )

    }

    return result
}

}