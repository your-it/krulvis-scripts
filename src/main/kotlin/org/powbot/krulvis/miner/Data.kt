package org.powbot.krulvis.miner

import org.powbot.krulvis.api.script.ScriptProfile
import org.powerbot.script.Tile

object Data {

    val TOOLS = intArrayOf()
}

data class MinerProfile(
    val center: Tile = Tile(0, 0, 0),
    val radius: Int = 15,
    val powermine: Boolean = false,
    val oreLocations: List<Tile> = emptyList()
) : ScriptProfile {

}