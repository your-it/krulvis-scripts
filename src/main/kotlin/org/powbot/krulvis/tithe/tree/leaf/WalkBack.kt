package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.TitheFarmer

class WalkBack(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Walking back") {
    override fun execute() {

        val topMostTile = script.getCornerPatchTile()
        val walkableTile = Tile(topMostTile.x() + 2, topMostTile.y())
        if (walkableTile.distance() > 1 && Movement.step(walkableTile)) {
            waitFor(long()) { walkableTile.distance() < 5 }
        }

    }
}