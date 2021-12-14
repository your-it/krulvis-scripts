package org.powbot.krulvis.slayer.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.local.Utils.getWalkableNeighbor
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.slayer.Slayer
import org.powbot.mobile.script.ScriptManager

class OpenBank(script: Slayer) : Leaf<Slayer>(script, "Opening bank") {

    override fun execute() {
        val nearest = Bank.nearest()
        if (nearest.tile().distance() > 30 || !nearest.reachable()) {
            ScriptManager.script()?.log?.info(
                "nearest=$nearest, distance=${
                    nearest.tile().distance()
                } is not reachable!"
            )
            val localPath = LocalPathFinder.findPath(nearest.getWalkableNeighbor())
            if (localPath.isNotEmpty()) {
                localPath.traverseUntilReached()
            } else {
                try {
                    Movement.walkTo(nearest)
                } catch (e: Exception) {
                    ScriptManager.script()?.log?.info("Failed to move to bank!")
                    ScriptManager.script()?.log?.info(e.stackTraceToString())
                }
            }
        } else {
            Bank.open()
        }
    }
}