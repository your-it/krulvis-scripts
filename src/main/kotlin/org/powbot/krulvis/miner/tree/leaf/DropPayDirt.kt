package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Miner

class DropPayDirt(script: Miner) : Leaf<Miner>(script, "Drop pay-dirt") {

    val nearHopper = Tile(3748, 5673, 0)

    override fun execute() {
        val hopper = getHopper()
        val totalPaydirt = Inventory.getCount(Ore.PAY_DIRT.id) + script.getMotherloadCount()
        if (hopper != null) {
            if (hopper.distance() > 3) {
                val path = LocalPathFinder.findPath(nearHopper)
                if (path.isNotEmpty()) {
                    println("Walking to hopper first! pathLength=${path.actions.size}")
                    path.traverse()
                } else {
                    walkWeb()
                }
            } else if (interact(hopper, "Deposit")) {
                script.lastPayDirtDrop = System.currentTimeMillis()
                sleep(600)
                waitFor(long()) { deposited() }
            }
        } else {
            walkWeb()
        }
        if (totalPaydirt >= 81 && deposited()) {
            script.shouldEmptySack = true
        }
    }

    fun walkWeb() {
        if (escapeTopFloor(nearHopper)) {
            Movement.moveTo(nearHopper)
        }
    }

    val topCenter = Tile(3757, 5679, 0)
    val northOfLadder = Tile(3755, 5675, 0)
    val ladderTile = Tile(3755, 5674, 0)
    fun escapeTopFloor(destination: Tile): Boolean {
        val pos = Players.local().tile()
        if (topCenter.distance() <= 8 && LocalPathFinder.findPath(pos, destination, true).isEmpty()) {
            if (northOfLadder.distance() <= 6 && LocalPathFinder.findPath(pos, northOfLadder, true).isNotEmpty()) {
                if (ladderTile.matrix().interact("Climb")) {
                    return waitFor { LocalPathFinder.findPath(Players.local().tile(), destination, true).isNotEmpty() }
                }
            } else {
                script.log.info("Walking to ladder first...")
                Movement.walkTo(northOfLadder)
            }
            return false
        }
        return true
    }

    fun deposited() = Inventory.getCount(Ore.PAY_DIRT.id) == 0

    fun getHopper() = Objects.stream().name("Hopper").action("Deposit").firstOrNull()
}