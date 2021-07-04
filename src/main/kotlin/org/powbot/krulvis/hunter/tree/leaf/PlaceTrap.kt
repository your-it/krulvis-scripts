package org.powbot.krulvis.hunter.tree.leaf

import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.hunter.Hunter
import org.powerbot.script.Tile
import java.util.*

class PlaceTrap(script: Hunter) : Leaf<Hunter>(script, "Loot trap") {
    override fun execute() {
        TODO("Not yet implemented")
    }

    fun getEmptySpot(): Optional<Tile> {
        return Optional.empty()
    }
}