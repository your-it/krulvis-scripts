package org.powbot.krulvis.runecrafter

import org.powbot.krulvis.api.script.ScriptProfile
import org.powerbot.script.Tile

data class RunecrafterProfile(val type: RuneType = RuneType.COSMIC) : ScriptProfile

enum class RuneType(
    val altar: Tile,
    val ruins: Tile,
    val bank: Tile,
    val talisman: Int,
    val tiara: Int,
    val rune: Int
) {
    COSMIC(
        Tile(2142, 4835, 0),
        Tile(2408, 4380, 0),
        Tile(2384, 4458, 0),
        1454, 5539, 564
    )
}

val PURE_ESSENCE = 7936