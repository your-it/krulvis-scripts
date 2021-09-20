package org.powbot.krulvis.api.extensions.magic

import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.extensions.items.Item

enum class Rune private constructor(
    override val id: Int,
    val tiaraID: Int,
    val talismanID: Int,
    vararg val runePowers: RunePower
) : Item {


    FIRE(554, 5537, 1442, RunePower.FIRE),
    WATER(555, 5531, 1444, RunePower.WATER),
    AIR(556, 5527, 1438, RunePower.AIR),
    EARTH(557, 5535, 1440, RunePower.EARTH),
    MIND(558, 5529, 1448, RunePower.MIND),
    BODY(559, -1, -1, RunePower.BODY),
    DEATH(560, 5533, 1446, RunePower.DEATH),
    NATURE(561, 5533, 1446, RunePower.NATURE),
    CHAOS(562, 5533, 1446, RunePower.CHAOS),
    LAW(563, 553, 1446, RunePower.LAW),
    COSMIC(564, 5533, 1446, RunePower.COSMIC),
    BLOOD(565, -1, -1, RunePower.BLOOD),
    WRATH(21880, -1, -1, RunePower.WRATH),
    STEAM(4694, -1, -1, RunePower.FIRE, RunePower.WATER),
    MIST(4695, -1, -1, RunePower.WATER, RunePower.AIR),
    MUD(4698, -1, -1, RunePower.EARTH, RunePower.WATER),
    LAVA(4699, -1, -1, RunePower.FIRE, RunePower.EARTH),
    SMOKE(4697, -1, -1, RunePower.FIRE, RunePower.AIR),
    DUST(4696, -1, -1, RunePower.AIR, RunePower.EARTH);

    override val ids: IntArray
        get() = intArrayOf(id)

    override fun hasWith(): Boolean = inInventory()

    override fun getCount(countNoted: Boolean): Int = Inventory.getCount(id)

    fun isElemental(): Boolean {
        return ELEMENTALS.contains(this)
    }

    override fun toString(): String {
        return name
    }

    companion object {

        var PURE_ESSENCE_ID = 7936
        var RUNE_ESSENCE_ID = 1436
        val ELEMENTALS = arrayOf(FIRE, WATER, EARTH, AIR)

        fun getRune(runeName: String): Rune? {
            return values().firstOrNull { it.name.equals(runeName, true) }
        }

        fun forId(id: Int): Rune? {
            return values().firstOrNull { it.id == id }
        }

        fun hasRuneForPower(runePower: RunePower): Boolean =
            values().filter { it.runePowers.contains(runePower) }.any { Inventory.containsOneOf(it.id) }

        fun getForPower(runePower: RunePower): Rune {
            return values().first { it.runePowers.contains(runePower) }
        }
    }
}
