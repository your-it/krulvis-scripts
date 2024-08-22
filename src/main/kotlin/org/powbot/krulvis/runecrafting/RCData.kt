package org.powbot.krulvis.runecrafting

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.extensions.teleports.CASTLE_WARS_ROD
import org.powbot.krulvis.api.extensions.teleports.FEROX_ENCLAVE_ROD
import org.powbot.krulvis.api.extensions.teleports.MOONCLAN_TELEPORT
import org.powbot.krulvis.api.extensions.teleports.OURANIA_TELEPORT
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.extensions.teleports.poh.openable.FAIRY_RING_DLS

const val FOOD_CONFIGURATION = "Food Config"
const val EAT_AT_CONFIG = "Eat at Config"
const val EQUIPMENT_CONFIG = "Equipment"
const val ESSENCE_TYPE_CONFIGURATION = "Essence Config"
const val DAEYALT_ESSENCE = "Daeyalt essence"
const val RUNE_ESSENCE = "Rune Essence"
const val PURE_ESSENCE = "Pure essence"
const val RUNE_ALTAR_CONFIGURATION = "Rune Altar Config"
const val ALTAR_TELEPORT_CONFIG = "Altar Teleport Config"
const val BANK_TELEPORT_CONFIG = "Bank Teleport Config"
const val METHOD_CONFIGURATION = "Method Config"
const val VILE_VIGOUR_CONFIG = "Vile Vigour Config"
const val ZMI_PAYMENT_RUNE_CONFIG = "ZMI Rune Payment Config"
const val ZMI_PROTECT_CONFIG = "Protection Prayer Config"
const val USE_POUCHES_OPTION = "Pouches"
const val ALTAR = "Altar"
const val ABYSS = "Abyss"
const val ASTRAL = "ASTRAL"
const val MIND = "MIND"
const val EARTH = "EARTH"
const val WATER = "WATER"
const val AIR = "AIR"
const val LAW = "LAW"
const val NATURE = "NATURE"
const val DEATH = "DEATH"
const val CHAOS = "DEATH"
const val BLOOD = "BLOOD"
const val SOUL = "SOUL"
const val COSMIC = "COSMIC"
const val FIRE = "FIRE"
const val ZMI = "ZMI"

val ouraniaPrayerAltarPath = listOf(
    Tile(2471, 3242, 0),
    Tile(2468, 3247, 0),
    Tile(2463, 3249, 0),
    Tile(2457, 3249, 0),
    Tile(2454, 3243, 0),
    Tile(2454, 3238, 0),
    Tile(2454, 3233, 0)
)
val astralAltarPath = listOf(
    Tile(2099, 3919, 0),
    Tile(2103, 3915, 0),
    Tile(2109, 3915, 0),
    Tile(2112, 3910, 0),
    Tile(2112, 3904, 0),
    Tile(2113, 3898, 0),
    Tile(2114, 3892, 0),
    Tile(2114, 3886, 0),
    Tile(2117, 3880, 0),
    Tile(2122, 3876, 0),
    Tile(2127, 3872, 0),
    Tile(2132, 3870, 0),
    Tile(2134, 3865, 0),
    Tile(2139, 3861, 0),
    Tile(2145, 3861, 0),
    Tile(2150, 3863, 0),
    Tile(2155, 3864, 0)
)

val zmiAltarPath = listOf(
    Tile(3013, 5624, 0),
    Tile(3013, 5619, 0),
    Tile(3013, 5613, 0),
    Tile(3013, 5607, 0),
    Tile(3013, 5602, 0),
    Tile(3015, 5597, 0),
    Tile(3016, 5592, 0),
    Tile(3016, 5587, 0),
    Tile(3016, 5582, 0),
    Tile(3021, 5579, 0),
    Tile(3026, 5578, 0),
    Tile(3031, 5578, 0),
    Tile(3036, 5581, 0),
    Tile(3041, 5582, 0),
    Tile(3046, 5580, 0),
    Tile(3051, 5579, 0),
    Tile(3057, 5579, 0)
)

enum class RuneAltar(val altarTeleports: Array<String>, val bankTeleports: Array<String>, val pathToAltar: List<Tile>) {
    COSMIC(arrayOf(), arrayOf(), emptyList()),
    NATURE(arrayOf(), arrayOf(), emptyList()),
    LAW(arrayOf(), arrayOf(), emptyList()),
    CHAOS(arrayOf(), arrayOf(), emptyList()),
    DEATH(arrayOf(), arrayOf(), emptyList()),
    SOUL(arrayOf(), arrayOf(), emptyList()),
    BLOOD(
        arrayOf(FAIRY_RING_DLS),
        arrayOf(CASTLE_WARS_JEWELLERY_BOX, CASTLE_WARS_ROD, FEROX_ENCLAVE_ROD),
        listOf(Tile(3234, 4832, 0))
    ),
    ASTRAL(
        arrayOf(), arrayOf(MOONCLAN_TELEPORT),
        pathToAltar = astralAltarPath
    ),
    ZMI(
        arrayOf(),
        arrayOf(OURANIA_TELEPORT),
        pathToAltar = zmiAltarPath
    )
    ;

    fun atAltar() = pathToAltar.last().distance() < 10
    fun getAltar(): GameObject? =
        Objects.stream(25, GameObject.Type.INTERACTIVE).name("Altar").action("Craft-rune").firstOrNull()
}
