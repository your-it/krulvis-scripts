package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.*
import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.waitFor
import kotlin.math.abs
import kotlin.math.floor

enum class Potion(
    val skill: Int, private val restore: Int,
    override vararg val ids: Int
) : Item {

    ENERGY(Constants.SKILLS_AGILITY, 10, 3008, 3010, 3012, 3014),
    SUPER_ENERGY(Constants.SKILLS_AGILITY, 20, 3016, 3018, 3020, 3022),
    STAMINA(Constants.SKILLS_AGILITY, 20, 12625, 12627, 12629, 12631),
    RANGED(Constants.SKILLS_RANGE, -1, 2444, 169, 171, 173),
    PRAYER(Constants.SKILLS_PRAYER, -1, 2434, 139, 141, 143),
    OVERLOAD(Constants.SKILLS_STRENGTH, -1, 11730, 11731, 11732, 11733),
    ABSORPTION(Constants.SKILLS_HITPOINTS, -1, 11734, 11735, 11736, 11737),
    COMBAT(Constants.SKILLS_STRENGTH, -1, 9739, 9741, 9743, 9745),
    ANTIFIRE_EXTENDED(-1, -1, 11951, 11953, 11955, 11957),
    ANTIFIRE(-1, -1, 2452, 2454, 2456, 2458),
    ANTIPOISON(-1, -1, 2446, 175, 177, 179);

    val bestPot: Int = ids[0]

    fun getRestore(): Int {
        return when (this) {
            OVERLOAD -> 5 + floor(Skills.realLevel(skill) * 0.15).toInt()
            RANGED -> 4 + floor(Skills.realLevel(skill) * 0.1).toInt()
            PRAYER -> floor(7 + Skills.realLevel(skill) / 4.0).toInt()
            COMBAT -> 3 + floor(Skills.realLevel(skill) / 10.0).toInt()
            else -> {
                restore
            }
        }
    }

    fun drink(): Boolean {
        val item = getInvItem(true)
        if (item != null) {
            Game.tab(Game.Tab.INVENTORY)
            val id = item.id
            val drank = item.interact("Drink")
            if (drank && (this == ANTIFIRE_EXTENDED || this == ANTIFIRE)
                && waitFor { id != (getInvItem()?.id ?: -1) }
            ) {
                antiFireTimer = Timer(if (this == ANTIFIRE_EXTENDED) 12 * 60 * 1000 else 5.8 * 60 * 1000)
            } else {
                return drank
            }
        }
        return false
    }

    fun isHighOnStamina(): Boolean {
        val actual = Varpbits.varpbit(1575)
        return (actual == 12582912 || actual == 4194304) || Movement.energyLevel() >= 60
    }

    fun needsRestore(percentage: Int): Boolean {
        return when (this) {
            ANTIFIRE_EXTENDED, ANTIFIRE -> !isProtectedFromFire()
            ANTIPOISON -> Combat.isPoisoned()
            PRAYER -> Skills.realLevel(skill) - Skills.level(skill) >= getRestore()
            STAMINA, ENERGY, SUPER_ENERGY -> !isHighOnStamina() && Movement.energyLevel() <= percentage
            else -> {
                val rl = if (skill == Constants.SKILLS_AGILITY) 100 else Skills.realLevel(skill)
                val cl = if (skill == Constants.SKILLS_AGILITY) Movement.energyLevel() else Skills.level(skill)
                val difference = abs(rl - cl)
                val restore = getRestore()
                val restorePercent = (restore * (percentage / 100.0)).toInt()
                difference <= restorePercent || cl < rl
            }
        }
    }

    override fun getCount(countNoted: Boolean): Int {
        return getInventoryCount(countNoted)
    }

    override fun hasWith(): Boolean {
        return inInventory()
    }

    companion object {

        var antiFireTimer = Timer(1)

        fun getAbsorptionRemainder(): Int {
            val s = Varpbits.varpbit(1067) and 0x7fff
            return if (s > 0) s / 32 else 0
        }

        fun isProtectedFromFire(): Boolean {
            return !antiFireTimer.isFinished()
        }
    }


}
