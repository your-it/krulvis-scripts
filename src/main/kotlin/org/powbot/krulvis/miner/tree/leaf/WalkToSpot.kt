package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.WebWalking
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Miner
import org.powbot.mobile.script.ScriptManager

class WalkToSpot(script: Miner) : Leaf<Miner>(script, "Walking to spot") {
    override fun execute() {
        val locs = script.rockLocations
        script.mineDelay.forceFinish()
        if (locs.isEmpty()) {
            script.log.warning("Script requires at least 1 rock location set in the Configuration")
            ScriptManager.stop()
        } else if (script.rockLocations.all { script.inTopFloorAreas(it) }
            && !script.inTopFloorAreas()
            && LocalPathFinder.findPath(Players.local().tile(), script.northOfLadder, true).isEmpty()) {
            script.log.info("Climbing ladder manually")
            val ladderGoingUp = Objects.stream().at(Tile(3755, 5673, 0)).name("Ladder").firstOrNull()
            if (ladderGoingUp != null && Utils.walkAndInteract(ladderGoingUp, "Climb")) {
                waitFor { script.northOfLadder.distance() <= 1 }
            }
        } else {
            val loc = locs.minByOrNull { it.distance() }
            if (loc != null)
                WebWalking.moveTo(
                    loc,
                    false,
                    { script.getMotherloadCount() >= 82 },
                    5,
                    90,
                    script.inTopFloorAreas()
                )
        }
    }
}