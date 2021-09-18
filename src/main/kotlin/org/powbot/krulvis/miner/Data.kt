package org.powbot.krulvis.miner

import org.powbot.api.Area
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.api.Tile

object Data {
    val TOOLS = intArrayOf(1265, 1267, 1269, 1271, 1273, 1275, 12297, Item.HAMMER, 11920, 12797, 13243, 13244)

    //Motherlode upper floor areas
    val TOP_AREA = Area(Tile(3761, 5673), Tile(3764, 5657))
    val TOP_AREA_NORTH = Area(Tile(3748, 5685), Tile(3766, 5673))
}