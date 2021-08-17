package org.powbot.krulvis.blastfurnace

import org.powbot.api.rt4.Varpbits

val COAL_BAG = 12019
val GOLD_GLOVES = 776
val ICE_GLOVES = 1580

enum class Ore(val itemId: Int, val maximum: Int, val settingId: Int, val shiftAmount: Int) {
    GOLD(444, 28, 548, 16),
    COAL(453, 205, 547, 0),
    IRON(440, 28, 547, 16),
    MITH(447, 28, 547, 24),
    ADAMANT(449, 28, 548, 0),
    RUNE(451, 28, 548, 8);

    var error = 0
    private val mask = 255
    val amount: Int
        get() = Varpbits.varpbit(settingId) shr shiftAmount and mask


}
//000000010000000000000000
//000010100000000000000000

enum class Bar(
    val itemId: Int,
    val shopId: Int,
    val settingId: Int,
    val shiftAmount: Int,
    val primary: Ore,
    val secondary: Ore,
    val secondaryMultiplier: Int
) {
    GOLD(2357, 113, 546, 16, Ore.GOLD, Ore.COAL, 0),
    IRON(2351, 109, 545, 8, Ore.IRON, Ore.COAL, 0),
    STEEL(2353, 110, 545, 16, Ore.IRON, Ore.COAL, 1),
    MITHRIL(2359, 111, 545, 24, Ore.MITH, Ore.COAL, 2),
    ADAMANTITE(2361, 112, 546, 0, Ore.ADAMANT, Ore.COAL, 3),
    RUNITE(2363, 113, 546, 8, Ore.RUNE, Ore.COAL, 4);

    var barsMade = 0
    val amount: Int
        get() = Varpbits.varpbit(settingId) shr shiftAmount and 0x1F

    fun addMade(made: Int) {
        barsMade += made
        barsMade
    }

    override fun toString(): String {
        return name
    }
}

fun main() {
    println((655360).shr(16).and(0x1F))
}

