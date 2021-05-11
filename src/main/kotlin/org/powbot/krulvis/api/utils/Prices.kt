package org.powbot.krulvis.api.utils

import com.google.gson.JsonParser
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.URL

object Prices {

    val GE_API_URL_BASE = "https://secure.runescape.com/m=itemdb_oldschool/api/catalogue/detail.json?item="
    val priceMap = mutableMapOf<Int, Int>()

    fun getPrice(id: Int): Int {
        when (id) {
            995 -> return 1
            3985, 3987, 3989, 3991 -> return 1000
        }
        if (priceMap.containsKey(id)) {
            return priceMap[id]!!
        } else {
            val price = getExchangePrice(id).toInt()
            if (price != -1) {
                priceMap[id] = price
            }
            return price
        }
    }

    private fun getExchangePrice(id: Int): Double {
        try {
            val conn = URL(GE_API_URL_BASE + id).openConnection()
            val parser = JsonParser()
            val ipr = InputStreamReader(conn.getInputStream())
            val parsed = parser.parse(ipr)
            if (parsed != null) {
                val el = parsed.asJsonObject.get("item")
                val item = el.asJsonObject
                if (item != null) {
                    val curr = item.get("current")
                    if (curr != null) {
                        val itemObj = curr.asJsonObject
                        if (itemObj != null) {
                            val priceEl = itemObj.get("price")
                            if (priceEl != null) {
                                val priceString = priceEl.asString.replace(",", "")
                                val p: Double
                                p = when {
                                    priceString.contains("m") -> (priceString.replace(
                                        "m".toRegex(),
                                        ""
                                    )).toDouble() * 1000000
                                    priceString.contains("k") -> (priceString.replace(
                                        "k".toRegex(),
                                        ""
                                    )).toDouble() * 1000
                                    else -> priceString.toDouble()
                                }
//                                println("RSGE Price: $p")
                                return p
                            }
                        }
                    }
                }
            }
            ipr.close()
            return -1.0
        } catch (fnfe: FileNotFoundException) {
            println("Untradable: $id?")
            return -1.0
        }
    }
}