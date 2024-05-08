package org.powbot.krulvis.runecrafting

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Objects

const val FOOD_CONFIGURATION = "Food Config"
const val ESSENCE_TYPE_CONFIGURATION = "Essence Config"
const val DAEYALT_ESSENCE = "Daeyalt essence"
const val RUNE_ESSENCE = "Rune Essence"
const val PURE_ESSENCE = "Pure essence"
const val RUNE_ALTAR_CONFIGURATION = "Rune Altar Config"
const val VILE_VIGOUR_CONFIG = "Vile Vigour Config"
const val ZMI_PAYMENT_RUNE_CONFIG = "ZMI Rune Payment Config"
const val ASTRAL = "ASTRAL"
const val EARTH = "EARTH"
const val WATER = "WATER"
const val AIR = "AIR"
const val FIRE = "FIRE"
const val ZMI = "ZMI"

enum class RuneAltar(val tile: Tile, val bankTeleport: Magic.MagicSpell?, val pathToAltar: List<Tile>) {
    ASTRAL(Tile(2158, 3864, 0), Magic.LunarSpell.MOONCLAN_TELEPORT,
            pathToAltar = listOf(Tile(2099, 3919, 0), Tile(2103, 3915, 0), Tile(2109, 3915, 0), Tile(2112, 3910, 0), Tile(2112, 3904, 0), Tile(2113, 3898, 0), Tile(2114, 3892, 0), Tile(2114, 3886, 0), Tile(2117, 3880, 0), Tile(2122, 3876, 0), Tile(2127, 3872, 0), Tile(2132, 3870, 0), Tile(2134, 3865, 0), Tile(2139, 3861, 0), Tile(2145, 3861, 0), Tile(2150, 3863, 0), Tile(2155, 3864, 0))),
    ZMI(Tile(3060, 5579, 0), Magic.LunarSpell.OURANIA_TELEPORT,
            pathToAltar = listOf(Tile(3013, 5624, 0), Tile(3013, 5619, 0), Tile(3013, 5613, 0), Tile(3013, 5607, 0), Tile(3013, 5602, 0), Tile(3015, 5597, 0), Tile(3016, 5592, 0), Tile(3016, 5587, 0), Tile(3016, 5582, 0), Tile(3021, 5579, 0), Tile(3026, 5578, 0), Tile(3031, 5578, 0), Tile(3036, 5581, 0), Tile(3041, 5582, 0), Tile(3046, 5580, 0), Tile(3051, 5579, 0), Tile(3057, 5579, 0)))
    ;

    fun getAltar(): GameObject? = Objects.stream().at(tile).name("Altar").firstOrNull()
}