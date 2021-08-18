package org.powbot.krulvis.miner

import com.google.gson.Gson
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.script.ScriptProfile
import org.powbot.api.Tile
import org.powbot.api.script.selectors.GameObjectOption
import com.google.gson.reflect.TypeToken

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

fun main() {
    val gson = Gson()
    val json =
        "[{\"x\":3728,\"y\":5674,\"floor\":0,\"p\":{\"x\":3728,\"y\":5674,\"z\":0}},{\"x\":3727,\"y\":5672,\"floor\":0,\"p\":{\"x\":3727,\"y\":5672,\"z\":0}},{\"x\":3727,\"y\":5671,\"floor\":0,\"p\":{\"x\":3727,\"y\":5671,\"z\":0}},{\"x\":3727,\"y\":5670,\"floor\":0,\"p\":{\"x\":3727,\"y\":5670,\"z\":0}},{\"x\":3727,\"y\":5668,\"floor\":0,\"p\":{\"x\":3727,\"y\":5668,\"z\":0}},{\"x\":3727,\"y\":5667,\"floor\":0,\"p\":{\"x\":3727,\"y\":5667,\"z\":0}},{\"x\":3727,\"y\":5666,\"floor\":0,\"p\":{\"x\":3727,\"y\":5666,\"z\":0}},{\"x\":3728,\"y\":5664,\"floor\":0,\"p\":{\"x\":3728,\"y\":5664,\"z\":0}},{\"x\":3728,\"y\":5663,\"floor\":0,\"p\":{\"x\":3728,\"y\":5663,\"z\":0}},{\"x\":3727,\"y\":5661,\"floor\":0,\"p\":{\"x\":3727,\"y\":5661,\"z\":0}},{\"x\":3728,\"y\":5659,\"floor\":0,\"p\":{\"x\":3728,\"y\":5659,\"z\":0}},{\"x\":3728,\"y\":5658,\"floor\":0,\"p\":{\"x\":3728,\"y\":5658,\"z\":0}},{\"x\":3723,\"y\":5662,\"floor\":0,\"p\":{\"x\":3723,\"y\":5662,\"z\":0}},{\"x\":3723,\"y\":5663,\"floor\":0,\"p\":{\"x\":3723,\"y\":5663,\"z\":0}},{\"x\":3723,\"y\":5664,\"floor\":0,\"p\":{\"x\":3723,\"y\":5664,\"z\":0}},{\"x\":3723,\"y\":5666,\"floor\":0,\"p\":{\"x\":3723,\"y\":5666,\"z\":0}},{\"x\":3723,\"y\":5667,\"floor\":0,\"p\":{\"x\":3723,\"y\":5667,\"z\":0}},{\"x\":3724,\"y\":5669,\"floor\":0,\"p\":{\"x\":3724,\"y\":5669,\"z\":0}},{\"x\":3724,\"y\":5670,\"floor\":0,\"p\":{\"x\":3724,\"y\":5670,\"z\":0}},{\"x\":3725,\"y\":5674,\"floor\":0,\"p\":{\"x\":3725,\"y\":5674,\"z\":0}},{\"x\":3726,\"y\":5681,\"floor\":0,\"p\":{\"x\":3726,\"y\":5681,\"z\":0}},{\"x\":3730,\"y\":5679,\"floor\":0,\"p\":{\"x\":3730,\"y\":5679,\"z\":0}},{\"x\":3730,\"y\":5678,\"floor\":0,\"p\":{\"x\":3730,\"y\":5678,\"z\":0}},{\"x\":3730,\"y\":5677,\"floor\":0,\"p\":{\"x\":3730,\"y\":5677,\"z\":0}},{\"x\":3725,\"y\":5675,\"floor\":0,\"p\":{\"x\":3725,\"y\":5675,\"z\":0}}]"
    val options = gson.fromJson<List<Tile>>(json, object : TypeToken<List<Tile>>() {}.type)
    options.map { GameObjectOption("Rock", it, "") }
    println(gson.toJson(options.map { GameObjectOption("Rock", it, "") }))
}