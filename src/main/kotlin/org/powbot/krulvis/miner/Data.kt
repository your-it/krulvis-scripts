package org.powbot.krulvis.miner

import org.powbot.krulvis.api.script.ScriptProfile
import org.powerbot.script.Tile

object Data {
    val TOOLS = intArrayOf(1265, 1267, 1269, 1271, 1273, 1275, 12297)
}


data class MinerProfile(
    val center: Tile = Tile(0, 0, 0),
    val radius: Int = 15,
    val dropOres: Boolean = false,
    val fastMining: Boolean = false,
    val oreLocations: List<Tile> = emptyList()
) : ScriptProfile