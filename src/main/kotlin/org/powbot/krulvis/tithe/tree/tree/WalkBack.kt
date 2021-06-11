package org.powbot.krulvis.tithe.tree.tree

import org.powbot.krulvis.api.ATContext.walk
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.TitheFarmer
import org.powerbot.script.Tile

class WalkBack(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Walking back") {
    override fun execute() {

        val topMostTile = script.getCornerPatchTile()
        val walkableTile = Tile(topMostTile.x() + 2, topMostTile.y())
        if (walkableTile.distance() > 1 && walk(walkableTile)) {
            waitFor(long()) { walkableTile.distance() < 5 }
        }

    }
}