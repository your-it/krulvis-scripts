package org.powbot.krulvis.orbcharger

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.extensions.items.Staff
import org.powbot.krulvis.api.extensions.magic.RunePower
import org.powbot.krulvis.api.utils.Utils.waitFor

enum class Orb(val id: Int, val obeliskTile: Tile, val spell: Magic.Spell) {
    WATER(571, Tile(2845, 3424, 0), Magic.Spell.CHARGE_WATER_ORB),
    FIRE(569, Tile.Nil, Magic.Spell.CHARGE_FIRE_ORB),
    EARTH(575, Tile.Nil, Magic.Spell.CHARGE_EARTH_ORB),
    AIR(573, Tile.Nil, Magic.Spell.CHARGE_AIR_ORB)
    ;

    fun castOnObelisk(): Boolean {
        if (Magic.magicspell() != spell) {
            if (Magic.cast(spell)) {
                waitFor { Magic.magicspell() == spell }
            }
        }
        return getObelisk()?.interact("Cast") == true
    }

    fun getObelisk(): GameObject? {
        val name = this.name.lowercase().replaceFirstChar { it.uppercase() }
        return Objects.stream().within(10.0).name("Obelisk of $name").firstOrNull()
    }

    fun staffEquipped() = Staff.getEquippedStaff()?.runePowers?.contains(RunePower.valueOf(name)) == true

    companion object {
        val UNPOWERED = 567
        val COSMIC = 564
    }

}