package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Varpbits.varpbit
import org.powbot.mobile.rscache.loader.ItemLoader


class Runepouch {
    var runes = HashMap<Int, Int>()
    var runeID = 0
    var runeName: String? = null
    private fun map() {
        runes.clear()
        runes[1] = 556
        runes[2] = 555
        runes[3] = 557
        runes[4] = 554
        runes[5] = 558
        runes[6] = 562
        runes[7] = 560
        runes[8] = 565
        runes[9] = 564
        runes[10] = 561
        runes[11] = 563
        runes[12] = 559
        runes[13] = 566
        runes[14] = 9075
        runes[15] = 4695
        runes[16] = 4698
        runes[17] = 4696
        runes[18] = 4699
        runes[19] = 4694
        runes[20] = 4697
        runes[21] = 21880
    }

    fun getRuneID(ID: Int): Int {
        map()
        runeID = runes[ID]!!
        return runeID
    }

    fun getRuneName(ID: Int): String? {
        map()
        val itemID = getRuneID(ID)
        runeName = ItemLoader.load(itemID)!!.name
        return runeName
    }

    val runeAmounts: IntArray
        get() {
            val varp1 = 1139
            val varp2 = 1140
            val firstRuneCount = varpbit(varp1) shr 18
            val secondRuneCount = varpbit(varp2) and 0xFFF
            val thirdRuneCount = varpbit(varp2) shr 14 and 0x1f4
            return intArrayOf(firstRuneCount, secondRuneCount, thirdRuneCount)
        }
    val runeIDs: IntArray
        get() {
            val varp1 = 1139
            val firstRuneID = varpbit(varp1) and 0x3F
            val secondRuneID = varpbit(varp1) shr 6 and 0x3F
            val thirdRuneID = varpbit(varp1) shr 12 and 0x3F
            return intArrayOf(getRuneID(firstRuneID), getRuneID(secondRuneID), getRuneID(thirdRuneID))
        }
    val runeNames: String
        get() {
            val rune1 = ItemLoader.load(runeIDs[0])!!.name
            val rune2 = ItemLoader.load(runeIDs[1])!!.name
            val rune3 = ItemLoader.load(runeIDs[2])!!.name
            return "$rune1 $rune2 $rune3"
        }

    fun getRuneAmount(ID: Int): Int {
        val IDs = runeIDs
        val counts = runeAmounts
        var count = 0
        for (i in 0..2) {
            if (IDs[i] == ID) {
                count = counts[i]
                break
            }
        }
        return count
    }
}