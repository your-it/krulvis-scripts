package org.powbot.krulvis.miner

import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.script.ScriptProfile
import org.powbot.api.Tile

object Data {
    val TOOLS = intArrayOf(1265, 1267, 1269, 1271, 1273, 1275, 12297, Item.HAMMER)
}


data class MinerProfile(
    val center: Tile = Tile(3725, 5669, 0),
    val radius: Int = 11,
    val dropOres: Boolean = false,
    val fastMining: Boolean = false,
    val hopFromPlayers: Boolean = false,
    val drawRocks: Boolean = false,
    val oreLocations: List<Tile> = listOf(
        Tile(3728, 5674, 0),
        Tile(3727, 5672, 0),
        Tile(3727, 5671, 0),
        Tile(3727, 5670, 0),
        Tile(3727, 5668, 0),
        Tile(3727, 5667, 0),
        Tile(3727, 5666, 0),
        Tile(3728, 5664, 0),
        Tile(3728, 5663, 0),
        Tile(3727, 5661, 0),
        Tile(3728, 5659, 0),
        Tile(3728, 5658, 0),
        Tile(3723, 5662, 0),
        Tile(3723, 5663, 0),
        Tile(3723, 5664, 0),
        Tile(3723, 5666, 0),
        Tile(3723, 5667, 0),
        Tile(3724, 5669, 0),
        Tile(3724, 5670, 0),
        Tile(3725, 5674, 0),
        Tile(3726, 5681, 0),
        Tile(3730, 5679, 0),
        Tile(3730, 5678, 0),
        Tile(3730, 5677, 0),
        Tile(3725, 5675, 0),
    )
) : ScriptProfile