package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Varpbits
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getCount

enum class Bar(
    override val ids: IntArray,
    val shopId: Int,
    //settingId & shiftAmount are used for Blast furnace
    val settingId: Int,
    val shiftAmount: Int,
    val primary: Ore,
    val secondary: Ore,
    val secondaryMultiplier: Int
) : Item {
    GOLD(intArrayOf(2357), 113, 546, 16, Ore.GOLD, Ore.COAL, 0),
    BRONZE(intArrayOf(2349), 109, -1, -1, Ore.TIN, Ore.COPPER, 1),
    IRON(intArrayOf(2351), 109, 545, 8, Ore.IRON, Ore.COAL, 0),
    STEEL(intArrayOf(2353), 110, 545, 16, Ore.IRON, Ore.COAL, 1),
    MITHRIL(intArrayOf(2359), 111, 545, 24, Ore.MITHRIL, Ore.COAL, 2),
    ADAMANTITE(intArrayOf(2361), 112, 546, 0, Ore.ADAMANTITE, Ore.COAL, 3),
    RUNITE(intArrayOf(2363), 113, 546, 8, Ore.RUNITE, Ore.COAL, 4);

    var barsMade = 0
    val blastFurnaceCount: Int
        get() = Varpbits.varpbit(settingId) shr shiftAmount and 0x1F

    fun addMade(made: Int) {
        barsMade += made
        barsMade
    }

    override fun hasWith(): Boolean {
        return Inventory.containsOneOf(*ids)
    }

    override fun getCount(countNoted: Boolean): Int {
        return Inventory.getCount(*ids)
    }

    override fun toString(): String {
        return name
    }
}





